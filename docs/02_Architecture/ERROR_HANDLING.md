# ShopSense AI – Error Handling Specification

## 1. Purpose

This document defines how ShopSense AI handles errors across the complete application.

The system contains several independent components:

- React frontend
- Spring Boot backend
- MySQL database
- Marketplace connectors
- Gemini AI service

Errors in one component should be handled at the appropriate layer without unnecessarily affecting unrelated functionality.

The primary reliability principle is:

> **Optional external services must not bring down the core ShopSense AI experience.**

---

# 2. Error Handling Principles

ShopSense AI follows these principles:

1. Use appropriate HTTP status codes.
2. Return consistent JSON error responses from the backend.
3. Never expose stack traces or sensitive implementation details to users.
4. Validate requests before processing them.
5. Validate database entities before performing operations.
6. Treat marketplace failures independently.
7. Treat Gemini failures independently.
8. Continue returning available marketplace results when one platform fails.
9. Never invent missing marketplace information.
10. Clearly communicate unavailable information to the frontend.
11. Log technical details on the backend while showing safe messages to users.
12. Keep authentication and authorization failures separate.
13. Handle external service timeouts explicitly.
14. Avoid exposing API keys, passwords, JWT secrets, or database credentials.
15. Design errors so future marketplace connectors can be added without changing the core error-handling architecture.

---

# 3. Error Handling Architecture

The general error flow is:

```text
User Request
     |
     v
React Frontend
     |
     v
Spring Boot Controller
     |
     v
Validation
     |
     v
Service Layer
     |
     +-------------------+
     |                   |
     v                   v
Database            External Services
                         |
                 +-------+-------+
                 |               |
                 v               v
            Marketplace       Gemini
             Connectors        API
                 |
                 v
          Error Handling
                 |
                 v
        Normalized Response
                 |
                 v
             Frontend
4. Standard API Error Response

All backend errors should use a consistent response structure.

Example:

{
  "timestamp": "2026-08-10T10:00:00Z",
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Product variant not found.",
  "path": "/api/v1/variants/1002/comparison"
}

The response may additionally contain a request or correlation ID in future versions.

5. Error Response Fields
Field	Description
timestamp	Time the error occurred
status	HTTP status code
error	Application-level error code
message	Safe user-readable message
path	API path that produced the error

The backend must never include:

Java stack traces
SQL queries
Database passwords
JWT secrets
Gemini API keys
Marketplace credentials
Internal server paths
Sensitive user information

in a client-facing error response.

6. HTTP Status Codes

ShopSense AI uses standard HTTP status codes.

Status	Meaning
200 OK	Request completed successfully
201 Created	Resource successfully created
204 No Content	Request succeeded without response content
400 Bad Request	Invalid request
401 Unauthorized	Authentication is missing or invalid
403 Forbidden	User is authenticated but not permitted
404 Not Found	Requested resource does not exist
409 Conflict	Duplicate or conflicting resource
422 Unprocessable Entity	Request is structurally valid but violates business rules
429 Too Many Requests	Rate limit exceeded
500 Internal Server Error	Unexpected backend error
502 Bad Gateway	External service produced an invalid/failed response
503 Service Unavailable	Service temporarily unavailable
7. Validation Errors

Invalid user input should be rejected before unnecessary processing.

Examples:

Invalid email
Empty product search
Invalid product ID
Invalid variant ID
Invalid wishlist request
Invalid pagination values

Example response:

{
  "timestamp": "2026-08-10T10:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Email must be a valid email address.",
  "path": "/api/v1/auth/register"
}

Spring Boot Bean Validation should be used wherever appropriate.

8. Authentication Errors

Authentication errors must return safe and consistent responses.

Missing JWT
401 Unauthorized

Example:

{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Authentication is required."
}
Invalid JWT
401 Unauthorized

The backend should not reveal why the token is invalid.

It should not expose:

Token parsing details
Signature details
JWT secret information
9. Authorization Errors

If a valid authenticated user attempts an operation they are not permitted to perform:

403 Forbidden

Example:

{
  "status": 403,
  "error": "FORBIDDEN",
  "message": "You are not permitted to perform this operation."
}
10. Resource Not Found

A resource that does not exist should return:

404 Not Found

Examples:

Product does not exist
Product variant does not exist
Wishlist item does not exist
Search history item does not exist

Example:

{
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Product variant not found.",
  "path": "/api/v1/variants/9999/comparison"
}
11. Duplicate Resource Errors

Duplicate operations should return:

409 Conflict

Example:

A user attempts to add the same product variant to their wishlist twice.

{
  "status": 409,
  "error": "RESOURCE_CONFLICT",
  "message": "Product is already in your wishlist.",
  "path": "/api/v1/wishlist"
}

The database unique constraint remains the final protection against duplicate entries.

12. Database Errors

Database failures should be handled by the backend.

Examples:

Connection failure
Query failure
Constraint violation
Transaction failure
Unexpected database timeout

The frontend should receive a safe message.

Example:

{
  "status": 500,
  "error": "DATABASE_ERROR",
  "message": "We could not complete the request right now. Please try again later."
}

Technical database details should be logged internally but not returned to the client.

13. Transaction Handling

Operations that modify multiple related records should use appropriate transaction boundaries.

For example:

Create Product
    |
    +-- Product
    +-- Specifications
    +-- Variants
    +-- Variant Attributes

If a critical operation fails, the transaction should be rolled back where appropriate.

This prevents partially created database records.

14. Marketplace Connector Errors

Marketplace connectors are external dependencies.

A connector can fail because of:

Timeout
Network failure
Rate limiting
Marketplace downtime
Invalid response
Product unavailable
Connector parsing error
Authentication failure
Unexpected marketplace changes

These errors must be isolated to the affected platform.

15. Marketplace Failure Principle

The most important marketplace rule is:

If one marketplace fails, continue processing the other marketplaces.

Example:

Amazon       SUCCESS
Flipkart     SUCCESS
Croma        FAILED
Reliance     SUCCESS

The comparison should still be returned.

The frontend should display:

Amazon       ₹114,999
Flipkart     ₹113,999
Croma        Temporarily unavailable
Reliance     ₹114,490
16. Marketplace Timeout

If a marketplace does not respond within the configured timeout:

Connector
    |
    v
Timeout
    |
    v
Mark platform as UNAVAILABLE
    |
    v
Continue other connectors

The timeout of one marketplace must not block the entire comparison indefinitely.

17. Marketplace Invalid Response

If a connector receives an unexpected marketplace response:

Marketplace
    |
    v
Unexpected Response
    |
    v
Connector validation fails
    |
    v
Mark platform as UNAVAILABLE
    |
    v
Continue comparison

The invalid marketplace response must not be passed directly to the frontend.

18. Marketplace No Offer

A marketplace can successfully respond but have no suitable offer.

This is not the same as a connector failure.

Example:

Amazon → NO_OFFER

The system should distinguish:

AVAILABLE
NO_OFFER
UNAVAILABLE

The frontend can display an appropriate message.

Example:

No suitable offer is currently available on this platform.

19. Marketplace Data Missing

Some platforms may not provide every field.

Example:

Amazon:
Price ✓
Seller ✓
Rating ✓
Delivery ✓

Platform X:
Price ✓
Seller ✓
Rating ✗
Delivery ✗

Missing information should be stored as:

NULL

rather than invented values.

The frontend may display:

Seller rating: Not available

or simply omit the field.

20. Marketplace Error Response

The comparison API should be capable of returning platform-level status information.

Example:

{
  "platformStatus": [
    {
      "platform": "Amazon",
      "status": "AVAILABLE"
    },
    {
      "platform": "Flipkart",
      "status": "AVAILABLE"
    },
    {
      "platform": "Croma",
      "status": "UNAVAILABLE",
      "message": "Marketplace data is temporarily unavailable."
    }
  ]
}

This allows the frontend to distinguish between successful and failed platforms.

21. Connector Logging

Marketplace connector errors should be logged internally.

Example internal log:

[WARN]
Platform: Croma
Operation: Product Comparison
Variant: 1002
Error: Request timeout

Logs may contain technical information necessary for debugging.

Client responses must contain only safe information.

22. Review Retrieval Errors

Review retrieval is an optional part of the marketplace workflow.

If reviews cannot be retrieved from one platform:

Amazon reviews       ✓
Flipkart reviews     ✓
Croma reviews        ✗

The system should still retain/process the available review sets.

The frontend may display:

Recent reviews are unavailable for Croma.

This should not prevent price comparison.

23. Review Refresh Failure

Reviews are refreshed approximately weekly.

If the refresh process fails for one platform:

Existing temporary review set
        |
        v
Refresh attempt
        |
        v
FAILED

The existing review set may remain available until a successful refresh occurs.

The system should not delete valid existing review data simply because a refresh attempt failed.

This prevents an external temporary failure from unnecessarily removing useful review information.

24. AI/Gemini Errors

Gemini is an optional analysis service.

Gemini may fail because of:

API timeout
Network failure
Rate limit
Invalid API response
Service outage
Authentication/configuration error
Unexpected response format

These failures must not affect the core product comparison.

25. AI Failure Principle

The main rule is:

AI failure must never make the core product comparison unavailable.

Example:

Product Information       ✓
Amazon Price              ✓
Flipkart Price            ✓
Croma Price               ✓
Recent Reviews             ✓
AI Summary                ✗

The user should still be able to compare prices and access marketplace links.

The AI section can display:

AI insights are temporarily unavailable. The marketplace comparison is still available.

26. AI Timeout

If Gemini does not respond within the configured timeout:

AI Service
    |
    v
Timeout
    |
    v
Return AI unavailable

The backend should not leave the request hanging indefinitely.

27. AI Invalid Response

If Gemini returns an unexpected or malformed response:

Gemini
   |
   v
Invalid Response
   |
   v
AI Response Validation
   |
   v
Reject Response
   |
   v
Return AI Unavailable

The invalid AI output must not be displayed as trusted information.

28. AI Hallucination Protection

The backend must provide Gemini with structured, trusted information.

The AI prompt must instruct Gemini:

Use only supplied data.
Do not invent prices.
Do not invent reviews.
Do not invent ratings.
Do not invent delivery information.
Do not invent availability.
Do not invent product specifications.
Do not recommend unrelated products.
Clearly acknowledge unavailable information.

The backend should also validate structured AI responses before displaying them.

29. AI Cache Failure

If the AI cache cannot be read:

Cache Read Failure
       |
       v
Attempt fresh AI generation

If the cache cannot be updated after a successful AI response, the current response can still be returned to the user.

The cache is an optimization and must not become a single point of failure.

30. Frontend Error Handling

The React frontend should translate backend errors into user-friendly UI states.

The frontend should not display raw backend exceptions.

Examples:

Product not found
Product not found.
Marketplace unavailable
Amazon data is temporarily unavailable.
AI unavailable
AI insights are temporarily unavailable.
Network failure
Unable to connect to ShopSense AI.
Please check your internet connection and try again.
31. Loading States

The frontend should clearly show loading states for operations that may take time.

Examples:

Searching products...
Loading product details...
Comparing prices...
Fetching marketplace offers...
Preparing AI insights...

Different sections may load independently.

For example, the price comparison can appear before the AI summary is ready.

32. Partial Rendering

The frontend should support partial results.

Example:

Product Details        ✓
Amazon                 ✓
Flipkart               ✓
Croma                  Loading...
AI Summary             Loading...

The user should not have to wait for every optional service before seeing available information.

33. Retry Strategy

Retries should be used carefully.

Appropriate cases may include:

Temporary marketplace timeout
Temporary network failure
Temporary AI service failure

Retries should use a limited number of attempts.

The system must not continuously retry a failed service.

Future implementations may use exponential backoff.

34. Rate Limiting

External services may impose request limits.

The backend should avoid excessive requests by using:

Current offer storage
Review caching
AI summary caching
Connector request limits
Controlled retries

Future versions may introduce explicit API rate limiting for public ShopSense endpoints.

35. Security Error Handling

Security-related errors must not reveal sensitive information.

For example, login failure should not reveal whether an email address exists.

Prefer:

Invalid email or password.

rather than:

Email exists but password is incorrect.

JWT errors should remain generic.

36. Logging Levels

The backend should use appropriate logging levels.

INFO

Normal important application events.

Example:

Product comparison request started.
DEBUG

Detailed development/debugging information.

WARN

Recoverable or unexpected conditions.

Example:

Croma connector timeout.
ERROR

Unexpected failures requiring investigation.

Example:

Database transaction failed.

Sensitive values must never be written to logs.

37. Sensitive Information

The following must never appear in logs or client responses:

Passwords
JWT secrets
JWT tokens where unnecessary
Gemini API keys
Database passwords
Marketplace credentials
Private user information
Internal authentication credentials
38. Global Exception Handling

Spring Boot should use a centralized exception-handling mechanism.

The existing Phase 1 GlobalExceptionHandler should be extended as the application grows.

The handler should convert known exceptions into consistent API responses.

Examples:

ResourceNotFoundException
BadRequestException
UserAlreadyExistsException
ValidationException
ExternalServiceException
AIServiceException

Unexpected exceptions should fall back to a safe 500 Internal Server Error.

39. Error Handling by Layer
Controller Layer

Handles:

Request validation
HTTP-level errors
DTO validation
Service Layer

Handles:

Business rule violations
Resource validation
Comparison orchestration
Error propagation
Repository Layer

Handles:

Database access
Persistence-related exceptions
Connector Layer

Handles:

Marketplace network errors
Timeouts
Invalid marketplace responses
Marketplace-specific failures
AI Service

Handles:

Gemini requests
Gemini timeouts
Invalid AI responses
AI cache errors
Frontend

Handles:

Loading states
Error messages
Partial results
Retry actions
User-friendly fallback UI
40. Error Handling Flow – Product Comparison
User requests comparison
          |
          v
Validate Variant
          |
     +----+----+
     |         |
   Invalid    Valid
     |         |
    404        v
          Connector Manager
               |
       +-------+-------+
       |       |       |
       v       v       v
    Amazon  Flipkart  Croma
       |       |       |
       v       v       v
     OK      ERROR      OK
       |       |       |
       +-------+-------+
               |
               v
       Partial Comparison
               |
               v
         Return Results
               |
               v
         AI Summary Request
               |
          +----+----+
          |         |
        Success    Failure
          |         |
          v         v
      AI Summary   Comparison
          |        remains
          +----+----+
               |
               v
              User
41. Error Handling Flow – Wishlist
User
 |
 v
POST /wishlist
 |
 v
Validate JWT
 |
 +---- Invalid → 401
 |
 v
Validate Product Variant
 |
 +---- Missing → 404
 |
 v
Check Existing Wishlist
 |
 +---- Exists → 409
 |
 v
Save Wishlist
 |
 +---- Database Failure → 500
 |
 v
201 Created
42. Error Handling Flow – Search History
User
 |
 v
POST /search-history
 |
 v
Validate JWT
 |
 +---- Invalid → 401
 |
 v
Validate Query
 |
 +---- Invalid → 400
 |
 v
Save History
 |
 +---- Database Failure → 500
 |
 v
Success
43. Graceful Degradation

ShopSense AI should continue providing useful functionality when optional components fail.

Example 1 – Marketplace Failure
Amazon       ✓
Flipkart     ✓
Croma        ✗

Result:

Show Amazon
Show Flipkart
Show Croma as unavailable
Example 2 – AI Failure
Price comparison ✓
Reviews          ✓
AI summary       ✗

Result:

Show comparison
Show reviews
Show AI unavailable message
Example 3 – Review Failure
Price comparison ✓
Amazon reviews   ✓
Croma reviews    ✗

Result:

Show price comparison
Show available reviews
Mark Croma reviews unavailable
44. Error Monitoring

The architecture should support future monitoring of:

Marketplace connector failure rates
Marketplace response times
Gemini failure rates
API response times
Database failures
Authentication failures
Unexpected backend exceptions

Monitoring infrastructure can be introduced later without changing the core application architecture.

45. Future Error Handling Extensions

Future versions may introduce:

Circuit breakers for marketplace connectors
Distributed tracing
Request correlation IDs
Centralized log aggregation
Advanced retry policies
Service health monitoring
External API rate-limit management
Automated connector health checks

These are future improvements and are not required for the initial implementation.

46. Core Error Handling Principle

The most important reliability rule for ShopSense AI is:

A failure in one optional component must not unnecessarily prevent the user from receiving useful information from the rest of the system.

Therefore:

Marketplace failure
        ↓
Other marketplaces continue

Review failure
        ↓
Price comparison continues

AI failure
        ↓
Core comparison continues

Cache failure
        ↓
Fresh processing can continue

One frontend component failure
        ↓
Other independent sections remain usable

The application should fail gracefully rather than fail completely.