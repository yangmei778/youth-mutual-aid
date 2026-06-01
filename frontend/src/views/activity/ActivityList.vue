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
      <span v-for="t in hotTypes" :key="t.value" class="quick-cat-pill"
        :class="{ active: filterType === t.value }"
        @click="filterType = filterType === t.value ? '' : t.value; loadList()">{{ t.label }}</span>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input v-model="searchKeyword" placeholder="搜索活动..." clearable :prefix-icon="Search" class="filter-search" @keyup.enter="loadList" />
        <el-select v-model="filterCity" placeholder="城市" clearable filterable @change="loadList" class="filter-sel">
          <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable @change="loadList" class="filter-sel">
          <el-option label="报名中" value="open" /><el-option label="已满员" value="full" /><el-option label="进行中" value="ongoing" />
        </el-select>
        <el-select v-model="filterSortBy" placeholder="排序" clearable @change="loadList" class="filter-sel">
          <el-option label="最新发布" value="createdAt" /><el-option label="即将开始" value="activityTime" />
        </el-select>
        <el-button type="primary" @click="loadList" class="filter-btn"><el-icon><Search /></el-icon> 搜索</el-button>
      </div>
    </div>

    <!-- 活动卡片列表 -->
    <div v-loading="loading" class="activity-grid-wrap">
      <el-empty v-if="!loading && activities.length === 0" description="暂无活动，换个条件试试吧" />
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
              <span><el-icon :size="14"><Clock /></el-icon> {{ formatDate(item.activityTime) }}</span>
              <span><el-icon :size="14"><Location /></el-icon> {{ item.location }}</span>
              <span><el-icon :size="14"><User /></el-icon> {{ item.currentMembers }}/{{ item.maxMembers }}人</span>
              <span v-if="item.costDesc"><el-icon :size="14"><Coin /></el-icon> {{ item.costDesc }}</span>
            </div>
            <div class="ac-foot">
              <span class="ac-pub">{{ item.nickname || '匿名' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10,20,30]" layout="total, sizes, prev, pager, next"
        @size-change="loadList" @current-change="loadList" />
    </div>

    <router-link to="/activity/publish">
      <el-button class="publish-fab" type="primary" size="large" circle>
        <el-icon :size="22"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Sunny, KnifeFork, PictureFilled, MapLocation, MoreFilled, Clock, Location, User, Coin } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { activityApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hotTypes = [{ value:'meal',label:'🍜 拼饭' },{ value:'exhibition',label:'🎨 拼展' },{ value:'travel',label:'✈️ 拼旅行' },{ value:'other',label:'🤝 其他' }]
const cities = ['北京','上海','广州','深圳','杭州','成都','武汉','南京','重庆','西安']

const typeIconMap = { meal: KnifeFork, exhibition: PictureFilled, travel: MapLocation, other: MoreFilled }
const statusTagType = s => ({ open:'success', full:'warning', ongoing:'primary' }[s]||'info')
const statusLabel = s => ({ open:'报名中', full:'已满员', ongoing:'进行中' }[s]||s)
const formatDate = d => { if(!d)return''; const dd=new Date(d); return `${dd.getFullYear()}-${String(dd.getMonth()+1).padStart(2,'0')}-${String(dd.getDate()).padStart(2,'0')} ${String(dd.getHours()).padStart(2,'0')}:${String(dd.getMinutes()).padStart(2,'0')}` }

const searchKeyword = ref(''), filterType = ref(''), filterCity = ref(''), filterStatus = ref(''), filterSortBy = ref('')
const activities = ref([]), loading = ref(false), page = ref(1), pageSize = ref(10), total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const params = { pageNum: page.value, pageSize: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterType.value) params.type = filterType.value
    if (filterCity.value) params.city = filterCity.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterSortBy.value) params.sortBy = filterSortBy.value
    const res = await activityApi.getList(params)
    activities.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch { activities.value = []; total.value = 0 }
  finally { loading.value = false }
}
const goDetail = id => router.push(`/activity/${id}`)
onMounted(() => loadList())
</script>

<style lang="scss" scoped>
.activity-list { position: relative; min-height: 100%; }

/* ====== 横幅 ====== */
.list-hero {
  position: relative; border-radius: 20px; overflow: hidden;
  margin-bottom: 24px; padding: 36px 32px;
  background: linear-gradient(135deg, #fef9f0 0%, #fdfaf5 50%, #f8f6fe 100%);
}
.list-hero-bg {
  position: absolute; inset: 0; pointer-events: none;
  background: radial-gradient(circle at 20% 20%, rgba(230,162,60,0.08) 0%, transparent 40%),
              radial-gradient(circle at 85% 70%, rgba(245,166,35,0.05) 0%, transparent 35%);
}
.list-hero-content { position: relative; z-index:1; display: flex; justify-content: space-between; align-items: center; }
.list-hero-badge { display: inline-flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; color: #e6a23c; margin-bottom: 8px; }
.list-hero-left {
  h1 { font-size: 26px; font-weight: 800; margin: 0 0 4px; color: var(--text-primary); }
  p { font-size: 14px; color: var(--text-secondary); margin: 0;
    strong { color: #e6a23c; font-size: 20px; font-weight: 800; } }
}
.lhs-item { text-align: center; .lhs-num { display: block; font-size: 32px; font-weight: 900; color: #e6a23c; } .lhs-lbl { font-size: 12px; color: #909399; } }

/* ====== 分类/筛选 ====== */
.quick-cats { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.quick-cat-pill {
  padding: 6px 16px; border-radius: 20px; font-size: 13px; font-weight: 500;
  background: #fff; color: var(--text-regular); border: 1px solid #edf0f4;
  cursor: pointer; transition: all 0.25s; user-select: none;
  &:hover { border-color: #e6a23c; color: #e6a23c; background: rgba(230,162,60,0.04); }
  &.active { background: rgba(230,162,60,0.08); color: #e6a23c; border-color: rgba(230,162,60,0.25); font-weight: 600; }
}

.filter-bar { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 18px 20px; margin-bottom: 24px; }
.filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap;
  .filter-search { flex: 1; min-width: 160px; }
  .filter-sel { width: 130px; flex-shrink: 0; }
  .filter-btn { flex-shrink: 0; border-radius: 10px; }
}

/* ====== 活动卡片 ====== */
.activity-grid-wrap { min-height: 300px; }
.activity-grid { display: grid; gap: 16px; }

.activity-card {
  display: flex; gap: 18px; align-items: flex-start;
  padding: 22px 24px; background: #fff;
  border: 1px solid #edf0f4; border-radius: 16px;
  cursor: pointer; transition: all 0.35s cubic-bezier(0.4,0,0.2,1);
  animation: cardIn 0.4s ease-out both;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 14px 36px rgba(0,0,0,0.06), 0 4px 10px rgba(230,162,60,0.04);
    border-color: rgba(230,162,60,0.18);
  }
}
@keyframes cardIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }

.ac-left { flex-shrink: 0; }
.ac-type-icon {
  width: 50px; height: 50px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, rgba(230,162,60,0.1), rgba(230,162,60,0.03));
  border-radius: 14px; color: #e6a23c;
}

.ac-body { flex: 1; min-width: 0; }
.ac-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;
  h3 { font-size: 16px; font-weight: 600; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; margin-right: 12px; display: flex; align-items: center; gap: 6px; }
}
.ac-mine { flex-shrink: 0; font-size: 11px; color: #e6a23c; background: rgba(230,162,60,0.06); padding: 1px 8px; border-radius: 4px; font-weight: 600; }
.ac-info {
  display: flex; flex-wrap: wrap; gap: 14px; margin-bottom: 8px;
  font-size: 13px; color: #909399;
  span { display: flex; align-items: center; gap: 4px; }
}
.ac-foot { .ac-pub { font-size: 12px; color: #b0b8c4; } }

.loc-link {
  display: inline-flex; align-items: center; gap: 4px;
  color: var(--primary-color); text-decoration: none; font-weight: 500;
  border-bottom: 1px dashed var(--primary-color);
  &:hover { color: #337ecc; border-bottom-style: solid; }
}

.pagination-wrap { display: flex; justify-content: center; margin-top: 28px; padding-bottom: 60px; }

.publish-fab {
  position: fixed; right: 40px; bottom: 80px;
  width: 52px; height: 52px; box-shadow: 0 6px 20px rgba(64,158,255,0.35);
  animation: fabPulse 2.5s ease-in-out infinite; z-index: 10;
  &:hover { transform: scale(1.08); box-shadow: 0 8px 28px rgba(64,158,255,0.45); animation: none; }
}
@keyframes fabPulse { 0%,100%{box-shadow:0 6px 20px rgba(64,158,255,0.35)} 50%{box-shadow:0 6px 30px rgba(64,158,255,0.55)} }

@media (max-width: 768px) {
  .activity-card { flex-direction: column; }
  .filter-row { flex-direction: column; .filter-sel, .filter-search { width: 100%; } }
}
</style>
