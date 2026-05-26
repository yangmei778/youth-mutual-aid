<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/home')">
          <span class="logo-icon">🤝</span>
          <span class="logo-text">互助青年</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          class="nav-menu"
          router
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/skill">技能交换</el-menu-item>
          <el-menu-item index="/goods">闲置共享</el-menu-item>
          <el-menu-item index="/activity">临时搭伴</el-menu-item>
        </el-menu>

        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
            <el-button :icon="ChatDotRound" circle @click="$router.push('/message')" />
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-avatar">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <span class="credit-badge-mini">{{ userStore.creditLevel.badge }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="credit">信用中心</el-dropdown-item>
                <el-dropdown-item command="admin" v-if="isAdmin">后台管理</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <p>© 2026 互助青年 - 青年同城互助平台</p>
    </el-footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { messageApi } from '@/api'
import { ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const unreadCount = ref(0)
let pollTimer = null

// 轮询未读消息数
async function fetchUnreadCount() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await messageApi.getUnreadCount?.()
    unreadCount.value = res.data || 0
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const activeMenu = computed(() => {
  const pathMap = {
    '/skill': '/skill',
    '/goods': '/goods',
    '/activity': '/activity',
  }
  return pathMap[route.path] || '/home'
})

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'credit':
      router.push('/credit')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 40px;

  .logo-icon {
    font-size: 28px;
  }

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: var(--primary-color);
    margin-left: 8px;
  }
}

.nav-menu {
  flex: 1;
  border-bottom: none !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.message-badge {
  :deep(.el-badge__content) {
    top: 4px;
  }
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  .user-name {
    font-size: 14px;
    color: var(--text-primary);
  }

  .credit-badge-mini {
    font-size: 14px;
  }
}

.main-content {
  flex: 1;
  padding: 20px;
}

.footer {
  text-align: center;
  color: var(--text-secondary);
  font-size: 12px;
  height: 48px;
  line-height: 48px;
  border-top: 1px solid var(--border-color);
}
</style>
