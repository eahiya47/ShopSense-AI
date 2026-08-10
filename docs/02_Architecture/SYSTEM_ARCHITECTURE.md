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

---

# ShopSense AI – Target System Architecture

## 3. Architecture Vision

The target architecture of ShopSense AI is designed as a modular, scalable e-commerce product discovery and comparison platform.

The system is designed around the following principle:

> ShopSense AI helps users make informed purchasing decisions by comparing the same product across multiple e-commerce platforms, without acting as the marketplace itself.

The platform will:

- Allow users to search for products without requiring authentication.
- Display relevant and highly rated products based on the user's search.
- Allow users to select a specific product and, when applicable, a specific variant.
- Retrieve current marketplace information from supported platforms.
- Compare prices, sellers, delivery information, ratings, and offers.
- Collect a limited set of recent customer reviews from supported platforms.
- Provide an AI-generated comparison summary for the selected product.
- Redirect users to the official marketplace product page for purchasing.
- Provide authenticated features such as wishlists and search history.
- Support the addition of new marketplaces without requiring major changes to the core application.

---

## 4. Target High-Level Architecture

```text
                              USER
                                |
                                v
                        React Frontend
                                |
                         HTTP / REST API
                                |
                                v
                       Spring Boot Backend
                                |
          +---------------------+---------------------+
          |                     |                     |
          v                     v                     v
   Authentication        Product/Search          User Features
      Module                Module              Wishlist/History
                                |
                                v
                         Search Service
                                |
                                v
                        Product Catalog
                                |
                                v
                     Relevant Product Results
                                |
                                v
                       Product Selection
                                |
                         Has Variants?
                         /           \
                       Yes            No
                        |              |
                        v              |
                 Variant Selection    |
                        |              |
                        +------+-------+
                               |
                               v
                       Connector Manager
                               |
             +-----------------+-----------------+
             |                 |                 |
             v                 v                 v
       Amazon Connector  Flipkart Connector  Croma Connector
             |                 |                 |
             +-----------------+-----------------+
                               |
                               v
                    Normalized Marketplace Data
                               |
                               v
                     Product Comparison Engine
                               |
                 +-------------+-------------+
                 |                           |
                 v                           v
          Latest Reviews               Platform Offers
                 |                           |
                 +-------------+-------------+
                               |
                               v
                         Gemini AI
                               |
                               v
                     AI Comparison Insights
                               |
                               v
                    Product Details Response
                               |
                               v
                        React Frontend
5. Core Architectural Principles
5.1 Separation of Responsibilities

Each major component of the system should have a clearly defined responsibility.

The system should avoid placing all business logic inside controllers or a single service.

The major responsibilities are separated into:

Authentication
User management
Product management
Search
Product normalization
Marketplace connectors
Product comparison
Review processing
AI analysis
Wishlist management
Search history
5.2 Modular Architecture

The system should be designed so that individual modules can be changed or expanded without requiring major changes to unrelated modules.

For example, adding a new marketplace should primarily require adding a new connector rather than modifying the entire search system.

5.3 Platform Independence

The core ShopSense AI system should not depend on the internal data format or naming conventions of any individual marketplace.

Marketplace-specific information must be converted into the standardized ShopSense AI format before entering the comparison layer.

5.4 AI as an Assistance Layer

AI should enhance the user's understanding of the selected product rather than control product discovery or purchasing decisions.

The AI should compare the selected product across platforms and explain differences.

The AI should not attempt to persuade the user to purchase a different product.

6. Product Data Architecture

ShopSense AI follows a hybrid product data architecture.

6.1 Static Product Data

Information that changes infrequently will be maintained in the ShopSense AI database.

Examples include:

Product name
Brand
Series
Model
Category
Product description
Product specifications
Product images
Product family
Variant definitions

This information forms the application's product catalog.

6.2 Dynamic Marketplace Data

Information that changes frequently should be obtained from marketplace connectors.

Examples include:

Current price
Seller
Availability
Delivery information
Current offers
Discounts
Marketplace-specific ratings
Latest customer reviews

This prevents outdated marketplace information from becoming permanently stored as the source of truth.

6.3 Product Identity

Each product will have a unique internal ShopSense AI Product ID.

The same physical product sold by multiple marketplaces will reference the same ShopSense AI product rather than creating separate products for every marketplace.

Example:

ShopSense Product ID: 101

Apple iPhone 16 Pro
        |
        +---- Amazon Offer
        |
        +---- Flipkart Offer
        |
        +---- Croma Offer

7. Product Variant Architecture

Every product must have at least one ProductVariant.

Products with meaningful selectable configurations use user-visible variants.

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

The frontend does not need to display the Standard variant as a
user-facing selection. If a product has only the Standard variant,
the frontend can proceed directly to the product comparison flow.

8. Marketplace Connector Architecture

ShopSense AI will use a connector-based architecture for marketplace integrations.

The Search Service will not directly communicate with individual marketplaces.

Instead, communication will follow:

Search Service
      |
      v
Connector Manager
      |
      +---- Amazon Connector
      |
      +---- Flipkart Connector
      |
      +---- Croma Connector
      |
      +---- Reliance Digital Connector
      |
      +---- Future Connectors

Each connector is responsible for understanding the data format and integration requirements of its assigned marketplace.

8.1 Connector Responsibilities

A marketplace connector is responsible for:

Searching or retrieving marketplace product information.
Retrieving current marketplace offers.
Retrieving available seller and delivery information where supported.
Retrieving relevant recent reviews where supported as part of the review refresh process.
Converting marketplace-specific data into the ShopSense AI standardized format.
Returning normalized results to the Connector Manager.
8.2 Adding New Marketplaces

The architecture should allow new marketplaces to be added independently.

For example, adding a new marketplace such as Vijay Sales should primarily involve implementing:

VijaySalesConnector

The existing Search Service, Comparison Engine, and AI workflow should not require major architectural changes.

9. Product Normalization

Different marketplaces may describe the same product using different names and formats.

Example:

Amazon:
Apple iPhone 16 Pro (Natural Titanium, 128GB)

Flipkart:
Apple iPhone 16 Pro 128 GB - Natural Titanium

Croma:
iPhone 16 Pro - 128GB - Natural Titanium

These should be converted into a standardized ShopSense AI representation:

Brand       : Apple
Series      : iPhone
Model       : 16 Pro
Storage     : 128GB
Color       : Natural Titanium
Category    : Smartphone

Product normalization allows the comparison engine to determine whether marketplace listings represent the same product or variant.

9.1 Primary Matching Strategy

Product normalization should be the primary method of identifying matching products.

This approach is:

Fast
Cost-efficient
Deterministic
Easier to maintain
Independent of AI services
9.2 AI Matching Fallback

In rare cases where normalized attributes are insufficient to determine whether two listings represent the same product, an AI-assisted verification mechanism may be introduced.

AI matching should be treated as a fallback rather than the primary product matching mechanism.

This reduces unnecessary AI usage and associated processing costs.

10. Search Architecture

ShopSense AI Version 1 will use a smart, relevance-based search system.

The search engine will not depend solely on exact keyword matching.

For example:

Search:
iPhone

may return:

iPhone 17 Pro Max
iPhone 17
iPhone 16 Pro
iPhone 16
iPhone 15

The search engine should consider multiple factors when ranking results, including:

Search relevance
Product popularity
Customer ratings
Product recency

The exact ranking algorithm can be refined as the system develops.

10.1 Future AI Search

AI-powered semantic search is intentionally outside the initial implementation.

Future versions may support natural-language searches such as:

Best phone for photography under ₹70000

or:

Laptop suitable for AI engineering

This can be introduced later without replacing the core product and connector architecture.

11. Product Comparison Architecture

Once a user selects a specific product and applicable variant, the system retrieves current marketplace information.

Example:

Apple iPhone 16 Pro – 256GB

Amazon
Price: ₹114,999

Flipkart
Price: ₹113,999

Croma
Price: ₹114,490

The Comparison Engine will organize information from different connectors into a consistent comparison structure.

Comparison information may include:

Current price
Seller
Seller rating
Delivery information
Availability
Offers
Discounts
Marketplace rating
Product URL
12. Review Processing

ShopSense AI will not permanently store every review from every marketplace.

The system will focus on a limited number of recent and relevant reviews.

The intended approach is:

Marketplace
     |
     v
Review Refresh Process
     |
     v
Retrieve recent reviews
     |
     v
Select approximately 10–15 relevant reviews
     |
     v
Temporary Review Storage
     |
     v
Used by AI Summary

The goal is to provide a representative and unbiased overview without maintaining a massive review database.

The system should avoid intentionally selecting only positive or only negative reviews.

13. AI Architecture

Gemini AI will be used primarily as a product comparison assistant.

AI processing will occur after the user has selected a specific product and, where applicable, a specific variant.

The AI should receive structured information such as:

Product information
Selected variant
Marketplace prices
Seller information
Delivery information
Offers
Ratings
Recent customer reviews

The AI should generate structured insights such as:

Best current price
Fastest delivery
Better-rated seller
Better available offer
Review summary
Common positive feedback
Common negative feedback
Overall platform comparison
13.1 AI Scope

The AI must remain focused on the product selected by the user.

For example, if the user selects:

iPhone 16 Pro 128GB

the AI should compare:

iPhone 16 Pro 128GB

Amazon vs Flipkart vs Croma

It should not automatically recommend:

Buy iPhone 17 instead.

The user makes the final purchasing decision.

14. User Architecture

ShopSense AI supports both public and authenticated functionality.

Public Features

Authentication should not be required for:

Product search
Product discovery
Product comparison
Product details
AI comparison insights
Redirecting to marketplace product pages
Authenticated Features

Authentication is required for:

Wishlist
Search history
Future price alerts
Future personalized features

Phase 1 already establishes JWT-based authentication.

15. Wishlist Architecture

Authenticated users can save selected products or product variants to their wishlist.

The wishlist should reference the ShopSense AI product variant rather than a marketplace-specific offer.

This allows the user's wishlist to remain valid even when marketplace prices change.

Example:

User
  |
  v
Wishlist
  |
  v
iPhone 16 Pro – 256GB
16. Search History Architecture

Authenticated users can have their search queries stored.

The search history will contain:

User ID
Search query
Search timestamp

The initial system will focus only on searched queries rather than tracking every user interaction.

17. Request Lifecycle

The main product discovery and comparison flow is:

User searches for a product
        |
        v
Frontend sends Search API request
        |
        v
Search Service
        |
        v
Product Catalog
        |
        v
Relevant Products
        |
        v
Frontend displays Product Cards
        |
        v
User selects a Product
        |
        v
Check whether the Product has Variants
        |
        +------ Yes ------> Variant Selection
        |                         |
        |                         v
        +------ No --------------+
                                  |
                                  v
                         Connector Manager
                                  |
                    +-------------+-------------+
                    |             |             |
                    v             v             v
                 Amazon       Flipkart        Croma
                    |             |             |
                    +-------------+-------------+
                                  |
                                  v
                         Normalize Results
                                  |
                                  v
                         Compare Marketplace
                                  |
                                  v
                         Retrieve Recent Reviews
                                  |
                                  v
                              Gemini AI
                                  |
                                  v
                       Product Details Response
                                  |
                                  v
                           React Frontend
18. Error Handling and Fault Tolerance

The failure of one external marketplace should not cause the entire application to fail.

For example:

Amazon        ❌ Unavailable
Flipkart      ✅ Available
Croma         ✅ Available
Reliance      ✅ Available

The system should continue processing the available platforms.

The frontend should clearly indicate that Amazon data is temporarily unavailable.

18.1 AI Failure

If the Gemini AI service is unavailable, the product comparison should continue to work.

The user should still receive:

Product information
Current marketplace offers
Seller information
Delivery information
Reviews where available

Only the AI comparison section should indicate that AI insights are temporarily unavailable.

AI failure must not make the core comparison functionality unavailable.

19. Data Freshness Strategy

Static product information will be stored in the ShopSense AI database.

Dynamic marketplace information should be obtained as close to the time of the user's request as practical.

A short-lived cache may be introduced to avoid repeatedly requesting identical marketplace data when many users search for the same product within a short period.

The cache must not become the permanent source of truth for marketplace prices.

20. Price History Strategy

The initial system does not require permanent long-term price history.

Current marketplace prices should be prioritized.

A limited price history mechanism may be introduced in the future to support:

Price history graphs
Price trend analysis
Price-drop notifications
AI price predictions

The architecture should allow this functionality to be added without changing the core Product and PlatformOffer concepts.

21. Scalability and Future Expansion

The architecture is designed to support future capabilities without requiring a fundamental redesign.

Potential future features include:

Additional marketplace connectors
Price alerts
Price history
Price trend visualization
AI semantic search
Personalized product recommendations
AI shopping assistant
Browser extension
Mobile application
Additional product categories
Region-specific marketplace support

New features should be implemented as independent modules or extensions whenever possible.

22. Architectural Principles

The following principles should be maintained throughout development:

Keep product data independent from marketplace data.
Use one ShopSense Product ID for the same product across platforms.
Every product must have at least one ProductVariant.
Products with meaningful selectable configurations use user-visible variants.
Products without meaningful selectable configurations use an internal Standard variant.
Keep marketplace integrations isolated inside connectors.
Normalize marketplace data before comparison.
Do not make AI responsible for basic deterministic operations.
Use AI only where it provides meaningful value.
Do not allow failure of one marketplace to break the entire application.
Do not make AI a dependency for the core product comparison functionality.
Keep APIs focused on a single responsibility.
Design current features so future functionality can be added without major architectural changes.
The final purchasing decision always remains with the user.
23. Phase-wise Architecture Evolution
Phase 1 – Foundation

Implemented:

React frontend
Spring Boot backend
MySQL database
User registration
User login
JWT authentication
User profile
Basic application architecture
Phase 2A – Product Search Foundation

Planned:

Product catalog
Product variants
Platform model
Platform offer model
Smart search
Search API
Product API
Variant handling
Connector interface
Mock marketplace connectors
Normalized marketplace response structure
Product comparison foundation
Phase 2B – Marketplace Integration

Planned:

Real marketplace connectors
Current marketplace offers
Marketplace product URLs
Seller information
Delivery information
Availability
Marketplace reviews

Actual integrations will depend on the permitted and technically available data-access methods of each marketplace.

Future Phases

Planned future capabilities include:

AI comparison assistant
Advanced review analysis
Price history
Price alerts
AI semantic search
Personalized shopping assistance
Additional marketplace integrations
Mobile and browser-based experiences
24. Architecture Summary

ShopSense AI is designed around a modular layered architecture in which the frontend communicates with a Spring Boot backend through REST APIs.

The backend separates authentication, user management, search, product management, marketplace integration, comparison, and AI processing into distinct responsibilities.

Product information is maintained independently from marketplace offers. Marketplace integrations are isolated behind connectors, allowing additional platforms to be introduced without redesigning the core application.

The system uses deterministic product normalization as the primary mechanism for identifying the same product across platforms. AI is reserved for higher-level analysis and comparison rather than basic data processing.

The architecture therefore provides a balance between:

Simplicity
Maintainability
Cost efficiency
Reliability
Scalability
Future AI integration

The architecture described in this document represents the target architecture for the complete ShopSense AI platform. Individual phases will implement this architecture incrementally.