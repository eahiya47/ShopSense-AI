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
---

# Target API Architecture

The following APIs extend the Phase 1 authentication foundation to support the complete ShopSense AI product discovery and comparison system.

## 4. API Architecture Principles

ShopSense AI uses RESTful APIs exposed through the Spring Boot backend.

Base URL:

`/api/v1`

The API architecture follows these principles:

- Product discovery is public and does not require authentication.
- Product comparison is public and does not require authentication.
- AI comparison summaries are public and do not require authentication.
- Wishlist functionality requires authentication.
- Search history functionality requires authentication.
- Marketplace connectors are internal backend services.
- Gemini AI is an internal backend integration.
- The frontend must never directly communicate with marketplace connectors or Gemini.
- JPA entities must not be exposed directly through API responses.
- DTOs should be used for API request and response models.
- External marketplace data must be normalized before being returned to the frontend.
- Failure of one marketplace must not cause the entire comparison request to fail.
- Failure of Gemini must not cause the core product comparison to fail.

---

# 5. Product Search

## Endpoint

`GET /products/search`

## Access

Public

## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `q` | Yes | User's search query |
| `page` | No | Result page, default 0 |
| `size` | No | Number of results, default 20 |
| `category` | No | Optional category filter |

## Example

`GET /api/v1/products/search?q=iphone`

## Example Response

```json
{
  "query": "iphone",
  "page": 0,
  "size": 20,
  "totalResults": 5,
  "products": [
    {
      "id": 101,
      "brand": "Apple",
      "series": "iPhone",
      "model": "17",
      "category": "Smartphone",
      "imageUrl": "...",
      "hasVariants": true
    },
    {
      "id": 102,
      "brand": "Apple",
      "series": "iPhone",
      "model": "16 Pro",
      "category": "Smartphone",
      "imageUrl": "...",
      "hasVariants": true
    },
    {
      "id": 103,
      "brand": "Apple",
      "series": "iPhone",
      "model": "16",
      "category": "Smartphone",
      "imageUrl": "...",
      "hasVariants": true
    }
  ]
}
Search Behavior

Search should not depend only on exact keyword matching.

For example:

iphone

may return:

iPhone 17
iPhone 17 Pro
iPhone 16 Pro
iPhone 16
iPhone 15

The initial search system should consider:

Product name
Brand
Series
Model
Category
Search relevance
Product popularity
Product ratings
Product recency

The initial implementation should use deterministic search and ranking.

AI-powered semantic search is a future feature.

6. Get Product Details
Endpoint

GET /products/{productId}

Access

Public

Example

GET /api/v1/products/102

Example Response
{
  "id": 102,
  "brand": "Apple",
  "series": "iPhone",
  "model": "16 Pro",
  "category": {
    "id": 10,
    "name": "Smartphone"
  },
  "description": "Apple iPhone 16 Pro",
  "imageUrl": "...",
  "hasVariants": true,
  "specifications": [
    {
      "name": "Processor",
      "value": "A18 Pro"
    },
    {
      "name": "Display",
      "value": "6.3 inch"
    }
  ]
}

This endpoint returns relatively stable ShopSense product information.

It should not be responsible for retrieving live marketplace offers.

7. Get Product Variants
Endpoint

GET /products/{productId}/variants

Access

Public

Example

GET /api/v1/products/102/variants

Example Response
{
  "productId": 102,
  "variants": [
    {
      "id": 1001,
      "name": "128GB / Natural Titanium",
      "isDefault": false,
      "attributes": [
        {
          "name": "Storage",
          "value": "128GB"
        },
        {
          "name": "Color",
          "value": "Natural Titanium"
        }
      ]
    },
    {
      "id": 1002,
      "name": "256GB / Natural Titanium",
      "isDefault": false,
      "attributes": [
        {
          "name": "Storage",
          "value": "256GB"
        },
        {
          "name": "Color",
          "value": "Natural Titanium"
        }
      ]
    }
  ]
}

For a product without meaningful user-selectable variants, the API returns the internal Standard variant.

{
  "productId": 500,
  "variants": [
    {
      "id": 5001,
      "name": "Standard",
      "isDefault": true,
      "attributes": []
    }
  ]
}

The frontend does not need to display the word Standard to the user.

### Variant Rule

Every product has at least one ProductVariant.

Products with meaningful selectable configurations expose those variants
to the user.

Products without meaningful selectable configurations use an internal
Standard variant. The frontend does not need to display "Standard" as a
user-facing selection.

8. Product Comparison

This is one of the core ShopSense AI APIs.

Endpoint

GET /variants/{variantId}/comparison

Access

Public

Example

GET /api/v1/variants/1002/comparison

Backend Responsibilities

The backend should:

Validate the selected product variant.
Identify active marketplace connectors.
Request current marketplace information through the Connector Manager.
Normalize marketplace-specific responses.
Select the best suitable offer from each platform.
Return the normalized comparison result.

The frontend must not communicate directly with individual marketplaces.

9. Comparison Response

Example:

{
  "variant": {
    "id": 1002,
    "productId": 102,
    "name": "256GB / Natural Titanium",
    "attributes": [
      {
        "name": "Storage",
        "value": "256GB"
      },
      {
        "name": "Color",
        "value": "Natural Titanium"
      }
    ]
  },
  "offers": [
    {
      "platform": {
        "id": 1,
        "name": "Amazon",
        "logoUrl": "..."
      },
      "originalPrice": 119999,
      "currentPrice": 114999,
      "currency": "INR",
      "sellerName": "Amazon",
      "sellerRating": 4.7,
      "availabilityStatus": "IN_STOCK",
      "availabilityDetails": "Only 2 left in stock",
      "deliveryInfo": "Delivery tomorrow",
      "offerDetails": "Bank discount available",
      "productUrl": "..."
    },
    {
      "platform": {
        "id": 2,
        "name": "Flipkart",
        "logoUrl": "..."
      },
      "originalPrice": 119999,
      "currentPrice": 113999,
      "currency": "INR",
      "sellerName": "XYZ Electronics",
      "sellerRating": 4.8,
      "availabilityStatus": "IN_STOCK",
      "availabilityDetails": "In stock",
      "deliveryInfo": "Delivery in 2 days",
      "offerDetails": "Bank offer available",
      "productUrl": "..."
    }
  ]
}
10. Marketplace Failure Handling

The comparison API must support partial results.

For example:

Amazon       FAILED
Flipkart     SUCCESS
Croma        SUCCESS

The API should still return the available platform offers.

Example:

{
  "offers": [
    {
      "platform": "Flipkart"
    },
    {
      "platform": "Croma"
    }
  ],
  "platformStatus": [
    {
      "platform": "Amazon",
      "status": "UNAVAILABLE",
      "message": "Marketplace data is temporarily unavailable."
    }
  ]
}

A failed marketplace must not cause the entire comparison request to fail.

11. Platform Status

The comparison response should allow the frontend to distinguish between:

AVAILABLE
UNAVAILABLE
NO_OFFER

This prevents missing marketplace data from being incorrectly interpreted as product unavailability.

12. Recent Reviews
Endpoint

GET /variants/{variantId}/reviews

Access

Public

Query Parameters
Parameter	Required	Description
platformId	No	Filter reviews by platform
limit	No	Maximum number of reviews
Example

GET /api/v1/variants/1002/reviews?platformId=1&limit=15

Example Response
{
  "variantId": 1002,
  "platform": {
    "id": 1,
    "name": "Amazon"
  },
  "reviews": [
    {
      "rating": 5,
      "text": "Excellent camera and performance.",
      "reviewDate": "2026-08-01"
    },
    {
      "rating": 4,
      "text": "Battery life is good.",
      "reviewDate": "2026-07-30"
    }
  ],
  "lastUpdated": "2026-08-08T10:00:00"
}

The API should expose the currently stored recent review set.

The system will generally maintain approximately 10–15 relevant recent reviews per platform.

Reviews are refreshed approximately weekly.

13. AI Comparison Summary

AI insights become available after the user has selected and viewed a specific product variant.

Endpoint

GET /variants/{variantId}/ai-summary

Access

Public

Example

GET /api/v1/variants/1002/ai-summary

Processing Flow

The backend should:

Check for a valid cached AI summary.
Return the cached summary if it is still valid.
If the cache has expired, collect the latest comparison information.
Use the currently stored recent reviews.
Send structured information to Gemini.
Generate the comparison summary.
Update the temporary AI cache.
Return the summary.
14. AI Summary Response

Example:

{
  "variantId": 1002,
  "summary": {
    "bestPrice": {
      "platform": "Flipkart",
      "price": 113999
    },
    "bestDelivery": {
      "platform": "Amazon",
      "details": "Delivery tomorrow"
    },
    "bestRatedOffer": {
      "platform": "Croma",
      "rating": 4.8
    },
    "bestOffer": {
      "platform": "Flipkart",
      "reason": "Lowest current price with a highly rated seller."
    },
    "reviewSummary": "Recent reviews are generally positive, with users praising camera quality and performance. Some users mention battery life as an area for improvement."
  },
  "generatedAt": "2026-08-08T10:00:00",
  "expiresAt": "2026-08-08T16:00:00"
}

The exact AI response structure may be refined during implementation.

15. AI Scope

The AI comparison must remain focused on the product selected by the user.

For example:

Selected Product:
iPhone 16 Pro 128GB

The AI compares:

Amazon
Flipkart
Croma

It should not automatically recommend another product such as:

iPhone 17
Samsung Galaxy
OnePlus

The current AI role is:

Explain the differences between marketplace offers for the specific product selected by the user.

The final purchasing decision always remains with the user.

16. Wishlist

Wishlist functionality requires authentication.

Add to Wishlist
Endpoint

POST /wishlist

Access

Protected

Request
{
  "productVariantId": 1002
}
Response

201 Created

{
  "message": "Product added to wishlist.",
  "productVariantId": 1002
}

The same user cannot add the same product variant more than once.

17. Get Wishlist
Endpoint

GET /wishlist

Access

Protected

Response
{
  "items": [
    {
      "id": 1,
      "productVariantId": 1002,
      "productName": "iPhone 16 Pro",
      "variantName": "256GB / Natural Titanium",
      "addedAt": "2026-08-08T10:00:00"
    }
  ]
}
18. Remove Wishlist Item
Endpoint

DELETE /wishlist/{productVariantId}

Access

Protected

Response

204 No Content

A user may only remove their own wishlist items.

19. Search History

The actual product search is public.

Persistent search history requires authentication.

Add Search History
Endpoint

POST /search-history

Access

Protected

Request
{
  "query": "iphone 16"
}
Response
{
  "message": "Search history saved."
}
20. Get Search History
Endpoint

GET /search-history

Access

Protected

Response
{
  "history": [
    {
      "id": 1,
      "query": "iphone 16",
      "searchedAt": "2026-08-08T10:00:00"
    },
    {
      "id": 2,
      "query": "gaming laptop",
      "searchedAt": "2026-08-07T18:30:00"
    }
  ]
}
21. Delete Search History
Endpoint

DELETE /search-history/{id}

Access

Protected

Response

204 No Content

A user may only delete their own search history entries.

22. Public and Protected API Summary
API	Authentication
Register	Public
Login	Public
User Profile	Required
Product Search	Public
Product Details	Public
Product Variants	Public
Product Comparison	Public
Recent Reviews	Public
AI Summary	Public
Add Wishlist	Required
Get Wishlist	Required
Remove Wishlist	Required
Add Search History	Required
Get Search History	Required
Delete Search History	Required
23. HTTP Status Codes

The API should use standard HTTP status codes.

Status	Meaning
200 OK	Successful request
201 Created	Resource successfully created
204 No Content	Successful deletion
400 Bad Request	Invalid request
401 Unauthorized	Authentication required or invalid
403 Forbidden	Authenticated but not permitted
404 Not Found	Resource not found
409 Conflict	Duplicate or conflicting resource
422 Unprocessable Entity	Valid request structure but invalid business data
429 Too Many Requests	Rate limit exceeded
500 Internal Server Error	Unexpected server error
502 Bad Gateway	External service failure where applicable
503 Service Unavailable	Temporary service unavailability
24. Standard Error Response

All API errors should use a consistent structure.

Example:

{
  "timestamp": "2026-08-08T10:00:00",
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Product variant not found.",
  "path": "/api/v1/variants/1002/comparison"
}

The backend must not expose stack traces, database details, API keys, or other sensitive implementation information to clients.

25. Authentication Header

Protected APIs require:

Authorization: Bearer <JWT_TOKEN>

Example:

GET /api/v1/users/profile

Authorization: Bearer eyJhbGciOiJIUzI1Ni...
26. Marketplace Redirect

ShopSense AI does not process purchases.

When the user selects a platform's purchase button, the frontend uses the productUrl returned by the comparison API.

Example:

User clicks:
"Buy from Flipkart"
        |
        v
Open productUrl
        |
        v
Official marketplace product page

The marketplace handles:

Cart
Payment
Order
Shipping
Returns
Cancellation

ShopSense AI only assists the user in making the purchasing decision.

27. Connector Boundary

Marketplace connectors are internal backend components.

The frontend must never directly communicate with marketplace connectors.

React Frontend
      |
      v
Spring Boot REST API
      |
      v
Connector Manager
      |
      +---- Amazon Connector
      +---- Flipkart Connector
      +---- Croma Connector
      +---- Future Connectors

Marketplace-specific implementation details remain inside the backend.

28. AI Service Boundary

Gemini is an internal backend integration.

The frontend must never directly call Gemini using a secret API key.

React Frontend
      |
      v
Spring Boot REST API
      |
      v
AI Service
      |
      v
Gemini API

Gemini API credentials must remain on the backend/server environment.

29. DTO-Based API Responses

JPA entities must not be exposed directly to the frontend.

The backend should use dedicated DTOs such as:

ProductResponse
ProductVariantResponse
PlatformOfferResponse
ReviewResponse
AISummaryResponse
WishlistResponse
SearchHistoryResponse

This keeps the public API independent of the internal database implementation.

30. Pagination

Pagination should be used for potentially large result sets.

Examples include:

Product search results
Search history
Wishlist
Reviews where applicable

Example:

?page=0&size=20

The API should return appropriate pagination metadata.

31. API Versioning

All current APIs use:

/api/v1

Future breaking API changes may use:

/api/v2

This prevents future API changes from unnecessarily breaking existing clients.

32. Future API Extensions

The architecture supports future APIs such as:

/api/v1/price-alerts
/api/v1/notifications
/api/v1/recommendations
/api/v1/semantic-search
/api/v1/user/preferences

These should only be implemented when their corresponding features are introduced.

33. API Architecture Summary

The API is divided into these major areas:

API Groups

Authentication
`/api/v1/auth/`

Users
`/api/v1/users/`

Products
`/api/v1/products/`

Variants
`/api/v1/variants/`

Wishlist
`/api/v1/wishlist/`

Search History
`/api/v1/search-history/`

Marketplace connectors and Gemini remain internal backend services.

The API architecture keeps product discovery public, user-specific functionality protected, marketplace integrations isolated, and AI functionality focused on the specific product variant selected by the user.