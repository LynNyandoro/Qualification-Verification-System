import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
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
