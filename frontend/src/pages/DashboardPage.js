import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import { CopyId } from "../components/CopyId";

export default function DashboardPage() {
  const { session } = useAuth();
  const isStudent = session?.role === "STUDENT";
  const [report, setReport] = useState(null);
  const [records, setRecords] = useState([]);

  useEffect(() => {
    api.get("/qualifications").then((res) => setRecords(res.data)).catch(() => setRecords([]));
    if (session?.role === "ADMIN" || session?.role === "VERIFIER") {
      api.get("/reports/verification").then((res) => setReport(res.data)).catch(() => setReport(null));
    }
  }, [session?.role]);

  const mine = records[0];

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Welcome, {session?.fullName}</h1>
      {isStudent ? (
        <p className="muted">
          {session?.studentStage === "ENROLLED" && "You are newly enrolled. A qualification will be awarded when you complete your programme."}
          {session?.studentStage === "GRADUATING" && "You are finishing this year. Your registrar will assign a verification code when the award is confirmed."}
          {session?.studentStage === "ALUMNI" && "You have completed study. Copy a credential ID and send it to an employer."}
          {!session?.studentStage && "View qualifications issued to you and share the verification code with employers."}
        </p>
      ) : session?.role === "VERIFIER" ? (
        <p className="muted">Search an employee by name or by qualification code, then confirm authenticity.</p>
      ) : (
        <p className="muted">
          Enrol students at your institution, award credentials to graduating students and alumni, and keep an audit trail.
        </p>
      )}
        </div>
      </div>
      <div className="stats">
        <div className="stat panel">
          <div className="muted">{isStudent ? "My records" : "Stored records"}</div>
          <h2>{records.length}</h2>
        </div>
        {!isStudent && (
          <>
            <div className="stat panel">
              <div className="muted">Verification checks</div>
              <h2>{report?.totalChecks ?? "—"}</h2>
            </div>
            <div className="stat panel">
              <div className="muted">Valid outcomes</div>
              <h2>{report?.valid ?? "—"}</h2>
            </div>
          </>
        )}
      </div>
      <div className="panel">
        {isStudent ? (
          <>
            <h3>Share with a verifier</h3>
            {mine ? (
              <div>
                <p>Share either identifier with an employer ({mine.title}).</p>
                <p className="muted">Credential ID</p>
                <CopyId value={mine.credentialId} />
                <p className="muted">Verification code</p>
                <CopyId value={mine.verificationCode} />
                <p className="muted">They paste either value in Search or Verify.</p>
              </div>
            ) : (
              <p>
                {session?.studentStage === "ENROLLED"
                  ? "No award yet — you have just started."
                  : session?.studentStage === "GRADUATING"
                    ? "No award yet — your institution still needs to assign the credential."
                    : "No credential is linked to this student account yet."}
              </p>
            )}
            <Link className="button" to="/search">
              Open my qualifications
            </Link>
          </>
        ) : (
          <>
            <h3>Quick start</h3>
            <p>
              Demo: credential ID <strong>MISM-MSU-0001</strong> and verification code <strong>QVS-DEMO12345</strong>
              (Amina Chikomo, MISM, Midlands State University). Student login: <strong>student / Student@123</strong>.
              Employers: <strong>econet</strong>, <strong>cbz</strong>, <strong>delta</strong>.
            </p>
            {(session?.role === "ADMIN" || session?.role === "ISSUER") && (
              <Link className="button" to="/qualifications/new">
                Add Credentials
              </Link>
            )}
            <Link className="button" to="/verify" style={{ marginLeft: "8px" }}>
              Run a verification
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
