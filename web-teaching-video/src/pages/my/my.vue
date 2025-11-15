<template>
  <view class="page">
    <view class="status-bar"></view>

    <!-- 头像卡片 -->
    <view class="card avatar-card" @click="toProfile">
      <image class="avatar" :src="IMG_URL + user.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="info">
        <text class="name">{{ user.username }}</text>
        <text class="account">{{ roleText }}</text>
      </view>
      <text class="arrow">›</text>
    </view>

    <!-- 资料卡片 -->
    <view class="card">
      <view class="cell" @click="openSexPicker">
        <text class="cell-title">性别</text>
        <text class="cell-value">{{ sexList[user.sex] }}</text>
        <text class="arrow">›</text>
      </view>
      <view class="cell" @click="openEditDesc">
        <text class="cell-title">个人简介</text>
        <text class="cell-value">{{ user.description || '未填写' }}</text>
        <text class="arrow">›</text>
      </view>
      <view class="cell" @click="openEditEmail">
        <text class="cell-title">邮箱</text>
        <text class="cell-value">{{ user.email || '未填写' }}</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 修改密码 -->
    <view class="card">
      <view class="cell" @click="toChangePwd">
        <text class="cell-title">修改密码</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="card">
      <text class="logout" @click="logout">退出登录</text>
    </view>

    <!-- 性别选择器 -->
    <u-picker :show="showSexPicker" :columns="[sexList]" @confirm="onSexConfirm" @cancel="showSexPicker = false" />

    <!-- 简介弹窗 -->
    <u-popup :show="showEditDesc" mode="center" @close="showEditDesc = false">
      <view class="pop">
        <textarea v-model="tempDesc" maxlength="100" placeholder="介绍一下自己~"></textarea>
        <view class="pop-btns">
          <button size="mini" @click="showEditDesc = false">取消</button>
          <button size="mini" type="primary" @click="saveDesc">保存</button>
        </view>
      </view>
    </u-popup>

    <!-- 邮箱弹窗 -->
    <u-popup :show="showEditEmail" mode="center" @close="showEditEmail = false">
      <view class="pop">
        <u-input v-model="tempEmail" type="text" placeholder="请输入邮箱" border="surround" clearable class="mt20" />
        <view class="pop-btns">
          <button size="mini" @click="showEditEmail = false">取消</button>
          <button size="mini" type="primary" @click="saveEmail">保存</button>
        </view>
      </view>
    </u-popup>

    <!-- 修改密码弹窗 -->
    <u-popup :show="showPwd" mode="center" @close="showPwd = false">
      <view class="pop">
        <u-input v-model="pwd.old" type="password" placeholder="旧密码" border="surround" clearable class="mt20" />
        <u-input v-model="pwd.new" type="password" placeholder="新密码" border="surround" clearable class="mt20" />
        <u-input v-model="pwd.confirm" type="password" placeholder="确认新密码" border="surround" clearable class="mt20" />
        <view class="pop-btns">
          <button size="mini" @click="showPwd = false">取消</button>
          <button size="mini" type="primary" @click="doChangePwd">保存</button>
        </view>
      </view>
    </u-popup>
    <!-- 头像选择 -->
    <u-action-sheet :show="showAvatarSheet" :actions="avatarActions" @select="onAvatarSelect"
      @close="showAvatarSheet = false" />
  </view>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { getUserInfo, updateUserInfo, updatePassword } from '@/api/user'
import { uploadFileRequest } from '@/api/video'
import { IMG_URL } from '@/utils/config.js'

// 用户数据
const user = reactive({
  id: 0,
  username: '',
  account: '',
  avatarUrl: '',
  sex: 0,
  description: '',
  email: '', // 添加邮箱字段
  role: 0
})
const sexList = ['保密', '男', '女'] // 性别列表
const tempDesc = ref('') // 临时简介
const tempEmail = ref('') // 临时邮箱
const showSexPicker = ref(false) // 性别选择器弹窗
const showEditDesc = ref(false) // 简介编辑弹窗
const showEditEmail = ref(false) // 邮箱编辑弹窗
const showAvatarSheet = ref(false) // 头像选择器
const avatarActions = [{ name: '拍照' }, { name: '从相册选择' }] // 头像列表

// 计算属性
const roleText = computed(() => (user.role === 0 ? '老师' : '学生')) // 角色

// 生命周期
onMounted(() => refresh())

// 工具函数
function normalizeUser(raw) {
  return {
    id: raw.id,
    username: raw.username || '用户',
    account: raw.account || '',
    avatarUrl: raw.avatarUrl || '/static/default-avatar.png',
    sex: raw.sex ?? 0,
    description: raw.description || '这个人很懒，什么也没写',
    email: raw.email || '', // 添加邮箱字段
    role: raw.role ?? 0
  }
}
async function refresh() {
  try {
    const { data } = await getUserInfo()
    Object.assign(user, normalizeUser(data))
  } catch (e) {
    uni.showToast({ title: e.message || '获取信息失败', icon: 'none' })
  }
}

// 头像上传
function toProfile() { showAvatarSheet.value = true }
async function onAvatarSelect(e) {
  uni.chooseImage({
    count: 1,
    sourceType: [e.name === '拍照' ? 'camera' : 'album'],
    success: async (res) => {
      try {
        const { data: url } = await uploadFileRequest(res.tempFilePaths[0])
        console.log(url)
        user.avatarUrl = url
        await patch({ avatarUrl: url })
      } catch (err) {
        uni.showToast({ title: err.message || '上传失败', icon: 'none' })
      }
    }
  })
}

// 性别
function openSexPicker() { showSexPicker.value = true }
async function onSexConfirm(e) {
  const sex = e.indexs[0]
  user.sex = sex
  await patch({ sex })
  showSexPicker.value = false
}

// 简介
function openEditDesc() {
  tempDesc.value = user.description
  showEditDesc.value = true
}
async function saveDesc() {
  user.description = tempDesc.value
  await patch({ description: tempDesc.value })
  showEditDesc.value = false
}

// 邮箱
function openEditEmail() {
  tempEmail.value = user.email
  showEditEmail.value = true
}
async function saveEmail() {
  user.email = tempEmail.value
  await patch({ email: tempEmail.value })
  showEditEmail.value = false
}

// 通用回写
async function patch(payload) {
  try {
    await updateUserInfo(payload)
    uni.showToast({ title: '已保存', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
    refresh()
  }
}

// 修改密码
const showPwd = ref(false)
const pwd = reactive({ old: '', new: '', confirm: '' })

function toChangePwd() {
  pwd.old = ''
  pwd.new = ''
  pwd.confirm = ''
  showPwd.value = true
}

async function doChangePwd() {
  if (!pwd.old || !pwd.new || !pwd.confirm) {
    uni.showToast({ title: '请完整填写', icon: 'none' })
    return
  }
  if (pwd.new !== pwd.confirm) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  try {
    console.log(111)
    await updatePassword({ oldPwd: pwd.old, newPwd: pwd.new })
    uni.showToast({ title: '密码已修改，请重新登录', icon: 'success' })
    showPwd.value = false
  } catch (e) {
    uni.showToast({ title: e.message || '修改失败', icon: 'none' })
  }
}

// 其他
function logout() {
  uni.showModal({
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })
}
</script>

<style scoped>
.page {
  background: #f6f6f6;
  min-height: 100vh;
}

.status-bar {
  height: var(--status-bar-height);
}

.card {
  margin: 20rpx 30rpx;
  padding: 0 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.avatar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 30rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
}

.name {
  font-size: 36rpx;
  margin-right: 30rpx;
  font-weight: 500;
  color: #000;
}

.account {
  font-size: 28rpx;
  color: #999;
}

.arrow {
  font-size: 36rpx;
  color: #c0c0c0;
}

.cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f2f2f2;
}

.cell:last-child {
  border-bottom: none;
}

.cell-title {
  font-size: 32rpx;
  color: #000;
}

.cell-value {
  font-size: 28rpx;
  color: #888;
}

.logout {
  display: block;
  width: 100%;
  text-align: center;
  font-size: 32rpx;
  color: #ff4d4f;
  padding: 30rpx 0;
}

.pop {
  width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

textarea {
  width: 100%;
  height: 200rpx;
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 30rpx;
}

.pop-btns {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  margin-top: 30rpx;
}

.mt20 {
  margin-top: 20rpx;
  background-color: #eee;
}
</style>