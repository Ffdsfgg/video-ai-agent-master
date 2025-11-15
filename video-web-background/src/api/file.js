// src/api/file.js
import request from '@/utils/request'

/**
 * 通用文件上传（本地磁盘）
 * @param {File} file
 * @returns string 返回路径
 */
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => res.data)
}

/**
 * 删除文件（本地磁盘）
 * @param {string} filename
 * @returns Promise
 */
export const deleteFile = (filename) =>
  request.delete(`/file/${filename}`)