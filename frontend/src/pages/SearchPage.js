import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import { CopyId } from "../components/CopyId";
import { PersonCell } from "../components/PersonCell";

export default function SearchPage() {
  const { session } = useAuth();
  const isStudent = session?.role === "STUDENT";
  const [filters, setFilters] = useState({ q: "", status: "" });
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");

  async function load(params) {
    setError("");
    try {
      const { data } = await api.get("/qualifications", { params });
      setRows(data);
    } catch (err) {
      setError(err.response?.data?.error || "Search failed");
    }
  }

  useEffect(() => {
    if (isStudent) {
      load();
      return;
    }
    load({
      status: filters.status || undefined,
    });
  }, [isStudent, filters.status]);

  async function onSubmit(event) {
    event.preventDefault();
    await load({
      q: filters.q || undefined,
      status: filters.status || undefined,
    });
  }

  async function handleRevoke(id) {
    try {
      const { data } = await api.put(`/qualifications/${id}/revoke`);
      setRows((current) => current.map((row) => (row.id === id ? { ...row, status: data.status } : row)));
    } catch (err) {
      setError(err.response?.data?.error || "Unable to revoke qualification");
    }
  }

  const activeCount = rows.filter((row) => row.status === "ACTIVE").length;
  const revokedCount = rows.filter((row) => row.status === "REVOKED").length;
  const expiredCount = rows.filter((row) => row.status === "EXPIRED").length;

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>{isStudent ? "My qualifications" : "Search qualifications"}</h1>
          <p className="muted">
            {isStudent
              ? "Copy the credential ID or the verification code to share with an employer."
              : "Find a holder by name, credential ID or verification code."}
          </p>
        </div>
      </div>

      <div className="stats">
        <div className="stat">
          <div className="muted">Visible records</div>
          <h2>{rows.length}</h2>
        </div>
        <div className="stat">
          <div className="muted">Active</div>
          <h2>{activeCount}</h2>
        </div>
        <div className="stat">
          <div className="muted">Revoked</div>
          <h2>{revokedCount}</h2>
        </div>
        <div className="stat">
          <div className="muted">Expired</div>
          <h2>{expiredCount}</h2>
        </div>
      </div>

      <div className={`page-grid ${isStudent ? "single-column" : ""}`}>
        <section className="surface page-main">
          {!isStudent && (
            <>
              <div className="tabs">
                {["", "ACTIVE", "REVOKED", "EXPIRED"].map((value) => (
                  <button
                    key={value || "all"}
                    type="button"
                    className={`tab ${filters.status === value ? "active" : ""}`}
                    onClick={() => setFilters({ ...filters, status: value })}
                  >
                    {value || "All records"}
                  </button>
                ))}
              </div>
              <form className="toolbar" onSubmit={onSubmit}>
                <div className="search-field">
                  <input
                    value={filters.q}
                    onChange={(e) => setFilters({ ...filters, q: e.target.value })}
                    placeholder="Search name or credential ID"
                  />
                </div>
                <button type="submit">Search</button>
              </form>
            </>
          )}
          {error && <div className="flash error">{error}</div>}
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name / institution</th>
                  <th>Qualification</th>
                  <th>Credential ID</th>
                  <th>Verification code</th>
                  <th>Status</th>
                  {!isStudent && <th></th>}
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    <td>
                      <PersonCell name={row.holderName} subtitle={row.issuingInstitution} />
                    </td>
                    <td>
                      <div className="person-name">{row.title}</div>
                      <div className="person-sub">{row.type}</div>
                    </td>
                    <td>
                      <CopyId value={row.credentialId} />
                    </td>
                    <td>
                      <CopyId value={row.verificationCode} />
                    </td>
                    <td>
                      <span className={`badge ${row.status}`}>{row.status}</span>
                    </td>
                    {!isStudent && (
                      <td>
                        <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
                          <Link className="row-action button" to="/verify">
                            View
                          </Link>
                          {(session?.role === "ADMIN" || session?.role === "ISSUER") && row.status !== "REVOKED" && (
                            <button type="button" className="button ghost" onClick={() => handleRevoke(row.id)}>
                              Revoke
                            </button>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {rows.length === 0 && (
            <p className="muted">
              {isStudent
                ? "No qualifications are linked to this student account yet."
                : "No results yet. Search by name or code."}
            </p>
          )}
        </section>

        {!isStudent && (
          <aside className="surface page-side">
            <h3>Verification workflow</h3>
            <ul className="task-list">
              <li>Search by candidate name to shortlist likely matches.</li>
              <li>Use credential code or ID for a final authenticity check.</li>
              <li>Revoke outdated records to keep employer checks accurate.</li>
            </ul>
            <div className="quick-actions">
              <Link className="button" to="/search-name">
                Search candidates
              </Link>
              <Link className="button ghost" to="/verify">
                Open verifier
              </Link>
            </div>
          </aside>
        )}
      </div>
    </div>
  );
}
