import request from "../utils/request";


//分页字幕查询
export const getSubtitlesList = (params) => request.get('/subtitles/page/list', { params })

//修改字幕信息
export const UpdatesSubtitles= (id,data) => request.put(`/subtitles/${id}`, data)

//增加字幕信息
export const addSubtitles= (data) => request.post('/subtitles', data)

//删除字幕信息
export const deleteSubtitles= (id) => request.delete(`/subtitles/${id}`)