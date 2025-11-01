export interface IdentitySessionData {
  userId: string;
  roles: string[];
  displayName?: string;
}

let currentIdentity: IdentitySessionData | null = null;

export const IdentitySession = {
  set(identity: IdentitySessionData | null) {
    currentIdentity = identity;
  },
  get(): IdentitySessionData | null {
    return currentIdentity;
  }
};
