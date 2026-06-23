# Calvera

AI-powered GitHub PR review tool. Calvera listens to GitHub `pull_request` webhooks, analyzes diffs with an LLM, posts a review with a summary and bug findings, and lets you chat with the analysis.

## Stack

- **Backend** — Spring Boot 3 (Java 21, virtual threads), Spring Modulith, Spring AI, Flyway, pgvector
- **Frontend** — React 19, Vite, Tailwind CSS, shadcn/ui
- **Infra** — PostgreSQL (pgvector), Redis, GitHub OAuth2

## How it works

1. A GitHub `pull_request` webhook fires on `opened` / `synchronize`
2. Calvera fetches the diff and queues an analysis job (Redis)
3. An LLM analyzes the diff, generates a summary and bug findings, and posts a GitHub PR review
4. The review and conversation are stored with vector embeddings for semantic chat
5. Users can chat with any review via a streaming SSE endpoint

## Prerequisites

- Java 21
- Node 20+
- Docker (for Postgres + Redis)
- GitHub OAuth App
- GitHub personal access token (for posting reviews)
- OpenAI-compatible LLM endpoint

## Setup

**1. Start dependencies**

```bash
docker compose up postgres redis -d
```

**2. Configure environment**

Create a `.env` file (used by Docker Compose and the backend):

```env
DB_USERNAME=calvera
DB_PASSWORD=yourpassword

GITHUB_CLIENT_ID=your_github_oauth_client_id
GITHUB_CLIENT_SECRET=your_github_oauth_client_secret
GITHUB_TOKEN=your_github_pat

JWT_SECRET_KEY=at_least_32_char_secret

LLM_API_KEY=your_openai_api_key
LLM_BASE_URL=https://api.openai.com   # or any OpenAI-compatible base URL

FRONTEND_URL=http://localhost:5173
```

**3. Run the backend**

```bash
./mvnw spring-boot:run
```

**4. Run the frontend**

```bash
cd frontend
npm install
npm run dev
```

Frontend at `http://localhost:5173`, backend at `http://localhost:8080`.

## GitHub webhook

Point your repo's webhook at `https://<your-host>/api/webhook/github` with content type `application/json` and the `pull_request` event selected.

## Docker (full stack)

```bash
docker compose up --build
```

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/reviews` | List all reviews |
| GET | `/api/reviews/:id` | Get a review |
| GET | `/api/reviews/:id/messages` | Get chat history |
| GET | `/api/reviews/:id/chat/stream` | Stream a chat response (SSE) |
| POST | `/api/reviews/:id/chat` | Send a chat message |
| GET | `/api/projects` | List projects |
| POST | `/api/projects` | Create a project |
| GET | `/api/user/me` | Get current user |
| POST | `/api/auth/refresh` | Refresh JWT tokens |
