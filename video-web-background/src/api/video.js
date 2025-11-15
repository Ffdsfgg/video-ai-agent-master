import request from "../utils/request";

//分页查询视频列表
export const getVideoList = (params) => request.get('/video/page', { params })

//修改视频
export const updateVideo = (data) => request.put(`/video/update`, data)

//分页查询视频基本信息
export const getVideoInfoList = (params) => request.get('/videoInfo/page/list', { params })

//修改视频基本信息
export const updateVideoInfo = (id,data) => request.put(`/videoInfo/${id}`, data)