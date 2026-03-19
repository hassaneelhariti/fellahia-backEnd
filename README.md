# Fellahia Backend

A Spring Boot backend for the Fellahia platform — connecting farmers (fellah) with lawyers for legal assistance, powered by AI chat.

## Tech Stack
- Java 17 + Spring Boot
- Spring Security + JWT
- Spring AI + Ollama
- PostgreSQL
- Docker

## Features
- Authentication (JWT + OTP)
- Lawyer & farmer profiles
- Legal case management
- AI-powered chat
- Token topup system
- File storage

## Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- PostgreSQL

### Run with Docker
```bash
docker-compose up
```

### Run locally
```bash
./mvnw spring-boot:run
```

## Environment Variables
Create a `.env` file based on `.env.example`:
```
DB_URL=jdbc:postgresql://localhost:5432/fellahia
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
```

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register user |
| POST | /api/auth/login | Login |
| POST | /api/auth/verify-otp | Verify OTP |
| GET | /api/users/profile | Get profile |
| POST | /api/cases | Submit legal case |
| GET | /api/cases | Get all cases |
| POST | /api/chat | Send chat message |
| POST | /api/topup | Topup tokens |

## License
MIT
