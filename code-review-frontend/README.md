# Code Review Assistant

A full‑stack tool that analyzes Java code for errors, warnings, optimizations, and security issues. It provides actionable suggestions, a visual UI with line‑by‑line highlights, a rules engine with configurable checks, a dashboard of metrics, and exportable reports.


## Key Features

- Code submission
  - Paste Java code in the editor
  - Upload a `.java` source file
- Static analysis
  - Syntax and type errors (via in‑memory Java compiler diagnostics)
  - Code style issues (long lines, unused imports, empty catch blocks)
  - Performance smells (nested loops, string concatenation in loops, console logging)
  - Security issues (SQL string concatenation, hardcoded secrets)
  - “Code smell” heuristics (unmatched braces, unreachable code after return)
- Suggestions and explanations
  - Each finding includes an explanation and a concrete suggestion
- Issue categorization
  - Errors, Warnings, Optimizations, Security
  - Severity levels: High / Medium / Low
- Visual highlighting
  - Line‑by‑line coloring in the editor
  - Filterable issues panel with details and severity badges
- Summary report
  - Aggregated counts by category
  - Export to HTML or TXT
- Dashboard
  - Total reviews, total issues, distribution by category
- Rule configuration
  - Enable/disable specific analysis rules via REST API and UI


## Tech Stack

- Frontend: React (Create React App), Axios
- Backend: Spring Boot (Java 17), Spring Web MVC, Spring Data JPA (H2 in‑memory)
- Build tools: Maven Wrapper (`mvnw`), npm


## Repository Structure

- `code-review-frontend/` — React UI
  - Talks to `http://localhost:8080/api`
  - Provides editor, uploads, dashboard, rule toggles, report download
- `code-review-backend/` — Spring Boot API
  - In‑memory H2 database, no external setup needed
  - Pluggable rules engine + compiler diagnostics
  - Exposes analyze, reviews, dashboard, rules, and export endpoints


## Prerequisites

- Java 17+ (JDK)
- Node.js 18+ and npm 9+

Verify versions:

```bash
java -version
node -v && npm -v
```


## Quick Start

1) Start the backend (port 8080)

```bash
cd "code-review-backend"
chmod +x mvnw   # first time only

# If your default Java is not 17, use this one-liner for the run:
JAVA_HOME="$("/usr/libexec/java_home" -v 17)" PATH="$JAVA_HOME/bin:$PATH" ./mvnw spring-boot:run
```

Backends starts at `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:codereviewdb`, user: `sa`, no password).

2) Start the frontend (port 3000)

```bash
cd "../code-review-frontend"
npm ci   # or: npm install
npm start
```

Open `http://localhost:3000`.


## Using the App

- Paste code or upload a `.java` file, then click “Analyze Code”.
- The editor highlights lines with issues; the Issues panel shows details.
- The Dashboard summarizes totals across analyses.
- The “Analysis Rules” section lets you toggle rules and save.
- After an analysis, use “Download Report (HTML/TXT)” to export the findings.


## Core API Endpoints

Base URL: `http://localhost:8080/api`

- Analyze inline code

```bash
curl -X POST "http://localhost:8080/api/analyze" \
  -H "Content-Type: application/json" \
  -d '{"code":"public class A { }","filename":"A.java"}'
```

- Analyze uploaded file

```bash
curl -X POST "http://localhost:8080/api/analyze/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/YourFile.java"
```

- List all reviews

```bash
curl "http://localhost:8080/api/reviews"
```

- Get a single review

```bash
curl "http://localhost:8080/api/reviews/1"
```

- Export a review (HTML or TXT)

```bash
curl -OJ "http://localhost:8080/api/reviews/1/export?format=html"
curl -OJ "http://localhost:8080/api/reviews/1/export?format=txt"
```

- Dashboard summary

```bash
curl "http://localhost:8080/api/dashboard/summary"
```

- Rule configuration
  - Get rule states

```bash
curl "http://localhost:8080/api/rules"
```

  - Update rule states (example)

```bash
curl -X PUT "http://localhost:8080/api/rules" \
  -H "Content-Type: application/json" \
  -d '{"todo":false,"long-line":true,"hardcoded-secret":true}'
```


## Built‑in Rules

Rules can be toggled via `/api/rules` and the UI:

- `todo` — Flags “TODO” comments (Warning, Low)
- `console-logging` — `System.out.println` usage (Optimization, Medium)
- `long-line` — Lines over 120 chars (Optimization, Low)
- `unmatched-braces` — Mismatched `{`/`}` (Error, High)
- `empty-catch` — Empty catch blocks (Warning, Medium)
- `hardcoded-secret` — Password/API key patterns (Security, High)
- `nested-loop` — Nested loops heuristic (Optimization, Medium)
- `string-concat-in-loop` — String concat in loops (Optimization, Low)
- `unreachable-after-return` — Unreachable statements (Warning, Medium)
- `unused-import` — Unused imports (Warning, Low)
- Compiler diagnostics — Real `javac` errors surfaced as findings (Error, High)


## Data & Storage

- H2 in‑memory database; data persists for the app lifetime
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:codereviewdb`
  - User: `sa`, Password: (empty)


## Testing

- Backend:

```bash
cd "code-review-backend"
JAVA_HOME="$("/usr/libexec/java_home" -v 17)" PATH="$JAVA_HOME/bin:$PATH" ./mvnw test
```

- Frontend:

```bash
cd "code-review-frontend"
npm test
```


## Troubleshooting

- “class file has wrong version 61.0, should be 52.0”
  - Your Java is too old. Run with Java 17:
    ```bash
    JAVA_HOME="$("/usr/libexec/java_home" -v 17)" PATH="$JAVA_HOME/bin:$PATH" ./mvnw spring-boot:run
    ```
- `./mvnw: permission denied`
  - `chmod +x mvnw`
- `node: command not found`
  - Install Node (e.g., `brew install node@20`) and ensure it’s in your PATH
- Port conflicts
  - Change backend port in `code-review-backend/src/main/resources/application.properties` via `server.port=...`


## Extensibility

- Add new analysis rules
  - Implement `AnalysisRule` and register it in `RuleRegistry`
  - Rules are composable and can be toggled at runtime
- Integrations (future)
  - Git providers (GitHub/GitLab/Bitbucket) for PR/branch analysis
  - CI/CD and IDE extensions (VS Code, IntelliJ)
  - Multi‑language support by adding language‑specific analyzers


## Security & Privacy

- Local deployment by default; uploads are stored in memory/H2 only
- Rules include checks for hardcoded secrets and unsafe SQL concatenation
- Consider adding HTTPS, authentication/authorization, and persistent DB for production


## License

For demonstration purposes; choose a license appropriate for your use case.


