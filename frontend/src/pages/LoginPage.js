import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import { ThemeToggle } from "../theme";

export default function LoginPage() {
  const { session, setSession } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "verifier", password: "Verifier@123" });
  const [error, setError] = useState("");

  if (session) {
    return <Navigate to="/" replace />;
  }

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    try {
      const { data } = await api.post("/auth/login", form);
      setSession(data);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.error || "Unable to sign in");
    }
  }

  return (
    <div className="auth-shell">
      <ThemeToggle />
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="kicker">MIM736 · DevOps practical</div>
        <h1>Qualification Verification System</h1>
        <p className="muted">Sign in as a student, issuer, verifier or administrator.</p>
        {error && <div className="flash error">{error}</div>}
        <label className="field">
          Username
          <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
        </label>
        <label className="field">
          Password
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
        </label>
        <button type="submit">Sign in</button>
        <p className="muted">
          New user? <Link to="/register">Create an account</Link>
        </p>
        <p className="muted">
          Demo: student / Student@123 · graduating / Graduating@123 · freshman / Freshman@123 · issuer / Issuer@123 ·
          econet / Econet@123 · cbz / Cbz@123 · delta / Delta@123. More accounts are in the README.
        </p>
      </form>
    </div>
  );
}
