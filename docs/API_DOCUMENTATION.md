# ShopSense AI REST API Specification

Base URL: `http://localhost:8080/api/v1`

## 1. Register User
- **Endpoint**: `POST /auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "name": "John Doe",
    "email": "john@example.com",
    "password": "Password123!"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "ROLE_USER"
  }
  ```

## 2. User Login
- **Endpoint**: `POST /auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "email": "john@example.com",
    "password": "Password123!"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "ROLE_USER"
  }
  ```

## 3. Get User Profile
- **Endpoint**: `GET /users/profile`
- **Access**: Protected (`Authorization: Bearer <JWT_TOKEN>`)
- **Response** (`200 OK`):
  ```json
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "ROLE_USER",
    "createdAt": "2026-08-02T10:00:00Z"
  }
  ```
