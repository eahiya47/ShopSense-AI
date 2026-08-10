# ShopSense AI – Request Lifecycle

## 1. Purpose

This document defines the end-to-end request lifecycle of ShopSense AI.

It describes how a user request travels through:

- React frontend
- Spring Boot REST API
- Application services
- MySQL database
- Marketplace connectors
- Data normalization
- Review processing
- Gemini AI
- Response generation

The lifecycle is designed to keep responsibilities separated and allow individual components to fail without bringing down the complete application.

---

# 2. High-Level Architecture

```text
                    User
                     |
                     v
              React Frontend
                     |
                     | HTTP / REST
                     v
             Spring Boot API
                     |
          +----------+----------+
          |                     |
          v                     v
       MySQL              Application Services
                                |
                    +-----------+-----------+
                    |                       |
                    v                       v
             Connector Manager          AI Service
                    |                       |
                    v                       v
             Marketplace APIs            Gemini
3. Main Product Discovery Lifecycle

The normal product discovery flow is:

User
 |
 | Search "iPhone"
 v
Product Search API
 |
 v
ShopSense Product Catalog
 |
 v
Relevant Product Results
 |
 | Select Product
 v
Product Details
 |
 | Select Variant
 v
Product Variant
 |
 v
Current Marketplace Comparison
 |
 v
AI Comparison Summary
 |
 v
User
4. Step 1 – User Searches for a Product

The user enters a search query.

Example:

iPhone

The React frontend sends:

GET /api/v1/products/search?q=iphone

No login is required.

5. Step 2 – Search Request Reaches Backend

The Spring Boot controller receives the request.

Conceptually:

ProductSearchController
        |
        v
ProductSearchService
        |
        v
ProductRepository

The controller should not contain search business logic.

The service layer handles:

Query processing
Search criteria
Filtering
Relevance ranking
Pagination
6. Step 3 – Search Catalog

The initial search uses the ShopSense product catalog.

The system may consider:

Brand
Product name
Series
Model
Category
Search relevance
Product popularity
Product ratings
Product recency

Example query:

iphone

may produce:

iPhone 17
iPhone 17 Pro
iPhone 16 Pro
iPhone 16
iPhone 15

The system should return relevant products rather than requiring an exact keyword match.

7. Step 4 – Search Results Returned

The backend returns normalized product information.

Example:

{
  "products": [
    {
      "id": 101,
      "brand": "Apple",
      "model": "iPhone 17",
      "hasVariants": true
    },
    {
      "id": 102,
      "brand": "Apple",
      "model": "iPhone 16 Pro",
      "hasVariants": true
    },
    {
      "id": 103,
      "brand": "Apple",
      "model": "iPhone 16",
      "hasVariants": true
    }
  ]
}

The frontend displays these products as cards.

8. Step 5 – User Selects a Product

The user selects a product card.

Example:

iPhone 16 Pro

The frontend requests:

GET /api/v1/products/{productId}

Example:

GET /api/v1/products/102
9. Step 6 – Product Details

The backend retrieves relatively stable product information from MySQL.

The response may contain:

Product name
Brand
Series
Model
Description
Image
Category
Product specifications

Example:

Product:
iPhone 16 Pro

Specifications:
Processor → A18 Pro
Display   → 6.3 inch
Camera    → 48MP

Marketplace connectors are not required for this basic request.

10. Step 7 – Variant Selection

If the product has meaningful variants, the frontend requests:

GET /api/v1/products/{productId}/variants

Example:

iPhone 16 Pro

Storage:
128GB
256GB
512GB

Color:
Natural Titanium
Black Titanium

The user selects the required configuration.

Example:

256GB / Natural Titanium
11. Standard Variant

If the product has no meaningful user-selectable variants, the system uses an internal:

Standard

variant.

Example:

Washing Machine
    |
    └── Standard Variant

The frontend can skip displaying a variant selection screen when there is only one default variant.

12. Step 8 – Request Current Marketplace Comparison

After a specific product variant is selected, the frontend requests:

GET /api/v1/variants/{variantId}/comparison

Example:

GET /api/v1/variants/1002/comparison

This is the point where live marketplace information becomes necessary.

13. Step 9 – Validate Variant

The backend first verifies that:

The variant exists.
The variant belongs to a valid product.
The product is active/available for comparison.

If the variant does not exist:

404 NOT FOUND

is returned.

The system should not contact marketplaces for an invalid variant.

14. Step 10 – Identify Active Platforms

The backend retrieves active platforms from the platforms table.

Example:

Amazon
Flipkart
Croma
Reliance Digital

Only active connectors should be requested.

If a platform is temporarily disabled:

is_active = false

the connector is skipped.

15. Step 11 – Connector Manager

The backend sends the comparison request to the Connector Manager.

ComparisonService
        |
        v
ConnectorManager
        |
        +---- AmazonConnector
        +---- FlipkartConnector
        +---- CromaConnector
        +---- FutureConnector

The Connector Manager provides a common interface for all marketplace connectors.

16. Step 12 – Marketplace Data Retrieval

Each connector retrieves the current information available for the selected product variant.

The connector may retrieve current marketplace information such as:

- Current price
- Original price
- Seller
- Seller rating
- Availability
- Delivery
- Marketplace offers
- Product URL

Recent reviews follow a separate refresh lifecycle and are not retrieved
from every marketplace during each product comparison request.

Each marketplace may return data in a different structure.

17. Step 13 – Marketplace Normalization

Marketplace responses must be converted into a common ShopSense format.

For example:

Amazon Response
        ↓
AmazonConnector
        ↓
NormalizedOffer

Flipkart Response
        ↓
FlipkartConnector
        ↓
NormalizedOffer

Croma Response
        ↓
CromaConnector
        ↓
NormalizedOffer

The application should not expose marketplace-specific response structures to the frontend.

18. Normalized Offer Structure

Internally, marketplace offers should follow a common structure such as:

platform
productVariant
originalPrice
currentPrice
currency
sellerName
sellerRating
availabilityStatus
availabilityDetails
deliveryInfo
offerDetails
productUrl
lastUpdatedAt

This allows the comparison service to process different marketplaces consistently.

19. Step 14 – Best Offer Selection

If a marketplace contains multiple sellers/offers, the system should select the best suitable offer.

The selection may consider:

Current price
Seller reliability
Seller rating
Availability
Delivery
Current offers
Other trust indicators

The lowest price alone must not automatically determine the selected offer.

20. Step 15 – Current Offer Storage

The selected current offer is stored/updated in:

platform_offers

The database maintains one current representative offer for:

Product Variant + Platform

The database constraint is:

UNIQUE(product_variant_id, platform_id)

When the marketplace data changes, the existing current offer is updated.

Old prices are not permanently retained in Version 1.

21. Step 16 – Marketplace Failure

Individual marketplace failures must be isolated.

Example:

Amazon       → SUCCESS
Flipkart     → SUCCESS
Croma        → FAILED
Reliance     → SUCCESS

The comparison request should still succeed.

The response contains the successful offers and indicates that Croma is temporarily unavailable.

22. Step 17 – No Offer Scenario

A marketplace connector may successfully respond but find no suitable offer.

This is different from a connector failure.

Example:

Amazon → NO_OFFER

The comparison service should distinguish:

AVAILABLE
NO_OFFER
UNAVAILABLE

The frontend can then display the appropriate state.

23. Step 18 – Comparison Response

The Comparison Service returns normalized current offers to the frontend.

Example:

iPhone 16 Pro 256GB

Amazon
₹114,999
Delivery tomorrow

Flipkart
₹113,999
Delivery in 2 days

Croma
₹114,490
Delivery today

The frontend displays the comparison clearly.

24. Step 19 – Marketplace Purchase Redirect

Each offer contains a marketplace product URL.

When the user clicks:

Buy from Flipkart

the frontend redirects the user to the supplied marketplace product URL.

ShopSense AI does not handle:

Cart
Payment
Order
Shipping
Returns
Cancellation

The external marketplace handles the purchase process.

25. Step 20 – Recent Review Processing

Reviews follow a separate refresh lifecycle from the live marketplace
comparison.

The system maintains approximately 10–15 recent relevant reviews for each:

Product Variant + Platform

Reviews are refreshed approximately weekly through the appropriate
marketplace connector.

The refresh process is:

```text
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

26. Step 21 – AI Summary Request

The AI summary is requested only after the user has selected and viewed a specific product variant.

The frontend requests:

GET /api/v1/variants/{variantId}/ai-summary

The AI is therefore not triggered during the initial product search.

27. Step 22 – AI Cache Check

The AI Service first checks the temporary AI summary cache.

AI Summary Cache
       |
       v
Is summary valid?
     /       \
   YES        NO
    |          |
    v          v
Return      Generate
cached      new summary
summary

If the cached summary is still valid, Gemini does not need to be called.

28. Step 23 – Prepare AI Input

If a new summary is required, the backend collects:

Product
Brand
Series
Model
Specifications
Selected Variant
Variant name
Variant attributes
Current Marketplace Offers
Platform
Current price
Original price
Seller
Seller rating
Availability
Delivery
Offers
Recent Reviews
Platform
Rating
Review text
Review date

The backend prepares this information as structured AI input.

29. Step 24 – Gemini Processing

The AI Service sends the structured information to Gemini.

The prompt instructs Gemini to:

Use only supplied information.
Not invent missing information.
Focus on the selected product variant.
Compare available marketplaces.
Summarize recent reviews fairly.
Explain trade-offs.
Avoid pushing the user toward a purchase.
Avoid recommending unrelated products.
30. Step 25 – AI Result Validation

The backend should validate the AI response before returning it to the frontend.

The response should be checked for:

Valid structure
Required fields
Correct product variant
No unexpected marketplace names
No malformed values
No unsupported claims where validation is possible

If the response cannot be validated, the backend should treat the AI request as failed.

31. Step 26 – AI Cache Update

After successful AI generation and validation:

AI Result
   |
   v
ai_summaries

The current cached summary is updated.

The cache contains:

product_variant_id
summary
generated_at
expires_at
32. Step 27 – AI Response to Frontend

The frontend receives the validated AI summary.

Example:

Best Price:
Flipkart

Fastest Delivery:
Amazon

Best Rated Offer:
Croma

Review Summary:
Recent reviews are generally positive...

The AI section appears on the selected product's detail/comparison page.

33. Complete Comparison Lifecycle

The complete lifecycle can be represented as:

User
 |
 | "iPhone"
 v
React Search
 |
 v
Product Search API
 |
 v
MySQL Product Catalog
 |
 v
Relevant Products
 |
 | Select iPhone 16 Pro
 v
Product Details API
 |
 v
Product Information
 |
 | Select 256GB / Titanium
 v
Variant API
 |
 v
Selected Product Variant
 |
 v
Comparison API
 |
 v
Connector Manager
 |
 +---- Amazon
 +---- Flipkart
 +---- Croma
 +---- Other Platforms
 |
 v
Normalize Marketplace Data
 |
 v
Select Best Offer Per Platform
 |
 v
Update Current Offers
 |
 v
Comparison Response
 |
 v
React Comparison Page
 |
 | Request AI Summary
 v
AI Cache
 |
 +---- Valid → Return Cached Summary
 |
 +---- Expired
          |
          v
     Recent Reviews
          +
     Current Offers
          +
     Product Data
          |
          v
        Gemini
          |
          v
     Validate Result
          |
          v
      Update Cache
          |
          v
     AI Comparison
          |
          v
          User
34. Authenticated User Lifecycle

Authentication is not required for normal product discovery.

If the user is logged in, additional functionality becomes available.

User
 |
 v
Login
 |
 v
JWT Token
 |
 v
Authenticated Frontend
 |
 +---- Wishlist
 |
 +---- Search History
 |
 +---- Future Alerts

The JWT is attached to protected API requests.

35. Search History Lifecycle

When an authenticated user searches:

User searches "iPhone 16"
        |
        v
Product Search API
        |
        v
Search Results
        |
        v
Search History Service
        |
        v
search_history table

The search itself remains public.

Only persistent user-specific history requires authentication.

36. Wishlist Lifecycle

When an authenticated user adds a product variant to their wishlist:

User
 |
 v
Add Wishlist
 |
 v
Wishlist API
 |
 v
Validate JWT
 |
 v
Validate Product Variant
 |
 v
Save Wishlist Entry
 |
 v
MySQL

The database prevents duplicate wishlist entries for the same user and product variant.

37. Error Propagation

Errors should be handled at the appropriate layer.

Marketplace Failure
        ↓
Connector Layer
        ↓
Mark Platform Unavailable
        ↓
Continue Comparison
Gemini Failure
        ↓
AI Service
        ↓
Return AI Unavailable
        ↓
Core Comparison Continues
Invalid Product
        ↓
Service Layer
        ↓
404 Response
        ↓
Frontend Error State

No single optional external service should bring down the entire product comparison experience.

38. Request Lifecycle Responsibilities
Frontend

Responsible for:

User interaction
Search input
Product cards
Variant selection
Comparison display
AI summary display
Wishlist interaction
Marketplace redirection
Authentication state
Controller Layer

Responsible for:

HTTP requests
Request validation
Response DTOs
HTTP status codes
Service Layer

Responsible for:

Business logic
Product search
Variant handling
Comparison orchestration
Best-offer selection
AI workflow
Wishlist logic
Search history logic
Repository Layer

Responsible for:

Database access
Query execution
Persistence
Connector Layer

Responsible for:

Marketplace communication
Marketplace response parsing
Marketplace-specific normalization
AI Service

Responsible for:

Preparing AI input
Gemini communication
AI response validation
AI summary caching
Database

Responsible for:

Persistent product data
Current marketplace data
Temporary review data
Temporary AI cache
User data
39. Performance Principles

The lifecycle is designed to avoid unnecessary external requests.

Product Search

Uses the local ShopSense catalog.

Product Details

Uses local product data.

Variant Selection

Uses local variant data.

Marketplace Comparison

Uses live marketplace data only after a specific variant is selected.

AI Summary

Uses a temporary cache to avoid unnecessary Gemini requests.

Reviews

Use the temporarily stored recent review set instead of retrieving reviews for every page request.

This approach reduces:

Marketplace API calls
Gemini API calls
Response latency
External service dependency
40. Data Freshness Principles

Different data types have different freshness requirements.

Data	Source	Freshness
Product information	ShopSense DB	Relatively static
Product specifications	ShopSense DB	Relatively static
Variants	ShopSense DB	Relatively static
Current prices	Marketplace connectors	Live/current
Availability	Marketplace connectors	Live/current
Delivery	Marketplace connectors	Live/current
Offers	Marketplace connectors	Live/current
Reviews	Temporary storage	Approximately weekly
AI Summary	Gemini cache	Temporary
41. Lifecycle Design Principle

The most important lifecycle principle is:

Fetch data at the point where freshness matters, and use stored data where repeated external requests are unnecessary.

Therefore:

Static Product Data
        ↓
Stored in ShopSense DB

Current Marketplace Data
        ↓
Fetched through connectors

Recent Reviews
        ↓
Temporarily stored and refreshed weekly

AI Summary
        ↓
Temporarily cached

This hybrid approach balances:

Freshness
Performance
Cost
Reliability
Scalability
42. Future Lifecycle Extensions

The lifecycle is designed to support future functionality without changing the fundamental architecture.

Potential future additions include:

Price history
Price-drop alerts
Price prediction
Semantic product search
AI-powered cross-product recommendations
Personalized recommendations
Notifications
Additional marketplace connectors

These features should be added as separate services/modules rather than tightly coupling them to the existing comparison workflow.

43. Final Lifecycle Principle

ShopSense AI follows this overall pattern:

Discover
   ↓
Select
   ↓
Identify Exact Variant
   ↓
Fetch Current Marketplace Data
   ↓
Normalize
   ↓
Compare
   ↓
Analyze Recent Reviews
   ↓
Generate/Load AI Insight
   ↓
Present
   ↓
Redirect to Marketplace

The system remains focused on helping users make informed purchasing decisions while keeping product data, marketplace data, and AI analysis clearly separated.