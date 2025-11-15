<template>
  <div class="thread-pool-monitor">
    <!-- 带背景图的刷新按钮 -->
    <el-button type="primary" @click="fetchStatus" :loading="loading" icon="Refresh"
      style="background: linear-gradient(135deg, #409EFF, #337ecc)" round>
      刷新状态
    </el-button>

    <!-- 带阴影的卡片容器 -->
    <el-row :gutter="24" style="margin-top: 24px;">
      <!-- 核心参数卡片 -->
      <el-col :span="12">
        <el-card shadow="hover" class="info-card parameter-card">
          <template #header>
            <div class="card-header">
              <el-icon name="Setting" style="margin-right: 8px; color: #409EFF"></el-icon>
              <span>核心参数</span>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <div class="label">核心线程数</div>
              <div class="value">{{ data.corePoolSize }}</div>
            </div>
            <div class="info-item">
              <div class="label">最大线程数</div>
              <div class="value">{{ data.maxPoolSize }}</div>
            </div>
            <div class="info-item">
              <div class="label">队列容量</div>
              <div class="value">{{ data.queueCapacity }}</div>
            </div>
            <div class="info-item">
              <div class="label">保活时间</div>
              <div class="value">{{ data.keepAliveSeconds }} 秒</div>
            </div>
            <div class="info-item">
              <div class="label">线程前缀</div>
              <div class="value">{{ data.poolName }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 运行状态卡片 -->
      <el-col :span="12">
        <el-card shadow="hover" class="info-card status-card">
          <template #header>
            <div class="card-header">
              <el-icon name="DataBoard" style="margin-right: 8px; color: #67C23A"></el-icon>
              <span>运行状态</span>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <div class="label">当前线程数</div>
              <div class="value">{{ data.currentPoolSize }}</div>
            </div>
            <div class="info-item">
              <div class="label">活跃线程数</div>
              <div class="value">{{ data.activeThreads }}</div>
            </div>
            <div class="info-item">
              <div class="label">已完成任务</div>
              <div class="value">{{ data.completedTasks }}</div>
            </div>
            <div class="info-item">
              <div class="label">队列任务数</div>
              <div class="value">{{ data.queueSize }}</div>
            </div>
            <div class="info-item">
              <div class="label">线程名称</div>
              <div class="value">taskExecutor1</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图卡片 -->
    <el-card style="margin-top: 24px; border-radius: 12px;" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon name="TrendChart" style="margin-right: 8px; color: #F56C6C"></el-icon>
          <span>线程与队列趋势图</span>
        </div>
      </template>
      <div ref="trendChart" style="height: 320px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getThreadPoolStatus } from '@/api/thread'

// 数据
const data = ref({
  activeThreads: 0,
  completedTasks: 0,
  queueSize: 0,
  corePoolSize: 2,
  currentPoolSize: 0,
  queueCapacity: 2,
  maxPoolSize: 2,
  keepAliveSeconds: 60,
  poolName: 'executor-1-'
})

const loading = ref(false)
const trendChart = ref(null)
let trendInstance = null

const timeData = ref([])
const activeData = ref([])
const queueData = ref([])

const generateTime = () => new Date().toLocaleTimeString()

// 修改 fetchStatus 使用真实 API
const fetchStatus = async () => {
  loading.value = true
  try {
    const res = await getThreadPoolStatus()
    if (res.code === 200) {
      data.value = res.data

      // 更新趋势数据
      timeData.value.push(generateTime())
      activeData.value.push(data.value.activeThreads)
      queueData.value.push(data.value.queueSize)

      if (timeData.value.length > 10) {
        timeData.value.shift()
        activeData.value.shift()
        queueData.value.shift()
      }

      updateTrendChart()
      ElMessage.success('刷新成功')
    } else {
      ElMessage.error(res.message || '获取失败')
    }
  } catch (err) {
    console.error('请求线程池状态失败:', err)
    ElMessage.error('网络错误，无法获取线程池状态')
  } finally {
    loading.value = false
  }
}

// 更新趋势图
const updateTrendChart = () => {
  trendInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.9)',
      textStyle: {
        color: '#333'
      }
    },
    legend: {
      data: ['活跃线程', '队列任务'],
      top: 10
    },
    grid: {
      left: '6%',
      right: '6%',
      bottom: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: timeData.value,
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: '#ccc'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#ccc'
        }
      }
    },
    series: [
      {
        name: '活跃线程',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        itemStyle: {
          color: '#409EFF',
          borderWidth: 2
        },
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.1)'
        },
        data: activeData.value
      },
      {
        name: '队列任务',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#67C23A',
          width: 2
        },
        itemStyle: {
          color: '#67C23A',
          borderWidth: 2
        },
        areaStyle: {
          color: 'rgba(103, 194, 58, 0.1)'
        },
        data: queueData.value
      }
    ]
  })
}

// 初始化图表
onMounted(() => {
  trendInstance = echarts.init(trendChart.value)
  updateTrendChart()
  fetchStatus()
  const timer = setInterval(fetchStatus, 2000)
  onUnmounted(() => clearInterval(timer))
})
</script>

<style scoped>
.thread-pool-monitor {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.card-header {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  background: #f9fafc;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.label {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.value {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.parameter-card:hover {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.status-card:hover {
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.15);
}

/* 添加响应式设计 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
