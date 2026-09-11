import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import AboutUsPage from "./pages/AboutUsPage";
import ContactUsPage from "./pages/ContactUsPage";
import { AuthContext } from "./auth";
import { ThemeProvider } from "./theme";

test("login screen explains authorised access and demo accounts", () => {
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
  expect(screen.getByText(/issuer \/ Issuer@123/i)).toBeInTheDocument();
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
