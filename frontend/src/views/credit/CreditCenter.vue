<template>
  <div class="credit-center page-container">
    <div class="cc-layout">
      <!-- 左侧导航 -->
      <div class="cc-sidebar">
        <div class="sidebar-title">信用中心</div>
        <div class="sidebar-nav">
          <div class="nav-item" :class="{ active: activeTab === 'mine' }" @click="activeTab = 'mine'">
            <el-icon :size="18"><Medal /></el-icon><span>我的信用</span>
          </div>
          <div class="nav-item" :class="{ active: activeTab === 'achievements' }" @click="activeTab = 'achievements'">
            <el-icon :size="18"><Trophy /></el-icon><span>互助成就</span>
          </div>
          <div class="nav-item" :class="{ active: activeTab === 'ranking' }" @click="activeTab = 'ranking'">
            <el-icon :size="18"><Star /></el-icon><span>信用排行</span>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="cc-content">
        <!-- ====== 我的信用 ====== -->
        <div v-if="activeTab === 'mine'">
          <!-- 分数卡片 -->
          <div class="score-hero" :class="'level-' + creditLevel.class">
            <div class="sh-bg-ring"></div>
            <div class="sh-inner">
              <div class="sh-icon">
                <el-icon :size="32"><component :is="creditLevel.icon" /></el-icon>
              </div>
              <div class="sh-score-wrap">
                <span class="sh-score">{{ creditInfo.score || 0 }}</span>
                <span class="sh-unit">信用分</span>
              </div>
              <div class="sh-level-tag">{{ creditLevel.label }}</div>
            </div>
            <div class="sh-progress">
              <span class="sh-prog-label">{{ progressLabel }}</span>
              <el-progress :percentage="progressPercent" :stroke-width="10" :color="progressColor" />
            </div>
          </div>

          <!-- 等级阶梯 -->
          <div class="level-ladder">
            <div v-for="(lv, i) in levelDescs" :key="lv.name" class="ladder-step"
              :class="{ reached: creditInfo.score >= lv.min, current: isCurrentLevel(lv, i) }">
              <div class="ls-dot">
                <el-icon :size="16"><component :is="lv.icon" /></el-icon>
              </div>
              <div class="ls-info">
                <span class="ls-name">{{ lv.name }}</span>
                <span class="ls-min">{{ lv.min }}分</span>
              </div>
              <div v-if="i < levelDescs.length - 1" class="ls-line"></div>
            </div>
          </div>

          <!-- 变动记录 -->
          <div class="cc-card">
            <div class="cc-head">信用变动记录</div>
            <div class="log-timeline" v-loading="logsLoading">
              <div v-for="log in creditLogs" :key="log.id" class="log-item" :class="log.changeValue > 0 ? 'log-up' : 'log-down'">
                <div class="log-dot"></div>
                <div class="log-body">
                  <div class="log-reason">{{ log.reason }}</div>
                  <div class="log-meta">
                    <span class="log-change" :class="log.changeValue>0?'c-up':'c-down'">
                      {{ log.changeValue > 0 ? '+' : '' }}{{ log.changeValue }}
                    </span>
                    <span class="log-score">{{ log.beforeScore }} → {{ log.afterScore }}</span>
                    <span class="log-time">{{ log.createdAt }}</span>
                  </div>
                </div>
              </div>
              <div v-if="!logsLoading&&creditLogs.length===0" class="empty-tip">暂无变动记录</div>
            </div>
          </div>
        </div>

        <!-- ====== 互助成就 ====== -->
        <div v-if="activeTab === 'achievements'">
          <div class="cc-card" v-loading="achLoading">
            <div class="cc-head">互助成就徽章</div>
            <div class="ach-progress-bar">
              <el-progress :percentage="achPercent" :stroke-width="8" :color="'#67c23a'" />
              <span class="ach-count">{{ earnedCount }}/{{ achievements.length }} 已获得</span>
            </div>
            <div class="ach-grid">
              <div v-for="ach in achievements" :key="ach.name" class="ach-card" :class="{ earned: ach.earned }">
                <div class="ach-card-icon">{{ ach.icon }}</div>
                <div class="ach-card-body">
                  <span class="a-name">{{ ach.name }}</span>
                  <span class="a-desc">{{ ach.desc }}</span>
                </div>
                <el-tag v-if="ach.earned" type="success" size="small" effect="dark" round>已获得</el-tag>
                <el-tag v-else type="info" size="small" effect="plain" round>未获得</el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- ====== 信用排行 ====== -->
        <div v-if="activeTab === 'ranking'">
          <div class="cc-card" v-loading="rankingLoading">
            <div class="cc-head">
              <span>信用分排行榜</span>
              <el-button text type="primary" size="small" @click="fetchLeaderboard">刷新</el-button>
            </div>
            <!-- 前三名 -->
            <div class="podium" v-if="leaderboard.length >= 3">
              <div class="podium-item podium-2" v-if="leaderboard[1]">
                <el-avatar :size="52" :src="leaderboard[1].avatar">{{ leaderboard[1].nickname?.charAt(0) }}</el-avatar>
                <span class="p-name">{{ leaderboard[1].nickname }}</span>
                <span class="p-score">{{ leaderboard[1].creditScore }}</span>
                <span class="p-medal">🥈</span>
              </div>
              <div class="podium-item podium-1" v-if="leaderboard[0]">
                <el-avatar :size="60" :src="leaderboard[0].avatar">{{ leaderboard[0].nickname?.charAt(0) }}</el-avatar>
                <span class="p-name">{{ leaderboard[0].nickname }}</span>
                <span class="p-score">{{ leaderboard[0].creditScore }}</span>
                <span class="p-medal">🥇</span>
              </div>
              <div class="podium-item podium-3" v-if="leaderboard[2]">
                <el-avatar :size="48" :src="leaderboard[2].avatar">{{ leaderboard[2].nickname?.charAt(0) }}</el-avatar>
                <span class="p-name">{{ leaderboard[2].nickname }}</span>
                <span class="p-score">{{ leaderboard[2].creditScore }}</span>
                <span class="p-medal">🥉</span>
              </div>
            </div>
            <!-- 4-N名 -->
            <div class="rank-list">
              <div v-for="item in leaderboard.slice(3)" :key="item.userId" class="rank-row">
                <span class="rr-num">{{ item.rank }}</span>
                <el-avatar :size="32" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
                <span class="rr-name">{{ item.nickname }}</span>
                <el-tag size="small" effect="plain">{{ item.levelName }}</el-tag>
                <span class="rr-score">{{ item.creditScore }}</span>
              </div>
            </div>
            <div v-if="!rankingLoading&&leaderboard.length===0" class="empty-tip">暂无排行数据</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { creditApi } from '@/api'
import { Sunrise, Medal, Trophy, Star } from '@element-plus/icons-vue'

const activeTab = ref('mine')
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
  if (s >= 201) return { label: '钻石', icon: Trophy, type: 'danger', class: 'diamond' }
  if (s >= 101) return { label: '金牌', icon: Medal, type: 'warning', class: 'gold' }
  if (s >= 51) return { label: '银牌', icon: Medal, type: '', class: 'silver' }
  if (s >= 21) return { label: '铜牌', icon: Medal, type: 'success', class: 'bronze' }
  return { label: '新手', icon: Sunrise, type: 'info', class: 'novice' }
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
  if (s >= 201) return 100; if (s >= 101) return ((s-101)/100)*100
  if (s >= 51) return ((s-51)/50)*100; if (s >= 21) return ((s-21)/30)*100
  return (s/21)*100
})
const progressColor = computed(() => {
  const s = creditInfo.value.score || 0
  if (s >= 201) return '#9B59B6'; if (s >= 101) return '#F5A623'
  if (s >= 51) return '#909399'; if (s >= 21) return '#E6A23C'
  return '#67C23A'
})
function isCurrentLevel(lv, i) {
  const s = creditInfo.value.score || 0
  const next = levelDescs[i+1]
  return s >= lv.min && (!next || s < next.min)
}

const leaderboard = ref([]); const rankingLoading = ref(false)
async function fetchCreditInfo() {
  try { const r = await creditApi.getCreditInfo(); creditInfo.value = r.data?.data || r.data || { score: 0 } }
  catch { ElMessage.error('获取信用信息失败') }
}
async function fetchCreditLogs() {
  logsLoading.value = true
  try { const r = await creditApi.getCreditLogs({ page: 1, size: 20 }); creditLogs.value = r.data?.records || r.data?.data?.records || [] }
  catch {} finally { logsLoading.value = false }
}
async function fetchLeaderboard() {
  rankingLoading.value = true
  try { const r = await creditApi.getLeaderboard(20); leaderboard.value = r.data?.data || r.data || [] }
  catch {} finally { rankingLoading.value = false }
}

const achievements = ref([]); const achLoading = ref(false)
const earnedCount = computed(() => achievements.value.filter(a => a.earned).length)
const achPercent = computed(() => achievements.value.length ? Math.round(earnedCount.value / achievements.value.length * 100) : 0)
async function fetchAchievements() {
  achLoading.value = true
  try { const r = await creditApi.getAchievements(); achievements.value = r.data?.data || r.data || [] }
  catch {} finally { achLoading.value = false }
}

onMounted(() => { fetchCreditInfo(); fetchCreditLogs(); fetchLeaderboard(); fetchAchievements() })
</script>

<style scoped>
.credit-center { max-width: 1000px; }
.cc-layout { display: flex; gap: 0; min-height: 500px; background: #fff; border-radius: 18px; border: 1px solid #edf0f4; overflow: hidden; }
.cc-sidebar { width: 180px; flex-shrink: 0; background: #f8f9fb; border-right: 1px solid #edf0f4; padding: 20px 0; }
.sidebar-title { font-size: 15px; font-weight: 700; color: var(--text-primary); padding: 0 20px 16px; }
.sidebar-nav { display: flex; flex-direction: column; gap: 2px; padding: 0 12px; }
.nav-item {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px; border-radius: 10px;
  cursor: pointer; transition: all 0.2s; font-size: 14px; color: var(--text-regular);
  &:hover { background: rgba(64,158,255,0.06); color: var(--primary-color); }
  &.active { background: rgba(64,158,255,0.08); color: var(--primary-color); font-weight: 600; }
}
.cc-content { flex: 1; padding: 24px 28px; overflow-y: auto; }

/* ====== 分数卡片 ====== */
.score-hero {
  position: relative; border-radius: 20px; padding: 32px; text-align: center;
  margin-bottom: 24px; overflow: hidden;
  background: linear-gradient(135deg, #f0f5ff 0%, #f8f9fd 50%, #fef9f0 100%);
  &.level-diamond { background: linear-gradient(135deg, #f5f0ff, #fdf5ff, #fef9f0); }
  &.level-gold { background: linear-gradient(135deg, #fef9f0, #fef5e0, #f8f9fd); }
}
.sh-bg-ring {
  position: absolute; top: -40px; right: -40px;
  width: 200px; height: 200px; border-radius: 50%;
  border: 20px solid rgba(64,158,255,0.04);
}
.sh-inner { position: relative; z-index: 1; }
.sh-icon {
  width: 60px; height: 60px; margin: 0 auto 14px; display: flex; align-items: center; justify-content: center;
  border-radius: 18px; background: linear-gradient(135deg, rgba(64,158,255,0.1), rgba(123,47,247,0.05));
  color: var(--primary-color);
}
.sh-score-wrap { margin-bottom: 8px; }
.sh-score { font-size: 64px; font-weight: 900; color: var(--text-primary); line-height: 1; }
.sh-unit { font-size: 16px; color: #909399; margin-left: 4px; }
.sh-level-tag {
  display: inline-block; padding: 4px 18px; border-radius: 20px;
  font-size: 14px; font-weight: 700; color: var(--primary-color);
  background: rgba(64,158,255,0.08);
}
.sh-progress { max-width: 360px; margin: 20px auto 0; }
.sh-prog-label { display: block; font-size: 13px; color: #909399; margin-bottom: 8px; }

/* ====== 等级阶梯 ====== */
.level-ladder { display: flex; justify-content: space-between; margin-bottom: 24px; padding: 20px 16px; background: #fff; border: 1px solid #edf0f4; border-radius: 16px; }
.ladder-step { display: flex; flex-direction: column; align-items: center; gap: 6px; position: relative; flex: 1; }
.ls-dot {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  border-radius: 50%; background: #f5f6f8; color: #b0b8c4; transition: all 0.3s;
}
.ladder-step.reached .ls-dot { background: rgba(64,158,255,0.1); color: var(--primary-color); }
.ladder-step.current .ls-dot { background: var(--primary-color); color: #fff; box-shadow: 0 0 0 6px rgba(64,158,255,0.15); }
.ls-info { text-align: center; }
.ls-name { font-size: 12px; font-weight: 600; color: var(--text-primary); display: block; }
.ls-min { font-size: 11px; color: #b0b8c4; }
.ls-line {
  position: absolute; top: 18px; left: 50%; width: 100%; height: 2px;
  background: #edf0f4; z-index: 0;
}
.ladder-step.reached .ls-line { background: rgba(64,158,255,0.2); }

/* ====== 卡片 ====== */
.cc-card { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 24px; }
.cc-head { font-size: 15px; font-weight: 700; padding-bottom: 14px; border-bottom: 2px solid #f5f6f8; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }

/* ====== 时间线 ====== */
.log-timeline { max-height: 400px; overflow-y: auto; }
.log-item { display: flex; gap: 14px; padding: 14px 0; &+& { border-top: 1px solid #f5f6f8; } }
.log-dot {
  width: 10px; height: 10px; border-radius: 50%; margin-top: 6px; flex-shrink: 0;
  .log-up & { background: #67c23a; }
  .log-down & { background: #f56c6c; }
}
.log-body { flex: 1; }
.log-reason { font-size: 14px; color: var(--text-primary); font-weight: 500; margin-bottom: 4px; }
.log-meta { display: flex; gap: 12px; font-size: 12px; color: #b0b8c4; }
.c-up { color:#67c23a; font-weight:700; }.c-down { color:#f56c6c; font-weight:700; }

/* ====== 成就 ====== */
.ach-progress-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; .el-progress { flex: 1; } }
.ach-count { font-size: 13px; color: #909399; white-space: nowrap; }
.ach-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.ach-card {
  display: flex; align-items: center; gap: 14px; padding: 16px 20px;
  border-radius: 14px; border: 1px solid #edf0f4; background: #fafbfc; transition: all 0.3s;
  &.earned { background: #f0f9eb; border-color: rgba(103,194,58,0.2); }
}
.ach-card-icon { font-size: 30px; flex-shrink: 0; }
.ach-card-body { flex: 1; display: flex; flex-direction: column; }
.a-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.a-desc { font-size: 12px; color: #909399; }

/* ====== 排行榜 ====== */
.podium { display: flex; justify-content: center; align-items: flex-end; gap: 16px; padding: 24px 0; }
.podium-item { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.p-name { font-size: 13px; font-weight: 600; }
.p-score { font-size: 16px; font-weight: 800; color: var(--primary-color); }
.p-medal { font-size: 28px; }
.rank-list { display: flex; flex-direction: column; }
.rank-row {
  display: flex; align-items: center; gap: 12px; padding: 12px 0;
  &+& { border-top: 1px solid #f5f6f8; }
  .rr-num { width: 28px; text-align: center; font-weight: 700; color: #909399; }
  .rr-name { flex: 1; font-size: 14px; font-weight: 500; }
  .rr-score { font-size: 14px; font-weight: 700; color: var(--primary-color); }
}
.empty-tip { text-align: center; padding: 40px 0; color: #b0b8c4; font-size: 13px; }

@media (max-width: 768px) {
  .cc-layout { flex-direction: column; }
  .cc-sidebar { width: 100%; border-right: none; border-bottom: 1px solid #edf0f4; }
  .sidebar-nav { flex-direction: row; padding-bottom: 12px; }
  .ach-grid { grid-template-columns: 1fr; }
}
</style>
