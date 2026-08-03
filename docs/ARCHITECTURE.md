# ShopSense AI Architecture Overview (Phase 1)

## Architecture Diagram
```
+-------------------------------------------------------+
|                    React Frontend                     |
|  Landing Page | Login | Register | Dashboard | Profile|
|            (Material-UI + React Router + Axios)       |
+---------------------------+---------------------------+
                            | HTTP/REST (JWT Bearer)
                            v
+-------------------------------------------------------+
|                   Spring Boot Backend                 |
|                                                       |
|  [ Controller Layer ] -> AuthController, UserController
|             |                                         |
|  [ Security Layer ]   -> JWT Filter, SecurityConfig   |
|             |                                         |
|  [ Service Layer ]    -> AuthService, UserService     |
|             |                                         |
|  [ Repository Layer ] -> UserRepository               |
+---------------------------+---------------------------+
                            | JDBC / JPA
                            v
+-------------------------------------------------------+
|                    MySQL Database                     |
|                     (shopsense_db)                    |
+-------------------------------------------------------+
```

## Layered Design Principles
- **Controller**: Exposes REST API endpoints and handles HTTP requests/responses.
- **Service**: Implements business logic and transaction management.
- **Repository**: Handles JPA data access abstraction.
- **Entity**: Maps domain objects to MySQL database tables.
- **DTO**: Decouples external API models from internal JPA domain entities.
- **Security**: Implements stateless JWT authentication and authorization.
