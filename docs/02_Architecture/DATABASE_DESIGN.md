# ShopSense AI – Database Design

## 1. Document Purpose

This document defines the target database architecture for ShopSense AI.

The database is designed to support:

- Product discovery
- Smart product search
- Product variants
- Marketplace comparison
- Current marketplace offers
- Recent customer reviews
- AI-generated comparison summaries
- User accounts
- Wishlists
- Search history
- Regional information
- Future price history and price prediction features

The database follows a relational design and is intended to work with the existing Spring Boot + Spring Data JPA backend and MySQL database.

---

# 2. Database Technology

## Database

**MySQL**

## Database Name

```text
shopsense_db
ORM

Spring Data JPA / Hibernate

Primary Key Strategy

Tables will use numeric BIGINT primary keys with auto-increment behavior unless otherwise specified.

3. Database Design Principles

The following principles must be maintained throughout development:

Product identity must be independent of marketplace identity.
The same physical product across different marketplaces must use one ShopSense product ID.
Product variants are optional.
Every marketplace offer must reference exactly one product variant.
Products without meaningful variants use an internal Standard variant.
Marketplace-specific information must not be stored directly in the product table.
Current marketplace offers are stored; permanent price history is not required initially.
Recent reviews are stored temporarily and refreshed approximately weekly.
AI summaries are derived data and should be temporarily cached.
Missing marketplace information must be represented using NULL rather than invented values.
User wishlist entries reference product variants rather than marketplace offers.
Search history stores only user search queries.
The schema should support future features without requiring major redesign.
4. Entity Relationship Overview
                         categories
                              |
                              | 1
                              |
                              | *
                              v
                           products
                         /     |      \
                        /      |       \
                       /       |        \
                      v        v         v
          product_specifications   product_variants
                                      |
                         +------------+-------------+
                         |            |             |
                         |            |             |
                         v            v             v
                variant_attributes  platform_offers  ai_summaries
                                      |
                                      |
                                      v
                                  platforms
                                      |
                                      |
                                      v
                                   reviews


                           users
                          /     \
                         /       \
                        v         v
                   wishlist   search_history
5. Categories
Table: categories

Categories are stored separately from products to support hierarchical product classification.

Example:

Electronics
├── Smartphones
├── Laptops
├── Tablets
├── TVs
├── Monitors
└── Headphones

A category may have a parent category.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
name	VARCHAR(100)	No	Category name
parent_id	BIGINT	Yes	Parent category ID
created_at	TIMESTAMP	No	Creation timestamp
updated_at	TIMESTAMP	No	Last update timestamp
Relationships
Category 1 ──────── * Product

A category can contain many products.

A top-level category has:

parent_id = NULL
6. Products
Table: products

The products table represents the actual ShopSense product independently of any marketplace.

For example:

Apple iPhone 16 Pro

is one ShopSense product even if it is sold on:

Amazon
Flipkart
Croma
Reliance Digital
Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key / ShopSense Product ID
brand	VARCHAR(100)	No	Product brand
series	VARCHAR(150)	Yes	Product series
model	VARCHAR(150)	No	Product model
category_id	BIGINT	No	Reference to category
description	TEXT	Yes	General product description
image_url	VARCHAR(500)	Yes	Main product image
has_variants	BOOLEAN	No	Indicates whether user-selectable variants exist
created_at	TIMESTAMP	No	Creation timestamp
updated_at	TIMESTAMP	No	Last update timestamp
Important Rule

The product table must NOT contain marketplace-specific information.

The following must not be stored directly in products:

Amazon price
Flipkart price
Croma price
Seller
Delivery
Marketplace availability
Marketplace reviews

Those belong to marketplace-related tables.

Relationship
Category 1 ──────── * Product
7. Product Specifications
Table: product_specifications

Product specifications use a flexible attribute-value model.

This avoids creating separate specification tables for every product category.

For example, a smartphone can have:

Processor     → A18 Pro
Display       → 6.3 inch
Camera        → 48MP
Battery       → 3582 mAh
Operating OS  → iOS

A laptop can use the same table:

Processor     → Intel Core Ultra 7
RAM           → 16GB
Storage       → 512GB SSD
Display       → 14 inch
Graphics      → Intel Arc
Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
product_id	BIGINT	No	Product reference
attribute_name	VARCHAR(100)	No	Specification name
attribute_value	VARCHAR(500)	No	Specification value
display_order	INT	No	Display ordering
created_at	TIMESTAMP	No	Creation timestamp
updated_at	TIMESTAMP	No	Last update timestamp
Relationship
Product 1 ──────── * ProductSpecification
Important Rule

Base product specifications belong here.

Variant-specific attributes such as:

Storage
RAM
Color
Screen size

should be represented through the variant system when they distinguish purchasable configurations.

8. Product Variants
Table: product_variants

A product may have multiple purchasable configurations.

Examples:

iPhone 16 Pro
├── 128GB / Natural Titanium
├── 256GB / Natural Titanium
└── 512GB / Natural Titanium

A laptop may have:

MacBook Air
├── 16GB RAM / 256GB SSD
├── 16GB RAM / 512GB SSD
└── 24GB RAM / 512GB SSD

Products without meaningful variants use an internal Standard variant.

Users do not need to see the term Standard Variant.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
product_id	BIGINT	No	Product reference
variant_name	VARCHAR(255)	No	Human-readable variant name
is_default	BOOLEAN	No	Indicates the default/Standard variant
created_at	TIMESTAMP	No	Creation timestamp
updated_at	TIMESTAMP	No	Last update timestamp
Relationships
Product 1 ──────── * ProductVariant
Variant Rules
Products with meaningful variants

Example:

iPhone 16 Pro
├── 128GB / Black
├── 256GB / Black
└── 512GB / Black
Products without meaningful variants

Example:

LG Washing Machine
└── Standard

The Standard variant exists internally so every marketplace offer can consistently reference a product variant.

9. Variant Attributes
Table: variant_attributes

Variant attributes use a flexible attribute-value structure.

This prevents fixed columns such as ram, storage, color, and screen_size from being required for every product category.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
variant_id	BIGINT	No	Variant reference
attribute_name	VARCHAR(100)	No	Attribute name
attribute_value	VARCHAR(255)	No	Attribute value
created_at	TIMESTAMP	No	Creation timestamp
updated_at	TIMESTAMP	No	Last update timestamp
Relationship
ProductVariant 1 ──────── * VariantAttribute
Example
Variant ID: 10

Storage → 256GB
Color   → Natural Titanium

Another category can use:

Variant ID: 50

RAM     → 16GB
Storage → 512GB SSD
Color   → Silver
10. Variant Attribute Constraints

A single variant should not contain duplicate attributes with conflicting values.

For example, this should not be allowed:

Variant 10
Storage → 256GB
Storage → 512GB

Therefore, the database should enforce:

UNIQUE(variant_id, attribute_name)

This allows:

Storage → 256GB
Color   → Black

but prevents two Storage attributes from being assigned to the same variant.

11. Platforms
Table: platforms

The platforms table represents supported e-commerce marketplaces.

Examples:

Amazon
Flipkart
Croma
Reliance Digital
Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
name	VARCHAR(100)	No	Marketplace name
website_url	VARCHAR(500)	Yes	Official marketplace website
logo_url	VARCHAR(500)	Yes	Platform logo
is_active	BOOLEAN	No	Connector availability status
created_at	TIMESTAMP	No	Creation timestamp
updated_at	TIMESTAMP	No	Last update timestamp
Important Rule

Platform information describes the marketplace itself.

It must not contain:

Product prices
Product reviews
Seller information
Delivery information

Those belong to marketplace offer/review data.

12. Platform Offers
Table: platform_offers

This is one of the most important tables in ShopSense AI.

A platform offer represents the current best suitable offer for a specific product variant on a specific marketplace.

Example:

iPhone 16 Pro - 256GB

Amazon
₹114,999

Flipkart
₹113,999

Croma
₹114,490

Only one current representative offer is maintained per platform for a product variant.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
product_variant_id	BIGINT	No	Exact product variant
platform_id	BIGINT	No	Marketplace
original_price	DECIMAL(12,2)	Yes	Original/MRP price
current_price	DECIMAL(12,2)	No	Current selling price
currency	VARCHAR(10)	No	Currency code such as INR
seller_name	VARCHAR(255)	Yes	Selected seller
seller_rating	DECIMAL(3,2)	Yes	Seller rating if available
availability_status	VARCHAR(30)	No	Controlled availability status
availability_details	VARCHAR(500)	Yes	Marketplace-specific availability wording
delivery_info	VARCHAR(500)	Yes	Delivery information
offer_details	TEXT	Yes	Current promotion or offer details
product_url	VARCHAR(1000)	No	Marketplace product URL
last_updated_at	TIMESTAMP	No	Last data refresh time
Unique Constraint

The table must enforce:

UNIQUE(product_variant_id, platform_id)

This guarantees only one current offer exists for a given product variant and platform.

Example
Product Variant: iPhone 16 Pro 256GB

Platform       Current Price
----------------------------
Amazon         ₹114,999
Flipkart       ₹113,999
Croma          ₹114,490
13. Best Offer Selection

ShopSense AI should show one representative offer from each platform.

The best offer should not be selected using price alone.

Selection may consider:

Current price
Seller rating/reliability
Availability
Delivery information
Current offers/discounts
Other available trust indicators

This prevents a very low-priced but poorly rated seller from automatically being selected.

The exact ranking algorithm can be refined during marketplace integration.

14. Price Management

ShopSense AI Version 1 stores only current marketplace prices.

When a marketplace price changes:

Old current price
        ↓
Updated with new current price

The old value is not retained permanently.

For example:

Monday:
₹114,999

Tuesday:
₹113,999

The database row is updated rather than creating a new historical price row.

15. Future Price History

Permanent price history is intentionally excluded from the initial implementation.

A future price_history table may be introduced to support:

Price history graphs
Price trend analysis
Price-drop alerts
Price predictions

The future table could reference the existing platform_offers structure without changing the core product architecture.

16. Availability

Availability uses a controlled status plus optional marketplace-specific details.

availability_status

Possible initial values:

IN_STOCK
OUT_OF_STOCK
UNAVAILABLE
UNKNOWN
availability_details

Stores the marketplace's own wording when available.

Example:

availability_status:
IN_STOCK

availability_details:
"Only 2 left in stock"

Another example:

availability_status:
OUT_OF_STOCK

availability_details:
"Currently unavailable"

This provides both structured comparison data and platform-specific information.

17. Reviews
Table: reviews

Reviews are stored temporarily rather than permanently.

The system will maintain approximately 10–15 recent and relevant reviews per product variant and platform.

Reviews belong to the combination:

ProductVariant + Platform

They do not directly reference PlatformOffer because the selected seller/offer can change while the product/platform relationship remains stable.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
product_variant_id	BIGINT	No	Product variant
platform_id	BIGINT	No	Marketplace
review_text	TEXT	No	Review content
rating	DECIMAL(3,2)	Yes	Customer rating
review_date	DATE	Yes	Original review date
source_url	VARCHAR(1000)	Yes	Source URL if available
fetched_at	TIMESTAMP	No	Time review was retrieved
Relationships
ProductVariant 1 ──────── * Review
Platform      1 ──────── * Review
18. Review Refresh Strategy

Reviews are not intended to be permanent historical data.

Approximately once per week:

Fetch latest relevant reviews
        ↓
Select approximately 10–15 reviews
        ↓
Replace previous temporary review set

The system should prioritize the most recent relevant reviews available.

A strict requirement that reviews must have been posted within exactly the previous seven days is not necessary.

This ensures sufficient review coverage even for products that receive few new reviews.

19. AI Summary Cache
Table: ai_summaries

AI summaries are derived from current product, marketplace, and review information.

They should be temporarily cached rather than permanently archived.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
product_variant_id	BIGINT	No	Product variant
summary	TEXT	No	Generated AI comparison summary
generated_at	TIMESTAMP	No	Summary generation time
expires_at	TIMESTAMP	No	Cache expiration time
Relationship
ProductVariant 1 ──────── * AI Summary

A unique/current-summary constraint may be used if only one active cached summary is maintained per variant.

20. AI Summary Lifecycle

When a user opens a product variant:

Check AI Summary Cache
        |
        v
Is cache valid?
     /       \
   YES        NO
    |          |
    v          v
Return      Fetch current
cached      marketplace data
summary          |
                 v
           Use recent reviews
                 |
                 v
              Gemini
                 |
                 v
          Store/update cache
                 |
                 v
            Return summary

The AI summary is not a source of truth.

The source data remains:

Product information
Product variant information
Current platform offers
Recent reviews
21. Users
Table: users

The Phase 1 implementation already provides the basic users table.

The target design extends the existing structure rather than creating a new user system.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
name	VARCHAR(100)	No	User name
email	VARCHAR(150)	No	Unique email
password	VARCHAR(255)	No	BCrypt-encrypted password
role	VARCHAR(20)	No	User role
country	VARCHAR(100)	Yes	User country
region	VARCHAR(100)	Yes	User region/state
created_at	TIMESTAMP	No	Account creation time
updated_at	TIMESTAMP	No	Last update time
Existing Phase 1 Security

Passwords must remain encrypted using BCrypt.

JWT authentication remains the authentication mechanism established in Phase 1.

22. User Regions

Country and region information is stored as basic user information.

The initial system does not require a separate region database.

The information is stored to support future capabilities such as:

Region-specific platform availability
Delivery availability
Regional offers
Currency support
Personalized marketplace results

These capabilities may be implemented in future phases.

23. Wishlist
Table: wishlist

The wishlist stores products that users intentionally save.

A wishlist entry references a ProductVariant, not a marketplace offer.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
user_id	BIGINT	No	User reference
product_variant_id	BIGINT	No	Saved product variant
created_at	TIMESTAMP	No	Time added
Relationship
User 1 ──────── * Wishlist
ProductVariant 1 ──────── * Wishlist
Unique Constraint

The same user should not be able to add the exact same product variant more than once.

UNIQUE(user_id, product_variant_id)
24. Search History
Table: search_history

Only user search queries are stored.

The system does not initially track every product click or page interaction.

Columns
Column	Type	Nullable	Description
id	BIGINT	No	Primary key
user_id	BIGINT	No	User reference
search_query	VARCHAR(500)	No	Search text
searched_at	TIMESTAMP	No	Search timestamp
Relationship
User 1 ──────── * SearchHistory

Example:

User
  |
  ├── "iPhone 16"
  ├── "Gaming Laptop"
  └── "Samsung TV"
25. Authentication and Public Access

Authentication is not required for:

Product search
Product discovery
Product details
Product comparison
AI comparison insights
Marketplace redirection

Authentication is required for:

Wishlist
Search history
Future price alerts
Future personalized features
26. Foreign Key Relationships

The major foreign key relationships are:

categories.parent_id
        → categories.id

products.category_id
        → categories.id

product_specifications.product_id
        → products.id

product_variants.product_id
        → products.id

variant_attributes.variant_id
        → product_variants.id

platform_offers.product_variant_id
        → product_variants.id

platform_offers.platform_id
        → platforms.id

reviews.product_variant_id
        → product_variants.id

reviews.platform_id
        → platforms.id

ai_summaries.product_variant_id
        → product_variants.id

wishlist.user_id
        → users.id

wishlist.product_variant_id
        → product_variants.id

search_history.user_id
        → users.id
27. Important Unique Constraints

The database should enforce the following important constraints:

users.email
    UNIQUE

variant_attributes
    UNIQUE(variant_id, attribute_name)

platform_offers
    UNIQUE(product_variant_id, platform_id)

wishlist
    UNIQUE(user_id, product_variant_id)

The category name may also be constrained according to the desired category hierarchy and application rules.

28. Data Ownership

Different data types have different ownership.

ShopSense-owned data
Product identity
Product catalog
Categories
Product specifications
Product variants
Variant attributes
User accounts
Wishlist
Search history
Marketplace-derived data
Current prices
Seller information
Availability
Delivery information
Marketplace offers
Marketplace product URLs
Marketplace ratings
Recent reviews
AI-derived data
AI comparison summaries

AI-derived information must not replace the original marketplace data.

29. Data Freshness
Static Product Data

Updated when product catalog information changes.

Examples:

Product name
Specifications
Product images
Current Marketplace Data

Updated through marketplace connectors.

Examples:

Current price
Availability
Seller
Delivery
Offers
Reviews

Refreshed approximately weekly.

AI Summary

Refreshed when the cached summary expires or when the application determines that the underlying comparison data has changed significantly.

30. Data Retention

Initial retention strategy:

Data	Retention
Product catalog	Long-term
Product specifications	Long-term
Product variants	Long-term
Categories	Long-term
Platform definitions	Long-term
Current platform offers	Current data only
Reviews	Temporary, approximately weekly refresh
AI summaries	Temporary cache
Users	Long-term
Wishlist	Long-term
Search history	Long-term initially
Price history	Not implemented initially

Future retention policies can be introduced as the application evolves.

31. Product and Marketplace Identity

The database must never create separate ShopSense products simply because different marketplaces use different names.

For example:

Amazon:
Apple iPhone 16 Pro 256GB Natural Titanium

Flipkart:
Apple iPhone 16 Pro 256 GB Natural Titanium

Croma:
iPhone 16 Pro - 256GB - Natural Titanium

These should map to the same:

ShopSense Product
        +
Product Variant

The connector normalization layer is responsible for mapping marketplace listings to the correct ShopSense product and variant.

32. Standard Variant Rule

Every product must have at least one product variant.

For products without meaningful selectable variants:

Product
└── Standard Variant

The Standard Variant is an internal implementation detail.

This guarantees that every platform_offer can reference exactly one product_variant_id.

33. Product Search and Database Usage

The search system will primarily search the ShopSense product catalog.

Search results may use:

Product name
Brand
Series
Model
Category
Product popularity
Customer rating
Product recency

Search results should not require live marketplace calls for every product.

This keeps product discovery fast and reduces unnecessary external requests.

34. Marketplace Comparison Usage

Live/current marketplace data is retrieved only when the user has selected a specific product and applicable variant.

The comparison process is:

Product Variant
       |
       v
Connector Manager
       |
       +---- Marketplace Connector 1
       +---- Marketplace Connector 2
       +---- Marketplace Connector 3
       |
       v
Normalized Offers
       |
       v
Current Platform Offers
       |
       v
Comparison

This prevents unnecessary marketplace requests during general product search.

35. AI Data Usage

Gemini AI receives structured information for the selected product variant.

The AI may receive:

Product specifications
Variant attributes
Current platform offers
Seller information
Delivery information
Offers
Ratings
Recent reviews

The AI should not be responsible for basic product matching or price calculations.

AI is an analysis layer rather than the database's source of truth.

36. Future Extensions

The database is intentionally designed to support future additions.

Potential future tables include:

price_history
price_alerts
notifications
user_preferences
recommendations
semantic_search_index

These are not part of the initial implementation.

They should only be introduced when the corresponding features are actually developed.

37. Future Price History Design

If price history is introduced later, the existing platform_offers table should remain responsible for current data.

A separate table can record historical values:

price_history
-------------------------
id
platform_offer_id
price
recorded_at

This allows:

Current Offer
     |
     +── Current Price

Historical Data
     |
     +── Monday
     +── Tuesday
     +── Wednesday
     +── ...

This design supports future:

Price graphs
Price trends
Price-drop detection
Price predictions

without storing unnecessary historical information in Version 1.

38. Database Design Summary

The core ShopSense AI database consists of:

categories
products
product_specifications
product_variants
variant_attributes
platforms
platform_offers
reviews
ai_summaries
users
wishlist
search_history

The central product relationship is:

Category
   |
   v
Product
   |
   v
ProductVariant
   |
   +------------------+
   |                  |
   v                  v
PlatformOffer      AI Summary
   |
   v
Platform

ProductVariant
   |
   v
Reviews
   |
   v
Platform

The user relationship is:

User
 |
 +---- Wishlist
 |
 +---- Search History

The design separates:

Static product information
Product-specific variants
Marketplace-specific current offers
Temporary recent reviews
Temporary AI-derived summaries
User-specific information

This separation provides a clean foundation for the ShopSense AI application while allowing future functionality such as price history, price alerts, semantic search, personalized recommendations, and additional marketplace integrations to be added without fundamentally redesigning the database.


### Before you save: one correction to be aware of

I intentionally made **`ai_summaries` one-to-many in the relationship description**, but our actual intended behavior is **one current cached summary per product variant**. We should enforce that in the implementation with a unique constraint on `product_variant_id` if we're keeping only one active cached summary.

So the final intended rule is:

```text
One ProductVariant → One current AI summary

not multiple active summaries.

Likewise, every product has at least one variant, with Standard used when no meaningful variants exist.