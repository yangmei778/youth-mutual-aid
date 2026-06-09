<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    :width="width"
    center
    :close-on-click-modal="false"
    destroy-on-close
    @close="handleClose"
  >
    <div class="cd-body">
      <div class="cd-icon-wrap" :class="'cd-' + type">
        <el-icon :size="28"><WarningFilled /></el-icon>
      </div>
      <p class="cd-message">{{ message }}</p>
    </div>
    <template #footer>
      <el-button size="large" @click="handleClose" class="cd-cancel">取消</el-button>
      <el-button size="large" :type="type === 'danger' ? 'danger' : 'primary'" @click="handleConfirm">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  message: String,
  title: { type: String, default: '请确认' },
  type: { type: String, default: 'warning' },
  width: { type: String, default: '420px' },
})

const emit = defineEmits(['resolve'])
const visible = ref(true)
const confirmText = props.type === 'danger' ? '确认删除' : '确认'

function handleConfirm() { emit('resolve', true); visible.value = false }
function handleClose() { emit('resolve', false); visible.value = false }
</script>

<style scoped>
.cd-body { display: flex; flex-direction: column; align-items: center; padding: 16px 0 8px; }
.cd-icon-wrap { width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; border-radius: 50%; margin-bottom: 18px; }
.cd-warning { background: rgba(230,162,60,0.1); color: #e6a23c; }
.cd-danger { background: rgba(245,108,108,0.1); color: #f56c6c; }
.cd-message { font-size: 15px; color: var(--text-primary); line-height: 1.6; text-align: center; max-width: 320px; }
.cd-cancel { min-width: 90px; border-radius: 10px; }
</style>
