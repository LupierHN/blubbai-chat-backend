# chat.blubbai Backend

![Build & Test](https://img.shields.io/badge/build-passing-brightgreen)
![Docker](https://img.shields.io/badge/docker-enabled-blue)

## 🚀 Project Overview

**backend.chat.blubbai** is a Spring Boot microservice that serves as a REST API for a chat client.  
It is implemented in Java (Gradle) and uses PostgreSQL as its database. The entire system runs containerized via Docker Compose.

---

## 📋 Table of Contents

1. [Prerequisites](#-prerequisites)
2. [Installation & Setup](#-installation--setup)
3. [Local Start](#-local-start)
4. [Environment Variables](#-environment-variables)
5. [Docker Compose](#-docker-compose)
6. [CI/CD with GitHub Actions](#cicd-with-github-actions)
7. [Server Deployment](#-server-deployment)
8. [Project Structure](#-project-structure)
9. [Key Components](#key-components)
10. [API Overview](#api-overview)
11. [Security & Filters](#security--filters)
12. [External API Integration](#external-api-integration)
13. [Testing](#testing)
14. [License](#-license)

---

## 🛠️ Prerequisites

- **Java 21**
- **Gradle 8+** (included in the build container)
- **Docker 23+** & **Docker Compose v2**
- **PostgreSQL 17+** (included in the Docker Compose setup)

---

## ⚙️ Installation & Setup

1. Clone the repository:
   ```bash
   git clone <gitURL>
   cd backend.chat.dvai
    ```
2. Adjust environment variables (see  [Environment Variables](#-environment-variables)).
3. Perform a local build and test:
   ```bash
   ./gradlew clean build
   ./gradlew test
   ```
4. Start the Docker containers:
   ```bash
   docker-compose up -d --build
   ```
## 🚦 Local Start
 - **The backend is accessible at:** `http://localhost:8080`
 - **Check health:**
   ```bash
   curl -X GET http://localhost:8080/api/health
   ```

## 🔑 Environment Variables
Create a `.env`file in the project root to define environment variables:
```dotenv
# Database
DB_URL=jdbc:postgresql://db:5432/chat_dvai
DB_USER=myuser
DB_PASSWORD=supersecret
JWT_SECRET=mysecret #Key for JWT signing must be at least 256 bits

# External APIs
PHONE_VALIDATION_API_KEY=your_abstractapi_phone_key
MAIL_VALIDATION_API_KEY=your_abstractapi_mail_key
```
- **DB_URL**: JDBC URL for the PostgreSQL database.
- **DB_USER**: Username for the database connection.
- **DB_PASSWORD**: Password for the database connection.
- **JWT_SECRET**: Secret for JWT signing (must be at least 256 bits).
- **PHONE_VALIDATION_API_KEY**: API key for phone validation (Abstract API).
- **MAIL_VALIDATION_API_KEY**: API key for mail validation (Abstract API).

***Note**: For local development and testing you can also set these variables in your IDE or terminal session.*

## 🐳 Docker Compose
- **Docker Compose** is included in the project and is used to start the application and its dependencies.

## CI/CD with GitHub Actions
- **GitHub Actions** is used to automate the build and test process.
- The configuration is located in the `.github/workflows` directory.
- Actions are triggered on every push to the `main` branch.
- Jobs:
  - **Build**:
    - Runs the build and tests.
  - **Deploy** (depends on successful build):
    - Copies the repo to the server via SSH.
    - Starts the Docker container on the server.

**Secrets in GitHub Actions**:
- `SSH_PRIVATE_KEY`: Private SSH key for server access.
- `SERVER_USER`: Username for server access.
- `SERVER_HOST`: IP address of the server.

## 🚀 Server Deployment
1. Prepare the server:
   ```bash
   sudo mkdir -p /opt/chat.dvai/{logs,data}
   sudo chown -R deployuser:deployuser /opt/chat.dvai
   sudo usermod -aG docker deployuser
    ```
2. Github Actions pushes:
   - Files are copied to `/opt/chat.dvai` (make sure the directory exists).
   - `docker compose down && docker compose up -d --build` is executed.
3. Result:
   - **The Backend is accessible at:** `http://<SERVER_IP>:8080`
     - **Check health:**
         ```bash
         curl -X GET http://<SERVER_IP>:8080/api/health
         ```

## 📂 Project Structure
```plaintext
blubbai-chat-backend/
├── src/                       # Source code
│   ├── main/                  # Java source code
│   ├── test/                  # Test source code
├── build.gradle               # Gradle build script
├── Dockerfile                 # Multi-stage Docker build
├── docker-compose.yml         # Docker Compose configuration
├── .env                       # Local environment variables (not in the repo)
├── .github/
│   └── workflows/
│       └── deploy.yml         # GitHub Actions workflow
└── README.md                  # Project README
```

## 🧩 Key Components

### Controllers

- **UserController.java**  
  Main REST controller for user management, authentication, registration, profile updates, 2FA, and token operations.  
  Endpoints:  
  - `/api/v1/user` (GET, PUT, DELETE)
  - `/api/v1/user/noa/register` (POST)
  - `/api/v1/user/noa/login` (POST)
  - `/api/v1/user/no2fa/2fa` (GET, POST)
  - `/api/v1/user/noa/validateToken` (POST)
  - `/api/v1/user/noa/renewToken` (POST)

- **ToolsController.java**  
  Utility endpoints for health checks, key generation, test token creation, and extracting bearer tokens.  
  Endpoints:  
  - `/tools/health` (GET)
  - `/tools/key` (GET)
  - `/tools/token` (GET)
  - `/tools/bearer` (GET)

### Services

- **UserService.java**  
  Handles user CRUD, authentication, password management, and 2FA logic.

- **PhoneNumberService.java**  
  Manages phone number persistence and validation.

### Utilities

- **ExternalApi.java**  
  Static methods for validating phone numbers and emails via Abstract API.  
  Uses environment variables for API keys.

- **EnvProvider.java**  
  Centralized access to environment variables, with error handling.

- **PasswordEncoder.java**  
  Provides a BCrypt password encoder bean for secure password hashing.

### Filters

- **JwtRequestFilter.java**  
  Intercepts requests, checks for JWT in the `Authorization` header, and sets authentication in the security context.

---

## 📖 API Overview

**UserController Endpoints:**

- `GET /api/v1/user`  
  Returns the authenticated user's profile.  
  - 200 OK: User object  
  - 404 Not Found: User does not exist  
  - 401/403: Unauthorized/Forbidden (handled by filter)

- `POST /api/v1/user/noa/register`  
  Registers a new user.  
  - 201 Created: List of tokens  
  - 400: Invalid email/phone  
  - 409: Username exists  
  - 500: Server error

- `POST /api/v1/user/noa/login`  
  Authenticates a user.  
  - 200: List of tokens  
  - 401: Invalid credentials  
  - 500: Server error

- `PUT /api/v1/user/update`  
  Updates user profile.  
  - 200: Updated user  
  - 400: Invalid email/phone  
  - 401: Wrong password  
  - 500: Server error

- `DELETE /api/v1/user/delete`  
  Deletes the authenticated user.  
  - 204: Deleted  
  - 404: Not found  
  - 401: Unauthorized

- `GET /api/v1/user/no2fa/2fa`  
  Initiates or manages 2FA.  
  - 200: QR code or status  
  - 400: Bad request  
  - 404: Not found  
  - 401: Unauthorized

- `POST /api/v1/user/no2fa/2fa`  
  Verifies 2FA code.  
  - 200: New access token  
  - 401: Wrong/expired code  
  - 404: Not found

- `POST /api/v1/user/noa/validateToken`  
  Validates a token.  
  - 200: true/false

- `POST /api/v1/user/noa/renewToken`  
  Renews a token.  
  - 200: New token  
  - 401: Unauthorized

**ToolsController Endpoints:**

- `GET /tools/health`  
  Health check (returns "OK").

- `GET /tools/key`  
  Generates a new JWT secret key (Base64).

- `GET /tools/token`  
  Generates a test token.

- `GET /tools/bearer`  
  Extracts the bearer token from the Authorization header.

---

## 🔒 Security & Filters

- **JWT Authentication:**  
  All protected endpoints require a valid JWT in the `Authorization` header (`Bearer <token>`).

- **Filters:**  
  - `JwtRequestFilter` is registered as a Spring bean and added to the filter chain in the security configuration.
  - Avoids double registration to prevent duplicate logging or authentication checks.

- **2FA:**  
  Enforced via a filter for protected endpoints (except `/noa/*` and `/no2fa/*`).

- **Logging:**  
  Request and response logging should be handled by a dedicated filter, registered only once in the filter chain.

---

## 🌐 External API Integration

- **Phone & Mail Validation:**  
  - Uses Abstract API for validation.
  - API keys are injected via environment variables.
  - See `ExternalApi.java` for implementation details.

- **Testing External APIs:**  
  - Integration tests mock environment variables to avoid leaking real keys.
  - Mocked tests simulate API responses for reliability.

---

## 🧪 Testing

- **Unit & Integration Tests:**  
  Located in `src/test/java/chat/blubbai/backend/utilsTests/`.
  - Uses JUnit 5 and Mockito for mocking.
  - Integration tests for real API calls mock environment variables for safety.
  - Run tests with:
    ```sh
    ./gradlew test
    ```

---

## 📜 License
This project is licensed under the MIT License.
Feel free to use, modify, and distribute!

