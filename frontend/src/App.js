import { useEffect, useState } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import Layout from "./components/Layout";
import { AuthContext } from "./auth";
import AuditPage from "./pages/AuditPage";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import RegisterQualificationPage from "./pages/RegisterQualificationPage";
import SearchPage from "./pages/SearchPage";
import VerifyPage from "./pages/VerifyPage";

function Protected({ children }) {
  const raw = localStorage.getItem("qvs-auth");
  const location = useLocation();
  if (!raw) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
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
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          element={
            <Protected>
              <Layout />
            </Protected>
          }
        >
          <Route path="/" element={<DashboardPage />} />
          <Route path="/qualifications/new" element={<RegisterQualificationPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/verify" element={<VerifyPage />} />
          <Route path="/audit" element={<AuditPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthContext.Provider>
  );
}
