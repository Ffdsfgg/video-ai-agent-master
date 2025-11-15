<template>
  <div class="video-analysis">
    <!-- 顶部搜索 -->
    <el-card>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="视频 ID">
          <el-input-number v-model="searchForm.videoId" :min="1" clearable />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker v-model="searchForm.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始"
            end-placeholder="结束" clearable />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="任意字段搜索" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card style="margin-top: 16px">
      <el-table :data="tableData" stripe>
        <el-table-column prop="videoId" label="视频 ID" width="90" />
        <el-table-column label="总结" show-overflow-tooltip>
          <template #default="{ row }">
            <div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px">
              {{ row.sumUp }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="教学大纲" show-overflow-tooltip>
          <template #default="{ row }">
            <div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px">
              {{ row.teachingOutline }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="内容概要" show-overflow-tooltip>
          <template #default="{ row }">
            <div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px">
              {{ row.contentSummary }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="结构化输出" show-overflow-tooltip>
          <template #default="{ row }">
            <div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px">
              {{ row.structuredOutput }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="表格内容" show-overflow-tooltip>
          <template #default="{ row }">
            <div style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px">
              {{ row.tableText }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="测验 JSON" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="copyJson(row.quizJson)">复制</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <!-- <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button> -->
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
        layout="prev, pager, next, sizes, jumper" style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData" @size-change="loadData" />
    </el-card>

    <!-- 新增和编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isAdd ? '新增解析结果' : '编辑解析结果'" width="700px" top="5vh" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="视频 ID">
          <el-input-number v-model="form.videoId" disabled />
        </el-form-item>

        <el-form-item label="JSON 数据" prop="jsonText">
          <el-input v-model="form.jsonText" type="textarea" :rows="4" placeholder="AI 返回的原始 JSON" />
        </el-form-item>

        <el-form-item label="总结" prop="sumUp">
          <el-input v-model="form.sumUp" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="教学大纲" prop="teachingOutline">
          <el-input v-model="form.teachingOutline" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="内容概要" prop="contentSummary">
          <el-input v-model="form.contentSummary" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="结构化输出" prop="structuredOutput">
          <el-input v-model="form.structuredOutput" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="表格内容" prop="tableText">
          <el-input v-model="form.tableText" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="测验 JSON" prop="quizJson">
          <el-input v-model="form.quizJson" type="textarea" :rows="4" placeholder="测验 JSON" />
          <el-button size="small" style="margin-top: 4px" @click="formatQuizJson">
            格式化
          </el-button>
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
import { getVideoInfoList, updateVideoInfo } from '@/api/video'
import dayjs from 'dayjs'

// 搜索表单 
const searchForm = reactive({
  videoId: undefined,
  timeRange: [],
  keyword: ''
})

// 分页 
const page = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])

// 弹窗表单 
const dialogVisible = ref(false)
const isAdd = ref(false)
const form = reactive({
  id: 0,
  videoId: 1,
  jsonText: '',
  sumUp: '',
  teachingOutline: '',
  contentSummary: '',
  structuredOutput: '',
  tableText: '',
  quizJson: '',
  createTime: '',
  updateTime: ''
})
const formRef = ref()
const rules = {
  videoId: [{ required: true, message: '请输入视频 ID', trigger: 'blur' }]
}

// 分页查询 
function loadData() {
  const [startTime, endTime] = searchForm.timeRange?.length === 2
    ? [
      dayjs(searchForm.timeRange[0]).format('YYYY-MM-DD HH:mm:ss'),
      dayjs(searchForm.timeRange[1]).format('YYYY-MM-DD HH:mm:ss')
    ]
    : [undefined, undefined]

  const params = {
    videoId: searchForm.videoId || undefined,
    startTime,
    endTime,
    keyword: searchForm.keyword || undefined,
    pageNum: page.current,
    pageSize: page.size
  }

  getVideoInfoList(params)
    .then(res => {
      if (res.code === 200) {
        // 后端返回时间戳转换前端格式化 
        tableData.value = (res.data.records || []).map(item => ({
          ...item,
          createTime: item.createTime ? dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') : '',
          updateTime: item.updateTime ? dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss') : ''
        }))
        page.total = res.data.total || 0
      } else {
        ElMessage.error(res.message || '查询失败')
      }
    })
    .catch(err => ElMessage.error(err?.response?.data?.message || '请求异常'))
}

// 重置搜索  
function resetSearch() {
  Object.assign(searchForm, { videoId: undefined, timeRange: [], keyword: '' })
  page.current = 1
  loadData()
}
function handleSearch() {
  page.current = 1
  loadData()
}

// 工具函数
function copyJson(text) {
  navigator.clipboard.writeText(text).then(() => ElMessage.success('已复制'))
}
function formatQuizJson() {
  try {
    form.quizJson = JSON.stringify(JSON.parse(form.quizJson), null, 2)
  } catch {
    ElMessage.error('JSON 格式错误')
  }
}

// 编辑  
function openEdit(row) {
  isAdd.value = false
  Object.assign(form, {
    ...row,
    // 回显时把后端时间戳转成字符串，方便展示
    createTime: row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '',
    updateTime: row.updateTime ? dayjs(row.updateTime).format('YYYY-MM-DD HH:mm:ss') : ''
  })
  dialogVisible.value = true
}

//保存  
async function submitForm() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    try {
      const { code } = await updateVideoInfo(form.id, {
        videoId: form.videoId,
        jsonText: form.jsonText,
        sumUp: form.sumUp,
        teachingOutline: form.teachingOutline,
        contentSummary: form.contentSummary,
        structuredOutput: form.structuredOutput,
        tableText: form.tableText,
        quizJson: form.quizJson,
        //* 后端自动更新时间，无需手动传 createTime / updateTime 
      })
      if (code === 200) {
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error('保存失败')
      }
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '网络异常')
    }
  })
}

// 重置 
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: 0,
    videoId: 1,
    jsonText: '',
    sumUp: '',
    teachingOutline: '',
    contentSummary: '',
    structuredOutput: '',
    tableText: '',
    quizJson: ''
  })
}

onMounted(loadData)
</script>

<style scoped>
.video-analysis {
  padding: 16px;
}
</style>