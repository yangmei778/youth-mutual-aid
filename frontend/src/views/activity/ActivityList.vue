<template>
  <div class="activity-list page-container">
    <!-- 页面头部横幅 -->
    <div class="list-hero list-hero-activity">
      <div class="list-hero-bg"></div>
      <div class="list-hero-content">
        <div class="list-hero-left">
          <span class="list-hero-badge">
            <svg viewBox="0 0 28 28" width="20" height="20" fill="none">
              <defs><linearGradient id="lha" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#f5a623"/><stop offset="100%" style="stop-color:#e6a23c"/></linearGradient></defs>
              <circle cx="8" cy="8" r="4" fill="url(#lha)" opacity="0.22"/>
              <circle cx="8" cy="8" r="2" fill="url(#lha)" opacity="0.55"/>
              <circle cx="20" cy="8" r="4" fill="url(#lha)" opacity="0.22"/>
              <circle cx="20" cy="8" r="2" fill="url(#lha)" opacity="0.55"/>
              <circle cx="14" cy="20" r="4" fill="url(#lha)" opacity="0.22"/>
              <circle cx="14" cy="20" r="2" fill="url(#lha)" opacity="0.55"/>
              <path d="M10 10.5l-1 5M18 10.5l1 5M9.5 9h9" stroke="url(#lha)" stroke-width="1.2" opacity="0.4" stroke-linecap="round"/>
            </svg>
            临时搭伴
          </span>
          <h1>找到有趣的同行伙伴</h1>
          <p>共 <strong>{{ total }}</strong> 场活动等你参加</p>
        </div>
        <div class="list-hero-stats">
          <div class="lhs-item"><span class="lhs-num">{{ total }}</span><span class="lhs-lbl">活动总数</span></div>
        </div>
      </div>
    </div>

    <!-- 热门类型 -->
    <div class="quick-cats">
      <span v-for="t in HOT_ACTIVITY_TYPES" :key="t.value" class="quick-cat-pill"
        :class="{ active: filters.type === t.value }"
        @click="filters.type = filters.type === t.value ? '' : t.value; handleSearch()">{{ t.label }}</span>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索活动..." clearable :prefix-icon="Search" class="filter-search" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-select v-model="filters.city" placeholder="城市" clearable filterable @change="handleSearch" class="filter-sel">
          <el-option v-for="c in CITIES" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" clearable @change="handleSearch" class="filter-sel">
          <el-option label="报名中" value="open" /><el-option label="已满员" value="full" /><el-option label="进行中" value="ongoing" />
        </el-select>
        <el-select v-model="filters.sortBy" placeholder="排序" clearable @change="handleSearch" class="filter-sel">
          <el-option label="最新发布" value="createdAt" /><el-option label="即将开始" value="activityTime" />
        </el-select>
        <el-button type="primary" @click="handleSearch" class="filter-btn"><el-icon><Search /></el-icon> 搜索</el-button>
        <el-button @click="resetFilters" class="filter-btn-reset">重置</el-button>
      </div>
    </div>

    <!-- 活动卡片列表 -->
    <div v-loading="loading" class="activity-grid-wrap">
      <el-empty v-if="!loading && activities.length === 0" description="暂无活动">
        <template #default>
          <div class="empty-actions">
            <el-button type="primary" @click="router.push('/activity/publish')">发起新活动</el-button>
          </div>
        </template>
      </el-empty>
      <div v-else class="activity-grid">
        <div v-for="(item, idx) in activities" :key="item.id" class="activity-card"
          :style="{ animationDelay: idx * 0.04 + 's' }" @click="goDetail(item.id)">
          <div class="ac-left">
            <div class="ac-type-icon">
              <el-icon :size="22"><component :is="typeIconMap[item.type] || MoreFilled" /></el-icon>
            </div>
          </div>
          <div class="ac-body">
            <div class="ac-top">
              <h3>{{ item.title }}
                <span v-if="item.userId === userStore.userInfo?.id" class="ac-mine">我的</span>
              </h3>
              <el-tag :type="statusTagType(item.status)" size="small" effect="plain">{{ statusLabel(item.status) }}</el-tag>
            </div>
            <div class="ac-info">
              <span><el-icon :size="14"><Clock /></el-icon> {{ formatTime(item.activityTime) }}</span>
              <span><el-icon :size="14"><Location /></el-icon> {{ item.location }}</span>
              <span><el-icon :size="14"><User /></el-icon> {{ item.currentMembers }}/{{ item.maxMembers }}人</span>
              <span v-if="item.costDesc"><el-icon :size="14"><Coin /></el-icon> {{ item.costDesc }}</span>
            </div>
            <div class="ac-foot">
              <span class="ac-pub">{{ item.nickname || '匿名' }} · {{ formatTime(item.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize" :total="total"
        :page-sizes="[10,20,30]" layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange" @current-change="handlePageChange" />
    </div>

    <router-link to="/activity/publish">
      <el-button class="publish-fab" type="primary" size="large" circle>
        <el-icon :size="22"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Search, Plus, KnifeFork, PictureFilled, MapLocation, MoreFilled, Clock, Location, User, Coin } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { activityApi } from '@/api'
import { formatTime } from '@/utils/date'
import { CITIES, HOT_ACTIVITY_TYPES, ACTIVITY_STATUS_MAP, ACTIVITY_STATUS_TAG } from '@/utils/constants'
import { useListPage } from '@/composables/useListPage'

const router = useRouter()
const userStore = useUserStore()

const {
  list: activities,
  total,
  loading,
  filters,
  pagination,
  loadList,
  handleSearch,
  resetFilters,
  handleSizeChange,
  handlePageChange,
} = useListPage(activityApi.getList, { pageSize: 10 })

const typeIconMap = { meal: KnifeFork, exhibition: PictureFilled, travel: MapLocation, other: MoreFilled }
function statusTagType(s) { return ACTIVITY_STATUS_TAG[s] || 'info' }
function statusLabel(s) { return ACTIVITY_STATUS_MAP[s] || s }
function goDetail(id) { router.push(`/activity/${id}`) }
</script>

<style lang="scss" scoped>
.activity-list {
  position: relative;
  min-height: 100%;
  --list-theme: var(--activity-color, #e6a23c);
}

// 模块专属配色覆盖
.list-hero {
  background: linear-gradient(135deg, #fef9f0 0%, #fdfaf5 50%, #f8f6fe 100%);
}
.list-hero-bg {
  background: radial-gradient(circle at 20% 20%, rgba(230,162,60,0.08) 0%, transparent 40%),
              radial-gradient(circle at 85% 70%, rgba(245,166,35,0.05) 0%, transparent 35%);
}
.list-hero-badge { color: var(--activity-color); }
.list-hero-left p strong { color: var(--activity-color); }
.lhs-num { color: var(--activity-color); }

// 筛选按钮专属色
.filter-btn {
  background: #f59e0b !important;
  border-color: #f59e0b !important;
}

// 活动卡片（独特列表布局）
.activity-grid-wrap { min-height: 300px; }
.activity-grid { display: grid; gap: 16px; }

.activity-card {
  display: flex;
  gap: 18px;
  align-items: flex-start;
  padding: 22px 24px;
  background: #fff;
  border: 1px solid #edf0f4;
  border-radius: 16px;
  cursor: pointer;
  transition: all var(--transition-slow);
  animation: cardIn 0.4s ease-out both;
  &:hover {
    transform: translateY(-3px);
    box-shadow: var(--shadow-lg), 0 4px 10px rgba(230,162,60,0.04);
    border-color: rgba(230,162,60,0.18);
  }
}
.ac-left { flex-shrink: 0; }
.ac-type-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(230,162,60,0.1), rgba(230,162,60,0.03));
  border-radius: 14px;
  color: var(--activity-color);
}
.ac-body { flex: 1; min-width: 0; }
.ac-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0;
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
    flex: 1;
    margin-right: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}
.ac-mine { flex-shrink: 0; font-size: 11px; color: var(--activity-color); background: rgba(230,162,60,0.06); padding: 1px 8px; border-radius: 4px; font-weight: 600; }
.ac-info {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #909399;
  span { display: flex; align-items: center; gap: 4px; }
}
.ac-foot .ac-pub { font-size: 12px; color: #b0b8c4; }

.loc-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  border-bottom: 1px dashed var(--primary-color);
  &:hover { color: #337ecc; border-bottom-style: solid; }
}

// FAB 模块专属配色
.publish-fab {
  background: #f59e0b !important;
  border-color: #f59e0b !important;
  box-shadow: 0 6px 20px rgba(245,158,11,0.35);
  &:hover { box-shadow: 0 8px 28px rgba(245,158,11,0.45); }
}

@media (max-width: 768px) {
  .activity-card { flex-direction: column; }
}
</style>
