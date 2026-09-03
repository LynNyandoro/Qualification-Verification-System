import { useState } from "react";
import api from "../api";
import { useAuth } from "../auth";

const empty = {
  holderName: "",
  holderNationalId: "",
  title: "",
  type: "DEGREE",
  issuingInstitution: "",
  issueDate: "",
  expiryDate: "",
  nqfLevel: "",
};

export default function RegisterQualificationPage() {
  const { session } = useAuth();
  const [form, setForm] = useState(empty);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  if (session?.role !== "ADMIN" && session?.role !== "ISSUER") {
    return <p>Only issuing institutions can register qualifications.</p>;
  }

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setResult(null);
    try {
      const payload = {
        ...form,
        nqfLevel: form.nqfLevel ? Number(form.nqfLevel) : null,
        expiryDate: form.expiryDate || null,
      };
      const { data } = await api.post("/qualifications", payload);
      setResult(data);
      setForm(empty);
    } catch (err) {
      setError(err.response?.data?.error || "Registration failed");
    }
  }

  return (
    <div>
      <div className="kicker">Issuing</div>
      <h1>Register a qualification</h1>
      {error && <div className="flash error">{error}</div>}
      {result && (
        <div className="flash ok">
          Registered. Verification code <strong>{result.verificationCode}</strong>. Integrity hash stored.
        </div>
      )}
      <form className="panel" onSubmit={onSubmit}>
        <div className="row">
          <label className="field">
            Holder name
            <input value={form.holderName} onChange={(e) => setForm({ ...form, holderName: e.target.value })} required />
          </label>
          <label className="field">
            National ID (optional)
            <input
              value={form.holderNationalId}
              onChange={(e) => setForm({ ...form, holderNationalId: e.target.value })}
            />
          </label>
        </div>
        <label className="field">
          Qualification title
          <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
        </label>
        <div className="row">
          <label className="field">
            Type
            <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              <option>DEGREE</option>
              <option>DIPLOMA</option>
              <option>CERTIFICATE</option>
              <option>PROFESSIONAL</option>
            </select>
          </label>
          <label className="field">
            NQF level
            <input
              type="number"
              min="1"
              max="10"
              value={form.nqfLevel}
              onChange={(e) => setForm({ ...form, nqfLevel: e.target.value })}
            />
          </label>
        </div>
        <label className="field">
          Issuing institution
          <input
            value={form.issuingInstitution}
            onChange={(e) => setForm({ ...form, issuingInstitution: e.target.value })}
            required
          />
        </label>
        <div className="row">
          <label className="field">
            Issue date
            <input type="date" value={form.issueDate} onChange={(e) => setForm({ ...form, issueDate: e.target.value })} required />
          </label>
          <label className="field">
            Expiry date
            <input type="date" value={form.expiryDate} onChange={(e) => setForm({ ...form, expiryDate: e.target.value })} />
          </label>
        </div>
        <button type="submit">Save credential</button>
      </form>
    </div>
  );
}
