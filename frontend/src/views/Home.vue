<template>
  <div class="home-page">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-orb orb-1"></div>
        <div class="hero-orb orb-2"></div>
        <div class="hero-orb orb-3"></div>
        <div class="hero-orb orb-4"></div>
      </div>
      <div class="hero-grid"></div>
      <div class="hero-inner">
        <div class="hero-badge"><span class="badge-dot"></span>青年同城互助平台</div>
        <h1>让每一次相遇<span class="hl">都有价值</span></h1>
        <p>技能互换、闲置共享、临时搭伴——与同城伙伴一起，探索更精彩的生活方式</p>
        <div class="hero-btns">
          <el-button type="primary" size="large" round @click="$router.push('/skill')">开始探索 <el-icon><ArrowRight /></el-icon></el-button>
          <el-button size="large" round class="btn-outline" @click="$router.push('/activity')">浏览活动</el-button>
        </div>
        <div class="hero-nums">
          <div class="hn"><strong>{{ animatedUserCount }}</strong><span>注册用户</span></div>
          <div class="hn"><strong>{{ animatedSkillCount }}</strong><span>技能分享</span></div>
          <div class="hn"><strong>{{ animatedTotalPosts }}</strong><span>互助内容</span></div>
        </div>
      </div>
    </section>

    <!-- 三大模块 — 大卡片 -->
    <section class="modules">
      <div class="sec-label">核心功能</div>
      <h2 class="sec-title">三大模块，探索你的互助方式</h2>
      <div class="mod-row">
        <div class="mod-card mod-skill" @click="$router.push('/skill')">
          <div class="mc-illustration">
            <svg viewBox="0 0 160 100" class="mc-svg">
              <defs><linearGradient id="hs" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#409eff"/><stop offset="100%" style="stop-color:#7b2ff7"/></linearGradient></defs>
              <rect x="15" y="30" width="50" height="50" rx="14" fill="url(#hs)" opacity="0.1"/>
              <rect x="25" y="40" width="30" height="30" rx="8" fill="url(#hs)" opacity="0.3"/>
              <rect x="95" y="30" width="50" height="50" rx="14" fill="url(#hs)" opacity="0.1"/>
              <circle cx="120" cy="55" r="14" fill="url(#hs)" opacity="0.3"/>
              <path d="M65 48l26-8M65 62l26 8" stroke="url(#hs)" stroke-width="3" stroke-linecap="round" opacity="0.5"/>
            </svg>
          </div>
          <div class="mc-body">
            <span class="mc-tag tag-blue">技能交换</span>
            <h3>互相教学，共同成长</h3>
            <p>编程、语言、音乐、健身——找到志同道合的伙伴</p>
          </div>
          <div class="mc-hover-icon"><el-icon :size="24"><ArrowRight /></el-icon></div>
        </div>

        <div class="mod-card mod-goods" @click="$router.push('/goods')">
          <div class="mc-illustration">
            <svg viewBox="0 0 160 100" class="mc-svg">
              <defs><linearGradient id="hg" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#43b84b"/><stop offset="100%" style="stop-color:#67c23a"/></linearGradient></defs>
              <rect x="20" y="42" width="120" height="50" rx="14" fill="url(#hg)" opacity="0.08"/>
              <rect x="20" y="42" width="120" height="14" rx="5" fill="url(#hg)" opacity="0.2"/>
              <rect x="38" y="62" width="36" height="30" rx="8" fill="url(#hg)" opacity="0.12"/>
              <rect x="86" y="62" width="36" height="30" rx="8" fill="url(#hg)" opacity="0.12"/>
              <path d="M55 22l16 20M105 22L89 42" stroke="url(#hg)" stroke-width="2.5" stroke-linecap="round" opacity="0.35"/>
              <circle cx="80" cy="16" r="7" fill="url(#hg)" opacity="0.18"/>
            </svg>
          </div>
          <div class="mc-body">
            <span class="mc-tag tag-green">闲置共享</span>
            <h3>让好物流转起来</h3>
            <p>书籍、数码、家居——借用、交换或赠送</p>
          </div>
          <div class="mc-hover-icon"><el-icon :size="24"><ArrowRight /></el-icon></div>
        </div>

        <div class="mod-card mod-activity" @click="$router.push('/activity')">
          <div class="mc-illustration">
            <svg viewBox="0 0 160 100" class="mc-svg">
              <defs><linearGradient id="ha" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#f5a623"/><stop offset="100%" style="stop-color:#e6a23c"/></linearGradient></defs>
              <circle cx="40" cy="30" r="16" fill="url(#ha)" opacity="0.1"/>
              <circle cx="40" cy="30" r="8" fill="url(#ha)" opacity="0.3"/>
              <circle cx="120" cy="30" r="16" fill="url(#ha)" opacity="0.1"/>
              <circle cx="120" cy="30" r="8" fill="url(#ha)" opacity="0.3"/>
              <circle cx="80" cy="74" r="16" fill="url(#ha)" opacity="0.1"/>
              <circle cx="80" cy="74" r="8" fill="url(#ha)" opacity="0.3"/>
              <path d="M52 38l-6 28M108 38l6 28M48 34h64" stroke="url(#ha)" stroke-width="2.5" opacity="0.28" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="mc-body">
            <span class="mc-tag tag-orange">临时搭伴</span>
            <h3>结伴同行不孤单</h3>
            <p>拼饭、看展、短途旅行——找到有趣的同行者</p>
          </div>
          <div class="mc-hover-icon"><el-icon :size="24"><ArrowRight /></el-icon></div>
        </div>
      </div>
    </section>

    <!-- 最新内容 Bento Grid -->
    <section class="bento" v-loading="dataLoading">
      <div class="sec-label">发现</div>
      <h2 class="sec-title">最新发布</h2>
      <template v-if="bentoItems.length">
        <div class="bento-grid">
          <div v-for="(item,i) in bentoItems.slice(0, pageSize)" :key="item.id" class="bento-card"
            :class="`bento-${item.size} bento-${item.module}`"
            :style="{ animationDelay: i*0.07+'s' }"
            @click="$router.push(item.link)">
            <!-- 卡片角标图标 -->
            <div class="bc-icon-corner">
              <svg v-if="item.module==='skill'" viewBox="0 0 32 32" width="18" height="18" fill="none">
                <rect x="4" y="10" width="10" height="10" rx="3" stroke="#409eff" stroke-width="2" opacity="0.5"/>
                <rect x="18" y="10" width="10" height="10" rx="3" stroke="#409eff" stroke-width="2" opacity="0.5"/>
                <path d="M12 13l5-2M12 17l5 2" stroke="#409eff" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <svg v-else-if="item.module==='goods'" viewBox="0 0 32 32" width="18" height="18" fill="none">
                <rect x="4" y="14" width="24" height="14" rx="4" stroke="#67c23a" stroke-width="2" opacity="0.5"/>
                <rect x="4" y="14" width="24" height="5" rx="2" fill="#67c23a" opacity="0.1"/>
                <path d="M12 8l4 6M20 8l-4 6" stroke="#67c23a" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <svg v-else viewBox="0 0 32 32" width="18" height="18" fill="none">
                <circle cx="10" cy="10" r="4" stroke="#e6a23c" stroke-width="2" opacity="0.5"/>
                <circle cx="22" cy="10" r="4" stroke="#e6a23c" stroke-width="2" opacity="0.5"/>
                <circle cx="16" cy="24" r="4" stroke="#e6a23c" stroke-width="2" opacity="0.5"/>
                <path d="M12 12l-1 7M20 12l1 7M11 11h10" stroke="#e6a23c" stroke-width="1.2" opacity="0.3" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="bc-inner">
              <div class="bc-meta">
                <span class="bc-mod" :class="`mod-${item.module}`">{{ item.moduleLabel }}</span>
                <span class="bc-sub">{{ item.subLabel }}</span>
              </div>
              <h3>{{ item.title }}</h3>
              <div class="bc-foot"><span>{{ item.user }}</span><span class="bc-time">{{ item.time }}</span></div>
            </div>
            <div class="bc-shimmer"></div>
          </div>
        </div>
        <div class="load-more" v-if="hasMore">
          <el-button round plain @click="loadMore">查看更多 <el-icon><ArrowDown /></el-icon></el-button>
        </div>
      </template>
      <div v-else-if="!dataLoading" class="empty-wide">
        <span class="empty-icon">📭</span>
        <p>还没有内容，成为第一个发布的人吧</p>
      </div>
    </section>

    <!-- 社区动态 -->
    <section class="feed-sec" v-if="feed.length">
      <div class="sec-label">社区</div>
      <h2 class="sec-title">最近动态</h2>
      <div class="feed-scroll">
        <div v-for="(item,i) in feed.slice(0, feedShowCount)" :key="i" class="feed-card" :style="{ animationDelay: i*0.06+'s' }">
          <div class="fc-icon-wrap" :class="fcIconClass(item)">
            <span class="fc-emoji">{{ item.icon }}</span>
          </div>
          <div class="fc-body">
            <div class="fc-top-row">
              <span class="fc-tag">{{ item.typeLabel }}</span>
              <span class="fc-time">{{ formatFeedTime(item.time) }}</span>
            </div>
            <p class="fc-text">{{ item.desc }}</p>
          </div>
        </div>
      </div>
      <div class="load-more" v-if="feed.length > feedShowCount">
        <el-button round plain @click="feedShowCount = Math.min(feedShowCount + 8, feed.length)">
          展开更多 <el-icon><ArrowDown /></el-icon>
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { skillApi, activityApi, goodsApi, communityApi } from '@/api'
import { ArrowRight, ArrowDown } from '@element-plus/icons-vue'

const latestSkills = ref([])
const latestGoods = ref([])
const latestActivities = ref([])
const feed = ref([])
const dataLoading = ref(true)
const feedShowCount = ref(4) // 动态初始显示4条
const pageSize = ref(4) // 每次加载数量
const hasMore = ref(true)
const stats = reactive({ userCount: 328, skillCount: 156, totalPosts: 420 })

function fcIconClass(item) {
  if (item.type === 'mutual_completed') return 'ficon-complete'
  if (item.type === 'review') return 'ficon-review'
  return 'ficon-default'
}

// 数字滚动动画
const animatedUserCount = ref(0)
const animatedSkillCount = ref(0)
const animatedTotalPosts = ref(0)
function animateValue(refVal, target, duration = 800) {
  const start = refVal.value
  const step = (target - start) / (duration / 16)
  let current = start
  function tick() {
    current += step
    if ((step > 0 && current >= target) || (step < 0 && current <= target)) {
      refVal.value = target; return
    }
    refVal.value = Math.round(current)
    requestAnimationFrame(tick)
  }
  tick()
}

// Bento items
const bentoItems = computed(() => {
  const items = []
  latestSkills.value.forEach((s, i) => items.push({
    id: 's'+s.id, module: 'skill', moduleLabel: '技能', subLabel: s.type==='teach'?'能教':'想学',
    title: s.title, user: s.nickname||'匿名', time: formatDate(s.createdAt),
    link: `/skill/${s.id}`, size: i===0?'lg':'sm'
  }))
  latestGoods.value.forEach((g, i) => items.push({
    id: 'g'+g.id, module: 'goods', moduleLabel: '物品', subLabel: exchangeLabel(g.exchangeType),
    title: g.title, user: g.userNickname||'匿名', time: formatDate(g.createdAt),
    link: `/goods/${g.id}`, size: i===0?'md':'sm'
  }))
  latestActivities.value.forEach((a, i) => items.push({
    id: 'a'+a.id, module: 'activity', moduleLabel: '活动', subLabel: typeLabel(a.type),
    title: a.title, user: `${a.currentMembers}/${a.maxMembers}人`, time: formatDate(a.activityTime),
    link: `/activity/${a.id}`, size: i===0?'md':'sm'
  }))
  return items.slice(0, 10)
})

function typeLabel(t) { const m={meal:'拼饭',exhibition:'拼展',travel:'拼旅行',other:'其他'}; return m[t]||t }
function exchangeLabel(t) { const m={borrow:'借用',gift:'赠送',exchange:'交换'}; return m[t]||t }
function formatDate(d) {
  if (!d) return ''
  const n=new Date(),t=new Date(d),diff=n-t
  if (diff<86400000) return '今天'; if (diff<172800000) return '昨天'
  if (diff<604800000) return Math.floor(diff/86400000)+'天前'
  return `${t.getMonth()+1}月${t.getDate()}日`
}
function formatFeedTime(t) {
  if (!t) return ''; const d=new Date(t),n=new Date(),diff=n-d
  if (diff<3600000) return Math.floor(diff/60000)+'分钟前'
  if (diff<86400000) return Math.floor(diff/3600000)+'小时前'
  return `${d.getMonth()+1}月${d.getDate()}日`
}

function loadMore() {
  pageSize.value += 4
  if (bentoItems.value.length < pageSize.value) {
    hasMore.value = false
    pageSize.value = bentoItems.value.length
  }
}

onMounted(async () => {
  dataLoading.value = true
  try {
    const [s,g,a,f] = await Promise.all([
      skillApi.getList({ pageNum:1, pageSize:4 }),
      goodsApi.getList({ pageNum:1, pageSize:4 }),
      activityApi.getList({ pageNum:1, pageSize:3 }),
      communityApi.getFeed({ pageNum:1, pageSize:8 }),
    ])
    latestSkills.value = s.data?.records || s.data?.data?.records || []
    latestGoods.value = g.data?.records || g.data?.data?.records || []
    latestActivities.value = a.data?.records || a.data?.data?.records || []
    feed.value = f.data?.data || f.data || []
    if (s.data?.total) stats.skillCount = s.data.total
    stats.totalPosts = stats.skillCount + (g.data?.total||0) + (a.data?.total||0)
    hasMore.value = bentoItems.value.length > pageSize.value
  } catch {} finally { dataLoading.value = false }
})

watch([() => stats.userCount, () => stats.skillCount, () => stats.totalPosts], () => {
  animateValue(animatedUserCount, stats.userCount)
  animateValue(animatedSkillCount, stats.skillCount)
  animateValue(animatedTotalPosts, stats.totalPosts)
}, { immediate: true })
</script>

<style lang="scss" scoped>
/* ====== HERO ====== */
.hero {
  position: relative; overflow: hidden;
  padding: 72px 24px 56px; text-align: center;
  background: linear-gradient(180deg, #f6f9ff 0%, #fafbfd 45%, #fff 100%);
}
.hero-bg { position: absolute; inset: 0; pointer-events: none; }
.hero-orb {
  position: absolute; border-radius: 50%; filter: blur(100px);
  &.orb-1 { width: 550px; height: 550px; top: -220px; right: -120px; background: rgba(64,158,255,0.07); animation: orbFloat 14s ease-in-out infinite; }
  &.orb-2 { width: 380px; height: 380px; bottom: -140px; left: -100px; background: rgba(122,100,255,0.05); animation: orbFloat 18s ease-in-out infinite reverse; }
  &.orb-3 { width: 260px; height: 260px; top: 50%; left: 55%; background: rgba(230,162,60,0.04); animation: orbFloat 11s ease-in-out infinite 3s; }
  &.orb-4 { width: 180px; height: 180px; top: 20%; left: 15%; background: rgba(103,194,58,0.04); animation: orbFloat 9s ease-in-out infinite 1s; }
}
@keyframes orbFloat { 0%,100%{transform:translate(0,0) scale(1)} 50%{transform:translate(30px,-20px) scale(1.1)} }

.hero-grid {
  position: absolute; inset: 0; pointer-events: none; opacity: 0.12;
  background-image:
    linear-gradient(rgba(64,158,255,0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64,158,255,0.15) 1px, transparent 1px);
  background-size: 60px 60px;
}

.hero-inner { position: relative; z-index: 1; max-width: 640px; margin: 0 auto; }
.hero-badge {
  display: inline-flex; align-items: center; gap: 8px; padding: 5px 16px;
  background: #fff; border: 1px solid #e8ecf2; border-radius: 20px;
  font-size: 13px; color: #6b7280; margin-bottom: 20px;
  .badge-dot { width: 7px; height: 7px; border-radius: 50%; background: #67c23a; animation: dotPulse 2s ease-in-out infinite; }
}
@keyframes dotPulse { 0%,100%{opacity:1} 50%{opacity:.3} }

.hero h1 { font-size: 50px; font-weight: 900; color: #111827; line-height: 1.2; margin: 0 0 14px; letter-spacing: -1.5px; }
.hero .hl { background: linear-gradient(135deg, #409eff 0%, #7b2ff7 50%, #e040fb 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.hero p { font-size: 16px; color: #6b7280; max-width: 480px; margin: 0 auto 28px; line-height: 1.65; }
.hero-btns { display: flex; gap: 12px; justify-content: center; margin-bottom: 44px; }
.btn-outline { border: 1.5px solid #d0d5dd !important; background: #fff !important; color: var(--text-regular) !important; &:hover { border-color: var(--primary-color) !important; color: var(--primary-color) !important; } }

.hero-nums { display: flex; gap: 0; justify-content: center; }
.hn { padding: 0 40px; text-align: center;
  &+& { border-left: 1px solid #e8ecf2; }
  strong { display: block; font-size: 30px; font-weight: 900; background: linear-gradient(135deg, #409eff, #7b2ff7); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
  span { font-size: 12px; color: #909399; }
}

/* ====== 模块 ====== */
.modules { max-width: 1100px; margin: 0 auto; padding: 72px 24px 56px; }
.sec-label { text-align: center; font-size: 11px; font-weight: 700; color: var(--primary-color); text-transform: uppercase; letter-spacing: 3px; }
.sec-title { text-align: center; font-size: 28px; font-weight: 800; color: var(--text-primary); margin: 8px 0 48px; }
.mod-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }

.mod-card {
  background: #fff; border: 1px solid #edf0f4; border-radius: 22px;
  padding: 32px 28px 28px; cursor: pointer; position: relative; overflow: hidden;
  transition: all 0.45s cubic-bezier(0.4,0,0.2,1);

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 24px 48px rgba(0,0,0,0.05), 0 8px 16px rgba(0,0,0,0.03);
    border-color: transparent;
    .mc-hover-icon { opacity: 1; transform: translate(0, -50%); }
    .mc-illustration svg { transform: scale(1.08); }
  }
}
.mc-illustration { height: 110px; display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.mc-svg { transition: transform 0.45s ease; }
.mc-tag { display: inline-block; padding: 3px 12px; border-radius: 7px; font-size: 12px; font-weight: 700; margin-bottom: 10px;
  &.tag-blue { background: rgba(64,158,255,0.07); color: #409eff; }
  &.tag-green { background: rgba(103,194,58,0.07); color: #52b818; }
  &.tag-orange { background: rgba(230,162,60,0.07); color: #e6a23c; }
}
.mod-card h3 { font-size: 18px; font-weight: 700; margin: 0 0 6px; color: var(--text-primary); }
.mod-card p { font-size: 14px; color: #6b7280; margin: 0; line-height: 1.5; }

.mc-hover-icon {
  position: absolute; right: 20px; top: 50%; transform: translate(20px, -50%);
  opacity: 0; transition: all 0.4s ease;
  color: var(--primary-color);
}

/* ====== BENTO ====== */
.bento { max-width: 1100px; margin: 0 auto; padding: 0 24px 56px; }
.bento-grid {
  display: grid; grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: 130px; gap: 16px;
}
.bento-card {
  background: #fff; border: 1px solid #edf0f4; border-radius: 18px;
  cursor: pointer; position: relative; overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4,0,0.2,1);
  animation: cardIn 0.5s ease-out both;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 14px 32px rgba(0,0,0,0.05), 0 4px 8px rgba(64,158,255,0.03);
    border-color: rgba(64,158,255,0.15);
    .bc-shimmer { left: 100%; }
  }
}
@keyframes cardIn { from{opacity:0;transform:translateY(18px)} to{opacity:1;transform:translateY(0)} }

.bento-lg { grid-column: span 2; grid-row: span 2; }
.bento-md { grid-column: span 2; grid-row: span 1; }
.bento-sm { grid-column: span 1; grid-row: span 1; }

.bc-inner { padding: 22px 24px; position: relative; z-index: 1; height: 100%; display: flex; flex-direction: column; }
.bc-meta { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.bc-mod { padding: 2px 8px; border-radius: 5px; font-size: 11px; font-weight: 700;
  &.mod-skill { background: rgba(64,158,255,0.06); color: #409eff; }
  &.mod-goods { background: rgba(103,194,58,0.06); color: #52b818; }
  &.mod-activity { background: rgba(230,162,60,0.06); color: #e6a23c; }
}
.bc-sub { font-size: 12px; color: #909399; }
.bento-card h3 { font-size: 15px; font-weight: 600; margin: 0 0 auto; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.bento-lg h3 { font-size: 20px; -webkit-line-clamp: 3; }
.bento-md h3 { font-size: 17px; }
.bc-foot { display: flex; justify-content: space-between; font-size: 12px; color: #b0b8c4; margin-top: 12px; }

/* 卡片角标图标 */
.bc-icon-corner {
  position: absolute; top: 14px; right: 14px; z-index: 2;
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  border-radius: 10px; background: rgba(64,158,255,0.03);
  transition: all 0.3s;
}
.bento-card:hover .bc-icon-corner { transform: scale(1.15); }

.bc-shimmer {
  position: absolute; top: 0; left: -100%; width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  transition: left 0.6s ease; pointer-events: none; z-index: 0;
}

/* 卡片模块色左边框 */
.bento-skill { border-left: 3px solid transparent; &:hover { border-left-color: rgba(64,158,255,0.3); } }
.bento-goods { border-left: 3px solid transparent; &:hover { border-left-color: rgba(103,194,58,0.3); } }
.bento-activity { border-left: 3px solid transparent; &:hover { border-left-color: rgba(230,162,60,0.3); } }

/* ====== 社区动态 ====== */
.feed-sec { max-width: 1100px; margin: 0 auto; padding: 0 24px 72px; }
.feed-scroll { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.feed-card {
  background: #fff; border: 1px solid #edf0f4; border-radius: 18px;
  padding: 20px; display: flex; gap: 14px; align-items: flex-start;
  transition: all 0.3s; animation: cardIn 0.5s ease-out both;
  &:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(0,0,0,0.04); border-color: rgba(64,158,255,0.12); }
}
.fc-icon-wrap {
  width: 44px; height: 44px; display: flex; align-items: center; justify-content: center;
  border-radius: 14px; flex-shrink: 0; font-size: 22px;
  &.ficon-complete { background: linear-gradient(135deg, rgba(103,194,58,0.1), rgba(103,194,58,0.03)); }
  &.ficon-review { background: linear-gradient(135deg, rgba(64,158,255,0.1), rgba(64,158,255,0.03)); }
  &.ficon-default { background: linear-gradient(135deg, rgba(230,162,60,0.1), rgba(230,162,60,0.03)); }
}
.fc-body { flex: 1; min-width: 0; }
.fc-top-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.fc-emoji { font-size: 22px; }
.fc-tag { font-size: 11px; color: #909399; background: #f5f6f8; padding: 2px 8px; border-radius: 6px; }
.fc-text { font-size: 14px; color: var(--text-primary); line-height: 1.55; margin: 0; }
.fc-time { font-size: 12px; color: #c0c8d4; flex-shrink: 0; }

.load-more { text-align: center; padding: 28px 0 0; }

.empty-wide { text-align: center; padding: 60px 20px;
  .empty-icon { font-size: 40px; }
  p { font-size: 15px; color: #909399; margin: 12px 0 0; }
}

@media (max-width: 900px) {
  .hero h1 { font-size: 34px; }
  .mod-row { grid-template-columns: 1fr; }
  .bento-grid { grid-template-columns: repeat(2, 1fr); }
  .bento-lg, .bento-md { grid-column: span 2; grid-row: span 1; }
  .feed-scroll { grid-template-columns: repeat(2, 1fr); }
  .hero-nums { flex-direction: column; gap: 14px; .hn+.hn { border-left: none; } }
}
@media (max-width: 500px) {
  .hero { padding: 48px 16px 40px; }
  .hero h1 { font-size: 28px; }
  .bento-grid { grid-template-columns: 1fr; grid-auto-rows: auto; }
  .bento-lg, .bento-md { grid-column: span 1; }
  .feed-scroll { grid-template-columns: 1fr; }
}
</style>
