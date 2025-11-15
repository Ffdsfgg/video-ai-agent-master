<template>
  <div class="video-manage">
    <!-- 顶部搜索 -->
    <el-card>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="文件名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 100px;">
            <el-option label="正常" value="0" />
            <el-option label="异常" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="公开课">
          <el-select v-model="searchForm.isPublic" clearable placeholder="全部" style="width: 100px;">
            <el-option label="是" value="0" />
            <el-option label="否" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频类别">
          <el-select v-model="searchForm.videoType" clearable placeholder="全部" style="width: 120px;">
            <el-option label="Java" value="Java" />
            <el-option label="Python" value="Python" />
            <el-option label="网络" value="网络" />
            <el-option label="数据库" value="数据库" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="前端" value="前端" />
            <el-option label="后端" value="后端" />
            <el-option label="移动开发" value="移动开发" />
            <el-option label="云计算" value="云计算" />
            <el-option label="大数据" value="大数据" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card style="margin-top: 16px">
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="180">
          <template #default="{ row }">
            <el-avatar :size="60" :src="IMG_URL + row.imgUrl" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名称" width="180" />
        <el-table-column prop="videoType" label="类别" width="180" />
        <el-table-column prop="type" label="格式" width="100" />
        <el-table-column label="状态" width="180">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="公开课" width="70">
          <template #default="{ row }">
            <el-tag :type="row.isPublic === 0 ? 'success' : 'info'">
              {{ row.isPublic === 0 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(row)">
              {{ row.status === 0 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
        layout="prev, pager, next, sizes, jumper" style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData" @size-change="loadData" />
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isAdd ? '新增视频' : '编辑视频'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="文件名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>

        <!-- 封面上传：拖拽或点击 -->
        <el-form-item label="封面">
          <el-upload class="cover-uploader" action="#" :show-file-list="false" :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'imgUrl')" drag>
            <img v-if="form.imgUrl" :src="IMG_URL + form.imgUrl" class="cover-img" />
            <div v-else class="cover-placeholder">
              <el-icon size="32"><upload-filled /></el-icon>
              <div>点击或拖拽上传封面</div>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 视频文件上传：拖拽或点击 -->
        <el-form-item label="视频文件" prop="url">
          <el-upload class="video-uploader" action="#" :show-file-list="false" :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'url')" drag>
            <div v-if="form.url" class="video-file">{{ getFileName(form.url) }}</div>
            <div v-else class="video-placeholder">
              <el-icon size="32"><upload-filled /></el-icon>
              <div>点击或拖拽上传视频</div>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 课件文件上传：拖拽或点击 -->
        <el-form-item label="课件文件" prop="coursewareUrl">
          <el-upload class="courseware-uploader" action="#" :show-file-list="false" :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'coursewareUrl')" drag>
            <div v-if="form.coursewareUrl" class="courseware-file">{{ getFileName(form.coursewareUrl) }}</div>
            <div v-else class="courseware-placeholder">
              <el-icon size="32"><upload-filled /></el-icon>
              <div>点击或拖拽上传课件</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="时长" prop="duration">
          <el-input v-model="form.duration" placeholder="如 12:34" />
        </el-form-item>

        <el-form-item label="视频格式" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="mp4" value="mp4" />
            <el-option label="avi" value="avi" />
            <el-option label="mov" value="mov" />
            <el-option label="wmv" value="wmv" />
            <el-option label="flv" value="flv" />
            <el-option label="mkv" value="mkv" />
            <el-option label="webm" value="webm" />
          </el-select>
        </el-form-item>

        <el-form-item label="视频类别" prop="videoType">
          <el-select v-model="form.videoType" style="width: 100%">
            <el-option label="Java" value="Java" />
            <el-option label="Python" value="Python" />
            <el-option label="网络" value="网络" />
            <el-option label="数据库" value="数据库" />
            <el-option label="人工智能" value="人工智能" />
            <el-option label="前端" value="前端" />
            <el-option label="后端" value="后端" />
            <el-option label="移动开发" value="移动开发" />
            <el-option label="云计算" value="云计算" />
            <el-option label="大数据" value="大数据" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="0" inactive-value="1" active-text="正常" inactive-text="异常" />
        </el-form-item>

        <el-form-item label="公开课">
          <el-switch v-model="form.isPublic" active-value="0" inactive-value="1" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" rows="4" :maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getVideoList, updateVideo } from '@/api/video'
import { uploadFile } from '@/api/file'
import { IMG_URL } from '@/utils/config'

// 获取文件名的工具函数
function getFileName(url) {
  if (typeof url !== 'string') return '未知文件'
  return url.split('/').pop() || '未知文件'
}

// 时间格式化函数
function formatTime(timestamp) {
  if (!timestamp) return '-'
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

// 文件处理函数
function handleFileChange(file, field) {
  // 将文件对象保存到对应的字段
  if (file.raw) {
    form[field] = file.raw
  }
}

// 搜索表单 
const searchForm = reactive({
  name: '',
  videoType: undefined,
  status: undefined,
  isPublic: undefined
})

/* 分页 */
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})
const tableData = ref([])

// 弹窗表单 
const dialogVisible = ref(false)
const isAdd = ref(false)
const form = reactive({
  id: 0,
  userId: 1,
  name: '',
  url: '',
  type: 'mp4',
  description: '',
  imgUrl: '',
  duration: '',
  status: '0',
  videoType: 'Java',
  coursewareUrl: '',
  isPublic: '0'
})
const formRef = ref()

// 表单校验规则 
const rules = {
  name: [{ required: true, message: '请输入文件名称', trigger: 'blur' }],
  url: [{ required: true, message: '请上传或填写视频地址', trigger: 'blur' }],
  type: [{ required: true, message: '请选择视频格式', trigger: 'change' }],
  videoType: [{ required: true, message: '请选择视频类别', trigger: 'change' }]
}

// 调后端分页接口 
function loadData() {
  const params = {
    name: searchForm.name || undefined,
    videoType: searchForm.videoType || undefined,
    status: searchForm.status !== undefined ? Number(searchForm.status) : undefined,
    isPublic: searchForm.isPublic !== undefined ? Number(searchForm.isPublic) : undefined,
    pageNum: page.current,
    pageSize: page.size
  }

  getVideoList(params).then(res => {
    if (res.code === 200) {
      tableData.value = res.data.records || []
      page.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  })
}

// 重置搜索条件 
function resetSearch() {
  Object.assign(searchForm, {
    name: '',
    videoType: undefined,
    status: undefined,
    isPublic: undefined
  })
  page.current = 1
  loadData()
}

// 搜索按钮 
function handleSearch() {
  page.current = 1
  loadData()
}

// 上传封面 
async function uploadCover(rawFile) {
  try {
    const res = await uploadFile(rawFile) // 调用你封装的 uploadFile
    if (res && res.data) {
      form.imgUrl = res.data
      ElMessage.success('封面上传成功')
      return res.data
    } else {
      ElMessage.error(res.message || '上传失败')
      return null
    }
  } catch (error) {
    ElMessage.error('上传请求失败')
    console.error(error)
    return null
  }
}

// 上传视频 
async function uploadVideo(rawFile) {
  try {
    const res = await uploadFile(rawFile)
    if (res && res.data) {
      form.url = res.data
      // 自动提取文件类型
      const ext = res.data.split('.').pop()?.toLowerCase()
      if (ext) form.type = ext
      ElMessage.success('视频上传成功')
      return res.data
    } else {
      ElMessage.error(res.message || '上传失败')
      return null
    }
  } catch (error) {
    ElMessage.error('上传请求失败')
    console.error(error)
    return null
  }
}

// 上传课件 
async function uploadCourseware(rawFile) {
  try {
    const res = await uploadFile(rawFile)
    if (res && res.data) {
      form.coursewareUrl = res.data
      ElMessage.success('课件上传成功')
      return res.data
    } else {
      ElMessage.error(res.message || '上传失败')
      return null
    }
  } catch (error) {
    ElMessage.error('上传请求失败')
    console.error(error)
    return null
  }
}

// 打开新增弹窗 
function openAdd() {
  isAdd.value = true
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗 
function openEdit(row) {
  isAdd.value = false
  // 深拷贝避免引用修改
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

// 切换状态
async function toggleStatus(row) {
  const newStatus = row.status === 0 ? 1 : 0
  try {
    await ElMessageBox.confirm(
      `确定${newStatus === 0 ? '启用' : '禁用'}该视频吗？`,
      '提示',
      { type: 'warning' }
    )
    // 调用修改接口
    const res = await updateVideo({ id: row.id, status: newStatus })
    if (res.code === 200) {
      row.status = newStatus
      ElMessage.success(`已${newStatus === 0 ? '启用' : '禁用'}`)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // 取消或请求失败
    console.log('操作取消或失败')
  }
}

// 删除视频
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该视频吗？', '提示', { type: 'warning' })
    // 注释掉删除调用，因为API不存在
    // const res = await deleteVideo(row.id)
    ElMessage.warning('删除功能暂未实现')
    return
  } catch (error) {
    console.log('删除取消或失败')
  }
}

// 提交表单
async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    // 处理文件上传
    let imgUrl = form.imgUrl
    let videoUrl = form.url
    let coursewareUrl = form.coursewareUrl

    // 如果是文件对象，则上传文件
    if (form.imgUrl instanceof File) {
      const result = await uploadCover(form.imgUrl)
      if (!result) {
        ElMessage.error('封面上传失败')
        return
      }
      imgUrl = result
    }

    if (form.url instanceof File) {
      const result = await uploadVideo(form.url)
      if (!result) {
        ElMessage.error('视频上传失败')
        return
      }
      videoUrl = result
    }

    if (form.coursewareUrl instanceof File) {
      const result = await uploadCourseware(form.coursewareUrl)
      if (!result) {
        ElMessage.error('课件上传失败')
        return
      }
      coursewareUrl = result
    }

    // 构造提交数据
    const submitData = {
      ...form,
      imgUrl: imgUrl,
      url: videoUrl,
      coursewareUrl: coursewareUrl
    }

    let res
    if (isAdd.value) {
      ElMessage.warning('新增功能暂未实现')
      return
    } else {
      res = await updateVideo(submitData)
    }

    if (res.code === 200) {
      ElMessage.success(isAdd.value ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadData() // 刷新列表
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('请求失败')
    console.error(error)
  }
}

// 弹窗关闭时重置表单 
function resetForm() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 重置 form 数据
  Object.assign(form, {
    id: 0,
    userId: 1,
    name: '',
    url: '',
    type: 'mp4',
    description: '',
    imgUrl: '',
    duration: '',
    status: '0',
    videoType: 'Java',
    coursewareUrl: '',
    isPublic: '0'
  })
}

// 组件挂载后加载第一页数据 
onMounted(loadData)
</script>

<style scoped>
.video-manage {
  padding: 16px;
}

.cover-uploader .cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder,
.video-placeholder,
.courseware-placeholder {
  text-align: center;
  color: #999;
  padding: 24px 0;
}

.cover-uploader,
.video-uploader,
.courseware-uploader {
  width: 100%;
}

.video-file,
.courseware-file {
  padding: 8px;
  font-size: 14px;
  color: #409eff;
  background: #f5f7fa;
  border-radius: 4px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>