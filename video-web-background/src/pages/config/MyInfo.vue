<template>
  <div class="my-profile">
    <el-card>
      <el-form ref="formRef" :model="profile" :rules="rules" label-width="100px" size="large">
        <!-- 头像：点击上传 -->
        <el-form-item label="头像">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false"
            :http-request="({ file, onSuccess }) => uploadAvatar(file, url => { profile.avatarUrl = url; onSuccess({ url }) })"
            drag>
            <img v-if="profile.avatarUrl" :src="IMG_URL + profile.avatarUrl" class="avatar-img" />
            <el-icon v-else size="40">
              <plus />
            </el-icon>
            <div class="avatar-tip" v-if="!profile.avatarUrl">点击或拖拽上传</div>
          </el-upload>
        </el-form-item>

        <!-- 用户名：直接输入 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profile.username" />
        </el-form-item>

        <!-- 邮箱：直接输入 -->
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profile.email" />
        </el-form-item>

        <!-- 性别：直接选择 -->
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="profile.sex">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 简介：直接输入 -->
        <el-form-item label="个人简介" prop="description">
          <el-input v-model="profile.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>

        <!-- 修改密码：直接输入（留空不修改） -->
        <el-form-item label="新密码" prop="password">
          <el-input v-model="profile.password" type="password" placeholder="留空不修改" show-password />
        </el-form-item>

        <!-- 保存按钮 -->
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { uploadFile } from '@/api/file'
import { IMG_URL } from '@/utils/config'

// 当前用户数据 
const profile = reactive({
  id: 1,
  username: '',
  avatarUrl: '',
  email: '',
  sex: 0,
  description: '',
  password: ''
})

// 校验规则 
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],

}
const formRef = ref()

// 加载当前用户数据
async function loadProfile() {
  try {
    const { code, data } = await getUserInfo()
    if (code === 200) {
      Object.assign(profile, data)
      profile.password = ''   // 密码框默认为空
      console.log('当前用户信息:', profile)
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '网络异常')
  }
}

//头像上传
async function uploadAvatar(file) {
  try {
    const data = await uploadFile(file) // 返回相对路径
    // 拼成完整访问地址
    console.log('头像上传成功:', data)
    profile.avatarUrl = data
    console.log('头像访问地址:', profile.avatarUrl)
    return profile.avatarUrl
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '上传异常')
  }
}

// 保存个人信息 
async function saveProfile() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {

      const data = await updateUserInfo({
        id: profile.id,
        username: profile.username,
        avatarUrl: profile.avatarUrl,
        email: profile.email,
        sex: profile.sex,
        description: profile.description,
        password: profile.password || undefined   // 留空不传
      })
      ElMessage.success('个人信息已更新')
      profile.password = ''   // 清空密码框

    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '网络异常')
    }
  })
}

onMounted(loadProfile)
</script>

<style scoped>
.my-profile {
  max-width: 600px;
  margin: 24px auto;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>