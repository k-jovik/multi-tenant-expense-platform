import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  },
);
export default api;

export function getApiErrorMessage(err: unknown, fallback: string): string {
  if (axios.isAxiosError(err)) {
    // Spring Boot @ControllerAdvice error body typically has `message`
    const body = err.response?.data as
      | { message?: string; error?: string }
      | undefined;
    const serverMessage = body?.message ?? body?.error;
    if (serverMessage) return serverMessage;

    if (err.response?.status === 409) {
      return "This expense was already processed. Refresh the page.";
    }
  }
  return fallback;
}