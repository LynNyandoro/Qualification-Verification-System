import { useState } from "react";
import api from "../api";

export default function VerifyPage() {
  const [form, setForm] = useState({ verificationCode: "QVS-DEMO12345", credentialId: "", expectedHash: "" });
  const [outcome, setOutcome] = useState(null);
  const [error, setError] = useState("");

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setOutcome(null);
    try {
      const { data } = await api.post("/verify", {
        verificationCode: form.verificationCode || null,
        credentialId: form.credentialId || null,
        expectedHash: form.expectedHash || null,
      });
      setOutcome(data);
    } catch (err) {
      setError(err.response?.data?.error || "Verification failed");
    }
  }

  return (
    <div>
      <div className="kicker">Authenticity</div>
      <h1>Verify a qualification</h1>
      <form className="panel" onSubmit={onSubmit}>
        <label className="field">
          Verification code
          <input
            value={form.verificationCode}
            onChange={(e) => setForm({ ...form, verificationCode: e.target.value })}
            placeholder="QVS-XXXXXXXXXX"
          />
        </label>
        <label className="field">
          Credential ID (optional)
          <input value={form.credentialId} onChange={(e) => setForm({ ...form, credentialId: e.target.value })} />
        </label>
        <label className="field">
          Expected SHA-256 hash (optional integrity check)
          <input value={form.expectedHash} onChange={(e) => setForm({ ...form, expectedHash: e.target.value })} />
        </label>
        <button type="submit">Verify authenticity</button>
      </form>
      {error && <div className="flash error">{error}</div>}
      {outcome && (
        <div className="panel" style={{ marginTop: 16 }}>
          <span className={`badge ${outcome.result}`}>{outcome.result}</span>
          <p>{outcome.message}</p>
          <p className="muted">Integrity hash match: {outcome.hashMatch ? "yes" : "no"}</p>
          {outcome.qualification && (
            <div>
              <h3>{outcome.qualification.title}</h3>
              <p>
                {outcome.qualification.holderName} · {outcome.qualification.issuingInstitution}
              </p>
              <p className="muted">Hash: {outcome.qualification.credentialHash}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
