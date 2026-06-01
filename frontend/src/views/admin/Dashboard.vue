<template>
  <div class="admin-dashboard page-container">
    <h2 style="margin-bottom: 20px">管理看板</h2>

    <!-- 第一行：核心统计 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="card in topCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <el-statistic :title="card.label" :value="card.value">
            <template #prefix>
              <el-icon :size="20" :color="card.color"><component :is="card.icon" /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：扩展统计 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="card in bottomCards" :key="card.label">
        <el-card shadow="hover" class="stat-card stat-card-sm">
          <div class="mini-stat">
            <el-icon :size="18" :color="card.color"><component :is="card.icon" /></el-icon>
            <div class="mini-stat-body">
              <span class="mini-stat-value">{{ card.value }}</span>
              <span class="mini-stat-label">{{ card.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：图表 -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>技能分类分布</span></template>
          <div ref="pieChartRef" class="chart-container"></div>
          <el-empty v-if="!hasCategoryData" description="暂无数据" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>近7天新增内容趋势</span></template>
          <div ref="trendChartRef" class="chart-container"></div>
          <el-empty v-if="!hasTrendData" description="暂无数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Cpu, Box, Calendar, Connection, Warning, Plus, DataAnalysis } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import * as echarts from 'echarts'

const stats = ref({
  userCount: 0,
  skillCount: 0,
  goodsCount: 0,
  activityCount: 0,
  mutualRecordCount: 0,
  pendingAuditCount: 0,
  recentUsers: 0,
  activeUsers: 0,
  categoryDistribution: [],
  dailyTrend: [],
})

const hasCategoryData = computed(() => stats.value.categoryDistribution?.length > 0)
const hasTrendData = computed(() => stats.value.dailyTrend?.length > 0)

const topCards = computed(() => [
  { label: '用户总数', value: stats.value.userCount, icon: User, color: '#409EFF' },
  { label: '技能数量', value: stats.value.skillCount, icon: Cpu, color: '#67C23A' },
  { label: '物品数量', value: stats.value.goodsCount, icon: Box, color: '#E6A23C' },
  { label: '活动数量', value: stats.value.activityCount, icon: Calendar, color: '#F56C6C' },
])

const bottomCards = computed(() => [
  { label: '互助记录', value: stats.value.mutualRecordCount, icon: Connection, color: '#409EFF' },
  { label: '待审核报名', value: stats.value.pendingAuditCount, icon: Warning, color: '#E6A23C' },
  { label: '近7天新增用户', value: stats.value.recentUsers, icon: Plus, color: '#67C23A' },
  { label: '近30天活跃用户', value: stats.value.activeUsers, icon: DataAnalysis, color: '#9B59B6' },
])

// ====== ECharts ======
const pieChartRef = ref(null)
const trendChartRef = ref(null)
let pieChart = null
let trendChart = null

function renderPieChart() {
  if (!pieChartRef.value || !hasCategoryData.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '50%'],
      data: stats.value.categoryDistribution,
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' },
      },
      label: { formatter: '{b}: {c}' },
    }],
  })
}

function renderTrendChart() {
  if (!trendChartRef.value || !hasTrendData.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  const data = stats.value.dailyTrend
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['技能', '物品', '活动'], bottom: 0 },
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: data.map(d => d.date.slice(5)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '技能', type: 'bar', data: data.map(d => d.skill), stack: 'total', color: '#409EFF' },
      { name: '物品', type: 'bar', data: data.map(d => d.goods), stack: 'total', color: '#67C23A' },
      { name: '活动', type: 'bar', data: data.map(d => d.activity), stack: 'total', color: '#E6A23C' },
    ],
  })
}

async function fetchStats() {
  try {
    const res = await adminApi.getStats()
    stats.value = res.data?.data || res.data || stats.value
    await nextTick()
    renderPieChart()
    renderTrendChart()
  } catch {
    ElMessage.error('获取统计数据失败')
  }
}

// 窗口大小变化时重绘
function handleResize() {
  pieChart?.resize()
  trendChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  margin-bottom: 12px;

  :deep(.el-statistic__head) {
    font-size: 14px;
    color: #909399;
  }

  :deep(.el-statistic__content) {
    font-size: 24px;
  }
}

.stat-card-sm :deep(.el-card__body) {
  padding: 16px;
}

.mini-stat {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mini-stat-body {
  display: flex;
  flex-direction: column;
}

.mini-stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.mini-stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.chart-card {
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
