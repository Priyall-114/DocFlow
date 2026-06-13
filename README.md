A secure, production-ready backend REST API for file and folder management built with Spring Boot. Supports user authentication, storage quotas, cloud file storage (AWS S3), and payment-based storage upgrades via Razorpay.

## Tech Stack

| Category | Technologies |
|----------|--------------|
| Backend | Spring Boot, Java |
| Database | MongoDB Atlas |
| Authentication | JWT (1-hour expiry + refresh token rotation) |
| File Storage | AWS S3 |
| Deployment | AWS Elastic Beanstalk |
| Payments | Razorpay (webhook signature verification) |
| API Docs | Swagger/OpenAPI |

## Features

### Core File Management
-  Complete CRUD operations for files and folders
-  Inline file viewing via dynamic MIME type detection
-  HTTP content streaming for large files
-  Automatic file organization with status tracking:
  - `PENDING` — Unorganized files routed to default "Random" folder
  - `ORGANIZED` — Manually placed files with custom folder structure

### Authentication & Security
-  JWT authentication (1-hour access token expiry)
-  Refresh token rotation for secure session management
-  Role-based access control (RBAC) on all endpoints

### Storage Management
-  Default 100MB quota per user
-  Real-time quota enforcement on every upload
-  Storage plan upgrades via Razorpay:
  - **Basic** — +2GB (₹49)
  - **Pro** — +5GB (₹199)
-  Webhook signature verification to prevent fake callbacks

### AWS Integration
-  File storage migrated to AWS S3
-  Folder-level S3 key structuring (`user-id/folder-id/file-name`)
-  Automatic MongoDB metadata sync on upload, rename, move, and delete

### API Documentation
-  Publicly accessible live API
-  Verified via Postman (15+ endpoints, 50+ test cases)
-  Swagger UI for interactive API exploration


## Live
http://DocFlow-env.eba-df4i7vtr.ap-south-1.elasticbeanstalk.com
