import { useEffect, useState } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import Layout from "./components/Layout";
import { AuthContext } from "./auth";
import AboutUsPage from "./pages/AboutUsPage";
import AgentInsightsPage from "./pages/AgentInsightsPage";
import AuditPage from "./pages/AuditPage";
import ContactUsPage from "./pages/ContactUsPage";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import RegisterQualificationPage from "./pages/RegisterQualificationPage";
import SearchPage from "./pages/SearchPage";
import SearchByNamePage from "./pages/SearchByNamePage";
import StudentsPage from "./pages/StudentsPage";
import StudentDetailPage from "./pages/StudentDetailPage";
import AdminPage from "./pages/AdminPage";
import VerifyPage from "./pages/VerifyPage";
import { useAuth } from "./auth";

function Protected({ children }) {
  const { session } = useAuth();
  const location = useLocation();
  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return children;
}

function GuestOnly({ children }) {
  const { session } = useAuth();
  if (session) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}

export default function App() {
  const [session, setSession] = useState(() => {
    const raw = localStorage.getItem("qvs-auth");
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    if (session) {
      localStorage.setItem("qvs-auth", JSON.stringify(session));
    } else {
      localStorage.removeItem("qvs-auth");
    }
  }, [session]);

  return (
    <AuthContext.Provider value={{ session, setSession }}>
      <Routes>
        <Route
          path="/"
          element={
            <GuestOnly>
              <LoginPage />
            </GuestOnly>
          }
        />
        <Route
          path="/login"
          element={
            <GuestOnly>
              <LoginPage />
            </GuestOnly>
          }
        />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          element={
            <Protected>
              <Layout />
            </Protected>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/students" element={<StudentsPage />} />
          <Route path="/students/:id" element={<StudentDetailPage />} />
          <Route path="/qualifications/new" element={<RegisterQualificationPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/search-name" element={<SearchByNamePage />} />
          <Route path="/verify" element={<VerifyPage />} />
          <Route path="/audit" element={<AuditPage />} />
          <Route path="/ai-insights" element={<AgentInsightsPage />} />
          <Route path="/about" element={<AboutUsPage />} />
          <Route path="/contact" element={<ContactUsPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </AuthContext.Provider>
  );
}
