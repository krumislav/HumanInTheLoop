# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Build (compile + package):**
```bash
./mvnw package
```

**Run tests:**
```bash
./mvnw test
```

**Run a single test class:**
```bash
./mvnw test -Dtest=HumanInTheLoopLlmApplicationTests
```

**Database setup (Docker — run once):**
```bash
docker run --name hitl-postgres \
  -e POSTGRES_DB=HumanInTheLoop \
  -e POSTGRES_USER=sa \
  -e POSTGRES_PASSWORD=1234 \
  -p 5432:5432 \
  -d postgres:16
```

**Start the database (subsequent runs):**
```bash
docker start hitl-postgres
```

## Configuration

Copy `src/main/resources/application.properties.example` to `application.properties` and fill in:
- `openrouter.api.key` — API key from [openrouter.ai](https://openrouter.ai)
- `spring.datasource.password` — PostgreSQL password (default: `1234`)

## Architecture

This is a **Spring Boot 4 / Java 17** web app implementing a Human-in-the-Loop LLM review workflow. Users ask questions, an AI answers via OpenRouter, and users approve/reject/correct responses. All feedback is persisted in PostgreSQL.

### Authentication
JWT-based stateless auth. `JwtAuthenticationFilter` intercepts requests and sets `SecurityContext`. The `SecurityConfig` is currently permissive (most routes use `.anyRequest().permitAll()`). `/api/dashboard/**` requires `ROLE_ADMIN`. The `User` entity implements Spring Security's `UserDetails` indirectly via `UserDetailsServiceImpl`.

### Data model
- `ChatSession` — groups messages per user, auto-titled from first message
- `ChatMessage` — one user prompt + AI response + `ReviewStatus` (PENDING / APPROVED / REJECTED / CORRECTED) + optional `correctedResponse`
- `ScientificPaper` + `PaperChunk` — uploaded PDFs split into 500-char chunks for RAG context injection
- `ModelAi` — AI model registry seeded at startup by `ModelsInit`

### RAG (Retrieval-Augmented Generation)
There are **two parallel implementations** of the RAG pipeline:
1. `ChatServiceImpl` — keyword search via `PaperChunkRepository.findAll()` + in-memory filtering; uses `RestTemplate` directly; limited to 2 chunks / 300 chars each
2. `OpenRouterServiceImpl` — uses `ScientificPaperService.searchChunks()` (DB-level `LIKE` query via `findByContentContainingIgnoreCase`); uses `RestClient`; returns up to 5 chunks with full content; includes paper title/author/year attribution in the system prompt

`ChatController` wires to `ChatService` (→ `ChatServiceImpl`). `OpenRouterServiceImpl` is injected separately and not currently used by any controller endpoint.

### Web layer
- `WebController` — serves Thymeleaf pages: `/`, `/login`, `/register`, `/menu`, `/home`, `/papers`, `/dashboard`
- `ChatController` — handles `/chat` GET and POST actions (send, approve, reject, correct, rename session)
- `AuthRestController` — `/api/auth/register` and `/api/auth/login`, returns JWT in `AuthResponse`
- `DashboardRestController` — `/api/dashboard/stats`, returns `DashboardStats` (counts by status), requires ADMIN role
- `ScientificPaperController` — paper upload and delete

### Key packages
```
config/      — Security, RestClient bean, ModelsInit (seeds AI models on startup), DataInitializer
security/    — JwtUtil, JwtAuthenticationFilter
model/       — JPA entities (all use Lombok @Data)
repository/  — Spring Data JPA interfaces
service/     — interfaces + impl/ with business logic
web/
  controller/ — Thymeleaf MVC controllers
  rest/       — JSON REST endpoints
  dto/        — LoginRequest, RegisterRequest, AuthResponse, DashboardStats
enums/        — ReviewStatus, Role
```
