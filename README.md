# E-commerce user API Documentation

## Overview

This API provides all the necessary endpoints to build a user-facing e-commerce application. It allows users view, search and filter products by name, price range, category and supplier. Users can also register, login, view and update their profile and shipping address, and manage their orders (create new orders, view order history and cancel existing orders). The API uses JWT for authentication and authorization, ensuring that only authenticated users can access protected endpoints.

## Technologies Used

- **Java 21** - Programming language used for developing the application.
- **Maven** - Build and dependency management.
- **Spring Boot 3.5.11** - Application framework for building the REST API.
- **Spring Web** - HTTP routing and REST controllers.
- **Spring Data JPA (Hibernate)** - ORM and database persistence layer.
- **Spring Security** - Authentication/authorization for protected endpoints.
- **JWT** - Token-based authentication.
- **Bean Validation (Jakarta Validation / Spring Validation)** - Request payload validation.
- **MariaDB JDBC Driver** - Database connectivity for MariaDB.
- **MariaDB** - Relational database used for application data.

## Database features

Database schema used was the one provided by for the assignment, but some additional features were implemented:

- `app_user` table was added to store user login credentials and role information. The table has a one-to-one relationship with the "customer" table. This was done to separate authentication data from customer profile data.
- `user_id` field was added to the "customer" table to link it to the "app_user" table.
- `version` field was added to the "product" table to implement optimistic locking.
- `reserved_quantity` field was added to the "product" table to keep track of the quantity of each product that is reserved for existing orders but not yet delivered.

Below are the implemented database features:

- **Indexing**:
  - `customer_id` in `orders` for fast retrieval of user orders.
  - `category_id` in `products` for efficient category-based queries.
  - `supplier_id` in `products` for efficient supplier-based queries.
  - `price` in `products` for efficient price range queries.
  - `username` in `app_users` for fast authentication lookups.

(example of one of the indexes created)
  ```
  CREATE INDEX idx_products_price
  ON products (price);
  ```
  
- **Information Security**:
  - Database user used by the API is configured with the least privileges necessary for application functionality.
    - `SELECT`: app_user, customeraddresses, customers, orderitems, orders, productcategories, products, suppliers
    - `POST`: orders, orderitems, customers, customeraddresses, app_user
    - `UPDATE`: products, orders, customers, customeraddresses, app_user
    - `DELETE`: NONE
  - This database user can only access the database from the same IP address as the API server.
    - Only `localhost`

- **Events**:
  - A scheduled MySQL event runs every 12 hours and updates the status of orders whose delivery date has already passed to "DELIVERED".
    ```
    CREATE EVENT auto_ship_orders
    ON SCHEDULE EVERY 12 HOUR
    DO
    UPDATE orders
    SET status = 'SHIPPED'
    WHERE delivery_date IS NOT NULL
    AND delivery_date < NOW()
    AND status = 'NEW';
    ```

- **Triggers**:
  - A database trigger is set up to automatically reduce the stock quantity and reserved quantity of products when orders that include them have their status updated to "SHIPPED".
    ```
    CREATE TRIGGER trg_order_status_shipped
    AFTER UPDATE ON orders
    FOR EACH ROW
    BEGIN
    IF OLD.status = 'NEW' AND NEW.status = 'SHIPPED' THEN
    UPDATE products p
    JOIN orderitems oi ON oi.product_id = p.id
    SET
    p.reserved_quantity = p.reserved_quantity - oi.quantity,
    p.stock_quantity = p.stock_quantity - oi.quantity
    WHERE oi.order_id = NEW.id;
    END IF;
    END
    ```
    
- **Locking**:
  - Optimistic locking is implemented for the "product" entity using a version field.
  - Pessimistic locking is used when creating and cancelling orders.

- **Temporal tables**:
  - App_user table was made into a temporal table, which allows for tracking changes to user accounts over time and provides the ability to query historical data about user accounts if needed (e.g., for account recovery).
    ```
    ALTER TABLE app_user
    ADD COLUMN valid_from TIMESTAMP(6) GENERATED ALWAYS AS ROW START,
    ADD COLUMN valid_to   TIMESTAMP(6) GENERATED ALWAYS AS ROW END,
    ADD PERIOD FOR SYSTEM_TIME (valid_from, valid_to),
    ADD SYSTEM VERSIONING;
    ```

- **Views**:
  - A database view named `order_summary` was created to simplify queries for simplified order information, such as total cost and the amount of different products in each order.
    ```
    CREATE VIEW order_summary AS
    SELECT
    o.customer_id,
    o.id AS order_id,
    o.order_date,
    o.delivery_date,
    o.status,
    SUM(oi.quantity * oi.unit_price) AS total_cost,
    COUNT(DISTINCT oi.product_id) AS amount_of_different_products
    FROM orders o
    JOIN orderitems oi ON oi.order_id = o.id
    GROUP BY o.id
    ```
    
- **Mass Operations**:
  - Since the API is focused on user-facing operations, there are no endpoints that need to perform mass updates or deletions.

## API security features

- **Authentication**:
  - JWT tokens are used for stateless authentication, with secure signing and expiration.

- **Validation**:
  - Input validation is implemented to prevent invalid data entering the database.

- **Hash-based password storage**:
  - User passwords are hashed using BCrypt before being stored in the database.

- **Endpoint protection**:
  - Endpoints are protected by only allowing authenticated users to access sensitive operations (e.g., viewing profile, managing orders).

- **Transactions**:
  - Order creation and cancellation operations are wrapped in transactions to ensure data consistency.

## Backup and recovery

An dedicated user with privileges only to perform backups was created in the database and its credentials were used to run the following PowerShell script.
The script creates a backup of the database using `mysqldump`, compresses the backup file, encrypts it with GPG and then deletes the unencrypted backup file and the uncompressed and unencrypted backup file.
<br><br>
This script can be scheduled to run at regular intervals using Windows Task Scheduler. The time for the scheduled task should be set to a time when the database is not expected to be under heavy load, such as 2:00 AM.
<br><br>
For the purposes of this assigment an environment variable named `DB_BACKUP_ENC_PASS` was not created, though for a production environment it would be necessary to store the encryption password in a secure way.
```
$DBName = "tietokantaratkaisutpopulated"
$DBUser = 
$DBPass = 
$BackupDir = "C:\mysql-backups"
$Date = Get-Date -Format "yyyy-MM-dd"
$EncPass = 

if (!(Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

$MySQLDump = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe"
$SqlFile = "$BackupDir\$DBName-$Date.sql"
$ZipFile = "$SqlFile.zip"
$EncryptedFile = "$ZipFile.gpg"

& $MySQLDump `
    --user=$DBUser `
    --password=$DBPass `
    --routines `
    --events `
    --triggers `
    --single-transaction `
    $DBName `
    > $SqlFile

Compress-Archive -Path $SqlFile -DestinationPath $ZipFile

Remove-Item $SqlFile

gpg --batch --yes --passphrase "$EncPass" -c $ZipFile

Remove-Item $ZipFile
```

Automated transfers of the backup files to cloud storage or an offsite location were also considered, but for the purposes of this assignment they were not implemented.

## API documentation

### Endpoints

Endpoints are categorized into two groups based on whether they require authentication or not. Endpoints that do not require authentication are accessible to anyone, while endpoints that require authentication can only be accessed by users who have logged in and obtained a valid JWT token.
<br>

#### Does not require authentication

Authentication

| Endpoint                      | Purpose                                         | requests body                                                                                                                                                                                                                                | response body                                                                                                                                                                                                                                                          | status code |
|-------------------------------|-------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| POST /auth/register           | Register a new user                             | ```{"username": "JohnDoe", "password": "secret123", "firstName": "John", "lastName": "Doe", "email": "JohnDoe@gmail.com", "phone": "0446753464", "street": "Roadyroad 5", "city": "City 17", "postalCode": "54321", "country": "Germany"}``` | A success message<br/>```{"message": "User registered successfully"}```<br/>or an error message<br/>```{"message": "Username already exists"}```, <br/>```{"username": "must not be blank"}``` etc.                                                                    | 200 or 400  |
| POST /auth/login              | Login to a user                                 | ```{"username": "JohnDoe", "password": "secret123"}```                                                                                                                                                                                       | A JWT token<br/>```{"token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKb2huRG9lIiwidXNlcklkIjoxMSwiaWF0IjoxNzczNTY4OTIzLCJleHAiOjE3NzM2NTUzMjN9.xuTay4kxPw70azqc7TvnR9VIlSfDDhlsY1FYpekJaF8"}```<br/>or an error message<br/>```{"message": "Invalid username or password"}``` | 200 or 400  |
| PATCH /auth/change_password   | Change the password of the authenticated user   | ```{"username": "JohnDoe", "oldPassword": "secret123", "newPassword": "newSecret456"}```                                                                                                                                                     | A success message<br/>```{"message": "Password changed successfully"}```<br/>or an error message<br/>```{"message": "Old password is incorrect"}```                                                                                                                    | 200 or 400  |

Product

| Endpoint                                    | Purpose                              | requests body | response body                                                                                                                                                       | status code |
|---------------------------------------------|--------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| GET /product/all                            | Retrieve all products                | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` | 200         |
| GET /product/{id}                           | Retrieve a specific product by ID    | {}            | A single product entity```{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}```               | 200 or 404  |
| GET /product/search/name/{name}             | Search for products by name          | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` | 200 or 404  |
| GET /product/search/price/{min}/{max}       | Search for products by price range   | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` | 200         |
| GET /product/search/supplier/{supplierName} | Search for products by supplier name | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` | 200 or 404  |

Category

| Endpoint           | Purpose                                   | requests body | response body                                                                                                                                                                                                                                                                                                             | status code |
|--------------------|-------------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| GET /category/all  | Retrieve all categories                   | {}            | List of category entities<br/>```[{"id":1,"name":"Elektroniikka","description":"Sähkölaitteet, komponentit ja elektroniikkatuotteet"}, ... ]```                                                                                                                                                                           | 200         |
| GET /category/{id} | Retrieve all products in a category by ID | {}            | List of product entities<br/>```[{"id":2,"name":"Happy Pack 382","description":"Score million throw thing instead ball line think.","price":546.08,"stock_quantity":407,"reserved_quantity":0,"category":{"id":10,"name":"Urheilu & vapaa-aika","description":"Urheiluvälineet, pelit ja vapaa-ajan tuotteet"}}, ... ]``` | 200 or 404  |

Supplier

| Endpoint           | Purpose                                                                                                                                         | requests body | response body                                                                                                                            | status code |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| GET /supplier/all  | Retrieve all suppliers (to be able to search for products by supplier name using the "GET /product/search/supplier/{supplierName}" endpoint.)   | {}            | List of supplier entities<br/>```[{"name": "Polar Electronics Oy"}, {"name": "EcoTech Ltd"}, {"name": "Suomen Viherpalvelut"}, ... ]```  | 200         |

<br>

#### Requires authentication
These endpoints require the user to be authenticated by including a valid JWT token in the "Authorization" header of the request. The token should be in the format ```Authorization: Bearer [token here]```. If the token is missing, invalid or expired, the API will respond with a 403 Forbidden status code.


Customer

| Endpoint             | Purpose                                            | requests body                                                                                         | response body                                                                                                                                                                                               | status code       |
|----------------------|----------------------------------------------------|-------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| GET /profile         | Retrieve the authenticated user's profile          | {}                                                                                                    | Customer entity<br/>```{"first_name": "Matti", "last_name": "Tepponen", "email": "Matti@tepponen.com", "phone": "0446753464"}```                                                                            | 200 or 403        |
| GET /address         | Retrieve the authenticated user's shipping address | {}                                                                                                    | Address entity<br/>```{"street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}```                                                                                          | 200 or 403        |
| PATCH /address       | Update the authenticated user's shipping address   | ```{"street": "New Street 1", "city": "New City", "postalCode": "12345", "country": "New Country"}``` | A success message<br/>```{"message": "Address updated successfully"}```<br/>or an error message<br/>```{"country": "Country cannot be empty"}``` etc.                                                       | 200 or 400 or 403 |
| PATCH /profile/phone | Update the authenticated user's phone number       | ```{"phone": "0441234567"}```                                                                         | A success message<br/>```{"message": "Phone number updated successfully"}```<br/>or an error message<br/>```{"phone": "Phone number cannot be empty"}```, ```{"phone": "Invalid phone number format"}```    | 200 or 400 or 403 |
| PATCH /profile/email | Update the authenticated user's email address      | ```{"email": "developer@example.com"}```                                                              | A success message<br/>```{"message": "Email address updated successfully"}```<br/>or an error message<br/>```{"email": "Email address cannot be empty"}```, ```{"email": "Invalid email address format"}``` | 200 or 400 or 403 |

Order

| Endpoint                  | Purpose                                                                                                                                            | requests body                                                                           | response body                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | status code        |
|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------|
| GET /order/list           | Lists all orders of the authenticated user                                                                                                         | {}                                                                                      | List of order entities<br/>```[{"id": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "shippingAddress": { "street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}, "totalPrice": 1773.50, "items": [{"quantity": 2, "product": {"name": "Happy Pack 382", "description": "Score million throw thing instead ball line think.", "price": 546.08, "category": {"name": "Urheilu & vapaa-aika"}}}, {"quantity": 1, "product": {"name": "Quantum Widget 457", "description": "Ask decide need next very capital.", "price": 681.34, "category": {"name": "Vaatteet"}}}]}, ... ]```                                                                                                                                                                                                                         | 200 or 403         |
| GET /order/list/summary   | Lists a summary of all orders of the authenticated user                                                                                            | {}                                                                                      | List of order summary entities<br/>```[{"orderId": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "totalPrice": 1773.50, "amountOfDifferentProducts": 2}, ... ]```                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | 200 or 403         |
| GET /order/{id}           | Retrieve a specific order of the authenticated user by ID                                                                                          | {}                                                                                      | A single order entity<br/>```{"id": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "shippingAddress": { "street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}, "totalPrice": 1773.50, "items": [{"quantity": 2, "product": {"name": "Happy Pack 382", "description": "Score million throw thing instead ball line think.", "price": 546.08, "category": {"name": "Urheilu & vapaa-aika"}}}, {"quantity": 1, "product": {"name": "Quantum Widget 457", "description": "Ask decide need next very capital.", "price": 681.34, "category": {"name": "Vaatteet"}}}]}```<br/>or an error message<br/>```{"error": "Order not found"}```, ```"error": Customer not found"```                                                                                                                               | 200 or 403 or 404  |
| POST /order/create        | Create a new order for the authenticated user (This also updates the reserved quantity of each product included in the order in the database.)     | ```{"items": [{ "productId": 2, "quantity": 2 }, { "productId": 3, "quantity": 1 }]}``` | A single order entity<br/>```{"id": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "shippingAddress": { "street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}, "totalPrice": 1773.50, "items": [{"quantity": 2, "product": {"name": "Happy Pack 382", "description": "Score million throw thing instead ball line think.", "price": 546.08, "category": {"name": "Urheilu & vapaa-aika"}}}, {"quantity": 1, "product": {"name": "Quantum Widget 457", "description": "Ask decide need next very capital.", "price": 681.34, "category": {"name": "Vaatteet"}}}]}```<br/>or an error message<br/>```{"items[0].quantity": "Quantity must be greater than zero"}```<br/>```{"items": "items cannot be empty"}``` etc.                                                                                  | 200 or 400 or 403  |
| PATCH /order/{id}/cancel  | Cancel an existing order of the autheticated user (This also updates the reserved quantity of each product included in the order in the database.) | {}                                                                                      | A single order entity<br/>```{"id": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "shippingAddress": { "street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}, "totalPrice": 1773.50, "items": [{"quantity": 2, "product": {"name": "Happy Pack 382", "description": "Score million throw thing instead ball line think.", "price": 546.08, "category": {"name": "Urheilu & vapaa-aika"}}}, {"quantity": 1, "product": {"name": "Quantum Widget 457", "description": "Ask decide need next very capital.", "price": 681.34, "category": {"name": "Vaatteet"}}}]}```<br/>or an error message<br/>```{"error": "Order already cancelled", "message": "This order cannot be cancelled again."}```,<br/> ```{"error", "Order already shipped", "message", "Shipped orders cannot be cancelled."}``` etc. | 200 or 400 or 403  |