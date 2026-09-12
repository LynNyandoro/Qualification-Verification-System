import { createContext, useContext, useLayoutEffect, useMemo, useState } from "react";

export const ThemeContext = createContext({ dark: false, toggle: () => {} });

const STORAGE_KEY = "qvs-theme";

function readStoredTheme() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === "dark" || stored === "light") {
      return stored === "dark";
    }
  } catch (err) {
    /* ignore */
  }
  return window.matchMedia?.("(prefers-color-scheme: dark)").matches ?? false;
}

function applyTheme(dark) {
  document.documentElement.setAttribute("data-theme", dark ? "dark" : "light");
}

export function ThemeProvider({ children }) {
  const [dark, setDark] = useState(readStoredTheme);

  useLayoutEffect(() => {
    applyTheme(dark);
    localStorage.setItem(STORAGE_KEY, dark ? "dark" : "light");
  }, [dark]);

  const value = useMemo(
    () => ({
      dark,
      toggle: (nextDark) => setDark((current) => (nextDark === undefined ? !current : nextDark)),
    }),
    [dark]
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  return useContext(ThemeContext);
}

export function ThemeToggle() {
  const { dark, toggle } = useTheme();

  return (
    <div className="theme-toggle theme-toggle-group" aria-label="Theme selector">
      <button
        type="button"
        className={`theme-option ${!dark ? "active" : ""}`}
        onClick={() => toggle(false)}
        aria-pressed={!dark}
        aria-label="Switch to light mode"
        title="Light mode"
      >
        Light
      </button>
      <button
        type="button"
        className={`theme-option ${dark ? "active" : ""}`}
        onClick={() => toggle(true)}
        aria-pressed={dark}
        aria-label="Switch to dark mode"
        title="Dark mode"
      >
        Dark
      </button>
    </div>
  );
}
