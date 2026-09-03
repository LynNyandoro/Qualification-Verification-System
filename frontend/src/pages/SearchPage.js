import { useState } from "react";
import api from "../api";

export default function SearchPage() {
  const [filters, setFilters] = useState({ holderName: "", title: "", institution: "", status: "" });
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    try {
      const { data } = await api.get("/qualifications", { params: { ...filters, status: filters.status || undefined } });
      setRows(data);
    } catch (err) {
      setError(err.response?.data?.error || "Search failed");
    }
  }

  return (
    <div>
      <div className="kicker">Records</div>
      <h1>Search qualifications</h1>
      <form className="panel" onSubmit={onSubmit}>
        <div className="row">
          <label className="field">
            Holder name
            <input value={filters.holderName} onChange={(e) => setFilters({ ...filters, holderName: e.target.value })} />
          </label>
          <label className="field">
            Title
            <input value={filters.title} onChange={(e) => setFilters({ ...filters, title: e.target.value })} />
          </label>
        </div>
        <div className="row">
          <label className="field">
            Institution
            <input value={filters.institution} onChange={(e) => setFilters({ ...filters, institution: e.target.value })} />
          </label>
          <label className="field">
            Status
            <select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
              <option value="">Any</option>
              <option>ACTIVE</option>
              <option>REVOKED</option>
              <option>EXPIRED</option>
            </select>
          </label>
        </div>
        <button type="submit">Search</button>
      </form>
      {error && <div className="flash error">{error}</div>}
      <div className="panel" style={{ marginTop: 16 }}>
        <table>
          <thead>
            <tr>
              <th>Holder</th>
              <th>Title</th>
              <th>Institution</th>
              <th>Code</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>{row.holderName}</td>
                <td>{row.title}</td>
                <td>{row.issuingInstitution}</td>
                <td>{row.verificationCode}</td>
                <td>
                  <span className={`badge ${row.status}`}>{row.status}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {rows.length === 0 && <p className="muted">No results yet. Run a search to retrieve records.</p>}
      </div>
    </div>
  );
}
