<template>
  <div class="admin-page">
    <div class="ap-hero">
      <div class="aph-left">
        <h2>信用管理</h2>
        <p>手动调整用户信用分</p>
      </div>
    </div>

    <div class="ap-card" style="max-width:600px">
      <el-form :model="form" label-position="top" size="large">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户ID">
              <el-input v-model.number="form.userId" placeholder="请输入要调整的用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变动分值">
              <el-input-number v-model="form.changeValue" :step="1" controls-position="right" style="width:100%" />
              <div class="form-hint">正数加分，负数扣分</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="调整原因">
          <el-input v-model="form.reason" placeholder="请填写调整原因" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
            <el-icon :size="16"><Check /></el-icon> 确认调整
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 信用变动记录 -->
    <div class="ap-card" style="margin-top:20px">
      <div class="cc-head">信用变动记录</div>
      <el-table :data="creditLogs" v-loading="logsLoading" stripe class="modern-table">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="changeValue" label="变动" width="80">
          <template #default="{ row }"><span :class="row.changeValue>0?'c-up':'c-down'">{{ row.changeValue>0?'+':'' }}{{ row.changeValue }}</span></template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="beforeScore" label="变动前" width="80" />
        <el-table-column prop="afterScore" label="变动后" width="80" />
        <el-table-column prop="createdAt" label="时间" width="160" />
      </el-table>
      <div v-if="!logsLoading && creditLogs.length===0" class="empty-tip">暂无记录</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { adminApi, creditApi } from '@/api'

const form = reactive({ userId: null, changeValue: 0, reason: '' })
const submitting = ref(false)
const creditLogs = ref([])
const logsLoading = ref(false)

async function fetchLogs() {
  logsLoading.value = true
  try { const r = await creditApi.getCreditLogs({ page:1, size:50 }); creditLogs.value = r.data?.records || r.data?.data?.records || [] }
  catch {} finally { logsLoading.value = false }
}

async function handleSubmit() {
  if (!form.userId || form.changeValue === 0) { ElMessage.warning('请填写用户ID和变动值'); return }
  submitting.value = true
  try {
    await adminApi.adjustCredit(form.userId, form.changeValue, form.reason)
    ElMessage.success('信用分调整成功')
    form.userId = null; form.changeValue = 0; form.reason = ''
    fetchLogs()
  } catch {} finally { submitting.value = false }
}

onMounted(() => fetchLogs())
</script>

<style scoped>
.admin-page { max-width: 100%; }
.ap-hero { margin-bottom: 20px; h2 { font-size: 22px; font-weight: 800; margin: 0; } p { font-size: 14px; color: #909399; margin: 4px 0 0; } }
.ap-card { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 28px 32px; }
.form-hint { font-size: 12px; color: #909399; margin-top: 4px; }
.cc-head { font-size: 15px; font-weight: 700; padding-bottom: 14px; border-bottom: 2px solid #f5f6f8; margin-bottom: 16px; }
.modern-table { :deep(th) { font-weight: 700; background: #f8f9fb; border: none; } :deep(td) { border-color: #f5f6f8; } }
.c-up { color:#67c23a;font-weight:700; }.c-down { color:#f56c6c;font-weight:700; }
.empty-tip { text-align: center; padding: 32px; color: #b0b8c4; }
</style>
