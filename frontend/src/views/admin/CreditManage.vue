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
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const form = reactive({ userId: null, changeValue: 0, reason: '' })
const submitting = ref(false)

async function handleSubmit() {
  if (!form.userId || form.changeValue === 0) { ElMessage.warning('请填写用户ID和变动值'); return }
  submitting.value = true
  try {
    await adminApi.adjustCredit(form.userId, form.changeValue, form.reason)
    ElMessage.success('信用分调整成功')
    form.userId = null; form.changeValue = 0; form.reason = ''
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.admin-page { max-width: 100%; }
.ap-hero { margin-bottom: 20px; h2 { font-size: 22px; font-weight: 800; margin: 0; } p { font-size: 14px; color: #909399; margin: 4px 0 0; } }
.ap-card { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 28px 32px; }
.form-hint { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
