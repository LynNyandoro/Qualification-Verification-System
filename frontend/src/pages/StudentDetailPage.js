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
    </div>
  );
}
