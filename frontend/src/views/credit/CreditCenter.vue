<template>
  <div class="credit-center page-container">
    <!-- 信用分展示 -->
    <el-card class="score-card" shadow="hover">
      <div class="score-display">
        <div class="score-number">{{ creditInfo.score || 0 }}</div>
        <div class="score-level">
          <el-tag :type="creditLevel.type" size="large" effect="dark">{{ creditLevel.label }}</el-tag>
        </div>
        <div class="score-progress">
          <span class="progress-label">距离下一等级还需 {{ needScore }} 分</span>
          <el-progress :percentage="progressPercent" :stroke-width="12" :color="progressColor" />
        </div>
      </div>
    </el-card>

    <!-- 信用变动记录 -->
    <el-card class="log-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>信用变动记录</span>
        </div>
      </template>
      <el-table :data="creditLogs" v-loading="logsLoading" stripe>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column prop="changeValue" label="变动" width="120">
          <template #default="{ row }">
            <span :class="row.changeValue > 0 ? 'change-positive' : 'change-negative'">
              {{ row.changeValue > 0 ? '+' : '' }}{{ row.changeValue }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column prop="beforeScore" label="变动前" width="100" />
        <el-table-column prop="afterScore" label="变动后" width="100" />
      </el-table>
      <el-empty v-if="!logsLoading && creditLogs.length === 0" description="暂无变动记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { creditApi } from '@/api'

const creditInfo = ref({ score: 0, level: '' })
const creditLogs = ref([])
const logsLoading = ref(false)

const creditLevel = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 90) return { label: '优秀', type: 'success' }
  if (s >= 70) return { label: '良好', type: '' }
  if (s >= 50) return { label: '一般', type: 'warning' }
  return { label: '待提升', type: 'danger' }
})

const needScore = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 90) return 0
  if (s >= 70) return 90 - s
  if (s >= 50) return 70 - s
  return 50 - s
})

const progressPercent = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 90) return 100
  if (s >= 70) return ((s - 70) / 20) * 100
  if (s >= 50) return ((s - 50) / 20) * 100
  return (s / 50) * 100
})

const progressColor = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 90) return '#67C23A'
  if (s >= 70) return '#409EFF'
  if (s >= 50) return '#E6A23C'
  return '#F56C6C'
})

async function fetchCreditInfo() {
  try {
    const res = await creditApi.getCreditInfo()
    creditInfo.value = res.data?.data || res.data || { score: 0 }
  } catch (e) {
    ElMessage.error('获取信用信息失败')
  }
}

async function fetchCreditLogs() {
  logsLoading.value = true
  try {
    const res = await creditApi.getCreditLogs({ page: 1, size: 20 })
    creditLogs.value = res.data?.data?.records || res.data?.data || []
  } catch (e) {
    // ignore
  } finally {
    logsLoading.value = false
  }
}

onMounted(() => {
  fetchCreditInfo()
  fetchCreditLogs()
})
</script>

<style scoped>
.score-card {
  margin-bottom: 20px;
}

.score-display {
  text-align: center;
  padding: 20px 0;
}

.score-number {
  font-size: 72px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
  margin-bottom: 12px;
}

.score-level {
  margin-bottom: 20px;
}

.score-progress {
  max-width: 400px;
  margin: 0 auto;
}

.progress-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.log-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.change-positive {
  color: #67C23A;
  font-weight: bold;
}

.change-negative {
  color: #F56C6C;
  font-weight: bold;
}
</style>
