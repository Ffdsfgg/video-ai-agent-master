import request from "../utils/request";
//用户登录
export const adminLogin = (data) => request.post('/user/admin',data);

//分页查询用户列表
export const getUserList = (params) => request.get('/user/page', { params })

// 获取个人信息
export const getUserInfo = () => request.get('/user/profile')

// 更新个人信息
export const updateUserInfo = (data) => request.put('/user/profile', data)