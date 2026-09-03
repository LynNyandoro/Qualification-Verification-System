import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";

export default function DashboardPage() {
  const { session } = useAuth();
  const [report, setReport] = useState(null);
  const [records, setRecords] = useState([]);

  useEffect(() => {
    api.get("/qualifications").then((res) => setRecords(res.data)).catch(() => setRecords([]));
    if (session?.role === "ADMIN" || session?.role === "VERIFIER") {
      api.get("/reports/verification").then((res) => setReport(res.data)).catch(() => setReport(null));
    }
  }, [session?.role]);

  return (
    <div>
      <div className="kicker">Overview</div>
      <h1>Welcome, {session?.fullName}</h1>
      <p className="muted">Register, search and verify academic and professional qualifications with a full audit trail.</p>
      <div className="stats">
        <div className="stat panel">
          <div className="muted">Stored records</div>
          <h2>{records.length}</h2>
        </div>
        <div className="stat panel">
          <div className="muted">Verification checks</div>
          <h2>{report?.totalChecks ?? "—"}</h2>
        </div>
        <div className="stat panel">
          <div className="muted">Valid outcomes</div>
          <h2>{report?.valid ?? "—"}</h2>
        </div>
      </div>
      <div className="panel">
        <h3>Quick start</h3>
        <p>
          Demo credential: <strong>QVS-DEMO12345</strong> (Amina Chikomo, MISM, Midlands State University).
        </p>
        <Link className="button" to="/verify">
          Run a verification
        </Link>
      </div>
    </div>
  );
}
