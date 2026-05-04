# Human-in-the-Loop LLM Monitoring Platform

A Spring Boot web application where users ask questions to an AI model, review the answers, and approve, reject, or correct them. All feedback is stored in a PostgreSQL database.

---

## Prerequisites

Make sure you have the following installed before starting:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Java 17](https://www.oracle.com/java/technologies/downloads/#java17) (or use the one bundled with IntelliJ/VS Code)
- [VS Code](https://code.visualstudio.com/) or [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- Maven (included in the project via `mvnw`)

---

## Step 1 — Start Docker Desktop

Open Docker Desktop and make sure it is running (whale icon in menu bar on Mac).

---

## Step 2 — Set up the PostgreSQL database

Open a terminal and run this command to create the database container:

```bash
docker run --name hitl-postgres \
  -e POSTGRES_DB=HumanInTheLoop \
  -e POSTGRES_USER=sa \
  -e POSTGRES_PASSWORD=1234 \
  -p 5432:5432 \
  -d postgres:16
```

This creates a PostgreSQL database with the correct name and credentials. You only need to run this **once**. After that, just start it with:

```bash
docker start hitl-postgres
```

To verify it is running:

```bash
docker ps
```

You should see `hitl-postgres` in the list with status `Up`.

---

## Step 3 — Load the shared database data

A database backup file `backup.sql` is included in this project. It contains all the existing data (chat messages, approved answers, etc.) so everyone on the team starts with the same data.

Run this command to import it:

```bash
docker exec -i hitl-postgres psql -U sa -d HumanInTheLoop < backup.sql
```

> If you get a permission error on Mac/Linux, make sure you are in the root folder of the project where `backup.sql` is located.

---

## Step 4 — Configure the API key

Open `src/main/resources/application.properties` and replace the placeholder with your OpenRouter API key:

```properties
openrouter.api.key=your-actual-api-key-here
```

To get a free API key:
1. Go to [openrouter.ai](https://openrouter.ai)
2. Sign up for free
3. Go to **Keys** → Create a new key
4. Copy and paste it into `application.properties`

---

## Step 5 — Run the application

On Mac/Linux:
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

On Windows:
```bash
mvnw.cmd spring-boot:run
```

Wait for this message in the terminal:
```
Started HumanInTheLoopLlmApplication in X seconds
```

---

## Step 6 — Open the app

Go to your browser and open:
```
http://localhost:8080
```

---

## Connecting to the database visually (optional)

You can inspect the database using **DataGrip**, **TablePlus**, or any PostgreSQL client:

```
Host:     localhost
Port:     5432
Database: HumanInTheLoop
User:     sa
Password: 1234
```

---

## Useful Docker commands

```bash
# Check if the container is running
docker ps

# Start the database (after first setup)
docker start hitl-postgres

# Stop the database
docker stop hitl-postgres

# View database logs
docker logs hitl-postgres
```

---

## Export your own database backup (for teammates)

If you want to share your updated data with teammates, run:

```bash
docker exec hitl-postgres pg_dump -U sa HumanInTheLoop > backup.sql
```

Then commit and push `backup.sql` to the shared repository.

---

## Project structure

```
src/main/java/mk/ukim/finki/humanintheloopllm/
  config/
    ModelsInit.java         — list of available AI models
    RestClientConfig.java   — HTTP client configuration
    SecurityConfig.java     — security and access rules
  controller/
    ChatController.java     — handles chat and review actions
  enums/
    ReviewStatus.java       — PENDING, APPROVED, REJECTED, CORRECTED
  model/
    ChatMessage.java        — stores questions, answers, status
    ModelAi.java            — AI model name and ID
  repository/
    ChatMessageRepository.java
  service/
    ChatService.java        — interface
    OpenRouterService.java  — interface
    impl/
      ChatServiceImpl.java
      OpenRouterServiceImpl.java  — makes API calls to OpenRouter

src/main/resources/
  templates/
    chat.html               — main chat UI
  static/css/
    chat.css                — styles
  application.properties    — database and API configuration
```

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.0.6 |
| Database | PostgreSQL 16 (via Docker) |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security |
| Frontend | Thymeleaf + CSS |
| AI Provider | OpenRouter API |
| Build tool | Maven |
| Java | 17 |