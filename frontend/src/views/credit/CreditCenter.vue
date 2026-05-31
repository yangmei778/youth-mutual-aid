<template>
  <div class="credit-center page-container">
    <!-- 信用分展示 -->
    <el-card class="score-card" shadow="hover">
      <div class="score-display">
        <div class="score-number">{{ creditInfo.score || 0 }}</div>
        <div class="score-level">
          <el-tag :type="creditLevel.type" size="large" effect="dark">
            {{ creditLevel.badge }} {{ creditLevel.label }}
          </el-tag>
        </div>
        <div class="score-progress">
          <span class="progress-label">{{ progressLabel }}</span>
          <el-progress :percentage="progressPercent" :stroke-width="12" :color="progressColor" />
        </div>
      </div>
    </el-card>

    <!-- 信用等级说明 -->
    <el-card class="level-card" shadow="hover">
      <template #header>
        <span>信用等级说明</span>
      </template>
      <el-row :gutter="12">
        <el-col :span="4" v-for="level in levelDescs" :key="level.name">
          <div class="level-item" :class="{ active: creditInfo.score >= level.min }">
            <div class="level-badge">{{ level.badge }}</div>
            <div class="level-name">{{ level.name }}</div>
            <div class="level-range">{{ level.min }}+分</div>
          </div>
        </el-col>
      </el-row>
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

// 统一等级体系（与 CreditBadge.vue 和 store 一致）
const levelDescs = [
  { name: '新手', badge: '🌱', min: 0, color: '#67C23A' },
  { name: '铜牌', badge: '🥉', min: 21, color: '#E6A23C' },
  { name: '银牌', badge: '🥈', min: 51, color: '#909399' },
  { name: '金牌', badge: '🥇', min: 101, color: '#F5A623' },
  { name: '钻石', badge: '💎', min: 201, color: '#9B59B6' },
]

const creditLevel = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 201) return { label: '钻石', badge: '💎', type: 'danger' }
  if (s >= 101) return { label: '金牌', badge: '🥇', type: 'warning' }
  if (s >= 51) return { label: '银牌', badge: '🥈', type: '' }
  if (s >= 21) return { label: '铜牌', badge: '🥉', type: 'success' }
  return { label: '新手', badge: '🌱', type: 'info' }
})

const progressLabel = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 201) return '已达最高等级'
  if (s >= 101) return `距离钻石还需 ${201 - s} 分`
  if (s >= 51) return `距离金牌还需 ${101 - s} 分`
  if (s >= 21) return `距离银牌还需 ${51 - s} 分`
  return `距离铜牌还需 ${21 - s} 分`
})

const progressPercent = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 201) return 100
  if (s >= 101) return ((s - 101) / 100) * 100
  if (s >= 51) return ((s - 51) / 50) * 100
  if (s >= 21) return ((s - 21) / 30) * 100
  return (s / 21) * 100
})

const progressColor = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 201) return '#9B59B6'
  if (s >= 101) return '#F5A623'
  if (s >= 51) return '#909399'
  if (s >= 21) return '#E6A23C'
  return '#67C23A'
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
    creditLogs.value = res.data?.data?.records || res.data?.data || res.data?.records || []
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

.level-card {
  margin-bottom: 20px;
}

.level-item {
  text-align: center;
  padding: 12px 0;
  border-radius: 8px;
  transition: all 0.3s;
}

.level-item.active {
  background: #f0f9eb;
}

.level-badge {
  font-size: 28px;
}

.level-name {
  font-size: 14px;
  font-weight: 500;
  margin: 4px 0;
}

.level-range {
  font-size: 12px;
  color: #909399;
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
