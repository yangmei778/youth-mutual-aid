<template>
  <div class="skill-list page-container">
    <div class="page-header">
      <h2>📚 技能交换</h2>
      <p>技能互换，共同成长</p>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索技能..."
            clearable
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="queryParams.type" placeholder="类型" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="我能教" value="teach" />
            <el-option label="我想学" value="learn" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="queryParams.category" placeholder="分类" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-input
            v-model="queryParams.city"
            placeholder="城市筛选"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-col>
        <el-col :span="2">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 技能卡片列表 -->
    <div v-loading="loading" class="skill-grid">
      <el-empty v-if="!loading && skillList.length === 0" description="暂无技能信息" />

      <el-row :gutter="16">
        <el-col v-for="skill in skillList" :key="skill.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card
            shadow="hover"
            class="skill-card"
            @click="goDetail(skill.id)"
          >
            <div class="skill-card-body">
              <div class="skill-card-top">
                <el-tag :type="skill.type === 'teach' ? 'success' : 'primary'" size="small">
                  {{ skill.type === 'teach' ? '能教' : '想学' }}
                </el-tag>
                <el-tag type="info" size="small" effect="plain">{{ skill.category }}</el-tag>
              </div>
              <h3 class="skill-title">{{ skill.title }}</h3>
              <div class="skill-meta">
                <span class="meta-item">
                  <el-icon><User /></el-icon>
                  {{ skill.nickname || '匿名用户' }}
                </span>
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ skill.viewCount || 0 }}
                </span>
              </div>
              <div class="skill-time">{{ formatTime(skill.createdAt) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[8, 12, 16, 24]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <!-- 浮动发布按钮 -->
    <router-link to="/skill/publish" class="publish-btn">
      <el-button type="primary" size="large" circle>
        <el-icon :size="24"><Plus /></el-icon>
      </el-button>
    </router-link>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { skillApi } from '@/api'
import { Search, User, View, Plus } from '@element-plus/icons-vue'

const router = useRouter()

const categories = ['编程', '语言', '音乐', '绘画', '健身', '烹饪', '摄影', '设计', '写作', '其他']

const loading = ref(false)
const skillList = ref([])
const total = ref(0)

const queryParams = reactive({
  keyword: '',
  type: '',
  category: '',
  city: '',
  page: 1,
  pageSize: 12,
})

async function fetchList() {
  loading.value = true
  try {
    const params = { ...queryParams }
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const res = await skillApi.getList(params)
    skillList.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchList()
}

function goDetail(id) {
  router.push(`/skill/${id}`)
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.skill-list {
  position: relative;
  min-height: 100%;
}

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
    padding: 16px;
  }

  .el-select,
  .el-input {
    width: 100%;
  }
}

.skill-grid {
  min-height: 200px;
}

.skill-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .skill-card-top {
    display: flex;
    gap: 8px;
    margin-bottom: 10px;
  }

  .skill-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .skill-meta {
    display: flex;
    gap: 16px;
    color: var(--text-secondary);
    font-size: 13px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .skill-time {
    margin-top: 10px;
    font-size: 12px;
    color: var(--text-secondary);
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-bottom: 60px;
}

.publish-btn {
  position: fixed;
  right: 40px;
  bottom: 80px;
  z-index: 10;
}
</style>
