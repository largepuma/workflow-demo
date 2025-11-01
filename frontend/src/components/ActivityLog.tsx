import type { ActivityLogItem } from "../types";
import { useI18n } from "../i18n/I18nContext";
import "./ActivityLog.css";

export interface ActivityLogProps {
  entries: ActivityLogItem[];
}

export function ActivityLog({ entries }: ActivityLogProps) {
  const { t } = useI18n();
  return (
    <section className="panel">
      <header className="panel-header">
        <h2>{t("log.title")}</h2>
        <span className="panel-subtitle">{t("log.caption")}</span>
      </header>
      <div className="log-container">
        {entries.length === 0 ? (
          <div className="log-empty">{t("log.empty")}</div>
        ) : (
          entries.map((entry) => (
            <div className="log-line" key={entry.id}>
              <span className="log-time">[{entry.timestamp}]</span>
              <span className="log-message">{entry.message}</span>
            </div>
          ))
        )}
      </div>
    </section>
  );
}

export default ActivityLog;
