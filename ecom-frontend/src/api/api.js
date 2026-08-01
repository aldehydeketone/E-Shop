import axios from "axios";

const api = axios.create({
    baseURL: `${import.meta.env.VITE_BACK_END_URL}/api`,
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