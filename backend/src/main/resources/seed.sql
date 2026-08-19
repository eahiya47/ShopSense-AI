-- ==============================================================================
-- ShopSense AI - Development / Demo Data Seed Script
-- Target Database: shopsense_db (MySQL)
-- Dependency Order:
--   1. categories
--   2. products
--   3. product_variants
--   4. product_specifications
--   5. variant_attributes
--   6. platforms
--   7. platform_offers
--   8. reviews
--   9. users (optional demo accounts)
-- ==============================================================================

USE shopsense_db;

-- Clear catalog and demo cache data only (Preserve user-owned tables: users, wishlist, search_history)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reviews;
TRUNCATE TABLE platform_offers;
TRUNCATE TABLE variant_attributes;
TRUNCATE TABLE product_specifications;
TRUNCATE TABLE product_variants;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE platforms;
TRUNCATE TABLE ai_summaries;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------------------------
-- 1. CATEGORIES (Parent categories first, then child sub-categories)
-- ------------------------------------------------------------------------------
INSERT INTO categories (id, name, parent_id, created_at, updated_at) VALUES
(1, 'Electronics', NULL, NOW(), NOW()),
(2, 'Smartphones', 1, NOW(), NOW()),
(3, 'Laptops', 1, NOW(), NOW()),
(4, 'Audio & Headphones', 1, NOW(), NOW()),
(5, 'Flagship Smartphones', 2, NOW(), NOW()),
(6, 'Mid-Range Smartphones', 2, NOW(), NOW()),
(7, 'Gaming & Ultraportable Laptops', 3, NOW(), NOW()),
(8, 'Premium Wireless Earbuds & Headphones', 4, NOW(), NOW());

-- ------------------------------------------------------------------------------
-- 2. PRODUCTS
-- ------------------------------------------------------------------------------
INSERT INTO products (id, category_id, brand, series, model, description, image_url, has_variants, created_at, updated_at) VALUES
(1, 5, 'Apple', 'iPhone Series', 'iPhone 15 Pro', 'Apple iPhone 15 Pro featuring Aerospace-grade titanium design, A17 Pro chip, customizable Action Button, and advanced 48MP main camera system.', 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW()),
(2, 5, 'Samsung', 'Galaxy S Series', 'Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra with Galaxy AI features, titanium frame, built-in S Pen, 200MP camera, and Snapdragon 8 Gen 3 Mobile Platform.', 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW()),
(3, 7, 'Apple', 'MacBook Air Series', 'MacBook Air M3', 'Incredibly thin and fast MacBook Air powered by the M3 chip. Delivers up to 18 hours of battery life and a stunning Liquid Retina display.', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW()),
(4, 7, 'ASUS', 'ROG Zephyrus', 'ROG Zephyrus G14', 'Compact 14-inch gaming laptop featuring AMD Ryzen 9 8945HS processor, NVIDIA GeForce RTX 4070 GPU, and ROG Nebula OLED Display.', 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW()),
(5, 8, 'Sony', '1000X Series', 'WH-1000XM5', 'Industry-leading noise cancelling wireless headphones with two processors, 8 microphones, exceptional sound quality, and crystal clear hands-free calling.', 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW()),
(6, 8, 'Apple', 'AirPods Pro', 'AirPods Pro 2nd Gen (USB-C)', 'AirPods Pro (2nd generation) with MagSafe Charging Case (USB-C) deliver up to 2x more Active Noise Cancellation than previous gen.', 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW()),
(7, 5, 'OnePlus', 'Flagship Series', 'OnePlus 12', 'Smooth Beyond Belief. Powered by Snapdragon 8 Gen 3, 4th Gen Hasselblad Camera System for Mobile, and 100W SUPERVOOC charging.', 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW()),
(8, 7, 'Dell', 'XPS Series', 'XPS 13', 'Iconic design crafted with machined aluminum. Intel Core Ultra processors with AI acceleration and vibrant InfinityEdge display.', 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&auto=format&fit=crop&q=80', 1, NOW(), NOW());

-- ------------------------------------------------------------------------------
-- 3. PRODUCT VARIANTS
-- ------------------------------------------------------------------------------
INSERT INTO product_variants (id, product_id, variant_name, is_default, created_at, updated_at) VALUES
-- Product 1: iPhone 15 Pro
(101, 1, '256GB - Natural Titanium', 1, NOW(), NOW()),
(102, 1, '512GB - Blue Titanium', 0, NOW(), NOW()),
-- Product 2: Galaxy S24 Ultra
(103, 2, '12GB RAM + 256GB - Titanium Gray', 1, NOW(), NOW()),
(104, 2, '12GB RAM + 512GB - Titanium Black', 0, NOW(), NOW()),
-- Product 3: MacBook Air M3
(105, 3, '8GB RAM + 256GB SSD - Midnight', 1, NOW(), NOW()),
(106, 3, '16GB RAM + 512GB SSD - Starlight', 0, NOW(), NOW()),
-- Product 4: ROG Zephyrus G14
(107, 4, '16GB RAM + 1TB SSD (RTX 4060) - Eclipse Gray', 1, NOW(), NOW()),
(108, 4, '32GB RAM + 1TB SSD (RTX 4070) - Platinum White', 0, NOW(), NOW()),
-- Product 5: Sony WH-1000XM5
(109, 5, 'Black', 1, NOW(), NOW()),
(110, 5, 'Silver', 0, NOW(), NOW()),
-- Product 6: AirPods Pro 2
(111, 6, 'White with USB-C Case', 1, NOW(), NOW()),
-- Product 7: OnePlus 12
(112, 7, '12GB RAM + 256GB - Silky Black', 1, NOW(), NOW()),
(113, 7, '16GB RAM + 512GB - Flowy Emerald', 0, NOW(), NOW()),
-- Product 8: Dell XPS 13
(114, 8, '16GB RAM + 512GB SSD - Graphite', 1, NOW(), NOW()),
(115, 8, '32GB RAM + 1TB SSD - Platinum', 0, NOW(), NOW());

-- ------------------------------------------------------------------------------
-- 4. PRODUCT SPECIFICATIONS
-- ------------------------------------------------------------------------------
INSERT INTO product_specifications (id, product_id, attribute_name, attribute_value, display_order, created_at, updated_at) VALUES
-- Product 1 (iPhone 15 Pro)
(1, 1, 'Processor', 'Apple A17 Pro (3nm)', 1, NOW(), NOW()),
(2, 1, 'Display', '6.1-inch Super Retina XDR OLED, 120Hz ProMotion', 2, NOW(), NOW()),
(3, 1, 'Primary Camera', '48 MP Main + 12 MP Ultra Wide + 12 MP 3x Telephoto', 3, NOW(), NOW()),
(4, 1, 'Battery & Charging', '3274 mAh, USB-C 3.0, MagSafe 15W', 4, NOW(), NOW()),
(5, 1, 'Build Material', 'Grade 5 Titanium frame, Ceramic Shield front', 5, NOW(), NOW()),

-- Product 2 (Galaxy S24 Ultra)
(6, 2, 'Processor', 'Snapdragon 8 Gen 3 for Galaxy', 1, NOW(), NOW()),
(7, 2, 'Display', '6.8-inch Dynamic AMOLED 2X, QHD+, 120Hz, 2600 nits', 2, NOW(), NOW()),
(8, 2, 'Primary Camera', '200 MP Quad Camera System (5x & 10x Optical Zoom)', 3, NOW(), NOW()),
(9, 2, 'Battery & Charging', '5000 mAh, 45W Wired Charging, Fast Wireless 2.0', 4, NOW(), NOW()),
(10, 2, 'Special Feature', 'Integrated S Pen, Galaxy AI Suite', 5, NOW(), NOW()),

-- Product 3 (MacBook Air M3)
(11, 3, 'Processor', 'Apple M3 chip (8-core CPU, 8/10-core GPU)', 1, NOW(), NOW()),
(12, 3, 'Display', '13.6-inch Liquid Retina display with True Tone', 2, NOW(), NOW()),
(13, 3, 'Battery Life', 'Up to 18 hours Apple TV app movie playback', 3, NOW(), NOW()),
(14, 3, 'Ports', 'MagSafe 3 charging port, Two Thunderbolt / USB 4 ports', 4, NOW(), NOW()),

-- Product 4 (ROG Zephyrus G14)
(15, 4, 'Processor', 'AMD Ryzen 9 8945HS Processor', 1, NOW(), NOW()),
(16, 4, 'Graphics', 'NVIDIA GeForce RTX 4070 Laptop GPU 8GB GDDR6', 2, NOW(), NOW()),
(17, 4, 'Display', '14-inch 3K 120Hz OLED ROG Nebula Display', 3, NOW(), NOW()),
(18, 4, 'Weight', '1.50 kg (3.31 lbs)', 4, NOW(), NOW()),

-- Product 5 (Sony WH-1000XM5)
(19, 5, 'Noise Cancellation', 'HD Noise Cancelling Processor QN1 & V1 Processor', 1, NOW(), NOW()),
(20, 5, 'Battery Life', 'Up to 30 hours with NC ON', 2, NOW(), NOW()),
(21, 5, 'Driver Unit', '30mm precision-engineered drivers', 3, NOW(), NOW()),
(22, 5, 'Connectivity', 'Bluetooth 5.2, Multipoint Connection, LDAC', 4, NOW(), NOW()),

-- Product 6 (AirPods Pro 2)
(23, 6, 'Audio Chip', 'Apple H2 headphone chip', 1, NOW(), NOW()),
(24, 6, 'Noise Control', 'Active Noise Cancellation, Adaptive Audio, Transparency', 2, NOW(), NOW()),
(25, 6, 'Charging Case', 'MagSafe Charging Case (USB-C) with Speaker and Lanyard Loop', 3, NOW(), NOW()),

-- Product 7 (OnePlus 12)
(26, 7, 'Processor', 'Snapdragon 8 Gen 3 Mobile Platform', 1, NOW(), NOW()),
(27, 7, 'Camera', '4th Gen Hasselblad Camera System (50MP + 64MP Periscope + 48MP)', 2, NOW(), NOW()),
(28, 7, 'Display', '6.82-inch 2K 120 Hz ProXDR Display (4500 nits peak)', 3, NOW(), NOW()),

-- Product 8 (Dell XPS 13)
(29, 8, 'Processor', 'Intel Core Ultra 7 155H Processor', 1, NOW(), NOW()),
(30, 8, 'Display', '13.4-inch FHD+ InfinityEdge Display', 2, NOW(), NOW()),
(31, 8, 'Material', 'CNC machined aluminum and Gorilla Glass 3', 3, NOW(), NOW());

-- ------------------------------------------------------------------------------
-- 5. VARIANT ATTRIBUTES
-- ------------------------------------------------------------------------------
INSERT INTO variant_attributes (id, variant_id, attribute_name, attribute_value, created_at, updated_at) VALUES
(1, 101, 'Storage', '256GB', NOW(), NOW()),
(2, 101, 'Color', 'Natural Titanium', NOW(), NOW()),
(3, 102, 'Storage', '512GB', NOW(), NOW()),
(4, 102, 'Color', 'Blue Titanium', NOW(), NOW()),
(5, 103, 'RAM & Storage', '12GB + 256GB', NOW(), NOW()),
(6, 103, 'Color', 'Titanium Gray', NOW(), NOW()),
(7, 104, 'RAM & Storage', '12GB + 512GB', NOW(), NOW()),
(8, 104, 'Color', 'Titanium Black', NOW(), NOW()),
(9, 105, 'RAM & SSD', '8GB / 256GB', NOW(), NOW()),
(10, 105, 'Color', 'Midnight', NOW(), NOW()),
(11, 106, 'RAM & SSD', '16GB / 512GB', NOW(), NOW()),
(12, 106, 'Color', 'Starlight', NOW(), NOW()),
(13, 107, 'GPU & RAM', 'RTX 4060 / 16GB', NOW(), NOW()),
(14, 107, 'Color', 'Eclipse Gray', NOW(), NOW()),
(15, 108, 'GPU & RAM', 'RTX 4070 / 32GB', NOW(), NOW()),
(16, 108, 'Color', 'Platinum White', NOW(), NOW()),
(17, 109, 'Color', 'Black', NOW(), NOW()),
(18, 110, 'Color', 'Silver', NOW(), NOW()),
(19, 111, 'Case Type', 'USB-C MagSafe Case', NOW(), NOW()),
(20, 112, 'RAM & Storage', '12GB + 256GB', NOW(), NOW()),
(21, 112, 'Color', 'Silky Black', NOW(), NOW()),
(22, 113, 'RAM & Storage', '16GB + 512GB', NOW(), NOW()),
(23, 113, 'Color', 'Flowy Emerald', NOW(), NOW()),
(24, 114, 'RAM & SSD', '16GB / 512GB', NOW(), NOW()),
(25, 114, 'Color', 'Graphite', NOW(), NOW()),
(26, 115, 'RAM & SSD', '32GB / 1TB', NOW(), NOW()),
(27, 115, 'Color', 'Platinum', NOW(), NOW());

-- ------------------------------------------------------------------------------
-- 6. PLATFORMS
-- ------------------------------------------------------------------------------
INSERT INTO platforms (id, name, website_url, logo_url, is_active, created_at, updated_at) VALUES
(1, 'Amazon', 'https://www.amazon.in', 'https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg', 1, NOW(), NOW()),
(2, 'Flipkart', 'https://www.flipkart.com', 'https://upload.wikimedia.org/wikipedia/commons/7/7a/Flipkart_logo.svg', 1, NOW(), NOW()),
(3, 'Croma', 'https://www.croma.com', 'https://upload.wikimedia.org/wikipedia/commons/e/e4/Croma_Logo.svg', 1, NOW(), NOW()),
(4, 'Reliance Digital', 'https://www.reliancedigital.in', 'https://upload.wikimedia.org/wikipedia/commons/3/30/Reliance_Digital_Logo.svg', 1, NOW(), NOW());

-- ------------------------------------------------------------------------------
-- 7. PLATFORM OFFERS
-- ------------------------------------------------------------------------------
INSERT INTO platform_offers (id, product_variant_id, platform_id, original_price, current_price, currency, seller_name, seller_rating, availability_status, availability_details, delivery_info, offer_details, product_url, last_updated_at) VALUES
-- Variant 101: iPhone 15 Pro 256GB
(1, 101, 1, 134900.00, 127990.00, 'INR', 'Appario Retail Private Ltd', 4.70, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', '10% Instant Discount up to ₹1,500 on ICICI Bank Credit Cards', 'https://www.amazon.in/dp/B0CHX1W1XY', NOW()),
(2, 101, 2, 134900.00, 126999.00, 'INR', 'SuperComNet', 4.60, 'IN_STOCK', 'In stock', 'Express Delivery in 24 hours', '5% Cashback on Flipkart Axis Bank Card', 'https://www.flipkart.com/apple-iphone-15-pro-natural-titanium-256-gb/p/itm101', NOW()),
(3, 101, 3, 134900.00, 129900.00, 'INR', 'Croma Retail', 4.50, 'IN_STOCK', 'Only 4 units remaining in store', 'Store Pickup & Home Delivery available', 'Flat ₹3,000 instant discount on HDFC Cards', 'https://www.croma.com/apple-iphone-15-pro-256gb/p/277001', NOW()),
(4, 101, 4, 134900.00, 128900.00, 'INR', 'Reliance Retail', 4.40, 'IN_STOCK', 'In stock', 'Standard Delivery in 2 days', 'Bank offer: Extra ₹2,500 off on select credit cards', 'https://www.reliancedigital.in/iphone-15-pro-256gb/p/493838101', NOW()),

-- Variant 102: iPhone 15 Pro 512GB
(5, 102, 1, 154900.00, 146900.00, 'INR', 'Appario Retail Private Ltd', 4.70, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', 'Flat ₹4,000 Instant Discount with SBI Cards', 'https://www.amazon.in/dp/B0CHX2V5YZ', NOW()),
(6, 102, 2, 154900.00, 145990.00, 'INR', 'SuperComNet', 4.60, 'IN_STOCK', 'In stock', 'Delivery by Tomorrow', 'Exchange bonus up to ₹8,000', 'https://www.flipkart.com/apple-iphone-15-pro-blue-titanium-512-gb/p/itm102', NOW()),

-- Variant 103: Galaxy S24 Ultra 256GB
(7, 103, 1, 134999.00, 124999.00, 'INR', 'STPL RED', 4.80, 'IN_STOCK', 'In stock', 'FREE Next Day Delivery', '₹10,000 Instant HDFC Bank Discount', 'https://www.amazon.in/dp/B0CS5X6Y7Z', NOW()),
(8, 103, 2, 134999.00, 123999.00, 'INR', 'Flashtech Retail', 4.50, 'IN_STOCK', 'In stock', 'Delivery in 2 Days', 'Extra ₹12,000 off on exchange of old flagship', 'https://www.flipkart.com/samsung-galaxy-s24-ultra-5g-titanium-gray-256-gb/p/itm103', NOW()),
(9, 103, 3, 134999.00, 125999.00, 'INR', 'Croma Retail', 4.50, 'IN_STOCK', 'In stock', 'Same Day Delivery available', 'Includes complimentary Wireless Charger Pad', 'https://www.croma.com/samsung-galaxy-s24-ultra-256gb/p/288103', NOW()),

-- Variant 105: MacBook Air M3 8GB/256GB
(10, 105, 1, 114900.00, 104900.00, 'INR', 'Appario Retail Private Ltd', 4.70, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', 'No Cost EMI up to 12 months on select cards', 'https://www.amazon.in/dp/B0CX23G105', NOW()),
(11, 105, 2, 114900.00, 103990.00, 'INR', 'IndiFlashMart', 4.60, 'IN_STOCK', 'In stock', 'Delivery by Tomorrow', '₹5,000 Instant Discount on HDFC Bank Cards', 'https://www.flipkart.com/apple-macbook-air-m3-8gb-256gb-midnight/p/itm105', NOW()),
(12, 105, 4, 114900.00, 105900.00, 'INR', 'Reliance Retail', 4.40, 'IN_STOCK', 'In stock', 'Free Delivery within 3 days', 'Student Discount: Additional 5% off with student ID', 'https://www.reliancedigital.in/macbook-air-m3-256gb/p/494105', NOW()),

-- Variant 107: ROG Zephyrus G14 RTX 4060
(13, 107, 1, 174990.00, 159990.00, 'INR', 'Appario Retail Private Ltd', 4.70, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', 'Includes ROG Gaming Backpack & Mouse', 'https://www.amazon.in/dp/B0D107G14', NOW()),
(14, 107, 2, 174990.00, 158490.00, 'INR', 'SuperComNet', 4.60, 'IN_STOCK', 'In stock', 'Delivery in 2 Days', 'Bank Discount ₹3,000 on Axis Bank Credit Cards', 'https://www.flipkart.com/asus-rog-zephyrus-g14-rtx-4060/p/itm107', NOW()),

-- Variant 109: Sony WH-1000XM5 Black
(15, 109, 1, 34990.00, 26990.00, 'INR', 'Appario Retail Private Ltd', 4.70, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', 'Flat ₹2,000 Instant Discount on all major credit cards', 'https://www.amazon.in/dp/B0A109XM5', NOW()),
(16, 109, 2, 34990.00, 26490.00, 'INR', 'SuperComNet', 4.60, 'IN_STOCK', 'In stock', 'Express Delivery', 'Extra ₹1,500 coupon discount at checkout', 'https://www.flipkart.com/sony-wh-1000xm5-black/p/itm109', NOW()),
(17, 109, 3, 34990.00, 27990.00, 'INR', 'Croma Retail', 4.50, 'IN_STOCK', 'In stock', 'Store Pickup Today', 'Includes 3-month Extended Warranty', 'https://www.croma.com/sony-wh1000xm5-black/p/269109', NOW()),

-- Variant 111: AirPods Pro 2 USB-C
(18, 111, 1, 24900.00, 21990.00, 'INR', 'Appario Retail Private Ltd', 4.70, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', '₹2,000 Instant Discount on ICICI Bank Cards', 'https://www.amazon.in/dp/B0C111APP2', NOW()),
(19, 111, 2, 24900.00, 21490.00, 'INR', 'IndiFlashMart', 4.60, 'IN_STOCK', 'In stock', 'Delivery by Tomorrow', 'Extra 5% off on Flipkart Axis Card', 'https://www.flipkart.com/apple-airpods-pro-2-usbc/p/itm111', NOW()),
(20, 111, 4, 24900.00, 22490.00, 'INR', 'Reliance Retail', 4.40, 'IN_STOCK', 'In stock', 'Standard Delivery', 'Flat ₹1,500 Bank Instant Off', 'https://www.reliancedigital.in/airpods-pro-2-usbc/p/493111', NOW()),

-- Variant 112: OnePlus 12 256GB
(21, 112, 1, 64999.00, 59999.00, 'INR', 'OnePlus Authorized Retail', 4.80, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', '₹5,000 Instant Discount on ICICI & OneCard', 'https://www.amazon.in/dp/B0OP112256', NOW()),
(22, 112, 2, 64999.00, 59499.00, 'INR', 'SuperComNet', 4.60, 'IN_STOCK', 'In stock', 'Delivery in 2 Days', 'Exchange bonus up to ₹4,000', 'https://www.flipkart.com/oneplus-12-silky-black-256gb/p/itm112', NOW()),

-- Variant 114: Dell XPS 13 16GB/512GB
(23, 114, 1, 149990.00, 139990.00, 'INR', 'Appario Retail Private Ltd', 4.70, 'IN_STOCK', 'In stock', 'FREE One-Day Delivery', 'Includes 1-Year Premium Support Plus', 'https://www.amazon.in/dp/B0XPS114512', NOW()),
(24, 114, 3, 149990.00, 141990.00, 'INR', 'Croma Retail', 4.50, 'IN_STOCK', 'In stock', 'Express Delivery', 'Flat ₹4,000 Cashback on HDFC Cards', 'https://www.croma.com/dell-xps-13-16gb-512gb/p/294114', NOW());

-- ------------------------------------------------------------------------------
-- 8. REVIEWS
-- ------------------------------------------------------------------------------
INSERT INTO reviews (id, product_variant_id, platform_id, reviewer_name, rating, review_title, review_text, review_date, verified_purchase, source_url, fetched_at) VALUES
-- Variant 101: iPhone 15 Pro 256GB
(1, 101, 1, 'Arjun Sharma', 5.00, 'Best iPhone ever made!', 'The Titanium build feels remarkably lightweight in hand compared to the 14 Pro. A17 Pro runs all heavy games effortlessly, and the Action button is very useful.', '2026-08-01', 1, 'https://www.amazon.in/review/R1101A', NOW()),
(2, 101, 1, 'Priya Nair', 4.50, 'Stunning Camera & Display', 'Camera quality, especially portrait mode and video recording, is top-tier. Battery life easily lasts a full day of moderate-to-heavy usage.', '2026-08-05', 1, 'https://www.amazon.in/review/R1101B', NOW()),
(3, 101, 2, 'Rohan Verma', 4.00, 'Great performance, pricey', 'Extremely smooth 120Hz display and premium build quality. Charging speed is improved with USB-C, though battery could be slightly larger.', '2026-08-10', 1, 'https://www.flipkart.com/review/R1101C', NOW()),
(4, 101, 3, 'Kavita Patel', 5.00, 'Smooth experience from Croma store', 'Purchased at Croma store with bank discount. Fantastic screen clarity, incredible camera sharpness, and overall stellar Apple ecosystem integration.', '2026-08-12', 1, 'https://www.croma.com/review/R1101D', NOW()),

-- Variant 103: Galaxy S24 Ultra 256GB
(5, 103, 1, 'Vikram Mehta', 5.00, 'Galaxy AI is a game changer!', 'The Live Translate and Circle to Search features are mind-blowing. The anti-reflective screen glare reduction works wonders in outdoor sunlight.', '2026-08-02', 1, 'https://www.amazon.in/review/R1103A', NOW()),
(6, 103, 2, 'Ananya Gupta', 4.50, 'Unbeatable Zoom Camera', 'The 200MP camera captures insane details. 5x and 10x optical zoom photos are crystal clear. Battery easily lasts 1.5 days.', '2026-08-07', 1, 'https://www.flipkart.com/review/R1103B', NOW()),
(7, 103, 3, 'Suresh Kumar', 4.00, 'Powerhouse Flagship', 'Fastest Android phone right now. S Pen is super convenient for notes and photo taking. A bit bulky in pocket but well worth the size.', '2026-08-14', 1, 'https://www.croma.com/review/R1103C', NOW()),

-- Variant 105: MacBook Air M3 8GB/256GB
(8, 105, 1, 'Sneha Rao', 5.00, 'Silent perfection and incredible battery!', 'No fan noise whatsoever, instant wake from sleep, and battery lasts almost two days of coding and web browsing. Midnight color looks sleek!', '2026-08-03', 1, 'https://www.amazon.in/review/R1105A', NOW()),
(9, 105, 2, 'Amitabh Joshi', 4.50, 'Super fast M3 chip', 'Significant upgrade over M1/Intel models. Handles video editing and multiple browser tabs with ease. MagSafe charging is very convenient.', '2026-08-08', 1, 'https://www.flipkart.com/review/R1105B', NOW()),

-- Variant 109: Sony WH-1000XM5 Black
(10, 109, 1, 'Deepak Chawla', 5.00, 'Best Noise Cancellation on the market', 'Cuts out office noise, airplane engines, and traffic completely. Sound signature is rich and warm with punchy bass and deep clarity.', '2026-08-04', 1, 'https://www.amazon.in/review/R1109A', NOW()),
(11, 109, 2, 'Meera Reddy', 4.50, 'Ultra comfortable for long listening sessions', 'Lightweight earcups don\'t press on ears. Call quality in windy environments is drastically better than the XM4.', '2026-08-09', 1, 'https://www.flipkart.com/review/R1109B', NOW()),

-- Variant 111: AirPods Pro 2 USB-C
(12, 111, 1, 'Siddharth Roy', 5.00, 'Essential accessory for iPhone users', 'Seamless switching between Mac and iPhone. Adaptive Audio automatically adjusts transparency when someone talks to you. USB-C case is great.', '2026-08-06', 1, 'https://www.amazon.in/review/R1111A', NOW()),

-- Variant 112: OnePlus 12 256GB
(13, 112, 1, 'Karan Malhotra', 5.00, '100W Charging is insane!', 'Full charge in under 26 minutes! The 2K display is bright and buttery smooth. Hasselblad colors in photography look very natural.', '2026-08-11', 1, 'https://www.amazon.in/review/R1112A', NOW());

-- ------------------------------------------------------------------------------
-- 9. USERS (Demo test accounts for authentication end-to-end testing)
-- Password for demo users is: password123 (BCrypt hashed)
-- ------------------------------------------------------------------------------
INSERT IGNORE INTO users (id, name, email, password, role, country, region, created_at, updated_at) VALUES
(1, 'Demo User', 'demo@shopsense.ai', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07Xd0XDMxs.AQ84d62', 'ROLE_USER', 'India', 'Maharashtra', NOW(), NOW()),
(2, 'Test Buyer', 'testbuyer@shopsense.ai', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07Xd0XDMxs.AQ84d62', 'ROLE_USER', 'India', 'Karnataka', NOW(), NOW());
