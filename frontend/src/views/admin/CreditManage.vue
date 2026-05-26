<template>
  <div class="credit-manage page-container">
    <el-card shadow="hover">
      <template #header>
        <span>信用管理</span>
      </template>
      <el-form :model="form" label-width="100px" style="max-width: 500px">
        <el-form-item label="用户ID">
          <el-input v-model.number="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="变动值">
          <el-input-number v-model="form.changeValue" :step="1" controls-position="right" />
          <div class="form-tip">正数为加分，负数为扣分</div>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入调整原因" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleAdjust">提交调整</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const form = ref({
  userId: null,
  changeValue: 0,
  reason: '',
})
const submitting = ref(false)

function resetForm() {
  form.value = { userId: null, changeValue: 0, reason: '' }
}

async function handleAdjust() {
  if (!form.value.userId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  if (form.value.changeValue === 0) {
    ElMessage.warning('变动值不能为0')
    return
  }
  if (!form.value.reason.trim()) {
    ElMessage.warning('请输入调整原因')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定对用户 ${form.value.userId} ${form.value.changeValue > 0 ? '加' : '扣'} ${Math.abs(form.value.changeValue)} 信用分？`,
      '确认调整',
      { type: 'warning' }
    )
    submitting.value = true
    await adminApi.adjustCredit(form.value.userId, form.value.changeValue, form.value.reason.trim())
    ElMessage.success('信用分调整成功')
    resetForm()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('调整失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
