<template>
  <div class="credit-center page-container">
    <el-tabs v-model="activeTab">
      <!-- 我的信用 -->
      <el-tab-pane label="我的信用" name="mine">
        <!-- 信用分展示 -->
        <el-card class="score-card" shadow="hover">
          <div class="score-display">
            <div class="score-number">{{ creditInfo.score || 0 }}</div>
            <div class="score-level">
              <el-tag :type="creditLevel.type" size="large" effect="dark">
                <el-icon :size="16"><component :is="creditLevel.icon" /></el-icon> {{ creditLevel.label }}
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
          <template #header><span>信用等级说明</span></template>
          <el-row :gutter="12">
            <el-col :span="4" v-for="level in levelDescs" :key="level.name">
              <div class="level-item" :class="{ active: creditInfo.score >= level.min }">
                <div class="level-badge">
                  <el-icon :size="28" :color="level.color"><component :is="level.icon" /></el-icon>
                </div>
                <div class="level-name">{{ level.name }}</div>
                <div class="level-range">{{ level.min }}+分</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 信用变动记录 -->
        <el-card class="log-card" shadow="hover">
          <template #header><span>信用变动记录</span></template>
          <el-table :data="creditLogs" v-loading="logsLoading" stripe>
            <el-table-column prop="createdAt" label="时间" width="180" />
            <el-table-column prop="changeValue" label="变动" width="100">
              <template #default="{ row }">
                <span :class="row.changeValue > 0 ? 'change-positive' : 'change-negative'">
                  {{ row.changeValue > 0 ? '+' : '' }}{{ row.changeValue }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="200" />
            <el-table-column prop="beforeScore" label="变动前" width="80" />
            <el-table-column prop="afterScore" label="变动后" width="80" />
          </el-table>
          <el-empty v-if="!logsLoading && creditLogs.length === 0" description="暂无变动记录" />
        </el-card>
      </el-tab-pane>

      <!-- 互助成就 -->
      <el-tab-pane label="互助成就" name="achievements">
        <el-card shadow="hover" v-loading="achLoading">
          <template #header><span>🏆 互助成就徽章</span></template>
          <div class="achievement-grid">
            <div v-for="ach in achievements" :key="ach.name" class="achievement-item" :class="{ earned: ach.earned }">
              <span class="ach-icon">{{ ach.icon }}</span>
              <div class="ach-info">
                <span class="ach-name">{{ ach.name }}</span>
                <span class="ach-desc">{{ ach.desc }}</span>
              </div>
              <el-tag v-if="ach.earned" type="success" size="small">已获得</el-tag>
              <el-tag v-else type="info" size="small">未获得</el-tag>
            </div>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 信用排行 -->
      <el-tab-pane label="信用排行" name="ranking">
        <el-card shadow="hover" v-loading="rankingLoading">
          <template #header>
            <div class="card-header">
              <span>🏆 信用分排行榜</span>
              <el-button text type="primary" @click="fetchLeaderboard">刷新</el-button>
            </div>
          </template>
          <div class="ranking-list">
            <div
              v-for="item in leaderboard"
              :key="item.userId"
              class="ranking-item"
              :class="getRankClass(item.rank)"
            >
              <div class="rank-number">
                <template v-if="item.rank === 1">🥇</template>
                <template v-else-if="item.rank === 2">🥈</template>
                <template v-else-if="item.rank === 3">🥉</template>
                <template v-else>{{ item.rank }}</template>
              </div>
              <el-avatar :size="40" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
              <div class="rank-info">
                <span class="rank-name">{{ item.nickname }}</span>
                <el-tag size="small" effect="plain">{{ item.levelName }}</el-tag>
              </div>
              <div class="rank-score">
                <span class="score-value">{{ item.creditScore }}</span>
                <span class="score-label">信用分</span>
              </div>
            </div>
            <el-empty v-if="!rankingLoading && leaderboard.length === 0" description="暂无排行数据" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { creditApi } from '@/api'
import { Sunrise, Medal, Trophy } from '@element-plus/icons-vue'

const activeTab = ref('mine')

// ====== 信用信息 ======
const creditInfo = ref({ score: 0 })
const creditLogs = ref([])
const logsLoading = ref(false)

const levelDescs = [
  { name: '新手', icon: Sunrise, min: 0, color: '#67C23A' },
  { name: '铜牌', icon: Medal, min: 21, color: '#E6A23C' },
  { name: '银牌', icon: Medal, min: 51, color: '#909399' },
  { name: '金牌', icon: Medal, min: 101, color: '#F5A623' },
  { name: '钻石', icon: Trophy, min: 201, color: '#9B59B6' },
]

const creditLevel = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 201) return { label: '钻石', icon: Trophy, type: 'danger' }
  if (s >= 101) return { label: '金牌', icon: Medal, type: 'warning' }
  if (s >= 51) return { label: '银牌', icon: Medal, type: '' }
  if (s >= 21) return { label: '铜牌', icon: Medal, type: 'success' }
  return { label: '新手', icon: Sunrise, type: 'info' }
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

// ====== 排行榜 ======
const leaderboard = ref([])
const rankingLoading = ref(false)

function getRankClass(rank) {
  if (rank === 1) return 'rank-top rank-1'
  if (rank === 2) return 'rank-top rank-2'
  if (rank === 3) return 'rank-top rank-3'
  return ''
}

async function fetchCreditInfo() {
  try {
    const res = await creditApi.getCreditInfo()
    creditInfo.value = res.data?.data || res.data || { score: 0 }
  } catch {
    ElMessage.error('获取信用信息失败')
  }
}

async function fetchCreditLogs() {
  logsLoading.value = true
  try {
    const res = await creditApi.getCreditLogs({ page: 1, size: 20 })
    creditLogs.value = res.data?.data?.records || res.data?.data || res.data?.records || []
  } catch {
    // ignore
  } finally {
    logsLoading.value = false
  }
}

async function fetchLeaderboard() {
  rankingLoading.value = true
  try {
    const res = await creditApi.getLeaderboard(20)
    leaderboard.value = res.data?.data || res.data || []
  } catch {
    // ignore
  } finally {
    rankingLoading.value = false
  }
}

// ====== 成就 ======
const achievements = ref([])
const achLoading = ref(false)
async function fetchAchievements() {
  achLoading.value = true
  try {
    const res = await creditApi.getAchievements()
    achievements.value = res.data?.data || res.data || []
  } catch { /* ignore */ }
  finally { achLoading.value = false }
}

onMounted(() => {
  fetchCreditInfo()
  fetchCreditLogs()
  fetchLeaderboard()
  fetchAchievements()
})
</script>

<style scoped>
.score-card { margin-bottom: 20px; }
.score-display { text-align: center; padding: 20px 0; }
.score-number { font-size: 72px; font-weight: bold; color: #409EFF; line-height: 1; margin-bottom: 12px; }
.score-level { margin-bottom: 20px; }
.score-progress { max-width: 400px; margin: 0 auto; }
.progress-label { display: block; font-size: 14px; color: #909399; margin-bottom: 8px; }

.level-card { margin-bottom: 20px; }
.level-item { text-align: center; padding: 12px 0; border-radius: 8px; transition: all 0.3s; }
.level-item.active { background: #f0f9eb; }
.level-badge { font-size: 28px; }
.level-name { font-size: 14px; font-weight: 500; margin: 4px 0; }
.level-range { font-size: 12px; color: #909399; }

.log-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.change-positive { color: #67C23A; font-weight: bold; }
.change-negative { color: #F56C6C; font-weight: bold; }

/* ====== 排行榜 ====== */
.ranking-list { min-height: 200px; }

.ranking-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  transition: background 0.2s;

  &:hover { background: #f5f7fa; }

  & + & { border-top: 1px solid #f0f0f0; }
}

.rank-number {
  width: 36px;
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.rank-top {
  background: #fef9e7;
  border-radius: var(--radius-md);

  & + & { border-top: none; }

  .rank-number { font-size: 28px; }
}

.rank-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;

  .rank-name { font-size: 15px; font-weight: 500; color: var(--text-primary); }
}

.rank-score {
  text-align: right;
  .score-value { font-size: 20px; font-weight: 700; color: var(--primary-color); display: block; }
  .score-label { font-size: 12px; color: var(--text-secondary); }
}

/* ====== 成就 ====== */
.achievement-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.achievement-item {
  display: flex; align-items: center; gap: 14px; padding: 16px 20px;
  border-radius: 12px; border: 1px solid #edf0f4; background: #fafbfc;
  transition: all 0.3s;
  &.earned { background: #f0f9eb; border-color: rgba(103,194,58,0.2); }
}
.ach-icon { font-size: 28px; flex-shrink: 0; }
.ach-info { flex: 1; display: flex; flex-direction: column; }
.ach-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.ach-desc { font-size: 12px; color: #909399; }

@media (max-width: 768px) { .achievement-grid { grid-template-columns: 1fr; } }
</style>
