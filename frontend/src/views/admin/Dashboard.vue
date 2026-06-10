<template>
  <div class="admin-dashboard page-container">
    <h2 style="margin-bottom: 20px">管理看板</h2>

    <!-- 统计卡片 2行4列 -->
    <div class="stats-grid">
      <div v-for="card in allCards" :key="card.label" class="stat-card" :class="{ clickable: card.link, alert: card.isAlert && card.value > 0 }" @click="card.link && $router.push(card.link)">
        <div class="sc-icon" :style="{ color: card.color }">
          <el-icon :size="20"><component :is="card.icon" /></el-icon>
        </div>
        <div class="sc-body">
          <span class="sc-value" :class="{ 'val-danger': card.isAlert && card.value > 0 }">{{ card.value }}</span>
          <span class="sc-label">{{ card.label }}</span>
        </div>
        <span v-if="card.isAlert && card.value > 0" class="sc-badge">{{ card.value }}</span>
      </div>
    </div>

    <!-- 第三行：操作日志 -->
    <el-card shadow="hover" class="chart-card">
      <template #header><span>最近操作日志</span></template>
      <div class="log-list" v-if="logs.length">
        <div v-for="l in logs" :key="l.id" class="log-item">
          <span class="log-action" :class="'act-'+l.action">{{ actionLabel(l.action) }}</span>
          <span class="log-target">{{ l.target }}</span>
          <span class="log-admin">{{ l.adminName }}</span>
          <span class="log-time">{{ formatLogTime(l.createdAt) }}</span>
        </div>
      </div>
      <div v-else class="empty-tip">暂无操作记录</div>
    </el-card>

    <!-- 图表 -->
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
import { User, Cpu, Box, Calendar, Connection, Warning, WarningFilled, Plus, DataAnalysis } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import request from '@/utils/request'
import * as echarts from 'echarts'

const logs = ref([])
function actionLabel(a) { const m={ban_user:'封禁用户',unban_user:'解封用户',offline_post:'下架内容',delete_post:'删除内容',handle_report:'处理举报',adjust_credit:'调整信用'}; return m[a]||a }
function formatLogTime(t) { if(!t)return''; const d=new Date(t); return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` }
async function fetchLogs() { try { const r = await request.get('/v1/admin/logs'); logs.value = r.data?.data || r.data || [] } catch {} }

const stats = ref({
  userCount: 0,
  skillCount: 0,
  goodsCount: 0,
  activityCount: 0,
  mutualRecordCount: 0,
  pendingAuditCount: 0,
  pendingReportCount: 0,
  recentUsers: 0,
  activeUsers: 0,
  categoryDistribution: [],
  dailyTrend: [],
})

const hasCategoryData = computed(() => stats.value.categoryDistribution?.length > 0)
const hasTrendData = computed(() => stats.value.dailyTrend?.length > 0)

const allCards = computed(() => [
  { label: '用户总数', value: stats.value.userCount, icon: User, color: '#409EFF', link: '/admin/users' },
  { label: '技能数量', value: stats.value.skillCount, icon: Cpu, color: '#409EFF', link: '/admin/audit' },
  { label: '物品数量', value: stats.value.goodsCount, icon: Box, color: '#67C23A', link: '/admin/audit' },
  { label: '活动数量', value: stats.value.activityCount, icon: Calendar, color: '#E6A23C', link: '/admin/audit' },
  { label: '互助记录', value: stats.value.mutualRecordCount, icon: Connection, color: '#409EFF' },
  { label: '待审核报名', value: stats.value.pendingAuditCount, icon: Warning, color: '#E6A23C', link: '/admin/audit', isAlert: true },
  { label: '待处理举报', value: stats.value.pendingReportCount, icon: WarningFilled, color: '#F56C6C', link: '/admin/reports', isAlert: true },
  { label: '近7天新增用户', value: stats.value.recentUsers, icon: Plus, color: '#67C23A', link: '/admin/users' },
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
  fetchLogs()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 24px; }

.stat-card { position: relative; background: #fff; border: 1px solid #edf0f4; border-radius: 14px; padding: 18px 20px; display: flex; align-items: center; gap: 14px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); transition: all 0.2s;
  &.clickable { cursor: pointer; &:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.06); } }
  &.alert { border-color: #fbc4c4; background: #fefafa; }
}

.sc-icon { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: rgba(64,158,255,0.06); flex-shrink: 0; }
.sc-body { min-width: 0; }
.sc-value { display: block; font-size: 24px; font-weight: 800; color: var(--text-primary); line-height: 1.1; }
.sc-value.val-danger { color: #f56c6c; }
.sc-label { font-size: 12px; color: #909399; }
.sc-badge { position: absolute; top: -6px; right: -6px; background: #f56c6c; color: #fff; font-size: 10px; font-weight: 700; min-width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; border-radius: 9px; padding: 0 4px; }

.chart-card { margin-bottom: 16px; }
.chart-container { width: 100%; height: 320px; }

.log-list { max-height: 300px; overflow-y: auto; }
.log-item { display: flex; gap: 14px; padding: 10px 0; font-size: 13px; align-items: center; &+& { border-top: 1px solid #f5f6f8; } }
.log-action { padding: 2px 8px; border-radius: 5px; font-size: 11px; font-weight: 600; white-space: nowrap;
  &.act-ban_user,&.act-delete_post { background: #fef0f0; color: #f56c6c; }
  &.act-unban_user { background: #f0f9eb; color: #67c23a; }
  &.act-offline_post,&.act-adjust_credit { background: #fef9e7; color: #e6a23c; }
  &.act-handle_report { background: rgba(64,158,255,0.08); color: #409eff; }
}
.log-target { flex: 1; color: var(--text-primary); }
.log-admin { color: #909399; white-space: nowrap; }
.log-time { color: #b0b8c4; font-size: 12px; white-space: nowrap; }
.empty-tip { text-align: center; padding: 32px; color: #b0b8c4; font-size: 13px; }

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 500px) { .stats-grid { grid-template-columns: 1fr; } }
</style>
