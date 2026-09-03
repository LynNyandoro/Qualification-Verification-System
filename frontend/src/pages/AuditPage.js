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

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Verification audit</h1>
          <p className="muted">Every authenticity check is kept as an append-only history.</p>
        </div>
      </div>
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
    </div>
  );
}
