import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import { ThemeToggle } from "../theme";

export default function RegisterPage() {
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    email: "",
    fullName: "",
    password: "",
    role: "STUDENT",
  });
  const [error, setError] = useState("");

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    try {
      const { data } = await api.post("/auth/register", form);
      setSession(data);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.error || "Unable to register");
    }
  }

  return (
    <div className="auth-shell">
      <ThemeToggle />
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="kicker">Create account</div>
        <h1>Join QVS</h1>
        {error && <div className="flash error">{error}</div>}
        <label className="field">
          Full name
          <input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
        </label>
        <label className="field">
          Username
          <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
        </label>
        <label className="field">
          Email
          <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        </label>
        <label className="field">
          Password
          <input
            type="password"
            minLength={8}
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
        </label>
        <label className="field">
          Role
          <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
            <option value="STUDENT">Student / graduate</option>
            <option value="VERIFIER">Verifier / employer</option>
            <option value="ISSUER">Issuer / institution</option>
          </select>
        </label>
        <button type="submit">Register</button>
        <p className="muted">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}
