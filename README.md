# Smart Energy Management System

This repository contains the Smart Energy Management System source code.

## Project structure
- `backend/` — Java 17, Spring Boot 3.2.5 REST API and web UI (HTML/CSS/JavaScript in `src/main/resources/static/`).
- `android-app/` — Android application source (Gradle project).
- `smart_energy.sql` — MySQL SQL dump supplied with the project.

## Backend local setup
1. Install JDK 17 or later, Maven (or use a Maven wrapper if added), and MySQL.
2. Create/import the `smart_energy` database using `smart_energy.sql` if this dump is the intended schema/data.
3. Configure database URL, username, and password outside version control (environment-specific configuration).
4. From `backend/`, run `mvn spring-boot:run`.
5. Open `http://localhost:8081/`.

## Deployment note
The web UI is currently packaged inside the Spring Boot backend at `backend/src/main/resources/static/`.
The easiest deployment is to host the Spring Boot application and its static UI together on a Java-capable host, with a separately hosted MySQL database. Vercel does not run a persistent Spring Boot service. To use Vercel, deploy the static UI separately and configure its API requests to point to the deployed backend URL.

## Security
Do not commit database passwords, API keys, access tokens, or production secrets. Configure production database credentials using your hosting provider's environment variables.
