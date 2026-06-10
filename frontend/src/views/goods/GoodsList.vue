<template>
  <div class="goods-list page-container">
    <!-- 页面头部横幅 -->
    <div class="list-hero list-hero-goods">
      <div class="list-hero-bg"></div>
      <div class="list-hero-content">
        <div class="list-hero-left">
          <span class="list-hero-badge">
            <svg viewBox="0 0 28 28" width="20" height="20" fill="none">
              <defs><linearGradient id="lhg" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#43b84b"/><stop offset="100%" style="stop-color:#67c23a"/></linearGradient></defs>
              <rect x="3" y="10" width="22" height="16" rx="4" fill="url(#lhg)" opacity="0.18"/>
              <rect x="3" y="10" width="22" height="5" rx="2" fill="url(#lhg)" opacity="0.35"/>
              <rect x="8" y="18" width="12" height="8" rx="3" fill="url(#lhg)" opacity="0.25"/>
              <path d="M10 5l4 5M18 5l-4 5" stroke="url(#lhg)" stroke-width="1.5" stroke-linecap="round" opacity="0.55"/>
            </svg>
            闲置共享
          </span>
          <h1>发现身边的闲置好物</h1>
          <p>共 <strong>{{ total }}</strong> 件物品等待流转</p>
        </div>
        <div class="list-hero-stats">
          <div class="lhs-item"><span class="lhs-num">{{ total }}</span><span class="lhs-lbl">物品总数</span></div>
        </div>
      </div>
    </div>

    <!-- 热门分类 -->
    <div class="quick-cats">
      <span v-for="c in HOT_GOODS_CATS" :key="c" class="quick-cat-pill" :class="{ active: filters.category === c }"
        @click="filters.category = filters.category === c ? '' : c; handleSearch()">{{ c }}</span>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索物品..." clearable :prefix-icon="Search" class="filter-search" @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-select v-model="filters.exchangeType" placeholder="交换方式" clearable @change="handleSearch" class="filter-sel">
          <el-option label="出售" value="sell" /><el-option label="借用" value="borrow" /><el-option label="赠送" value="gift" /><el-option label="交换" value="exchange" />
        </el-select>
        <el-select v-model="filters.city" placeholder="城市" clearable filterable @change="handleSearch" class="filter-sel">
          <el-option v-for="c in CITIES" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filters.sortBy" placeholder="排序" clearable @change="handleSearch" class="filter-sel">
          <el-option label="最新发布" value="createdAt" />
        </el-select>
        <el-button type="primary" @click="handleSearch" class="filter-btn"><el-icon><Search /></el-icon> 搜索</el-button>
        <el-button @click="resetFilters" class="filter-btn-reset">重置</el-button>
      </div>
    </div>

    <!-- 物品卡片 -->
    <div v-loading="loading" class="goods-grid-wrap">
      <el-empty v-if="!loading && goodsList.length === 0" description="暂无物品">
        <template #default>
          <div class="empty-actions">
            <el-button type="primary" @click="router.push('/goods/publish')">发布我的闲置</el-button>
          </div>
        </template>
      </el-empty>
      <div v-else class="goods-grid">
        <div v-for="(item, idx) in goodsList" :key="item.id" class="goods-card"
          :style="{ animationDelay: idx * 0.04 + 's' }" @click="goDetail(item.id)">
          <div class="gc-image">
            <img v-if="item.images && item.images.length" :src="getFirstImage(item.images)" alt="" />
            <div v-else class="gc-placeholder">
              <svg viewBox="0 0 48 48" width="36" height="36" fill="none">
                <rect x="6" y="14" width="36" height="28" rx="5" stroke="#c8d6e5" stroke-width="2"/>
                <path d="M6 22h36M16 32h16v10H16z" stroke="#c8d6e5" stroke-width="2"/>
              </svg>
            </div>
            <span class="gc-tag" :class="'tag-'+item.exchangeType">{{ tagLabel(item.exchangeType) }}</span>
          </div>
          <div class="gc-body">
            <h3>{{ item.title }}
              <span v-if="item.userId === userStore.userInfo?.id" class="gc-mine">我的</span>
            </h3>
            <div class="gc-row">
              <el-rate v-model="item.conditionLevel" disabled :max="5" size="small" />
              <span class="gc-views">👁 {{ item.viewCount || 0 }}</span>
            </div>
            <div class="gc-foot">
              <span class="gc-user">{{ item.userNickname || '匿名用户' }} · {{ formatTime(item.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :total="total" :page-sizes="[8,12,20]" layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange" @current-change="handlePageChange" />
    </div>

    <router-link to="/goods/publish">
      <el-button class="publish-fab" type="primary" circle size="large">
        <el-icon :size="22"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { goodsApi } from '@/api'
import { Search, Plus } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/date'
import { CITIES, HOT_GOODS_CATS, EXCHANGE_TYPE_MAP } from '@/utils/constants'
import { useListPage } from '@/composables/useListPage'

const router = useRouter()
const userStore = useUserStore()

const {
  list: goodsList,
  total,
  loading,
  filters,
  pagination,
  loadList,
  handleSearch,
  resetFilters,
  handleSizeChange,
  handlePageChange,
} = useListPage(goodsApi.getList)

function tagLabel(t) { return EXCHANGE_TYPE_MAP[t] || t }
function getFirstImage(imgs) { if (Array.isArray(imgs)) return imgs[0]; return imgs?.split(',')[0] || '' }
function goDetail(id) { router.push(`/goods/${id}`) }
</script>

<style lang="scss" scoped>
.goods-list {
  position: relative;
  min-height: 100%;
  --list-theme: var(--goods-color, #67c23a);
}

// 模块专属配色覆盖
.list-hero {
  background: linear-gradient(135deg, #f2faf0 0%, #f8fbf7 50%, #fdf8f0 100%);
}
.list-hero-bg {
  background: radial-gradient(circle at 90% 10%, rgba(103,194,58,0.08) 0%, transparent 40%),
              radial-gradient(circle at 10% 80%, rgba(67,184,75,0.05) 0%, transparent 35%);
}
.list-hero-badge { color: var(--goods-color); }
.list-hero-left p strong { color: var(--goods-color); }
.lhs-num { color: var(--goods-color); }

// 物品卡片网格
.goods-grid-wrap { min-height: 300px; }
.goods-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }

.goods-card {
  background: #fff;
  border: 1px solid #edf0f4;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-slow);
  animation: cardIn 0.4s ease-out both;
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg), 0 4px 10px rgba(103,194,58,0.04);
    border-color: rgba(103,194,58,0.15);
  }
}
.gc-image {
  position: relative;
  height: 170px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
  .goods-card:hover img { transform: scale(1.04); }
  .gc-placeholder { color: #c8d6e5; }
}
.gc-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  &.tag-borrow    { background: rgba(64,158,255,0.1); color: #409eff; }
  &.tag-gift      { background: rgba(103,194,58,0.1); color: #67c23a; }
  &.tag-exchange  { background: rgba(230,162,60,0.1); color: #e6a23c; }
  &.tag-sell      { background: rgba(245,108,108,0.1); color: #f56c6c; }
}
.gc-body {
  padding: 14px 16px 16px;
  h3 {
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 8px;
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}
.gc-mine { flex-shrink: 0; font-size: 11px; color: var(--goods-color); background: rgba(103,194,58,0.06); padding: 1px 8px; border-radius: 4px; font-weight: 600; }
.gc-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.gc-views { font-size: 12px; color: #b0b8c4; }
.gc-foot .gc-user { font-size: 13px; color: #909399; }

// FAB 模块专属配色
.publish-fab {
  background: #10b981 !important;
  border-color: #10b981 !important;
  box-shadow: 0 6px 20px rgba(16,185,129,0.35);
  &:hover { box-shadow: 0 8px 28px rgba(16,185,129,0.45); }
}

// 响应式：物品卡片网格
@media (max-width: 900px) { .goods-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 500px) { .goods-grid { grid-template-columns: 1fr; } }
</style>
