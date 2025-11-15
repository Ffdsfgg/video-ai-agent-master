import {get, post,put} from "@/utils/requet.js";

//用户登录
export const userLogin = (data) => post('/user',data);

//用户注册
export const register = (data) => post('/user/register',data);

//获取视频信息
export const getVideoInfo = (id) => post(`/videoInfo/${id}`);


//获取验证码图片
export const getCodeImg = () => get('/utils/captcha');

//校验验证码Param
export const getCodeImgValidate = (token,code) => get(`/utils/captcha/validate?token=${token}&code=${code}`);

//角色验证
export const roleValidate = () => get('/user/role/validate');

// 获取个人信息
export const getUserInfo = () => get('/user/profile')

// 更新个人信息（字段透传）
export const updateUserInfo = (data) => put('/user/profile', data)

// 修改密码
export const updatePassword = (data) => put('/user/password', data)