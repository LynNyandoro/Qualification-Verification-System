import { useEffect, useState } from "react";
import api from "../api";

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
      <div className="kicker">Accountability</div>
      <h1>Verification audit history</h1>
      {error && <div className="flash error">{error}</div>}
      <div className="panel">
        <table>
          <thead>
            <tr>
              <th>When</th>
              <th>Actor</th>
              <th>Code / ID</th>
              <th>Method</th>
              <th>Result</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>{new Date(row.verifiedAt).toLocaleString()}</td>
                <td>{row.verifiedBy}</td>
                <td>{row.verificationCode || row.credentialId}</td>
                <td>{row.method}</td>
                <td>
                  <span className={`badge ${row.result}`}>{row.result}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {rows.length === 0 && <p className="muted">No verification events recorded yet.</p>}
      </div>
    </div>
  );
}
