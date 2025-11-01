# Workflow Demo

Camunda workflow demo project composed of:

- **backend/** – Spring Boot with Camunda 7.21 and an embedded H2 database providing BPM orchestration, REST APIs, and structured audit logging.
- **frontend/** – React (Vite + TypeScript) single-page application for starting workflows, handling approval/manual tasks, and inspecting process status.

## Tooling Requirements

| Component | Version |
|-----------|---------|
| Java      | 17 (or newer compatible release) |
| Maven     | 3.9+ |
| Node.js   | 18+ |
| npm       | 9+ |

## Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`, auto-deploys the Camunda BPMN model, and exposes REST endpoints such as `/api/process` and `/api/tasks`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs at `http://localhost:5173` and proxies `/api` requests to `http://localhost:8080`. Override the target via `VITE_API_PROXY_TARGET` if needed. Build static assets with `npm run build`.

The UI includes a lightweight persona switcher (top right). Choosing Initiator/Approver/Executor updates outgoing requests with the appropriate `X-User-Id` and `X-User-Roles` headers.

## Testing

Backend integration tests:

```bash
cd backend
mvn test
```

Frontend linting and production build:

```bash
cd frontend
npm run lint
npm run build
```

## Directory Structure

```
backend/   Spring Boot + Camunda service
frontend/  React + Vite command center
```
