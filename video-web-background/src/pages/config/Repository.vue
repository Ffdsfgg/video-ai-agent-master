<template>
  <div class="knowledge-base">
    <!-- 上传区域 -->
    <el-card>
      <el-upload class="upload-area" drag multiple :auto-upload="false" :file-list="fileList"
        accept=".md,.txt,.docx,.pdf" :on-change="handleChange" :on-remove="handleRemove">
        <el-icon size="48"><upload-filled /></el-icon>
        <div class="upload-tip">拖拽或点击上传文件</div>
        <div class="upload-tip-sub">支持 .md .txt 单文件 ≤ 10MB</div>
      </el-upload>

      <el-button type="primary" style="margin-top: 12px" :disabled="!canUpload" @click="doUpload">
        开始上传
      </el-button>
    </el-card>

    <!-- 已上传文件列表 -->
    <el-card style="margin-top: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>已上传文件</span>
          <el-input v-model="keyword" placeholder="搜索文件名" clearable style="width: 240px" @input="handleSearch" />
        </div>
      </template>

      <el-table :data="filteredTableData" stripe>
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="文件名" width="250" />
        <el-table-column prop="type" label="类型" width="250" />
        <el-table-column prop="size" label="大小" width="250">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="250">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="preview(row)">预览</el-button>
            <el-button size="small" @click="download(row)">下载</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
        layout="prev, pager, next, sizes, jumper" style="margin-top: 16px; justify-content: flex-end"
        @current-change="handlePageChange" @size-change="handleSizeChange" />
    </el-card>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="700px" top="5vh">
      <div v-if="previewType === 'md' || previewType === 'txt'" class="preview-content">
        <pre>{{ previewContent }}</pre>
      </div>
      <div v-else class="preview-content">
        <p>暂不支持在线预览 {{ previewType.toUpperCase() }} 文件，请下载后查看。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getknowledge, deleteknowledge, addknowledge } from '@/api/knowledge'
import { uploadFile } from '@/api/file'
import { IMG_URL } from '@/utils/config'

// 上传列表和搜索 
const fileList = ref([])
const keyword = ref('')
const page = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])
const allTableData = ref([]) // 存储所有数据用于本地搜索

// 预览 
const previewVisible = ref(false)
const previewContent = ref('')
const previewType = ref('')

// 计算属性：过滤后的表格数据
const filteredTableData = computed(() => {
  let filtered = allTableData.value
  // 根据关键字过滤
  if (keyword.value) {
    filtered = filtered.filter(item => 
      item.name && item.name.toLowerCase().includes(keyword.value.toLowerCase())
    )
  }
  // 更新总数量
  page.total = filtered.length
  // 分页处理
  const start = (page.current - 1) * page.size
  return filtered.slice(start, start + page.size)
})

// 计算属性：是否可以上传
const canUpload = computed(() => {
  return fileList.value && fileList.value.length > 0
})

// 时间格式化函数
function formatTime(timestamp) {
  if (!timestamp) return '-'
  // 处理时间戳
  const date = new Date(parseInt(timestamp))
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-')
}

/* 读取数据 */
async function loadData() {
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000 // 获取所有数据用于本地搜索
    }

    const res = await getknowledge(params)
    if (res.code === 200) {
      // 检查返回的数据结构
      if (Array.isArray(res.data)) {
        // 如果是数组格式，直接使用
        allTableData.value = res.data
        tableData.value = res.data
        page.total = res.data.length
      } else if (res.data.records) {
        // 如果是分页格式
        allTableData.value = res.data.records
        tableData.value = res.data.records
        page.total = res.data.total || res.data.records.length
      } else {
        allTableData.value = []
        tableData.value = []
        page.total = 0
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
    console.error(error)
  }
}

// 获取当前最大ID
function getCurrentMaxId() {
  if (allTableData.value.length === 0) return 0;
  
  // 从所有数据中提取ID并转换为数字
  const ids = allTableData.value.map(item => {
    const idNum = Number(item.id);
    return isNaN(idNum) ? 0 : idNum;
  });
  
  // 返回最大ID
  return Math.max(...ids);
}

// 处理搜索
function handleSearch() {
  page.current = 1 // 搜索时重置到第一页
}

// 处理分页变化
function handlePageChange(newPage) {
  page.current = newPage
}

// 处理页面大小变化
function handleSizeChange(newSize) {
  page.size = newSize
  page.current = 1
}

// 文件 change 事件
function handleChange(uploadFile) {
  console.log('文件改变:', uploadFile)
  const { raw } = uploadFile
  if (!raw) return
  
  const ext = raw.name.split('.').pop().toLowerCase()
  const allow = ['md', 'txt']
  if (!allow.includes(ext)) {
    ElMessage.error('不支持的文件类型')
    return
  }
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }
  
  // 检查是否已存在相同文件名
  const exists = fileList.value.some(f => f.name === raw.name)
  if (exists) {
    ElMessage.error('文件名已存在')
    return
  }
  
  // 添加到文件列表
  fileList.value.push(raw)
  console.log('当前文件列表:', fileList.value)
}

// 移除文件
function handleRemove(file) {
  console.log('移除文件:', file)
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
  console.log('移除后文件列表:', fileList.value)
}

// 开始上传
async function doUpload() {
  console.log('开始上传，文件列表:', fileList.value)
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  try {
    ElMessage.info('上传中...')

    // 获取当前最大ID
    const maxId = getCurrentMaxId();
    console.log('当前最大ID:', maxId)
    
    // 创建文件列表的副本进行遍历
    const filesToUpload = [...fileList.value]
    
    for (let i = 0; i < filesToUpload.length; i++) {
      const rawFile = filesToUpload[i]
      console.log('正在上传文件:', rawFile.name)

      try {
        // 先上传文件到文件服务器
        const uploadRes = await uploadFile(rawFile)
        console.log('文件上传响应:', uploadRes)
        
        // 修复：确保获取到正确的文件路径字符串
        let fileUrl = ''
        if (typeof uploadRes === 'string') {
          fileUrl = uploadRes
        } else if (uploadRes && uploadRes.data) {
          // 处理可能的响应结构
          if (typeof uploadRes.data === 'string') {
            fileUrl = uploadRes.data
          } else if (uploadRes.data.url) {
            fileUrl = uploadRes.data.url
          } else {
            // 尝试直接使用返回对象
            fileUrl = JSON.stringify(uploadRes.data)
          }
        } else {
          throw new Error('文件上传返回格式错误')
        }

        // 2. 保存知识库记录到数据库
        // 重要：自动生成ID（最后一个ID+1）
        const newId = maxId + i + 1; // 从maxId+1开始递增
        
        const knowledgeData = {
          id: newId,  // 使用新生成的ID
          name: rawFile.name,
          localDiskUrl: fileUrl, // 确保是字符串
          type: rawFile.name.split('.').pop().toUpperCase(), // 文件类型
          size: rawFile.size, // 文件大小
          userId: 1, // 假设用户ID为1
          createTime: new Date().getTime() // 添加当前时间戳
        }

        const res = await addknowledge(knowledgeData)
        if (res.code !== 200) {
          ElMessage.error(`${rawFile.name} 保存记录失败: ${res.message}`)
        } else {
          ElMessage.success(`${rawFile.name} 上传成功 (ID: ${newId})`)
        }
      } catch (uploadError) {
        console.error('文件上传错误:', uploadError)
        ElMessage.error(`${rawFile.name} 上传失败: ${uploadError.message || uploadError}`)
      }
    }

    fileList.value = []
    ElMessage.success('上传完成')
    loadData() // 重新加载列表
  } catch (error) {
    ElMessage.error('上传失败')
    console.error('上传过程错误:', error)
  }
}

// 删除 
function handleDelete(row) {
  ElMessageBox.confirm('确定删除该文件？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await deleteknowledge(row.id)
      if (res.code === 200) {
        ElMessage.success('已删除')
        loadData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  })
}

// 预览 
async function preview(row) {
  try {
    previewType.value = row.name.split('.').pop().toLowerCase()

    if (previewType.value === 'txt' || previewType.value === 'md') {
      // 构造文件完整URL
      const fileUrl = IMG_URL + row.localDiskUrl
      const response = await fetch(fileUrl)
      const text = await response.text()
      previewContent.value = text
      previewVisible.value = true
    } else {
      previewContent.value = ''
      previewVisible.value = true
    }
  } catch (error) {
    ElMessage.error('预览失败')
    console.error(error)
  }
}

// 下载 
function download(row) {
  try {
    // 构造文件完整URL
    const fileUrl = IMG_URL + row.localDiskUrl
    const a = document.createElement('a')
    a.href = fileUrl
    a.download = row.name
    a.click()
  } catch (error) {
    ElMessage.error('下载失败')
    console.error(error)
  }
}

// 格式化大小 
function formatSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

onMounted(loadData)
</script>

<style scoped>
.knowledge-base {
  padding: 16px;
}

.upload-area {
  text-align: center;
  padding: 20px 0;
}

.upload-tip {
  font-size: 16px;
  margin-top: 8px;
  color: #606266;
}

.upload-tip-sub {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.preview-content {
  max-height: 60vh;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

:deep(.el-upload-dragger) {
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

:deep(.el-upload-dragger:hover) {
  border-color: var(--el-color-primary);
  background-color: #f0f9ff;
}

:deep(.el-upload-dragger .el-upload__text) {
  color: #606266;
}
</style>