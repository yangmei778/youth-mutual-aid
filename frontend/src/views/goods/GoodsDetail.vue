<template>
  <div class="goods-detail page-container" v-loading="loading">
    <template v-if="goods">
      <!-- 图片轮播 -->
      <el-card class="image-card" shadow="never" v-if="imageList.length">
        <el-carousel v-if="imageList.length > 1" height="400px" indicator-position="outside">
          <el-carousel-item v-for="(img, i) in imageList" :key="i">
            <img :src="img" alt="" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>
        <img v-else :src="imageList[0]" alt="" class="single-img" />
      </el-card>

      <el-row :gutter="20">
        <!-- 物品信息 -->
        <el-col :span="16">
          <el-card class="info-card" shadow="never">
            <h2 class="goods-title">{{ goods.title }}</h2>

            <div class="info-row">
              <el-tag :type="tagType(goods.exchangeType)" size="large">
                {{ tagLabel(goods.exchangeType) }}
              </el-tag>
              <el-tag type="info" size="large">{{ goods.category }}</el-tag>
            </div>

            <el-divider />

            <div class="info-section">
              <h3>物品描述</h3>
              <p class="description">{{ goods.description }}</p>
            </div>

            <div class="info-section">
              <h3>新旧程度</h3>
              <div class="condition-display">
                <el-progress
                  :percentage="goods.conditionLevel * 10"
                  :stroke-width="18"
                  :text-inside="true"
                  :format="() => `${goods.conditionLevel} / 10`"
                />
              </div>
            </div>

            <div class="info-section" v-if="goods.exchangeType === 'exchange' && goods.expectedItems">
              <h3>期望交换物品</h3>
              <p>{{ goods.expectedItems }}</p>
            </div>

            <div class="info-section" v-if="goods.price != null && goods.price > 0">
              <h3>预期价格</h3>
              <p class="price-text">¥ {{ goods.price.toFixed(2) }}</p>
            </div>

            <div class="info-section" v-if="goods.exchangeType === 'borrow' && goods.borrowDays">
              <h3>可借用天数</h3>
              <p>{{ goods.borrowDays }} 天</p>
            </div>

            <div class="info-section">
              <span class="view-count">
                <el-icon><View /></el-icon>
                {{ goods.viewCount || 0 }} 次浏览
              </span>
            </div>
          </el-card>
        </el-col>

        <!-- 发布者信息 & 操作 -->
        <el-col :span="8">
          <el-card class="publisher-card" shadow="never">
            <div class="publisher-info">
              <el-avatar :size="56" :src="goods.userAvatar">
                {{ goods.userNickname?.charAt(0) || '?' }}
              </el-avatar>
              <div class="publisher-detail">
                <router-link
                  :to="`/user/${goods.userId}`"
                  class="publisher-name"
                >
                  {{ goods.userNickname || '匿名用户' }}
                </router-link>
                <el-tag v-if="goods.userCreditLevel" type="warning" size="small" class="credit-badge">
                  信用: {{ goods.userCreditLevel }}
                </el-tag>
              </div>
            </div>

            <el-divider />

            <div class="action-buttons">
              <!-- 发布者不能申请自己的物品 -->
              <el-button
                v-if="goods.userId !== userStore.userInfo?.id"
                type="primary"
                size="large"
                @click="openRequestDialog"
                style="width: 100%"
              >
                {{ requestButtonText }}
              </el-button>
              <el-button
                v-if="goods.userId !== userStore.userInfo?.id"
                size="large"
                @click="router.push(`/message?chat=${goods.userId}`)"
                style="width: 100%"
              >
                <el-icon><ChatDotRound /></el-icon> 联系TA
              </el-button>
              <template v-if="goods.userId === userStore.userInfo?.id">
                <el-tag type="info" size="large" effect="plain" style="width:100%;text-align:center;justify-content:center;">
                  这是我发布的物品
                </el-tag>
                <el-button size="large" type="danger" plain style="width:100%" @click="handleOffline" :loading="offlining">
                  下架物品
                </el-button>
                <el-button size="large" type="danger" style="width:100%" @click="handleDelete" :loading="deleting">
                  删除物品
                </el-button>
              </template>
              <el-button size="large" @click="$router.push('/goods')" style="width: 100%">
                返回列表
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 申请对话框 -->
      <el-dialog v-model="requestDialogVisible" :title="requestButtonText" width="500px" destroy-on-close class="request-dialog">
        <!-- 物品摘要 -->
        <div class="req-summary" v-if="goods">
          <div class="req-item-preview">
            <img v-if="goods.images" :src="getFirstImg(goods.images)" class="req-thumb" />
            <div v-else class="req-thumb-placeholder"><el-icon :size="24"><Box /></el-icon></div>
            <div>
              <strong>{{ goods.title }}</strong>
              <span v-if="goods.price>0" class="req-price">¥ {{ goods.price }}</span>
              <span class="req-type">{{ tagLabel(goods.exchangeType) }}</span>
            </div>
          </div>
        </div>
        <el-form ref="requestFormRef" :model="requestForm" :rules="requestRules" label-position="top">
          <el-form-item label="申请留言" prop="requestMessage">
            <el-input
              v-model="requestForm.requestMessage"
              type="textarea"
              placeholder="介绍一下你自己，说明申请理由、使用时长、联系方式等..."
              :rows="5"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="requestDialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="submitRequest" :loading="requesting" size="large">发送申请</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { goodsApi, mutualApi, reportApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Box } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const goods = ref(null)
const loading = ref(false)

const requestDialogVisible = ref(false)
const requesting = ref(false)
const requestFormRef = ref(null)

const requestForm = reactive({
  requestMessage: '',
})

const requestRules = {
  requestMessage: [
    { required: true, message: '请输入申请留言', trigger: 'blur' },
  ],
}

const requestButtonText = computed(() => {
  if (!goods.value) return '申请'
  const map = { borrow: '申请借用', exchange: '申请交换', gift: '申请领取' }
  return map[goods.value.exchangeType] || '申请'
})

/** 后端 images 是逗号分隔字符串，解析为数组用于展示 */
const imageList = computed(() => {
  if (!goods.value?.images) return []
  if (Array.isArray(goods.value.images)) return goods.value.images
  return goods.value.images.split(',').filter(Boolean)
})

function tagType(type) {
  const map = { borrow: '', gift: 'success', exchange: 'warning' }
  return map[type] || 'info'
}

function tagLabel(type) {
  const map = { borrow: '借用', gift: '赠送', exchange: '交换' }
  return map[type] || type
}
function getFirstImg(imgs) {
  if (!imgs) return ''
  if (Array.isArray(imgs)) return imgs[0]
  return imgs.split(',')[0] || ''
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await goodsApi.getDetail(route.params.id)
    goods.value = res.data
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

function openRequestDialog() {
  requestForm.requestMessage = ''
  requestDialogVisible.value = true
}

async function submitRequest() {
  await requestFormRef.value?.validate()
  requesting.value = true
  try {
    await mutualApi.createRequest({
      type: 'goods',
      participantId: goods.value.userId,
      relatedId: goods.value.id,
      requestMessage: requestForm.requestMessage,
    })
    ElMessage.success('申请已发送')
    requestDialogVisible.value = false
  } catch {
    // 错误已在拦截器中处理
  } finally {
    requesting.value = false
  }
}

const offlining = ref(false)
async function handleOffline() {
  try { await ElMessageBox.confirm('确定要下架这个物品吗？下架后其他用户将无法看到。', '确认下架', { type: 'warning' }) }
  catch { return }
  offlining.value = true
  try {
    await goodsApi.offline(goods.value.id)
    ElMessage.success('物品已下架')
    router.push('/goods')
  } catch { /* error handled by interceptor */ }
  finally { offlining.value = false }
}

const deleting = ref(false)
async function handleDelete() {
  try { await ElMessageBox.confirm('确定要永久删除这个物品吗？此操作不可恢复。', '确认删除', { type: 'error', confirmButtonText: '删除' }) }
  catch { return }
  deleting.value = true
  try { await goodsApi.deleteGoods(goods.value.id); ElMessage.success('已删除'); router.push('/goods') }
  catch { /* error handled */ } finally { deleting.value = false }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.image-card {
  margin-bottom: 20px;

  :deep(.el-card__body) {
    padding: 0;
  }
}

.carousel-img,
.single-img {
  width: 100%;
  max-height: 400px;
  object-fit: contain;
  display: block;
}

.single-img {
  border-radius: 4px;
}

.info-card {
  .goods-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 12px;
  }
}

.info-row {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
}

.info-section {
  margin-bottom: 20px;

  h3 {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  p {
    color: var(--text-regular);
    line-height: 1.8;
  }

  .description {
    white-space: pre-wrap;
  }

  .view-count {
    display: flex;
    align-items: center;
    gap: 4px;
    color: var(--text-secondary);
    font-size: 14px;
  }
}

.condition-display {
  max-width: 400px;
}

.publisher-card {
  .publisher-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .publisher-detail {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .publisher-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--primary-color, #409eff);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  .credit-badge {
    width: fit-content;
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ====== 申请对话框 ====== */
.req-summary {
  background: #f8f9fb; border-radius: 14px; padding: 16px; margin-bottom: 20px;
}
.req-item-preview {
  display: flex; align-items: center; gap: 14px;
  strong { font-size: 15px; color: var(--text-primary); display: block; }
}
.req-thumb {
  width: 56px; height: 56px; object-fit: cover; border-radius: 10px; flex-shrink: 0;
}
.req-thumb-placeholder {
  width: 56px; height: 56px; display: flex; align-items: center; justify-content: center;
  background: #edf0f4; border-radius: 10px; flex-shrink: 0; color: #b0b8c4;
}
.req-price {
  font-size: 16px; font-weight: 800; color: #f56c6c; margin-right: 8px;
}
.req-type {
  font-size: 12px; color: #909399; background: #edf0f4; padding: 1px 8px; border-radius: 4px;
}

.price-text { font-size: 24px; font-weight: 800; color: #f56c6c; }
</style>
