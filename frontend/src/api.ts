import type {
  ProcessStatusResponse,
  StartProcessRequest,
  StartProcessResponse,
  TaskActionRequest,
  TaskDecisionRequest,
  TaskListItem
} from "./types";
import { IdentitySession } from "./session";

const baseUrl = import.meta.env.VITE_API_BASE ?? "";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers ?? {});
  if (!headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const identity = IdentitySession.get();
  if (identity?.userId) {
    headers.set("X-User-Id", identity.userId);
  }
  if (identity?.roles?.length) {
    headers.set("X-User-Roles", identity.roles.join(","));
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...init,
    headers
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = data?.error ?? response.statusText;
    throw new Error(message);
  }

  return data as T;
}

export const Api = {
  startProcess: (payload: StartProcessRequest) =>
    request<StartProcessResponse>("/api/process/start", {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  getProcessStatus: (processInstanceId: string) =>
    request<ProcessStatusResponse>(`/api/process/${processInstanceId}`),

  findTasks: (role: string, userId: string) =>
    request<TaskListItem[]>(`/api/tasks?role=${encodeURIComponent(role)}&userId=${encodeURIComponent(userId)}`),

  approveTask: (taskId: string, payload: TaskDecisionRequest) =>
    request(`/api/tasks/${taskId}/approve`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  rejectTask: (taskId: string, payload: TaskDecisionRequest) =>
    request(`/api/tasks/${taskId}/reject`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  completeTask: (taskId: string, payload: TaskActionRequest) =>
    request(`/api/tasks/${taskId}/complete`, {
      method: "POST",
      body: JSON.stringify(payload)
    })
};
