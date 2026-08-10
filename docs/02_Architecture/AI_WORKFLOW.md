# ShopSense AI – AI Workflow Specification

## 1. Purpose

This document defines how Artificial Intelligence is used within ShopSense AI.

The AI exists to help users understand and compare marketplace information for a **specific product variant selected by the user**.

The AI is not the source of product, price, availability, or review data.

The backend and marketplace connectors provide the factual data.

The AI analyzes that data and presents an understandable comparison.

---

# 2. AI Role

The ShopSense AI assistant acts as a knowledgeable product comparison salesperson.

Its role is to:

- Explain differences between marketplace offers.
- Identify the current best price.
- Compare delivery information.
- Compare seller ratings where available.
- Explain current offers and discounts.
- Summarize recent customer feedback.
- Highlight advantages and disadvantages based on available data.
- Help the user make an informed purchasing decision.

The AI must not pressure the user into purchasing a product.

The final purchasing decision always belongs to the user.

---

# 3. AI Scope

The AI operates at the **specific product variant level**.

Example:

```text
User searches:
iPhone 16

Search results:
- iPhone 17
- iPhone 17 Pro
- iPhone 16
- iPhone 16 Pro
- iPhone 15

The user selects:

iPhone 16 Pro

and then selects:

256GB / Natural Titanium

The AI is now responsible only for analyzing:

iPhone 16 Pro
256GB / Natural Titanium

across the available marketplaces.

4. AI Must Not Perform Cross-Product Recommendations

The current AI should not automatically recommend another product.

For example, if the user selected:

iPhone 16 Pro 256GB

the AI should not respond:

"You should buy the iPhone 17 instead."

It may mention information about the selected product, but it must remain focused on the user's chosen product variant.

Future cross-product recommendation functionality may be introduced separately.

5. AI Input Sources

The AI receives structured information from trusted backend sources.

The main inputs are:

Product Information
Brand
Series
Model
Description
Category
Product specifications
Variant Information
Variant name
Variant attributes
Storage
RAM
Color
Screen size
Other applicable variant attributes
Marketplace Offers
Platform name
Current price
Original price
Seller
Seller rating
Availability
Delivery information
Current offers
Product URL where necessary
Recent Reviews
Review text
Customer rating
Review date
Platform

The AI should only use information supplied by the backend.

6. AI Data Trust Hierarchy

The AI should treat information according to the following priority:

1. Structured backend product data
2. Current normalized marketplace offer data
3. Recent marketplace review data
4. Derived calculations performed by the backend
5. AI interpretation

AI interpretation must never override factual backend data.

For example, if the backend says:

Flipkart:
₹113,999

Amazon:
₹114,999

the AI must not claim that Amazon is cheaper.

7. Price Information

Price comparison should primarily be calculated by the backend.

The backend knows:

original_price
current_price

The AI receives these values and explains them.

Example:

Amazon:
₹114,999

Flipkart:
₹113,999

The AI may say:

"Flipkart currently has the lower listed price by ₹1,000."

The AI should not independently invent or estimate prices.

8. Discount Calculation

The backend should calculate numerical discount information whenever possible.

For example:

Original Price: ₹119,999
Current Price: ₹113,999

The backend can calculate:

Discount:
₹6,000

The AI can then explain the discount.

The AI should not trust promotional wording blindly if structured price information is available.

9. Marketplace Comparison

The AI may compare the selected variant across available platforms.

Example:

Amazon
₹114,999
Delivery tomorrow

Flipkart
₹113,999
Delivery in 2 days

Croma
₹114,490
Delivery today

The AI can produce an explanation such as:

"Flipkart currently has the lowest listed price, while Croma provides the fastest stated delivery among the available offers."

The AI should clearly distinguish between factual data and interpretation.

10. Best Offer

The backend performs the primary best-offer selection.

The selection may consider:

Current price
Seller rating
Availability
Delivery
Offers
Other available trust indicators

The AI may explain why an offer appears preferable.

It must not automatically treat the lowest price as the best choice.

Example:

Offer A
₹110,000
Seller rating: 2.8

Offer B
₹111,000
Seller rating: 4.8

The AI should be able to explain that Offer B may provide a better balance of price and seller reliability.

11. Seller Ratings

Seller ratings are marketplace-derived information.

The AI may use seller ratings when they are available.

If seller rating information is unavailable:

seller_rating = null

the AI must not invent a rating.

It should simply avoid making a seller-rating comparison for that platform.

12. Availability

Availability information comes from the marketplace connector.

Example:

availability_status:
IN_STOCK

availability_details:
"Only 2 left in stock"

The AI may explain this information.

It must not convert:

UNKNOWN

into:

IN_STOCK

or make assumptions about stock availability.

13. Delivery Information

The AI may compare delivery information when provided.

Example:

Amazon:
Delivery tomorrow

Flipkart:
Delivery in 2 days

The AI can state:

"Amazon currently shows the earlier delivery estimate."

If delivery information is unavailable for a platform, the AI must not invent an estimate.

14. Marketplace Offers and Discounts

The AI may explain available marketplace offers.

Examples:

Bank discount
Coupon
Promotional offer
Exchange offer
Platform-specific discount

The AI must distinguish between:

Listed product price

and:

Conditional offer

For example:

"The listed price is ₹114,999. An additional bank offer may reduce the effective price if the stated conditions are met."

The AI must not assume that every user qualifies for a conditional offer.

15. Review Analysis

The system stores approximately 10–15 recent relevant reviews per platform.

These reviews are refreshed approximately weekly.

The AI uses these reviews to produce a concise and balanced summary.

Example input:

Amazon:
10 recent reviews

Flipkart:
12 recent reviews

Croma:
10 recent reviews

The AI can identify:

Frequently praised features
Common complaints
Repeated issues
General sentiment
Important differences between platforms
16. Review Neutrality

The AI must provide an unbiased summary.

It should not:

Select only positive reviews.
Select only negative reviews.
Promote a specific marketplace.
Hide meaningful recurring complaints.
Treat one review as universal evidence.

Example:

If several recent reviews praise:

Camera quality

but several also mention:

Battery life

the summary should acknowledge both.

17. Review Source Attribution

Where possible, the AI summary should identify the marketplace from which the review information originated.

Example:

"Recent Amazon reviews generally praise the camera, while some reviewers mention battery life as an area for improvement."

The AI should not merge marketplace-specific information in a way that makes it impossible to understand its source.

18. Review Limitations

The AI must recognize that a sample of 10–15 recent reviews does not represent every customer.

Therefore, it should use wording such as:

"Recent reviews suggest..."
"Among the sampled reviews..."
"Several recent customers mentioned..."

It should avoid absolute claims such as:

"Everyone agrees that..."

19. AI Summary Content

The AI summary should generally contain:

Best Price

Which platform currently has the lowest valid listed price.

Delivery

Which platform has the better stated delivery estimate, if available.

Seller/Offer Quality

Which offer appears more trustworthy based on available seller and offer information.

Review Insights

A balanced summary of recent customer feedback.

Overall Comparison

A concise explanation of the main differences between the available platforms.

20. AI Should Not Make Unsupported Claims

The AI must not invent:

Prices
Discounts
Reviews
Ratings
Sellers
Delivery dates
Availability
Product specifications
Marketplace policies

If information is unavailable, the AI should acknowledge that it is unavailable.

Example:

"Delivery information was not available from this platform."

21. AI Failure Handling

If Gemini is unavailable, times out, or returns an invalid response:

The core product comparison must continue working.

The frontend should still display:

Product information
Product specifications
Current marketplace offers
Prices
Availability
Delivery information
Reviews where available

The AI section can display:

"AI insights are temporarily unavailable."

The application must not fail completely because Gemini is unavailable.

22. AI Cache

AI summaries are temporarily cached.

The cache contains:

product_variant_id
summary
generated_at
expires_at

When the user opens a product variant:

Check cached summary
        |
        v
Is it still valid?
      /   \
    YES    NO
     |      |
     v      v
  Return   Fetch current
  cache    comparison data
              |
              v
            Gemini
              |
              v
        Update cache

The exact cache duration can be configured during implementation.

23. AI Cache Freshness

The AI summary should not be considered permanent.

A summary may become outdated when:

Prices change significantly.
Marketplace offers change.
Availability changes.
Review data is refreshed.
The cache expiration time is reached.

The system should regenerate the summary when the cached information is no longer considered fresh.

24. AI Prompt Structure

The backend should send Gemini a structured prompt containing:

Product:
- Brand
- Model
- Specifications

Selected Variant:
- Variant name
- Variant attributes

Marketplace Offers:
- Platform
- Current price
- Original price
- Seller
- Seller rating
- Availability
- Delivery
- Offers

Recent Reviews:
- Platform
- Rating
- Review text
- Review date

The prompt should explicitly instruct the model to:

Use only supplied information.
Avoid inventing missing information.
Focus only on the selected product variant.
Compare available marketplaces.
Provide balanced review analysis.
Clearly identify unavailable information.
Avoid pushing the user toward a purchase.
Avoid recommending unrelated products.
25. AI Output Structure

The AI should return structured information where possible.

Conceptually:

{
  "bestPrice": {
    "platform": "Flipkart",
    "price": 113999,
    "reason": "Lowest current listed price"
  },
  "bestDelivery": {
    "platform": "Amazon",
    "details": "Delivery tomorrow"
  },
  "offerAssessment": {
    "platform": "Flipkart",
    "reason": "Lower price with a highly rated seller"
  },
  "reviewSummary": "Recent reviews are generally positive...",
  "overallSummary": "..."
}

The exact DTO structure may be refined during implementation.

26. Backend Calculation vs AI Interpretation

The system should clearly separate deterministic calculations from AI interpretation.

Backend should handle:
Price comparison
Discount calculations
Availability status
Data normalization
Marketplace status
Variant identification
Review selection
Cache expiration
AI should handle:
Natural-language explanation
Review summarization
Balanced interpretation
Explanation of trade-offs
Human-friendly comparison

This separation improves accuracy and reliability.

27. AI and Smart Search

The initial version does not use AI for product search.

Initial search uses the ShopSense product catalog and deterministic relevance logic.

Future versions may introduce:

Semantic search
Natural-language product queries
Intent recognition
AI-powered search refinement

These capabilities should be added separately from the current product comparison AI.

28. AI and Cross-Product Recommendations

Cross-product recommendations are outside the initial AI scope.

For example:

User selects:
iPhone 16 Pro

The AI focuses on:

iPhone 16 Pro

It does not automatically compare:

iPhone 17
Samsung Galaxy
Google Pixel

A future recommendation engine may provide such functionality.

29. AI Security

Gemini API credentials must never be exposed to the frontend.

The API key must be stored using backend environment configuration or a secure secret-management mechanism.

The frontend only communicates with:

/api/v1/variants/{variantId}/ai-summary

The backend communicates with Gemini.

30. AI Data Privacy

The AI request should contain only information required to generate the comparison.

User passwords, JWT tokens, private account information, or unrelated user data must never be included in Gemini prompts.

31. AI Workflow Summary

The complete AI workflow is:

User
 |
 | Selects product
 v
Product Variant
 |
 v
Current Marketplace Data
 |
 +---- Amazon
 +---- Flipkart
 +---- Croma
 |
 v
Normalized Comparison Data
 |
 v
Recent Reviews
 |
 v
Check AI Cache
 |
 +---- Valid ----> Return Cached Summary
 |
 +---- Expired
          |
          v
       Gemini
          |
          v
   Structured AI Result
          |
          v
      Cache Result
          |
          v
      Frontend
32. Core AI Principle

The central principle of ShopSense AI is:

The backend provides the facts. The AI explains the facts.

The AI must never become the source of truth for:

Prices
Product specifications
Availability
Seller information
Marketplace information
Customer reviews

This principle must be maintained throughout the implementation.

33. Future AI Extensions

The architecture can later support:

AI-powered semantic search
Cross-product recommendations
Personalized product recommendations
Price prediction
Natural-language product queries
Conversational product comparison
AI-powered price-drop explanations

These are future extensions and are not part of the initial implementation.