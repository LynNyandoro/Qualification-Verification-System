export function initials(name = "") {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) {
    return "Q";
  }
  return parts
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

export function PersonCell({ name, subtitle }) {
  return (
    <div className="person">
      <div className="avatar">{initials(name)}</div>
      <div>
        <div className="person-name">{name}</div>
        {subtitle && <div className="person-sub">{subtitle}</div>}
      </div>
    </div>
  );
}
