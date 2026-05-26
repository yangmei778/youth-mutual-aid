<template>
  <div class="content-audit page-container">
    <el-card shadow="hover">
      <template #header>
        <span>内容审核</span>
      </template>
      <el-tabs v-model="activeTab">
        <!-- 技能审核 -->
        <el-tab-pane label="技能" name="skill">
          <el-table :data="skillList" v-loading="skillLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="userName" label="发布者" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '正常' : '已下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'active'"
                  type="danger"
                  size="small"
                  @click="handleOffline('skill', row.id)"
                >下架</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!skillLoading && skillList.length === 0" description="暂无技能内容" />
        </el-tab-pane>

        <!-- 物品审核 -->
        <el-tab-pane label="物品" name="goods">
          <el-table :data="goodsList" v-loading="goodsLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="userName" label="发布者" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '正常' : '已下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'active'"
                  type="danger"
                  size="small"
                  @click="handleOffline('goods', row.id)"
                >下架</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!goodsLoading && goodsList.length === 0" description="暂无物品内容" />
        </el-tab-pane>

        <!-- 活动审核 -->
        <el-tab-pane label="活动" name="activity">
          <el-table :data="activityList" v-loading="activityLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="userName" label="发布者" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '正常' : '已下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'active'"
                  type="danger"
                  size="small"
                  @click="handleOffline('activity', row.id)"
                >下架</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!activityLoading && activityList.length === 0" description="暂无活动内容" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const activeTab = ref('skill')
const skillList = ref([])
const goodsList = ref([])
const activityList = ref([])
const skillLoading = ref(false)
const goodsLoading = ref(false)
const activityLoading = ref(false)

async function fetchSkills() {
  skillLoading.value = true
  try {
    const res = await adminApi.getSkills({ page: 1, size: 50 })
    skillList.value = res.data?.data?.records || res.data?.data || []
  } catch (e) {
    // ignore
  } finally {
    skillLoading.value = false
  }
}

async function fetchGoods() {
  goodsLoading.value = true
  try {
    const res = await adminApi.getGoods({ page: 1, size: 50 })
    goodsList.value = res.data?.data?.records || res.data?.data || []
  } catch (e) {
    // ignore
  } finally {
    goodsLoading.value = false
  }
}

async function fetchActivities() {
  activityLoading.value = true
  try {
    const res = await adminApi.getActivities({ page: 1, size: 50 })
    activityList.value = res.data?.data?.records || res.data?.data || []
  } catch (e) {
    // ignore
  } finally {
    activityLoading.value = false
  }
}

async function handleOffline(type, id) {
  try {
    await ElMessageBox.confirm('确定下架该内容？', '提示', { type: 'warning' })
    if (type === 'skill') {
      await adminApi.offlineSkill(id)
      fetchSkills()
    } else if (type === 'goods') {
      await adminApi.offlineGoods(id)
      fetchGoods()
    } else if (type === 'activity') {
      await adminApi.offlineActivity(id)
      fetchActivities()
    }
    ElMessage.success('下架成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

watch(activeTab, (val) => {
  if (val === 'skill' && skillList.value.length === 0) fetchSkills()
  if (val === 'goods' && goodsList.value.length === 0) fetchGoods()
  if (val === 'activity' && activityList.value.length === 0) fetchActivities()
})

onMounted(() => {
  fetchSkills()
})
</script>
