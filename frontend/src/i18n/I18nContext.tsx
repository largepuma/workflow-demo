import { createContext, useContext, useMemo, useState } from "react";
import { translations, type SupportedLocale } from "./translations";

type TranslationParams = Record<string, string | number | undefined>;

interface I18nContextValue {
  locale: SupportedLocale;
  setLocale: (locale: SupportedLocale) => void;
  t: (key: keyof typeof translations["en"], params?: TranslationParams) => string;
}

const defaultLocale: SupportedLocale = "en";

const I18nContext = createContext<I18nContextValue | undefined>(undefined);

const compile = (template: string, params?: TranslationParams) =>
  template.replace(/\{\{(.*?)\}\}/g, (_, token: string) => {
    const value = params?.[token.trim()];
    return value === undefined ? "" : String(value);
  });

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [locale, setLocale] = useState<SupportedLocale>(defaultLocale);

  const value = useMemo<I18nContextValue>(() => {
    const resource = translations[locale] ?? translations[defaultLocale];
    return {
      locale,
      setLocale,
      t: (key, params) => {
        const template = resource[key] ?? translations[defaultLocale][key] ?? key;
        return compile(template as string, params);
      }
    };
  }, [locale]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nContextValue {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error("useI18n must be used within an I18nProvider");
  }
  return context;
}
