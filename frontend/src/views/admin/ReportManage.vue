<template>
  <div class="admin-page">
    <div class="ap-hero">
      <div class="aph-left">
        <h2>举报管理</h2>
        <p><strong>{{ pendingCount }}</strong> 条待处理</p>
      </div>
      <div class="aph-right">
        <el-radio-group v-model="statusFilter" size="small" @change="fetchReports">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="pending">待处理</el-radio-button>
          <el-radio-button value="approved">已通过</el-radio-button>
          <el-radio-button value="rejected">已驳回</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 内容预览抽屉 -->
    <el-drawer v-model="previewVisible" title="被举报内容" size="500px" direction="rtl">
      <template v-if="previewLoading" ><div style="text-align:center;padding:40px"><el-icon class="is-loading" :size="24"><Loading /></el-icon></div></template>
      <template v-else-if="previewData">
        <div class="preview-section">
          <div class="ps-label">标题</div>
          <div class="ps-value">{{ previewData.title || '（无标题）' }}</div>
        </div>
        <div class="preview-section" v-if="previewData.description">
          <div class="ps-label">描述</div>
          <div class="ps-value ps-desc">{{ previewData.description }}</div>
        </div>
        <div class="preview-section">
          <div class="ps-label">发布者</div>
          <div class="ps-value">{{ previewData.nickname || '用户' + previewData.userId }}</div>
        </div>
        <div class="preview-section" v-if="previewData.category">
          <div class="ps-label">分类</div>
          <div class="ps-value">{{ previewData.category }}</div>
        </div>
        <div class="preview-section" v-if="previewData.location">
          <div class="ps-label">地点</div>
          <div class="ps-value">{{ previewData.location }}</div>
        </div>
        <div class="preview-actions">
          <el-button type="primary" @click="openInNewTab">在新页面查看完整内容</el-button>
          <el-button v-if="previewReport?.status==='pending'" type="danger" @click="handleAction(previewReport,'approved');previewVisible=false">确认违规并处理</el-button>
        </div>
      </template>
    </el-drawer>

    <div class="ap-card">
      <el-table :data="reports" v-loading="loading" stripe class="modern-table">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-row">
              <div><strong>举报详情：</strong>{{ row.description || '未填写详细描述' }}</div>
              <div style="margin-top:6px">
                <el-button type="primary" size="small" @click="viewContent(row)">查看被举报内容</el-button>
                <el-button size="small" @click="openInNewTabById(row)">新窗口打开</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="reporterName" label="举报人" width="100" />
        <el-table-column label="被举报" min-width="160">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="row.targetType==='user'?'danger':'info'">{{ typeLabel(row.targetType) }}</el-tag>
            <span style="margin-left:6px;font-size:12px;color:#909399">#{{ row.targetId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" width="100" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small" effect="dark" round>{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="success" size="small" plain @click="handleAction(row,'approved')">通过</el-button>
              <el-button type="danger" size="small" plain @click="handleAction(row,'rejected')">驳回</el-button>
            </template>
            <div v-else class="handled-info">{{ row.handlerName || '系统' }} · {{ row.handleNote || '-' }}</div>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading&&reports.length===0" class="empty-tip">暂无举报记录</div>
    </div>

    <!-- 处理对话框 -->
    <el-dialog v-model="actionDialogVisible" title="处理举报" width="500px" destroy-on-close>
      <div class="ad-report-info">
        <div class="ad-row"><span>举报人</span><strong>{{ currentReport?.reporterName }}</strong></div>
        <div class="ad-row"><span>类型</span><el-tag size="small">{{ typeLabel(currentReport?.targetType) }}</el-tag> ID:{{ currentReport?.targetId }}</div>
        <div class="ad-row"><span>原因</span><strong>{{ currentReport?.reason }}</strong></div>
        <div class="ad-row" v-if="currentReport?.description"><span>描述</span>{{ currentReport?.description }}</div>
      </div>
      <el-form :model="actionForm" label-position="top">
        <el-form-item :label="actionForm.status==='approved'?'确认违规处理':'驳回理由'">
          <el-input v-model="actionForm.handleNote" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item v-if="actionForm.status==='approved'" label="扣除信用分">
          <el-input-number v-model="actionForm.deductCredit" :min="0" :max="50" :step="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="actionSubmitting" @click="confirmAction">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { adminApi, skillApi, goodsApi, activityApi } from '@/api'

const reports = ref([]); const loading = ref(false); const statusFilter = ref('pending')
const pendingCount = computed(() => reports.value.filter(r => r.status === 'pending').length)

function typeLabel(t) { const m={skill:'技能',goods:'物品',activity:'活动',user:'用户'}; return m[t]||t }
function statusLabel(s) { const m={pending:'待处理',approved:'已通过',rejected:'已驳回'}; return m[s]||s }
function statusTag(s) { const m={pending:'warning',approved:'danger',rejected:'info'}; return m[s]||'' }

// 内联预览
const previewVisible = ref(false); const previewLoading = ref(false)
const previewData = ref(null); const previewReport = ref(null)

async function viewContent(row) {
  previewReport.value = row
  previewVisible.value = true
  previewLoading.value = true
  try {
    const type = row.targetType
    let res
    if (type === 'skill') res = await skillApi.getDetail(row.targetId)
    else if (type === 'goods') res = await goodsApi.getDetail(row.targetId)
    else if (type === 'activity') res = await activityApi.getDetail(row.targetId)
    else { previewData.value = { title: '用户举报', description: '被举报用户ID: ' + row.targetId, nickname: '用户' + row.targetId }; return }
    previewData.value = res.data
  } catch { previewData.value = { title: '(无法加载)' } }
  finally { previewLoading.value = false }
}

function openInNewTab() { if (previewReport.value) openInNewTabById(previewReport.value) }
function openInNewTabById(row) {
  const pathMap = { skill: '/skill/', goods: '/goods/', activity: '/activity/', user: '/user/' }
  window.open((pathMap[row.targetType] || '/skill/') + row.targetId, '_blank')
}

// 举报操作
async function fetchReports() {
  loading.value = true
  try { const params = { pageNum:1, pageSize:50 }; if (statusFilter.value) params.status = statusFilter.value
    const res = await adminApi.getReports(params); reports.value = res.data?.records || res.data?.data?.records || [] }
  catch {} finally { loading.value = false }
}

const actionDialogVisible = ref(false); const actionSubmitting = ref(false)
const currentReport = ref(null)
const actionForm = reactive({ status:'', handleNote:'', deductCredit:10 })

function handleAction(row, status) {
  currentReport.value = row; actionForm.status = status; actionForm.handleNote = ''; actionForm.deductCredit = status==='approved'?10:0
  actionDialogVisible.value = true
}
async function confirmAction() {
  actionSubmitting.value = true
  try { await adminApi.handleReport(currentReport.value.id, { status:actionForm.status, handleNote:actionForm.handleNote, deductCredit:actionForm.deductCredit }); ElMessage.success('处理完成'); actionDialogVisible.value=false; fetchReports() }
  catch {} finally { actionSubmitting.value = false }
}

onMounted(() => fetchReports())
</script>

<style scoped>
.admin-page { max-width: 100%; }
.ap-hero { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  h2 { font-size: 22px; font-weight: 800; margin: 0; }
  p { font-size: 14px; color: #909399; margin: 4px 0 0; strong { color: #f56c6c; } }
}
.ap-card { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 20px 24px; }
.modern-table { :deep(th) { font-weight: 700; background: #f8f9fb; border: none; } :deep(td) { border-color: #f5f6f8; } }
.expand-row { padding: 8px 0 8px 40px; font-size: 13px; color: var(--text-regular); }
.handled-info { font-size: 12px; color: #909399; }
.empty-tip { text-align: center; padding: 48px 0; color: #b0b8c4; }

.ad-report-info { background: #f8f9fb; border-radius: 12px; padding: 16px; margin-bottom: 16px; }
.ad-row { display: flex; gap: 12px; padding: 6px 0; font-size: 14px;
  span:first-child { color: #909399; width: 60px; flex-shrink: 0; }
  strong { color: var(--text-primary); }
  &+& { border-top: 1px solid #edf0f4; }
}

.preview-section { margin-bottom: 16px; }
.ps-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.ps-value { font-size: 14px; color: var(--text-primary); }
.ps-desc { white-space: pre-wrap; line-height: 1.6; background: #f8f9fb; padding: 12px; border-radius: 8px; }
.preview-actions { display: flex; gap: 10px; margin-top: 20px; padding-top: 16px; border-top: 1px solid #edf0f4; }
</style>
