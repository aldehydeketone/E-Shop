import axios from "axios";

const apiUrl = import.meta.env.VITE_API_URL?.replace(/\/$/, "");

if (!apiUrl) {
    console.error("VITE_API_URL is not configured. API requests will be unavailable.");
}

const api = axios.create({
    baseURL: apiUrl ? `${apiUrl}/api` : "/api",
    withCredentials: true,
});

// Attach JWT from localStorage as Bearer token on every request.
// The backend AuthTokenFilter supports both Cookie and Authorization header.
// For cross-origin requests (frontend on :5173, backend on :8080),
// cookies are not automatically sent, so we use the Bearer token approach.
api.interceptors.request.use((config) => {
    const auth = JSON.parse(localStorage.getItem("auth") || "{}");
    if (auth?.jwtToken) {
        config.headers["Authorization"] = `Bearer ${auth.jwtToken}`;
    }
    return config;
});

export default api;
