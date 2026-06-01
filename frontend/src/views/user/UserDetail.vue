<template>
  <div class="user-detail page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="profile-header">
            <el-avatar :size="80" :src="user.avatar">{{ user.nickname?.charAt(0) || 'U' }}</el-avatar>
            <h3>{{ user.nickname }}</h3>
            <CreditBadge :score="user.creditScore || 0" />
            <p v-if="user.city" class="city"><el-icon :size="14"><Location /></el-icon> {{ user.city }}</p>
            <p v-if="user.bio" class="bio">{{ user.bio }}</p>
          </div>
          <el-divider />
          <div class="actions">
            <el-button type="primary" @click="sendPrivateMsg">发私信</el-button>
            <el-button @click="initMutual">发起互助</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="TA的技能" name="posts">
            <el-empty v-if="userPosts.length === 0" description="暂无发布" />
            <el-row :gutter="12" v-else>
              <el-col :span="12" v-for="post in userPosts" :key="post.id">
                <el-card shadow="hover" class="post-card" @click="$router.push(`/skill/${post.id}`)">
                  <div class="post-title">{{ post.title }}</div>
                  <el-tag :type="post.type === 'teach' ? 'success' : 'primary'" size="small">
                    {{ post.type === 'teach' ? '能教' : '想学' }}
                  </el-tag>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="TA的评价" name="reviews">
            <el-empty v-if="reviews.length === 0" description="暂无评价" />
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <el-rate v-model="review.rating" disabled />
                <span class="review-time">{{ review.createdAt }}</span>
              </div>
              <p v-if="review.content">{{ review.content }}</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <!-- 发起互助对话框 -->
    <el-dialog v-model="mutualDialogVisible" title="发起互助请求" width="480px">
      <el-form :model="mutualForm" label-width="80px">
        <el-form-item label="互助类型">
          <el-select v-model="mutualForm.type">
            <el-option label="技能交换" value="skill" />
            <el-option label="物品共享" value="goods" />
            <el-option label="临时搭伴" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="留言">
          <el-input v-model="mutualForm.requestMessage" type="textarea" :rows="3" placeholder="简要说明互助内容" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mutualDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMutual" :loading="mutualSubmitting">发送请求</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { userApi, reviewApi, mutualApi, messageApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import CreditBadge from '@/components/CreditBadge.vue'

const route = useRoute()
const userStore = useUserStore()
const userId = route.params.id
const user = ref({})
const userPosts = ref([])
const reviews = ref([])
const activeTab = ref('posts')
const mutualDialogVisible = ref(false)
const mutualSubmitting = ref(false)
const mutualForm = ref({
  type: 'skill',
  requestMessage: '',
})

onMounted(async () => {
  try {
    const [userRes, postsRes, reviewsRes] = await Promise.all([
      userApi.getUserDetail(userId),
      userApi.getUserPosts(userId, 1, 20),
      userApi.getUserReviews(userId, 1, 20),
    ])
    user.value = userRes.data?.data || userRes.data || {}
    userPosts.value = postsRes.data?.records || postsRes.data?.data?.records || []
    reviews.value = reviewsRes.data?.records || reviewsRes.data?.data?.records || []
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  }
})

async function sendPrivateMsg() {
  try {
    const { value } = await ElMessageBox.prompt('请输入消息内容', '发送私信', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputType: 'textarea',
    })
    if (value) {
      await messageApi.sendMessage({ receiverId: Number(userId), content: value, type: 'text' })
      ElMessage.success('消息已发送')
    }
  } catch (e) { /* cancelled */ }
}

function initMutual() {
  mutualForm.value = { type: 'skill', requestMessage: '' }
  mutualDialogVisible.value = true
}

async function submitMutual() {
  if (!mutualForm.value.requestMessage.trim()) {
    ElMessage.warning('请填写留言')
    return
  }
  mutualSubmitting.value = true
  try {
    await mutualApi.createRequest({
      participantId: Number(userId),
      type: mutualForm.value.type,
      relatedId: null,
      requestMessage: mutualForm.value.requestMessage,
    })
    ElMessage.success('互助请求已发送')
    mutualDialogVisible.value = false
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    mutualSubmitting.value = false
  }
}
</script>

<style scoped>
.profile-header {
  text-align: center;
  padding: 20px 0;
}

.profile-header h3 {
  margin: 12px 0 8px;
  font-size: 20px;
}

.city {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin: 8px 0;
}

.bio {
  text-align: left;
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin-top: 12px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.post-card {
  cursor: pointer;
  margin-bottom: 12px;
}

.post-title {
  font-weight: 500;
  margin-bottom: 8px;
}

.review-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.review-time {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
