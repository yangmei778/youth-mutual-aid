<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="220px" class="admin-aside">
        <div class="admin-logo">
          <el-icon :size="20"><Connection /></el-icon>
          <span>管理后台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/admin">
            <el-icon><DataAnalysis /></el-icon>
            <span>管理看板</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/audit">
            <el-icon><DocumentChecked /></el-icon>
            <span>内容审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/credit">
            <el-icon><Trophy /></el-icon>
            <span>信用管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <el-icon><WarningFilled /></el-icon>
            <span>举报管理</span>
            <el-badge :value="reportBadge" :hidden="!reportBadge" class="menu-badge" />
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <el-button text @click="$router.push('/')">返回前台</el-button>
        </el-header>
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataAnalysis, User, DocumentChecked, Trophy, Connection, WarningFilled, Setting, Document } from '@element-plus/icons-vue'

import { ref, onMounted } from 'vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
const reportBadge = ref(0)

async function fetchReportBadge() {
  try {
    const { adminApi } = await import('@/api')
    const r = await adminApi.getReports({ status: 'pending', pageNum: 1, pageSize: 1 })
    const total = r.data?.data?.total || r.data?.total || 0
    reportBadge.value = total
  } catch {}
}
onMounted(() => { fetchReportBadge(); setInterval(fetchReportBadge, 30000) })
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #304156;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;

  .admin-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    flex-shrink: 0;
  }

  :deep(.el-menu) {
    flex: 1;
    border-right: none;
    overflow-y: auto;
  }
}

.admin-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.admin-main {
  background: #f5f7fa;
}
.menu-badge {
  margin-left: auto;
  :deep(.el-badge__content) { top: 50%; transform: translateY(-50%); }
}
</style>
