You are an experienced Software Architect, Senior Full-Stack Engineer, and AI Solutions Engineer.

Your task is to build the foundation of a production-ready web application called "ShopSense AI".

## Project Vision

ShopSense AI is an AI-powered e-commerce product comparison platform.

The purpose of this platform is to help users make better purchasing decisions by comparing products from multiple e-commerce platforms such as Amazon, Flipkart, Croma, Reliance Digital, and others.

Unlike traditional comparison websites, ShopSense AI will not only compare prices but will also use AI to generate intelligent summaries, compare product specifications, summarize customer reviews, and recommend the best buying option based on user preferences.

The AI capabilities will be implemented in later phases. For this phase, focus only on building a clean, scalable, and production-ready foundation.

====================================================
PHASE 1 GOAL
====================================================

Build a professional full-stack project structure that is scalable and easy to extend in future phases.

Do NOT implement product search, comparison logic, AI features, or platform integrations yet.

====================================================
TECH STACK
====================================================

Frontend
- React (JavaScript)
- Material UI
- React Router
- Axios

Backend
- Spring Boot
- Java
- Maven
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate

Database
- MySQL

Documentation
- Swagger / OpenAPI

Version Control
- Git

====================================================
PROJECT STRUCTURE
====================================================

Create separate projects:

shopsense-ai/
    frontend/
    backend/
    database/
    docs/

Maintain a clean architecture.

Backend should follow a layered architecture:

- Controller
- Service
- Repository
- Entity
- DTO
- Security
- Configuration
- Exception Handling
- Utility

Organize the code for long-term maintainability and scalability.

====================================================
AUTHENTICATION
====================================================

Implement a complete JWT Authentication system.

Required features:

- User Registration
- User Login
- Password Encryption
- JWT Token Generation
- JWT Validation
- Protected Routes
- User Profile endpoint

Use Spring Security best practices.

====================================================
DATABASE
====================================================

Configure MySQL.

Create an initial User table with appropriate fields.

Design the database in a way that future modules such as Products, Platform Offers, Wishlist, Search History, Price Alerts, and AI Analysis can be added without major restructuring.

====================================================
FRONTEND
====================================================

Create a clean and professional UI.

Required pages:

- Landing Page
- Login
- Register
- Dashboard (placeholder)
- Profile (placeholder)

Configure routing properly.

Create a responsive layout.

The dashboard can simply display:

"Welcome to ShopSense AI"

No search functionality is required in this phase.

====================================================
BACKEND
====================================================

Implement REST APIs for:

- Register
- Login
- Get Current User Profile

Use proper HTTP status codes.

Implement request validation.

Create global exception handling.

Configure CORS.

Generate Swagger documentation.

====================================================
ENGINEERING EXPECTATIONS
====================================================

Write clean, readable, modular, production-quality code.

Follow SOLID principles wherever appropriate.

Avoid unnecessary complexity.

Keep the project scalable because future phases will include:

- Multi-platform product search
- Product normalization engine
- AI recommendation engine
- Gemini API integration
- Review summarization
- Price tracking
- Wishlist
- Search history
- Analytics dashboard

Do not generate placeholder code for these future modules unless it helps establish a clean architecture.

====================================================
VERIFICATION
====================================================

After implementation:

1. Install all required dependencies.

2. Run the backend.

3. Resolve every compilation error.

4. Resolve every runtime error.

5. Run the frontend.

6. Verify that:
   - Registration works.
   - Login works.
   - JWT Authentication works.
   - Protected endpoints require authentication.
   - Frontend successfully communicates with backend.

7. If any issue occurs, debug and fix it automatically before continuing.

Do not stop after generating files.

Continue until the application runs successfully.

====================================================
DELIVERABLE
====================================================

When Phase 1 is complete, provide a summary containing:

- Project structure created
- Dependencies installed
- APIs implemented
- Database schema created
- Security implementation
- Files created or modified
- Verification results
- Any remaining recommendations before Phase 2

Do not begin Phase 2.
Stop only after Phase 1 is fully functional and verified.