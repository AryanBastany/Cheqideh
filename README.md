# Cheqideh

Minimal Spring Boot service for issuing and presenting cheques. Implements SAYAD mock calls, non-bearer/unconditional checks, six-month presentation window, bounce logic (account blocking after 3 bounces in 12 months), H2 persistence, and JWT-protected endpoints (role `TELLER`).

---

## Requirements

* Java 17+ (or the project's target Java)
* Maven 3.6+

---

## Build

```bash
# clone repo
git clone https://github.com/AryanBastany/Cheqideh
cd Cheqideh

# build
mvn clean package -DskipTests
```

To run tests (unit + integration):

```bash
mvn test
```

---

## Run (dev)

The project ships with a mock SAYAD server running on port `8081`, and the application runs on `8080` by default.

Run with Maven:

```bash
mvn spring-boot:run
```

Spring Boot properties are configured in `src/main/resources/ or src/test/resources/  application.yml`.

### H2 Console

H2 console is exposed at `/h2` (configured in `application.yml`). Default JDBC URL (in-memory) is `jdbc:h2:mem:testdb`.

Open: `http://localhost:8080/h2`

---

## Security (JWT)

Endpoints require `ROLE_TELLER`. You must first authenticate and obtain a token from the login endpoint.

### Authentication

Send a request to:

```
POST /api/auth/login
```

With JSON body:

```json
{
  "username": "teller1",
  "password": "password"
}
```

If credentials are valid, the response will contain a JWT token:

```json
{
  "token": "<JWT_TOKEN>"
}
```

Use this token in all subsequent requests:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## Tests

* Unit tests of ChequeServiceTest covers `ChequeValidator`.
* Unit tests of SayadClientTest covers the SAYAD client stub.
* Integration test (tests in the CheqidehIntegrationTest) runs with H2 in-memory and covers happy path and bounce path, including account blocking after 3 bounces in 12 months.

Run them with:

```bash
mvn test
```
