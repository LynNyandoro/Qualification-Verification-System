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
      toggle: () => setDark((current) => !current),
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
    <button
      type="button"
      className="ghost theme-toggle"
      onClick={toggle}
      aria-pressed={dark}
      aria-label={dark ? "Switch to light mode" : "Switch to dark mode"}
      title={dark ? "Light mode" : "Dark mode"}
    >
      {dark ? "Light" : "Dark"}
    </button>
  );
}
