<template>
  <div class="home-page page-container">
    <!-- Hero Section -->
    <div class="hero-section">
      <h1>🤝 欢迎来到互助青年</h1>
      <p class="subtitle">同城互助，让生活更美好</p>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="$router.push('/skill')">
          📚 技能交换
        </el-button>
        <el-button size="large" @click="$router.push('/goods')">
          🎁 闲置共享
        </el-button>
        <el-button size="large" @click="$router.push('/activity')">
          🎉 临时搭伴
        </el-button>
      </div>
    </div>

    <!-- Feature Cards -->
    <el-row :gutter="20" class="feature-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="feature-card" @click="$router.push('/skill')">
          <template #header>
            <div class="card-header">
              <span class="feature-icon">📚</span>
              <span>技能交换</span>
            </div>
          </template>
          <p>编程教学、健身指导、语言学习... 互相教学，共同成长</p>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="feature-card" @click="$router.push('/goods')">
          <template #header>
            <div class="card-header">
              <span class="feature-icon">🎁</span>
              <span>闲置共享</span>
            </div>
          </template>
          <p>让闲置物品发挥价值，借用、交换、赠送，循环利用</p>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="feature-card" @click="$router.push('/activity')">
          <template #header>
            <div class="card-header">
              <span class="feature-icon">🎉</span>
              <span>临时搭伴</span>
            </div>
          </template>
          <p>拼饭、拼展、拼旅行... 一个人也能找到有趣的搭伴</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- Latest Skills Section -->
    <div class="section" v-if="latestSkills.length">
      <div class="section-header">
        <h2>📚 最新技能</h2>
        <el-button text type="primary" @click="$router.push('/skill')">查看全部</el-button>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="skill in latestSkills" :key="skill.id">
          <el-card shadow="hover" class="item-card" @click="$router.push(`/skill/${skill.id}`)">
            <div class="item-title">{{ skill.title }}</div>
            <div class="item-meta">
              <el-tag :type="skill.type === 'teach' ? 'success' : 'primary'" size="small">
                {{ skill.type === 'teach' ? '能教' : '想学' }}
              </el-tag>
              <span v-if="skill.category" class="item-category">{{ skill.category }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Latest Activities Section -->
    <div class="section" v-if="latestActivities.length">
      <div class="section-header">
        <h2>🎉 最新活动</h2>
        <el-button text type="primary" @click="$router.push('/activity')">查看全部</el-button>
      </div>
      <el-row :gutter="16">
        <el-col :span="8" v-for="activity in latestActivities" :key="activity.id">
          <el-card shadow="hover" class="item-card" @click="$router.push(`/activity/${activity.id}`)">
            <div class="item-title">{{ activity.title }}</div>
            <div class="item-meta">
              <span>{{ typeIcons[activity.type] || '🤝' }} {{ activity.location }}</span>
              <span>{{ activity.currentMembers }}/{{ activity.maxMembers }}人</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { skillApi, activityApi } from '@/api'

const latestSkills = ref([])
const latestActivities = ref([])

const typeIcons = {
  meal: '🍜',
  exhibition: '🎨',
  travel: '✈️',
  other: '🤝',
}

onMounted(async () => {
  try {
    const [skillRes, activityRes] = await Promise.all([
      skillApi.getList({ pageNum: 1, pageSize: 4 }),
      activityApi.getList({ pageNum: 1, pageSize: 3 }),
    ])
    latestSkills.value = skillRes.data?.records || []
    latestActivities.value = activityRes.data?.records || []
  } catch (e) {
    // 静默处理，首页可以无数据
  }
})
</script>

<style lang="scss" scoped>
.home-page {
  h1 {
    font-size: 28px;
    color: var(--el-text-color-primary);
    margin-bottom: 8px;
  }

  .subtitle {
    color: var(--el-text-color-secondary);
    font-size: 16px;
    margin-bottom: 24px;
  }
}

.hero-section {
  text-align: center;
  padding: 40px 0 20px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 40px;
}

.feature-cards {
  margin-bottom: 40px;
}

.feature-card {
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-4px);
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
  }

  .feature-icon {
    font-size: 24px;
  }

  p {
    color: var(--el-text-color-regular);
    line-height: 1.6;
  }
}

.section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    font-size: 20px;
    color: var(--el-text-color-primary);
  }
}

.item-card {
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }
}

.item-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.item-category {
  margin-left: 8px;
}
</style>
