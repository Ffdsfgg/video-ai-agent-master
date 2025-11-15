<template>
  <div class="subtitle-manage">
    <!-- 顶部搜索 -->
    <el-card>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="视频 ID">
          <el-input-number v-model="searchForm.videoId" :min="1" clearable />
        </el-form-item>

        <el-form-item label="名称">
          <el-input v-model="searchForm.designation" clearable placeholder="请输入视频名称" style="width: 150px;" />
        </el-form-item>

        <el-form-item label="字幕类型">
          <el-select v-model="searchForm.subtitleType" clearable placeholder="全部" style="width: 100px;">
            <el-option label="VTT" value="VTT" />
          </el-select>
        </el-form-item>
        <el-form-item label="语言">
          <el-select v-model="searchForm.languagType" clearable placeholder="全部" style="width: 100px;">
            <el-option label="中文" value="中文" />
            <el-option label="英文" value="英文" />
            <el-option label="日文" value="日文" />
            <el-option label="法文" value="法文" />
            <el-option label="德文" value="德文" />
            <el-option label="俄文" value="俄文" />
            <el-option label="西班牙文" value="西班牙文" />
            <el-option label="韩文" value="韩文" />
            <el-option label="意大利文" value="意大利文" />
            <el-option label="阿拉伯文" value="阿拉伯文" />
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
        <el-table-column prop="videoId" label="视频 ID" width="90" />
        <el-table-column prop="designation" label="视频名称" width="250" />

        <el-table-column label="字幕文件">
          <template #default="{ row }">
            <el-link v-if="row.subtitleUrl && typeof row.subtitleUrl === 'string'"
              :href="`${IMG_URL}${row.subtitleUrl}`" type="primary" target="_blank" :underline="false">
              {{ getFileName(row.subtitleUrl) }}
            </el-link>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>

        <el-table-column prop="subtitleType" label="类型" width="80" />
        <el-table-column prop="languagType" label="语言" width="80">
          <template #default="{ row }">
            <!-- 修复字段名拼写错误：兼容 languagType 和 languagType -->
            {{ row.languagType || row.languagType || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ dayjs(row.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
        layout="prev, pager, next, sizes, jumper" style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData" @size-change="loadData" />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isAdd ? '新增字幕' : '编辑字幕'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="视频 ID" prop="videoId">
          <el-input-number v-model="form.videoId" :min="1" style="width: 100%" />
        </el-form-item>

        <!-- 字幕文件上传 -->
        <el-form-item label="字幕文件" prop="subtitleUrl">
          <el-upload class="subtitle-uploader" action="#" :show-file-list="false" :before-upload="beforeUploadSubtitle"
            :http-request="uploadSubtitle" drag>
            <div v-if="!form.subtitleUrl || typeof form.subtitleUrl !== 'string'" class="placeholder">
              <el-icon size="32"><upload-filled /></el-icon>
              <div>点击或拖拽上传 .vtt 文件</div>
            </div>
            <div v-else class="file-name">
              {{ getFileName(form.subtitleUrl) }}
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="字幕类型" prop="subtitleType">
          <el-select v-model="form.subtitleType" disabled style="width: 100%">
            <el-option label="VTT" value="VTT" />
          </el-select>
        </el-form-item>

        <el-form-item label="语言" prop="languagType">
          <el-select v-model="form.languagType" style="width: 100%">
            <el-option label="中文" value="中文" />
            <el-option label="英文" value="英文" />
            <el-option label="日文" value="日文" />
            <el-option label="法文" value="法文" />
            <el-option label="德文" value="德文" />
            <el-option label="俄文" value="俄文" />
            <el-option label="西班牙文" value="西班牙文" />
            <el-option label="韩文" value="韩文" />
            <el-option label="意大利文" value="意大利文" />
            <el-option label="阿拉伯文" value="阿拉伯文" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getSubtitlesList, UpdatesSubtitles, addSubtitles, deleteSubtitles } from '@/api/subtitles'
import { uploadFile } from '@/api/file'
import { IMG_URL } from '@/utils/config'
import dayjs from 'dayjs'

// 获取文件名的工具函数
function getFileName(url) {
  if (typeof url !== 'string') return '未知文件'
  return url.split('/').pop() || '未知文件'
}

// 搜索表单 
const searchForm = reactive({
  videoId: undefined,
  subtitleType: undefined,
  languagType: undefined,
  designation: undefined
})

// 分页 
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})
const tableData = ref([])

// 弹窗控制 
const dialogVisible = ref(false)
const isAdd = ref(false)

// 表单数据 
const form = reactive({
  id: 0,
  videoId: 1,
  subtitleUrl: '',
  subtitleType: 'VTT',
  languagType: '中文',
  createTime: '',
  designation: '',
  updateTime: ''
})

const formRef = ref()

// 表单校验规则
const rules = {
  videoId: [{ required: true, message: '请输入视频 ID', trigger: 'blur' }],
  subtitleUrl: [{ required: true, message: '请上传 .vtt 字幕文件', trigger: 'change' }],
  languagType: [{ required: true, message: '请选择语言', trigger: 'change' }]
}

// 上传前校验：仅允许 .vtt 文件 
function beforeUploadSubtitle(file) {
  const extension = file.name.toLowerCase().split('.').pop()
  const isVTT = extension === 'vtt'

  if (!isVTT) {
    ElMessage.error('仅支持上传 .vtt 格式的字幕文件！')
  }

  return isVTT
}

// 调用上传接口
async function uploadSubtitle(option) {
  const { file, onSuccess, onError } = option
  try {
    const relativePath = await uploadFile(file) // 假设返回如：'subtitles/123.vtt'
    form.subtitleUrl = relativePath
    onSuccess()
    ElMessage.success('✅ 上传成功')
  } catch (err) {
    onError(err)
    ElMessage.error(err?.response?.data?.message || '上传失败')
    return Promise.reject(err)
  }
}

// 加载数据 
function loadData() {
  const params = {
    videoId: searchForm.videoId,
    subtitleType: searchForm.subtitleType,
    languagType: searchForm.languagType,
    designation: searchForm.designation,
    pageNum: page.current,
    pageSize: page.size
  }

  getSubtitlesList(params)
    .then(res => {
      if (res.code === 200) {
        // 修复后端返回数据字段名拼写错误
        const records = res.data.records || []
        const fixedRecords = records.map(item => ({
          ...item,
          // 兼容后端字段名拼写错误
          languagType: item.languagType || item.languagType || ''
        }))
        tableData.value = fixedRecords
        page.total = res.data.total || 0
      } else {
        ElMessage.error(res.message || '查询失败')
      }
    })
    .catch(err => {
      ElMessage.error(err?.response?.data?.message || '请求异常')
    })
}

// 搜索与重置
function handleSearch() {
  page.current = 1
  loadData()
}

function resetSearch() {
  searchForm.videoId = undefined
  searchForm.subtitleType = undefined
  searchForm.languagType = undefined
  searchForm.designation = undefined
  page.current = 1
  loadData()
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
  const rowData = { ...row }
  if (rowData.subtitleUrl && typeof rowData.subtitleUrl !== 'string') {
    rowData.subtitleUrl = ''
  }
  // 兼容后端字段名拼写错误
  rowData.languagType = rowData.languagType || rowData.languagType || '中文'
  Object.assign(form, rowData)
  dialogVisible.value = true
}

// 删除字幕 
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该字幕吗？此操作不可恢复', '提示', {
      type: 'warning'
    })
    console.log(row.id)

    const res = await deleteSubtitles(row.id)
    console.log(res)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData() // 重新加载数据
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单 
async function submitForm() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return

    // 确保传递给后端的数据格式正确
    const payload = {
      videoId: form.videoId,
      subtitleUrl: typeof form.subtitleUrl === 'string' ? form.subtitleUrl : '',
      subtitleType: form.subtitleType,
      languagType: form.languagType,
      designation:'Linux运维'
    }

    try {
      let res
      if (isAdd.value) {
        // 新增字幕
        res = await addSubtitles(payload)
      } else {
        // 编辑字幕
        res = await UpdatesSubtitles(form.id, payload)
      }

      if (res.code === 200) {
        ElMessage.success(isAdd.value ? '新增成功' : '修改成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (err) {
      ElMessage.error(err?.response?.data?.message || '网络异常')
    }
  })
}

// 关闭弹窗时重置表单 
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: 0,
    videoId: 1,
    subtitleUrl: '',
    subtitleType: 'VTT',
    languagType: '中文'
  })
}

// 页面加载 
onMounted(loadData)
</script>

<style scoped>
.subtitle-manage {
  padding: 16px;

}

.subtitle-uploader .el-upload {
  width: 100%;
}

.placeholder {
  padding: 24px 0;
  text-align: center;
  color: #999;
}


.placeholder .el-icon {
  margin-bottom: 8px;
}

.file-name {
  padding: 8px;
  font-size: 14px;
  color: #409eff;
  background: #f5f7fa;
  border-radius: 4px;
  text-align: center;
}

.text-muted {
  color: #999;
  font-style: italic;
}
</style>