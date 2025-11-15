// src/utils/TokenManager.js
export const TokenManager = {
    getToken() {
        return localStorage.getItem("video_token");
    },
    setToken(token) {
        localStorage.setItem("video_token", token);
    },
    removeToken() {
        localStorage.removeItem("video_token");
    },
};