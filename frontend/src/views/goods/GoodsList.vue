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
      <span v-for="c in hotCats" :key="c" class="quick-cat-pill" :class="{ active: filters.category === c }"
        @click="filters.category = filters.category === c ? '' : c; loadList()">{{ c }}</span>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="搜索物品..." clearable :prefix-icon="Search" class="filter-search" @keyup.enter="loadList" @clear="loadList" />
        <el-select v-model="filters.exchangeType" placeholder="交换方式" clearable @change="loadList" class="filter-sel">
          <el-option label="出售" value="sell" /><el-option label="借用" value="borrow" /><el-option label="赠送" value="gift" /><el-option label="交换" value="exchange" />
        </el-select>
        <el-select v-model="filters.city" placeholder="城市" clearable filterable @change="loadList" class="filter-sel">
          <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filters.sortBy" placeholder="排序" clearable @change="loadList" class="filter-sel">
          <el-option label="最新发布" value="createdAt" />
        </el-select>
        <el-button type="primary" @click="loadList" class="filter-btn"><el-icon><Search /></el-icon> 搜索</el-button>
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
        @size-change="loadList" @current-change="loadList" />
    </div>

    <router-link to="/goods/publish">
      <el-button class="publish-fab" type="primary" circle size="large">
        <el-icon :size="22"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { goodsApi } from '@/api'
import { Search, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const hotCats = ['数码', '书籍', '家居', '服饰', '运动', '食品']
const cities = ['北京','上海','广州','深圳','杭州','成都','武汉','南京','重庆','西安']

const filters = reactive({ keyword: '', category: '', exchangeType: '', city: '', sortBy: '' })
const pagination = reactive({ page: 1, pageSize: 12 })
const goodsList = ref([])
const total = ref(0)
const loading = ref(false)

function tagLabel(t) { const m = { sell: '出售', borrow: '借用', gift: '赠送', exchange: '交换' }; return m[t] || t }
import { formatTime } from '@/utils/date'
function getFirstImage(imgs) { if (Array.isArray(imgs)) return imgs[0]; return imgs?.split(',')[0] || '' }

async function loadList() {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.pageSize, ...filters }
    Object.keys(params).forEach(k => { if (!params[k]) delete params[k] })
    const res = await goodsApi.getList(params)
    goodsList.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* silent */ }
  finally { loading.value = false }
}

function resetFilters() {
  filters.keyword = ''; filters.category = ''; filters.exchangeType = ''; filters.city = ''
  pagination.page = 1; loadList()
}
function goDetail(id) { router.push(`/goods/${id}`) }
onMounted(() => loadList())
</script>

<style lang="scss" scoped>
.goods-list { position: relative; min-height: 100%; }

/* ====== 复用SkillList的横幅/分类/筛选样式 ====== */
.list-hero {
  position: relative; border-radius: 20px; overflow: hidden;
  margin-bottom: 24px; padding: 36px 32px;
  background: linear-gradient(135deg, #f2faf0 0%, #f8fbf7 50%, #fdf8f0 100%);
}
.list-hero-bg {
  position: absolute; inset: 0; pointer-events: none;
  background: radial-gradient(circle at 90% 10%, rgba(103,194,58,0.08) 0%, transparent 40%),
              radial-gradient(circle at 10% 80%, rgba(67,184,75,0.05) 0%, transparent 35%);
}
.list-hero-content { position: relative; z-index:1; display: flex; justify-content: space-between; align-items: center; }
.list-hero-badge { display: inline-flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; color: #67c23a; margin-bottom: 8px; }
.list-hero-left {
  h1 { font-size: 26px; font-weight: 800; margin: 0 0 4px; color: var(--text-primary); }
  p { font-size: 14px; color: var(--text-secondary); margin: 0;
    strong { color: #67c23a; font-size: 20px; font-weight: 800; } }
}
.lhs-item { text-align: center; .lhs-num { display: block; font-size: 32px; font-weight: 900; color: #67c23a; } .lhs-lbl { font-size: 12px; color: #909399; } }

.quick-cats { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.quick-cat-pill {
  padding: 6px 16px; border-radius: 20px; font-size: 13px; font-weight: 500;
  background: #fff; color: var(--text-regular); border: 1px solid #edf0f4;
  cursor: pointer; transition: all 0.25s; user-select: none;
  &:hover { border-color: #67c23a; color: #67c23a; background: rgba(103,194,58,0.04); }
  &.active { background: rgba(103,194,58,0.08); color: #67c23a; border-color: rgba(103,194,58,0.25); font-weight: 600; }
}

.filter-bar { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 18px 20px; margin-bottom: 24px; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap;
  .filter-search { flex: 1; min-width: 160px; }
  .filter-sel { width: 130px; flex-shrink: 0; }
  .filter-btn, .filter-btn-reset { flex-shrink: 0; border-radius: 10px; }
}

/* ====== 卡片列表 ====== */
.goods-grid-wrap { min-height: 300px; }
.goods-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }

.goods-card {
  background: #fff; border: 1px solid #edf0f4; border-radius: 16px;
  overflow: hidden; cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4,0,0.2,1);
  animation: cardIn 0.4s ease-out both;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 14px 36px rgba(0,0,0,0.06), 0 4px 10px rgba(103,194,58,0.04);
    border-color: rgba(103,194,58,0.15);
  }
}
@keyframes cardIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }

.gc-image {
  position: relative; height: 170px; background: #f5f7fa;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
  img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
  .goods-card:hover img { transform: scale(1.04); }

  .gc-placeholder { color: #c8d6e5; }
}

.gc-tag {
  position: absolute; top: 10px; right: 10px;
  padding: 3px 12px; border-radius: 8px; font-size: 12px; font-weight: 600;
  &.tag-borrow { background: rgba(64,158,255,0.1); color: #409eff; }
  &.tag-gift { background: rgba(103,194,58,0.1); color: #67c23a; }
  &.tag-exchange { background: rgba(230,162,60,0.1); color: #e6a23c; }
  &.tag-sell { background: rgba(245,108,108,0.1); color: #f56c6c; }
}

.gc-body { padding: 14px 16px 16px;
  h3 { font-size: 15px; font-weight: 600; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: flex; align-items: center; gap: 6px; }
}
.gc-mine { flex-shrink: 0; font-size: 11px; color: #67c23a; background: rgba(103,194,58,0.06); padding: 1px 8px; border-radius: 4px; font-weight: 600; }
.gc-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.gc-views { font-size: 12px; color: #b0b8c4; }
.gc-foot { .gc-user { font-size: 13px; color: #909399; } }

.pagination-wrapper { display: flex; justify-content: center; margin-top: 28px; padding-bottom: 60px; }

.publish-fab {
  position: fixed; right: 40px; bottom: 80px;
  width: 52px; height: 52px; box-shadow: 0 6px 20px rgba(16,185,129,0.35);
  animation: fabPulse 2.5s ease-in-out infinite; z-index: 10;
  background: #10b981 !important; border-color: #10b981 !important;
  &:hover { transform: scale(1.08); box-shadow: 0 8px 28px rgba(16,185,129,0.45); animation: none; }
}
@keyframes fabPulse { 0%,100%{box-shadow:0 6px 20px rgba(16,185,129,0.35)} 50%{box-shadow:0 6px 30px rgba(16,185,129,0.55)} }
.empty-actions { display: flex; gap: 10px; justify-content: center; margin-top: 12px; }

@media (max-width: 900px) { .goods-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 500px) { .goods-grid { grid-template-columns: 1fr; } .filter-row { flex-direction: column; .filter-sel, .filter-search { width: 100%; } } }
</style>
