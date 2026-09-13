import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../api";
import { CopyId } from "../components/CopyId";
import { PersonCell } from "../components/PersonCell";

export default function StudentDetailPage() {
  const { id } = useParams();
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get(`/students/${id}`)
      .then((res) => setDetail(res.data))
      .catch((err) => setError(err.response?.data?.error || "Unable to load student"));
  }, [id]);

  if (error) {
    return <div className="flash error">{error}</div>;
  }
  if (!detail) {
    return <p className="muted">Loading…</p>;
  }

  const student = detail.student;
  const activeCount = detail.credentials.filter((row) => row.status === "ACTIVE").length;
  const revokedCount = detail.credentials.filter((row) => row.status === "REVOKED").length;

  return (
    <div>
      <div className="page-head">
        <div>
          <p className="muted">
            <Link to="/students">Students</Link>
          </p>
          <h1>{student.fullName}</h1>
          <p className="muted">
            {student.username} · {student.institutionName} · {student.studentStage}
          </p>
        </div>
      </div>

      <div className="stats">
        <div className="stat">
          <div className="muted">Credentials</div>
          <h2>{detail.credentials.length}</h2>
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
          <div className="muted">Student stage</div>
          <h3>{student.studentStage}</h3>
        </div>
      </div>

      <div className="page-grid">
        <section className="surface page-main">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Credential ID</th>
                  <th>Verification code</th>
                  <th>Qualification</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {detail.credentials.map((row) => (
                  <tr key={row.id}>
                    <td>
                      <CopyId value={row.credentialId} />
                    </td>
                    <td>
                      <CopyId value={row.verificationCode} />
                    </td>
                    <td>
                      <PersonCell name={row.title} subtitle={row.type} />
                    </td>
                    <td>
                      <span className={`badge ${row.status}`}>{row.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {detail.credentials.length === 0 && <p className="muted">No credentials have been awarded yet.</p>}
        </section>

        <aside className="surface page-side">
          <h3>Student profile</h3>
          <p className="person-name">{student.fullName}</p>
          <p className="muted">
            {student.username} · {student.institutionName}
          </p>
          <ul className="task-list">
            <li>Share either credential ID or verification code with employers.</li>
            <li>Use Search and Verify pages for authenticity checks.</li>
            <li>Revoke outdated credentials from registrar workflows when needed.</li>
          </ul>
          <div className="quick-actions">
            <Link className="button ghost" to="/search">
              Open search
            </Link>
            <Link className="button ghost" to="/verify">
              Open verify
            </Link>
          </div>
        </aside>
      </div>
    </div>
  );
}
