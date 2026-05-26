<template>
  <div class="user-detail page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="profile-header">
            <el-avatar :size="80" :src="user.avatar">{{ user.nickname?.charAt(0) || 'U' }}</el-avatar>
            <h3>{{ user.nickname }}</h3>
            <CreditBadge :score="user.creditScore || 0" />
            <p v-if="user.city" class="city">📍 {{ user.city }}</p>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { userApi, reviewApi, mutualApi, messageApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreditBadge from '@/components/CreditBadge.vue'

const route = useRoute()
const userId = route.params.id
const user = ref({})
const userPosts = ref([])
const reviews = ref([])
const activeTab = ref('posts')

onMounted(async () => {
  try {
    const [userRes, postsRes, reviewsRes] = await Promise.all([
      userApi.getUserDetail(userId),
      userApi.getUserPosts(userId, 1, 20),
      reviewApi.getReceivedReviews({ pageNum: 1, pageSize: 20 }),
    ])
    user.value = userRes.data || {}
    userPosts.value = postsRes.data?.records || []
    reviews.value = (reviewsRes.data?.records || []).filter(r => r.revieweeId == userId)
  } catch (e) { /* ignore */ }
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
  ElMessage.info('请到对应发布页面发起互助请求')
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
