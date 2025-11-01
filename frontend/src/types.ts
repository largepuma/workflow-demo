export type ProcessState = "APPROVAL_PENDING" | "MANUAL_PENDING" | "COMPLETED" | "REJECTED" | null;

export interface TaskSummary {
  taskId: string;
  taskName: string;
  processInstanceId: string;
  createdAt?: string | null;
}

export interface StartProcessResponse {
  processInstanceId: string;
  state: ProcessState;
  currentTask?: TaskSummary | null;
}

export interface TaskListItem {
  taskId: string;
  taskName: string;
  processInstanceId: string;
  createdAt?: string;
  payload?: Record<string, unknown>;
}

export interface HistoryEntry {
  activityId: string;
  activityName?: string | null;
  activityType?: string | null;
  assignee?: string | null;
  result?: string | null;
  startTime?: string | null;
  endTime?: string | null;
}

export interface ProcessStatusResponse {
  processInstanceId: string;
  state: ProcessState;
  currentTask?: TaskSummary | null;
  history: HistoryEntry[];
  variables: Record<string, unknown>;
}

export interface StartProcessRequest {
  initiator: string;
  approverId: string;
  executorId: string;
  payload: Record<string, unknown>;
}

export interface TaskDecisionRequest {
  userId?: string;
  comment?: string;
  reason?: string;
}

export interface TaskActionRequest {
  userId?: string;
  comment?: string;
}

export interface ActivityLogItem {
  id: string;
  timestamp: string;
  message: string;
}
