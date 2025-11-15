<template>
  <view class="page">
    <!-- 顶部固定区 -->
    <view class="sticky-wrapper">
      <view class="player-wrapper">
        <div id="dplayer-container" class="dplayer-box"></div>
      </view>
      <view class="tabs-wrapper">
        <up-tabs :list="list1" @click="handleTabClick" lineColor="#1677ff" />
      </view>
    </view>

    <!-- 内容区 -->
    <view class="content-wrapper">
      <!-- 骨架 -->
      <view v-if="currentTab === 2 && loadingTeachingOutline" class="skeleton" />
      <view v-else-if="currentTab === 3 && loadingContentSummary" class="skeleton" />
      <view v-else-if="currentTab === 4 && loadingStructuredOutput" class="skeleton" />
      <view v-else-if="currentTab === 6 && loadingCoursewareAnalysis" class="skeleton" />

      <!-- 字幕 -->
      <view v-show="currentTab === 0" class="panel">
        <h2 class="panel-title">字幕内容</h2>
        <p class="panel-tip">点击字幕可直接跳转到对应时间</p>
        <!-- 字幕切换选择框 -->
        <view class="subtitle-selector">
          <text style="margin-right: 16rpx;">切换字幕：</text>
          <select v-model="selectedSubtitle" @change="changeSubtitle" style="padding: 10rpx; font-size: 28rpx;">
            <option v-for="(sub, index) in subtitleOptions" :key="index" :value="sub.url">
              {{ sub.languageType }}
            </option>
          </select>
        </view>
        <ul class="subtitle-list">
          <li v-for="(s, i) in parsedSubtitles" :key="i" @click="jumpToTime(s.startTime)">
            {{ s.text }}
          </li>
        </ul>
      </view>

      <!-- 视频介绍 -->
      <view v-show="currentTab === 1" class="panel">
        <h2 class="panel-title">视频介绍</h2>
        <p class="panel-body">{{ videoIntroduction || '该视频暂无介绍' }}</p>
      </view>

      <!-- 教学大纲 -->
      <view v-show="currentTab === 2" class="panel">
        <h2 class="panel-title">教学大纲</h2>
        <view v-show="loadingTeachingOutline && !teachingOutline" class="loading-tip">
          正在生成教学大纲...
        </view>
        <view class="html-content" v-html="formatText(teachingOutline)" />
      </view>

      <!-- 内容摘要 -->
      <view v-show="currentTab === 3" class="panel">
        <h2 class="panel-title">内容摘要</h2>
        <view v-show="loadingContentSummary && !contentSummary" class="loading-tip">
          正在生成内容摘要...
        </view>
        <view class="html-content" v-html="formatText(contentSummary)" />
      </view>

      <!-- 视频总结 -->
      <view v-show="currentTab === 4" class="panel">
        <h2 class="panel-title">总结</h2>
        <view v-show="loadingStructuredOutput && !structuredOutput" class="loading-tip">
          正在生成分段总结...
        </view>
        <view class="html-content" v-html="formatText(structuredOutput)" />
      </view>

      <!-- 图表分析 -->
      <view v-show="currentTab === 5" class="panel">
        <h2 class="panel-title">图表分析</h2>
        <div id="chart-container" style="height: 320px;"></div>
        <div id="keyword-cloud-container" style="height: 320px; margin-top: 40px;"></div>
      </view>

      <!-- 课件分析 -->
      <view v-show="currentTab === 6" class="panel">
        <h2 class="panel-title">课件分析</h2>
        <view v-if="coursewareAnalysis">
          <view v-show="loadingCoursewareAnalysis && !coursewareAnalysis" class="loading-tip">
            正在生成课件分析...
          </view>
          <view v-if="coursewareAnalysis" style="margin-bottom: 20rpx;">
            <button class="download-button" @click="downloadCourseware">下载课件</button>
          </view>
          <view class="html-content" v-html="formatText(coursewareAnalysis)" />
        </view>
        <view v-else>
          <h3 class="no-data">该视频暂无课件分析</h3>

        </view>

      </view>

      <!-- AI问答 -->
      <view v-show="currentTab === 7" class="panel">
        <h2 class="panel-title">AI问答</h2>

        <scroll-view class="chat-box" scroll-y :scroll-top="chatScrollTop" :scroll-with-animation="true">
          <view class="message">
            <u-avatar size="48" src="/static/robot.png" />
            <view class="bubble">
              <text>我是你当前视频的智能AI，我可以根据当前视频的内容回答问题</text>
            </view>
          </view>


          <view v-for="(msg, index) in chatMessages.filter((_, i) => i > 5 || showContext)" :key="index"
            :class="['message', msg.role]">
            <u-avatar v-if="msg.role === 'system' && index !== 0" size="48" src="/static/robot.png" />
            <view class="bubble" v-if="index !== 0">
              <text v-html="formatText(msg.content)"></text>
            </view>
          </view>
        </scroll-view>
        <view class="input-area">
          <input v-model="userQuestion" :disabled="chatThinking" placeholder="请输入关于视频的问题…" class="question-input"
            @confirm="sendQuestion" />
          <button :disabled="!userQuestion.trim() || chatThinking" @click="sendQuestion" class="ask-button">发送</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { onPullDownRefresh, onReachBottom } from "@dcloudio/uni-app"
import DPlayer from 'dplayer'
import { onLoad } from '@dcloudio/uni-app'
import { getVideoInfo, instrtVideoInfo } from '@/api/videosInfo.js'
import { IMG_URL, API_URL } from '@/utils/config'
import { getVideoById } from '@/api/video.js'
import { getsubtitles } from '@/api/subtitles.js'
import * as echarts from 'echarts'
import 'echarts-wordcloud'

//基础变量
let dp = null, barChart = null, wordCloudChart = null

const list1 = [
  { name: '字幕内容' },
  { name: '视频介绍' },
  { name: '教学大纲' },
  { name: '内容摘要' },
  { name: '视频总结' },
  { name: '图表分析' },
  { name: '课件分析' },
  { name: 'AI问答' }
]

const videoId = ref(0)
const currentTab = ref(0)
const parsedSubtitles = ref([])
const teachingOutline = ref('')
const contentSummary = ref('')
const structuredOutput = ref('')
const videoIntroduction = ref('')
const subtitlesUrl = ref('')
const loadingTeachingOutline = ref(false)
const loadingContentSummary = ref(false)
const loadingStructuredOutput = ref(false)
const coursewareAnalysis = ref('')
const loadingCoursewareAnalysis = ref(false)

// 字幕相关
const selectedSubtitle = ref('')
const subtitleOptions = ref([])

const keywordCloudData = ref([
  { name: '人工智能', value: 42 },
  { name: '语义理解', value: 15 }
])

const yAxisData = ref([])
const seriesData = ref([])
/* -------- AI 问答 -------- */
const userQuestion = ref('')
const chatMessages = ref([])
const chatThinking = ref(false)
const chatScrollTop = ref(0)
const showContext = ref(true)

function scrollChatToBottom() {
  nextTick(() => (chatScrollTop.value = 99999))
}

async function sendQuestion() {
  if (!userQuestion.value.trim() || chatThinking.value) return
  chatMessages.value.push({ role: 'user', content: userQuestion.value.trim() })
  scrollChatToBottom()
  userQuestion.value = ''
  chatThinking.value = true
  const aiIdx = chatMessages.value.length
  chatMessages.value.push({ role: 'assistant', content: '' })
  try {
    const res = await fetch(`${API_URL}/qwenTextAI/stream/flux`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(chatMessages.value)
    })
    if (!res.ok) throw new Error('网络异常')
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const ln of lines) {
        if (ln.startsWith('data:')) {
          chatMessages.value[aiIdx].content += ln.slice(5).trim()
          scrollChatToBottom()
        }
      }
    }
  } catch (e) {
    uni.showToast({ title: '网络异常', icon: 'none' })
  } finally {
    chatThinking.value = false
  }
}

// 生命周期
onLoad(opts => {
  if (opts?.id) videoId.value = Number(opts.id)
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

// 页面加载
// 修改后的 onMounted
onMounted(async () => {
  try {
    await loadData();
  } catch (error) {
    console.error('页面加载失败:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
});


// 加载数据函数
async function loadData() {
  try {
    const [{ data: vData }] = await Promise.all([
      getVideoById(videoId.value)
    ])

    videoIntroduction.value = vData.description

    // 获取所有字幕选项
    const { data: subData } = await getsubtitles(videoId.value)
    console.log('字幕数据:', subData)

    if (subData && subData.length > 0) {
      subtitleOptions.value = subData.map(item => ({
        languageType: item.languagType || item.languageType || '未知语言', // 兼容后端拼写错误
        url: IMG_URL + item.subtitleUrl
      }))

      // 设置默认选中的字幕为第一个选项
      selectedSubtitle.value = subtitleOptions.value[0].url
      subtitlesUrl.value = subData[0].subtitleUrl
    }

    initDPlayer(vData, selectedSubtitle.value)

    const { data: info } = await getVideoInfo(videoId.value)
    teachingOutline.value = info.teachingOutline || ''
    contentSummary.value = info.contentSummary || ''
    structuredOutput.value = info.structuredOutput || ''
    coursewareAnalysis.value = info.coursewareAnalysis || ''

    // 加载图表分析数据
    keywordCloudData.value = JSON.parse(info.sumUp)
    console.log('关键词云数据:', keywordCloudData.value)

    console.log('视频信息:', info.tableText)
    yAxisData.value = JSON.parse(info.tableText)[0].yAxis
    seriesData.value = JSON.parse(info.tableText)[0].series
    console.log('图表数据:', yAxisData.value, seriesData.value)

    // 组装系统上下文
    const ctx = [
      teachingOutline.value ? `【教学大纲】${teachingOutline.value}` : '',
      contentSummary.value ? `【内容摘要】${contentSummary.value}` : '',
      structuredOutput.value ? `【视频总结】${structuredOutput.value}` : '',
      coursewareAnalysis.value ? `【课件分析】${coursewareAnalysis.value}` : ''
    ].filter(Boolean).join('\n\n')

    chatMessages.value = [{
      role: 'system',
      content: `以下是本视频的系统资料，请基于这些资料回答用户问题：\n\n${ctx},不要输出JSON和Markdown表格的内容，直接生成可以展示的输出。`
    }]

    await fetchSubtitles()
    window.jumpToTime = jumpToTime
  } finally {
    // 停止下拉刷新动画
    uni.stopPullDownRefresh()
  }
}

// 刷新数据
function refreshData() {
  loadData()
}

// 初始化DPlayer
function initDPlayer(vData, subtitleUrl) {
  // 如果已存在播放器，先销毁
  if (dp) {
    dp.destroy()
  }

  dp = new DPlayer({
    container: document.getElementById('dplayer-container'),
    autoplay: false,
    theme: '#1677ff',
    video: {
      url: IMG_URL + vData.url,
      type: 'auto'
    },
    subtitle: {
      url: subtitleUrl,
      type: 'webvtt',
      fontSize: '20px',
      bottom: '40px',
      color: '#fff'
    },
    pluginOptions: {
      subtitle: {
        defaultShow: true
      }
    }
  })
}

// 字幕解析
async function fetchSubtitles() {
  if (selectedSubtitle.value) {
    uni.request({
      url: selectedSubtitle.value,
      success: res => {
        if (res.statusCode === 200) parsedSubtitles.value = parseVtt(res.data);
      }
    });
  }
}

function parseVtt(vtt) {
  return vtt
    .split('\n')
    .reduce((acc, line) => {
      const m = line.match(/(\d{2}:\d{2}:\d{2}\.\d{3}) --> (\d{2}:\d{2}:\d{2}\.\d{3})/);
      if (m) {
        acc.push({ startTime: parseVttTime(m[1]), text: '' });
      } else if (acc.length && line.trim() && !line.includes('WEBVTT')) {
        acc[acc.length - 1].text += line.trim() + ' ';
      }
      return acc;
    }, [])
    .map(s => ({ ...s, text: s.text.trim() }));
}

function parseVttTime(t) {
  const [h, m, s] = t.split(':').map(Number);
  return h * 3600 + m * 60 + s;
}

// 切换字幕
function changeSubtitle() {
  if (dp) {
    // 重新初始化播放器以切换字幕
    getVideoById(videoId.value).then(({ data: vData }) => {
      initDPlayer(vData, selectedSubtitle.value);
      // 重新获取字幕内容用于显示
      fetchSubtitles();
    });
  }
}

// Tab 切换
function handleTabClick(item) {
  currentTab.value = item.index;
  const tab = list1[item.index].name;
  if (tab === '图表分析') nextTick(() => setTimeout(initCharts, 100));
  if (tab === '教学大纲' && !teachingOutline.value) startStream('teachingOutline');
  if (tab === '内容摘要' && !contentSummary.value) startStream('contentSummary');
  if (tab === '视频总结' && !structuredOutput.value) startStream('structuredOutput');
  if (tab === '课件分析' && !coursewareAnalysis.value) startStream('coursewareAnalysis');
}

// SSE 流
function startStream(target) {
  let url = `${API_URL}/qwenTextAI/stream`
    + `?subtitlesUrl=${encodeURIComponent(subtitlesUrl.value)}&id=${videoId.value}&next=${target}`;
  if (target === 'teachingOutline') url += `我只要教学大纲，自动划分章节结构，生成教学大纲，不用分的太细，每条20字左右,要自动划分章节结构好比第一章，第二章,...`;
  if (target === 'structuredOutput') url += `要先来个全部总结，然后在是分段总结,提炼视频内容，总结核心内容，关键点。`;
  if (target === 'coursewareAnalysis') url += `我要课件分析，给我根据内容来点建议，内容是：${teachingOutline.value+structuredOutput.value+contentSummary.value}`;
  if (target === 'teachingOutline') loadingTeachingOutline.value = true;
  if (target === 'contentSummary') loadingContentSummary.value = true;
  if (target === 'structuredOutput') loadingStructuredOutput.value = true;
  if (target === 'coursewareAnalysis') loadingCoursewareAnalysis.value = true;
  const src = new EventSource(url);
  let buffer = '';
  src.onmessage = e => {
    buffer += e.data;
    if (target === 'teachingOutline') teachingOutline.value = buffer;
    if (target === 'contentSummary') contentSummary.value = buffer;
    if (target === 'structuredOutput') structuredOutput.value = buffer;
    if (target === 'coursewareAnalysis') coursewareAnalysis.value = buffer;
  };
  src.onerror = () => {
    src.close();
    loadingTeachingOutline.value = false;
    loadingContentSummary.value = false;
    loadingStructuredOutput.value = false;
    loadingCoursewareAnalysis.value = false;
    instrtVideoInfo({
      videoId: videoId.value,
      teachingOutline: teachingOutline.value,
      contentSummary: contentSummary.value,
      structuredOutput: structuredOutput.value,
      coursewareAnalysis: coursewareAnalysis.value
    });
  };
}

// 图表初始化
function initCharts() {
  const barDom = document.getElementById('chart-container');
  if (barDom) {
    barChart && barChart.dispose();
    barChart = echarts.init(barDom);
    barChart.setOption({
      title: {
        text: '章节重点度',
        left: 'right'
      },
      tooltip: {
        trigger: 'item' // 对于饼图或玫瑰图，使用 'item' 触发器
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          radius: '50%',
          name: '重点度',
          type: 'pie',
          color: ['#FF6B6B', '#4ECDC4', '#5D4037', '#FFD166', '#2E8BC0', '#F94144', '#F8961E', '#F9C74F', '#90BE6D', '#43AA8B',],
          data: yAxisData.value.map((name, index) => ({
            value: seriesData.value[index],
            name: name
          })),

          label: {
            show: true,
            formatter: '{b}: {c} ({d}%)'
          }
        }
      ]
    });
  }
  const cloudDom = document.getElementById('keyword-cloud-container');
  if (cloudDom) {
    wordCloudChart && wordCloudChart.dispose();
    wordCloudChart = echarts.init(cloudDom);
    wordCloudChart.setOption({
      title: {
        text: '字幕关键词云',
        left: 'center'
      },
      series: [{
        type: 'wordCloud',
        shape: 'circle',
        left: 'center',
        top: 'center',
        width: '90%',
        height: '90%',
        sizeRange: [12, 50],
        rotationRange: [-45, 45],
        gridSize: 8,
        textStyle: {
          fontFamily: 'sans-serif',
          fontWeight: 'bold',
          color: () => `rgb(${Math.round(Math.random() * 160 + 40)}, ${Math.round(
            Math.random() * 160 + 40
          )}, ${Math.round(Math.random() * 160 + 40)})`
        },
        data: keywordCloudData.value
      }]
    });
  }
}

// 工具
function jumpToTime(sec) {
  if (dp) {
    dp.seek(sec);
    dp.play();
  }
}

function parseTimeString(t) {
  const p = t.split(':').map(Number);
  return p.length === 2 ? p[0] * 60 + p[1] : p[0] * 3600 + p[1] * 60 + p[2];
}

function formatText(text) {
  if (!text) return '';
  const html = text
    .replace(/\*\*([^*]+)\*\*/g, '<span style="font-weight:bold;">$1</span>')
    .replace(/VTT/g, '')
    // 你已有的时间戳/换行等逻辑继续放在下面
    .replace(/\\n/g, '\n')
    .replace(/#/g, '')
    .replace(/\n/g, '<br/>')
    .replace(/(\d{2}:\d{2}:\d{2}|\d{2}:\d{2})/g, match => {
      const sec = parseTimeString(match);
      return `<span class="time-link" onclick="window.jumpToTime(${sec})" style="color:#2979ff;cursor:pointer;text-decoration:underline;">${match}</span>`;
    });
  return html;
}

// 课件下载
function downloadCourseware() {
  const downloadUrl = `${IMG_URL}/courseware/${videoId.value}.pptx`;
  uni.downloadFile({
    url: downloadUrl,
    success: res => {
      if (res.statusCode === 200) {
        uni.saveFile({
          tempFilePath: res.tempFilePath,
          success: () => uni.showToast({ title: '课件已保存' })
        });
      }
    },
    fail: () => uni.showToast({ title: '下载失败', icon: 'none' })
  });
}
</script>

<style scoped>
.page {
  --primary: #1677ff;
  --primary-light: #e6f4ff;
  --bg: #edebeb;
  --card: #ffffff;
  --text: #1a1a1a;
  --text-light: #252525;
  --border: #e5e7eb;
  --shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  --radius: 12rpx;
  --font: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Arial, sans-serif;
  max-width: 800rpx;
  margin: 0 auto;
  min-height: 100vh;
  padding: 24rpx;
  background-color: var(--bg);
  font-family: var(--font);
}

.sticky-wrapper {
  position: sticky;
  top: 0;
  background-color: var(--card);
  z-index: 99;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  margin-bottom: 32rpx;
}

.player-wrapper {
  margin-bottom: 32rpx;
}

.dplayer-box {
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}

.tabs-wrapper {
  margin-bottom: 24rpx;
}

.panel {
  background: var(--card);
  border-radius: var(--radius);
  padding: 32rpx;
  box-shadow: var(--shadow);
  margin-bottom: 24rpx;
}

.panel-title {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 16rpx;
}

.panel-body,
.html-content {
  font-size: 30rpx;
  line-height: 1.75;
  color: var(--text-light);
}

.panel-tip {
  font-size: 26rpx;
  color: var(--text-light);
  margin-bottom: 24rpx;
}

.subtitle-selector {
  margin-bottom: 24rpx;
  padding: 16rpx;
  background-color: var(--primary-light);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
}

.subtitle-list li {
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--border);
  font-size: 28rpx;
  color: var(--text-light);
  cursor: pointer;
}

.subtitle-list li:hover {
  color: var(--primary);
}

.time-link {
  color: var(--primary);
  cursor: pointer;
  text-decoration: underline;
  font-weight: 600;
}

#chart-container,
#keyword-cloud-container {
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  margin-top: 24rpx;
}

.feedback-textarea,
.question-input {
  width: 100%;
  padding: 20rpx 0;
  margin-bottom: 10rpx;
  font-size: 28rpx;
  border: 1rpx solid var(--border);
  border-radius: var(--radius);
  background: var(--card);
  color: var(--text);
}

.submit-button,
.ask-button {
  background-color: var(--primary);
  color: #fff;
  font-size: 28rpx;
  border: none;
  border-radius: var(--radius);
  transition: background 0.2s;
}

.submit-button:hover,
.ask-button:hover {
  background-color: #0958d9;
}

.chat-box {
  height: 650rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.message {
  display: flex;
  margin-bottom: 20rpx;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .bubble {
  background: #2979ff;
  color: #fff;
  margin-right: 20rpx;
}

.message.assistant .bubble {
  background: #fff;
  margin-left: 20rpx;
}

.bubble {
  max-width: 70%;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  font-size: 28rpx;
  line-height: 1.6;
}

.typing {
  color: #fefefe62;
}

::v-deep .dplayer-subtitle {
  background-color: #c4c4c4a9;
}

.download-button {
  background-color: var(--primary);
  color: #fff;
  padding: 20rpx 32rpx;
  font-size: 28rpx;
  border: none;
  border-radius: var(--radius);
  transition: background 0.2s;
}

.download-button:hover {
  background-color: #0958d9;
}
</style>