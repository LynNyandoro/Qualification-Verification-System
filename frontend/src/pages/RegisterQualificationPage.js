import { useEffect, useState } from "react";
import api from "../api";
import { useAuth } from "../auth";

const empty = {
  holderName: "",
  holderNationalId: "",
  holderUsername: "",
  title: "",
  type: "DEGREE",
  issuingInstitution: "",
  issueDate: "",
  expiryDate: "",
  nqfLevel: "",
};

export default function RegisterQualificationPage() {
  const { session } = useAuth();
  const [form, setForm] = useState({ ...empty, issuingInstitution: session?.institutionName || "" });
  const [students, setStudents] = useState([]);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/students").then((res) => setStudents(res.data)).catch(() => setStudents([]));
  }, []);

  if (session?.role !== "ADMIN" && session?.role !== "ISSUER") {
    return <p>Only issuing institutions can award qualifications.</p>;
  }

  const awardable = students.filter((row) => row.studentStage !== "ENROLLED");
  const graduating = students.filter((row) => row.studentStage === "GRADUATING").length;
  const alumni = students.filter((row) => row.studentStage === "ALUMNI").length;

  function onPickStudent(username) {
    const picked = students.find((row) => row.username === username);
    setForm({
      ...form,
      holderUsername: username,
      holderName: picked ? picked.fullName : form.holderName,
      issuingInstitution: picked?.institutionName || session?.institutionName || form.issuingInstitution,
    });
  }

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setResult(null);
    try {
      const payload = {
        ...form,
        holderUsername: form.holderUsername || null,
        nqfLevel: form.nqfLevel ? Number(form.nqfLevel) : null,
        expiryDate: form.expiryDate || null,
      };
      const { data } = await api.post("/qualifications", payload);
      setResult(data);
      setForm({ ...empty, issuingInstitution: session?.institutionName || "" });
    } catch (err) {
      setError(err.response?.data?.error || "Registration failed");
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Award a qualification</h1>
          <p className="muted">Newly enrolled students cannot receive an award until they are marked as graduating or alumni.</p>
        </div>
      </div>
      {error && <div className="flash error">{error}</div>}
      {result && (
        <div className="flash ok">
          Awarded. Verification code <strong>{result.verificationCode}</strong>.
        </div>
      )}
      <div className="page-grid">
        <section className="surface page-main">
          <form className="panel" onSubmit={onSubmit}>
            <label className="field">
              Student (graduating or alumni)
              <select value={form.holderUsername} onChange={(e) => onPickStudent(e.target.value)}>
                <option value="">Select a student</option>
                {awardable.map((row) => (
                  <option key={row.id} value={row.username}>
                    {row.fullName} ({row.username} · {row.studentStage})
                  </option>
                ))}
              </select>
            </label>
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
                <input
                  type="date"
                  value={form.issueDate}
                  onChange={(e) => setForm({ ...form, issueDate: e.target.value })}
                  required
                />
              </label>
              <label className="field">
                Expiry date
                <input type="date" value={form.expiryDate} onChange={(e) => setForm({ ...form, expiryDate: e.target.value })} />
              </label>
            </div>
            <button type="submit">Save credential</button>
          </form>
        </section>

        <aside className="surface page-side">
          <h3>Award readiness</h3>
          <div className="stats compact-stats">
            <div className="stat">
              <div className="muted">Eligible students</div>
              <h2>{awardable.length}</h2>
            </div>
            <div className="stat">
              <div className="muted">Graduating</div>
              <h2>{graduating}</h2>
            </div>
            <div className="stat">
              <div className="muted">Alumni</div>
              <h2>{alumni}</h2>
            </div>
          </div>
          <ul className="task-list">
            <li>Use student picker to preload holder and institution details.</li>
            <li>Ensure issue date is accurate before publishing a code.</li>
            <li>Share verification code with candidate after saving.</li>
          </ul>
        </aside>
      </div>
    </div>
  );
}
