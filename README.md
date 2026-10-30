# TinyMX — Fresher-friendly URL Shortener

A simple URL shortener built with Java 17, Spring Boot MVC, Thymeleaf, Spring Data JPA, MySQL and optional Gemini AI.

## Included
- MVC: `HomeController` returns a Thymeleaf page; `ShortUrl` is the JPA model.
- Repository pattern: `ShortUrlRepository` handles database access.
- Service layer: `ShortUrlService` keeps URL validation and short-code logic outside the controller.
- Constructor dependency injection.
- Random short codes, optional custom slugs, redirects, and a recent-links list.
- Optional Gemini endpoint for a suggested title/description.
- Dockerfile and Docker Compose for a repeatable local deployment.

No DTO classes are used; the simple AI endpoint accepts a `Map` so the example stays small. The form itself uses normal MVC form fields.

## Run with Docker (recommended)
1. Install Docker Desktop or Docker Engine with the Compose plugin.
2. Copy `.env.example` to `.env`.
3. Change `DB_PASSWORD` and `MYSQL_ROOT_PASSWORD` to strong, different values.
4. Optionally set `GEMINI_API_KEY` and `GEMINI_MODEL` in `.env`. URL shortening works without Gemini.
5. From this folder run:

   ```bash
   docker compose up --build -d
   ```
6. Open <http://localhost:8080>.
7. View logs with `docker compose logs -f app`; stop with `docker compose down`.

The database is stored in a named Docker volume. To delete the database as well, run `docker compose down -v` (this permanently removes its data).

## Run without Docker
- Install Java 17+, Maven 3.9+, and MySQL 8.
- Create a database named `urlshort`.
- Set environment variables `DB_HOST=localhost`, `DB_PORT=3306`, `DB_NAME=urlshort`, `DB_USER`, and `DB_PASSWORD`.
- Optionally set `GEMINI_API_KEY` and `GEMINI_MODEL`.
- Run `mvn spring-boot:run` and visit <http://localhost:8080>.

## Endpoints
- `GET /` — home page.
- `POST /shorten` — form submission; redirects back to the home page.
- `GET /{code}` — redirects to the original URL.
- `POST /api/ai/link-insight` — JSON body example: `{"url":"https://example.com","context":"class notes"}`.

## Deployment notes
- For a real public deployment, set `APP_BASE_URL` to the public HTTPS origin, such as `https://your-domain.example`.
- Keep `.env` out of Git. Do not commit API keys or production passwords.
- Use HTTPS through your hosting platform or reverse proxy.
- `ddl-auto=update` is convenient for a learning project. For production, use Flyway/Liquibase and `ddl-auto=validate`.
- The recent-links section is intentionally a small demo and is not multi-user/private. Add authentication and rate limiting before exposing it as a public service.
- A custom slug is checked for uniqueness before saving; for high-concurrency production traffic, also handle the database unique-constraint race explicitly.

## Design patterns / structure
```text
Browser
  -> HomeController (MVC Controller)
      -> ShortUrlService (Service Layer)
          -> ShortUrlRepository (Repository Pattern)
              -> MySQL

Browser -> /{code} -> HomeController -> Service -> Repository -> redirect
```

Constructor injection is used so Spring supplies the repository/service dependencies. There is no need to add more patterns just for the sake of it; MVC, Repository, Service Layer and Dependency Injection are enough for this fresher-level project.
