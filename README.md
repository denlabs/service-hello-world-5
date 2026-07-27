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
