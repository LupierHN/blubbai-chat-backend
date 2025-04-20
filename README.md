# chat.dvai Backend

![Build & Test](https://img.shields.io/badge/build-passing-brightgreen)
![Docker](https://img.shields.io/badge/docker-enabled-blue)

## 🚀 Project Overview

**backend.chat.dvai** is a Spring Boot microservice that serves as a REST API for a chat client.  
It is implemented in Java (Gradle) and uses PostgreSQL as its database. The entire system runs containerized via Docker Compose.

---

## 📋 Table of Contents

1. [Prerequisites](#-prerequisites)
2. [Installation & Setup](#-installation--setup)
3. [Local Start](#-local-start)
4. [Environment Variables](#-environment-variables)
5. [Docker Compose](#-docker-compose)
6. [CI/CD with GitHub Actions](#-cicd-with-github-actions)
7. [Server Deployment](#-server-deployment)
8. [Project Structure](#-project-structure)
9. [License](#-license)

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
   git clone https://github.com/your-username/backend.chat.dvai.git
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

## 🔑 Environment Variables
Create a `.env`file in the project root to define environment variables:
```dotenv
# Database
DB_URL=jdbc:postgresql://db:5432/chat_dvai
DB_USER=myuser
DB_PASSWORD=supersecret
```
- **DB_URL**: JDBC URL for the PostgreSQL database.
- **DB_USER**: Username for the database connection.
- **DB_PASSWORD**: Password for the database connection.

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
   **The Backend is accessible at:** `http://<SERVER_IP>:8080`

## 📂 Project Structure
```plaintext
backend.chat.dvai/
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
## 📜 Lizenz
This project is licensed under the MIT License.
Feel free to use, modify, and distribute!

