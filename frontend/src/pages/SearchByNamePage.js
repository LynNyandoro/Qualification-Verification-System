import { useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import { CopyId } from "../components/CopyId";

export default function SearchByNamePage() {
  const auth = useAuth();
  const session = auth?.session;
  const allowed = session?.role === "ADMIN" || session?.role === "ISSUER" || session?.role === "VERIFIER";
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");

  async function onSubmit(event) {
    event.preventDefault();
    const trimmed = query.trim();
    if (!trimmed) {
      setRows([]);
      setError("Enter a candidate name to search.");
      return;
    }

    setError("");
    try {
      const { data } = await api.get("/qualifications", { params: { q: trimmed } });
      setRows(data);
    } catch (err) {
      setError(err.response?.data?.error || "Search failed");
    }
  }

  if (!allowed) {
    return <p>Only authorized registrars, employers, and administrators can search by candidate name.</p>;
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Search by candidate name</h1>
          <p className="muted">Search for a candidate by name and review matching qualifications or verification details.</p>
        </div>
      </div>

      <form className="panel" onSubmit={onSubmit}>
        <label className="field">
          Candidate name
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Enter candidate name"
          />
        </label>
        <button type="submit">Search</button>
      </form>

      {error && <div className="flash error">{error}</div>}

      {rows.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Candidate</th>
                <th>Qualification</th>
                <th>Credential ID</th>
                <th>Verification code</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>
                    <div className="person-name">{row.holderName}</div>
                    <div className="person-sub">{row.issuingInstitution}</div>
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
                  <td>
                    <Link className="row-action button" to="/verify">
                      Verify
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!error && rows.length === 0 && query.trim() && (
        <p className="muted">No matching qualifications were found for that candidate name.</p>
      )}
    </div>
  );
}
