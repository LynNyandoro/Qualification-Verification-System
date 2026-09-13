import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import { PersonCell } from "../components/PersonCell";

const empty = {
  username: "",
  email: "",
  password: "Student@123",
  fullName: "",
  studentStage: "ENROLLED",
};

const stageLabel = {
  ENROLLED: "Newly enrolled",
  GRADUATING: "Finishing this year",
  ALUMNI: "Alumni",
};

export default function StudentsPage() {
  const { session } = useAuth();
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState(empty);
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");
  const [adding, setAdding] = useState(false);
  const [tab, setTab] = useState("ALL");
  const [query, setQuery] = useState("");

  const canManage = session?.role === "ADMIN" || session?.role === "ISSUER";

  async function load() {
    const { data } = await api.get("/students");
    setRows(data);
  }

  useEffect(() => {
    if (!canManage) {
      return;
    }
    load().catch((err) => setError(err.response?.data?.error || "Unable to load students"));
  }, [canManage]);

  if (!canManage) {
    return <p>Only university registrars can manage student enrolment.</p>;
  }

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setOk("");
    try {
      await api.post("/students", form);
      setOk(`Enrolled ${form.fullName} at ${session?.institutionName || "the institution"}.`);
      setForm(empty);
      setAdding(false);
      await load();
    } catch (err) {
      setError(err.response?.data?.error || "Enrolment failed");
    }
  }

  const visible = rows.filter((row) => {
    const matchTab = tab === "ALL" || row.studentStage === tab;
    const q = query.trim().toLowerCase();
    const matchQuery =
      !q ||
      row.fullName.toLowerCase().includes(q) ||
      row.username.toLowerCase().includes(q) ||
      (row.institutionName || "").toLowerCase().includes(q);
    return matchTab && matchQuery;
  });
  const total = rows.length;
  const enrolled = rows.filter((row) => row.studentStage === "ENROLLED").length;
  const graduating = rows.filter((row) => row.studentStage === "GRADUATING").length;
  const alumni = rows.filter((row) => row.studentStage === "ALUMNI").length;

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Student admin</h1>
          <p className="muted">Students at {session?.institutionName || "all institutions"}.</p>
        </div>
        <button type="button" onClick={() => setAdding((value) => !value)}>
          {adding ? "Close form" : "+ Enrol student"}
        </button>
      </div>

      <div className="stats">
        <div className="stat">
          <div className="muted">Total students</div>
          <h2>{total}</h2>
        </div>
        <div className="stat">
          <div className="muted">Newly enrolled</div>
          <h2>{enrolled}</h2>
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

      {error && <div className="flash error">{error}</div>}
      {ok && <div className="flash ok">{ok}</div>}

      <div className="page-grid">
        <section className="surface page-main">
          <div className="tabs">
            {[
              ["ALL", "All students"],
              ["ENROLLED", "Newly enrolled"],
              ["GRADUATING", "Graduating"],
              ["ALUMNI", "Alumni"],
            ].map(([value, label]) => (
              <button key={value} type="button" className={`tab ${tab === value ? "active" : ""}`} onClick={() => setTab(value)}>
                {label}
              </button>
            ))}
          </div>
          <div className="toolbar">
            <div className="search-field">
              <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search students" />
            </div>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name / institution</th>
                  <th>Username</th>
                  <th>Stage</th>
                  <th>Credentials</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {visible.map((row) => (
                  <tr key={row.id}>
                    <td>
                      <PersonCell name={row.fullName} subtitle={row.institutionName} />
                    </td>
                    <td>{row.username}</td>
                    <td>
                      <span className={`badge ${row.studentStage}`}>{stageLabel[row.studentStage] || row.studentStage}</span>
                    </td>
                    <td>{row.credentialCount}</td>
                    <td>
                      <Link className="button" to={`/students/${row.id}`}>
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {visible.length === 0 && <p className="muted">No students in this group yet.</p>}
        </section>

        <aside className="surface page-side">
          <h3>Enrolment panel</h3>
          <p className="muted">Add a new student and assign the stage that reflects their academic lifecycle.</p>
          {adding ? (
            <form className="form-card form-compact" onSubmit={onSubmit}>
              <div className="row">
                <label className="field">
                  Full name
                  <input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
                </label>
                <label className="field">
                  Username
                  <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
                </label>
              </div>
              <div className="row">
                <label className="field">
                  Email
                  <input
                    type="email"
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                    required
                  />
                </label>
                <label className="field">
                  Temporary password
                  <input
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                    minLength={8}
                    required
                  />
                </label>
              </div>
              <label className="field">
                Stage
                <select value={form.studentStage} onChange={(e) => setForm({ ...form, studentStage: e.target.value })}>
                  <option value="ENROLLED">Newly enrolled</option>
                  <option value="GRADUATING">Finishing this year</option>
                  <option value="ALUMNI">Alumni</option>
                </select>
              </label>
              <button type="submit">Save student</button>
            </form>
          ) : (
            <ul className="task-list">
              <li>Mark ENROLLED for new intakes with no credentials.</li>
              <li>Move to GRADUATING when award verification is pending.</li>
              <li>Set ALUMNI once at least one credential is issued.</li>
            </ul>
          )}
        </aside>
      </div>
    </div>
  );
}
