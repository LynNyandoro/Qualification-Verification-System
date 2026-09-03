import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth";

export default function Layout() {
  const { session, setSession } = useAuth();
  const canIssue = session?.role === "ADMIN" || session?.role === "ISSUER";

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">QVS</div>
        <NavLink className="nav-link" to="/" end>
          Dashboard
        </NavLink>
        {canIssue && (
          <NavLink className="nav-link" to="/qualifications/new">
            Register qualification
          </NavLink>
        )}
        <NavLink className="nav-link" to="/search">
          Search records
        </NavLink>
        <NavLink className="nav-link" to="/verify">
          Verify authenticity
        </NavLink>
        <NavLink className="nav-link" to="/audit">
          Audit history
        </NavLink>
        <div className="session-box">
          <div>{session?.fullName}</div>
          <div>{session?.role}</div>
          <button className="secondary" style={{ marginTop: 12 }} onClick={() => setSession(null)}>
            Sign out
          </button>
        </div>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
