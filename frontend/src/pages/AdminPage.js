import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import { PersonCell } from "../components/PersonCell";

export default function AdminPage() {
  const { session } = useAuth();
  const [tab, setTab] = useState("users");
  const [users, setUsers] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [students, setStudents] = useState([]);
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");

  useEffect(() => {
    if (session?.role !== "ADMIN") {
      return;
    }
    Promise.all([api.get("/admin/users"), api.get("/admin/institutions"), api.get("/students")])
      .then(([userRes, instRes, studentRes]) => {
        setUsers(userRes.data);
        setInstitutions(instRes.data);
        setStudents(studentRes.data);
      })
      .catch((err) => setError(err.response?.data?.error || "Unable to load directory"));
  }, [session?.role]);

  if (session?.role !== "ADMIN") {
    return <p>Only system operators can open the directory.</p>;
  }

  const q = query.trim().toLowerCase();
  const visibleUsers = users.filter(
    (row) =>
      !q ||
      row.fullName.toLowerCase().includes(q) ||
      row.username.toLowerCase().includes(q) ||
      row.role.toLowerCase().includes(q)
  );
  const visibleStudents = students.filter(
    (row) =>
      !q ||
      row.fullName.toLowerCase().includes(q) ||
      row.username.toLowerCase().includes(q) ||
      (row.institutionName || "").toLowerCase().includes(q)
  );
  const credentialTotal = institutions.reduce((sum, row) => sum + (row.credentialCount || 0), 0);

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>System directory</h1>
          <p className="muted">Every user, institution, student and credential count in QVS.</p>
        </div>
      </div>
      <div className="stats">
        <div className="stat">
          <div className="muted">Total users</div>
          <h2>{users.length}</h2>
        </div>
        <div className="stat">
          <div className="muted">Institutions</div>
          <h2>{institutions.length}</h2>
        </div>
        <div className="stat">
          <div className="muted">Students</div>
          <h2>{students.length}</h2>
        </div>
        <div className="stat">
          <div className="muted">Credentials</div>
          <h2>{credentialTotal}</h2>
        </div>
      </div>
      {error && <div className="flash error">{error}</div>}
      <div className="page-grid">
        <section className="surface page-main">
          <div className="tabs">
            {[
              ["users", "All users"],
              ["institutions", "Institutions"],
              ["students", "Students"],
            ].map(([value, label]) => (
              <button key={value} type="button" className={`tab ${tab === value ? "active" : ""}`} onClick={() => setTab(value)}>
                {label}
              </button>
            ))}
          </div>
          <div className="toolbar">
            <div className="search-field">
              <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Filter directory" />
            </div>
          </div>
          {tab === "users" && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Username</th>
                    <th>Role</th>
                    <th>Institution</th>
                  </tr>
                </thead>
                <tbody>
                  {visibleUsers.map((row) => (
                    <tr key={row.id}>
                      <td>
                        <PersonCell name={row.fullName} subtitle={row.email} />
                      </td>
                      <td>{row.username}</td>
                      <td>{row.role}</td>
                      <td>{row.institutionName || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {tab === "institutions" && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Institution</th>
                    <th>Students</th>
                    <th>Credentials</th>
                  </tr>
                </thead>
                <tbody>
                  {institutions.map((row) => (
                    <tr key={row.id}>
                      <td>
                        <PersonCell name={row.name} subtitle={row.code} />
                      </td>
                      <td>{row.studentCount}</td>
                      <td>{row.credentialCount}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {tab === "students" && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Student / institution</th>
                    <th>Username</th>
                    <th>Stage</th>
                    <th>Credentials</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {visibleStudents.map((row) => (
                    <tr key={row.id}>
                      <td>
                        <PersonCell name={row.fullName} subtitle={row.institutionName} />
                      </td>
                      <td>{row.username}</td>
                      <td>{row.studentStage}</td>
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
          )}
        </section>

        <aside className="surface page-side">
          <h3>Governance notes</h3>
          <ul className="task-list">
            <li>Keep institution code mappings current before intake periods.</li>
            <li>Audit role assignments after onboarding new issuers and verifiers.</li>
            <li>Use student detail pages to inspect credential allocation gaps.</li>
          </ul>
        </aside>
      </div>
    </div>
  );
}
