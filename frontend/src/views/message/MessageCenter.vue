<template>
  <div class="message-center page-container">
    <el-card shadow="hover">
      <el-tabs v-model="activeTab">
        <!-- 系统通知 -->
        <el-tab-pane label="系统通知" name="notification">
          <div class="tab-header">
            <el-button type="primary" size="small" @click="handleMarkAllRead" :disabled="!hasUnread">全部已读</el-button>
          </div>
          <div class="notification-list" v-loading="notifLoading">
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ unread: !item.isRead }"
              @click="handleMarkRead(item)"
            >
              <div class="notif-dot" v-if="!item.isRead"></div>
              <div class="notif-content">
                <div class="notif-title">{{ item.title }}</div>
                <div class="notif-body">{{ item.content }}</div>
                <div class="notif-time">{{ item.createdAt }}</div>
              </div>
              <el-tag v-if="!item.isRead" type="danger" size="small" effect="dark">未读</el-tag>
              <el-tag v-else type="info" size="small">已读</el-tag>
            </div>
            <el-empty v-if="!notifLoading && notifications.length === 0" description="暂无通知" />
          </div>
        </el-tab-pane>

        <!-- 私信 -->
        <el-tab-pane label="私信" name="message">
          <div class="message-list" v-loading="msgLoading">
            <div v-for="item in messages" :key="item.id" class="message-item">
              <el-avatar :size="40" :src="item.senderAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="msg-content">
                <div class="msg-header">
                  <span class="msg-name">{{ item.senderName || '用户' }}</span>
                  <span class="msg-time">{{ item.createdAt }}</span>
                </div>
                <div class="msg-body">{{ item.content }}</div>
              </div>
            </div>
            <el-empty v-if="!msgLoading && messages.length === 0" description="暂无私信" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { messageApi } from '@/api'

const activeTab = ref('notification')
const notifications = ref([])
const messages = ref([])
const notifLoading = ref(false)
const msgLoading = ref(false)

const hasUnread = computed(() => notifications.value.some(n => !n.isRead))

async function fetchNotifications() {
  notifLoading.value = true
  try {
    const res = await messageApi.getNotifications({ page: 1, size: 50 })
    notifications.value = res.data?.data?.records || res.data?.data || []
  } catch (e) {
    // ignore
  } finally {
    notifLoading.value = false
  }
}

async function fetchMessages() {
  msgLoading.value = true
  try {
    const res = await messageApi.getMessages({ page: 1, size: 50 })
    messages.value = res.data?.data?.records || res.data?.data || []
  } catch (e) {
    // ignore
  } finally {
    msgLoading.value = false
  }
}

async function handleMarkRead(item) {
  if (item.isRead) return
  try {
    await messageApi.markAsRead(item.id)
    item.isRead = true
  } catch (e) {
    ElMessage.error('标记失败')
  }
}

async function handleMarkAllRead() {
  try {
    await messageApi.markAllRead()
    notifications.value.forEach(n => { n.isRead = true })
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchNotifications()
  fetchMessages()
})
</script>

<style scoped>
.tab-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.notification-list {
  max-height: 600px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-item.unread:hover {
  background-color: #d9ecff;
}

.notif-dot {
  position: absolute;
  top: 16px;
  left: 6px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.notif-content {
  flex: 1;
}

.notif-title {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 4px;
}

.notif-body {
  color: #606266;
  font-size: 14px;
  margin-bottom: 4px;
}

.notif-time {
  color: #909399;
  font-size: 12px;
}

.message-list {
  max-height: 600px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.msg-content {
  flex: 1;
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.msg-name {
  font-weight: bold;
  font-size: 15px;
}

.msg-time {
  color: #909399;
  font-size: 12px;
}

.msg-body {
  color: #606266;
  font-size: 14px;
}
</style>
