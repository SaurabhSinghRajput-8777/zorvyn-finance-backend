# Project Zorvyn: Finance Data Processing Backend

A secure, RBAC-enabled Finance Data Processing backend designed to handle transaction processing with clear auditability, security standards, and comprehensive analytics.

---

## 📌 How Assignment Requirements Are Addressed

- **User & Role Management:** Handled in `/api/v1/users`. Multi-level RBAC is enforced via Spring Security `@PreAuthorize`.
- **Financial Records:** Full CRUD APIs implemented in the `/api/v1/transactions` domain.
- **Dashboard APIs:** Aggregated data is available via `/api/v1/analytics/summary` and `/api/v1/analytics/recent`.
- **Access Control:** Implemented using Spring Security with JWT-based authentication and method-level RBAC (`@PreAuthorize`). Viewers cannot edit; Analysts can view; Admins have full control.
- **Validation:** Handled via Jakarta Validation (`@Valid`) and a `GlobalExceptionHandler` returning standardized error wrappers.
- **Data Persistence:** Fully persisted using PostgreSQL with Spring Data JPA.

---

## 🏗 Architecture & Engineering Decisions

This project is designed following modern backend practices. This approach was chosen to keep the system modular and maintainable while avoiding the unnecessary complexity of microservices for this scope.

- **Modular Monolith (Package-by-Feature):** The codebase is organized by business domain (`auth`, `user`, `transaction`, `analytics`). Domains communicate via clearly defined service interfaces to prevent logic leakage.

- **Lightweight CQRS in Analytics:** The analytics engine uses Spring `JdbcTemplate` to execute native read-only SQL queries directly against the database, fully decoupling it from the transaction entity models while maximizing reporting performance.

- **Stateless JWT Authentication & Security:** Fully stateless Spring Security 6 integration. Passwords are securely hashed using BCrypt before storage. Tokens determine authentication and method-level access.

- **Precision Financial Modeling:** Amounts are always handled with `BigDecimal (precision=19, scale=4)` to eliminate floating-point rounding errors.

- **Soft-Deletion:** Active records are never hard-deleted. Hibernate `@SQLDelete` hooks intercept `deleteById()` to perform an `UPDATE is_deleted=true`, preserving an audit trail.

---

## ⚖️ Assumptions & Tradeoffs

- **JWT over Sessions:** Chosen to ensure stateless scalability and simplify testing, assuming clients can securely store tokens.
- **PostgreSQL over SQLite:** Chosen for robust concurrency handling and native aggregation functions needed for the analytics engine.
- **Modular Monolith over Microservices:** Chosen to keep deployment and local development simple while maintaining strict domain boundaries.
- **Rate Limiting:** Omitted due to time constraints and assignment scope, but the system is structured to easily support an API Gateway or Bucket4j implementation in the future.
- **Testing:** Basic unit and integration tests are included to demonstrate validation of business logic and API behavior.
---

## 🗄 Data Model

The system utilizes a normalized relational schema:

- **User:**  
  `(id, username, password (hashed via BCrypt, never exposed), role, status, createdAt, updatedAt)`

- **FinancialRecord:**  
  `(id, userId, amount, type, category, transactionDate, notes, isDeleted, createdAt, updatedAt)`

---

## 🚀 Quick Start Guide

### Prerequisites
- Docker and Docker Compose installed

### Running the Application

```bash
docker compose up -d --build
```

The system spins up two containers: a PostgreSQL database and the Spring Boot API. The backend waits for the database to be fully healthy before booting.

---

## 📖 Exploring the API

👉 **Swagger UI:** http://localhost:8080/swagger-ui.html  

> Click the **"Authorize"** button and enter your Bearer token after logging in via `/api/v1/auth/login` to test all secure endpoints.

---

## 📖 API Contract Overview

All API responses (including errors) follow a standardized format:

```json
{
  "success": false,
  "data": null,
  "message": "Validation failed"
}
```

> Example: Invalid input (e.g., negative amount) returns a `400 Bad Request` with validation details.

---

### 1. Auth (PUBLIC)
- `POST /api/v1/auth/login` → Authenticates users and returns a JWT.

---

### 2. User Domain (ADMIN only)
- `GET /api/v1/users` → List all users  
- `POST /api/v1/users` → Create a new user  
- `PUT /api/v1/users/{id}/status` → Toggle user status (ACTIVE/INACTIVE)

---

### 3. Transaction Domain (ADMIN & ANALYST)
- `GET /api/v1/transactions` → Retrieves filtered, paginated ledgers (supports `userId`, `type`, `category`)
- `POST /api/v1/transactions` (ADMIN) → Create a new record  
- `PUT /api/v1/transactions/{id}` (ADMIN) → Update an existing record  
- `DELETE /api/v1/transactions/{id}` (ADMIN) → Perform soft-deletion  

---

### 4. Analytics Domain (ADMIN, ANALYST, VIEWER)
- `GET /api/v1/analytics/summary` → Dashboard with totals, category breakdowns, and 12-month trends  
- `GET /api/v1/analytics/recent` → Retrieve the 5 most recent transactions  

---

## 🛠 Tech Stack

- Java 21 LTS & Spring Boot 4.0.5  
- PostgreSQL 15 & Spring Data JPA (with JdbcTemplate for CQRS)  
- Spring Security 6 & JJWT 0.12.5  
- SpringDoc OpenAPI  
- Docker & Docker Compose  
