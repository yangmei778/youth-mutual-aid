<template>
  <div class="activity-detail page-container">
    <div v-loading="loading">
      <template v-if="activity">
        <!-- 活动信息卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="info-header">
              <div class="title-row">
                <span class="type-icon">{{ typeIconMap[activity.type] || '🤝' }}</span>
                <h2 class="activity-title">{{ activity.title }}</h2>
                <el-tag :type="statusTagType(activity.status)" size="default">{{ statusLabel(activity.status) }}</el-tag>
              </div>
              <el-button text @click="$router.push('/activity')">返回列表</el-button>
            </div>
          </template>

          <div class="info-body">
            <div class="info-row">
              <span class="info-label">活动类型</span>
              <el-tag size="small" type="info">{{ typeLabel(activity.type) }}</el-tag>
            </div>
            <div class="info-row">
              <span class="info-label">活动时间</span>
              <span>{{ formatDate(activity.activityTime) }}</span>
            </div>
            <div class="info-row" v-if="activity.endTime">
              <span class="info-label">报名截止</span>
              <span>{{ formatDate(activity.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">活动地点</span>
              <span>📍 {{ activity.location }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">参与人数</span>
              <div class="members-progress">
                <el-progress
                  :percentage="memberPercentage"
                  :stroke-width="18"
                  :text-inside="true"
                  :format="() => `${activity.currentMembers}/${activity.maxMembers}`"
                />
              </div>
            </div>
            <div class="info-row">
              <span class="info-label">费用说明</span>
              <span>💰 {{ activity.costDesc || '免费' }}</span>
            </div>
            <div class="info-row description-row">
              <span class="info-label">活动描述</span>
              <p class="description-text">{{ activity.description }}</p>
            </div>
          </div>
        </el-card>

        <!-- 组织者信息 -->
        <el-card class="organizer-card" shadow="never">
          <template #header>
            <span>组织者</span>
          </template>
          <div class="organizer-body" v-if="activity.publisher">
            <el-avatar :size="48" :src="activity.publisher.avatar">
              {{ activity.publisher.nickname?.charAt(0) }}
            </el-avatar>
            <div class="organizer-info">
              <router-link :to="`/user/${activity.publisher.id}`" class="organizer-name">
                {{ activity.publisher.nickname }}
              </router-link>
              <el-tag v-if="activity.publisher.creditScore" size="small" type="warning" class="credit-badge">
                信用 {{ activity.publisher.creditScore }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <!-- 成员列表 -->
        <el-card class="members-card" shadow="never">
          <template #header>
            <span>报名成员（{{ activity.currentMembers || 0 }}/{{ activity.maxMembers }}）</span>
          </template>
          <el-table :data="activity.members || []" stripe style="width: 100%" empty-text="暂无成员">
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="memberStatusType(row.status)" size="small">{{ memberStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 报名按钮 -->
        <div class="action-bar" v-if="canJoin">
          <el-button type="primary" size="large" @click="joinDialogVisible = true">报名参加</el-button>
        </div>
        <div class="action-bar" v-else-if="alreadyJoined">
          <el-button type="info" size="large" disabled>已报名</el-button>
        </div>
        <div class="action-bar" v-else-if="isOrganizer">
          <el-tag type="info" size="large" effect="plain">这是我发布的活动</el-tag>
        </div>
      </template>

      <el-empty v-if="!loading && !activity" description="活动不存在" />
    </div>

    <!-- 报名对话框 -->
    <el-dialog v-model="joinDialogVisible" title="报名参加" width="460px" destroy-on-close>
      <el-form ref="joinFormRef" :model="joinForm" :rules="joinRules">
        <el-form-item label="报名留言" prop="applyMessage">
          <el-input
            v-model="joinForm.applyMessage"
            type="textarea"
            placeholder="请简要介绍自己，让组织者更了解你"
            :rows="4"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="joining" @click="handleJoin">确认报名</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { activityApi } from '@/api'

const route = useRoute()
const userStore = useUserStore()

const typeIconMap = {
  meal: '🍜',
  exhibition: '🎨',
  travel: '✈️',
  other: '🤝',
}

const typeLabel = (type) => {
  const map = { meal: '拼饭', exhibition: '拼展', travel: '拼旅行', other: '其他' }
  return map[type] || type
}

const statusTagType = (status) => {
  const map = { open: 'success', full: 'warning', ongoing: 'primary' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { open: '报名中', full: '已满员', ongoing: '进行中' }
  return map[status] || status
}

const memberStatusType = (status) => {
  const map = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const memberStatusLabel = (status) => {
  const map = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

const activity = ref(null)
const loading = ref(false)

const memberPercentage = computed(() => {
  if (!activity.value) return 0
  return Math.round((activity.value.currentMembers / activity.value.maxMembers) * 100)
})

const alreadyJoined = computed(() => {
  if (!activity.value) return false
  return activity.value.currentUserJoined === true
})

// 判断当前用户是否为活动组织者
const isOrganizer = computed(() => {
  return userStore.userInfo?.id === activity.value?.publisher?.id
})

const canJoin = computed(() => {
  if (!activity.value) return false
  return activity.value.status === 'open' && !alreadyJoined.value && !isOrganizer.value
})

const joinDialogVisible = ref(false)
const joining = ref(false)
const joinFormRef = ref(null)
const joinForm = reactive({
  applyMessage: '',
})

const joinRules = {
  applyMessage: [{ required: true, message: '请填写报名留言', trigger: 'blur' }],
}

const loadDetail = async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const res = await activityApi.getDetail(id)
    activity.value = res.data
  } catch {
    activity.value = null
  } finally {
    loading.value = false
  }
}

const handleJoin = async () => {
  const valid = await joinFormRef.value?.validate().catch(() => false)
  if (!valid) return

  joining.value = true
  try {
    await activityApi.join(activity.value.id, { applyMessage: joinForm.applyMessage })
    ElMessage.success('报名成功，请等待组织者审核')
    joinDialogVisible.value = false
    joinForm.applyMessage = ''
    loadDetail()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '报名失败，请重试')
  } finally {
    joining.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.activity-detail {
  h2 {
    font-size: 24px;
    color: var(--text-primary);
    margin-bottom: 4px;
  }
}

.info-card {
  margin-bottom: 20px;

  .info-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .title-row {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .type-icon {
    font-size: 28px;
  }

  .activity-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
  }

  .info-body {
    .info-row {
      display: flex;
      align-items: flex-start;
      padding: 10px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }
    }

    .info-label {
      flex-shrink: 0;
      width: 80px;
      color: var(--text-secondary);
      font-size: 14px;
    }

    .members-progress {
      flex: 1;
      max-width: 300px;
    }

    .description-row {
      flex-direction: column;
      gap: 8px;
    }

    .description-text {
      margin: 0;
      line-height: 1.8;
      color: var(--text-regular);
      font-size: 14px;
      white-space: pre-wrap;
    }
  }
}

.organizer-card {
  margin-bottom: 20px;

  .organizer-body {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .organizer-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .organizer-name {
    font-size: 16px;
    font-weight: 500;
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  .credit-badge {
    margin-left: 4px;
  }
}

.members-card {
  margin-bottom: 20px;
}

.action-bar {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
