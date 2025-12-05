import React, { useState, useEffect } from "react";
import {
  analyzeCode,
  analyzeFile,
  fetchDashboardSummary,
  fetchRules,
  updateRules,
  exportReportUrl,
} from "./api";
import "./App.css";

function App() {
  const [code, setCode] = useState("// Paste your Java code here\n");
  const [filename, setFilename] = useState("MyClass.java");
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState("");
  const [rules, setRules] = useState(null);
  const [rulesSaving, setRulesSaving] = useState(false);

  useEffect(() => {
    loadDashboard();
    loadRules();
  }, []);

  const loadDashboard = async () => {
    try {
      const res = await fetchDashboardSummary();
      setDashboard(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  const loadRules = async () => {
    try {
      const res = await fetchRules();
      setRules(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleAnalyzeText = async () => {
    if (!code.trim()) {
      setError("Please enter some code.");
      return;
    }
    setError("");
    setLoading(true);
    try {
      const res = await analyzeCode(code, filename);
      setAnalysis(res.data);
      loadDashboard();
    } catch (e) {
      console.error(e);
      setError("Something went wrong while analyzing.");
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;
    setError("");
    setLoading(true);
    try {
      const res = await analyzeFile(file);
      setAnalysis(res.data);
      setFilename(file.name);
      setCode(await file.text());
      loadDashboard();
    } catch (e) {
      console.error(e);
      setError("File upload failed.");
    } finally {
      setLoading(false);
    }
  };

  const getLineClass = (lineNumber) => {
    if (!analysis) return "";
    const issuesOnLine = analysis.issues.filter(
      (issue) => issue.lineNumber === lineNumber
    );
    if (issuesOnLine.length === 0) return "";
    const hasError = issuesOnLine.some((i) => i.category === "ERROR");
    const hasSecurity = issuesOnLine.some((i) => i.category === "SECURITY");
    if (hasError) return "line-error";
    if (hasSecurity) return "line-security";
    return "line-warning";
  };

  const handleToggleRule = (name) => {
    if (!rules) return;
    const updated = { ...rules, [name]: !rules[name] };
    setRules(updated);
  };

  const handleSaveRules = async () => {
    if (!rules) return;
    setRulesSaving(true);
    try {
      await updateRules(rules);
    } catch (e) {
      console.error(e);
    } finally {
      setRulesSaving(false);
    }
  };

  const handleDownloadReport = (format) => {
    if (!analysis || !analysis.reviewId) return;
    const url = exportReportUrl(analysis.reviewId, format);
    window.open(url, "_blank");
  };

  const codeLines = code.split("\n");

  return (
    <div className="app">
      <header className="app-header">
        <h1>Code Review Assistant</h1>
        <p>Analyze Java code for errors, style issues, performance and security.</p>
      </header>

      <section className="dashboard">
        <h2>Dashboard</h2>
        {dashboard ? (
          <div className="dashboard-grid">
            <div className="card">
              <h3>Total Reviews</h3>
              <p>{dashboard.totalReviews}</p>
            </div>
            <div className="card">
              <h3>Total Issues</h3>
              <p>{dashboard.totalIssues}</p>
            </div>
            <div className="card">
              <h3>Errors</h3>
              <p>{dashboard.totalErrors}</p>
            </div>
            <div className="card">
              <h3>Warnings</h3>
              <p>{dashboard.totalWarnings}</p>
            </div>
            <div className="card">
              <h3>Optimizations</h3>
              <p>{dashboard.totalOptimizations}</p>
            </div>
            <div className="card">
              <h3>Security</h3>
              <p>{dashboard.totalSecurityIssues}</p>
            </div>
          </div>
        ) : (
          <p>Loading dashboard...</p>
        )}
      </section>

      <section className="rules-section">
        <h2>Analysis Rules</h2>
        {!rules && <p>Loading rules...</p>}
        {rules && (
          <>
            <div className="rules-list" style={{ display: "grid", gap: 8 }}>
              {Object.entries(rules).map(([name, enabled]) => (
                <label key={name} style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <input
                    type="checkbox"
                    checked={enabled}
                    onChange={() => handleToggleRule(name)}
                  />
                  <span>{name}</span>
                </label>
              ))}
            </div>
            <div style={{ marginTop: 10 }}>
              <button onClick={handleSaveRules} disabled={rulesSaving}>
                {rulesSaving ? "Saving..." : "Save Rules"}
              </button>
            </div>
          </>
        )}
      </section>

      <section className="editor-section">
        <div className="editor-controls">
          <div>
            <label>Filename:</label>
            <input
              value={filename}
              onChange={(e) => setFilename(e.target.value)}
            />
          </div>
          <div>
            <label>Upload Java file:</label>
            <input type="file" accept=".java" onChange={handleFileChange} />
          </div>
          <button onClick={handleAnalyzeText} disabled={loading}>
            {loading ? "Analyzing..." : "Analyze Code"}
          </button>
          {analysis && (
            <>
              <button onClick={() => handleDownloadReport("html")} style={{ marginLeft: 8 }}>
                Download Report (HTML)
              </button>
              <button onClick={() => handleDownloadReport("txt")} style={{ marginLeft: 8 }}>
                Download Report (TXT)
              </button>
            </>
          )}
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="editor-and-issues">
          <div className="code-editor">
            <h2>Code Editor</h2>
            <div className="code-area">
              {codeLines.map((line, index) => (
                <div
                  key={index}
                  className={`code-line ${getLineClass(index + 1)}`}
                >
                  <span className="line-number">{index + 1}</span>
                  <textarea
                    value={line}
                    onChange={(e) => {
                      const copy = [...codeLines];
                      copy[index] = e.target.value;
                      setCode(copy.join("\n"));
                    }}
                  />
                </div>
              ))}
            </div>
          </div>

          <div className="issues-panel">
            <h2>Issues</h2>
            {!analysis && <p>No analysis yet. Run it to see issues.</p>}
            {analysis && analysis.issues.length === 0 && (
              <p>No issues found. Great job!</p>
            )}
            {analysis && analysis.issues.length > 0 && (
              <ul>
                {analysis.issues.map((issue) => (
                  <li key={issue.id || issue.lineNumber}>
                    <strong>
                      [{issue.category}] (Line {issue.lineNumber || "N/A"}){" "}
                      {issue.title}
                    </strong>
                    <p>{issue.description}</p>
                    <p className="suggestion">
                      <b>Suggestion:</b> {issue.suggestion}
                    </p>
                    <p className="severity">
                      Severity:{" "}
                      <span
                        style={{
                          padding: "2px 6px",
                          borderRadius: 4,
                          background:
                            issue.severity === "HIGH"
                              ? "#fde2e1"
                              : issue.severity === "MEDIUM"
                              ? "#fff4ce"
                              : "#e1f3fb",
                          color:
                            issue.severity === "HIGH"
                              ? "#a4262c"
                              : issue.severity === "MEDIUM"
                              ? "#8a6d3b"
                              : "#0b6aa1",
                        }}
                      >
                        {issue.severity}
                      </span>
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {analysis && (
          <div className="summary-report">
            <h2>Summary Report</h2>
            <p>
              <b>File:</b> {analysis.filename} | <b>Review ID:</b>{" "}
              {analysis.reviewId}
            </p>
            <ul>
              <li>Errors: {analysis.errorCount}</li>
              <li>Warnings: {analysis.warningCount}</li>
              <li>Optimizations: {analysis.optimizationCount}</li>
              <li>Security: {analysis.securityCount}</li>
            </ul>
            <p>
            </p>
          </div>
        )}
      </section>
    </div>
  );
}

export default App;
