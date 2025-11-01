import type { ProcessState } from "../types";
import { useI18n } from "../i18n/I18nContext";
import "./StatusTag.css";

export interface StatusTagProps {
  value?: ProcessState | string | null;
  placeholder?: string;
}

const normalize = (value?: ProcessState | string | null) => value?.toString().toUpperCase() ?? null;

export function StatusTag({ value, placeholder }: StatusTagProps) {
  const { t } = useI18n();
  const normalized = normalize(value);
  if (!normalized) {
    return <span className="status-tag neutral">{placeholder ?? t("statusTag.unknown")}</span>;
  }

  let tone: string = "pending";
  let labelKey = "statusTag.pending" as const;
  if (normalized.includes("COMPLETED") || normalized.includes("APPROVED")) {
    tone = "completed";
    labelKey = "statusTag.completed";
  } else if (normalized.includes("REJECT")) {
    tone = "rejected";
    labelKey = "statusTag.rejected";
  } else if (normalized.includes("MANUAL")) {
    tone = "manual";
    labelKey = "statusTag.manual";
  }

  return <span className={`status-tag ${tone}`}>{t(labelKey)}</span>;
}

export default StatusTag;
