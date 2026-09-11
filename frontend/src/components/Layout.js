import { NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth";
import { ThemeToggle } from "../theme";

function initials(name) {
  return (name || "U")
    .split(" ")
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

function Icon({ d }) {
  return (
    <svg className="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
      <path d={d} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

const titles = {
  "/": "Dashboard",
  "/admin": "Directory",
  "/students": "Students",
  "/qualifications/new": "Award qualification",
  "/search": "Records",
  "/verify": "Verify",
  "/audit": "Audit",
  "/about": "About Us",
  "/contact": "Contact Us",
};

export default function Layout() {
  const { session, setSession } = useAuth();
  const location = useLocation();
  const canIssue = session?.role === "ADMIN" || session?.role === "ISSUER";
  const canAudit = session?.role !== "STUDENT";
  const canVerify = session?.role !== "STUDENT";
  const recordsLabel = session?.role === "STUDENT" ? "My qualifications" : "Search records";
  const title = location.pathname.startsWith("/students/")
    ? "Student record"
    : titles[location.pathname] || "QVS";
  const isAdmin = session?.role === "ADMIN";

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          QVS
          <span>Zimbabwe</span>
        </div>
        <nav>
          <NavLink className="nav-link" to="/" end>
            <Icon d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1v-9.5z" />
            Dashboard
          </NavLink>
          {isAdmin && (
            <NavLink className="nav-link" to="/admin">
              <Icon d="M4 6h16M4 12h16M4 18h10" />
              Directory
            </NavLink>
          )}
          {canIssue && (
            <NavLink className="nav-link" to="/students">
              <Icon d="M16 19v-1a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v1M12 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM20 19v-1a4 4 0 0 0-3-3.87M16 3.13a3 3 0 0 1 0 5.74" />
              Students
            </NavLink>
          )}
          {canIssue && (
            <NavLink className="nav-link" to="/qualifications/new">
              <Icon d="M12 5v14M5 12h14" />
              Award
            </NavLink>
          )}
          <NavLink className="nav-link" to="/search">
            <Icon d="M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM21 21l-4.3-4.3" />
            {recordsLabel}
          </NavLink>
          {canVerify && (
            <NavLink className="nav-link" to="/verify">
              <Icon d="M9 12l2 2 4-4M12 22c5-2 8-6 8-11V6l-8-3-8 3v5c0 5 3 9 8 11z" />
              Verify
            </NavLink>
          )}
          {canAudit && (
            <NavLink className="nav-link" to="/audit">
              <Icon d="M8 7h12M8 12h12M8 17h8M4 7h.01M4 12h.01M4 17h.01" />
              Audit
            </NavLink>
          )}
          <NavLink className="nav-link" to="/about">
            <Icon d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 15h-2v-2h2zm0-4h-2V7h2z" />
            About Us
          </NavLink>
          <NavLink className="nav-link" to="/contact">
            <Icon d="M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2zm0 2 8 5 8-5" />
            Contact Us
          </NavLink>
        </nav>
        <div className="sidebar-foot">
          <ThemeToggle />
        </div>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <div className="topbar-title">{title}</div>
          <div className="topbar-user">
            <span className="bell" aria-hidden="true" />
            <div className="avatar">{initials(session?.fullName)}</div>
            <div>
              <div className="topbar-name">{session?.fullName}</div>
              <div className="muted tiny">
                {session?.role}
                {session?.institutionName ? ` · ${session.institutionName}` : ""}
              </div>
            </div>
            <button type="button" className="ghost" onClick={() => setSession(null)}>
              Sign out
            </button>
          </div>
        </header>
        <main className="main">
          <div className="content-card">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
