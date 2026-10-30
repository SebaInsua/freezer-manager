# Freezer Manager

Freezer Manager is a REST API designed to help households manage food purchases, freezer inventory, and meal preparation.

The project focuses on inventory management, food tracking, batch control, and freezer capacity monitoring.

## Project Goals

- Track food purchases and quantities.
- Manage inventory by batches.
- Register food consumption and meal preparation.
- Estimate freezer capacity and available space.
- Keep a history of inventory movements.

## Tech Stack

- **Java 21**
- **Spring Boot**
- **Spring Data JPA / Hibernate**
- **Spring Security**
- **JWT Authentication**
- **PostgreSQL**
- **Flyway**
- **Maven**
- **Docker & Docker Compose**
- **Swagger / OpenAPI**

## Requirements

To run the application using Docker, you need:

- Docker
- Docker Compose

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/SebaInsua/freezer-manager.git
cd freezer-manager
```

### 2. Configure environment variables

Create your local environment file from the example:

```bash
cp .env.example .env
```

Review the `.env` file and configure the database credentials and JWT secret.

Generate a secure Base64-encoded JWT secret with:

```bash
openssl rand -base64 32
```

Copy the generated value into the `JWT_SECRET` variable in your `.env` file.

**Important:** Never commit your `.env` file or expose production credentials.

### 3. Build and start the application

```bash
docker compose up -d --build
```

This command builds the API image and starts the application together with PostgreSQL.

PostgreSQL uses a persistent Docker volume to preserve database data between container restarts.

The API waits for PostgreSQL to become healthy before starting.

### 4. Verify the application

Check the container status:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs -f api
```

The API is available at:

```text
http://localhost:8080
```

Swagger UI documentation:

```text
http://localhost:8080/swagger-ui/index.html
```

Protected endpoints require a valid JWT access token. Use the **Authorize** button in Swagger UI to provide your bearer token.

### 5. Stop the application

```bash
docker compose down
```

This stops and removes the containers while preserving the PostgreSQL data volume.

To remove the database volume as well:

```bash
docker compose down -v
```

**Warning:** This permanently deletes the persisted PostgreSQL data.

## Running Tests

The standard test profile uses an H2 in-memory database, allowing most tests to run without starting PostgreSQL or Docker.

Run the test suite with:

```bash
./mvnw test
```

On Windows Command Prompt:

```cmd
mvnw.cmd test
```

The project also includes PostgreSQL integration tests using a separate PostgreSQL test database. These tests require the PostgreSQL test instance to be running and configured for the `postgres-test` profile.

## Database Migrations

Database schema changes are managed with Flyway.

Migrations are applied automatically when the application starts with the Docker profile.

## Project Status

Currently in development. Features are being implemented progressively.
