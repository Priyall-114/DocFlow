# DocFlow

A secure, production-ready file and folder management platform with an integrated AI layer — built with a Spring Boot backend and a Python RAG microservice. Users can upload, organize,search, and manage files with storage quotas and payment-based upgrades, and can also ask natural-language questions about their documents and get AI-generated answers grounded in their own files.

## System Overview

DocFlow is composed of two independently running services:

- **Backend (Java / Spring Boot)** — handles authentication, file/folder CRUD, storage quotas, S3 storage, and payments. This is the single entry point for all clients.
- **RAG Service (Python / FastAPI)** — handles document ingestion (chunking, embedding, vector storage) and retrieval-augmented question answering. It is internal-only and is only ever called by the Spring Boot backend, never directly by a client.

```
Client → Spring Boot (auth, files, payments)
              ├── AWS S3 (file storage)
              ├── MongoDB Atlas (metadata)
              ├── Razorpay (payments)
              └── DocFlow RAG Service (Python) — triggered on upload / query
                        ├── MongoDB Atlas Vector Search (chunks + embeddings)
                        └── Google Gemini (answer generation)
```

All authentication is centralized in Spring Boot. The RAG service has no auth layer of its own — the trust boundary is enforced upstream, so the Python service stays lightweight and there's no duplicated security logic.

## Tech Stack

| Category | Technologies |
|----------|--------------|
| Backend | Spring Boot, Java |
| AI / RAG Service | Python, FastAPI |
| Database | MongoDB Atlas (documents + Vector Search) |
| Authentication | JWT (1-hour expiry + refresh token rotation) |
| File Storage | AWS S3 |
| Embeddings | Sentence Transformers (`all-MiniLM-L6-v2`) |
| LLM | Google Gemini (`gemini-2.5-flash`) |
| PDF Processing | pdfplumber |
| Payments | Razorpay (webhook signature verification) |
| Deployment | AWS Elastic Beanstalk |
| API Docs | Swagger/OpenAPI |

## Features

### Core File Management
- Complete CRUD operations for files and folders
- Inline file viewing via dynamic MIME type detection
- HTTP content streaming for large files
- Automatic file organization with status tracking:
  - `PENDING` — Unorganized files routed to default "Random" folder
  - `ORGANIZED` — Manually placed files with custom folder structure

### Authentication & Security
- JWT authentication (1-hour access token expiry)
- Refresh token rotation for secure session management
- Role-based access control (RBAC) on all endpoints

### Storage Management
- Default 100MB quota per user
- Real-time quota enforcement on every upload
- Storage plan upgrades via Razorpay:
  - **Basic** — +2GB (₹49)
  - **Pro** — +5GB (₹199)
- Webhook signature verification to prevent fake callbacks

### AWS Integration
- File storage on AWS S3
- Folder-level S3 key structuring (`user-id/folder-id/file-name`)
- Automatic MongoDB metadata sync on upload, rename, move, and delete

### AI / RAG — Ask Your Documents
- Ask natural-language questions about any uploaded document and get a grounded, context-aware answer
- **Ingestion pipeline**: S3 download → PDF text extraction → custom recursive chunking (built from scratch, no LangChain/LlamaIndex) → batch embedding → vector storage in MongoDB Atlas
- **Retrieval pipeline**: query embedding → MongoDB Atlas Vector Search (HNSW-based ANN) → top-k relevant chunks → Gemini generates the final answer using retrieved context
- **User-level isolation** — vector search is filtered by `user_id` at the index level, so no user can ever retrieve another user's document chunks
- Non-blocking ingestion via background tasks — file uploads return immediately while chunking/embedding continues asynchronously
- Per-file processing status (`EmbeddingStatus: completed / failed`) synced back to the file's metadata so the frontend can reflect real-time state
- Answers are returned along with their source chunks (filename, matched text, similarity score) for transparency

### API Documentation
- Verified via Postman (12+ endpoints)
- Swagger UI for interactive API exploration

## Repository Structure

```
DocFlow/
├── backend-java/          Spring Boot backend (auth, files, payments)
└── rag-service-python/    FastAPI RAG microservice (ingestion, retrieval, generation)
```

Each service has its own README with setup instructions specific to that stack.

## Why a Separate Python Service Instead of Doing RAG in Java

Java has limited first-class support for the ML tooling this needed (embedding models, vector operations), while Python's ecosystem (Sentence Transformers, PyMongo, PDF libraries) made building the pipeline from scratch far more direct. Rather than forcing RAG into the existing Spring Boot codebase, it was built as an independent microservice and integrated over REST — keeping the Java backend focused on what it already does well, and letting the RAG service evolve independently (e.g. swapping embedding models or LLM providers) without touching the core platform.

