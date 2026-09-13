import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
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

function TopIcon({ path, label }) {
  return (
    <button type="button" className="top-icon" aria-label={label} title={label}>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d={path} strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </button>
  );
}

const titles = {
  "/dashboard": "Dashboard",
  "/admin": "Directory",
  "/students": "Students",
  "/qualifications/new": "Add credentials",
  "/search": "Records",
  "/search-name": "Search by name",
  "/verify": "Verify",
  "/audit": "Audit",
  "/ai-insights": "AI insights",
  "/about": "About Us",
  "/contact": "Contact Us",
};

function SidebarSection({ label, children }) {
  return (
    <div className="nav-section">
      <p className="nav-section-label">{label}</p>
      {children}
    </div>
  );
}

export default function Layout() {
  const { session, setSession } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const canIssue = session?.role === "ADMIN" || session?.role === "ISSUER";
  const canAudit = session?.role !== "STUDENT";
  const canVerify = session?.role !== "STUDENT";
  const recordsLabel = session?.role === "STUDENT" ? "My qualifications" : "Search records";
  const title = location.pathname.startsWith("/students/")
    ? "Student record"
    : titles[location.pathname] || "QVS";
  const isAdmin = session?.role === "ADMIN";
  const roleLabel = session?.role?.toLowerCase() || "user";

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">Q</span>
          <div>
            EduVerify
            <span>Qualification workspace</span>
          </div>
        </div>
        <nav>
          <SidebarSection label="Dashboard & apps">
            <NavLink className="nav-link" to="/dashboard" end>
              <Icon d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1v-9.5z" />
              Dashboard
            </NavLink>
            <NavLink className="nav-link" to="/search">
              <Icon d="M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM21 21l-4.3-4.3" />
              {recordsLabel}
            </NavLink>
            {canVerify && (
              <NavLink className="nav-link" to="/search-name">
                <Icon d="M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM21 21l-4.3-4.3M11 8v6M8 11h6" />
                Search by name
              </NavLink>
            )}
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
            {canVerify && (
              <NavLink className="nav-link" to="/ai-insights">
                <Icon d="M12 2 2 7v6c0 5.6 4.2 8.5 10 11 5.8-2.5 10-5.4 10-11V7L12 2zm0 7a2.6 2.6 0 1 1 0 5.2A2.6 2.6 0 0 1 12 9zm0 8c-2.2 0-4.2-.8-5.5-2.3.8-1.5 2.4-2.5 4.2-2.5h2.6c1.8 0 3.4 1 4.2 2.5A7.2 7.2 0 0 1 12 17z" />
                AI insights
              </NavLink>
            )}
          </SidebarSection>
          {(isAdmin || canIssue) && (
            <SidebarSection label="Operations">
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
                  Add credentials
                </NavLink>
              )}
            </SidebarSection>
          )}
          <SidebarSection label="Information">
            <NavLink className="nav-link" to="/about">
              <Icon d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm1 15h-2v-2h2zm0-4h-2V7h2z" />
              About Us
            </NavLink>
            <NavLink className="nav-link" to="/contact">
              <Icon d="M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2zm0 2 8 5 8-5" />
              Contact Us
            </NavLink>
          </SidebarSection>
        </nav>
        <div className="sidebar-foot">
          <div className="sidebar-user">
            <div className="avatar">{initials(session?.fullName)}</div>
            <div>
              <div className="topbar-name">{session?.fullName}</div>
              <div className="muted tiny">{roleLabel}</div>
            </div>
          </div>
          <ThemeToggle />
        </div>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <div>
            <div className="topbar-title">Qualification Verification System</div>
            <div className="topbar-heading">{title}</div>
          </div>
          <div className="topbar-actions">
            <div className="topbar-tools">
              <TopIcon path="M9 3H5a2 2 0 0 0-2 2v4M15 3h4a2 2 0 0 1 2 2v4M21 15v4a2 2 0 0 1-2 2h-4M3 15v4a2 2 0 0 0 2 2h4" label="Fullscreen" />
              <TopIcon path="M5 6h14M5 12h14M5 18h14" label="Menu options" />
            </div>
            <label className="top-search">
              <input type="search" placeholder="Search students, credentials or codes..." />
            </label>
            <span className="chip">{session?.role}</span>
            <span className="bell" aria-hidden="true" />
            <div>
              <div className="topbar-name">{session?.fullName}</div>
              <div className="muted tiny topbar-meta">
                {session?.role}
                {session?.institutionName ? ` · ${session.institutionName}` : ""}
              </div>
            </div>
            <button
              type="button"
              className="ghost soft-ghost"
              onClick={() => {
                setSession(null);
                navigate("/login", { replace: true });
              }}
            >
              Sign out
            </button>
          </div>
        </header>
        <main className="main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
