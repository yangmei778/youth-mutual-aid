<template>
  <div class="skill-list page-container">
    <!-- 页面头部横幅 -->
    <div class="list-hero">
      <div class="list-hero-bg"></div>
      <div class="list-hero-content">
        <div class="list-hero-left">
          <span class="list-hero-badge">
            <svg viewBox="0 0 28 28" width="20" height="20" fill="none">
              <defs><linearGradient id="lhh" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#409eff"/><stop offset="100%" style="stop-color:#7b2ff7"/></linearGradient></defs>
              <rect x="2" y="9" width="10" height="10" rx="3" fill="url(#lhh)" opacity="0.25"/>
              <rect x="6" y="12" width="4" height="4" rx="1.5" fill="url(#lhh)" opacity="0.7"/>
              <rect x="16" y="9" width="10" height="10" rx="3" fill="url(#lhh)" opacity="0.25"/>
              <circle cx="21" cy="14" r="2.5" fill="url(#lhh)" opacity="0.7"/>
              <path d="M12 11.5l4-2M12 16.5l4 2" stroke="url(#lhh)" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            技能交换
          </span>
          <h1>发现你想要的技能</h1>
          <p>共 <strong>{{ total }}</strong> 个技能等你来探索</p>
        </div>
        <div class="list-hero-stats">
          <div class="lhs-item"><span class="lhs-num">{{ total }}</span><span class="lhs-lbl">技能总数</span></div>
        </div>
      </div>
    </div>

    <!-- 热门分类快捷标签 -->
    <div class="quick-cats">
      <span
        v-for="cat in hotCategories"
        :key="cat"
        class="quick-cat-pill"
        :class="{ active: queryParams.category === cat }"
        @click="queryParams.category = queryParams.category === cat ? '' : cat; handleSearch()"
      >{{ cat }}</span>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索技能..."
          clearable
          :prefix-icon="Search"
          class="filter-search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="queryParams.type" placeholder="全部类型" clearable @change="handleSearch" class="filter-sel">
          <el-option label="我能教" value="learn" />
          <el-option label="我想学" value="teach" />
        </el-select>
        <el-select v-model="queryParams.city" placeholder="全部城市" clearable filterable @change="handleSearch" class="filter-sel">
          <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="queryParams.sortBy" placeholder="综合排序" clearable @change="handleSearch" class="filter-sel">
          <el-option label="最新发布" value="createdAt" />
          <el-option label="信用分高→低" value="creditScore" />
        </el-select>
        <el-button type="primary" @click="handleSearch" class="filter-btn">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>
    </div>

    <!-- 智能推荐 -->
    <div v-if="showRecommendations && recommendations.length" class="recommend-section">
      <div class="recommend-header">
        <div class="recommend-title">
          <span class="rec-star">✨</span>
          <span>智能匹配推荐</span>
          <span class="rec-count">{{ recommendations.length }}个匹配</span>
        </div>
        <el-button text type="info" size="small" @click="showRecommendations = false">收起</el-button>
      </div>
      <div class="recommend-grid">
        <div v-for="rec in recommendations" :key="rec.id" class="recommend-card" @click="goDetail(rec.id)">
          <div class="rec-card-top">
            <span class="rec-match-tag">匹配 · {{ rec.category }}</span>
            <span class="rec-teach-tag">能教</span>
          </div>
          <h4>{{ rec.title }}</h4>
          <div class="rec-card-foot">
            <span class="rec-user">{{ rec.nickname || '匿名用户' }}</span>
            <span v-if="rec.creditScore" class="rec-credit">⭐ {{ rec.creditScore }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 技能卡片列表 -->
    <div v-loading="loading" class="skill-grid-wrap">
      <el-empty v-if="!loading && skillList.length === 0" description="暂无匹配的技能，试试换个条件筛选吧" />

      <div v-else class="skill-grid">
        <div
          v-for="(skill, idx) in skillList"
          :key="skill.id"
          class="skill-card"
          :style="{ animationDelay: idx * 0.04 + 's' }"
          @click="goDetail(skill.id)"
        >
          <div class="sc-accent" :class="skill.type === 'teach' ? 'accent-teach' : 'accent-learn'"></div>
          <div class="sc-body">
            <div class="sc-badges">
              <span class="sc-type" :class="skill.type === 'teach' ? 'type-teach' : 'type-learn'">
                {{ skill.type === 'teach' ? '能教' : '想学' }}
              </span>
              <span class="sc-cat">{{ skill.category }}</span>
              <span v-if="skill.userId === userStore.userInfo?.id" class="sc-mine">我的</span>
            </div>
            <h3>{{ skill.title }}</h3>
            <p class="sc-desc" v-if="skill.description">{{ skill.description }}</p>
            <div class="sc-meta">
              <div class="sc-user">
                <el-avatar :size="22" :src="skill.avatar">{{ skill.nickname?.charAt(0) || '?' }}</el-avatar>
                <span>{{ skill.nickname || '匿名用户' }}</span>
              </div>
              <div class="sc-stats">
                <span class="sc-views">👁 {{ skill.viewCount || 0 }}</span>
                <span class="sc-time">{{ formatTime(skill.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[8, 12, 16, 24]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <!-- 发布按钮 -->
    <router-link to="/skill/publish" class="publish-btn">
      <el-button type="primary" size="large" circle class="publish-fab">
        <el-icon :size="22"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { skillApi } from '@/api'
import { Search, User, View, Plus, Star, Close } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const hotCategories = ['编程', '语言', '音乐', '健身', '绘画', '摄影']
const cities = ['北京','上海','广州','深圳','杭州','成都','武汉','南京','重庆','西安']

const loading = ref(false)
const skillList = ref([])
const total = ref(0)

const recommendations = ref([])
const showRecommendations = ref(true)

async function fetchRecommendations() {
  try {
    const res = await skillApi.getRecommendations({ limit: 6 })
    recommendations.value = res.data?.data || res.data || []
  } catch { /* silent */ }
}

const queryParams = reactive({
  keyword: '', type: '', category: '', city: '', sortBy: '',
  page: 1, pageSize: 12,
})

async function fetchList() {
  loading.value = true
  try {
    const params = { ...queryParams, pageNum: params.page || queryParams.page }
    delete params.page
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) delete params[key]
    })
    const res = await skillApi.getList(params)
    skillList.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* silent */ }
  finally { loading.value = false }
}

function handleSearch() { queryParams.page = 1; fetchList() }
function goDetail(id) { router.push(`/skill/${id}`) }

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time), now = new Date(), diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff/60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff/3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff/86400000)}天前`
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

onMounted(() => { fetchList(); fetchRecommendations() })
</script>

<style lang="scss" scoped>
.skill-list { position: relative; min-height: 100%; }

/* ====== 页面头部横幅 ====== */
.list-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 24px;
  padding: 36px 32px;
  background: linear-gradient(135deg, #f0f5ff 0%, #f8f9fd 50%, #fdf6f0 100%);
}
.list-hero-bg {
  position: absolute; inset: 0;
  background:
    radial-gradient(circle at 10% 90%, rgba(64,158,255,0.08) 0%, transparent 40%),
    radial-gradient(circle at 80% 20%, rgba(123,47,247,0.05) 0%, transparent 35%);
  pointer-events: none;
}
.list-hero-content {
  position: relative; z-index: 1;
  display: flex; justify-content: space-between; align-items: center;
}
.list-hero-badge {
  display: inline-flex; align-items: center; gap: 8px;
  font-size: 13px; font-weight: 600; color: var(--primary-color);
  margin-bottom: 8px;
}
.list-hero-left {
  h1 { font-size: 26px; font-weight: 800; margin: 0 0 4px; color: var(--text-primary); }
  p { font-size: 14px; color: var(--text-secondary); margin: 0;
    strong { color: var(--primary-color); font-size: 20px; font-weight: 800; }
  }
}
.lhs-item { text-align: center; .lhs-num { display: block; font-size: 32px; font-weight: 900; color: var(--primary-color); } .lhs-lbl { font-size: 12px; color: #909399; } }

/* ====== 热门分类 ====== */
.quick-cats {
  display: flex; gap: 8px; flex-wrap: wrap;
  margin-bottom: 16px;
}
.quick-cat-pill {
  padding: 6px 16px; border-radius: 20px;
  font-size: 13px; font-weight: 500;
  background: #fff; color: var(--text-regular);
  border: 1px solid #edf0f4;
  cursor: pointer; transition: all 0.25s;
  user-select: none;

  &:hover { border-color: var(--primary-color); color: var(--primary-color); background: rgba(64,158,255,0.04); }
  &.active { background: rgba(64,158,255,0.08); color: var(--primary-color); border-color: rgba(64,158,255,0.25); font-weight: 600; }
}

/* ====== 筛选栏 ====== */
.filter-bar {
  background: #fff; border: 1px solid #edf0f4;
  border-radius: 16px; padding: 18px 20px;
  margin-bottom: 20px;
}
.filter-row {
  display: flex; gap: 10px; align-items: center; flex-wrap: wrap;
  .filter-search { flex: 1; min-width: 180px; }
  .filter-sel { width: 140px; flex-shrink: 0; }
  .filter-btn { flex-shrink: 0; border-radius: 10px; }
}

/* ====== 推荐区域 ====== */
.recommend-section {
  background: linear-gradient(135deg, #fffaeb 0%, #fff7e0 100%);
  border: 1px solid #f5dab1; border-radius: 18px;
  padding: 20px 24px; margin-bottom: 24px;
}
.recommend-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.recommend-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 700; color: var(--text-primary);
  .rec-star { font-size: 18px; }
  .rec-count { font-size: 12px; font-weight: 400; color: #b88230; background: rgba(230,162,60,0.1); padding: 2px 8px; border-radius: 10px; }
}
.recommend-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.recommend-card {
  background: #fff; border: 1px solid #f5dab1; border-radius: 14px;
  padding: 18px; cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4,0,0.2,1);

  &:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(230,162,60,0.1); border-color: var(--warning-color); }
  h4 { font-size: 15px; font-weight: 600; margin: 10px 0; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
.rec-card-top { display: flex; justify-content: space-between; align-items: center; }
.rec-match-tag { font-size: 11px; color: #b88230; background: rgba(230,162,60,0.08); padding: 2px 8px; border-radius: 6px; }
.rec-teach-tag { font-size: 11px; font-weight: 600; color: #67c23a; background: rgba(103,194,58,0.08); padding: 2px 8px; border-radius: 6px; }
.rec-card-foot { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: var(--text-secondary); }
.rec-credit { color: #e6a23c; font-weight: 600; }

/* ====== 卡片列表 ====== */
.skill-grid-wrap { min-height: 300px; }
.skill-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }

.skill-card {
  background: #fff; border: 1px solid #edf0f4;
  border-radius: 16px; overflow: hidden;
  cursor: pointer; transition: all 0.35s cubic-bezier(0.4,0,0.2,1);
  animation: cardIn 0.4s ease-out both;
  display: flex;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 14px 36px rgba(0,0,0,0.06), 0 4px 10px rgba(64,158,255,0.04);
    border-color: rgba(64,158,255,0.15);

    .sc-accent { width: 5px; }
  }
}
@keyframes cardIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }

.sc-accent {
  width: 3px; flex-shrink: 0;
  transition: width 0.3s ease;
  &.accent-teach { background: linear-gradient(180deg, #67c23a, #85d044); }
  &.accent-learn { background: linear-gradient(180deg, #409eff, #7b2ff7); }
}

.sc-body {
  flex: 1; padding: 18px 20px; min-width: 0;
}

.sc-badges {
  display: flex; gap: 8px; align-items: center;
  margin-bottom: 10px;
}

.sc-type {
  padding: 2px 10px; border-radius: 6px; font-size: 11px; font-weight: 700;
  &.type-teach { background: rgba(103,194,58,0.08); color: #52b818; }
  &.type-learn { background: rgba(64,158,255,0.08); color: #409eff; }
}
.sc-cat { font-size: 12px; color: #909399; }
.sc-mine { margin-left: auto; font-size: 11px; color: var(--primary-color); background: rgba(64,158,255,0.06); padding: 1px 8px; border-radius: 4px; font-weight: 600; }

.sc-body h3 {
  font-size: 16px; font-weight: 600; color: var(--text-primary);
  margin: 0 0 6px; line-height: 1.4;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.sc-desc {
  font-size: 13px; color: #909399; line-height: 1.5;
  margin: 0 0 12px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.sc-meta {
  display: flex; justify-content: space-between; align-items: center;
}
.sc-user {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: var(--text-secondary);
}
.sc-stats {
  display: flex; gap: 12px; font-size: 12px; color: #b0b8c4;
}

/* ====== 分页 ====== */
.pagination-wrapper { display: flex; justify-content: center; margin-top: 28px; padding-bottom: 60px; }

/* ====== 发布按钮 ====== */
.publish-btn { position: fixed; right: 40px; bottom: 80px; z-index: 10; }
.publish-fab {
  width: 52px; height: 52px;
  box-shadow: 0 6px 20px rgba(64,158,255,0.35);
  transition: all 0.3s;
  animation: fabPulse 2.5s ease-in-out infinite;

  &:hover { transform: scale(1.08); box-shadow: 0 8px 28px rgba(64,158,255,0.45); animation: none; }
}
@keyframes fabPulse { 0%, 100% { box-shadow: 0 6px 20px rgba(64,158,255,0.35); } 50% { box-shadow: 0 6px 30px rgba(64,158,255,0.55); } }

@media (max-width: 900px) {
  .skill-grid, .recommend-grid { grid-template-columns: repeat(2, 1fr); }
  .filter-row { .filter-sel { width: 120px; } }
  .list-hero-content { flex-direction: column; text-align: center; }
}
@media (max-width: 500px) {
  .skill-grid, .recommend-grid { grid-template-columns: 1fr; }
  .filter-row { flex-direction: column; .filter-sel, .filter-search { width: 100%; } }
}
</style>
