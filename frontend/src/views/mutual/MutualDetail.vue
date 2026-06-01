<template>
  <div class="mutual-detail page-container">
    <el-page-header @back="$router.back()" content="互助详情" />

    <el-card v-loading="loading" class="detail-card" shadow="hover">
      <template v-if="record">
        <!-- 状态横幅 -->
        <div class="status-banner" :class="`status-${record.status}`">
          <span class="status-icon"><el-icon :size="24"><component :is="statusIcon" /></el-icon></span>
          <span class="status-text">{{ statusMap[record.status] }}</span>
        </div>

        <!-- 进度时间线 -->
        <div class="progress-timeline" v-if="record.status !== 'cancelled'">
          <el-steps :active="activeStep" :process-status="stepStatus" finish-status="success" align-center>
            <el-step title="发起请求" :description="formatTime(record.createdAt)" />
            <el-step title="对方接受" :description="formatTime(record.startTime)" />
            <el-step title="进行中" :description="stepDesc.ongoing" />
            <el-step title="双方确认" :description="stepDesc.confirmed" />
            <el-step title="互评完成" :description="stepDesc.reviewed" />
          </el-steps>
        </div>
        <div v-else class="cancelled-notice">
          <el-result icon="error" title="该互助已取消" :sub-title="`取消时间：${formatTime(record.updatedAt)}`" />
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <el-descriptions :column="2" border class="info-section">
          <el-descriptions-item label="互助类型">
            <el-tag>{{ typeMap[record.type] || record.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="statusType[record.status]">{{ statusMap[record.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发起人">{{ record.initiatorNickname || `用户${record.initiatorId}` }}</el-descriptions-item>
          <el-descriptions-item label="参与人">{{ record.participantNickname || `用户${record.participantId}` }}</el-descriptions-item>
          <el-descriptions-item label="请求留言" :span="2">{{ record.requestMessage || '无' }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ record.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ record.startTime || '尚未开始' }}</el-descriptions-item>
          <el-descriptions-item label="确认状态" :span="2">
            <span>发起人：{{ record.initiatorConfirmed ? '已确认' : '未确认' }}</span>
            <el-divider direction="vertical" />
            <span>参与人：{{ record.participantConfirmed ? '已确认' : '未确认' }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="action-section" v-if="canOperate">
          <el-divider>操作</el-divider>
          <el-space wrap>
            <!-- 我是参与人 & 状态=待确认 → 可以接受/拒绝 -->
            <template v-if="isParticipant && record.status === 'pending'">
              <el-button type="primary" @click="handleAccept" :loading="operating">接受请求</el-button>
              <el-button type="danger" @click="handleReject" :loading="operating">拒绝请求</el-button>
            </template>

            <!-- 状态=进行中 → 可以确认完成 -->
            <template v-if="record.status === 'ongoing'">
              <el-button type="success" @click="handleConfirm" :loading="operating">
                {{ myConfirmed ? '已确认完成' : '确认互助完成' }}
              </el-button>
            </template>

            <!-- 状态=待确认/进行中 → 可以取消 -->
            <template v-if="record.status === 'pending' || record.status === 'ongoing'">
              <el-button @click="handleCancel" :loading="operating">取消互助</el-button>
            </template>
          </el-space>
        </div>

        <!-- 评价区域 -->
        <div class="review-section" v-if="record.status === 'completed'">
          <el-divider>评价</el-divider>

          <!-- 已评价提示 -->
          <el-alert v-if="myReview" title="您已完成评价" type="success" :closable="false" show-icon>
            <template #default>
              <div class="existing-review">
                <el-rate v-model="myReview.rating" disabled />
                <p>{{ myReview.content }}</p>
              </div>
            </template>
          </el-alert>

          <!-- 未评价 → 显示评价表单 -->
          <div v-else class="review-form">
            <el-form :model="reviewForm" label-width="80px">
              <el-form-item label="评分">
                <el-rate v-model="reviewForm.rating" :colors="['#F56C6C', '#E6A23C', '#67C23A']" show-text />
              </el-form-item>
              <el-form-item label="评价内容">
                <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="请输入评价内容" maxlength="500" show-word-limit />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitReview" :loading="submitting">提交评价</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 对方评价 -->
          <div v-if="otherReview" class="other-review" style="margin-top: 16px;">
            <h4>对方评价</h4>
            <div class="existing-review">
              <el-rate v-model="otherReview.rating" disabled />
              <p>{{ otherReview.content }}</p>
            </div>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { mutualApi, reviewApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Connection, CircleCheck, CircleClose, QuestionFilled } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const operating = ref(false)
const submitting = ref(false)
const record = ref(null)
const myReview = ref(null)
const otherReview = ref(null)

const reviewForm = ref({
  rating: 5,
  content: '',
})

const typeMap = { skill: '技能交换', goods: '物品共享', activity: '临时搭伴' }
const statusMap = { pending: '待确认', ongoing: '进行中', completed: '已完成', cancelled: '已取消' }
const statusType = { pending: 'warning', ongoing: 'primary', completed: 'success', cancelled: 'info' }

// 进度步骤计算
const activeStep = computed(() => {
  if (!record.value) return 0
  const s = record.value.status
  if (s === 'pending') return 0
  if (s === 'ongoing') return 2
  if (s === 'completed') {
    // 如果有一方已评价，进入步骤4；否则步骤3
    return (myReview.value || otherReview.value) ? 4 : 3
  }
  return 0
})

const stepStatus = computed(() => {
  if (!record.value) return 'wait'
  if (record.value.status === 'cancelled') return 'error'
  return 'process'
})

const stepDesc = computed(() => {
  if (!record.value) return {}
  const r = record.value
  return {
    ongoing: r.startTime ? '互助已开始' : '等待对方响应',
    confirmed: r.initiatorConfirmed && r.participantConfirmed
      ? '双方均已确认'
      : r.initiatorConfirmed || r.participantConfirmed
        ? '一方已确认'
        : '等待确认',
    reviewed: (myReview.value || otherReview.value) ? '已评价' : '等待评价',
  }
})

// 格式化时间显示
function formatTime(time) {
  if (!time) return '—'
  const d = new Date(time)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

const statusIcon = computed(() => {
  const map = { pending: Clock, ongoing: Connection, completed: CircleCheck, cancelled: CircleClose }
  return map[record.value?.status] || QuestionFilled
})

const currentUserId = computed(() => userStore.userInfo?.id)
const isInitiator = computed(() => record.value?.initiatorId === currentUserId.value)
const isParticipant = computed(() => record.value?.participantId === currentUserId.value)
const canOperate = computed(() => {
  if (!record.value) return false
  const s = record.value.status
  return (isParticipant.value && s === 'pending') || s === 'ongoing' || s === 'pending'
})
const myConfirmed = computed(() => {
  if (!record.value) return false
  return isInitiator.value ? record.value.initiatorConfirmed : record.value.participantConfirmed
})

async function loadRecord() {
  loading.value = true
  try {
    const res = await mutualApi.getRecordDetail(route.params.id)
    record.value = res.data?.data || res.data || null
  } catch (e) {
    ElMessage.error('获取互助详情失败')
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  if (!record.value || record.value.status !== 'completed') return
  try {
    // 获取我发出的评价，看是否已评价这个互助记录
    const sentRes = await reviewApi.getSentReviews({ pageNum: 1, pageSize: 50 })
    const sentList = sentRes.data?.records || sentRes.data?.data?.records || []
    const mySent = sentList.find(r => r.mutualRecordId == route.params.id)
    if (mySent) {
      myReview.value = mySent
    }

    // 获取对方评价
    const otherUserId = isInitiator.value ? record.value.participantId : record.value.initiatorId
    const receivedRes = await reviewApi.getReceivedReviews({ pageNum: 1, pageSize: 50 })
    const receivedList = receivedRes.data?.records || receivedRes.data?.data?.records || []
    const other = receivedList.find(r => r.reviewerId == otherUserId && r.mutualRecordId == route.params.id)
    if (other) {
      otherReview.value = other
    }
  } catch (e) { /* ignore */ }
}

async function handleAccept() {
  operating.value = true
  try {
    await mutualApi.acceptRequest(route.params.id)
    ElMessage.success('已接受互助请求')
    await loadRecord()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    operating.value = false
  }
}

async function handleReject() {
  try {
    await ElMessageBox.confirm('确定拒绝此互助请求？', '拒绝确认', { type: 'warning' })
    operating.value = true
    await mutualApi.rejectRequest(route.params.id)
    ElMessage.success('已拒绝互助请求')
    await loadRecord()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    operating.value = false
  }
}

async function handleConfirm() {
  if (myConfirmed.value) return
  try {
    await ElMessageBox.confirm('确认互助已完成？双方确认后将获得信用分奖励。', '确认完成', { type: 'info' })
    operating.value = true
    await mutualApi.confirmComplete(route.params.id)
    ElMessage.success('已确认互助完成')
    await loadRecord()
    await loadReviews()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    operating.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定取消此互助？取消后不可恢复。', '取消确认', { type: 'warning' })
    operating.value = true
    await mutualApi.cancelRecord(route.params.id)
    ElMessage.success('已取消互助')
    await loadRecord()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    operating.value = false
  }
}

async function submitReview() {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  submitting.value = true
  try {
    await reviewApi.submitReview({
      mutualRecordId: Number(route.params.id),
      revieweeId: isInitiator.value ? record.value.participantId : record.value.initiatorId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
    })
    ElMessage.success('评价提交成功')
    await loadReviews()
  } catch (e) {
    ElMessage.error('评价提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadRecord()
  await loadReviews()
})
</script>

<style scoped>
.detail-card {
  margin-top: 16px;
}

.status-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
}

.status-pending {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-ongoing {
  background: #ecf5ff;
  color: #409eff;
}

.status-completed {
  background: #f0f9eb;
  color: #67c23a;
}

.status-cancelled {
  background: #fef0f0;
  color: #f56c6c;
}

.status-icon {
  font-size: 24px;
}

/* ---- 进度时间线 ---- */
.progress-timeline {
  margin-bottom: 8px;
  padding: 20px 16px;
  background: #fafbfc;
  border-radius: var(--radius-md);
}

.cancelled-notice {
  margin-bottom: 16px;
}

.info-section {
  margin-bottom: 20px;
}

.action-section {
  margin-top: 8px;
}

.review-form {
  max-width: 600px;
}

.existing-review p {
  margin: 8px 0 0;
  color: #606266;
}

.other-review h4 {
  margin-bottom: 8px;
  color: #606266;
}
</style>
