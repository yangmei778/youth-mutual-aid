<template>
  <span class="credit-badge" :class="`level-${levelInfo.class}`">
    <el-icon :size="14">
      <component :is="levelInfo.icon" />
    </el-icon>
    {{ levelInfo.name }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { Sunrise, Medal, Trophy } from '@element-plus/icons-vue'

const props = defineProps({
  score: {
    type: Number,
    default: 0,
  },
  // 可选：直接传入 icon 名称和颜色（用于 store 中已计算的 creditLevel）
  iconName: {
    type: String,
    default: '',
  },
  color: {
    type: String,
    default: '',
  },
})

const iconMap = {
  Sunrise,
  Medal,
  Trophy,
}

const levelInfo = computed(() => {
  // 如果传入了 iconName，使用外部计算好的等级信息
  if (props.iconName) {
    return {
      name: computeName(),
      icon: iconMap[props.iconName] || Sunrise,
      class: computeClass(),
    }
  }
  // 否则根据分数自行计算
  const s = props.score || 0
  if (s >= 201) return { name: '钻石', icon: Trophy, class: 'diamond' }
  if (s >= 101) return { name: '金牌', icon: Medal, class: 'gold' }
  if (s >= 51)  return { name: '银牌', icon: Medal, class: 'silver' }
  if (s >= 21)  return { name: '铜牌', icon: Medal, class: 'bronze' }
  return { name: '新手', icon: Sunrise, class: 'novice' }
})

function computeName() {
  const s = props.score || 0
  if (s >= 201) return '钻石'
  if (s >= 101) return '金牌'
  if (s >= 51)  return '银牌'
  if (s >= 21)  return '铜牌'
  return '新手'
}

function computeClass() {
  const s = props.score || 0
  if (s >= 201) return 'diamond'
  if (s >= 101) return 'gold'
  if (s >= 51)  return 'silver'
  if (s >= 21)  return 'bronze'
  return 'novice'
}
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
  white-space: nowrap;
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
