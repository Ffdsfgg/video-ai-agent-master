<template>
  <div class="user-manage">
    <!-- 顶部搜索 -->
    <el-card>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 100px;">
            <el-option label="正常" value="0" />
            <el-option label="异常" value="1" />
            <el-option label="注销" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" clearable placeholder="全部" style="width: 100px;">
            <el-option label="老师" value="0" />
            <el-option label="学生" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card style="margin-top: 16px">
      <el-table :data="tableData" stripe>
        <el-table-column label="头像" width="90" align="center">
          <template #default="{ row }">
            <el-upload class="avatar-uploader" action="#" :show-file-list="false"
              :http-request="({ file, onSuccess }) => uploadAvatar(file, url => { row.avatarUrl = url; onSuccess({ url }) })">
              <el-avatar :size="40" :src="IMG_URL + row.avatarUrl" fit="cover" style="cursor: pointer" />
            </el-upload>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-tag :type="row.role === 0 ? 'success' : 'info'">
              {{ row.role === 0 ? '老师' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="row.status === '0'
              ? 'success'
              : row.status === '1'
                ? 'warning'
                : 'danger'
              ">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="简介" show-overflow-tooltip width="230" />
        <el-table-column prop="createIp" label="创建IP" width="220" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <!-- <el-button size="small" type="warning" @click="toggleStatus(row)">
              {{ row.status === '0' ? '禁用' : row.status === 0 ? '启用' : '' }}
            </el-button> -->
            <el-button size="small" type="danger" @click="handleDelete(row)">
              注销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
        layout="prev, pager, next, sizes, jumper" style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData" @size-change="loadData" />
    </el-card>

    <!-- 新增和编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isAdd ? '新增用户' : '编辑用户'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="密码" v-if="isAdd" prop="password">
          <el-input v-model="form.password" show-password />
        </el-form-item>

        <!-- 头像上传 -->
        <el-form-item label="头像">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false"
            :http-request="({ file, onSuccess }) => uploadAvatar(file, url => { form.avatarUrl = url; onSuccess({ url }) })">
            <!-- 直接显示 form.avatarUrl，加上 IMG_URL 前缀 -->
            <el-avatar :size="60" :src="IMG_URL + form.avatarUrl" fit="cover" style="cursor: pointer" />
            <div class="avatar-tip">点击上传</div>
          </el-upload>
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio label="0">老师</el-radio>
            <el-radio label="1">学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="正常" value="0" />
            <el-option label="异常" value="1" />
            <el-option label="注销" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" rows="3" />
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
import { getUserList } from '@/api/user'
import { IMG_URL } from '@/utils/config'


//搜索表单 
const searchForm = reactive({
  username: '',
  status: undefined,
  role: undefined
})

//分页 
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})
const tableData = ref([])

// 弹窗相关
const dialogVisible = ref(false)
const isAdd = ref(false)
const form = reactive({
  id: 0,
  username: '',
  password: '',
  email: '',
  avatarUrl: '',
  description: '',
  sex: 0,
  status: '0',
  role: '0'
})
const formRef = ref()

// 表单校验规则 
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 调用后端分页接口获取数据 
function loadData() {
  const params = {
    username: searchForm.username || undefined,
    status: searchForm.status !== undefined ? Number(searchForm.status) : undefined,
    role: searchForm.role !== undefined ? Number(searchForm.role) : undefined,
    pageNum: page.current,
    pageSize: page.size
  }

  getUserList(params).then(res => {
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
  searchForm.username = ''
  searchForm.status = undefined
  searchForm.role = undefined
  page.current = 1
  loadData()
}

// 搜索按钮 
function handleSearch() {
  page.current = 1
  loadData()
}

// 状态文字映射
function statusLabel(s) {
  return s === 0 ? '正常' : s === 1 ? '异常' : '注销'
}

// 打开新增弹窗
function openAdd() {
  isAdd.value = true
  dialogVisible.value = true
}

// 打开编辑弹窗 
function openEdit(row) {
  isAdd.value = false
  Object.assign(form, row)
  dialogVisible.value = true
}


// 注销用户
function handleDelete(row) {
  ElMessageBox.confirm('确定注销该用户吗？', '提示', { type: 'warning' }).then(() => {
    // TODO: 调用后端接口注销用户
    row.status = 2
    ElMessage.success('已注销')
  })
}

// 头像上传
function uploadAvatar(file, onSuccess) {
  const url = URL.createObjectURL(file)
  onSuccess(url)
}

//提交新增或编辑表单
function submitForm() {
  formRef.value?.validate(valid => {
    if (!valid) return
    if (isAdd.value) {
      // TODO: 调用后端新增接口
      ElMessage.success('新增成功')
    } else {
      // TODO: 调用后端修改接口
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

// 弹窗关闭时重置表单
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: 0,
    username: '',
    password: '',
    email: '',
    avatarUrl: '',
    description: '',
    sex: 0,
    status: '0',
    role: '0'
  })
}

// 页面加载完拉取第一页数据
onMounted(loadData)
</script>

<style scoped>
.user-manage {
  padding: 16px;
}

.avatar-uploader .el-avatar {
  cursor: pointer;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin-top: 4px;
}
</style>