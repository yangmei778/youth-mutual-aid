<template>
  <el-card class="user-card" shadow="hover" @click="goToProfile">
    <div class="user-card-content">
      <el-avatar :size="48" :src="user.avatar">
        {{ user.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="user-info">
        <div class="nickname">{{ user.nickname || '未知用户' }}</div>
        <CreditBadge :score="user.creditScore || 0" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import CreditBadge from './CreditBadge.vue'

const props = defineProps({
  user: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

function goToProfile() {
  if (props.user.id) {
    router.push(`/user/${props.user.id}`)
  }
}
</script>

<style scoped>
.user-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.user-card:hover {
  transform: translateY(-2px);
}

.user-card-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nickname {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}
</style>
