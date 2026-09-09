# Government Training Management System — Practical Test Task 1

## Task 1
Prevent the same officer from being nominated more than once for the same training programme.

### Rule
`OFFICER + TRAINING = UNIQUE`

The department is intentionally NOT part of the uniqueness rule.

## Technology
- Java 17
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- H2 Database
- HTML / CSS / JavaScript

## Run
1. Open this folder in IntelliJ IDEA or VS Code.
2. Make sure Java 17+ and Maven are installed.
3. Run:

```bash
mvn spring-boot:run
```

4. Open:

`http://localhost:8080`

## Test Task 1
1. Select John Perera.
2. Select Finance Division.
3. Select Java Programming.
4. Click Submit Nomination.
5. It should succeed.
6. Select John Perera again.
7. Select Administration Division.
8. Select Java Programming again.
9. Click Submit Nomination.
10. The system should return a duplicate error.

Expected message:

"This officer has already been nominated for this training programme."

## API endpoints
GET `/api/officers`
GET `/api/departments`
GET `/api/trainings`
GET `/api/nominations`

POST `/api/nominations`

Example POST body:

```json
{
  "officerId": 1,
  "trainingId": 1,
  "departmentId": 2
}
```

Duplicate response:

HTTP `409 Conflict`

```json
{
  "message": "This officer has already been nominated for this training programme."
}
```

## Database protection
The Nomination entity contains:

```java
@UniqueConstraint(
    name = "uk_officer_training",
    columnNames = {"officer_id", "training_id"}
)
```

This protects the database from duplicate nominations even if two requests arrive at nearly the same time.

## H2 Console
URL:
`http://localhost:8080/h2-console`

JDBC URL:
`jdbc:h2:file:./data/trainingdb`

Username:
`sa`

Password:
leave empty

## Repository Tasks
- task 1
- task2
- task 3
