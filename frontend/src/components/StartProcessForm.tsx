import { useEffect, useState } from "react";
import { Api } from "../api";
import type { ActivityLogItem, StartProcessRequest, StartProcessResponse } from "../types";
import { useAuth } from "../context/AuthContext";
import { useI18n } from "../i18n/I18nContext";
import "./StartProcessForm.css";

export interface StartProcessFormProps {
  onStarted: (response: StartProcessResponse) => void;
  pushLog: (entry: ActivityLogItem) => void;
}

interface FormState {
  approverId: string;
  executorId: string;
  payloadText: string;
}

const defaultState: FormState = {
  approverId: "approver-1",
  executorId: "executor-1",
  payloadText: JSON.stringify({ amount: 1000, title: "Sample request" }, null, 2)
};

export function StartProcessForm({ onStarted, pushLog }: StartProcessFormProps) {
  const { identity } = useAuth();
  const { t } = useI18n();
  const [form, setForm] = useState<FormState>(defaultState);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [messageTone, setMessageTone] = useState<"success" | "error" | null>(null);

  useEffect(() => {
    setMessage(null);
    setMessageTone(null);
  }, [identity]);

  const handleChange = (field: keyof FormState) => (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((prev) => ({ ...prev, [field]: event.target.value }));

  const resetForm = () => {
    setForm(defaultState);
    setMessage(null);
    setMessageTone(null);
  };

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);
    setMessageTone(null);

    try {
      let payload: Record<string, unknown> = {};
      if (form.payloadText.trim().length > 0) {
        payload = JSON.parse(form.payloadText);
      }

      const request: StartProcessRequest = {
        initiator: identity.userId,
        approverId: form.approverId.trim(),
        executorId: form.executorId.trim(),
        payload
      };

      const response = await Api.startProcess(request);

      const successMessage = t("start.success", { id: response.processInstanceId, state: response.state ?? t("statusTag.unknown") });
      setMessage(successMessage);
      setMessageTone("success");
      pushLog({
        id: crypto.randomUUID(),
        timestamp: new Date().toLocaleTimeString(),
        message: successMessage
      });

      onStarted(response);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "Unknown error";
      const localized = t("start.error", { message: errorMessage });
      setMessage(localized);
      setMessageTone("error");
      pushLog({
        id: crypto.randomUUID(),
        timestamp: new Date().toLocaleTimeString(),
        message: localized
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <header className="card-header">
        <h2>{t("start.heading")}</h2>
        <p>{t("start.description", { name: identity.displayName ?? identity.roles[0], userId: identity.userId })}</p>
      </header>
      <form className="form" onSubmit={submit}>
        <div className="form-grid">
          <label>
            {t("start.approver")}
            <input required value={form.approverId} onChange={handleChange("approverId")} />
          </label>
          <label>
            {t("start.executor")}
            <input required value={form.executorId} onChange={handleChange("executorId")} />
          </label>
        </div>
        <label>
          {t("start.payload")}
          <textarea rows={6} value={form.payloadText} onChange={handleChange("payloadText")} />
        </label>
        <div className="form-actions">
          <button className="btn primary" type="submit" disabled={loading}>
            {loading ? t("start.launch.loading") : t("start.launch")}
          </button>
          <button className="btn secondary" type="button" onClick={resetForm}>
            {t("start.reset")}
          </button>
        </div>
        {message && (
          <div className={`alert ${messageTone ?? "neutral"}`} role="alert">
            {message}
          </div>
        )}
      </form>
    </section>
  );
}

export default StartProcessForm;
