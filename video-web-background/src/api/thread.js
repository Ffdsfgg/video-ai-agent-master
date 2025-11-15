import request from "../utils/request";

// 更新个人信息
export const getThreadPoolStatus  = (data) => request.get('/api/thread-pool/status', data)