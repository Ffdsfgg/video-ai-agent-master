import request from "../utils/request";

// 获取本地知识库
export const getknowledge  = (data) => request.get('/knowledge', data)

//删除本地知识库
export const deleteknowledge  = (data) => request.delete(`/knowledge/${data}`)

//添加本地知识库
export const addknowledge  = (data) => request.post('/knowledge', data) 

