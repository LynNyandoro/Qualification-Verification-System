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
  const activeRecords = records.filter((row) => row.status === "ACTIVE").length;
  const revokedRecords = records.filter((row) => row.status === "REVOKED").length;
  const expiredRecords = records.filter((row) => row.status === "EXPIRED").length;
  const healthRate =
    report?.totalChecks && report?.valid !== undefined ? Math.round((report.valid / report.totalChecks) * 100) : null;
  const recent = records.slice(0, 4);
  const quickTasks = isStudent
    ? ["Keep your credential ID and verification code ready for employers.", "Open My qualifications to track award status."]
    : [
        "Search by candidate name before running code verification.",
        "Verify questionable records and review the audit log.",
        "Keep revoked credentials updated after disciplinary decisions.",
      ];
  const courseCards = [
    {
      title: isStudent ? "My records" : "Stored records",
      subtitle: "Verified catalog",
      value: records.length || 0,
      progress: Math.min((records.length || 0) * 10, 100),
      tone: "blue",
    },
    {
      title: "Active credentials",
      subtitle: "Ready to verify",
      value: activeRecords,
      progress: records.length ? Math.round((activeRecords / records.length) * 100) : 0,
      tone: "orange",
    },
    {
      title: "Revoked",
      subtitle: "Flagged records",
      value: revokedRecords,
      progress: records.length ? Math.round((revokedRecords / records.length) * 100) : 0,
      tone: "red",
    },
    {
      title: "Expired",
      subtitle: "Renewal required",
      value: expiredRecords,
      progress: records.length ? Math.round((expiredRecords / records.length) * 100) : 0,
      tone: "teal",
    },
  ];
  const chartBars = [24, 38, 72, 58, 36, 44, 50];
  const sideTitle = isStudent ? "Share details" : "Verification readiness";

  return (
    <div className="dashboard-page">
      <div className="page-head">
        <div>
          <h1>Welcome, {session?.fullName}</h1>
          {isStudent ? (
            <p className="muted">
              {session?.studentStage === "ENROLLED" &&
                "You are newly enrolled. A qualification will be awarded when you complete your programme."}
              {session?.studentStage === "GRADUATING" &&
                "You are finishing this year. Your registrar will assign a verification code when the award is confirmed."}
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

      <div className="dashboard-layout">
        <div className="dashboard-main">
          <section className="surface">
            <div className="surface-head">
              <h3>{isStudent ? "Your credentials" : "Credential lanes"}</h3>
              <Link className="text-link" to="/search">
                View all
              </Link>
            </div>
            <div className="course-grid">
              {courseCards.map((card) => (
                <article key={card.title} className={`course-card ${card.tone}`}>
                  <div className="course-title">{card.title}</div>
                  <div className="course-sub">{card.subtitle}</div>
                  <div className="course-progress">
                    <span style={{ width: `${card.progress}%` }} />
                  </div>
                  <div className="course-foot">
                    <strong>{card.value}</strong>
                    <span>{card.progress}%</span>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <div className="dashboard-split">
            <section className="surface">
              <div className="surface-head">
                <h3>New activity</h3>
              </div>
              {recent.length > 0 ? (
                <div className="activity-list">
                  {recent.map((row) => (
                    <article key={row.id} className="activity-item">
                      <div>
                        <p className="person-name">{row.holderName}</p>
                        <p className="person-sub">
                          {row.title} · {row.issuingInstitution}
                        </p>
                      </div>
                      <span className={`badge ${row.status}`}>{row.status}</span>
                    </article>
                  ))}
                </div>
              ) : (
                <p className="muted">No qualifications loaded yet.</p>
              )}
            </section>

            <section className="surface">
              <div className="surface-head">
                <h3>Statistics</h3>
              </div>
              <p className="stat-big">{report?.totalChecks ?? records.length ?? 0}</p>
              <p className="muted">Checks logged this period</p>
              <div className="mini-chart" aria-hidden="true">
                {chartBars.map((height, idx) => (
                  <span key={idx} style={{ height: `${height}%` }} />
                ))}
              </div>
              <div className="mini-labels">
                <span>Mon</span>
                <span>Tue</span>
                <span>Wed</span>
                <span>Thu</span>
                <span>Fri</span>
                <span>Sat</span>
                <span>Sun</span>
              </div>
            </section>
          </div>
        </div>

        <aside className="dashboard-side">
          <section className="surface">
            <div className="surface-head">
              <h3>Upcoming group tasks</h3>
            </div>
            <div className="upcoming-list">
              {quickTasks.map((task) => (
                <article key={task} className="upcoming-item">
                  <p>{task}</p>
                  <div className="upcoming-meta">
                    <span className="dot-users" />
                    <span>Priority</span>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section className="surface">
            <div className="surface-head">
              <h3>{sideTitle}</h3>
            </div>
            {isStudent ? (
              <>
                {mine ? (
                  <div>
                    <p>Share either identifier with an employer ({mine.title}).</p>
                    <p className="muted">Credential ID</p>
                    <CopyId value={mine.credentialId} />
                    <p className="muted">Verification code</p>
                    <CopyId value={mine.verificationCode} />
                    <p className="muted">They can paste either value in Search or Verify.</p>
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
              </>
            ) : (
              <>
                <p className="muted">Verification quality rate</p>
                <h2>{healthRate !== null ? `${healthRate}%` : "—"}</h2>
                <div className="progress-line">
                  <span style={{ width: `${healthRate ?? 0}%` }} />
                </div>
                <p className="muted">Calculated from valid outcomes over total verification checks.</p>
              </>
            )}
          </section>

          <section className="surface">
            <div className="surface-head">
              <h3>Quick actions</h3>
            </div>
            <div className="quick-actions">
              {(session?.role === "ADMIN" || session?.role === "ISSUER") && (
                <Link className="button" to="/qualifications/new">
                  Add credential
                </Link>
              )}
              {isStudent && (
                <Link className="button" to="/search">
                  Open my qualifications
                </Link>
              )}
              {!isStudent && (
                <Link className="button" to="/search-name">
                  Search candidate
                </Link>
              )}
              {!isStudent && (
                <Link className="button ghost" to="/ai-insights">
                  AI insights
                </Link>
              )}
              <Link className="button ghost" to="/verify">
                Run verification
              </Link>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}
