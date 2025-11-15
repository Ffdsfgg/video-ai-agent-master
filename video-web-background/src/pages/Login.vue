<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="card-header">用户登录</div>
      </template>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loginLoading" style="width: 100%">
            {{ loginLoading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        <!-- <div class="register-tip">
          还没有账号？
          <span @click="goToRegister" class="register-link">请注册</span>
        </div> -->
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../api/user.js'
import { ElMessage } from 'element-plus'
import { TokenManager } from '../utils/TokenManager.js'

const router = useRouter()

// 表单数据
const loginForm = ref({
  username: '',
  password: ''
})

// 表单校验规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 表单引用
const loginFormRef = ref(null)

// 登录加载状态
const loginLoading = ref(false)

// 登录逻辑
const handleLogin = async () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loginLoading.value = true
    try {
      const data = await adminLogin(loginForm.value)
      if (data.code === 200) {
        ElMessage.success('登录成功')
        TokenManager.setToken(data.data)
        router.push('/admin')
      } else {
        ElMessage.error(data.message || '登录失败，请重试')
      }
    } catch (err) {
      ElMessage.error('网络错误，请稍后再试')
    } finally {
      loginLoading.value = false
    }
  })
}

// 注册跳转
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea, #764ba2); // 渐变背景
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.register-tip {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}
</style>