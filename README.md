# service-hello-world

Spring Boot service providing a greeting API backed by JPA persistence.

## Stack

| Component | Version / Choice |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.9 |
| Web | Spring MVC (`spring-boot-starter-web`) |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 (in-memory) |
| Schema migrations | Liquibase (XML changelogs) |
| Boilerplate reduction | Lombok (compile-time only) |
| Build | Maven (wrapper included) |

## Build and test

```bash
./mvnw clean verify
```

## Run

```bash
./mvnw spring-boot:run
# or
java -jar target/service-hello-world-0.0.1-SNAPSHOT.jar
```

The service listens on port `8080`. Override with `--server.port=<port>`.

## API

### `POST /greeting`

Creates a greeting for the supplied name, persists it, and returns the stored record.

Request body:

```json
{ "name": "World" }
```

Response `201 Created`:

```json
{ "id": 1, "name": "World", "message": "Hello, World!", "createdAt": "2026-01-01T00:00:00Z" }
```

A blank or missing `name` (or one longer than 100 characters) returns `400 Bad Request`.

### `GET /greetings`

Returns stored greetings as a page, using the standard Spring pageable request
parameters:

| Parameter | Default | Notes |
| --- | --- | --- |
| `page` | `0` | Zero-based page index. |
| `size` | `20` | Page size, capped at `100`. |
| `sort` | `createdAt,desc` then `id,desc` | Repeatable, `property,(asc|desc)`. |

Example: `GET /greetings?page=1&size=2&sort=createdAt,asc`

Response `200 OK`:

```json
{
  "content": [
    { "id": 1, "name": "World", "message": "Hello, World!", "createdAt": "2026-01-01T00:00:00Z" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

The envelope is an explicit DTO (`PageResponse`), so the JSON shape stays stable
regardless of Spring Data's internal `Page` serialization. Requesting a page past
the last one returns `200 OK` with an empty `content` array.

## Database

H2 runs in memory at `jdbc:h2:mem:helloworld` (user `sa`, empty password) and is
recreated on every start. The H2 console is available at
<http://localhost:8080/h2-console>.

`spring.jpa.hibernate.ddl-auto` is set to `validate`: Liquibase owns the schema,
Hibernate only verifies that entity mappings match it.

## Liquibase changelogs

- Master changelog: `src/main/resources/db/changelog/db.changelog-master.xml`
  (referenced as `classpath:db/changelog/db.changelog-master.xml`).
- Individual migrations go in `src/main/resources/db/changelog/changes/` as XML
  files; the master changelog picks them up automatically in alphabetical order,
  so prefix new files with an incrementing number
  (e.g. `001-create-greeting-table.xml`).

## Lombok

Lombok is declared `optional` and wired into the compiler's annotation processor
path, so it is used at compile time only and is excluded from the executable jar.
