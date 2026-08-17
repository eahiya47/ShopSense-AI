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

### Status

**NEXT**

### Objective

Create the marketplace integration abstraction that allows ShopSense AI
to communicate with multiple marketplace platforms through a common
interface.

This stage establishes the connector architecture using mock marketplace
implementations.

Real external marketplace integrations are NOT required in this stage.

---

## 9.1 Connector Architecture

The core abstraction is:

```text
MarketplaceConnector
        |
        +-- AmazonConnector
        +-- FlipkartConnector
        +-- CromaConnector
        |
        +-- Future connectors

A ConnectorManager is responsible for coordinating the available
marketplace connectors.

The core application must communicate with the MarketplaceConnector
interface rather than directly depending on a specific marketplace.

9.2 MarketplaceConnector

The connector interface should define the common operations required by
ShopSense AI to retrieve marketplace information for a selected product
variant.

The interface should be marketplace-independent.

Marketplace-specific implementation details must remain inside the
individual connector.

9.3 Mock Connectors

Create mock implementations for the initial platforms:

Amazon
Flipkart
Croma

The mock connectors should return realistic, deterministic marketplace
data.

They must not call external websites or APIs.

Example:

AmazonConnector
      |
      v
Mock Amazon offer data

FlipkartConnector
      |
      v
Mock Flipkart offer data

CromaConnector
      |
      v
Mock Croma offer data

The rest of the application must not need to know that these are mock
connectors.

9.4 Normalized Marketplace Data

Different marketplaces may use different response formats.

ShopSense AI must normalize marketplace information into a common internal
representation.

The normalized offer should support information such as:

Platform
Product variant
Current price
Original price
Seller
Seller rating
Availability
Delivery information
Offer information
Product URL
Retrieval timestamp

The normalized model must not expose marketplace-specific response
structures to the rest of the application.

9.5 ConnectorManager

ConnectorManager coordinates marketplace connectors.

Its responsibilities include:

Maintain the available connectors.
Request marketplace data from connectors.
Associate results with the appropriate platform.
Normalize connector results where necessary.
Isolate connector failures.
Return available marketplace results to the calling service.

The manager must not contain marketplace-specific scraping or parsing
logic.

9.6 Failure Isolation

A failure in one marketplace connector must not prevent other connectors
from returning results.

Example:

Amazon       → SUCCESS
Flipkart     → SUCCESS
Croma        → FAILED

The result should still contain:

Amazon       → Available
Flipkart     → Available
Croma        → Unavailable

Possible connector states include:

AVAILABLE
UNAVAILABLE
NO_OFFER

The system must not invent marketplace data when a connector fails.

9.7 Connector Timeout and Exceptions

The connector architecture must support failures such as:

Network failure
Timeout
Invalid response
Marketplace unavailable
No offer
Unexpected connector exception

At this stage, mock connectors may simulate failures for testing.

The connector manager should isolate failures so that one failed connector
does not terminate the entire marketplace operation.

9.8 Database Interaction

Stage 3 establishes the connector framework.

The connectors should retrieve and return normalized marketplace information.

The actual comparison workflow and current PlatformOffer persistence
will be implemented in Stage 4.

Do not introduce permanent price history.

9.9 Out of Scope

Do NOT implement in Stage 3:

Real Amazon API integration
Real Flipkart API integration
Real Croma API integration
Web scraping
Marketplace authentication credentials
Product comparison API
Best-offer selection
Permanent price history
Review retrieval
Review refresh
Gemini
AI summaries
Wishlist
Search history
Frontend marketplace comparison UI
9.10 Testing

Stage 3 must test:

Each mock connector returns normalized data.
ConnectorManager can coordinate multiple connectors.
Multiple successful connectors return multiple results.
One failed connector does not prevent other results.
NO_OFFER is handled separately from UNAVAILABLE.
Connector exceptions are isolated.
No marketplace-specific response structure leaks outside the connector
layer.

The tests should use deterministic mock data.

9.11 Completion Criteria

Stage 3 is complete when:

MarketplaceConnector exists.
ConnectorManager exists.
Amazon mock connector exists.
Flipkart mock connector exists.
Croma mock connector exists.
Normalized marketplace data model exists.
Connector failures are isolated.
Connector tests pass.
Existing Stage 1 and Stage 2 tests continue to pass.
No real marketplace integration is required.
No frontend changes are required.
No Gemini functionality is added.

After verification, Stage 3 must be committed to Git before beginning
Stage 4.

10. Stage 4 — Product Comparison

### Status

**NEXT**

### Objective

Connect the Product Catalog with the Marketplace Connector Framework to
provide a unified comparison of the selected ProductVariant across
multiple platforms.

This is the stage where ShopSense AI begins performing its core
price-comparison functionality.

The comparison must use the current marketplace data retrieved through the
connector framework.

---

## 10.1 Comparison Flow

The comparison flow is:

```text
Selected ProductVariant
        |
        v
Comparison Service
        |
        v
ConnectorManager
        |
        +----------------+----------------+
        |                |                |
        v                v                v
     Amazon          Flipkart          Croma
     Connector       Connector        Connector
        |                |                |
        +----------------+----------------+
                         |
                         v
                Normalized Results
                         |
                         v
              Best Offer Per Platform
                         |
                         v
                Comparison Response

The comparison service must operate on the specific ProductVariant selected
by the user.

The system must not compare different product variants as if they were the
same configuration.

Example:

iPhone 16 Pro 256GB

must be compared against the corresponding 256GB configuration offered by
each platform.

10.2 Comparison Service

Create a dedicated comparison service responsible for:

Receiving a ProductVariant ID.
Validating that the ProductVariant exists.
Requesting marketplace data through ConnectorManager.
Receiving normalized connector results.
Selecting the best representative offer from each platform.
Updating the current PlatformOffer records where appropriate.
Returning a unified comparison response.

The comparison service must not contain marketplace-specific logic.

Marketplace-specific behavior remains inside the connector layer.

10.3 Best Offer Per Platform

The system must select the best available offer from each platform.

It must NOT select only the single cheapest offer globally.

Example:

Amazon
    Offer A → ₹115,999
    Offer B → ₹114,999
    Offer C → ₹116,499

    Selected:
    ₹114,999


Flipkart
    Offer A → ₹114,499
    Offer B → ₹113,999
    Offer C → ₹115,299

    Selected:
    ₹113,999


Croma
    Offer A → ₹114,490

    Selected:
    ₹114,490

The comparison result therefore contains:

Amazon    → ₹114,999
Flipkart  → ₹113,999
Croma     → ₹114,490

Each platform gets its own best representative offer.

If the connector architecture currently returns only one normalized offer
per platform, that result is treated as that platform's representative
offer.

The architecture should remain extensible for connectors that may return
multiple offers in the future.

10.4 Current PlatformOffer Storage

The existing PlatformOffer entity is used to maintain the current
representative offer for each:

ProductVariant + Platform

When fresh marketplace information is received:

Existing PlatformOffer
        |
        v
Fresh marketplace data
        |
        v
Update current PlatformOffer

The system should update the existing current offer where the corresponding
ProductVariant and Platform already exist.

A new record may be created when no current offer exists.

10.5 No Permanent Price History

Permanent historical price storage is NOT implemented in Stage 4.

The system maintains only the current representative offer.

Example:

Monday:
₹114,999

        ↓

Wednesday:
₹113,999

        ↓

Current stored value:
₹113,999

The previous value does not need to be permanently retained.

The architecture must remain extensible so a future PriceHistory
capability can be introduced without redesigning the comparison service.

10.6 Comparison Response

Create an API response DTO representing the comparison result.

The response should identify:

ProductVariant
Platform
Current price
Original price where available
Currency
Seller
Seller rating
Availability
Availability details
Delivery information
Offer information
Product URL
Retrieval timestamp
Platform status

The response should make it clear which platform each offer belongs to.

10.7 Public Comparison API

Implement:

GET /api/v1/variants/{variantId}/comparison
Access

Public.

No JWT authentication is required.

Request

The selected variantId identifies the exact product configuration to
compare.

Example:

GET /api/v1/variants/25/comparison
Response

Return the current comparison information available from the supported
platforms.

10.8 Marketplace Failure Isolation

A failed marketplace must not prevent successful platforms from appearing.

Example:

Amazon
    → SUCCESS

Flipkart
    → SUCCESS

Croma
    → UNAVAILABLE

The API response should still contain:

Amazon
    → Available offer

Flipkart
    → Available offer

Croma
    → Unavailable

The comparison service must not fabricate a price or offer for an
unavailable platform.

Possible platform statuses include:

AVAILABLE
UNAVAILABLE
NO_OFFER

These statuses must remain distinguishable.

10.9 Connector Failure Handling

The comparison service must rely on ConnectorManager for connector
execution and failure isolation.

Possible failures include:

Connector timeout
Network failure
Invalid marketplace response
Connector exception
Marketplace unavailable
No offer available

One connector failure must not terminate the entire comparison operation.

10.10 Comparison Data Freshness

Marketplace comparison information is considered live/current data.

When a comparison request is made, the connector framework should retrieve
current marketplace information.

The system must not rely only on stale database prices when a live
connector is available.

The current PlatformOffer records are updated as part of processing fresh
marketplace information.

The stored offer therefore represents the latest successfully retrieved
current information.

10.11 Product Validation

Before performing comparison:

Verify the ProductVariant exists.
Retrieve its associated Product.
Confirm that the selected variant is valid for that Product.
Request marketplace information for that exact variant.

If the variant does not exist:

404 RESOURCE_NOT_FOUND

If the variant exists but no platform has an available offer:

Return a valid comparison response indicating that no offers are
currently available.

Do not return fabricated pricing.

10.12 Database Interaction

Stage 4 may read and update:

ProductVariant
Platform
PlatformOffer

The comparison service must use the existing JPA repositories.

Do not introduce a separate permanent price-history table.

Do not modify the existing Product/Variant architecture.

10.13 Architecture Separation

The intended responsibility boundaries are:

ComparisonController
        |
        v
ComparisonService
        |
        v
ConnectorManager
        |
        v
MarketplaceConnector
        |
        v
NormalizedOffer
        |
        v
ComparisonService
        |
        +--> PlatformOfferRepository
        |
        v
ComparisonResponse

Responsibilities:

ComparisonController
Receive HTTP request.
Validate/extract path parameters.
Delegate to ComparisonService.
Return HTTP response.
ComparisonService
Validate ProductVariant.
Coordinate comparison.
Select representative platform offers.
Update current PlatformOffer records.
Build comparison response.
ConnectorManager
Coordinate marketplace connectors.
Isolate connector failures.
Return normalized connector results.
MarketplaceConnector
Handle marketplace-specific communication.
Return normalized marketplace data.
PlatformOfferRepository
Persist current platform offers.
10.14 Error Handling

Use the existing GlobalExceptionHandler.

Expected behavior:

Invalid variant ID
    → 404 RESOURCE_NOT_FOUND

No offers available
    → Valid comparison response with unavailable/no-offer statuses

Connector failure
    → Other platform results still returned

Unexpected backend failure
    → Existing standardized 500 response

Do not expose stack traces, connector internals, or database details.

10.15 Testing

Stage 4 must include tests for:

Successful comparison across all mock platforms.
Correct ProductVariant validation.
Correct ProductVariant → Product relationship.
Best offer selection per platform.
Current PlatformOffer creation when no record exists.
Current PlatformOffer update when a record already exists.
One connector failure while other platforms succeed.
NO_OFFER handling.
UNAVAILABLE handling.
Variant not found → 404.
No available offers.
Comparison endpoint is accessible without JWT.
Existing Stage 1, Stage 2, and Stage 3 tests continue to pass.

Tests must use deterministic mock marketplace connectors.

Do not use real external marketplace APIs in tests.

10.16 Out of Scope

Do NOT implement in Stage 4:

Permanent price history
Price graphs
Price prediction
Real marketplace APIs
Web scraping
Review retrieval
Weekly review refresh
Gemini / AI summaries
Wishlist
Search history
Price alerts
Frontend comparison UI

These features belong to later stages.

10.17 Completion Criteria

Stage 4 is complete when:

ComparisonService exists.
ComparisonController exists.
GET /api/v1/variants/{variantId}/comparison works.
The exact selected ProductVariant is compared.
ConnectorManager supplies marketplace results.
Best offer from each platform is selected.
Current PlatformOffer records are created or updated.
Permanent price history is not introduced.
Marketplace failures are isolated.
AVAILABLE, UNAVAILABLE, and NO_OFFER remain distinguishable.
Comparison uses current marketplace information.
The API returns a clean comparison DTO.
All Stage 4 tests pass.
All previous stage tests continue to pass.
No frontend changes are required.
No AI/review functionality is added.

After verification, Stage 4 must be committed to Git before beginning
Stage 5.

11. Stage 5 — Review System

### Status

**NEXT**

### Objective

Implement the review data lifecycle for ShopSense AI.

Reviews are handled separately from live marketplace price comparison.

The system should maintain a small, recent, relevant set of reviews for each
ProductVariant + Platform combination.

The review system is designed primarily to support:

- Product detail pages
- Review summaries
- Platform comparison context
- Future AI analysis

---

## 11.1 Review Storage Strategy

The system should temporarily store approximately:

10–15 recent relevant reviews per:

```text
ProductVariant + Platform

The system does NOT need to permanently store every review retrieved from a
marketplace.

The stored review set represents the currently relevant recent review
sample.

11.2 Review Lifecycle

The review lifecycle is separate from live price comparison.

Marketplace Connector
        |
        v
Recent Reviews
        |
        v
Review Selection
        |
        v
Temporary Review Storage
        |
        v
Product Details / AI

Reviews are refreshed approximately weekly.

Normal product comparison requests do NOT retrieve reviews from every
marketplace again.

11.3 Review Refresh

During a review refresh:

Identify the ProductVariant and Platform.
Retrieve recent reviews through the appropriate marketplace connector.
Select approximately 10–15 relevant recent reviews.
Validate and normalize the review data.
Replace the existing temporary review set with the refreshed set.

A successful refresh should result in the stored review set representing
the latest available review sample.

11.4 Refresh Failure

If a marketplace review refresh fails:

Existing valid review set
        |
        v
Refresh attempt
        |
        v
FAILED
        |
        v
Keep existing review data

The system must NOT delete valid existing reviews simply because a refresh
failed.

The failure should be logged appropriately.

The next scheduled refresh can attempt the operation again.

11.5 Review Data

A stored review may contain information such as:

Review ID or marketplace review identifier
ProductVariant
Platform
Reviewer name where appropriate
Rating
Review title
Review text
Review date
Verified purchase indicator where available
Source/reference URL where available
Retrieved timestamp

Do not store unnecessary personal information.

11.6 Review Relevance

The review selection process should prioritize recent and relevant reviews.

The system does not need to store every review returned by a marketplace.

The selection should produce approximately 10–15 reviews per platform.

If fewer valid recent reviews are available, store the available valid reviews
rather than inventing or duplicating reviews.

11.7 Temporary Review Data

The review storage is intended to represent the current review sample.

The system does not need permanent historical review versions.

Example:

Week 1
10 recent reviews
        |
        v
Week 2 refresh
        |
        v
Replace with latest 10–15 reviews

Old review records that are no longer part of the current review set may be
removed or replaced according to the implementation strategy.

11.8 Review API

Implement a public endpoint for retrieving the currently stored review
sample for a selected ProductVariant.

Endpoint:

GET /api/v1/variants/{variantId}/reviews

The endpoint should return:

ProductVariant information where appropriate
Platform
Current stored reviews
Review count
Review freshness/retrieval information where appropriate

The endpoint reads the currently stored review data.

It does NOT trigger a marketplace review refresh.

11.9 Review Refresh Architecture

The refresh mechanism must be separated from the normal user request flow.

Conceptually:

                 User Request
                      |
                      v
              Review API
                      |
                      v
             Stored Reviews

Separately:

        Scheduled Refresh
               |
               v
      Marketplace Connector
               |
               v
       Recent Reviews
               |
               v
       Review Processing
               |
               v
      Temporary Storage

The user-facing Review API must not wait for a marketplace refresh.

11.10 Marketplace Connector Responsibility

The marketplace connector is responsible for obtaining marketplace-specific
review data.

The review service is responsible for:

Processing connector review results
Selecting the recent relevant sample
Persisting the current review set
Handling refresh failures
Serving stored reviews

Marketplace-specific parsing must remain inside the connector layer.

11.11 Connector Review Support

The existing MarketplaceConnector architecture should be extended only
as required to support review retrieval.

Do not break the existing offer/comparison functionality.

The connector abstraction should remain marketplace-independent.

Future connectors must be able to support reviews without requiring changes
to the core review service architecture.

11.12 Review API Security

The review endpoint is public.

No JWT authentication is required.

Users must be able to view reviews without logging in.

11.13 Error Handling

Use the existing GlobalExceptionHandler.

Expected behavior:

Invalid variant ID
    → 404 RESOURCE_NOT_FOUND

No stored reviews
    → Valid response with empty review list

Refresh failure
    → Existing stored reviews remain available

Unexpected backend failure
    → Standardized 500 response

Do not expose marketplace connector internals or stack traces.

11.14 Testing

Stage 5 must test:

Review retrieval from a connector.
Review normalization.
Selection of approximately 10–15 recent reviews.
Fewer than 10 available reviews.
More than 15 available reviews.
Review storage for ProductVariant + Platform.
Successful weekly refresh behavior.
Failed refresh preserves existing reviews.
Review API returns stored reviews.
Review API does not trigger a marketplace refresh.
Missing variant returns 404.
No reviews returns a valid empty response.
Multiple platforms maintain separate review sets.
Existing Stage 1–4 tests continue to pass.

Use deterministic mock review data.

Do not use real external marketplace APIs in tests.

11.15 Out of Scope

Do NOT implement in Stage 5:

Gemini review summaries
AI-generated review analysis
Permanent review history
Sentiment prediction
Review authenticity detection
Real marketplace review APIs
Web scraping
Wishlist
Search history
Frontend review UI

AI processing will be implemented in a later stage.

11.16 Completion Criteria

Stage 5 is complete when:

Review data model exists.
Marketplace connector review support exists.
Recent reviews can be retrieved and normalized.
Approximately 10–15 reviews are maintained per ProductVariant + Platform.
Review refresh is separate from the normal user request.
Successful refresh replaces the current temporary review set.
Failed refresh preserves existing valid reviews.
Public review API works.
Multiple platforms maintain independent review sets.
Review API does not trigger live marketplace retrieval.
All Stage 5 tests pass.
All previous stage tests continue to pass.
No Gemini or AI review analysis is implemented.
No frontend changes are required.

After verification, Stage 5 must be committed to Git before beginning
Stage 6.

12. Stage 6 — User Features

### Status

**NEXT**

### Objective

Implement authenticated user-specific functionality for ShopSense AI.

Stage 6 introduces:

- Wishlist
- Search History

Both features are associated with the authenticated User from Phase 1.

These features must not interfere with the public product catalog,
marketplace comparison, or review APIs.

---

## 12.1 User Feature Architecture

The intended architecture is:

```text
Authenticated User
        |
        +--------------------+
        |                    |
        v                    v
     Wishlist          Search History
        |                    |
        v                    v
 ProductVariant         Search Query


User-specific data must always be associated with the authenticated user.

The application must obtain the user identity from the authenticated JWT
security context.

Do not accept a user ID from the client as the source of authorization.

12.2 Wishlist
Objective

Allow authenticated users to save ProductVariants that they are
interested in.

A wishlist entry belongs to:

User + ProductVariant

It does NOT belong to a marketplace or PlatformOffer.

Example:

User
 |
 +-- iPhone 16 Pro 256GB
 |
 +-- Laptop 16GB / 1TB

The platform and price can change independently after the product is added
to the wishlist.

12.2.1 Wishlist Data Model

Create a wishlist entity representing:

Wishlist entry ID
User
ProductVariant
Created timestamp

The relationship must be:

User
  |
  +-- WishlistEntry
          |
          +-- ProductVariant

Add a database-level unique constraint for:

(user_id, product_variant_id)

This prevents duplicate wishlist entries.

12.2.2 Wishlist Operations

Authenticated users must be able to:

Add a ProductVariant to their wishlist.
View their wishlist.
Remove a ProductVariant from their wishlist.

The user must only be able to access their own wishlist entries.

12.2.3 Wishlist API

Implement:

POST   /api/v1/wishlist
GET    /api/v1/wishlist
DELETE /api/v1/wishlist/{variantId}

All three endpoints are protected.

JWT authentication is required.

12.2.4 Add Wishlist Request

Example:

{
  "variantId": 25
}

The backend must obtain the authenticated user from the security context.

Do not accept:

{
  "userId": 10,
  "variantId": 25
}

as the authorization mechanism.

The authenticated JWT determines the owner.

12.2.5 Duplicate Wishlist Entry

If the user already has the ProductVariant in the wishlist:

POST /api/v1/wishlist

must not create a duplicate entry.

Return an appropriate standardized client error using the existing
exception-handling architecture.

12.2.6 Invalid ProductVariant

If the requested ProductVariant does not exist:

404 RESOURCE_NOT_FOUND

Do not create a wishlist entry.

12.2.7 Wishlist Response

The wishlist response should use DTOs.

Do not expose JPA entities.

A wishlist item should provide enough product information for the frontend
to display the saved item, such as:

Wishlist entry ID
ProductVariant ID
Product ID
Product name
Brand
Model
Variant name
Variant attributes
Added timestamp

Do not retrieve marketplace offers as part of the basic wishlist operation
unless explicitly required by a later feature.

12.3 Search History
Objective

Store the searches performed by authenticated users.

Search history is user-specific information.

It is separate from the product catalog and search engine.

12.3.1 Search History Data Model

Create a SearchHistory entity representing:

Search history ID
User
Search query
Search timestamp

Relationship:

User
 |
 +-- SearchHistory
       |
       +-- query
       +-- searchedAt
12.3.2 Search History Ownership

Every search history record must belong to exactly one User.

The authenticated JWT determines the owner.

Never allow the client to specify another user's ID to access or create
history records.

12.3.3 Search History API

Implement:

POST   /api/v1/search-history
GET    /api/v1/search-history
DELETE /api/v1/search-history/{historyId}
DELETE /api/v1/search-history

All endpoints are protected.

12.3.4 Recording a Search

Example request:

{
  "query": "iphone 16 pro"
}

The backend should:

Obtain the authenticated user.
Validate the query.
Create a SearchHistory record.
Associate it with the authenticated user.
Store the current timestamp.

The search history endpoint is responsible only for recording history.

The product search endpoint remains responsible for searching products.

12.3.5 Search History Validation

Reject:

Null query
Empty query
Whitespace-only query
Excessively long query

Use the existing validation and global exception-handling architecture.

Normalize unnecessary surrounding whitespace before storing the query.

Do not silently invent or modify the user's actual search meaning.

12.3.6 Search History Retrieval

GET /api/v1/search-history must return only the authenticated user's
history.

Results should be ordered from newest to oldest.

The endpoint must never expose another user's search history.

Pagination may be supported if consistent with the existing API conventions,
but it is not mandatory for the initial implementation.

12.3.7 Search History Deletion
Delete one
DELETE /api/v1/search-history/{historyId}

The authenticated user may delete only their own history entry.

If the history entry does not exist, return:

404 RESOURCE_NOT_FOUND

If the history entry belongs to another user, do not expose it.

Use the project's existing authorization/error-handling conventions.

Delete all
DELETE /api/v1/search-history

Delete all search history belonging to the authenticated user only.

Do not delete history belonging to other users.

12.4 Security

The following endpoints are protected:

/api/v1/wishlist/**
/api/v1/search-history/**

The existing JWT authentication mechanism must be reused.

Do not create a second authentication mechanism.

The authenticated principal should be resolved through the existing security
architecture.

12.5 Layered Architecture

Follow:

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

Create appropriate:

Entities
Repositories
DTOs
Services
Controllers

Controllers must remain thin.

Business logic belongs in services.

Database access belongs in repositories.

JPA entities must not be returned directly from REST endpoints.

12.6 Error Handling

Use the existing GlobalExceptionHandler.

Expected cases include:

Unauthenticated request
    → 401 UNAUTHORIZED


Invalid ProductVariant
    → 404 RESOURCE_NOT_FOUND


Duplicate wishlist entry
    → Standardized 400-level response


Invalid search query
    → 400 BAD_REQUEST


Missing history entry
    → 404 RESOURCE_NOT_FOUND

Do not expose database errors, stack traces, or implementation details.

12.7 Data Ownership Rules

The most important security rule in Stage 6 is:

Authenticated User A
        |
        +-- can access User A's wishlist
        +-- can access User A's search history


Authenticated User B
        |
        +-- can access User B's wishlist
        +-- can access User B's search history

User A must never be able to retrieve, modify, or delete User B's
user-specific data by manipulating IDs in the request.

Repository/service queries must enforce ownership.

12.8 Testing

Stage 6 must include tests for:

Wishlist
Authenticated user can add a valid ProductVariant.
Unauthenticated user cannot add a wishlist item.
Duplicate wishlist entry is rejected safely.
Invalid ProductVariant returns 404.
User can retrieve their own wishlist.
User cannot access another user's wishlist entries.
User can delete their own wishlist item.
User cannot delete another user's wishlist item.
Wishlist response uses DTOs.
Unique constraint prevents duplicate user + variant entries.
Search History
Authenticated user can record a valid search.
Unauthenticated user cannot record history.
Empty query is rejected.
Whitespace-only query is rejected.
Overly long query is rejected.
Query whitespace is normalized.
User retrieves only their own history.
History is ordered newest first.
User can delete their own history entry.
User cannot delete another user's history entry.
User can delete all of their own history.
Deleting history does not affect another user.
Regression
All Stage 1–5 tests continue to pass.
Existing authentication functionality remains intact.
Public product/catalog/comparison/review endpoints remain public.

Use authenticated test principals or JWT-based test authentication
consistent with the existing test architecture.

12.9 Database Constraints

The database must enforce the important ownership relationships.

Wishlist:

UNIQUE(user_id, product_variant_id)

Search history:

user_id → users.id

ProductVariant references must use existing foreign-key relationships.

Do not introduce unnecessary denormalized product or platform data.

12.10 Out of Scope

Do NOT implement in Stage 6:

AI recommendations
Gemini
Personalized AI summaries
Price alerts
Price history
Review analysis
Real marketplace integrations
Frontend wishlist UI
Frontend search history UI
Recommendation engine

These will be handled in later stages.

12.11 Completion Criteria

Stage 6 is complete when:

Wishlist entity exists.
SearchHistory entity exists.
Proper foreign keys exist.
Wishlist duplicate constraint exists.
Wishlist APIs work.
Search History APIs work.
JWT authentication is reused.
User ownership is enforced at service/repository level.
JPA entities are not exposed through REST.
Validation works.
Global exception handling is used.
All Stage 6 tests pass.
All Stage 1–5 tests continue to pass.
Public catalog APIs remain accessible.
No AI or recommendation functionality is added.
No frontend changes are required.

After verification, Stage 6 must be committed to Git before beginning
Stage 7.

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