import { useMemo } from "react";
import { useAuth } from "../context/AuthContext";
import type { IdentitySessionData } from "../session";
import { useI18n } from "../i18n/I18nContext";
import type { TranslationKey } from "../i18n/translations";
import "./IdentitySwitcher.css";

type PersonaKey = "initiator" | "approver" | "executor";

interface PersonaDefinition {
  key: PersonaKey;
  userId: IdentitySessionData["userId"];
  roles: PersonaKey[];
  labelKey: TranslationKey;
}

const personas: PersonaDefinition[] = [
  { key: "initiator", userId: "initiator-1", roles: ["initiator"], labelKey: "identity.initiator" },
  { key: "approver", userId: "approver-1", roles: ["approver"], labelKey: "identity.approver" },
  { key: "executor", userId: "executor-1", roles: ["executor"], labelKey: "identity.executor" }
];

export function IdentitySwitcher() {
  const { identity, setIdentity } = useAuth();
  const { t } = useI18n();

  const activeKey = useMemo(() => {
    const persona = personas.find((item) => item.userId === identity.userId);
    return persona?.key ?? identity.roles[0] ?? "initiator";
  }, [identity]);

  const handleSwitch = (persona: PersonaDefinition) => {
    const displayName = t(persona.labelKey);
    setIdentity({
      userId: persona.userId,
      roles: persona.roles,
      displayName
    });
  };

  return (
    <div className="identity-switcher">
      {personas.map((persona) => (
        <button
          key={persona.key}
          type="button"
          className={`identity-chip ${activeKey === persona.key ? "active" : ""}`}
          onClick={() => handleSwitch(persona)}
        >
          <span className="label">{t(persona.labelKey)}</span>
          <span className="hint">{persona.userId}</span>
        </button>
      ))}
    </div>
  );
}

export default IdentitySwitcher;
