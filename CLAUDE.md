# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Camunda-based workflow demonstration application with a Spring Boot backend and React (Vite + TypeScript) frontend. The system demonstrates BPM workflow orchestration with approval flows, manual tasks, and real-time process monitoring.

**Technology Stack:**
- Backend: Spring Boot 3.2.5 + Camunda 7.21 + H2 in-memory database
- Frontend: React 18 + Vite 5 + TypeScript 5
- Logging: Structured audit logging with Logstash encoder

## Development Commands

### Backend
```bash
cd backend
mvn spring-boot:run              # Run backend server (http://localhost:8080)
mvn test                         # Run integration tests
mvn clean package                # Build JAR artifact
```

### Frontend
```bash
cd frontend
npm install                      # Install dependencies (first time)
npm run dev                      # Start dev server (http://localhost:5173)
npm run build                    # Build production bundle (requires tsc to pass)
npm run lint                     # Run ESLint with zero warnings policy
```

## Architecture Patterns

### Backend Architecture

**Identity System:**
The application uses a mock identity system based on HTTP headers rather than traditional authentication. The `IdentityContextFilter` extracts `X-User-Id` and `X-User-Roles` headers from each request and stores them in `IdentityContextHolder` (a ThreadLocal context). This allows the frontend persona switcher to simulate different users without actual authentication.

**Service Layer:**
- `ProcessService` - Handles workflow instance creation and startup
- `ProcessStatusService` - Queries process state, variables, and history
- `TaskApplicationService` - Manages task completion, approval, and rejection operations

**Audit Logging:**
The `WorkflowAuditLogger` uses Logstash structured logging to emit JSON logs for all workflow events. The `WorkflowEventListener` is registered as both an ExecutionListener and TaskListener on the Camunda process definition and delegates to the audit logger. Logs include process instance IDs, task IDs, timestamps, and operation results.

**Process Model:**
The BPMN workflow is located at `backend/src/main/resources/processes/workflow-demo.bpmn`. It's auto-deployed on startup via Camunda's process deployment mechanism.

### Frontend Architecture

**API Layer:**
All backend communication flows through `src/api.ts`, which provides a typed facade (`Api` object) over fetch calls. The `request()` helper automatically injects identity headers from `IdentitySession`.

**State Management:**
- Context APIs: `AuthContext` manages current identity/persona, `I18nContext` handles translations
- Component-level state: The main `App` component maintains process status and activity log state, passing callbacks down to child components

**Component Structure:**
- `StartProcessForm` - Initiates new workflow instances
- `TaskBoard` - Displays and allows interaction with approval/manual tasks
- `ProcessStatusPanel` - Shows process state, variables, and history
- `ActivityLog` - Displays chronological operation log
- `IdentitySwitcher` - Persona/role switcher (Initiator/Approver/Executor)

**Persona System:**
The frontend uses `IdentitySession` (localStorage-backed) to persist the selected persona. When switching personas, the UI sets different `X-User-Id` and `X-User-Roles` headers to simulate different users interacting with the workflow.

**Vite Configuration:**
The dev server proxies `/api` requests to `http://localhost:8080` (configurable via `VITE_API_PROXY_TARGET` env var).

## Key Implementation Details

**Running Tests:**
Backend tests use `@SpringBootTest` and `camunda-bpm-assert` for workflow assertions. The H2 database is recreated for each test run.

**Building Both Layers:**
To prepare a full production deployment, build the frontend first (`cd frontend && npm run build`), then copy `frontend/dist` contents into `backend/src/main/resources/static` before running `mvn clean package`.

**Database Access:**
The H2 console is enabled at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:camunda`, username: `sa`, no password). Useful for inspecting Camunda tables during development.

**Camunda Web UI:**
Camunda's built-in web applications (Cockpit, Tasklist, Admin) are available at `http://localhost:8080` (login: `demo` / `demo`).

**Internationalization:**
The frontend uses a custom i18n system in `src/i18n/`. Translation keys are defined in `translations.ts` and accessed via the `useI18n()` hook.
