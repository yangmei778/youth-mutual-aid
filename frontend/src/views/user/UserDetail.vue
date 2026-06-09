<template>
  <div class="user-detail page-container">
    <!-- 顶部卡片 -->
    <div class="ud-hero">
      <div class="udh-bg"></div>
      <div class="udh-inner">
        <div class="udh-left">
          <el-avatar :size="72" :src="user.avatar" class="udh-avatar">{{ user.nickname?.charAt(0) || 'U' }}</el-avatar>
          <div>
            <h2>{{ user.nickname || '用户' }}</h2>
            <div class="udh-meta">
              <span v-if="user.city"><el-icon :size="14"><Location /></el-icon> {{ user.city }}</span>
              <span v-if="user.age">{{ user.age }}岁</span>
            </div>
          </div>
        </div>
        <div class="udh-right">
          <CreditBadge :score="user.creditScore || 0" />
          <div class="udh-actions">
            <el-button type="primary" round @click="sendPrivateMsg"><el-icon :size="16"><ChatDotRound /></el-icon> 发私信</el-button>
            <el-button round @click="initMutual">发起互助</el-button>
          </div>
        </div>
      </div>
      <p v-if="user.bio" class="udh-bio">{{ user.bio }}</p>
    </div>

    <!-- 内容区 -->
    <div class="ud-body">
      <div class="ud-main">
        <div class="ud-card">
          <div class="udc-head">TA的技能 <span class="count-badge">{{ userPosts.length }}</span></div>
          <div class="posts-grid" v-if="userPosts.length">
            <div v-for="post in userPosts" :key="post.id" class="post-card" @click="$router.push(`/skill/${post.id}`)">
              <span class="pc-type" :class="post.type==='teach'?'type-teach':'type-learn'">{{ post.type==='teach'?'能教':'想学' }}</span>
              <h4>{{ post.title }}</h4>
            </div>
          </div>
          <div v-else class="empty-sm">TA还没有发布技能</div>
        </div>

        <div class="ud-card">
          <div class="udc-head">TA的评价 <span class="count-badge">{{ reviews.length }}</span></div>
          <div class="review-list" v-if="reviews.length">
            <div v-for="r in reviews" :key="r.id" class="review-item">
              <div class="ri-top">
                <el-rate v-model="r.rating" disabled size="small" />
                <span class="ri-time">{{ r.createdAt }}</span>
              </div>
              <p v-if="r.content">{{ r.content }}</p>
            </div>
          </div>
          <div v-else class="empty-sm">暂无评价</div>
        </div>
      </div>
    </div>

    <!-- 互助对话框 -->
    <el-dialog v-model="mutualDialogVisible" title="发起互助请求" width="460px" destroy-on-close>
      <el-form :model="mutualForm" label-position="top">
        <el-form-item label="互助类型">
          <el-select v-model="mutualForm.type" style="width:100%">
            <el-option label="技能交换" value="skill" />
            <el-option label="物品共享" value="goods" />
            <el-option label="临时搭伴" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="留言">
          <el-input v-model="mutualForm.requestMessage" type="textarea" :rows="4" placeholder="简要说明互助内容..." maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mutualDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="mutualSubmitting" @click="submitMutual">发送请求</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { userApi, mutualApi, messageApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, ChatDotRound } from '@element-plus/icons-vue'
import CreditBadge from '@/components/CreditBadge.vue'

const route = useRoute()
const userStore = useUserStore()
const userId = route.params.id
const user = ref({}); const userPosts = ref([]); const reviews = ref([])
const activeTab = ref('posts')
const mutualDialogVisible = ref(false); const mutualSubmitting = ref(false)
const mutualForm = ref({ type: 'skill', requestMessage: '' })

onMounted(async () => {
  try {
    const [ur, pr, rr] = await Promise.all([
      userApi.getUserDetail(userId), userApi.getUserPosts(userId,1,20), userApi.getUserReviews(userId,1,20),
    ])
    user.value = ur.data?.data || ur.data || {}
    userPosts.value = pr.data?.records || pr.data?.data?.records || []
    reviews.value = rr.data?.records || rr.data?.data?.records || []
  } catch {}
})

async function sendPrivateMsg() {
  try {
    const { value } = await ElMessageBox.prompt('请输入消息内容', '发私信', { confirmButtonText:'发送', cancelButtonText:'取消', inputType:'textarea' })
    if (value) { await messageApi.sendMessage({ receiverId:Number(userId), content:value }); ElMessage.success('已发送') }
  } catch {}
}

function initMutual() { mutualForm.value = { type:'skill', requestMessage:'' }; mutualDialogVisible.value = true }
async function submitMutual() {
  if (!mutualForm.value.requestMessage.trim()) { ElMessage.warning('请填写留言'); return }
  mutualSubmitting.value = true
  try { await mutualApi.createRequest({ participantId:Number(userId), type:mutualForm.value.type, relatedId:null, requestMessage:mutualForm.value.requestMessage }); ElMessage.success('已发送'); mutualDialogVisible.value=false }
  catch {} finally { mutualSubmitting.value = false }
}
</script>

<style scoped>
.user-detail { max-width: 800px; }

.ud-hero {
  position: relative; border-radius: 22px; padding: 32px 32px 24px;
  margin-bottom: 24px; background: #fff; border: 1px solid #edf0f4; overflow: hidden;
}
.udh-bg { position: absolute; top: 0; left: 0; right: 0; height: 80px; background: linear-gradient(135deg, #f0f5ff, #f8f9fd, #fef9f0); border-radius: 22px 22px 0 0; }
.udh-inner { position: relative; z-index: 1; display: flex; justify-content: space-between; align-items: center; }
.udh-left { display: flex; align-items: center; gap: 18px; }
.udh-avatar { border: 4px solid #fff; box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.udh-left h2 { font-size: 20px; font-weight: 800; margin: 0; }
.udh-meta { display: flex; gap: 16px; margin-top: 4px; font-size: 13px; color: #909399; span { display: flex; align-items: center; gap: 4px; } }
.udh-right { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; position: relative; z-index: 1; }
.udh-actions { display: flex; gap: 8px; }
.udh-bio { position: relative; z-index: 1; margin: 16px 0 0; font-size: 14px; color: #606266; line-height: 1.6; }

.ud-body { display: flex; gap: 24px; }
.ud-main { flex: 1; }

.ud-card { background: #fff; border: 1px solid #edf0f4; border-radius: 18px; padding: 24px; margin-bottom: 20px; }
.udc-head { font-size: 16px; font-weight: 700; padding-bottom: 14px; border-bottom: 2px solid #f5f6f8; margin-bottom: 16px; }
.count-badge { font-size: 12px; color: #909399; background: #f5f6f8; padding: 2px 8px; border-radius: 8px; margin-left: 6px; font-weight: 400; }

.posts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.post-card { padding: 16px; border: 1px solid #edf0f4; border-radius: 14px; cursor: pointer; transition: all 0.2s;
  &:hover { border-color: rgba(64,158,255,0.2); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.04); }
  h4 { font-size: 14px; font-weight: 600; margin: 8px 0 0; }
}
.pc-type { display: inline-block; padding: 2px 8px; border-radius: 5px; font-size: 11px; font-weight: 700;
  &.type-teach { background: rgba(103,194,58,0.07); color: #52b818; }
  &.type-learn { background: rgba(64,158,255,0.07); color: #409eff; }
}

.review-list { display: flex; flex-direction: column; }
.review-item { padding: 14px 0; &+& { border-top: 1px solid #f5f6f8; } p { margin: 6px 0 0; font-size: 14px; color: #606266; line-height: 1.6; } }
.ri-top { display: flex; justify-content: space-between; align-items: center; }
.ri-time { font-size: 12px; color: #b0b8c4; }

.empty-sm { text-align: center; padding: 32px 0; font-size: 13px; color: #b0b8c4; }

@media (max-width: 600px) { .udh-inner { flex-direction: column; gap: 16px; } .udh-right { flex-direction: row; } }
</style>
