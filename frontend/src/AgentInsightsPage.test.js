import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import AgentInsightsPage from "./pages/AgentInsightsPage";
import api from "./api";

jest.mock("./api");

test("agent insights page requests analysis and renders summary", async () => {
  const payload = {
    data: {
      mode: "RULE_BASED",
      model: "local-heuristics",
      summary: "Analysed 24 recent checks.",
      detectedRisks: ["Detected 1 tampered credential check."],
      recommendedActions: ["Escalate tampered outcomes."],
      resultBreakdown: { VALID: 23, TAMPERED: 1 },
      methodBreakdown: { CODE: 21, CREDENTIAL_ID: 3 },
      analysedEvents: 24,
      flaggedEvents: 1,
      confidenceScore: 87,
      trend: {
        direction: "IMPROVING",
        currentFlagRate: 4.2,
        previousFlagRate: 9.6,
        delta: -5.4,
        summary: "Flagged rate is down by 5.4 points (9.6% -> 4.2%).",
      },
      generatedAt: "2026-09-13T10:45:00Z",
      note: "Local mode",
    },
  };
  api.post.mockResolvedValue(payload);

  render(<AgentInsightsPage />);

  await screen.findByText(/Analysed 24 recent checks/i);
  expect(api.post).toHaveBeenCalledWith(
    "/ai/verification-insights",
    expect.objectContaining({ maxEvents: 120 })
  );
  expect(screen.getByText(/agentic ai insights/i)).toBeInTheDocument();
  expect(screen.getByText(/87%/i)).toBeInTheDocument();
  expect(screen.getByText(/IMPROVING/i)).toBeInTheDocument();

  fireEvent.click(screen.getByRole("button", { name: /generate insight/i }));
  await waitFor(() => expect(api.post).toHaveBeenCalledTimes(2));
});
