# Freezer Manager

Freezer Manager is a REST API designed to help households manage food
purchases, freezer inventory, and food consumption.

The application provides inventory tracking, batch management, storage
capacity monitoring, and a history of stock movements. It also includes
user authentication and authorization to ensure that each user can
access only their own data.

## Project Goals

-   Track food purchases and quantities.
-   Manage inventory through batches.
-   Register food entries and consumption.
-   Monitor freezer capacity and available space.
-   Keep a history of inventory movements.
-   Provide secure, user-specific inventory management.

## Features

-   **User Authentication:** User registration and login using JWT.
-   **Password Security:** Passwords are securely hashed using BCrypt.
-   **User Data Isolation:** Users can only access their own resources.
-   **Inventory Management:** Manage food items, storage locations, and
    quantities.
-   **Batch Tracking:** Track inventory quantities and costs by batch.
-   **Movement History:** Review food entries and exits with filtering
    and pagination.
-   **Capacity Monitoring:** Estimate freezer capacity and available
    storage space.
-   **Concurrency Control:** Protect inventory operations using database
    locking to prevent inconsistent stock updates.
-   **Pagination and Filtering:** Retrieve large collections
    efficiently.

## Tech Stack

-   Java 21
-   Spring Boot 4.0.8
-   Spring Data JPA / Hibernate
-   Spring Security
-   JWT Authentication
-   PostgreSQL 17
-   Flyway
-   Maven
-   Docker & Docker Compose
-   Swagger / OpenAPI
-   JUnit and Mockito

## Requirements

To run the application using Docker, you need:

-   Docker
-   Docker Compose

## Getting Started

### 1. Clone the repository

``` bash
git clone https://github.com/SebaInsua/freezer-manager.git
cd freezer-manager
```

### 2. Configure environment variables

Create your local environment file from the example:

``` bash
cp .env.example .env
```

Review the `.env` file and configure the database credentials and JWT
secret.

Generate a secure Base64-encoded JWT secret with:

``` bash
openssl rand -base64 32
```

Copy the generated value into the `JWT_SECRET` variable in your `.env`
file.

**Important:** Never commit your `.env` file or expose production
credentials.

### 3. Build and start the application

``` bash
docker compose up -d --build
```

This command builds the API image and starts the application together
with PostgreSQL.

PostgreSQL uses a persistent Docker volume to preserve database data
between container restarts.

The API waits for PostgreSQL to become healthy before starting.

### 4. Verify the application

Check the container status:

``` bash
docker compose ps
```

View application logs:

``` bash
docker compose logs -f api
```

The API is available at:

``` text
http://localhost:8080
```

Swagger UI documentation:

``` text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

``` text
http://localhost:8080/v3/api-docs
```

Protected endpoints require a valid JWT access token. Use the
**Authorize** button in Swagger UI to provide your bearer token.

## API Overview

The API uses the `/api/v1` base path.

Main resource endpoints:

  Resource                Endpoint
  ----------------------- -------------------------------
  Authentication          `/api/v1/auth`
  Foods                   `/api/v1/foods`
  Storages                `/api/v1/storages`
  Storage items           `/api/v1/storageitems`
  Storage batches         `/api/v1/storagebatches`
  Storage entries         `/api/v1/storagesentry`
  Storage entry details   `/api/v1/storage-entrydetail`
  Storage exits           `/api/v1/storagesexit`
  Movement history        `/api/v1/movements`

The API provides resource operations for managing foods, storage
locations, inventory entries, exits, batches, and movement history.

Refer to Swagger UI for the complete endpoint definitions, request
schemas, response schemas, and available operations.

## Authentication

Freezer Manager uses JWT-based authentication.

### Register a user

**Endpoint:** `POST /api/v1/auth/register`

Request:

``` json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

Successful response: `201 Created`

``` json
{
  "token": "your-jwt-token",
  "tokenType": "Bearer"
}
```

### Login

**Endpoint:** `POST /api/v1/auth/login`

Request:

``` json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

Successful response: `200 OK`

``` json
{
  "token": "your-jwt-token",
  "tokenType": "Bearer"
}
```

### Access protected endpoints

Include the JWT in the `Authorization` header:

``` bash
curl http://localhost:8080/api/v1/foods \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Pagination and Filtering

Collection endpoints support pagination using Spring Data pagination
parameters.

Common parameters include:

  Parameter   Description
  ----------- --------------------------------------------
  `page`      Page number, starting at 0
  `size`      Number of elements per page
  `sort`      Sorting criteria, using Spring Data syntax

Example:

``` http
GET /api/v1/foods?page=0&size=10
```

Paginated responses include the content and page metadata.

Example response structure:

``` json
{
  "content": [],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

The exact response also includes additional pagination metadata provided
by Spring Data.

### Movement History

**Endpoint:** `GET /api/v1/movements`

The movement history provides a unified view of inventory entries and
exits belonging to the authenticated user.

Available optional filters:

  Parameter     Description                        Example
  ------------- ---------------------------------- -----------------------
  `type`        Movement type: `ENTRY` or `EXIT`   `ENTRY`
  `storageId`   Storage identifier                 `1`
  `foodId`      Food identifier                    `5`
  `fromDate`    Start date and time                `2026-10-01T00:00:00`
  `toDate`      End date and time                  `2026-10-31T23:59:59`

Dates use the ISO 8601 date-time format.

Example request:

``` http
GET /api/v1/movements?type=ENTRY&storageId=1&fromDate=2026-10-01T00:00:00&toDate=2026-10-31T23:59:59&page=0&size=10
```

Movement results are ordered chronologically in descending order.

## Error Handling

The API uses a centralized exception handler to provide structured error
responses.

Example:

``` json
{
  "message": "Email o contraseña incorrectos.",
  "status": 401
}
```

Main HTTP status codes:

  Status   Description
  -------- --------------------------------------------------
  400      Invalid request or validation error
  401      Authentication failed or authentication required
  403      Access denied
  404      Requested resource not found
  409      Resource already exists
  500      Unexpected internal server error

## Database Migrations

Database schema changes are managed with Flyway.

Migrations are applied automatically when the application starts with
the development or Docker profile.

Current migrations:

  Migration   Description
  ----------- --------------------------------------------------
  V1          Initial database schema
  V2          Add users and inventory batches
  V3          Set the default active state for storage batches

Hibernate uses schema validation (`ddl-auto=validate`) to verify that
the entity mappings match the database schema.

## Stopping the Application

To stop and remove the containers:

``` bash
docker compose down
```

This preserves the PostgreSQL data volume.

To remove the database volume as well:

``` bash
docker compose down -v
```

**Warning:** This permanently deletes the persisted PostgreSQL data.

## Running Tests

The project includes automated unit and integration tests.

### Standard Test Suite

The standard test profile uses an H2 in-memory database, allowing most
tests to run without PostgreSQL or Docker.

Run the test suite with:

``` bash
./mvnw clean test
```

On Windows Command Prompt:

``` cmd
mvnw.cmd clean test
```

### PostgreSQL Integration Tests

The project also includes PostgreSQL integration tests, including
database connectivity and concurrent inventory operations.

These tests use a dedicated PostgreSQL test database running through
Docker Compose.

Start the test database:

``` bash
docker compose --profile testing up -d postgres-test
```

Wait until the PostgreSQL test container reports a healthy status:

``` bash
docker compose ps
```

Then run the tests:

``` bash
./mvnw test
```

The tests annotated with `@ActiveProfiles("postgres-test")`
automatically activate the PostgreSQL test configuration.

Stop the test database when finished:

``` bash
docker compose --profile testing stop postgres-test
```

The test database uses a separate persistent volume from the main
application database.

## Continuous Integration

The project includes a GitHub Actions workflow that validates the
application through automated tests and Docker Compose.

The workflow checks the PostgreSQL test environment, runs the Maven test
suite, builds the application containers, and performs basic API
authentication and endpoint checks.

## Project Status

Freezer Manager is an ongoing backend portfolio project.

The current MVP includes:

-   User registration and JWT authentication.
-   User-specific data access.
-   Food and storage management.
-   Inventory batches and stock control.
-   Entry and exit movement history.
-   Freezer capacity monitoring.
-   Pagination and filtering.
-   Concurrency protection.
-   Docker-based deployment and database migrations.
-   Automated tests and CI validation.

Further improvements and features are planned as the project evolves.
