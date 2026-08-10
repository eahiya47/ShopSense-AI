# ShopSense AI – Phase 2A Implementation Plan

## 1. Phase Objective

Phase 2A establishes the core product discovery and comparison foundation
of ShopSense AI.

The phase begins with the existing Phase 1 authentication application and
incrementally adds the product catalog, product search, product variants,
marketplace connector architecture, product comparison, review handling,
AI analysis, and frontend integration.

The implementation must follow the finalized architecture documents in:

- `docs/02_Architecture/SYSTEM_ARCHITECTURE.md`
- `docs/02_Architecture/DATABASE_DESIGN.md`
- `docs/02_Architecture/API_SPECIFICATION.md`
- `docs/02_Architecture/AI_WORKFLOW.md`
- `docs/02_Architecture/REQUEST_LIFECYCLE.md`
- `docs/02_Architecture/ERROR_HANDLING.md`

These architecture documents are the source of truth.

---

# 2. Existing Starting Point

The current application already contains:

- Spring Boot 3.2.3 backend
- Java 17
- Maven
- React 18
- Vite
- Material UI
- React Router
- Axios
- JWT authentication
- User registration
- User login
- User profile
- Protected routes
- Global backend exception handling
- Swagger/OpenAPI
- MySQL-compatible database structure
- H2 development database configuration

Phase 1 authentication functionality must remain working throughout
Phase 2A.

---

# 3. Implementation Principles

Phase 2A must follow these principles:

1. Implement one stage at a time.
2. Verify every stage before moving to the next stage.
3. Commit each completed stage to Git.
4. Do not implement future stages early.
5. Do not unnecessarily rewrite working Phase 1 functionality.
6. Keep controllers thin.
7. Keep business logic in services.
8. Keep persistence logic in repositories.
9. Use DTOs for API responses.
10. Keep marketplace integrations isolated behind connectors.
11. Keep AI functionality isolated from deterministic business logic.
12. Design the implementation so future marketplace connectors and features
    can be added without redesigning the core architecture.

---

# 4. Phase 2A Architecture Flow

The intended application flow is:

```text
User
 |
 v
React Frontend
 |
 v
Spring Boot REST API
 |
 +--------------------+
 |                    |
 v                    v
Product Catalog     User Features
 |                    |
 v                    +-- Wishlist
Product Search        +-- Search History
 |
 v
Product
 |
 v
ProductVariant
 |
 v
Marketplace Comparison
 |
 +----------+----------+
 |          |          |
 v          v          v
Platform  Platform  Platform
Offer     Offer     Offer
 |
 v
Recent Reviews
 |
 v
AI Analysis
 |
 v
Gemini
5. Product and Variant Rules

Every product must have at least one ProductVariant.

Products with meaningful selectable configurations expose those variants
to the user.

Examples:

iPhone 16 Pro
    |
    +-- 128GB
    +-- 256GB
    +-- 512GB
Laptop
    |
    +-- 16GB RAM / 512GB SSD
    +-- 16GB RAM / 1TB SSD
    +-- 32GB RAM / 1TB SSD

Products without meaningful selectable configurations use an internal
Standard variant.

Example:

Washing Machine
    |
    +-- Standard

The frontend does not need to display Standard as a user-facing
selection.

The core relationship is:

Product
   |
   +-- ProductVariant
          |
          +-- PlatformOffer
                 |
                 +-- Platform
6. Data Lifecycle Rules

ShopSense AI uses different freshness strategies for different data.

Static / Catalog Data

Examples:

Product name
Brand
Model
Category
Product specifications
Variant information

These are stored in the database and changed only when catalog information
changes.

Live Marketplace Data

The following information is retrieved through marketplace connectors
during product comparison:

Current price
Original price
Seller
Seller rating
Availability
Delivery information
Current offers
Product URL

Current representative platform offers may be updated in the database.

Permanent price history is not required in Phase 2A.

Review Data

Reviews follow a separate refresh lifecycle.

Approximately 10–15 recent relevant reviews per platform are temporarily
stored and refreshed approximately weekly.

The normal product comparison request does not retrieve reviews from every
marketplace again.

7. Phase 2A Stages
Stage 1 — Backend Catalog Database Foundation
Status

COMPLETED

Objective

Create the core JPA entities and repositories required for the ShopSense
product catalog and marketplace comparison architecture.

Entities
Category
Product
ProductSpecification
ProductVariant
VariantAttribute
Platform
PlatformOffer
Repositories
CategoryRepository
ProductRepository
ProductSpecificationRepository
ProductVariantRepository
VariantAttributeRepository
PlatformRepository
PlatformOfferRepository
Additional User Support

The User entity supports:

Country
Region

without changing the existing authentication architecture.

Verification

Stage 1 was verified using:

mvn clean test

Results:

BUILD SUCCESS

Existing application tests passed and catalog repository tests passed.

Git

Stage 1 was committed and pushed after verification.

8. Stage 2 — Product Catalog Services and REST APIs
Status

NEXT

Objective

Expose the product catalog through clean public REST APIs.

The backend must provide:

GET /api/v1/products/search
GET /api/v1/products/{productId}
GET /api/v1/products/{productId}/variants
8.1 Product Search

The search endpoint must:

Be public.
Accept a user search query.
Use deterministic database-backed search.
Support non-exact keyword matching.
Rank relevant products.
Support pagination.
Avoid Gemini or AI semantic search at this stage.

Example:

Search:
iphone

Possible results:

iPhone 17
iPhone 17 Pro
iPhone 16
iPhone 16 Pro
iPhone 15

The initial search system should be structured so a future semantic search
capability can be introduced without replacing the public API.

8.2 Product Details

The product details endpoint must return stable catalog information.

It may include:

Product ID
Category
Brand
Model
Name
Description
Product specifications
Variant information where appropriate
hasVariants

It must not retrieve live marketplace data during this stage.

It must not retrieve reviews during this stage.

It must not retrieve AI results during this stage.

JPA entities must not be returned directly.

DTOs must be used.

8.3 Product Variants

The variant endpoint must return variants belonging to a product.

The response must support meaningful selectable configurations such as:

RAM
Storage
Color
Other category-appropriate variant attributes

The system must not assume every category has the same variant attributes.

Products without meaningful variants must use the internal Standard
variant.

8.4 Backend Structure

Use:

Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database

Create or extend:

Product services
Variant services
Product search service
Product DTOs
Variant DTOs
Search response DTOs
Required repository query methods
Product controllers
8.5 Error Handling

Use the existing global exception handling architecture.

Expected behavior:

Invalid product ID
    -> 404

Invalid search request
    -> 400

Unexpected backend failure
    -> 500

Do not expose stack traces or database implementation details.

8.6 Security

The following endpoints are public:

GET /api/v1/products/search
GET /api/v1/products/{productId}
GET /api/v1/products/{productId}/variants

Do not require JWT authentication for them.

8.7 Testing

Stage 2 must include tests for:

Valid product search
Partial/non-exact search
No matching products
Pagination
Existing product
Missing product
Existing product variants
Standard variant behavior
Public endpoint access without authentication
8.8 Out of Scope

Do not implement:

Marketplace connectors
Live prices
Product comparison
Reviews
Gemini
Wishlist
Search history
Frontend product pages
9. Stage 3 — Marketplace Connector Framework
Status

PLANNED

Objective

Create an isolated marketplace integration architecture.

The core abstraction will be:

MarketplaceConnector

with a manager/orchestrator responsible for coordinating connectors.

Initial connectors will be mock implementations:

AmazonConnector
FlipkartConnector
CromaConnector

The application must not depend directly on marketplace-specific response
formats.

Connectors will return normalized marketplace information.

The intended structure is:

MarketplaceConnector
        |
        +-- AmazonConnector
        +-- FlipkartConnector
        +-- CromaConnector
        +-- Future connectors

Real marketplace integrations can be introduced later without changing
the core comparison architecture.

10. Stage 4 — Product Comparison
Status

PLANNED

Objective

Compare the selected product variant across marketplaces.

Flow:

Selected ProductVariant
        |
        v
Connector Manager
        |
        +-- Amazon
        +-- Flipkart
        +-- Croma
        |
        v
Normalized Offers
        |
        v
Best Offer Per Platform
        |
        v
Comparison Response

The system should select the best representative offer from each platform.

It should not simply return one global cheapest offer.

Each platform can have:

Price
Original price
Seller
Seller rating
Availability
Delivery
Offers
Product URL

If one platform fails:

Amazon       SUCCESS
Flipkart     SUCCESS
Croma        FAILED

the comparison must still return:

Amazon       Available
Flipkart     Available
Croma        Unavailable

A marketplace failure must not prevent other marketplaces from being
displayed.

11. Stage 5 — Review System
Status

PLANNED

Objective

Implement the separate recent-review lifecycle.

The system should maintain approximately:

10–15 recent relevant reviews
per ProductVariant + Platform

Review flow:

Marketplace Connector
        |
        v
Recent Reviews
        |
        v
Select relevant reviews
        |
        v
Temporary Review Storage
        |
        v
Weekly Refresh

The system should replace the temporary review set during successful
refreshes.

If a refresh fails, existing valid review data should not be unnecessarily
deleted.

Reviews are not retrieved from every marketplace on every comparison
request.

12. Stage 6 — User Features
Status

PLANNED

Implement authenticated user functionality.

Wishlist

Users can:

Add a product variant
View wishlist
Remove a product variant

Wishlist entries belong to the authenticated user.

Wishlist should reference the product variant rather than a specific
marketplace offer.

Search History

Users can:

Save searches
View their search history
Remove history entries where supported by the final API

Search history belongs to the authenticated user.

13. Stage 7 — AI Integration
Status

PLANNED

AI is only used after the user has selected and viewed a specific product
variant.

Example:

Search:
iPhone 16

        |
        v

Select:
iPhone 16 Pro 256GB

        |
        v

Product Details
        |
        v
Marketplace Comparison
        |
        v
AI Summary

The AI must focus only on the selected product variant.

It must not replace the selected product with another product.

It should explain:

Which platform is cheaper
Which platform offers better delivery
Which platform has better seller/review signals
Which platform has better offers
Important trade-offs

The backend supplies trusted structured data to Gemini.

Gemini must not invent:

Prices
Ratings
Reviews
Availability
Delivery information
Product specifications

Deterministic calculations remain in backend code.

Gemini is responsible for explanation and synthesis.

14. Stage 8 — AI Cache
Status

PLANNED

AI summaries should be cached to reduce unnecessary Gemini requests.

The cache should be associated with the selected product variant.

Conceptually:

AI Request
    |
    v
Check Cache
   / \
 YES  NO
  |    |
  v    v
Return Gemini
       |
       v
     Cache

The cache is an optimization.

A cache failure must not prevent the AI service from attempting fresh
generation when appropriate.

15. Stage 9 — Frontend Product Integration
Status

PLANNED

Connect the existing React application to the product APIs.

The main user flow should become:

Landing Page
      |
      v
Search
      |
      v
Search Results
      |
      v
Product Card
      |
      v
Product Details
      |
      v
Variant Selection
      |
      v
Marketplace Comparison
      |
      v
Reviews
      |
      v
AI Summary
      |
      v
Marketplace Buy Link

The frontend should provide appropriate:

Loading states
Empty states
Error states
Marketplace unavailable states
AI unavailable states
16. Stage 10 — Full Testing and Integration
Status

PLANNED

Test the complete application.

Backend
Repository tests
Service tests
Controller tests
Security tests
Integration tests
Marketplace

Test:

Successful connector
Failed connector
Timeout
No offer
Missing marketplace data
Reviews

Test:

Review retrieval
Review selection
Weekly refresh
Refresh failure
AI

Test:

Successful AI response
AI timeout
Invalid AI response
Cache hit
Cache miss
Cache expiration
Frontend

Test:

Search
Product selection
Variant selection
Comparison
Reviews
AI summary
Wishlist
Search history
Loading states
Error states
17. Git Workflow

Every completed implementation stage should follow:

Implement
    |
    v
Test
    |
    v
Review
    |
    v
Commit
    |
    v
Push
    |
    v
Next Stage

Do not combine several major stages into one unreviewed commit.

18. Phase 2A Completion Criteria

Phase 2A is considered complete when:

Product catalog is available through REST APIs.
Deterministic smart product search works.
Product details work.
Product variants work.
Standard variants work correctly.
Marketplace connector architecture works.
Marketplace comparison works.
Best offer per platform is returned.
Marketplace failures are isolated.
Current offers are maintained.
Recent reviews are refreshed and available.
Authentication remains functional.
Wishlist works.
Search history works.
Product-specific AI summaries work.
AI caching works.
React frontend is integrated.
Loading/error/partial-result states work.
Backend and frontend tests pass.
The complete user flow works.
19. Future Extensions

Phase 2A should leave the architecture ready for future features such as:

Real marketplace connectors
Additional marketplaces
AI-powered semantic search
Price history
Price graphs
Price prediction
Price alerts
Advanced recommendation systems
More regional marketplace support
Additional product categories
Improved personalization

These features are not required for Phase 2A.

The core architecture should support these extensions without requiring a
complete redesign.