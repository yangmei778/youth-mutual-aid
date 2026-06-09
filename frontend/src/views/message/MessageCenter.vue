<template>
  <div class="message-center page-container">
    <div class="msg-layout">
      <!-- 左侧标签 -->
      <div class="msg-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">消息中心</span>
        </div>
        <div class="sidebar-nav">
          <div class="nav-item" :class="{ active: activeTab === 'notification' }" @click="activeTab = 'notification'">
            <span class="nav-icon nav-icon-bell">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            </span>
            <span class="nav-label">系统通知</span>
            <span v-if="notifUnread" class="nav-badge">{{ notifUnread }}</span>
          </div>
          <div class="nav-item" :class="{ active: activeTab === 'message' }" @click="activeTab = 'message'">
            <span class="nav-icon nav-icon-chat">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </span>
            <span class="nav-label">私信</span>
            <span v-if="msgUnread" class="nav-badge">{{ msgUnread }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="msg-content">
        <!-- ====== 通知面板 ====== -->
        <div v-if="activeTab === 'notification'" class="notif-panel">
          <div class="panel-head">
            <h3>系统通知</h3>
            <el-button v-if="hasUnread" text type="primary" size="small" @click="handleMarkAllRead">全部已读</el-button>
          </div>
          <div class="notif-list" v-loading="notifLoading">
            <div v-for="item in notifications" :key="item.id" class="notif-card" :class="[isNotifUnread(item) ? 'unread' : '', isNotifUnread(item) ? notifCardClass(item) : '']">
              <div class="ncard-left">
                <div class="ncard-icon" :class="iconClass(item.type)">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                    <template v-if="isRequest(item.type)"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></template>
                    <template v-else-if="item.type?.includes('complete')"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></template>
                    <template v-else><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></template>
                  </svg>
                </div>
              </div>
              <div class="ncard-body" @click="handleNotifClick(item)">
                <div class="ncard-title">{{ item.title }}</div>
                <div class="ncard-text" v-if="item.content">{{ item.content }}</div>
                <div class="ncard-time">{{ formatNotifTime(item.createdAt) }}</div>
                <span class="ncard-hint">点击查看 →</span>
              </div>
              <div class="ncard-right">
                <span v-if="isNotifUnread(item)" class="unread-dot"></span>
                <span class="ncard-type">{{ typeLabel(item.type) }}</span>
                <div class="ncard-del-wrap">
                  <el-button text size="small" class="ncard-del-btn" @click.stop="deleteNotif(item)" title="删除此通知">
                    <svg viewBox="0 0 18 18" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                      <polyline points="3 5 5 5 15 5"/><path d="M6 5V4a1 1 0 011-1h4a1 1 0 011 1v1"/><line x1="7" y1="8" x2="7" y2="13"/><line x1="11" y1="8" x2="11" y2="13"/><path d="M5 5l1 10h6l1-10"/>
                    </svg>
                  </el-button>
                </div>
              </div>
            </div>
            <div v-if="!notifLoading && notifications.length === 0" class="empty-state">
              <svg viewBox="0 0 80 80" width="80" height="80" fill="none"><circle cx="40" cy="40" r="30" stroke="#e0e5ec" stroke-width="2" stroke-dasharray="4 4"/><path d="M40 25v20M40 50v2" stroke="#c8d6e5" stroke-width="2" stroke-linecap="round"/></svg>
              <p>暂无通知</p>
              <span>互助请求和系统消息会出现在这里</span>
            </div>
          </div>
        </div>

        <!-- ====== 私信面板 ====== -->
        <div v-if="activeTab === 'message'" class="chat-panel">
          <div class="chat-layout">
            <!-- 会话列表 -->
            <div class="chat-sidebar">
              <div class="conv-search">
                <el-input v-model="convSearch" placeholder="搜索对话..." :prefix-icon="Search" size="small" clearable />
              </div>
              <div class="conv-list" v-loading="convLoading">
                <div v-for="conv in filteredConvs" :key="conv.userId" class="conv-item"
                  :class="{ active: activeConv?.userId === conv.userId, unread: conv.unreadCount > 0 }"
                  @click="openConversation(conv)">
                  <el-avatar :size="44" :src="conv.avatar">{{ conv.nickname?.charAt(0) }}</el-avatar>
                  <div class="conv-body">
                    <div class="conv-top">
                      <span class="conv-name">{{ conv.nickname }}</span>
                      <span class="conv-time">{{ formatConvTime(conv.lastTime) }}</span>
                    </div>
                    <div class="conv-bottom">
                      <span class="conv-preview">{{ conv.lastContent }}</span>
                      <span v-if="conv.unreadCount > 0" class="conv-unread">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="!convLoading && filteredConvs.length === 0" class="empty-state" style="padding: 40px 16px;">
                  <p style="font-size: 14px;">{{ convSearch ? '没有匹配的对话' : '暂无对话' }}</p>
                  <span>从技能/物品/活动详情页联系发布者开始对话</span>
                </div>
              </div>
            </div>

            <!-- 聊天窗口 -->
            <div class="chat-main">
              <template v-if="activeConv">
                <div class="chat-header">
                  <el-avatar :size="38" :src="activeConv.avatar">{{ activeConv.nickname?.charAt(0) }}</el-avatar>
                  <div class="ch-info">
                    <span class="ch-name">{{ activeConv.nickname }}</span>
                  </div>
                </div>
                <div class="chat-messages" ref="chatMsgRef">
                  <div v-for="(msg, idx) in chatMessages" :key="msg.id" class="chat-msg"
                    :class="msg.senderId === currentUserId ? 'msg-mine' : 'msg-other'">
                    <el-avatar v-if="msg.senderId !== currentUserId" :size="30" :src="activeConv.avatar" class="msg-avatar">
                      {{ activeConv.nickname?.charAt(0) }}
                    </el-avatar>
                    <div class="msg-bubble">
                      <div class="msg-text">{{ msg.content }}</div>
                    </div>
                    <el-avatar v-if="msg.senderId === currentUserId" :size="30" :src="userStore.userInfo?.avatar" class="msg-avatar">
                      {{ userStore.userInfo?.nickname?.charAt(0) }}
                    </el-avatar>
                  </div>
                </div>
                <div class="chat-input-area">
                  <div class="chat-input-row">
                    <el-input v-model="chatInput" type="textarea" :rows="2" placeholder="输入消息... 按 Ctrl+Enter 发送"
                      @keyup.enter.ctrl="sendChat" class="chat-textarea" />
                    <el-button type="primary" :loading="chatSending" :disabled="!chatInput.trim()" @click="sendChat" class="send-btn">
                      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                    </el-button>
                  </div>
                </div>
              </template>
              <div v-else class="chat-empty-state">
                <svg viewBox="0 0 100 80" width="100" height="80" fill="none">
                  <rect x="15" y="15" width="70" height="55" rx="12" stroke="#e0e5ec" stroke-width="2"/>
                  <circle cx="35" cy="42" r="14" stroke="#e0e5ec" stroke-width="2"/>
                  <circle cx="65" cy="42" r="14" stroke="#e0e5ec" stroke-width="2"/>
                  <path d="M25 38h20M25 46h12" stroke="#e0e5ec" stroke-width="1.5" stroke-linecap="round"/>
                  <path d="M55 38h20M55 46h12" stroke="#e0e5ec" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <p>选择一个对话</p>
                <span>点击左侧会话开始聊天</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Close } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { messageApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)
const activeTab = ref('notification')

// ====== 通知 ======
const notifications = ref([])
const readIds = ref([]) // 本地已读缓存（响应式数组）
const notifLoading = ref(false)
const hasUnread = computed(() => notifications.value.some(n => isNotifUnread(n)))
const notifUnread = computed(() => notifications.value.filter(n => !n.isRead).length)

function isRequest(t) { return t && (t.includes('request') || t.includes('|')) }
function canNavigate(item) {
  const t = item.type || ''
  return t.startsWith('reported|') || t.startsWith('content_offline|') || t.includes('activity_') || t.includes('mutual_')
}
function isNotifUnread(item) { return !item.isRead && !readIds.value.includes(item.id) }
async function deleteNotif(item) {
  try { await messageApi.deleteNotification(item.id); fetchNotifications(); refreshUnread() } catch {}
}
function notifCardClass(item) {
  const t = item.type || ''
  if (t.startsWith('reported|')) return 'notif-warn'
  if (t.startsWith('content_offline|')) return 'notif-danger'
  return ''
}
function parseNotifType(t) {
  // type 格式: "skill|请求者ID|relatedId" 或纯字符串
  if (!t) return null
  if (t.includes('|')) {
    const parts = t.split('|')
    return { itemType: parts[0], requesterId: parts[1], relatedId: parts[2] }
  }
  return null
}
function handleNotifClick(item) {
  handleMarkRead(item)
  // 根据通知类型跳转到对应内容
  const t = item.type || ''
  const rid = item.relatedId

  // 被举报 / 被下架 / 举报结果 → 跳转到对应内容
  if (t.startsWith('reported|') || t.startsWith('content_offline|') || t.startsWith('report_result|')) {
    const targetType = t.split('|')[1]
    const routeMap = { skill: '/skill/', goods: '/goods/', activity: '/activity/' }
    if (rid && targetType) router.push((routeMap[targetType] || '/skill/') + rid + '?reason=' + encodeURIComponent(item.content || ''))
    return
  }
  // 旧格式report_result不跳转
  if (t === 'report_result') return

  // 互助请求 → 跳转到内容详情
  if (isRequest(t)) {
    goToItem(item)
    return
  }

  // 报名通过/拒绝 → 跳转到活动
  if (t.includes('activity_approved') || t.includes('activity_rejected')) {
    if (rid) router.push('/activity/' + rid)
    return
  }

  // 互助接受/拒绝/取消 → 跳转到互助记录
  if (t.includes('mutual_')) {
    if (rid) router.push('/mutual/' + rid)
    return
  }

  // 管理员：新举报 → 举报管理页
  if (t === 'report_new') {
    router.push('/admin/reports')
    return
  }

  // 举报处理结果 → 跳转到被举报的内容（仅新格式 report_result|type）
  if (t.startsWith('report_result|')) {
    const targetType = t.split('|')[1]
    const routeMap = { skill: '/skill/', goods: '/goods/', activity: '/activity/' }
    if (rid && targetType) router.push((routeMap[targetType] || '/skill/') + rid + '?reason=' + encodeURIComponent(item.content || ''))
    return
  }
  // 旧格式 report_result 不跳转（relatedId是reportId不是postId）
  if (t === 'report_result') return
}
function goToItem(item) {
  handleMarkRead(item)
  const info = parseNotifType(item.type)
  if (info && info.relatedId && info.relatedId !== '0') {
    const path = '/' + item.type.split('|')[0] + '/' + info.relatedId
    router.push(path).catch(() => { location.href = path })
  } else if (item.relatedId) {
    router.push('/mutual/' + item.relatedId).catch(() => {})
  } else {
    ElMessage.warning('无法跳转')
  }
}
function chatWithRequester(item) {
  handleMarkRead(item)
  const info = parseNotifType(item.type)
  if (!info?.requesterId) return
  // 直接路由跳转到私信并带用户参数
  activeTab.value = 'message'
  router.push('/message?chat=' + info.requesterId)
  // 同时主动拉取会话
  setTimeout(async () => {
    await fetchConversations()
    const c = conversations.value.find(cc => cc.userId == info.requesterId)
    if (c) openConversation(c)
  }, 600)
}

function typeLabel(t) {
  if (!t) return ''
  if (t.includes('|')) t = t.split('|')[0]
  if (t.includes('request')) return '互助请求'
  if (t.includes('reject')) return '已拒绝'
  if (t.includes('complete')) return '已完成'
  if (t.includes('cancel')) return '已取消'
  if (t.includes('approved')) return '报名通过'
  return ''
}
function iconClass(t) {
  if (!t) return ''
  if (t.includes('request')) return 'icon-request'
  if (t.includes('complete') || t.includes('approved')) return 'icon-success'
  if (t.includes('reject') || t.includes('cancel')) return 'icon-danger'
  return ''
}
function formatNotifTime(t) {
  if (!t) return ''
  const d = new Date(t), now = new Date()
  const diff = now - d
  if (diff < 3600000) return Math.floor(diff/60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff/3600000) + ' 小时前'
  return (d.getMonth()+1) + '月' + d.getDate() + '日 ' + String(d.getHours()).padStart(2,'0') + ':' + String(d.getMinutes()).padStart(2,'0')
}

async function fetchNotifications() {
  notifLoading.value = true
  try {
    const res = await messageApi.getNotifications({ page: 1, size: 50 })
    notifications.value = res.data?.records || res.data?.data?.records || []
  } catch { /* ignore */ }
  finally { notifLoading.value = false }
}
function handleMarkRead(item) {
  if (readIds.value.includes(item.id)) return
  readIds.value.push(item.id)
  item.isRead = true
  refreshUnread()
  messageApi.markAsRead(item.id).catch(() => {})
}
async function handleMarkAllRead() {
  try {
    await messageApi.markAllRead()
    notifications.value.forEach(n => { n.isRead = true; readIds.value.push(n.id) })
    ElMessage.success('已全部已读'); refreshUnread()
  } catch { ElMessage.error('操作失败') }
}
async function refreshUnread() {
  try { const r = await messageApi.getUnreadCount(); userStore.unreadCount = r.data || 0 } catch {}
}

// ====== 私信 ======
const conversations = ref([])
const convLoading = ref(false)
const activeConv = ref(null)
const chatMessages = ref([])
const chatInput = ref('')
const chatSending = ref(false)
const chatMsgRef = ref(null)
const convSearch = ref('')

const msgUnread = computed(() => conversations.value.reduce((s, c) => s + (c.unreadCount || 0), 0))
const filteredConvs = computed(() => {
  if (!convSearch.value) return conversations.value
  const q = convSearch.value.toLowerCase()
  return conversations.value.filter(c => c.nickname?.toLowerCase().includes(q) || c.lastContent?.toLowerCase().includes(q))
})

async function fetchConversations() {
  convLoading.value = true
  try {
    const res = await messageApi.getConversations?.()
    conversations.value = res.data?.data || res.data || []
  } catch { /* ignore */ }
  finally { convLoading.value = false }
}
async function openConversation(conv) {
  activeConv.value = conv
  try {
    const res = await messageApi.getMessages({ targetUserId: conv.userId, page: 1, size: 50 })
    chatMessages.value = (res.data?.records || res.data?.data?.records || []).reverse()
    if (conv.unreadCount > 0) {
      await messageApi.markAllMessagesRead?.(conv.userId)
      conv.unreadCount = 0; refreshUnread(); fetchConversations()
    }
    await nextTick(); scrollChatBottom()
  } catch { /* ignore */ }
}
function scrollChatBottom() {
  if (chatMsgRef.value) chatMsgRef.value.scrollTop = chatMsgRef.value.scrollHeight
}
async function sendChat() {
  if (!chatInput.value.trim() || !activeConv.value) return
  chatSending.value = true
  const content = chatInput.value.trim()
  try {
    await messageApi.sendMessage({ receiverId: activeConv.value.userId, content })
    chatMessages.value.push({ id: Date.now(), senderId: currentUserId.value, content, createdAt: new Date().toISOString() })
    chatInput.value = ''; activeConv.value.lastContent = content
    await nextTick(); scrollChatBottom(); fetchConversations()
  } catch { /* ignore */ }
  finally { chatSending.value = false }
}
function formatConvTime(t) {
  if (!t) return ''
  const d = new Date(t), now = new Date()
  if (d.toDateString() === now.toDateString()) return String(d.getHours()).padStart(2,'0') + ':' + String(d.getMinutes()).padStart(2,'0')
  return (d.getMonth()+1) + '/' + d.getDate()
}

onMounted(async () => {
  await fetchConversations(); fetchNotifications()
  const chatUserId = route.query.chat
  if (chatUserId) {
    const conv = conversations.value.find(c => c.userId == chatUserId)
    if (conv) { activeTab.value = 'message'; openConversation(conv) }
  }
})
</script>

<style scoped>
.message-center { max-width: 1000px; }

.msg-layout { display: flex; gap: 0; min-height: 580px; background: #fff; border-radius: 18px; border: 1px solid #edf0f4; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.03); }

/* ====== 左侧导航 ====== */
.msg-sidebar { width: 200px; flex-shrink: 0; background: #f8f9fb; border-right: 1px solid #edf0f4; display: flex; flex-direction: column; }
.sidebar-header { padding: 20px 20px 16px; }
.sidebar-title { font-size: 17px; font-weight: 700; color: var(--text-primary); }
.sidebar-nav { display: flex; flex-direction: column; gap: 2px; padding: 0 12px; }
.nav-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 14px;
  border-radius: 10px; cursor: pointer; transition: all 0.2s;
  font-size: 14px; color: var(--text-regular); position: relative;
  &:hover { background: rgba(64,158,255,0.06); color: var(--primary-color); }
  &.active { background: rgba(64,158,255,0.08); color: var(--primary-color); font-weight: 600; }
}
.nav-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 10px; }
.nav-icon-bell { background: rgba(230,162,60,0.1); color: #e6a23c; }
.nav-icon-chat { background: rgba(64,158,255,0.1); color: #409eff; }
.nav-badge { margin-left: auto; background: #f56c6c; color: #fff; font-size: 11px; font-weight: 700; min-width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; border-radius: 9px; padding: 0 5px; }

/* ====== 右侧内容 ====== */
.msg-content { flex: 1; min-width: 0; }

/* ====== 通知面板 ====== */
.notif-panel { height: 100%; display: flex; flex-direction: column; }
.panel-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 24px; border-bottom: 1px solid #edf0f4;
  h3 { font-size: 16px; font-weight: 700; margin: 0; }
}
.notif-list { flex: 1; overflow-y: auto; padding: 12px 20px; }

.notif-card {
  position: relative;
  display: flex; gap: 14px; padding: 14px 16px; border-radius: 12px;
  cursor: pointer; transition: all 0.2s; margin-bottom: 4px;
  &:hover { background: #f8f9fb; }
  &.unread { background: #f0f5ff; border-left: 3px solid var(--primary-color); padding-left: 13px; }
  &.notif-warn { border-left: 3px solid #e6a23c; background: #fef9e7; padding-left: 13px; }
  &.notif-danger { border-left: 3px solid #f56c6c; background: #fef0f0; padding-left: 13px; }
}
.ncard-left { flex-shrink: 0; }
.ncard-icon {
  width: 38px; height: 38px; display: flex; align-items: center; justify-content: center;
  border-radius: 12px;
  &.icon-request { background: rgba(64,158,255,0.08); color: #409eff; }
  &.icon-success { background: rgba(103,194,58,0.08); color: #67c23a; }
  &.icon-danger { background: rgba(245,108,108,0.08); color: #f56c6c; }
}
.ncard-body { flex: 1; min-width: 0; }
.ncard-title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.ncard-text { font-size: 13px; color: #606266; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.ncard-time { font-size: 12px; color: #b0b8c4; margin-top: 6px; }
.ncard-right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; flex-shrink: 0; }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary-color); margin-top: 4px; }
.ncard-type { font-size: 11px; color: #909399; background: #f5f6f8; padding: 2px 8px; border-radius: 6px; }
.ncard-del-wrap { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); opacity: 0; transition: all 0.25s ease; }
.notif-card:hover .ncard-del-wrap { opacity: 1; }
.ncard-del-btn {
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  border-radius: 10px; color: #b0b8c4; transition: all 0.2s;
  &:hover { background: #fef0f0; color: #f56c6c; transform: scale(1.08); }
}
.ncard-hint { font-size: 12px; color: var(--primary-color); font-weight: 500; display: block; margin-top: 6px; }

/* ====== 聊天面板 ====== */
.chat-panel { height: 100%; }
.chat-layout { display: flex; height: 100%; }
.chat-sidebar { width: 290px; flex-shrink: 0; border-right: 1px solid #edf0f4; display: flex; flex-direction: column; }
.conv-search { padding: 14px; border-bottom: 1px solid #edf0f4; }
.conv-list { flex: 1; overflow-y: auto; }
.conv-item {
  display: flex; align-items: center; gap: 12px; padding: 14px 16px;
  cursor: pointer; transition: all 0.2s; border-bottom: 1px solid #f5f6f8;
  &:hover { background: #f8f9fb; }
  &.active { background: #f0f5ff; border-left: 3px solid var(--primary-color); padding-left: 13px; }
}
.conv-body { flex: 1; min-width: 0; }
.conv-top, .conv-bottom { display: flex; justify-content: space-between; align-items: center; }
.conv-top { margin-bottom: 4px; }
.conv-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.conv-time { font-size: 11px; color: #b0b8c4; }
.conv-preview { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.conv-unread { background: #f56c6c; color: #fff; font-size: 10px; font-weight: 700; min-width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; border-radius: 9px; padding: 0 4px; flex-shrink: 0; }

/* 右侧聊天 */
.chat-main { flex: 1; display: flex; flex-direction: column; min-width: 0; background: #fafbfc; }
.chat-header {
  display: flex; align-items: center; gap: 12px; padding: 14px 20px;
  border-bottom: 1px solid #edf0f4; background: #fff;
}
.ch-info { .ch-name { font-size: 15px; font-weight: 600; color: var(--text-primary); } }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px 24px; }
.chat-msg { display: flex; align-items: flex-end; gap: 8px; margin-bottom: 18px; }
.msg-mine { flex-direction: row-reverse; }
.msg-other { flex-direction: row; }
.msg-avatar { flex-shrink: 0; }
.msg-bubble { max-width: 65%; padding: 12px 16px; border-radius: 18px; position: relative; }
.msg-mine .msg-bubble { background: linear-gradient(135deg, #409eff, #3373d9); color: #fff; border-bottom-right-radius: 4px; }
.msg-other .msg-bubble { background: #fff; color: var(--text-primary); border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.msg-text { font-size: 14px; line-height: 1.55; word-break: break-word; }
.chat-input-area { padding: 14px 20px; background: #fff; border-top: 1px solid #edf0f4; }
.chat-input-row { display: flex; gap: 10px; align-items: flex-end;
  .chat-textarea { flex: 1; :deep(.el-textarea__inner) { border-radius: 14px; background: #f8f9fb; border-color: #e8ecf1; } }
  .send-btn { flex-shrink: 0; width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
}
.chat-empty-state {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px;
  p { font-size: 16px; font-weight: 600; color: var(--text-primary); margin: 0; }
  span { font-size: 13px; color: #b0b8c4; }
}

/* ====== 空状态 ====== */
.empty-state {
  text-align: center; padding: 60px 20px;
  p { font-size: 15px; font-weight: 600; color: var(--text-primary); margin: 12px 0 4px; }
  span { font-size: 13px; color: #b0b8c4; }
}

@media (max-width: 768px) {
  .msg-layout { flex-direction: column; }
  .msg-sidebar { width: 100%; flex-direction: row; border-right: none; border-bottom: 1px solid #edf0f4; }
  .sidebar-nav { flex-direction: row; padding: 0 0 12px 12px; }
  .chat-layout { flex-direction: column; }
  .chat-sidebar { width: 100%; max-height: 180px; }
  .chat-main { min-height: 400px; }
}
</style>
