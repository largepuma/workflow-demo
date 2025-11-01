import { useEffect, useMemo, useState } from "react";
import { Api } from "../api";
import type { ActivityLogItem, TaskListItem } from "../types";
import { formatDateTime } from "../utils/format";
import StatusTag from "./StatusTag";
import { useAuth } from "../context/AuthContext";
import { useI18n } from "../i18n/I18nContext";
import "./TaskBoard.css";

export interface TaskBoardProps {
  role: "approver" | "executor";
  title: string;
  onStatusRefresh?: (processInstanceId: string) => Promise<void>;
  pushLog: (entry: ActivityLogItem) => void;
}

export function TaskBoard({ role, title, onStatusRefresh, pushLog }: TaskBoardProps) {
  const { identity } = useAuth();
  const { t } = useI18n();
  const [tasks, setTasks] = useState<TaskListItem[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [messageTone, setMessageTone] = useState<"success" | "error" | null>(null);
  const [loading, setLoading] = useState(false);

  const hasRole = useMemo(() => identity.roles.some((item) => item.toLowerCase() === role), [identity, role]);

  const loadTasks = async () => {
    if (!hasRole) {
      setTasks([]);
      setMessage(t("tasks.permission.denied"));
      setMessageTone("error");
      return;
    }
    setLoading(true);
    try {
      const results = await Api.findTasks(role, identity.userId);
      setTasks(results);
      if (results.length === 0) {
        setMessage(t("tasks.empty"));
        setMessageTone("success");
      } else {
        setMessage(null);
        setMessageTone(null);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Unknown error";
      setMessage(t("tasks.load.error", { message: errorMessage }));
      setMessageTone("error");
      pushLog({
        id: crypto.randomUUID(),
        timestamp: new Date().toLocaleTimeString(),
        message: t("tasks.load.error", { message: errorMessage })
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [identity, role]);

  const action = async (task: TaskListItem, kind: "approve" | "reject" | "complete") => {
    if (!hasRole) {
      setMessage(t("tasks.permission.denied"));
      setMessageTone("error");
      return;
    }
    try {
      if (kind === "approve") {
        const comment = window.prompt(t("tasks.approve.prompt"), t("tasks.prompt.approvalDefault"));
        await Api.approveTask(task.taskId, { comment: comment ?? "" });
        pushLog({
          id: crypto.randomUUID(),
          timestamp: new Date().toLocaleTimeString(),
          message: t("tasks.approve.log", { taskId: task.taskId, userId: identity.userId })
        });
      } else if (kind === "reject") {
        const reason = window.prompt(t("tasks.reject.prompt"), t("tasks.prompt.rejectDefault"));
        if (!reason) {
          return;
        }
        await Api.rejectTask(task.taskId, { reason });
        pushLog({
          id: crypto.randomUUID(),
          timestamp: new Date().toLocaleTimeString(),
          message: t("tasks.reject.log", { taskId: task.taskId, userId: identity.userId, reason })
        });
      } else {
        const comment = window.prompt(t("tasks.complete.prompt"), t("tasks.prompt.completeDefault"));
        await Api.completeTask(task.taskId, { comment: comment ?? "" });
        pushLog({
          id: crypto.randomUUID(),
          timestamp: new Date().toLocaleTimeString(),
          message: t("tasks.complete.log", { taskId: task.taskId, userId: identity.userId })
        });
      }

      await loadTasks();
      if (onStatusRefresh) {
        await onStatusRefresh(task.processInstanceId);
      }
      setMessage(t("tasks.operation.success"));
      setMessageTone("success");
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Unknown error";
      setMessage(t("tasks.operation.error", { message: errorMessage }));
      setMessageTone("error");
      pushLog({
        id: crypto.randomUUID(),
        timestamp: new Date().toLocaleTimeString(),
        message: t("tasks.operation.error", { message: errorMessage })
      });
    }
  };

  return (
    <section className="card board-card">
      <header className="card-header">
        <h2>{title}</h2>
        <p>
          {t("tasks.role.info", { name: identity.displayName ?? identity.userId, userId: identity.userId })}
        </p>
      </header>
      <button className="btn primary" type="button" onClick={() => loadTasks()} disabled={loading}>
        {loading ? t("tasks.refresh.loading") : t("tasks.refresh")}
      </button>
      {message && (
        <div className={`alert ${messageTone ?? "neutral"}`} role="status">
          {message}
        </div>
      )}
      {tasks.length === 0 ? (
        <div className="empty">{t("tasks.empty")}</div>
      ) : (
        <div className="task-list">
          {tasks.map((task) => (
            <article key={task.taskId} className="task-card">
              <header>
                <div>
                  <strong className="task-title">{task.taskName}</strong>
                  <div className="task-meta">{t("status.field.processId")}: {task.processInstanceId}</div>
                </div>
                <StatusTag value={role === "approver" ? "PENDING" : "MANUAL"} />
              </header>
              <div className="task-body">
                <div>
                  {t("status.start")}: {formatDateTime(task.createdAt)}
                </div>
                {task.payload && Object.keys(task.payload).length > 0 && (
                  <pre className="payload">{JSON.stringify(task.payload, null, 2)}</pre>
                )}
              </div>
              <footer className="task-actions">
                {role === "approver" ? (
                  <>
                    <button className="btn primary" type="button" onClick={() => action(task, "approve")}>
                      {t("tasks.approve")}
                    </button>
                    <button className="btn secondary" type="button" onClick={() => action(task, "reject")}>
                      {t("tasks.reject")}
                    </button>
                  </>
                ) : (
                  <button className="btn primary" type="button" onClick={() => action(task, "complete")}>
                    {t("tasks.complete")}
                  </button>
                )}
              </footer>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default TaskBoard;
