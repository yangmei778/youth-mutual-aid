<template>
  <div class="activity-detail page-container">
    <div v-loading="loading" class="ad-wrap">
      <template v-if="activity">
        <!-- Hero Banner -->
        <div class="ad-hero" :class="`hero-${activity.type}`">
          <div class="hero-bg-glow"></div>
          <div class="hero-inner">
            <div class="hero-left">
              <div class="hero-badge-row">
                <span class="hero-type-badge">
                  <el-icon :size="18"><component :is="typeIconMap[activity.type] || MoreFilled" /></el-icon>
                  {{ typeLabel(activity.type) }}
                </span>
                <el-tag :type="statusTagType(activity.status)" size="small" effect="dark" round>{{ statusLabel(activity.status) }}</el-tag>
              </div>
              <h1>{{ activity.title }}</h1>
            </div>
            <div class="hero-right">
              <div class="hero-stat">
                <span class="hs-num">{{ activity.currentMembers }}</span>
                <span class="hs-lbl">已报名</span>
              </div>
              <div class="hero-stat-div"></div>
              <div class="hero-stat">
                <span class="hs-num">{{ activity.maxMembers }}</span>
                <span class="hs-lbl">人数上限</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 主体两栏 -->
        <div class="ad-body">
          <!-- 左栏 -->
          <div class="ad-main">
            <!-- 活动信息 -->
            <div class="ad-card info-block">
              <div class="info-grid">
                <div class="info-item">
                  <div class="ii-icon ii-time"><el-icon :size="20"><Clock /></el-icon></div>
                  <div class="ii-text">
                    <span class="ii-label">活动时间</span>
                    <span class="ii-value">{{ formatDate(activity.activityTime) }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <div class="ii-icon ii-loc"><el-icon :size="20"><Location /></el-icon></div>
                  <div class="ii-text">
                    <span class="ii-label">活动地点</span>
                    <span class="ii-value">{{ activity.location }}</span>
                  </div>
                </div>
                <div class="info-item" v-if="activity.endTime">
                  <div class="ii-icon ii-deadline"><el-icon :size="20"><Timer /></el-icon></div>
                  <div class="ii-text">
                    <span class="ii-label">报名截止</span>
                    <span class="ii-value">{{ formatDate(activity.endTime) }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <div class="ii-icon ii-cost"><el-icon :size="20"><Coin /></el-icon></div>
                  <div class="ii-text">
                    <span class="ii-label">费用说明</span>
                    <span class="ii-value">{{ activity.costDesc || '免费' }}</span>
                  </div>
                </div>
              </div>

              <!-- 人员进度 -->
              <div class="member-bar">
                <div class="mb-head">
                  <span>参与进度</span>
                  <span class="mb-count">{{ activity.currentMembers }}/{{ activity.maxMembers }}</span>
                </div>
                <el-progress :percentage="memberPercentage" :stroke-width="10" :color="progressColor" />
              </div>

              <!-- 描述 -->
              <div class="desc-block" v-if="activity.description">
                <h3>活动详情</h3>
                <p>{{ activity.description }}</p>
              </div>
            </div>

            <!-- 地图 -->
            <div class="ad-card" v-if="activity.location">
              <div class="card-head">
                <span class="card-head-icon"><el-icon :size="16"><MapLocation /></el-icon></span>
                <span>活动位置</span>
              </div>
              <MapView :location="activity.location" />
            </div>

            <!-- 活动回顾 -->
            <div class="ad-card" v-if="activity">
              <div class="card-head">
                <span>活动回顾</span>
                <span class="card-head-count">{{ reviews.length }}条</span>
                <el-button v-if="canReview" type="primary" size="small" round @click="reviewDialogVisible = true">
                  发布回顾
                </el-button>
              </div>
              <div class="review-list" v-loading="reviewsLoading">
                <div v-for="r in reviews" :key="r.id" class="r-item">
                  <el-avatar :size="36" :src="r.avatar">{{ r.nickname?.charAt(0) }}</el-avatar>
                  <div class="r-body">
                    <div class="r-head">
                      <span class="r-name">{{ r.nickname }}</span>
                      <span class="r-time">{{ r.createdAt }}</span>
                    </div>
                    <p class="r-text">{{ r.content }}</p>
                    <div v-if="r.images" class="r-imgs">
                      <img v-for="(img,i) in getReviewImages(r.images)" :key="i" :src="img" class="r-thumb" @click.stop="previewImage=img;showPreview=true" />
                    </div>
                  </div>
                  <el-button v-if="r.userId===userStore.userInfo?.id" text type="danger" size="small" @click="handleDeleteReview(r)">删除</el-button>
                </div>
                <div v-if="!reviewsLoading&&reviews.length===0" class="empty-hint">还没有回顾，来分享你的体验吧</div>
              </div>
            </div>
          </div>

          <!-- 右栏 -->
          <div class="ad-side">
            <!-- 组织者 -->
            <div class="ad-card">
              <div class="card-head"><span>组织者</span></div>
              <div class="organizer-block">
                <el-avatar :size="52" :src="activity.avatar">{{ activity.nickname?.charAt(0)||'?' }}</el-avatar>
                <router-link :to="`/user/${activity.userId}`" class="org-name">{{ activity.nickname||'匿名用户' }}</router-link>
                <el-tag v-if="activity.creditScore" size="small" type="warning" effect="plain">⭐ {{ activity.creditScore }}</el-tag>
              </div>
            </div>

            <!-- 报名成员 -->
            <div class="ad-card">
              <div class="card-head">
                <span>报名成员</span>
                <span class="card-head-count">{{ activity.currentMembers||0 }}/{{ activity.maxMembers }}</span>
              </div>
              <div class="member-list" v-if="(activity.members||[]).length">
                <div v-for="m in activity.members" :key="m.id" class="ml-item">
                  <el-avatar :size="34" :src="m.avatar">{{ m.nickname?.charAt(0) }}</el-avatar>
                  <div class="ml-info">
                    <span class="ml-name">{{ m.nickname }}</span>
                    <span class="ml-msg" v-if="m.applyMessage">{{ m.applyMessage }}</span>
                  </div>
                  <div class="ml-right">
                    <el-tag :type="memberTag(m.status)" size="small">{{ memberLabel(m.status) }}</el-tag>
                    <!-- 组织者审核 -->
                    <template v-if="isOrganizer && m.status === 0">
                      <el-button type="success" link size="small" @click="handleApprove(m)" :loading="m._approving">通过</el-button>
                      <el-button type="danger" link size="small" @click="handleReject(m)" :loading="m._rejecting">拒绝</el-button>
                    </template>
                  </div>
                </div>
              </div>
              <div v-else class="empty-hint">还没有人报名</div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-card">
              <template v-if="canJoin">
                <el-button type="primary" size="large" round class="join-btn" @click="joinDialogVisible = true">
                  <el-icon :size="18"><Plus /></el-icon> 报名参加
                </el-button>
              </template>
              <template v-else-if="alreadyJoined">
                <el-button type="info" size="large" round disabled class="join-btn">已报名</el-button>
              </template>
              <template v-else-if="isOrganizer">
                <div class="org-actions">
                  <span class="org-tag">我发布的活动</span>
                  <el-button type="danger" @click="handleDelete" :loading="deleting">删除</el-button>
                </div>
              </template>
            </div>

            <!-- 管理员操作 -->
            <div v-if="isAdmin && !isOrganizer" class="admin-bar">
              <el-tag type="danger" size="small" effect="dark">管理员操作</el-tag>
              <el-button size="small" type="warning" plain @click="adminOffline">下架</el-button>
              <el-button size="small" type="danger" @click="adminDelete" :loading="adminOfflining">删除</el-button>
            </div>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading&&!activity" description="活动不存在" />

      <!-- 图片预览 -->
      <el-dialog v-model="showPreview" title="图片预览" width="600px">
        <img :src="previewImage" style="width:100%" v-if="previewImage" />
      </el-dialog>

      <!-- 发布回顾 -->
      <el-dialog v-model="reviewDialogVisible" title="发布活动回顾" width="480px" destroy-on-close>
        <el-form :model="reviewForm">
          <el-form-item label="感受分享">
            <el-input v-model="reviewForm.content" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="分享你的活动体验和感受..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewDialogVisible=false">取消</el-button>
          <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">发布</el-button>
        </template>
      </el-dialog>

      <!-- 报名 -->
      <el-dialog v-model="joinDialogVisible" title="报名参加" width="460px" destroy-on-close>
        <el-form ref="joinFormRef" :model="joinForm" :rules="joinRules">
          <el-form-item label="报名留言" prop="applyMessage">
            <el-input v-model="joinForm.applyMessage" type="textarea" placeholder="介绍一下自己，让组织者更了解你" :rows="4" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="joinDialogVisible=false">取消</el-button>
          <el-button type="primary" :loading="joining" @click="handleJoin">确认报名</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { showConfirm } from '@/utils/confirm'
import { activityApi, adminApi } from '@/api'
import { KnifeFork, PictureFilled, MapLocation, MoreFilled, Clock, Location, User, Coin, Edit, Plus, Timer } from '@element-plus/icons-vue'
import MapView from '@/components/MapView.vue'

const route = useRoute()
const userStore = useUserStore()

const typeIconMap = { meal: KnifeFork, exhibition: PictureFilled, travel: MapLocation, other: MoreFilled }
const typeLabel = t => ({ meal: '拼饭', exhibition: '拼展', travel: '拼旅行', other: '其他' }[t] || t)
const statusTagType = s => ({ open: 'success', full: 'warning', ongoing: 'primary' }[s] || 'info')
const statusLabel = s => ({ open: '报名中', full: '已满员', ongoing: '进行中' }[s] || s)
const memberTag = s => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')
const memberLabel = s => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || s)
const formatDate = d => {
  if (!d) return ''
  const t = new Date(d)
  return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')} ${String(t.getHours()).padStart(2,'0')}:${String(t.getMinutes()).padStart(2,'0')}`
}

const activity = ref(null)
const loading = ref(false)
const memberPercentage = computed(() => activity.value ? Math.round(activity.value.currentMembers / activity.value.maxMembers * 100) : 0)
const progressColor = computed(() => {
  const p = memberPercentage.value
  if (p >= 100) return '#f56c6c'; if (p >= 70) return '#e6a23c'
  return '#67c23a'
})
const alreadyJoined = computed(() => activity.value?.currentUserJoined === true)
const isOrganizer = computed(() => userStore.userInfo?.id === activity.value?.userId)
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const adminOfflining = ref(false)
async function adminOffline() {
  try { await showConfirm('管理员下架此活动？', '管理操作') } catch { return }
  adminOfflining.value = true
  try { await adminApi.offlineActivity(activity.value.id); ElMessage.success('已下架'); loadDetail() } catch {} finally { adminOfflining.value = false }
}
async function adminDelete() {
  try { await showConfirm('管理员永久删除此活动？不可恢复', '管理操作', 'danger') } catch { return }
  try { await adminApi.deleteActivity(activity.value.id); ElMessage.success('已删除'); window.history.back() } catch {}
}
const canJoin = computed(() => activity.value?.status === 'open' && !alreadyJoined.value && !isOrganizer.value)

const joinDialogVisible = ref(false); const joining = ref(false); const joinFormRef = ref(null)
const joinForm = reactive({ applyMessage: '' })
const joinRules = { applyMessage: [{ required: true, message: '请填写报名留言', trigger: 'blur' }] }

const loadDetail = async () => {
  const id = route.params.id; if (!id) return
  loading.value = true
  try { const res = await activityApi.getDetail(id); activity.value = res.data; fetchReviews() }
  catch { activity.value = null } finally { loading.value = false }
}
const handleJoin = async () => {
  if (!await joinFormRef.value?.validate().catch(() => false)) return
  joining.value = true
  try { await activityApi.join(activity.value.id, { applyMessage: joinForm.applyMessage }); ElMessage.success('报名成功，请等待审核'); joinDialogVisible.value = false; joinForm.applyMessage = ''; loadDetail() }
  catch (e) { ElMessage.error(e?.response?.data?.message || '报名失败') } finally { joining.value = false }
}
async function handleApprove(m) {
  try { await showConfirm(`通过「${m.nickname}」的报名申请？`, '审核') } catch { return }
  m._approving = true
  try { await activityApi.approveMember(activity.value.id, m.id); ElMessage.success('已通过'); loadDetail() } catch {} finally { m._approving = false }
}
async function handleReject(m) {
  try { await showConfirm(`拒绝「${m.nickname}」的报名申请？`, '审核') } catch { return }
  m._rejecting = true
  try { await activityApi.rejectMember(activity.value.id, m.id); ElMessage.success('已拒绝'); loadDetail() } catch {} finally { m._rejecting = false }
}

const reviews = ref([]); const reviewsLoading = ref(false)
const reviewDialogVisible = ref(false); const reviewSubmitting = ref(false)
const previewImage = ref(''); const showPreview = ref(false)
const reviewForm = reactive({ content: '' })
const getReviewImages = imgs => imgs ? (Array.isArray(imgs) ? imgs : imgs.split(',').filter(Boolean)) : []
const canReview = computed(() => alreadyJoined.value || isOrganizer.value)

async function fetchReviews() {
  if (!activity.value) return; reviewsLoading.value = true
  try { const res = await activityApi.getReviews(activity.value.id, { pageNum: 1, pageSize: 20 }); reviews.value = res.data?.records || res.data?.data?.records || [] }
  catch {} finally { reviewsLoading.value = false }
}
async function submitReview() {
  if (!reviewForm.content.trim()) { ElMessage.warning('请填写回顾内容'); return }
  reviewSubmitting.value = true
  try { await activityApi.publishReview(activity.value.id, { content: reviewForm.content, images: '' }); ElMessage.success('发布成功'); reviewDialogVisible.value = false; reviewForm.content = ''; fetchReviews() }
  catch {} finally { reviewSubmitting.value = false }
}
async function handleDeleteReview(r) {
  try { await showConfirm('删除这条回顾？', '确认') } catch { return }
  try { await activityApi.deleteReview(activity.value.id, r.id); ElMessage.success('已删除'); fetchReviews() } catch {}
}
const deleting = ref(false)
async function handleDelete() {
  try { await showConfirm('永久删除此活动？不可恢复', '确认删除', 'danger') } catch { return }
  deleting.value = true
  try { await activityApi.deleteActivity(activity.value.id); ElMessage.success('已删除'); window.history.back() } catch {} finally { deleting.value = false }
}

onMounted(() => loadDetail())
</script>

<style lang="scss" scoped>
.activity-detail { max-width: 1060px; }
.ad-wrap { animation: fadeUp 0.4s ease-out; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }

/* ====== Hero Banner ====== */
.ad-hero {
  position: relative; border-radius: 20px; padding: 36px 36px 32px;
  margin-bottom: 24px; overflow: hidden;
  background: #fff; border: 1px solid #edf0f4;

  &.hero-meal { border-left: 4px solid #409eff; }
  &.hero-exhibition { border-left: 4px solid #9b59b6; }
  &.hero-travel { border-left: 4px solid #e6a23c; }
}
.hero-bg-glow {
  position: absolute; top: -60px; right: -40px;
  width: 240px; height: 240px; border-radius: 50%;
  background: radial-gradient(circle, rgba(64,158,255,0.06) 0%, transparent 70%);
  pointer-events: none;
}
.hero-inner { position: relative; z-index: 1; display: flex; justify-content: space-between; align-items: flex-start; }
.hero-left { flex: 1; }
.hero-badge-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.hero-type-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 5px 14px; border-radius: 8px;
  font-size: 13px; font-weight: 700;
  background: rgba(64,158,255,0.06); color: #409eff;
}
.hero h1 { font-size: 26px; font-weight: 800; color: var(--text-primary); margin: 0; line-height: 1.3; }
.hero-right { display: flex; align-items: center; gap: 0; flex-shrink: 0; }
.hero-stat { text-align: center; padding: 0 24px; }
.hs-num { display: block; font-size: 36px; font-weight: 900; color: var(--primary-color); line-height: 1; }
.hs-lbl { font-size: 12px; color: #909399; margin-top: 4px; display: block; }
.hero-stat-div { width: 1px; height: 44px; background: #edf0f4; }

/* ====== 两栏 ====== */
.ad-body { display: flex; gap: 24px; align-items: flex-start; }
.ad-main { flex: 1; min-width: 0; }
.ad-side { width: 320px; flex-shrink: 0; position: sticky; top: 76px; }

/* ====== 卡片 ====== */
.ad-card { background: #fff; border: 1px solid #edf0f4; border-radius: 18px; padding: 24px; margin-bottom: 20px; }
.card-head { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; padding-bottom: 14px; border-bottom: 2px solid #f5f6f8; font-size: 15px; font-weight: 700; color: var(--text-primary); }
.card-head-icon { color: var(--primary-color); }
.card-head-count { color: #909399; font-weight: 400; font-size: 13px; margin-left: 4px; }
.card-head .el-button { margin-left: auto; }

/* 信息网格 */
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0; }
.info-item { display: flex; gap: 14px; padding: 16px 0;
  &:nth-child(-n+2) { padding-top: 0; }
}
.ii-icon { width: 42px; height: 42px; display: flex; align-items: center; justify-content: center; border-radius: 12px; flex-shrink: 0;
  &.ii-time { background: rgba(64,158,255,0.06); color: #409eff; }
  &.ii-loc { background: rgba(230,162,60,0.06); color: #e6a23c; }
  &.ii-deadline { background: rgba(245,108,108,0.06); color: #f56c6c; }
  &.ii-cost { background: rgba(103,194,58,0.06); color: #67c23a; }
}
.ii-text { display: flex; flex-direction: column; gap: 2px; }
.ii-label { font-size: 12px; color: #909399; }
.ii-value { font-size: 14px; font-weight: 600; color: var(--text-primary); }

/* 人员进度 */
.member-bar { padding: 18px 0 0; }
.mb-head { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; color: var(--text-regular); }
.mb-count { font-weight: 700; color: var(--primary-color); }

/* 描述 */
.desc-block { margin-top: 20px; padding-top: 18px; border-top: 1px solid #f0f2f5;
  h3 { font-size: 15px; font-weight: 700; margin: 0 0 8px; }
  p { font-size: 14px; line-height: 1.8; color: #606266; margin: 0; white-space: pre-wrap; }
}

/* 回顾 */
.review-list { min-height: 60px; }
.r-item { display: flex; gap: 12px; padding: 16px 0; align-items: flex-start;
  &+& { border-top: 1px solid #f5f6f8; }
}
.r-body { flex: 1; }
.r-head { display: flex; gap: 10px; align-items: center; margin-bottom: 6px; }
.r-name { font-size: 14px; font-weight: 600; }
.r-time { font-size: 12px; color: #b0b8c4; }
.r-text { font-size: 14px; line-height: 1.6; color: #606266; margin: 0; }
.r-imgs { display: flex; gap: 8px; margin-top: 8px; .r-thumb { width: 72px; height: 72px; object-fit: cover; border-radius: 8px; cursor: pointer; } }

/* 组织者 */
.organizer-block { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 8px 0; }
.org-name { font-size: 16px; font-weight: 600; color: var(--primary-color); text-decoration: none; &:hover { text-decoration: underline; } }

/* 成员列表 */
.member-list { display: flex; flex-direction: column; }
.ml-item { display: flex; gap: 10px; padding: 12px 0; align-items: center;
  &+& { border-top: 1px solid #f5f6f8; }
}
.ml-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.ml-name { font-size: 14px; font-weight: 500; }
.ml-msg { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 2px; }
.ml-right { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }

/* 操作 */
.action-card { margin-top: 8px; }
.join-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 700; }
.org-actions { display: flex; flex-direction: column; gap: 10px; align-items: center; }
.org-tag { font-size: 14px; color: #909399; }

.empty-hint { text-align: center; padding: 24px 0; font-size: 13px; color: #b0b8c4; }
.admin-bar { margin-top: 16px; padding: 14px 18px; background: #fef0f0; border: 1px solid #fbc4c4; border-radius: 14px; display: flex; align-items: center; gap: 10px; }

@media (max-width: 860px) {
  .ad-body { flex-direction: column; }
  .ad-side { width: 100%; position: static; }
  .info-grid { grid-template-columns: 1fr; }
  .hero-inner { flex-direction: column; gap: 20px; }
  .hero-right { align-self: flex-start; }
  .ad-hero { padding: 24px 20px; }
}
</style>
