<template>
  <view class="page">
    <!-- 搜索框 -->
    <view class="search-box">
      <u-search
        v-model="searchText"
        placeholder="请输入视频名称或描述"
        :clearabled="true"
        height="60rpx"
        bg-color="#fff"
        border-color="#eee"
        @change="onSearch"
      />
    </view>

    <!-- 分类标签 -->
    <scroll-view scroll-x class="tag-scroll">
      <view class="tag-list">
        <view
          v-for="tag in allTags"
          :key="tag"
          class="tag-item"
          :class="{ active: selectedTag === tag }"
          @tap="selectTag(tag)"
        >
          {{ tag || '全部' }}
        </view>
      </view>
    </scroll-view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton">
      <view v-for="i in 3" :key="i" class="skeleton-card" />
    </view>

    <!-- 竖直列表 -->
    <scroll-view v-else scroll-y class="list">
      <!-- 公开视频（去重后的，支持标签筛选） -->
      <view v-for="video in filteredPublicVideos" :key="video.id" class="card card--public" @tap="play(video)">
        <view class="cover-box">
          <up-image :src="IMG_URL + video.imgUrl" width="100%" height="180rpx" radius="16rpx" lazy-load />
          <view class="badge">公开课</view>
          <view class="type-tag">{{ video.videoType }}</view>
          <view class="mask">
            <u-icon name="play-circle-fill" size="48" color="#fff" />
          </view>
        </view>

        <view class="info">
          <text class="title">{{ video.name }}</text>
          <text class="desc">{{ video.description }}</text>
        </view>
      </view>

      <!-- 普通视频 -->
      <view v-for="item in filteredVideos" :key="item.id" class="card">
        <view class="cover-box" @tap="play(item)">
          <up-image :src="item.imgUrl
            ? IMG_URL + item.imgUrl
            : IMG_URL + '/2025/07/18/7c467c37-7818-4c13-9c78-a0f54fe676b8.jpg'
            " width="100%" height="180rpx" radius="16rpx" lazy-load />
          <view class="type-tag">{{ item.videoType }}</view>
          <view class="mask">
            <u-icon name="play-circle-fill" size="48" color="#fff" />
          </view>
        </view>

        <view class="info">
          <text class="title">{{ item.name }}</text>
          <text class="desc">{{ item.description }}</text>
        </view>

        <view class="actions">
          <view class="action-btn edit" @tap.stop="editVideo(item)">
            <u-icon name="edit-pen" color="#fff" size="22" />
            <text>编辑</text>
          </view>
          <view class="action-btn delete" @tap.stop="deleteVideo(item.id)">
            <u-icon name="trash" color="#fff" size="22" />
            <text>删除</text>
          </view>
        </view>
      </view>

      <!-- 空占位 -->
      <view v-if="!loading && !filteredPublicVideos.length && !filteredVideos.length" class="empty">
        <u-icon name="file-text" size="80" color="#aaa" />
        <text>还没有内容</text>
      </view>
    </scroll-view>

    <!-- 悬浮按钮 -->
    <view class="fab-menu">
      <view class="fab-item upload" @tap="goUpload" hover-class="fab-hover">
        <u-icon name="plus" color="#fff" size="28" />
        <text class="fab-tip">上传</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { onPullDownRefresh, onReachBottom } from "@dcloudio/uni-app"
import { getVideo, getPublicVideo, delVideo } from '@/api/video.js'
import { IMG_URL } from '@/utils/config.js'

const videos = ref([])
const publicVideos = ref([])
const loading = ref(true)

// 搜索关键词
const searchText = ref('')

// 当前选中的标签
const selectedTag = ref('')

// 获取所有标签（去重）
const allTags = computed(() => {
  const tags = [...videos.value, ...publicVideos.value].map(v => v.videoType).filter(Boolean)
  return ['全部', ...new Set(tags)]
})

// 筛选后的公开视频（支持标签筛选，去重）
const filteredPublicVideos = computed(() => {
  // 先进行标签筛选
  let filtered = publicVideos.value
  if (selectedTag.value && selectedTag.value !== '全部') {
    filtered = filtered.filter(video => video.videoType === selectedTag.value)
  }
  
  // 再进行搜索筛选
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    filtered = filtered.filter(video => 
      video.name.toLowerCase().includes(keyword) || 
      video.description.toLowerCase().includes(keyword)
    )
  }
  
  // 最后进行去重（排除已在普通视频中的）
  const normalVideoIds = new Set(videos.value.map(v => v.id))
  return filtered.filter(pub => !normalVideoIds.has(pub.id))
})

// 筛选后的普通视频（支持模糊搜索和标签筛选）
const filteredVideos = computed(() => {
  return videos.value.filter(item => {
    const keyword = searchText.value.toLowerCase()
    const matchSearch = !keyword || 
      item.name.toLowerCase().includes(keyword) || 
      item.description.toLowerCase().includes(keyword)
    const matchTag = !selectedTag.value || selectedTag.value === '全部' || item.videoType === selectedTag.value
    return matchSearch && matchTag
  })
})

// 页面加载时获取数据
onMounted(async () => {
  await loadData()
})

// 下拉刷新事件
onPullDownRefresh(() => {
  console.log("下拉刷新")
  refreshData()
})

// 上拉加载更多事件
onReachBottom(() => {
  console.log("上拉加载更多")
  // 这里可以实现分页加载逻辑
})

// 加载数据函数
async function loadData() {
  try {
    const [pub, list] = await Promise.all([getPublicVideo(), getVideo()])
    publicVideos.value = (pub.data || []).filter(v => v.isPublic === 0)
    videos.value = list.data || []
  } finally {
    loading.value = false
    // 停止下拉刷新动画
    uni.stopPullDownRefresh()
  }
}

// 刷新数据
function refreshData() {
  loading.value = true
  loadData()
}

function play(video) {
  uni.navigateTo({ url: `/pages/video/video?id=${encodeURIComponent(video.id)}` })
}

function goUpload() {
  uni.navigateTo({ url: '/pages/video/fileVideo' })
}

function editVideo(video) {
  uni.navigateTo({
    url: `/pages/video/fileVideo?editMode=1&v=${encodeURIComponent(JSON.stringify(video))}`
  })
}

function onSearch() {
  // 输入框变化时自动触发筛选
}

function selectTag(tag) {
  selectedTag.value = tag
}

const deleteVideo = (id) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这个视频吗？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '删除中...' })
        try {
          await delVideo(id)
          videos.value = videos.value.filter(v => v.id !== id)
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
/* 全局重置与基础 */
.page {
  background: #f7f8fa;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.list {
  flex: 1;
  padding: 32rpx;
}

/* 搜索框 */
.search-box {
  padding: 20rpx 32rpx;
  background: #fff;
}

/* 分类标签 */
.tag-scroll {
  white-space: nowrap;
  padding: 0 32rpx 20rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.tag-list {
  display: inline-flex;
  gap: 20rpx;
}

.tag-item {
  padding: 10rpx 24rpx;
  background: #f5f5f5;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #666;
  transition: all 0.2s;

  &.active {
    background: #2979ff;
    color: #fff;
  }
}

/* 骨架屏 */
.skeleton {
  padding: 32rpx;

  .skeleton-card {
    height: 136rpx;
    border-radius: 24rpx;
    background: linear-gradient(90deg, #f2f2f2 25%, #e6e6e6 50%, #f2f2f2 75%);
    background-size: 200% 100%;
    animation: skeleton 1.5s infinite;
    margin-bottom: 32rpx;
  }
}

@keyframes skeleton {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

/* 空占位 */
.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-size: 28rpx;

  text {
    margin-top: 16rpx;
  }
}

/* 竖直卡片 */
.card {
  background: #fff;
  border-radius: 24rpx;
  margin-bottom: 32rpx;
  margin-right: 90rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;

  &:active {
    transform: scale(0.98);
  }

  &--public {
    border: 2rpx solid #2979ff;
  }
}

.cover-box {
  position: relative;

  .badge {
    position: absolute;
    top: 8rpx;
    left: 8rpx;
    background: #2979ff;
    color: #fff;
    font-size: 26rpx;
    padding: 2rpx 8rpx;
    border-radius: 8rpx;
    z-index: 3;
  }

  .type-tag {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    background: #ff9800;
    color: #fff;
    font-size: 26rpx;
    padding: 2rpx 8rpx;
    border-radius: 8rpx;
    z-index: 3;
  }

  .mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.info {
  padding: 24rpx;

  .title {
    font-size: 30rpx;
    font-weight: 600;
    color: #222;
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .desc {
    font-size: 24rpx;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.actions {
  display: flex;
  border-top: 1rpx solid #f1f1f1;

  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx 0;
    font-size: 24rpx;
    color: #fff;

    &.edit {
      background: #52c41a;
    }

    &.delete {
      background: #ff4d4f;
    }

    u-icon {
      margin-right: 8rpx;
    }
  }
}

/* 悬浮按钮 */
.fab-menu {
  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.fab-item {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.15);
  transition: transform 0.2s;
  position: relative;

  &.upload {
    background: #2979ff;
    color: #fff;
  }


  &:active {
    transform: scale(0.9);
  }
}

.fab-tip {
  position: absolute;
  right: 110rpx;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s;
}

.fab-item:hover .fab-tip,
.fab-item:active .fab-tip {
  opacity: 1;
}
</style>