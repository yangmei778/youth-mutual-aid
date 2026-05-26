<template>
  <div class="goods-list page-container">
    <div class="page-header">
      <h2>闲置共享</h2>
      <p>让闲置物品发挥价值，借用、交换、赠送</p>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-input v-model="filters.keyword" placeholder="搜索物品名称..." clearable @keyup.enter="loadList">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filters.category" placeholder="物品分类" clearable @change="loadList">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filters.exchangeType" placeholder="交换方式" clearable @change="loadList">
            <el-option label="借用" value="borrow" />
            <el-option label="赠送" value="gift" />
            <el-option label="交换" value="exchange" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-input v-model="filters.city" placeholder="所在城市" clearable @keyup.enter="loadList" />
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="primary" @click="loadList">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 物品卡片列表 -->
    <el-row :gutter="16" class="goods-grid" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="6" v-for="item in goodsList" :key="item.id">
        <el-card class="goods-card" shadow="hover" @click="goDetail(item.id)">
          <div class="goods-image">
            <img v-if="item.images && item.images.length" :src="item.images[0]" alt="" />
            <div v-else class="image-placeholder">
              <el-icon :size="40"><Picture /></el-icon>
            </div>
            <el-tag
              class="exchange-tag"
              :type="tagType(item.exchangeType)"
              size="small"
            >
              {{ tagLabel(item.exchangeType) }}
            </el-tag>
          </div>
          <div class="goods-info">
            <h3 class="goods-title">{{ item.title }}</h3>
            <div class="goods-meta">
              <span class="condition-level">
                <el-rate v-model="item.conditionLevel" disabled :max="5" size="small" />
              </span>
              <span class="view-count">
                <el-icon><View /></el-icon>
                {{ item.viewCount || 0 }}
              </span>
            </div>
            <div class="goods-footer">
              <span class="publisher">{{ item.userNickname || '匿名用户' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && goodsList.length === 0" description="暂无物品" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="total"
        :page-sizes="[8, 12, 20]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>

    <!-- 发布按钮 -->
    <router-link to="/goods/publish">
      <el-button class="publish-fab" type="primary" circle size="large">
        <el-icon :size="24"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi } from '@/api'
import { Search, Picture, View, Plus } from '@element-plus/icons-vue'

const router = useRouter()

const categories = ['数码', '书籍', '家居', '服饰', '运动', '食品', '其他']

const filters = reactive({
  keyword: '',
  category: '',
  exchangeType: '',
  city: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 12,
})

const goodsList = ref([])
const total = ref(0)
const loading = ref(false)

function tagType(type) {
  const map = { borrow: '', gift: 'success', exchange: 'warning' }
  return map[type] || 'info'
}

function tagLabel(type) {
  const map = { borrow: '借用', gift: '赠送', exchange: '交换' }
  return map[type] || type
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filters,
    }
    // 去掉空值
    Object.keys(params).forEach(k => {
      if (!params[k]) delete params[k]
    })
    const res = await goodsApi.getList(params)
    goodsList.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.category = ''
  filters.exchangeType = ''
  filters.city = ''
  pagination.page = 1
  loadList()
}

function goDetail(id) {
  router.push(`/goods/${id}`)
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 24px;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  p {
    color: var(--text-secondary);
    font-size: 14px;
  }
}

.filter-card {
  margin-bottom: 20px;

  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.goods-grid {
  min-height: 200px;
}

.goods-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.goods-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;
  border-radius: 4px 4px 0 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #c0c4cc;
  }

  .exchange-tag {
    position: absolute;
    top: 8px;
    right: 8px;
  }
}

.goods-info {
  padding: 12px;
}

.goods-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;

  .view-count {
    display: flex;
    align-items: center;
    gap: 4px;
    color: var(--text-secondary);
    font-size: 12px;
  }
}

.goods-footer {
  .publisher {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.publish-fab {
  position: fixed;
  right: 40px;
  bottom: 80px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  z-index: 10;
}
</style>
