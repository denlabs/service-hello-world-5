export default {
  team: {
    name: "agentic-sdlc-team",
    coordinator: "coordinator",
    specialists: [
      "requirements-specialist",
      "planning-specialist",
      "frontend-specialist",
      "backend-specialist",
      "fullstack-specialist",
      "qa-specialist",
      "scribe"
    ]
  },
  memory: {
    enabled: true,
    path: ".squad/memory"
  },
  governance: {
    humanReviewRequiredFor: [
      "backlog-finalisation",
      "compliance-escalation"
    ]
  },
  plugins: [
    "prompt-context-loader",
    "artifact-writer",
    "read-only-tooling-proxy"
  ]
};
