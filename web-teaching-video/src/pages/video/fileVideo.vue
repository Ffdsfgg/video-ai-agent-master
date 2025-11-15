<template>
  <view class="container">
    <u-navbar :title="pageTitle" />

    <view class="form-wrapper">
      <u-form ref="formRef" :model="formData" :rules="rules" labelWidth="80">

        <!-- 选择视频 -->
        <u-form-item label="选择视频" prop="url" required>
          <view class="file-selector" @tap="chooseVideo">
            <view v-if="!formData.url" class="placeholder">
              <u-icon name="plus" size="24" color="#999" />
              <text>点击选择视频（仅支持 mp4 / mov / avi）</text>
            </view>
            <view v-else class="file-info">
              <u-icon name="play-circle" size="20" color="#2979ff" />
              <text class="file-name">{{ videoName }}</text>
            </view>
          </view>
        </u-form-item>

        <!-- 上传封面图 -->
        <u-form-item label="封面图" prop="imgUrl">
          <view class="img-box" @tap="chooseImage">
            <up-image v-if="formData.imgUrl" :show-loading="true" :src="IMG_URL + formData.imgUrl" width="80px"
              height="80px" />
            <view v-else class="placeholder">
              <u-icon name="photo" size="28" color="#999" />
              <text>点击上传封面（仅支持 jpg / png / webp）</text>
            </view>
          </view>
        </u-form-item>

        <!-- 上传课件 -->
        <u-form-item label="上传课件" prop="coursewareUrl">
          <view class="file-selector" @tap="chooseCourseware">
            <view v-if="!formData.coursewareUrl" class="placeholder">
              <u-icon name="plus" size="24" color="#999" />
              <text>点击选择课件（仅支持 pdf / ppt / pptx / doc / docx）</text>
            </view>
            <view v-else class="file-info">
              <u-icon name="document" size="20" color="#2979ff" />
              <text class="file-name">{{ coursewareName }}</text>
            </view>
          </view>
        </u-form-item>

        <!-- 视频名称 -->
        <u-form-item label="视频名称" prop="name" required>
          <u-input v-model="formData.name" placeholder="请输入视频名称" maxlength="50" />
        </u-form-item>

        <!-- 视频类型 -->
        <u-form-item label="视频类型" prop="type" required>
          <u-input v-model="formData.type" placeholder="请选择类型" disabled @tap="showTypePicker = true" />
        </u-form-item>

        <!-- 介绍 -->
        <u-form-item label="介绍" prop="description">
          <u-textarea v-model="formData.description" placeholder="请输入视频介绍（选填）" maxlength="200" count />
        </u-form-item>

        <!-- 公开课（仅老师可改） -->
        <u-form-item label="公开课">
          <u-switch v-model="formData.isPublic" :disabled="!isTeacher" activeColor="#2979ff" />
        </u-form-item>

        <!-- 提交按钮 -->
        <u-button type="primary" :loading="submitting" :disabled="!formData.url" @tap="submitInfo">
          {{ buttonText }}
        </u-button>
      </u-form>
    </view>

    <!-- 类型选择器 -->
    <u-picker :show="showTypePicker" :columns="[typeOptions]" @confirm="onTypeConfirm"
      @cancel="showTypePicker = false" />

    <!-- 全屏遮罩 -->
    <u-overlay :show="showLoadingCover" z-index="9999">
      <view class="loading-mask">
        <u-loading-icon size="48" color="#fff" text="正在处理，请稍候…" vertical text-color="#fff" />
      </view>
    </u-overlay>
  </view>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { API_URL, IMG_URL } from '@/utils/config.js'
import { roleValidate } from '@/api/user.js'

const formRef = ref()
const submitting = ref(false)
const showTypePicker = ref(false)
const showLoadingCover = ref(false)
const isTeacher = ref(false)

// 动态标题 & 按钮
const pageTitle = computed(() => (formData.value.id ? '编辑视频' : '上传视频'))
const buttonText = computed(() => (formData.value.id ? '保存修改' : '提交信息'))

//表单模型
const formData = ref({
  id: null,
  name: '',
  url: '',
  imgUrl: '',
  type: '',
  description: '',
  coursewareUrl: '',
  isPublic: false
})

const typeOptions = ['教学', '娱乐', '纪录片', 'Vlog', '其他']
const rules = {
  name: [{ required: true, message: '请输入视频名称' }],
  type: [{ required: true, message: '请选择视频类型' }]
}

const videoName = ref('')
const coursewareName = ref('')

//生命周期
onLoad((opts) => {
  if (opts.editMode && opts.v) {
    const v = JSON.parse(decodeURIComponent(opts.v))
    formData.value = {
      id: v.id,
      name: v.name,
      url: v.url,
      imgUrl: v.imgUrl,
      type: v.videoType || v.type,
      description: v.description || '',
      coursewareUrl: v.coursewareUrl || '',
      isPublic: v.isPublic === 0
    }
    videoName.value = v.name
    coursewareName.value = v.coursewareUrl ? v.coursewareUrl.split('/').pop() : ''
  }
})

onMounted(async () => {
  const { data } = await roleValidate()
  isTeacher.value = data
})

//文件选择 
const chooseVideo = () => {
  uni.chooseVideo({
    sourceType: ['album', 'camera'],
    maxDuration: 60,
    success: (res) => {
      const filePath = res.tempFilePath
      const fileName = res.name || 'video.mp4'
      const pureName = fileName.replace(/\.[^.]+$/, '')
      videoName.value = pureName
      formData.value.name = pureName
      formData.value.type = fileName.split('.').pop().toLowerCase()

      uni.showLoading({ title: '正在上传视频...', mask: true })
      uni.uploadFile({
        url: `${API_URL}/file`,
        filePath,
        name: 'file',
        success: (uploadRes) => {
          const data = JSON.parse(uploadRes.data)
          if (data.code === 200) formData.value.url = data.data
          else uni.showToast({ title: data.message || '视频上传失败', icon: 'none' })
        },
        complete: () => uni.hideLoading()
      })
    }
  })
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      uni.uploadFile({
        url: `${API_URL}/file`,
        filePath: res.tempFilePaths[0],
        name: 'file',
        success: (uploadRes) => {
          const data = JSON.parse(uploadRes.data)
          if (data.code === 200) formData.value.imgUrl = data.data
          else uni.showToast({ title: data.message || '封面上传失败', icon: 'none' })
        }
      })
    }
  })
}

const chooseCourseware = () => {
  uni.chooseFile({
    count: 1,
    type: 'file',
    extension: ['.pdf', '.ppt', '.pptx', '.doc', '.docx'],
    success: (res) => {
      const tempFilePath = res.tempFiles[0].path
      const fileName = res.tempFiles[0].name
      coursewareName.value = fileName.replace(/\.[^.]+$/, '')
      uni.uploadFile({
        url: `${API_URL}/file`,
        filePath: tempFilePath,
        name: 'file',
        success: (uploadRes) => {
          const data = JSON.parse(uploadRes.data)
          if (data.code === 200) formData.value.coursewareUrl = data.data
          else uni.showToast({ title: data.message || '课件上传失败', icon: 'none' })
        }
      })
    }
  })
}

const onTypeConfirm = (e) => { formData.value.type = e.value[0] }

//提交
const submitInfo = async () => {
  await formRef.value.validate()
  submitting.value = true
  showLoadingCover.value = true

  // 把布尔值转成 0/1
  const payload = {
    ...formData.value,
    isPublic: formData.value.isPublic ? 0 : 1
  }

  const api = payload.id ? `${API_URL}/video/update` : `${API_URL}/video`
  const method = payload.id ? 'PUT' : 'POST'

  uni.request({
    url: api,
    method,
    data: payload,
    header: { 'Content-Type': 'application/json', Authorization: uni.getStorageSync('token') || '' },
    success: (res) => {
      if (res.data.code === 200) {
        uni.showToast({ title: payload.id ? '更新成功' : '提交成功' })
        uni.navigateBack({ delta: 1 })
      } else {
        uni.showToast({ title: res.data.message || '操作失败', icon })
      }
    },
    fail: () => uni.showToast({ title: '网络错误', icon: 'none' }),
    complete: () => {
      submitting.value = false
      showLoadingCover.value = false
    }
  })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.form-wrapper {
  padding: 24rpx;
}

.file-selector,
.img-box {
  padding: 40rpx;
  border: 2rpx dashed #ddd;
  border-radius: 8rpx;
  text-align: center;
}

.file-name,
.placeholder {
  font-size: 28rpx;
  color: #333;
}

.loading-mask {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.8);
}
</style>