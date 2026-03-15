# E-commerce user API Documentation

## Overview

This API provides all the necessary endpoints to build a user-facing e-commerce application. It allows users view, search and filter products by name, price range, category and supplier. Users can also register, login, view and update their profile and shipping address, and manage their orders (create new orders, view order history and cancel existing orders). The API uses JWT for authentication and authorization, ensuring that only authenticated users can access protected endpoints.

## Technologies Used

- **Java 21** - Programming language used for developing the application.
- **Spring Boot 3.5.11** - Application framework for building the REST API.
- **Spring Web** - HTTP routing and REST controllers.
- **Spring Data JPA (Hibernate)** - ORM and database persistence layer.
- **MariaDB** - Relational database used for application data.
- **MariaDB JDBC Driver** - Database connectivity for MariaDB.
- **Spring Security** - Authentication/authorization for protected endpoints.
- **JWT** - Token-based authentication (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).
- **Bean Validation (Jakarta Validation / Spring Validation)** - Request payload validation.
- **Maven** - Build and dependency management.

## Database features

Database schema used was the one provided by for the assignment, but some additional features were implemented:

- "app_user" table was added to store user login credentials and role information. The table has a one-to-one relationship with the "customer" table. This was done to separate authentication data from customer profile data.
- "user_id" field was added to the "customer" table to link it to the "app_user" table.
- "version" field was added to the "product" table to implement optimistic locking.
- "reserved_quantity" field was added to the "product" table to keep track of the quantity of each product that is reserved for existing orders but not yet delivered.

Below are the implemented database features:

- **Indexing**:
  - 'customer_id' in 'orders' for fast retrieval of user orders.
  - 'category_id' in 'products' for efficient category-based queries.
  - 'supplier_id' in 'products' for efficient supplier-based queries.
  - 'price' in 'products' for efficient price range queries.
  - 'username' in 'app_users' for fast authentication lookups.
  
- **Information Security**:
  - Database user used by the API is configured with the least privileges necessary for application functionality.
  - This database user can only access the database from the same IP address as the API server.

- **Database events**:
  - A scheduled MySQL event runs every 12 hours and updates the status of orders whose delivery date has already passed to "DELIVERED".

- **Triggers**:
  - A database trigger is set up to automatically reduce the stock quantity and reserved quantity of products when orders that include them have their status updated to "SHIPPED".

- **Locking**:
  - Optimistic locking is implemented for the "product" entity using a version field.

- **Temporal tables**:
  - App_user table was made into a temporal table, which allows for tracking changes to user accounts over time and provides the ability to query historical data about user accounts if needed (e.g., for account recovery).
    ```
    ALTER TABLE app_user
    ADD COLUMN valid_from TIMESTAMP(6) GENERATED ALWAYS AS ROW START,
    ADD COLUMN valid_to   TIMESTAMP(6) GENERATED ALWAYS AS ROW END,
    ADD PERIOD FOR SYSTEM_TIME (valid_from, valid_to),
    ADD SYSTEM VERSIONING;
    ```


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

| Endpoint            | Purpose              | requests body                                                                                                                                                                                                                                | response body                                                                                                                                                                                                                                                            |
|---------------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| POST /auth/register | Register a new user  | ```{"username": "JohnDoe", "password": "secret123", "firstName": "John", "lastName": "Doe", "email": "JohnDoe@gmail.com", "phone": "0446753464", "street": "Roadyroad 5", "city": "City 17", "postalCode": "54321", "country": "Germany"}``` | A success message<br/>```{"message": "User registered successfully"}```<br/>or an error message<br/>```{"message": "Username already exists"}```, <br/>```{"username": "must not be blank"}``` etc.                                                                      |
| POST /auth/login    | Login to a user      | ```{"username": "JohnDoe", "password": "secret123"}```                                                                                                                                                                                       | A JWT token<br/>```{"token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKb2huRG9lIiwidXNlcklkIjoxMSwiaWF0IjoxNzczNTY4OTIzLCJleHAiOjE3NzM2NTUzMjN9.xuTay4kxPw70azqc7TvnR9VIlSfDDhlsY1FYpekJaF8"}```<br/>or an error message<br/>```{"message": "Invalid username or password"}```   |

Product

| Endpoint                                    | Purpose                              | requests body | response body                                                                                                                                                       |
|---------------------------------------------|--------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET /product/all                            | Retrieve all products                | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` |
| GET /product/{id}                           | Retrieve a specific product by ID    | {}            | A single product entity```{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}```               |
| GET /product/search/name/{name}             | Search for products by name          | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` |
| GET /product/search/price/{min}/{max}       | Search for products by price range   | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` |
| GET /product/search/supplier/{supplierName} | Search for products by supplier name | {}            | List of product entities<br/>```[{"name":"Super Bug 360","description":"Her fall move current him.","price":22.22,"category":{"name":"Kodin tarvikkeet"}}, ... ]``` |

Category

| Endpoint           | Purpose                                   | requests body | response body                                                                                                                                                                                                                                                                                                             |
|--------------------|-------------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET /category/all  | Retrieve all categories                   | {}            | List of category entities<br/>```[{"id":1,"name":"Elektroniikka","description":"Sähkölaitteet, komponentit ja elektroniikkatuotteet"}, ... ]```                                                                                                                                                                           |
| GET /category/{id} | Retrieve all products in a category by ID | {}            | List of product entities<br/>```[{"id":2,"name":"Happy Pack 382","description":"Score million throw thing instead ball line think.","price":546.08,"stock_quantity":407,"reserved_quantity":0,"category":{"id":10,"name":"Urheilu & vapaa-aika","description":"Urheiluvälineet, pelit ja vapaa-ajan tuotteet"}}, ... ]``` |

Supplier

| Endpoint           | Purpose                                  | requests body | response body                                                                                                                            |
|--------------------|------------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------|
| GET /supplier/all  | Retrieve all suppliers                   | {}            | List of supplier entities<br/>```[{"name": "Polar Electronics Oy"}, {"name": "EcoTech Ltd"}, {"name": "Suomen Viherpalvelut"}, ... ]```  |

<br>

#### Requires authentication

Customer

| Endpoint       | Purpose                                             | requests body                                                                                         | response body                                                                                                                                         |
|----------------|-----------------------------------------------------|-------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET /profile   | Retrieve the authenticated user's profile           | {}                                                                                                    | Customer entity<br/>```{"first_name": "Matti", "last_name": "Tepponen", "email": "Matti@tepponen.com", "phone": "0446753464"}```                      |
| GET /address   | Retrieve the authenticated user's shipping address  | {}                                                                                                    | Address entity<br/>```{"street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}```                                    |
| PATCH /address | Update the authenticated user's shipping address    | ```{"street": "New Street 1", "city": "New City", "postalCode": "12345", "country": "New Country"}``` | A success message<br/>```{"message": "Address updated successfully"}```<br/>or an error message<br/>```{"country": "Country cannot be empty"}``` etc. |

Order

| Endpoint                 | Purpose                                                                                                                                            | requests body                                                                            | response body                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET /order/list          | Lists all orders of the authenticated user                                                                                                         | {}                                                                                       | List of order entities<br/>```[{"id": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "shippingAddress": { "street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}, "totalPrice": 1773.50, "items": [{"quantity": 2, "product": {"name": "Happy Pack 382", "description": "Score million throw thing instead ball line think.", "price": 546.08, "category": {"name": "Urheilu & vapaa-aika"}}}, {"quantity": 1, "product": {"name": "Quantum Widget 457", "description": "Ask decide need next very capital.", "price": 681.34, "category": {"name": "Vaatteet"}}}]}, ... ]```                                                                                                                                                                                                                         |
| POST /order/create       | Create a new order for the authenticated user (This also updates the reserved quantity of each product included in the order in the database.)     | ```{"items": [{ "productId": 2, "quantity": 2 }, { "productId": 3, "quantity": 1 }]}```  | A single order entity<br/>```{"id": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "shippingAddress": { "street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}, "totalPrice": 1773.50, "items": [{"quantity": 2, "product": {"name": "Happy Pack 382", "description": "Score million throw thing instead ball line think.", "price": 546.08, "category": {"name": "Urheilu & vapaa-aika"}}}, {"quantity": 1, "product": {"name": "Quantum Widget 457", "description": "Ask decide need next very capital.", "price": 681.34, "category": {"name": "Vaatteet"}}}]}```<br/>or an error message<br/>```{"items[0].quantity": "Quantity must be greater than zero"}```<br/>```{"items": "items cannot be empty"}``` etc.                                                                                  |
| PATCH /order/{id}/cancel | Cancel an existing order of the autheticated user (This also updates the reserved quantity of each product included in the order in the database.) | {}                                                                                       | A single order entity<br/>```{"id": 200016, "orderDate": "2026-03-14T17:42:24", "deliveryDate": null, "status": "CANCELLED", "shippingAddress": { "street": "Aurajoentie 5", "city": "Turku", "postalCode": "54321", "country": "Finland"}, "totalPrice": 1773.50, "items": [{"quantity": 2, "product": {"name": "Happy Pack 382", "description": "Score million throw thing instead ball line think.", "price": 546.08, "category": {"name": "Urheilu & vapaa-aika"}}}, {"quantity": 1, "product": {"name": "Quantum Widget 457", "description": "Ask decide need next very capital.", "price": 681.34, "category": {"name": "Vaatteet"}}}]}```<br/>or an error message<br/>```{"error": "Order already cancelled", "message": "This order cannot be cancelled again."}```,<br/> ```{"error", "Order already shipped", "message", "Shipped orders cannot be cancelled."}``` etc. |