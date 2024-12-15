# crypto-finance-app

## Description
Your assignment is to implement a simple Spring Java application, when given a collection (called wallet) of crypto assets with their positions (symbol, quantity and price) for your clients.
You must retrieve from time to time (frequency must be set as a mutable argument) and concurrently (for each asset), their latest prices from the Coincap API (https://docs.coincap.io/) and update them in a database.
You should provide a REST API to return the updated total financial value of the wallet at any given time (current and past). 
Also, you must save the results for the wallet in a SQL database.

### Input
| Symbol | Quantity | Price       |
|--------|----------|-------------|
| BTC    | 0.12345  | 37870.5058  |
| ETH    | 4.89532  | 2004.9774   |

### Output
Database with correct data filled.
REST API returning a JSON with:
```total={X},best_asset={X},best_performance={X},worst_asset={X},worst_performance= {X}```

## TECHNICAL BRIEFING

Required environment, tools and languages:
- Java 17+ with Spring 3.
- Build the project with Maven or Gradle.
- Write your code in English.
- Feel free to use any additional Java libraries you want.

### Mandatory Technical Requirements:
Write tests, it’s up to you what to test and how.
You are free to use any library for database, the only requirement is that is must be SQL.
Retrieve the prices simultaneously by groups of 3 assets concurrently, i.e., at any  point, at most 3 threads will be active processing tasks – but never single threaded (unless there is only one asset in the wallet). For example, if you process a wallet with more than 3 assets, and each API call takes 10s, your code should log something like:
```
Now is 10:00:00
Submitted request ASSET_A at 10:00:01
Submitted request ASSET_B at 10:00:01
Submitted request ASSET_C at 10:00:01
(program hangs, waiting for some of the previous requests to finish)
Submitted request ASSET_D at 10:00:11
```

#### Where:
- total: total financial value in USD of the entire wallet
- best_asset: which asset had the best performance (value increase) the wallet compared to the latest price retrieved from the API
- best_performance: percentage of the performance of the best_asset
- worst_asset: which asset had the worst performance (value decrease) from the wallet compared to the latest price retrieved from the API
- worst_performance: percentage of the performance of the worst_asset
- Values rounded to 2 decimal places, HALF_UP

## Questions
- Question #1: frequency is a property relative to the service or to the endpoint?
- Question #2: Should the wallet have its own id and allow to search using that id?
- Question #3: When saving the wallet details, should the response include the calculation? Or only when the user queries the wallet to fetch the info?

## Setup/Run
### Run manually
1. mvn clean package
2. start docker container: `docker-compose up -d db`, it will start a container of postgres running on port 5432
3. run jar: `java -jar crypto-finance-app/crypto-finance-service/target/crypto-finance-service-0.0.1-SNAPSHOT.jar`
4. server port exposed will be 8080 by default (can be changed on application.properties file)

### Start using docker-compose
- Run the docker-compose with `docker-compose up -d`
- container `crypto-finance-app` will contain the spring boot app running on port 8080
- container `crypto-finance-app` will be activated using the `docker` profile
- container `db` will run Postgres, will be accessible by the `crypto-finance-app`

## Configurations
- Postman collection will be available on the folder `postman`
- Docker environment will have the properties located on `crypto-finance-service/resources/application-docker.properties`

