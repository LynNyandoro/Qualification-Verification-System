import { useState } from "react";
import api from "../api";

export default function VerifyPage() {
  const [form, setForm] = useState({
    verificationCode: "QVS-DEMO12345",
    credentialId: "MISM-MSU-0001",
    candidateName: "",
    expectedHash: "",
  });
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
        candidateName: form.candidateName || null,
        expectedHash: form.expectedHash || null,
      });
      setOutcome(data);
    } catch (err) {
      setError(err.response?.data?.error || "Verification failed");
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
              <h1>Confirm a credential</h1>
          <p className="muted">
            Search by holder name first if you prefer, then paste the verification code or credential ID to confirm
            authenticity and write an audit record.
          </p>
        </div>
      </div>

      <div className="page-grid">
        <section className="surface page-main">
          <form className="panel" onSubmit={onSubmit}>
            <label className="field">
              Candidate name
              <input
                value={form.candidateName}
                onChange={(e) => setForm({ ...form, candidateName: e.target.value })}
                placeholder="Amina Chikomo"
              />
            </label>
            <label className="field">
              Verification code
              <input
                value={form.verificationCode}
                onChange={(e) => setForm({ ...form, verificationCode: e.target.value })}
                placeholder="QVS-DEMO12345"
              />
            </label>
            <label className="field">
              Credential ID (optional)
              <input
                value={form.credentialId}
                onChange={(e) => setForm({ ...form, credentialId: e.target.value })}
                placeholder="MISM-MSU-0001"
              />
            </label>
            <label className="field">
              Expected SHA-256 hash (optional integrity check)
              <input value={form.expectedHash} onChange={(e) => setForm({ ...form, expectedHash: e.target.value })} />
            </label>
            <button type="submit">Verify authenticity</button>
          </form>
          {error && <div className="flash error">{error}</div>}
          {outcome && (
            <div className="surface result-surface">
              <div className="surface-head">
                <h3>Verification outcome</h3>
                <span className={`badge ${outcome.result}`}>{outcome.result}</span>
              </div>
              <p>{outcome.message}</p>
              <p className="muted">Integrity hash match: {outcome.hashMatch ? "yes" : "no"}</p>
              {outcome.qualification && (
                <div className="record-summary">
                  <h3>{outcome.qualification.title}</h3>
                  <p>
                    {outcome.qualification.holderName} · {outcome.qualification.issuingInstitution}
                  </p>
                  <p className="muted">Hash: {outcome.qualification.credentialHash}</p>
                </div>
              )}
            </div>
          )}
        </section>

        <aside className="surface page-side">
          <h3>Verification checklist</h3>
          <ol className="ordered-list">
            <li>Search by candidate name to confirm holder context.</li>
            <li>Paste the verification code or credential ID exactly as issued.</li>
            <li>Use expected hash when validating a printed document.</li>
            <li>Review audit history after each completed check.</li>
          </ol>
          <div className="stats compact-stats">
            <div className="stat">
              <div className="muted">Default code</div>
              <h3>QVS-DEMO12345</h3>
            </div>
            <div className="stat">
              <div className="muted">Default ID</div>
              <h3>MISM-MSU-0001</h3>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
