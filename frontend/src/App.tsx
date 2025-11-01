import { useState } from "react";
import ActivityLog from "./components/ActivityLog";
import { Api } from "./api";
import ProcessStatusPanel from "./components/ProcessStatusPanel";
import StartProcessForm from "./components/StartProcessForm";
import TaskBoard from "./components/TaskBoard";
import type { ActivityLogItem, ProcessStatusResponse, StartProcessResponse } from "./types";
import { IdentitySwitcher } from "./components/IdentitySwitcher";
import { useAuth } from "./context/AuthContext";
import { useI18n } from "./i18n/I18nContext";
import "./App.css";

type TabKey = "start" | "approval" | "manual" | "status";

function createLog(message: string): ActivityLogItem {
  return {
    id: crypto.randomUUID(),
    timestamp: new Date().toLocaleTimeString(),
    message
  };
}

export function App() {
  const { identity } = useAuth();
  const { t } = useI18n();
  const [activeTab, setActiveTab] = useState<TabKey>("start");
  const [activityLog, setActivityLog] = useState<ActivityLogItem[]>([]);
  const [latestStatus, setLatestStatus] = useState<ProcessStatusResponse | null>(null);

  const pushLog = (draft: ActivityLogItem | string) => {
    const entry = typeof draft === "string" ? createLog(draft) : draft;
    setActivityLog((prev) => [entry, ...prev]);
  };

  const handleProcessStarted = async (response: StartProcessResponse) => {
    setActiveTab("status");
    pushLog(t("start.log.switch", { id: response.processInstanceId }));
    setLatestStatus(null);
  };

  const refreshStatus = async (processInstanceId: string) => {
    try {
      const status = await Api.getProcessStatus(processInstanceId);
      setLatestStatus(status);
      pushLog(
        t("status.log.refresh", {
          id: processInstanceId,
          state: status.state ?? t("statusTag.unknown")
        })
      );
    } catch (err) {
      const message = err instanceof Error ? err.message : "Unknown error";
      pushLog(t("status.log.refresh.error", { message }));
    }
  };

  const handleStatusLoaded = (status: ProcessStatusResponse) => {
    setLatestStatus(status);
  };

  const tabsContent = {
    start: (
      <StartProcessForm
        onStarted={handleProcessStarted}
        pushLog={(entry) => pushLog(entry)}
      />
    ),
    approval: (
      <TaskBoard
        role="approver"
        title={t("tasks.heading.approval")}
        onStatusRefresh={refreshStatus}
        pushLog={(entry) => pushLog(entry)}
      />
    ),
    manual: (
      <TaskBoard
        role="executor"
        title={t("tasks.heading.manual")}
        onStatusRefresh={refreshStatus}
        pushLog={(entry) => pushLog(entry)}
      />
    ),
    status: (
      <ProcessStatusPanel
        initialStatus={latestStatus}
        onStatusLoaded={handleStatusLoaded}
        pushLog={(entry) => pushLog(entry)}
      />
    )
  } as const;

  return (
    <div className="app-shell">
      <header className="hero">
        <div>
          <h1>{t("app.title")}</h1>
          <p>{t("app.subtitle")}</p>
        </div>
        <div className="hero-identity">
          <div className="hero-identity-info">
            <span className="hero-identity-label">{t("app.currentIdentity")}</span>
            <strong>{identity.displayName ?? identity.userId}</strong>
            <span className="hero-identity-hint">{identity.userId}</span>
          </div>
          <IdentitySwitcher />
        </div>
      </header>

      <div className="tabs">
        {(
          [
            { id: "start", label: t("tabs.start") },
            { id: "approval", label: t("tabs.approval") },
            { id: "manual", label: t("tabs.manual") },
            { id: "status", label: t("tabs.status") }
          ] as Array<{ id: TabKey; label: string }>
        ).map((tab) => (
          <button
            key={tab.id}
            className={`tab-button ${activeTab === tab.id ? "active" : ""}`}
            type="button"
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <main className="content">{tabsContent[activeTab]}</main>

      <ActivityLog entries={activityLog} />
    </div>
  );
}

export default App;
