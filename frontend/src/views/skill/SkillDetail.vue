<template>
  <div class="skill-detail page-container">
    <div v-loading="loading" class="detail-content">
      <el-empty v-if="!loading && !skill" description="技能信息不存在" />

      <template v-if="skill">
        <div class="page-header">
          <el-button text @click="router.push('/skill')">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>

        <el-row :gutter="20">
          <!-- 左侧：技能信息 -->
          <el-col :xs="24" :sm="16" :md="16">
            <el-card shadow="never" class="info-card">
              <template #header>
                <div class="card-title-row">
                  <h2>{{ skill.title }}</h2>
                  <div class="title-tags">
                    <el-tag :type="skill.type === 'teach' ? 'success' : 'primary'">
                      {{ skill.type === 'teach' ? '能教' : '想学' }}
                    </el-tag>
                    <el-tag type="info" effect="plain">{{ skill.category }}</el-tag>
                  </div>
                </div>
              </template>

              <div class="info-section">
                <div class="info-row">
                  <span class="info-label">技能描述</span>
                  <div class="info-content description-text">{{ skill.description }}</div>
                </div>

                <el-divider />

                <el-row :gutter="24">
                  <el-col :span="8">
                    <div class="info-item">
                      <span class="info-label">可用时间</span>
                      <span class="info-value">{{ skill.availableTime || '未指定' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="info-item">
                      <span class="info-label">期望地点</span>
                      <span class="info-value">{{ skill.preferredLocation || '未指定' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="info-item">
                      <span class="info-label">支持线上</span>
                      <span class="info-value">
                        <el-tag :type="skill.onlineSupport === 1 ? 'success' : 'info'" size="small">
                          {{ skill.onlineSupport === 1 ? '是' : '否' }}
                        </el-tag>
                      </span>
                    </div>
                  </el-col>
                </el-row>

                <el-divider />

                <div class="info-row">
                  <span class="info-label">浏览次数</span>
                  <span class="info-value">
                    <el-icon><View /></el-icon>
                    {{ skill.viewCount || 0 }}
                  </span>
                </div>
              </div>

              <!-- 下架横幅 -->
              <div v-if="isOffline" class="offline-banner">
                <el-icon :size="20"><WarningFilled /></el-icon>
                <div>
                  <strong>此内容已被管理员下架</strong>
                  <span v-if="reportReason">{{ reportReason }}</span>
                  <span v-else-if="isPublisher">如有疑问请联系管理员</span>
                </div>
              </div>

              <div class="action-bar">
                <!-- 已下架：只显示返回 -->
                <template v-if="isOffline">
                  <el-button size="large" @click="router.push('/skill')">返回列表</el-button>
                </template>
                <template v-else>
                  <el-button v-if="!isPublisher" type="primary" size="large" @click="showRequestDialog = true">发起交换请求</el-button>
                  <el-button v-if="!isPublisher" size="large" @click="contactPublisher"><el-icon><ChatDotRound /></el-icon> 联系TA</el-button>
                  <template v-if="isPublisher">
                    <el-tag type="info" size="large" effect="plain">这是我发布的技能</el-tag>
                    <el-button size="large" type="danger" plain @click="handleOffline" :loading="offlining">下架技能</el-button>
                    <el-button size="large" type="danger" @click="handleDelete" :loading="deleting">删除技能</el-button>
                  </template>
                  <el-button size="large" @click="router.push('/skill')">返回列表</el-button>
                  <el-button text type="danger" size="small" @click="openReportDialog" v-if="!isPublisher">举报</el-button>
                </template>
              </div>

              <!-- 管理员操作 -->
              <div v-if="isAdmin && !isPublisher && !isOffline" class="admin-bar">
                <el-tag type="danger" size="small" effect="dark">管理员操作</el-tag>
                <el-button size="small" type="warning" plain @click="adminOffline">下架</el-button>
                <el-button size="small" type="danger" @click="adminDelete" :loading="adminOfflining">删除</el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：发布者信息 -->
          <el-col :xs="24" :sm="8" :md="8">
            <el-card shadow="never" class="publisher-card">
              <template #header>
                <span>发布者</span>
              </template>

              <div class="publisher-info">
                <el-avatar :size="64" :src="skill.avatar">
                  {{ skill.nickname?.charAt(0) || '?' }}
                </el-avatar>
                <div class="publisher-detail">
                  <router-link :to="`/user/${skill.userId}`" class="publisher-name">
                    {{ skill.nickname || '匿名用户' }}
                  </router-link>
                  <el-tag v-if="skill.creditScore" type="warning" size="small" effect="plain" class="credit-tag">
                    <el-icon><Star /></el-icon>
                    信用 {{ skill.creditScore }}
                  </el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
    </div>

    <!-- 举报对话框 -->
    <el-dialog v-model="reportDialogVisible" title="举报内容" width="460px" destroy-on-close>
      <el-form :model="reportForm">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reason" placeholder="请选择">
            <el-option label="虚假信息" value="虚假信息" />
            <el-option label="违规内容" value="违规内容" />
            <el-option label="欺诈行为" value="欺诈行为" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="reportForm.description" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reportSubmitting" @click="handleReport">提交举报</el-button>
      </template>
    </el-dialog>

    <!-- 交换请求对话框 -->
    <el-dialog v-model="showRequestDialog" title="发起交换请求" width="500px" :close-on-click-modal="false" destroy-on-close>
      <!-- 技能摘要 -->
      <div class="req-summary" v-if="skill">
        <div class="req-item-preview">
          <div class="req-icon-wrap"><el-icon :size="22"><Collection /></el-icon></div>
          <div>
            <strong>{{ skill.title }}</strong>
            <span class="req-type">{{ skill.type==='teach'?'能教':'想学' }} · {{ skill.category }}</span>
          </div>
        </div>
      </div>
      <el-form ref="requestFormRef" :model="requestForm" :rules="requestRules" label-position="top">
        <el-form-item label="交换留言" prop="requestMessage">
          <el-input v-model="requestForm.requestMessage" type="textarea"
            placeholder="介绍一下你自己，说明你希望如何交换、你的技能或资源、时间安排..."
            :rows="5" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRequestDialog = false" size="large">取消</el-button>
        <el-button type="primary" :loading="requesting" @click="handleSendRequest" size="large">发送请求</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { skillApi, mutualApi, reportApi, adminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, View, Star, ChatDotRound, WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 判断当前用户是否为该技能的发布者，防止自己给自己发起交换请求
const isPublisher = computed(() => userStore.userInfo?.id === skill.value?.userId)
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const isOffline = computed(() => skill.value?.status === 0)
const reportReason = computed(() => route.query.reason || '')

// 管理员操作
const adminOfflining = ref(false)
async function adminOffline() {
  try { await ElMessageBox.confirm('管理员下架此技能？', '管理操作', { type: 'warning' }) } catch { return }
  adminOfflining.value = true
  try { await adminApi.offlineSkill(skill.value.id); ElMessage.success('已下架'); fetchDetail() } catch {} finally { adminOfflining.value = false }
}
async function adminDelete() {
  try { await ElMessageBox.confirm('管理员永久删除此技能？不可恢复。', '管理操作', { type: 'error', confirmButtonText: '删除' }) } catch { return }
  try { await adminApi.deleteSkill(skill.value.id); ElMessage.success('已删除'); router.push('/skill') } catch {}
}

const loading = ref(false)
const skill = ref(null)

const showRequestDialog = ref(false)
const requesting = ref(false)
const requestFormRef = ref(null)

const requestForm = reactive({
  requestMessage: '',
})

const requestRules = {
  requestMessage: [
    { required: true, message: '请输入留言', trigger: 'blur' },
    { min: 5, max: 300, message: '留言长度5-300个字符', trigger: 'blur' },
  ],
}

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await skillApi.getDetail(id)
    skill.value = res.data
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

async function handleSendRequest() {
  await requestFormRef.value?.validate()
  requesting.value = true
  try {
    await mutualApi.createRequest({
      type: 'skill',
      participantId: skill.value.userId,
      relatedId: skill.value.id,
      requestMessage: requestForm.requestMessage,
    })
    ElMessage.success('交换请求已发送')
    showRequestDialog.value = false
    requestForm.requestMessage = ''
  } catch {
    // 错误已在拦截器中处理
  } finally {
    requesting.value = false
  }
}

// ====== 举报 ======
const reportDialogVisible = ref(false)
const reportSubmitting = ref(false)
const reportForm = reactive({ reason: '违规内容', description: '' })

function openReportDialog() {
  reportForm.reason = '违规内容'
  reportForm.description = ''
  reportDialogVisible.value = true
}

async function handleReport() {
  if (!reportForm.reason) { ElMessage.warning('请选择举报原因'); return }
  reportSubmitting.value = true
  try {
    await reportApi.submitReport({
      targetType: 'skill',
      targetId: skill.value.id,
      reason: reportForm.reason,
      description: reportForm.description,
    })
    ElMessage.success('举报已提交，我们会尽快处理')
    reportDialogVisible.value = false
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || ''
    if (msg.includes('重复') || msg.includes('已举报')) {
      ElMessageBox.alert('您已举报过此内容，请耐心等待管理员处理', '无法重复举报', { confirmButtonText: '知道了', type: 'warning' })
    }
    // 其他错误已在拦截器显示
  } finally { reportSubmitting.value = false }
}

// ====== 下架技能 ======
const offlining = ref(false)
async function handleOffline() {
  try { await ElMessageBox.confirm('确定要下架这个技能吗？下架后其他用户将无法看到。', '确认下架', { type: 'warning' }) }
  catch { return }
  offlining.value = true
  try {
    await skillApi.offline(skill.value.id)
    ElMessage.success('技能已下架')
    router.push('/skill')
  } catch { /* error handled by interceptor */ }
  finally { offlining.value = false }
}

function contactPublisher() {
  router.push(`/message?chat=${skill.value.userId}`)
}

// ====== 删除技能 ======
const deleting = ref(false)
async function handleDelete() {
  try { await ElMessageBox.confirm('确定要永久删除这个技能吗？此操作不可恢复。', '确认删除', { type: 'error', confirmButtonText: '删除' }) }
  catch { return }
  deleting.value = true
  try {
    await skillApi.deleteSkill(skill.value.id)
    ElMessage.success('技能已删除')
    router.push('/skill')
  } catch { /* error handled by interceptor */ }
  finally { deleting.value = false }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style lang="scss" scoped>
.skill-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.info-card {
  .card-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;

    h2 {
      font-size: 22px;
      color: var(--text-primary);
      margin: 0;
    }

    .title-tags {
      display: flex;
      gap: 8px;
    }
  }
}

.info-section {
  .info-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 4px;
    display: block;
  }

  .info-value {
    color: var(--text-primary);
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .description-text {
    color: var(--text-primary);
    line-height: 1.8;
    white-space: pre-wrap;
  }

  .info-row {
    margin-bottom: 16px;
  }

  .info-item {
    .info-value {
      margin-top: 4px;
    }
  }
}

.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.publisher-card {
  .publisher-info {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .publisher-detail {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .publisher-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    text-decoration: none;

    &:hover {
      color: var(--el-color-primary);
    }
  }

  .credit-tag {
    width: fit-content;
  }
}

.admin-bar {
  margin-top: 32px; margin-bottom: 16px;
  padding: 16px 20px;
  background: #fef0f0; border: 1px solid #fbc4c4;
  border-radius: 14px;
  display: flex; align-items: center; gap: 12px;
}
.offline-banner, .report-banner {
  display: flex; align-items: flex-start; gap: 14px;
  padding: 18px 22px; margin-bottom: 16px;
  border-radius: 14px;
  strong { display: block; font-size: 15px; }
  span { font-size: 13px; color: #909399; }
  .el-icon { margin-top: 2px; flex-shrink: 0; }
}
.offline-banner { background: #fef0f0; border: 1px solid #fbc4c4;
  strong { color: #f56c6c; }
  .el-icon { color: #f56c6c; }
}
.report-banner { background: #fef9e7; border: 1px solid #f5dab1;
  strong { color: #e6a23c; }
  .el-icon { color: #e6a23c; }
}
</style>
