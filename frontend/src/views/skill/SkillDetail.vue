<template>
  <div class="skill-detail page-container">
    <div v-loading="loading" class="detail-content">
      <el-empty v-if="!loading && !skill" description="技能信息不存在" />

      <template v-if="skill">
        <div class="page-header">
          <el-button text @click="router.push('/skill')">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>

        <el-row :gutter="20">
          <!-- 左侧：技能信息 -->
          <el-col :xs="24" :sm="16" :md="16">
            <el-card shadow="never" class="info-card">
              <template #header>
                <div class="card-title-row">
                  <h2>{{ skill.title }}</h2>
                  <div class="title-tags">
                    <el-tag :type="skill.type === 'teach' ? 'success' : 'primary'">
                      {{ skill.type === 'teach' ? '能教' : '想学' }}
                    </el-tag>
                    <el-tag type="info" effect="plain">{{ skill.category }}</el-tag>
                  </div>
                </div>
              </template>

              <div class="info-section">
                <div class="info-row">
                  <span class="info-label">技能描述</span>
                  <div class="info-content description-text">{{ skill.description }}</div>
                </div>

                <el-divider />

                <el-row :gutter="24">
                  <el-col :span="8">
                    <div class="info-item">
                      <span class="info-label">可用时间</span>
                      <span class="info-value">{{ skill.availableTime || '未指定' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="info-item">
                      <span class="info-label">期望地点</span>
                      <span class="info-value">{{ skill.preferredLocation || '未指定' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="info-item">
                      <span class="info-label">支持线上</span>
                      <span class="info-value">
                        <el-tag :type="skill.onlineSupport ? 'success' : 'info'" size="small">
                          {{ skill.onlineSupport ? '是' : '否' }}
                        </el-tag>
                      </span>
                    </div>
                  </el-col>
                </el-row>

                <el-divider />

                <div class="info-row">
                  <span class="info-label">浏览次数</span>
                  <span class="info-value">
                    <el-icon><View /></el-icon>
                    {{ skill.viewCount || 0 }}
                  </span>
                </div>
              </div>

              <div class="action-bar">
                <el-button type="primary" size="large" @click="showRequestDialog = true">
                  发起交换请求
                </el-button>
                <el-button size="large" @click="router.push('/skill')">
                  返回列表
                </el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：发布者信息 -->
          <el-col :xs="24" :sm="8" :md="8">
            <el-card shadow="never" class="publisher-card">
              <template #header>
                <span>发布者</span>
              </template>

              <div class="publisher-info">
                <el-avatar :size="64" :src="skill.avatar">
                  {{ skill.nickname?.charAt(0) || '?' }}
                </el-avatar>
                <div class="publisher-detail">
                  <router-link :to="`/user/${skill.userId}`" class="publisher-name">
                    {{ skill.nickname || '匿名用户' }}
                  </router-link>
                  <el-tag v-if="skill.creditScore" type="warning" size="small" effect="plain" class="credit-tag">
                    <el-icon><Star /></el-icon>
                    信用 {{ skill.creditScore }}
                  </el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
    </div>

    <!-- 交换请求对话框 -->
    <el-dialog
      v-model="showRequestDialog"
      title="发起交换请求"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="requestFormRef" :model="requestForm" :rules="requestRules">
        <el-form-item label="留言" prop="requestMessage">
          <el-input
            v-model="requestForm.requestMessage"
            type="textarea"
            placeholder="介绍一下你自己，说明你想如何交换..."
            :rows="4"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showRequestDialog = false">取消</el-button>
        <el-button type="primary" :loading="requesting" @click="handleSendRequest">
          发送请求
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { skillApi, mutualApi } from '@/api'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Star } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const skill = ref(null)

const showRequestDialog = ref(false)
const requesting = ref(false)
const requestFormRef = ref(null)

const requestForm = reactive({
  requestMessage: '',
})

const requestRules = {
  requestMessage: [
    { required: true, message: '请输入留言', trigger: 'blur' },
    { min: 5, max: 300, message: '留言长度5-300个字符', trigger: 'blur' },
  ],
}

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await skillApi.getDetail(id)
    skill.value = res.data
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

async function handleSendRequest() {
  await requestFormRef.value?.validate()
  requesting.value = true
  try {
    await mutualApi.createRequest({
      type: 'skill',
      participantId: skill.value.userId,
      relatedId: skill.value.id,
      requestMessage: requestForm.requestMessage,
    })
    ElMessage.success('交换请求已发送')
    showRequestDialog.value = false
    requestForm.requestMessage = ''
  } catch {
    // 错误已在拦截器中处理
  } finally {
    requesting.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style lang="scss" scoped>
.skill-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.info-card {
  .card-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;

    h2 {
      font-size: 22px;
      color: var(--text-primary);
      margin: 0;
    }

    .title-tags {
      display: flex;
      gap: 8px;
    }
  }
}

.info-section {
  .info-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 4px;
    display: block;
  }

  .info-value {
    color: var(--text-primary);
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .description-text {
    color: var(--text-primary);
    line-height: 1.8;
    white-space: pre-wrap;
  }

  .info-row {
    margin-bottom: 16px;
  }

  .info-item {
    .info-value {
      margin-top: 4px;
    }
  }
}

.action-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.publisher-card {
  .publisher-info {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .publisher-detail {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .publisher-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    text-decoration: none;

    &:hover {
      color: var(--el-color-primary);
    }
  }

  .credit-tag {
    width: fit-content;
  }
}
</style>
