# DocFlow RAG Microservice

A retrieval-augmented generation (RAG) service built with FastAPI, powering the "Ask Your Documents" feature of DocFlow. Handles document ingestion, chunking, embedding, vector search, and answer generation. Built from scratch — no LangChain or LlamaIndex — so every stage of the pipeline (chunking, retrieval, prompt construction) is custom code.

This service is internal-only: it is called exclusively by the Spring Boot backend, which owns authentication and the client-facing API. The RAG service trusts the identifiers it's given and has no auth layer of its own.

## Pipeline

### Ingestion (on file upload)

1. Spring Boot uploads the file to S3 and stores metadata (including the S3 key) in MongoDB.
2. Spring Boot calls this service's upload endpoint with `user_id`, `file_id`, `s3_key`, `file_name`.
3. The service downloads the file from S3 and extracts text (`pdfplumber`).
4. Text is split using a custom `RecursiveChunker` (chunk_size=509, overlap=90, separators=`["\n\n", "\n", ". ", " ", ""]`), with a merge step so small leftover pieces get folded into neighboring chunks instead of becoming their own tiny fragments.
5. Chunks are embedded with Sentence Transformers (`all-MiniLM-L6-v2`).
6. Embeddings are stored in a separate `chunks` collection in MongoDB Atlas, keyed by `file_id` and `user_id`.

Ingestion runs asynchronously via FastAPI `BackgroundTasks` — the upload endpoint returns immediately, and per-file status (`EmbeddingStatus: completed / failed`) is synced back to the file's metadata.

### Retrieval + Generation (on query)

`GET /documents/query?user_id&question&top_k`

1. The query is embedded with the same MiniLM model.
2. MongoDB Atlas `$vectorSearch` (index: `Docflow_index`) retrieves the top-k most similar chunks, filtered by `user_id` at the index level so no user can ever retrieve another user's chunks.
3. Retrieved chunks are assembled into a prompt.
4. The LLM (Groq `openai/gpt-oss-120b`, previously Gemini `gemini-2.5-flash`) generates the answer.
5. The response includes the answer plus its source chunks (filename, matched text, similarity score) for transparency.

## Evaluation

The pipeline is evaluated with [RAGAS](https://github.com/GoldSharon/ragas) (community fork, installed from GitHub — the official package hit a `google-vertexai` module error in this setup).

### Testset generation

- A synthetic testset (question + reference answer pairs) is generated from the source PDFs rather than hand-written, using RAGAS's testset generator.
- Source documents: 5–7 OS/DSA lecture-slide PDFs — fairly shallow, bullet-point content rather than detailed prose. This matters for interpreting the results below.
- RAGAS's default `HeadlinesExtractor` transform crashed on these PDFs (they lack clear section headings), so headline-based transforms were filtered out of `default_transforms` and the knowledge graph (`DOCUMENT`/`CHUNK` nodes + parent-child relationships) was built manually instead.
- Embeddings for testset similarity linking use the same MiniLM model as production (not the generation LLM's embeddings), so evaluation stays consistent with what's actually deployed.
- Testset size: 20–30 questions.

### Metrics

RAGAS metrics are computed by manually instantiating each metric class (e.g. `AnswerRelevancy(strictness=1)`) rather than importing the default instances, since Groq only supports `n=1`.

Latest run:

| Metric | Score |
|--------|-------|
| Faithfulness | 0.795 |
| Answer Relevancy | 0.572 |
| Context Precision | 0.679 |
| Context Recall | 0.532 |

### Interpreting the numbers

- **Answer relevancy** is the strongest signal here — the model's answers are on-topic and address the question asked.
- **Context precision/recall** are lower mainly because the source PDFs are shallow (slide titles with little explanatory text), so there's often not enough substantive content in any single chunk for retrieval to score highly against reference answers. This was confirmed by manually inspecting failing cases.
- Some of the variance across earlier runs also came from testset issues (one mismatched question/reference pair) and generation-side judge-call failures during LLM provider migration (Gemini → Groq), both since addressed.
- **Planned next steps**: author supplementary paragraphs covering identified content gaps (e.g. Process ID, primary/secondary storage, load balancing) and re-run to see how much of the precision/recall gap closes once source content is less shallow; apply a retrieval de-duplication fix (fetch `top_k * 3` via `$vectorSearch`, de-dupe by chunk text, trim to `top_k` — implemented but not yet re-benchmarked).

## Setup

Two separate environments are used — production and evaluation have different dependency needs.

**Production (Python 3.14)**

```
pip install -r requirements.txt
```

Environment variables (see `.env.example`): MongoDB Atlas connection string, AWS S3 credentials, Groq API key.

**Evaluation / RAGAS (Python 3.11 venv, separate from production)**

```
python -m venv ragas-env
ragas-env\Scripts\activate
pip install -r eval/requirements.txt
```

RAGAS is installed from the community fork (not PyPI):

```
pip install git+https://github.com/GoldSharon/ragas.git
```

### Running

```
uvicorn main:app --reload
```

### Running an evaluation

```
python eval/testset_generation.py   # generates the synthetic testset
python eval/evaluation.py           # runs RAGAS metrics against the testset
```

## Why Python for the RAG layer

Java has limited first-class support for the ML tooling this needed — embedding models, vector operations, PDF/text processing libraries. Python's ecosystem (Sentence Transformers, PyMongo, pdfplumber) made building the pipeline from scratch far more direct, so this was built as an independent service and integrated with the Java backend over REST rather than forced into the existing Spring Boot codebase.
