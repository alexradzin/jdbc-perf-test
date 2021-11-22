# jdbc-perf-test

This is a simple project created to measure and compare various strategies of the database bulk write operations.
The project does not have "production" code. It contains 2 unit tests that do not perform any validations but rather
implement bulk update scenarious and write the measurements results to the STDOUT. The data can be later used for analysis.

The code itself is DB independent. The DB specific customizations use properties files named as 

```
dbname.properties
``` 

Right now 2 databases are supported:
 * MySQL
 * PostgreSQL
 
Connection to database cannot be done without JDBC URL and the credentials that can be configured either by editing
file `mysql.properties` and `postgresql.properties` or by overriding these properties using command line. 

The relevant properties are: 
 * `jdbc.url` that defines the JDBC URL
 * `jdbc.properties` that defines the connection properties, typically user and password
 
 Passing the properties from command line requires prefix equal to the database name, e.g. `mysql` or `postgresql`:
 
 ```
./gradlew test \
'-Dmysql.jdbc.url=jdbc:mysql://localhost:3306/public?rewriteBatchedStatements=true&allowLoadLocalInfile=true' \ 
'-Dmysql.jdbc.properties=user=USER,password=PASSWORD' \
'-Dpostgresql.jdbc.url=jdbc:postgresql://localhost:5432/test_db' \  
'-Dpostgresql.jdbc.properties=user=USER,password=PASSWORD' 
```
    
 