<template>
	<view class="chat-container">
		<!-- 滚动消息列表 -->
		<scroll-view :scroll-into-view="scrollToView" scroll-y="true" class="message-list-scroll-view"
			:style="{ height: scrollViewHeight }" @scrolltoupper="getHistoryChat" :scroll-with-animation="!isFirstListChat">
			<u-loadmore v-if="loadHistory" status="loading" />
			<view v-for="message in messageList" :key="message.id" :id="`message${message.id}`" class="message-item-wrapper">
				<!-- 左边 AI 消息 -->
				<view v-if="message.type === 0" class="message-item-left">
					<u--image :src="you.avatar" :width="'40px'" :height="'40px'" radius="8" :showLoading="true" />
					<view class="message-bubble ai-bubble">
						<text class="bubble-text" v-html="formatText(message.content)"></text>
					</view>
				</view>

				<!-- 右边用户消息 -->
				<view v-if="message.type === 1" class="message-item-right">
					<view class="message-bubble user-bubble">
						<text class="bubble-text">{{ message.content }}</text>
					</view>
					<u--image :width="'40px'" :height="'40px'" radius="8" :showLoading="true" />
				</view>
			</view>
		</scroll-view>

		<!-- 输入区域 -->
		<view class="input-container">
			<view class="input-wrapper">
				<view class="message-input">
					<u--textarea ref="textareaRef" v-model="messageInput" placeholder="请输入消息内容" autoHeight @focus="keyboardUp"
						@confirm="sendMessage" />
				</view>
				<view class="send-button" @click="sendMessage">
					<text>发送</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted, onBeforeMount } from 'vue'
import { getVideoListOpen } from "@/api/video.js"
import { API_URL } from '../../utils/config'
// 用户与 AI 信息
const me = ref({})
const you = ref({
	nickname: '智能客服',
	avatar:
		'https://tse4-mm.cn.bing.net/th/id/OIP-C.cm8W7HDVsi62PM5dceWJxwHaEt?r=0&rs=1&pid=ImgDetMain&cb=idpwebp2&o=7&rm=3',
	username: '智能客服',
	isAI: true,
})

// 消息列表与输入
const messageList = ref([
	{
		id: 1,
		type: 0,
		content: '你好！我是你的rag视频解析智能助手，我可以帮你选择适合你的视频。',
	},
])
const messageInput = ref('')

// 滚动控制
const scrollToView = ref('')
const scrollViewHeight = ref('')

// 分页
const loadHistory = ref(false)
const isFirstListChat = ref(true)

// 常量
const Constant = {
	softType: 1,
}

const videoList = ref([]);


// 键盘弹起处理
const keyboardUp = (value, height) => {
	if (Constant.softType === 1) {
		chatSuitable(height)
	}
}

const chatSuitable = (keyBoardHeight) => {
	scrollViewHeight.value = `calc(100vh - 60px - ${keyBoardHeight}px)`
	scrollToView.value = ''
	setTimeout(() => {
		scrollToView.value = `message${messageList.value[messageList.value.length - 1]?.id || ''}`
	}, 150)
}

const toBottom = () => {
	scrollToView.value = ''
	setTimeout(() => {
		const lastMsg = messageList.value[messageList.value.length - 1]
		scrollToView.value = lastMsg ? `message${lastMsg.id}` : ''
	}, 150)
}

// 发送消息
const sendMessage = async () => {
	if (!messageInput.value.trim()) return

	// 添加用户的消息到列表中
	const userMessage = {
		id: Date.now(),
		type: 1,
		content: messageInput.value,
	}
	messageList.value.push(userMessage)
	toBottom()
	messageInput.value = ''

	// 构建请求体，包含完整的对话历史作为上下文
	const requestBody = messageList.value.map(msg => ({
		role: msg.type === 0 ? 'assistant' : 'user', // 根据消息类型确定角色
		content: msg.content,
	})).concat([{
		role: 'system',
		content: '你是视频解析助手，你可以根据用户提供的分析他需要什么视频。',
	}, {
		role: 'system',
		content: `这是我目前拥有的视频信息：${JSON.stringify(videoList.value)}`, // 使用JSON.stringify确保正确格式化视频信息
	}])

	try {
		const res = await fetch(`${API_URL}/qwenTextAI/stream/flux`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				token: uni.getStorageSync('token'),
				Authorization: uni.getStorageSync('token'),
			},
			body: JSON.stringify(requestBody),
		})

		if (!res.ok) throw new Error('请求失败')

		const reader = res.body.getReader()
		const decoder = new TextDecoder()

		const aiMessage = {
			id: Date.now() + 1,
			type: 0,
			content: '',
		}
		messageList.value.push(aiMessage)
		toBottom()

		let isDone = false
		while (!isDone) {
			const { done, value } = await reader.read()
			isDone = done
			if (done) break

			const chunk = decoder.decode(value, { stream: true })
			const lines = chunk
				.split('\n')
				.filter(line => line.startsWith('data:'))
				.map(line => line.slice(5).trim())

			for (const line of lines) {
				if (line === '[DONE]') continue
				try {
					const parsed = JSON.parse(line)
					if (parsed.text) {
						aiMessage.content += parsed.text
					}
				} catch (e) {
					aiMessage.content += line
				}
			}

			messageList.value = [...messageList.value]
			toBottom()
		}
	} catch (error) {
		uni.showToast({ title: 'AI 回复失败', icon: 'none' })
		console.error('发送消息失败:', error)
	}
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
// 获取历史消息
const getHistoryChat = () => {
	// TODO: 实现分页加载历史消息
}

// 生命周期
onBeforeMount(() => {
	me.value = uni.getStorageSync('curUser') || {}
})

onMounted(async () => {

	const { data } = await getVideoListOpen();
	console.log(data);
	videoList.value = data;
})
</script>

<style scoped>
/* 整体容器 */
.chat-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	/* background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%); */
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
}

/* 消息列表滚动视图 */
.message-list-scroll-view {
	flex: 1;
	padding: 16px 12px 0 12px;
}

/* 消息项包装器 */
.message-item-wrapper {
	margin-bottom: 12px;
}

/* 消息项布局 */
.message-item-left {
	display: flex;
	align-items: flex-start;
}

.message-item-right {
	display: flex;
	align-items: flex-start;
	justify-content: flex-end;
}

/* 消息气泡 */
.message-bubble {
	max-width: 75%;
	padding: 12px 16px;
	border-radius: 18px;
	font-size: 15px;
	line-height: 1.5;
	position: relative;
}

.ai-bubble {
	background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
	border: 1px solid #e9ecef;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.user-bubble {
	background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
	color: white;
	box-shadow: 0 2px 8px rgba(67, 160, 71, 0.2);
}

.bubble-text {
	word-break: break-word;
	color: inherit;
	line-height: 1.5;
}

/* 输入容器 */
.input-container {
	position: fixed;
	bottom: 48px;
	left: 0;
	right: 0;
	padding: 8px 12px;
	backdrop-filter: blur(10px);
	-webkit-backdrop-filter: blur(10px);
	z-index: 999;
	padding-bottom: calc(8px + env(safe-area-inset-bottom));
}

/* 输入包装器 */
.input-wrapper {
	display: flex;
	align-items: center;
	border-radius: 24px;
	padding: 4px;
	box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 输入框样式 */
.message-input {
	flex: 1;
	margin-right: 8px;
}

.message-input>>>.u-textarea {
	background-color: #ffffff !important;
	border-radius: 20px !important;
	padding: 10px 14px !important;
	font-size: 15px !important;
	height: 20px;
	border: 1px solid #dee2e6 !important;
}

.message-input>>>.u-textarea__textarea {
	color: #495057;
}

.message-input>>>.u-textarea__textarea::placeholder {
	color: #adb5bd;
}

/* 发送按钮 */
.send-button {
	padding: 10px 16px;
	background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%);
	color: white;
	border-radius: 18px;
	font-size: 15px;
	font-weight: 500;
	transition: all 0.2s ease;
}

.send-button:active {
	transform: scale(0.95);
	opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 375px) {
	.message-bubble {
		font-size: 14px;
	}

	.message-input>>>.u-textarea {
		font-size: 14px !important;
		min-height: 36px !important;
	}

	.send-button {
		font-size: 14px;
		padding: 8px 14px;
	}
}

/* 消息时间显示 */
.message-time {
	color: #6c757d;
	font-size: 12px;
	text-align: center;
	margin: 10px 0;
	padding: 2px 8px;
	background-color: rgba(255, 255, 255, 0.8);
	border-radius: 12px;
	display: inline-block;
	align-self: center;
}
</style>