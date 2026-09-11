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
    }
  }, [isStudent]);

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
    </div>
  );
}
