<template>
  <div class="admin-dashboard page-container">
    <h2 style="margin-bottom: 20px">管理看板</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="用户总数" :value="stats.userCount">
            <template #prefix>
              <el-icon style="color: #409EFF"><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="技能数量" :value="stats.skillCount">
            <template #prefix>
              <el-icon style="color: #67C23A"><Cpu /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="物品数量" :value="stats.goodsCount">
            <template #prefix>
              <el-icon style="color: #E6A23C"><Box /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="活动数量" :value="stats.activityCount">
            <template #prefix>
              <el-icon style="color: #F56C6C"><Calendar /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Cpu, Box, Calendar } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const stats = ref({
  userCount: 0,
  skillCount: 0,
  goodsCount: 0,
  activityCount: 0,
})

async function fetchStats() {
  try {
    const res = await adminApi.getStats()
    stats.value = res.data?.data || res.data || stats.value
  } catch (e) {
    ElMessage.error('获取统计数据失败')
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.stat-card {
  text-align: center;
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
}
</style>
