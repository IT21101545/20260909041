# Government Training Management System — Practical Test

## Task 1 — Duplicate Nominations

Prevent the same officer from being nominated more than once for the same training programme.

### Rule

`OFFICER + TRAINING = UNIQUE`

The department is intentionally NOT part of the uniqueness rule. If a duplicate is attempted, the
error message names the department that already holds the nomination.

## Task 2 — Limited Training Capacity

Training programmes have a maximum number of seats (`Training.maxParticipants`), but can receive
more valid nominations than seats available.

### Rule

- Nominations are processed strictly in the order they arrive (`nominatedAt` timestamp).
- The first N valid nominations (N = capacity) are `CONFIRMED`.
- Any nomination after that is `WAITLISTED`.
- Cancelling a `CONFIRMED` nomination (`POST /api/nominations/{id}/cancel`) automatically promotes
  the longest-waiting `WAITLISTED` nomination for that same training to `CONFIRMED`.
- Cancelling a `WAITLISTED` nomination simply removes them from the queue.

## Technology

- Java 17
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- Microsoft SQL Server (runtime) / H2 (test only)
- HTML / CSS / JavaScript

## Database setup (SQL Server Express)

1. **Create the database.** Unlike MySQL, SQL Server's JDBC driver can't auto-create a database.
   In SSMS, right-click **Databases** → **New Database...** → name it `trainingdb` → OK.
   (Tables are still created automatically by `spring.jpa.hibernate.ddl-auto=update`.)
2. **Enable SQL Server authentication (mixed mode).** Already done in your setup — confirmed via
   Server Properties → Security.
3. **Enable TCP/IP and SQL Server Browser**, required to connect to a _named instance_
   (`HamdhanAnsar\SQLEXPRESS`) from Java:
   - Open **SQL Server Configuration Manager**.
   - Under **SQL Server Network Configuration → Protocols for SQLEXPRESS**, right-click **TCP/IP** → Enable.
   - Go to **SQL Server Services**, find **SQL Server Browser**, right-click → Start (and set it to
     Automatic startup if you don't want to repeat this every reboot).
   - Restart the **SQL Server (SQLEXPRESS)** service after enabling TCP/IP.
4. **Set your real `sa` password** in `application.properties` (see below) — the one you just set
   in SSMS's Login Properties dialog.

```properties
spring.datasource.url=jdbc:sqlserver://HamdhanAnsar\\SQLEXPRESS;databaseName=trainingdb;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=your_sa_password
```

If your machine name is different from `HamdhanAnsar`, or you're not using the `SQLEXPRESS`
instance name, adjust the URL to match (check the "Server" field in SSMS's connection dialog).

## Run

1. Open this folder in IntelliJ IDEA or VS Code.
2. Make sure Java 17+, Maven, and SQL Server Express are installed and the SQL Server (SQLEXPRESS)
   and SQL Server Browser services are running.
3. Set your `sa` password in `application.properties` (see above).
4. Run:

```bash
mvn spring-boot:run
```

5. Open:

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
POST `/api/nominations/{id}/cancel`

Example POST body (`/api/nominations`):

```json
{
  "officerId": 1,
  "trainingId": 1,
  "departmentId": 2
}
```

Duplicate response (Task 1):

HTTP `409 Conflict`

```json
{
  "message": "This officer has already been nominated for this training programme by Finance Division. Duplicate nomination from another department is not allowed."
}
```

Cancel response (Task 2) — HTTP `200 OK`, returns the cancelled nomination. If it was `CONFIRMED`,
the longest-waiting `WAITLISTED` nomination for that training is promoted to `CONFIRMED` server-side
in the same call.

## Database protection

The Nomination entity contains:

```java
@UniqueConstraint(
    name = "uk_officer_training",
    columnNames = {"officer_id", "training_id"}
)
```

This protects the database from duplicate nominations even if two requests arrive at nearly the same time.

## Testing Task 2 manually

1. Set a training's `maxParticipants` low in `DataInitializer` (or use MySQL Workbench to edit it)
   to make it easy to test, e.g. 2.
2. Submit nominations for 3 different officers to that training.
3. The first 2 should show status `CONFIRMED`; the 3rd should show `WAITLISTED`.
4. Click "Cancel" on one of the `CONFIRMED` rows.
5. The `WAITLISTED` nomination should automatically flip to `CONFIRMED`.

## Task 3 — Training Eligibility

An officer must satisfy the eligibility rules configured for a training programme before a
nomination is created. Rules are stored in the `eligibility_rules` table, so adding or changing a
rule does not require changing the nomination workflow.

Supported rule types are:

- `ALLOWED_DEPARTMENT` — the officer's department must match one of the configured values.
- `ALLOWED_DESIGNATION` — the officer's designation must match one of the configured values.
- `MIN_YEARS_OF_SERVICE` — the officer must have at least the configured number of years.

Eligibility is checked before the nomination is assigned `CONFIRMED` or `WAITLISTED`. An ineligible
request returns HTTP `400 Bad Request` with the message `Officer is not eligible for this training
programme.`
