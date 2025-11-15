<template>
  <div class="all-device">
    <!-- 介绍卡片 -->
    <el-card shadow="never" class="intro-card" v-if="showIntro">
      <template #header>
        <div class="intro-header">
          <span>视频解析系统 V2.0</span>
          <el-icon class="close-btn" @click="showIntro = false">
            <Close />
          </el-icon>
        </div>
      </template>
      <p class="intro-text">
        本系统增加了<b>字幕生成（支持多种语言）</b>、<b>教学大纲自动生成</b>和<b>视频内容总结</b>，<b>课件分析</b>，<b>RAG + Tool
          Calling</b>，<b>PostgreSQL+pgvector</b>，<b>AI视频筛选</b>，<b>流量分析</b>，<b>后台系统</b>，<b>用户管理</b>，<b>视频管理</b>，<b>本地知识库</b>，<b>实时答疑</b>等。
      </p>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top: 18px;ma">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-title">视频分类情况</div>
          </template>
          <div ref="pieRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <el-col :ls="30" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-title">一周视频解析情况（每周一更新）</div>
          </template>
          <div ref="barRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 流量管理图表区域 -->
    <el-card shadow="hover" style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <div class="card-title">流量管理监控（每周一更新）</div>
        </div>
      </template>
      <div ref="trafficRef" class="chart-box"></div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  MarkLineComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { Close } from '@element-plus/icons-vue'

// 注册ECharts组件
echarts.use([
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  MarkLineComponent,
  CanvasRenderer
])

// 介绍卡片
const showIntro = ref(true)

// 图表 DOM
const pieRef = ref<HTMLDivElement>()
const barRef = ref<HTMLDivElement>()
const trafficRef = ref<HTMLDivElement>()

// 渲染饼图（视频分类流量分布）
function renderPie() {
  if (!pieRef.value) return
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: [50, 150],
        center: ['50%', '50%'],
        data: [
          { value: 13, name: 'java' },
          { value: 10, name: 'python' },
          { value: 7, name: '运维/AI/Linux' },
          { value: 5, name: '网络' },
          { value: 8, name: '前端' },
          { value: 16, name: '测试' },
          { value: 10, name: '产品' },
          { value: 6, name: 'UI' }
        ],
        roseType: 'area',
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    ]
  });
}

// 渲染柱状图（近7日流量趋势）
function renderBar() {
  if (!barRef.value) return
  const chart = echarts.init(barRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLabel: { rotate: 45 }
    },
    yAxis: { type: 'value', name: '视频解析数量' },
    series: [
      {
        type: 'bar',
        name: '视频解析',
        data: [15, 10, 13, 6, 16, 8, 6],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#764ba2' },
            { offset: 1, color: '#a679ec' }
          ])
        }
      }
    ]
  })
}

// 渲染流量管理监控图表
function renderTraffic() {
  if (!trafficRef.value) return;

  const chart = echarts.init(trafficRef.value);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['上传流量', '下载流量', '总流量', '监控录像', '课程视频', '安全培训', '其他']
    },
    xAxis: [
      {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '流量总量'
      }
    ],
    series: [
      {
        name: '上传流量',
        type: 'bar',
        emphasis: {
          focus: 'series'
        },
        data: [32, 33, 30, 33, 39, 33, 32]
      },
      {
        name: '下载流量',
        type: 'bar',
        stack: '流量类型',
        emphasis: {
          focus: 'series'
        },
        data: [12, 13, 10, 13, 9, 23, 21]
      },
      {
        name: '总流量',
        type: 'bar',
        data: [86, 102, 96, 103, 168, 160, 157],
        emphasis: {
          focus: 'series'
        },
        markLine: {
          lineStyle: {
            type: 'dashed'
          },
          data: [[{ type: 'min' }, { type: 'max' }]]
        }
      },

      {
        name: '其他',
        type: 'bar',
        stack: '视频分类',
        emphasis: {
          focus: 'series'
        },
        data: [6, 8, 9, 8, 11, 11, 12]
      }
    ]
  };

  chart.setOption(option);

  // 响应窗口大小变化
  window.addEventListener('resize', () => {
    chart.resize();
  });
}

onMounted(async () => {
  await nextTick()
  renderPie()
  renderBar()
  renderTraffic() // 初始化流量管理监控图表
})
</script>

<style scoped>
.intro-card {
  margin: 0 16px 0 16px;
  border-left: 4px solid #764ba2;
}

.intro-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
}

.close-btn {
  cursor: pointer;
  color: #909399;
}

.close-btn:hover {
  color: #764ba2;
}

.intro-text {
  font-size: 14px;
  margin: 20px;
  color: #666;
  line-height: 1.6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart-box {
  width: 100%;
  height: 420px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 14px 20px;
}

:deep(.el-card__body) {
  padding: 0;
}
</style>