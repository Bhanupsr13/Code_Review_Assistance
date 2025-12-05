import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

export const analyzeCode = (code, filename = "inline.java") =>
  api.post("/analyze", { code, filename });

export const analyzeFile = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return api.post("/analyze/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const fetchReviews = () => api.get("/reviews");

export const fetchDashboardSummary = () => api.get("/dashboard/summary");

export const fetchRules = () => api.get("/rules");

export const updateRules = (updates) => api.put("/rules", updates);

export const exportReportUrl = (reviewId, format = "html") =>
  `${api.defaults.baseURL.replace(/\/$/, "")}/reviews/${reviewId}/export?format=${encodeURIComponent(format)}`;