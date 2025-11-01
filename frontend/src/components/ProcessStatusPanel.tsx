import { useEffect, useState } from "react";
import { Api } from "../api";
import type { ActivityLogItem, ProcessStatusResponse } from "../types";
import { formatDateTime } from "../utils/format";
import StatusTag from "./StatusTag";
import { useI18n } from "../i18n/I18nContext";
import "./ProcessStatusPanel.css";

export interface ProcessStatusPanelProps {
  initialStatus?: ProcessStatusResponse | null;
  onStatusLoaded?: (status: ProcessStatusResponse) => void;
  pushLog: (entry: ActivityLogItem) => void;
}

export function ProcessStatusPanel({ initialStatus, onStatusLoaded, pushLog }: ProcessStatusPanelProps) {
  const [processId, setProcessId] = useState(initialStatus?.processInstanceId ?? "");
  const [status, setStatus] = useState<ProcessStatusResponse | null>(initialStatus ?? null);
  const [message, setMessage] = useState<string | null>(null);
  const [messageTone, setMessageTone] = useState<"success" | "error" | null>(null);
  const [loading, setLoading] = useState(false);
  const { t } = useI18n();

  useEffect(() => {
    if (initialStatus?.processInstanceId) {
      setProcessId(initialStatus.processInstanceId);
      setStatus(initialStatus);
    }
  }, [initialStatus]);

  const fetchStatus = async (id: string) => {
    const trimmed = id.trim();
    if (!trimmed) {
      setMessage(t("status.fetch.error", { message: "Process instance ID is required" }));
      setMessageTone("error");
      return;
    }
    setLoading(true);
    try {
      const result = await Api.getProcessStatus(trimmed);
      setStatus(result);
      setMessage(t("status.fetch.success"));
      setMessageTone("success");
      pushLog({
        id: crypto.randomUUID(),
        timestamp: new Date().toLocaleTimeString(),
        message: t("status.log.refresh", { id: trimmed, state: result.state ?? t("statusTag.unknown") })
      });
      onStatusLoaded?.(result);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Unknown error";
      setMessage(t("status.fetch.error", { message: errorMessage }));
      setMessageTone("error");
      pushLog({
        id: crypto.randomUUID(),
        timestamp: new Date().toLocaleTimeString(),
        message: t("status.log.refresh.error", { message: errorMessage })
      });
    } finally {
      setLoading(false);
    }
  };

  const submit = (event: React.FormEvent) => {
    event.preventDefault();
    fetchStatus(processId);
  };

  return (
    <section className="card">
      <header className="card-header">
        <h2>{t("status.heading")}</h2>
        <p>{t("status.description")}</p>
      </header>

      <form className="status-filter" onSubmit={submit}>
        <label>
          {t("status.field.processId")}
          <input value={processId} onChange={(event) => setProcessId(event.target.value)} placeholder="process-instance-id" />
        </label>
        <button className="btn primary" type="submit" disabled={loading}>
          {loading ? t("status.fetch.loading") : t("status.fetch")}
        </button>
      </form>

      {message && (
        <div className={`alert ${messageTone ?? "neutral"}`} role="status">
          {message}
        </div>
      )}

      {status && (
        <div className="status-summary">
          <div className="summary-header">
            <div>
              <div className="label">{t("status.field.processId")}</div>
              <div className="value">{status.processInstanceId}</div>
            </div>
            <StatusTag value={status.state} />
          </div>

          {status.currentTask && (
            <div className="summary-block">
              <div className="block-title">{t("status.currentTask")}</div>
              <div>{status.currentTask.taskName}</div>
              <div className="muted">ID: {status.currentTask.taskId}</div>
              <div className="muted">{t("status.start")}: {formatDateTime(status.currentTask.createdAt ?? undefined)}</div>
            </div>
          )}

          <div className="summary-block">
            <div className="block-title">{t("status.variables")}</div>
            <pre>{JSON.stringify(status.variables ?? {}, null, 2)}</pre>
          </div>

          <div className="summary-block">
            <div className="block-title">{t("status.timeline")}</div>
            <div className="timeline">
              {status.history.length === 0 && <div className="muted">{t("status.timeline.empty")}</div>}
              {status.history.map((entry) => (
                <div className="timeline-item" key={`${entry.activityId}-${entry.startTime ?? ""}`}>
                  <div className="timeline-header">
                    <strong>{entry.activityName || entry.activityId}</strong>
                    <span className="muted">{entry.activityType}</span>
                  </div>
                  {entry.assignee && (
                    <div>
                      {t("status.handler")}: {entry.assignee}
                    </div>
                  )}
                  {entry.result && (
                    <div>
                      {t("status.result")}: <StatusTag value={entry.result} />
                    </div>
                  )}
                  <div className="muted">{t("status.start")}: {formatDateTime(entry.startTime)}</div>
                  <div className="muted">{t("status.end")}: {formatDateTime(entry.endTime)}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default ProcessStatusPanel;
