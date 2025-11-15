import axios from "axios";
import {ElMessage} from "element-plus";
import {TokenManager} from "./TokenManager.js";
import {API_URL} from "./config.js";
// 创建 axios 实例
const request = axios.create({
    baseURL: API_URL || "/api",
    timeout: 10000,
    headers: {
        "Content-Type": "application/json;charset=utf-8",
    },
});

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        const token = TokenManager.getToken();
        if (token) {
            config.headers.Authorization = token;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        const {response} = error;

        if (response) {
            switch (response.status) {
                case 401:
                    // token 失效
                    TokenManager.removeToken();
                    window.location.href = "/login";
                    break;
                case 403:
                    ElMessage.error("权限不足");
                    break;
                case 500:
                    ElMessage.error("服务器错误");
                    break;
                default:
                    ElMessage.error(response.data?.message || "请求失败");
            }
        } else {
            ElMessage.error("网络错误，请检查网络连接");
        }

        return Promise.reject(error);
    }
);

export default request;
