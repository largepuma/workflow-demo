import { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { IdentitySessionData } from "../session";
import { IdentitySession } from "../session";

export interface AuthContextValue {
  identity: IdentitySessionData;
  setIdentity: (identity: IdentitySessionData) => void;
}

const defaultIdentity: IdentitySessionData = {
  userId: "initiator-1",
  roles: ["initiator"],
  displayName: "Initiator"
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [identity, setIdentityState] = useState<IdentitySessionData>(() => {
    IdentitySession.set(defaultIdentity);
    return defaultIdentity;
  });

  useEffect(() => {
    IdentitySession.set(identity);
  }, [identity]);

  const value = useMemo<AuthContextValue>(
    () => ({
      identity,
      setIdentity: (next) => setIdentityState(next)
    }),
    [identity]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
