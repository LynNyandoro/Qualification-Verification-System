import { useEffect, useState } from "react";
import api from "../api";
import { PersonCell } from "../components/PersonCell";

export default function AuditPage() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get("/audit")
      .then((res) => setRows(res.data))
      .catch((err) => setError(err.response?.data?.error || "Unable to load audit trail"));
  }, []);

  const totalChecks = rows.length;
  const validChecks = rows.filter((row) => row.result === "VALID").length;
  const flaggedChecks = rows.filter((row) => ["REVOKED", "TAMPERED", "NOT_FOUND", "INVALID", "EXPIRED"].includes(row.result)).length;
  const latestMs = rows.reduce((max, row) => {
    const parsed = new Date(row.verifiedAt).getTime();
    return Number.isFinite(parsed) && parsed > max ? parsed : max;
  }, 0);
  const latestLabel = latestMs ? new Date(latestMs).toLocaleString() : "No checks yet";
  const methods = rows.reduce((acc, row) => {
    const key = row.method || "UNKNOWN";
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});
  const methodEntries = Object.entries(methods).sort((a, b) => b[1] - a[1]);

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Verification audit</h1>
          <p className="muted">Every authenticity check is kept as an append-only history.</p>
        </div>
      </div>

      <div className="stats">
        <div className="stat">
          <div className="muted">Total checks</div>
          <h2>{totalChecks}</h2>
        </div>
        <div className="stat">
          <div className="muted">Valid results</div>
          <h2>{validChecks}</h2>
        </div>
        <div className="stat">
          <div className="muted">Flagged results</div>
          <h2>{flaggedChecks}</h2>
        </div>
        <div className="stat">
          <div className="muted">Latest check</div>
          <h3>{latestLabel}</h3>
        </div>
      </div>

      <div className="page-grid">
        <section className="surface page-main">
          {error && <div className="flash error">{error}</div>}
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Actor</th>
                  <th>When</th>
                  <th>Code / ID</th>
                  <th>Method</th>
                  <th>Result</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    <td>
                      <PersonCell name={row.verifiedBy} subtitle={row.method} />
                    </td>
                    <td>{new Date(row.verifiedAt).toLocaleString()}</td>
                    <td>{row.verificationCode || row.credentialId}</td>
                    <td>{row.method}</td>
                    <td>
                      <span className={`badge ${row.result}`}>{row.result}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {rows.length === 0 && <p className="muted">No verification events recorded yet.</p>}
        </section>

        <aside className="surface page-side">
          <h3>Method usage</h3>
          {methodEntries.length === 0 ? (
            <p className="muted">Method distribution appears after verification events are logged.</p>
          ) : (
            <ul className="task-list">
              {methodEntries.map(([method, count]) => (
                <li key={method}>
                  {method}: <strong>{count}</strong>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>
    </div>
  );
}
