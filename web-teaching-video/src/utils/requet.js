import { API_URL } from "@/utils/config.js";

const httpInterceptor = {
    invoke(options) {
        if (!options.url.startsWith("http")) {
            options.url = API_URL + options.url;
        }
        if (!options.timeout) {
            options.timeout = 360000;
        }
        if (!options.header) {
            options.header = {};
        }
        options.header["Content-Type"] = "application/json";

        const token = uni.getStorageSync("token");
        if (token) {
            options.header["Authorization"] = token;
        }

        return options;
    }
};

uni.addInterceptor("request", httpInterceptor);
// 封装统一请求方法
async function request(url, method = "GET", data = {}) {
    try {
        const res = await uni.request({
            url,
            method,
            data
        });

        // 请求成功，检查返回状态码
        const responseData = res.data;

        // 判断是否是 401 状态码（token 失效）
        if (responseData && responseData.code === 401) {
            uni.showToast({ title: "登录已失效", icon: "none" });
            setTimeout(() => {
                uni.navigateTo({
                    url: "/pages/login/login"
                });
            }, 1500);
            throw new Error("token 失效");
        }

        // 返回响应数据
        return responseData;

    } catch (err) {
        // 网络错误或其他异常
        console.error("请求失败:", err);
        uni.showToast({ title: "网络异常", icon: "none" });
        throw err;
    }
}

// 导出 get/post/del 方法
export async function get(url, params = {}) {
    return request(url, "GET", params);
}

export async function post(url, data = {}) {
    return request(url, "POST", data);
}

export async function del(url, data = {}) {
    return request(url, "DELETE", data);
}
export async function put(url, data = {}) {
    return request(url, "PUT", data);
}