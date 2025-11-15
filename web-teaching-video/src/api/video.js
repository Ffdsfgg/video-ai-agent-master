import {get, post,del} from "../utils/requet.js";
import {API_URL} from "@/utils/config.js";
//上传文件
export const uploadFileRequest = (filePath) => {
    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: `${API_URL}/file`, // 替换为你的后端接口地址
            filePath: filePath, 
            name: "file", 
            success: (res) => {
                try {
                    const data = JSON.parse(res.data);
                    resolve(data); // 成功返回数据
                } catch (e) {
                    reject("响应解析失败");
                }
            },
            fail: (err) => {
                reject(err); // 上传失败
            },
        });
    });
};

//上传视频信息
export const uploadVideoRequest = (data) => post('/video', data);

//获取视频列表
export const getVideo = () => get('/video');


//根据id获取视频详情
export const getVideoById = (id) => get(`/video/${id}`);

//查询公开视频
export const getPublicVideo = () => get('/video/public');

//删除视频
export const delVideo = (id) => del(`/video/${id}`);

//修改视频
export const updateVideo = (id, data) => post('/video/update', data);

//获取已经公开的视频
export const getVideoListOpen = () => get('/video/open');