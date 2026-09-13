import { useCallback, useEffect, useMemo, useState } from "react";
import api from "../api";

const DEFAULT_PROMPT = "Identify high-risk verification patterns and recommend next actions.";

function toRows(map) {
  return Object.entries(map || {}).sort((a, b) => b[1] - a[1]);
}

function formatDelta(delta) {
  if (typeof delta !== "number") {
    return "—";
  }
  return `${delta > 0 ? "+" : ""}${delta.toFixed(1)} pts`;
}

export default function AgentInsightsPage() {
  const [prompt, setPrompt] = useState(DEFAULT_PROMPT);
  const [maxEvents, setMaxEvents] = useState(120);
  const [insight, setInsight] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const resultRows = useMemo(() => toRows(insight?.resultBreakdown), [insight?.resultBreakdown]);
  const methodRows = useMemo(() => toRows(insight?.methodBreakdown), [insight?.methodBreakdown]);

  const fetchInsight = useCallback(async (analysisPrompt, analysisMaxEvents) => {
    setLoading(true);
    setError("");
    try {
      const payload = {
        prompt: (analysisPrompt || "").trim(),
        maxEvents: Number(analysisMaxEvents) || 120,
      };
      const { data } = await api.post("/ai/verification-insights", payload);
      setInsight(data);
    } catch (err) {
      setError(err.response?.data?.error || "Unable to generate AI insights right now.");
    } finally {
      setLoading(false);
    }
  }, []);

  const runInsight = async (event) => {
    event?.preventDefault();
    await fetchInsight(prompt, maxEvents);
  };

  useEffect(() => {
    fetchInsight(DEFAULT_PROMPT, 120);
  }, [fetchInsight]);

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Agentic AI insights</h1>
          <p className="muted">
            Ask the AI copilot to analyze verification history, detect risk patterns, and suggest investigation actions.
          </p>
        </div>
      </div>

      <div className="stats">
        <div className="stat">
          <div className="muted">Analysed events</div>
          <h2>{insight?.analysedEvents ?? 0}</h2>
        </div>
        <div className="stat">
          <div className="muted">Flagged outcomes</div>
          <h2>{insight?.flaggedEvents ?? 0}</h2>
        </div>
        <div className="stat">
          <div className="muted">Mode</div>
          <h3>{insight?.mode || "—"}</h3>
        </div>
        <div className="stat">
          <div className="muted">Model</div>
          <h3>{insight?.model || "—"}</h3>
        </div>
        <div className="stat">
          <div className="muted">Confidence score</div>
          <h2>{insight?.confidenceScore ?? 0}%</h2>
        </div>
        <div className="stat">
          <div className="muted">Flag trend</div>
          <h3>{insight?.trend?.direction || "—"}</h3>
          <p className="muted tiny">{formatDelta(insight?.trend?.delta)}</p>
        </div>
      </div>

      <div className="page-grid">
        <section className="surface page-main">
          {error && <div className="flash error">{error}</div>}
          <form className="form-card" onSubmit={runInsight}>
            <label className="field">
              Analysis prompt
              <textarea
                rows={4}
                value={prompt}
                onChange={(event) => setPrompt(event.target.value)}
                placeholder="Describe what you want the AI assistant to investigate."
              />
            </label>
            <div className="row">
              <label className="field">
                Max recent events
                <input
                  type="number"
                  min={20}
                  max={250}
                  value={maxEvents}
                  onChange={(event) => setMaxEvents(event.target.value)}
                />
              </label>
              <div className="field">
                Generated
                <input readOnly value={insight?.generatedAt || "Not generated yet"} />
              </div>
            </div>
            <button type="submit" disabled={loading}>
              {loading ? "Generating..." : "Generate insight"}
            </button>
          </form>

          {insight && (
            <section className="surface ai-insight-block">
              <h3>AI summary</h3>
              <p>{insight.summary}</p>
              {insight?.trend?.summary && <p className="muted">{insight.trend.summary}</p>}
              <p className="muted tiny">{insight.note}</p>
            </section>
          )}

          {insight?.detectedRisks?.length > 0 && (
            <section className="surface ai-insight-block">
              <h3>Detected risks</h3>
              <ul className="task-list">
                {insight.detectedRisks.map((risk) => (
                  <li key={risk}>{risk}</li>
                ))}
              </ul>
            </section>
          )}

          {insight?.recommendedActions?.length > 0 && (
            <section className="surface ai-insight-block">
              <h3>Recommended actions</h3>
              <ol className="ordered-list">
                {insight.recommendedActions.map((action) => (
                  <li key={action}>{action}</li>
                ))}
              </ol>
            </section>
          )}
        </section>

        <aside className="surface page-side">
          <h3>Result breakdown</h3>
          {resultRows.length === 0 ? (
            <p className="muted">No result distribution yet.</p>
          ) : (
            <ul className="task-list">
              {resultRows.map(([key, value]) => (
                <li key={key}>
                  {key}: <strong>{value}</strong>
                </li>
              ))}
            </ul>
          )}

          <h3 className="ai-side-title">Method breakdown</h3>
          {methodRows.length === 0 ? (
            <p className="muted">No method distribution yet.</p>
          ) : (
            <ul className="task-list">
              {methodRows.map(([key, value]) => (
                <li key={key}>
                  {key}: <strong>{value}</strong>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>
    </div>
  );
}
