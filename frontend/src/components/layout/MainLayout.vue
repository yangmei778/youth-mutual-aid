<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/home')">
          <span class="logo-icon">
            <el-icon :size="26" color="var(--primary-color)"><Connection /></el-icon>
          </span>
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
          <el-badge :value="userStore.unreadCount" :hidden="userStore.unreadCount === 0" class="message-badge">
            <el-button :icon="ChatDotRound" circle @click="$router.push('/message')" />
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-avatar">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <span class="credit-badge-mini" :style="{ color: userStore.creditLevel.color }">
                <el-icon :size="14"><component :is="userStore.creditLevel.icon" /></el-icon>
                {{ userStore.creditLevel.name }}
              </span>
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
import { ChatDotRound, Connection, Sunrise, Medal, Trophy } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

let pollTimer = null

// 轮询未读消息数
async function fetchUnreadCount() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await messageApi.getUnreadCount?.()
    userStore.unreadCount = res.data || 0
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
  const path = route.path
  if (path.startsWith('/skill')) return '/skill'
  if (path.startsWith('/goods')) return '/goods'
  if (path.startsWith('/activity')) return '/activity'
  return '/home'
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
  background: #f5f6f8;
}

/* ====== 玻璃拟态导航栏 ====== */
.header {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 0;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all 0.3s ease;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

/* ====== Logo ====== */
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 40px;
  transition: transform 0.2s ease;

  &:hover { transform: scale(1.03); }

  .logo-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 38px; height: 38px;
    border-radius: 10px;
    background: linear-gradient(135deg, #409eff, #2c6fce);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.25);
  }

  .logo-text {
    font-size: 18px;
    font-weight: 800;
    color: var(--text-primary);
    margin-left: 10px;
    letter-spacing: 1px;
    background: linear-gradient(135deg, #409eff 0%, #2c6fce 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

/* ====== 导航菜单 ====== */
.nav-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;

  :deep(.el-menu-item) {
    font-weight: 500;
    font-size: 15px;
    color: var(--text-regular);
    border-bottom: 2px solid transparent;
    transition: all 0.3s ease;

    &:hover {
      color: var(--primary-color);
      background: rgba(64, 158, 255, 0.04);
    }

    &.is-active {
      color: var(--primary-color);
      font-weight: 600;
      border-bottom-color: var(--primary-color);
      background: transparent;
    }
  }
}

/* ====== 右侧区域 ====== */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.message-badge {
  :deep(.el-badge__content) {
    top: 4px;
  }
  :deep(.el-button) {
    transition: all 0.3s ease;
    &:hover { transform: scale(1.08); background: rgba(64, 158, 255, 0.06); }
  }
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(64, 158, 255, 0.05);
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
  }

  .credit-badge-mini {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    font-size: 12px;
    font-weight: 500;
  }
}

/* ====== 主内容区 ====== */
.main-content {
  flex: 1;
  padding: 24px 20px;
}

/* ====== 底部 ====== */
.footer {
  text-align: center;
  color: #b0b5be;
  font-size: 12px;
  height: 48px;
  line-height: 48px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  background: #fff;
}
</style>
