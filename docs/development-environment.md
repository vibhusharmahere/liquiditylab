# LiquidityLab — Development Environment

## Machine

- Platform: Apple Silicon Mac
- CPU architecture: `arm64`
- macOS version: 26.3

## Verified tools

| Tool | Version | Status |
|---|---|---|
| Git | 2.52.0 | Verified |
| Java JDK | Eclipse Temurin 21.0.12.1 LTS | Verified |
| Java compiler | 21.0.12.1 | Verified |
| Node.js | 24.14.1 | Verified |
| npm | 11.11.0 | Verified |
| Homebrew | 7.0.9 | Verified |
| Docker CLI | 29.8.2 | Verified |
| Docker Engine | 29.8.2 | Running |
| Docker Compose | 5.6.0 | Verified |

## Planned application tooling

- Backend: Java 21 and Spring Boot
- Build: Maven Wrapper, generated with the backend project
- Frontend: React and TypeScript
- Database: PostgreSQL running in Docker
- Tests: JUnit and Spring integration tests
- API documentation: OpenAPI / Swagger

Exact Spring Boot and dependency versions will be selected when the project skeleton is generated.

## Initial setup checks

```bash
git --version
java -version
javac -version
node -v
npm -v
brew --version
docker --version
docker compose version
docker info
```

## Repository workflow

- `main`: stable branch
- `feature/project-foundation`: current development branch
- Keep changes small and commit working increments.
- Use `.gitignore` to exclude generated files and local secrets.

## Notes

- Java, Node.js, npm, Git, Homebrew and Docker have been verified in the local terminal.
- Docker Engine is running.
- Spring Boot, Maven Wrapper, React and PostgreSQL application configuration are not yet implemented.
- LiquidityLab uses fictional data and is not intended for production banking.
