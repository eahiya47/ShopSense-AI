# ShopSense AI Database Documentation

## MySQL Setup
1. Ensure MySQL Server is installed and running on port 3306.
2. Execute `schema.sql` to create database `shopsense_db` and required table structure:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
3. Database configuration for Spring Boot is specified in `backend/src/main/resources/application.properties`.
