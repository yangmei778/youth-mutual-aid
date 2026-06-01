<template>
  <div class="report-manage page-container">
    <h2 style="margin-bottom:20px">举报管理</h2>

    <el-card shadow="hover">
      <el-table :data="reports" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column prop="targetType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="reason" label="原因" width="120" />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="success" size="small" @click="handleAction(row, 'approved')">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleAction(row, 'rejected')">
                驳回
              </el-button>
            </template>
            <div v-else class="handled-info">
              <span class="handler-name">{{ row.handlerName || '—' }}</span>
              <span class="handle-note">{{ row.handleNote || '无备注' }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length === 0" description="暂无举报" />
    </el-card>

    <!-- 处理对话框 -->
    <el-dialog v-model="actionDialogVisible" title="处理举报" width="460px" destroy-on-close>
      <el-form :model="actionForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-tag :type="actionForm.status === 'approved' ? 'danger' : 'info'" size="large">
            {{ actionForm.status === 'approved' ? '确认违规' : '驳回举报' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="actionForm.handleNote" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="扣除信用分" v-if="actionForm.status === 'approved'">
          <el-input-number v-model="actionForm.deductCredit" :min="0" :max="50" :step="5" />
          <span style="margin-left:8px;color:#909399;font-size:13px">可选，0表示不扣分</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionSubmitting" @click="confirmAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const reports = ref([])
const loading = ref(false)

function typeLabel(t) {
  const m = { skill: '技能', goods: '物品', activity: '活动', user: '用户' }
  return m[t] || t
}
function statusLabel(s) {
  const m = { pending: '待处理', approved: '已通过', rejected: '已驳回' }
  return m[s] || s
}
function statusTag(s) {
  const m = { pending: 'warning', approved: 'danger', rejected: 'info' }
  return m[s] || ''
}

async function fetchReports() {
  loading.value = true
  try {
    const res = await adminApi.getReports({ pageNum: 1, pageSize: 50 })
    reports.value = res.data?.data?.records || res.data?.data || []
  } catch { /* ignore */ }
  finally { loading.value = false }
}

// 处理操作
const actionDialogVisible = ref(false)
const actionSubmitting = ref(false)
const currentReport = ref(null)
const actionForm = reactive({ status: '', handleNote: '', deductCredit: 10 })

function handleAction(row, status) {
  currentReport.value = row
  actionForm.status = status
  actionForm.handleNote = ''
  actionForm.deductCredit = status === 'approved' ? 10 : 0
  actionDialogVisible.value = true
}

async function confirmAction() {
  actionSubmitting.value = true
  try {
    await adminApi.handleReport(currentReport.value.id, {
      status: actionForm.status,
      handleNote: actionForm.handleNote,
      deductCredit: actionForm.deductCredit,
    })
    ElMessage.success('处理完成')
    actionDialogVisible.value = false
    fetchReports()
  } catch { /* ignore */ }
  finally { actionSubmitting.value = false }
}

onMounted(() => fetchReports())
</script>

<style scoped>
.handled-info {
  font-size: 13px;
  color: var(--text-secondary);
  .handler-name { display: block; color: var(--text-primary); }
  .handle-note { display: block; font-size: 12px; }
}
</style>
