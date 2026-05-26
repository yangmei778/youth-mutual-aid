<template>
  <div class="activity-list page-container">
    <h2>🎉 临时搭伴</h2>
    <p>拼饭、拼展、拼旅行，一个人也能找到有趣的搭伴</p>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-input v-model="searchKeyword" placeholder="搜索活动" clearable :prefix-icon="Search" @keyup.enter="loadList" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterType" placeholder="活动类型" clearable @change="loadList">
            <el-option label="拼饭" value="meal" />
            <el-option label="拼展" value="exhibition" />
            <el-option label="拼旅行" value="travel" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-input v-model="filterCity" placeholder="城市" clearable @keyup.enter="loadList" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterStatus" placeholder="状态" clearable @change="loadList">
            <el-option label="报名中" value="open" />
            <el-option label="已满员" value="full" />
            <el-option label="进行中" value="ongoing" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="loadList">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 活动卡片列表 -->
    <div v-loading="loading" class="activity-cards">
      <el-empty v-if="!loading && activities.length === 0" description="暂无活动" />

      <el-card
        v-for="item in activities"
        :key="item.id"
        class="activity-card"
        shadow="hover"
        @click="goDetail(item.id)"
      >
        <div class="card-body">
          <div class="card-left">
            <span class="type-icon">{{ typeIconMap[item.type] || '🤝' }}</span>
          </div>
          <div class="card-main">
            <div class="card-title-row">
              <span class="card-title">{{ item.title }}</span>
              <el-tag :type="statusTagType(item.status)" size="small">{{ statusLabel(item.status) }}</el-tag>
            </div>
            <div class="card-info">
              <span class="info-item">🕐 {{ formatDate(item.activityTime) }}</span>
              <span class="info-item">📍 {{ item.location }}</span>
              <span class="info-item">👥 {{ item.currentMembers }}/{{ item.maxMembers }}</span>
              <span class="info-item">💰 {{ item.costDesc || '免费' }}</span>
            </div>
            <div class="card-footer">
              <span class="publisher">发布者：{{ item.publisher?.nickname || '匿名' }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>

    <!-- 发布活动浮动按钮 -->
    <router-link to="/activity/publish">
      <el-button class="publish-btn" type="primary" size="large" circle>
        <el-icon :size="24"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus } from '@element-plus/icons-vue'
import { activityApi } from '@/api'

const router = useRouter()

const typeIconMap = {
  meal: '🍜',
  exhibition: '🎨',
  travel: '✈️',
  other: '🤝',
}

const statusTagType = (status) => {
  const map = { open: 'success', full: 'warning', ongoing: 'primary' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { open: '报名中', full: '已满员', ongoing: '进行中' }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

const searchKeyword = ref('')
const filterType = ref('')
const filterCity = ref('')
const filterStatus = ref('')

const activities = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterType.value) params.type = filterType.value
    if (filterCity.value) params.city = filterCity.value
    if (filterStatus.value) params.status = filterStatus.value

    const res = await activityApi.getList(params)
    activities.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    activities.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  router.push(`/activity/${id}`)
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.activity-list {
  h2 {
    font-size: 24px;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  p {
    color: var(--text-secondary);
    margin-bottom: 20px;
  }
}

.filter-card {
  margin-bottom: 20px;

  .el-select,
  .el-input {
    width: 100%;
  }
}

.activity-cards {
  min-height: 200px;
}

.activity-card {
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateX(4px);
  }

  .card-body {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }

  .card-left {
    flex-shrink: 0;

    .type-icon {
      font-size: 36px;
      line-height: 1;
    }
  }

  .card-main {
    flex: 1;
    min-width: 0;
  }

  .card-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    margin-right: 12px;
  }

  .card-info {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 8px;

    .info-item {
      font-size: 13px;
      color: var(--text-regular);
    }
  }

  .card-footer {
    .publisher {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.publish-btn {
  position: fixed;
  right: 40px;
  bottom: 80px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  z-index: 100;
}
</style>
