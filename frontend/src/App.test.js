import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "./App";
import LoginPage from "./pages/LoginPage";
import AboutUsPage from "./pages/AboutUsPage";
import ContactUsPage from "./pages/ContactUsPage";
import SearchByNamePage from "./pages/SearchByNamePage";
import { AuthContext } from "./auth";
import { ThemeProvider, ThemeToggle } from "./theme";

function renderApp(path) {
  window.localStorage.removeItem("qvs-auth");
  return render(
    <MemoryRouter initialEntries={[path]}>
      <ThemeProvider>
        <App />
      </ThemeProvider>
    </MemoryRouter>
  );
}

test("root and login routes show the sign-in page", () => {
  renderApp("/");
  expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
  expect(screen.getByText(/sign in as a student/i)).toBeInTheDocument();
});

test("login path shows the sign-in page", () => {
  renderApp("/login");
  expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
});

test("protected dashboard redirects guests to login", () => {
  renderApp("/dashboard");
  expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
});

test("login screen does not expose demo credentials", () => {
  render(
    <MemoryRouter>
      <ThemeProvider>
        <AuthContext.Provider value={{ session: null, setSession: () => {} }}>
          <LoginPage />
        </AuthContext.Provider>
      </ThemeProvider>
    </MemoryRouter>
  );
  expect(screen.getByText(/Qualification Verification System/i)).toBeInTheDocument();
  expect(screen.queryByText(/student \/ Student@123/i)).not.toBeInTheDocument();
  expect(screen.queryByText(/issuer \/ Issuer@123/i)).not.toBeInTheDocument();
});

test("about and contact pages link to each other", () => {
  const { rerender } = render(
    <MemoryRouter>
      <ThemeProvider>
        <AboutUsPage />
      </ThemeProvider>
    </MemoryRouter>
  );

  expect(screen.getByText(/Our mission/i)).toBeInTheDocument();
  expect(screen.getByRole("link", { name: /contact us/i })).toHaveAttribute("href", "/contact");

  rerender(
    <MemoryRouter>
      <ThemeProvider>
        <ContactUsPage />
      </ThemeProvider>
    </MemoryRouter>
  );

  expect(screen.getByText(/Tell us how we can help/i)).toBeInTheDocument();
  expect(screen.getByRole("link", { name: /about us/i })).toHaveAttribute("href", "/about");
});

test("registrar and employer dashboard can search by candidate name", () => {
  render(
    <MemoryRouter>
      <ThemeProvider>
        <AuthContext.Provider value={{ session: { role: "VERIFIER" }, setSession: () => {} }}>
          <SearchByNamePage />
        </AuthContext.Provider>
      </ThemeProvider>
    </MemoryRouter>
  );

  expect(screen.getByText(/Search by candidate name/i)).toBeInTheDocument();
  expect(screen.getByPlaceholderText(/enter candidate name/i)).toBeInTheDocument();
});

test("theme toggle can switch between dark and light mode", () => {
  render(
    <ThemeProvider>
      <ThemeToggle />
    </ThemeProvider>
  );

  const toggle = screen.getByRole("button", { name: /switch to dark mode/i });
  expect(document.documentElement.getAttribute("data-theme")).toBe("light");

  fireEvent.click(toggle);
  expect(document.documentElement.getAttribute("data-theme")).toBe("dark");
  expect(screen.getByRole("button", { name: /switch to light mode/i })).toBeInTheDocument();

  fireEvent.click(screen.getByRole("button", { name: /switch to light mode/i }));
  expect(document.documentElement.getAttribute("data-theme")).toBe("light");
});
