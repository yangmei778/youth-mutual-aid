<template>
  <div class="admin-page">
    <div class="ap-hero">
      <div class="aph-left">
        <h2>操作日志</h2>
        <p>共 <strong>{{ total }}</strong> 条操作记录</p>
      </div>
      <div class="aph-right">
        <el-select v-model="actionFilter" placeholder="操作类型" clearable style="width:180px" @change="fetchLogs">
          <el-option v-for="a in actionOptions" :key="a.value" :label="a.label" :value="a.value" />
        </el-select>
      </div>
    </div>

    <div class="ap-card">
      <el-table :data="logs" v-loading="loading" stripe class="modern-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="操作类型" width="120">
          <template #default="{ row }">
            <span class="log-action-tag" :class="'act-' + row.action">{{ actionLabel(row.action) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作对象" min-width="220">
          <template #default="{ row }">
            <span class="log-target-text">{{ row.target }}</span>
          </template>
        </el-table-column>
        <el-table-column label="详情" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="log-detail-text">{{ row.detail || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column label="操作时间" width="170">
          <template #default="{ row }">
            <span>{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && logs.length === 0" class="empty-tip">暂无操作记录</div>

      <div class="ap-footer" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchLogs"
          @current-change="fetchLogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'

const logs = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const actionFilter = ref('')

const actionOptions = [
  { label: '全部', value: '' },
  { label: '封禁用户', value: 'ban_user' },
  { label: '解封用户', value: 'unban_user' },
  { label: '下架内容', value: 'offline_post' },
  { label: '删除内容', value: 'delete_post' },
  { label: '处理举报', value: 'handle_report' },
  { label: '调整信用', value: 'adjust_credit' },
]

function actionLabel(a) {
  const m = {
    ban_user: '封禁用户',
    unban_user: '解封用户',
    offline_post: '下架内容',
    delete_post: '删除内容',
    handle_report: '处理举报',
    adjust_credit: '调整信用',
  }
  return m[a] || a
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

async function fetchLogs() {
  loading.value = true
  try {
    const params = { pageNum: page.value, pageSize: pageSize.value }
    if (actionFilter.value) params.action = actionFilter.value
    const r = await adminApi.getLogs(params)
    // 响应拦截器已解包 response.data，所以 r 就是 R 对象，r.data 就是 PageResult
    const pageData = r.data
    logs.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch {
    logs.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchLogs())
</script>

<style scoped>
.admin-page { max-width: 100%; }

.ap-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.ap-hero h2 {
  font-size: 22px;
  font-weight: 800;
  margin: 0;
}
.ap-hero p {
  font-size: 14px;
  color: #909399;
  margin: 4px 0 0;
}
.ap-hero p strong {
  color: var(--primary-color);
}

.ap-card {
  background: #fff;
  border: 1px solid #edf0f4;
  border-radius: 16px;
  padding: 20px 24px;
}

.modern-table :deep(th) {
  font-weight: 700;
  background: #f8f9fb;
  border: none;
}
.modern-table :deep(td) {
  border-color: #f5f6f8;
}

.log-action-tag {
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.log-action-tag.act-ban_user,
.log-action-tag.act-delete_post {
  background: #fef0f0;
  color: #f56c6c;
}
.log-action-tag.act-unban_user {
  background: #f0f9eb;
  color: #67c23a;
}
.log-action-tag.act-offline_post,
.log-action-tag.act-adjust_credit {
  background: #fef9e7;
  color: #e6a23c;
}
.log-action-tag.act-handle_report {
  background: rgba(64, 158, 255, 0.08);
  color: #409eff;
}

.log-target-text {
  font-weight: 500;
  color: var(--text-primary);
}

.log-detail-text {
  font-size: 12px;
  color: #909399;
}

.empty-tip {
  text-align: center;
  padding: 48px 0;
  color: #b0b8c4;
}

.ap-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
