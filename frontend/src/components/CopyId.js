export function CopyId({ value }) {
  async function copy() {
    try {
      await navigator.clipboard.writeText(value);
    } catch {
      window.prompt("Copy credential ID", value);
    }
  }
  return (
    <span className="copy-id">
      <code>{value}</code>
      <button type="button" className="ghost" onClick={copy}>
        Copy
      </button>
    </span>
  );
}
