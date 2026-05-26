<template>
  <span class="credit-badge" :class="levelClass">
    {{ levelInfo.badge }} {{ levelInfo.name }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  score: {
    type: Number,
    default: 0,
  },
})

const levelInfo = computed(() => {
  const s = props.score || 0
  if (s >= 201) return { name: '钻石', badge: '💎', class: 'diamond' }
  if (s >= 101) return { name: '金牌', badge: '🥇', class: 'gold' }
  if (s >= 51) return { name: '银牌', badge: '🥈', class: 'silver' }
  if (s >= 21) return { name: '铜牌', badge: '🥉', class: 'bronze' }
  return { name: '新手', badge: '🌱', class: 'novice' }
})

const levelClass = computed(() => `level-${levelInfo.value.class}`)
</script>

<style scoped>
.credit-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.level-novice {
  background: #f0f9eb;
  color: #67c23a;
}

.level-bronze {
  background: #fdf6ec;
  color: #e6a23c;
}

.level-silver {
  background: #f4f4f5;
  color: #909399;
}

.level-gold {
  background: #fef0e6;
  color: #f5a623;
}

.level-diamond {
  background: #f0e6ff;
  color: #9b59b6;
}
</style>
