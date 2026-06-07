<template>
  <div class="admin-page">
    <div class="ap-hero">
      <div class="aph-left">
        <h2>内容审核</h2>
        <p>管理技能、物品、活动内容</p>
      </div>
    </div>

    <div class="ap-card">
      <!-- 搜索栏 -->
      <div class="audit-search">
        <el-input v-model="searchKeyword" placeholder="搜索标题或描述..." clearable :prefix-icon="Search" style="width:280px" @keyup.enter="fetchAll" @clear="fetchAll" />
        <span class="audit-count">共 {{ totalCount }} 条</span>
      </div>
      <el-tabs v-model="activeTab" class="modern-tabs" @tab-change="fetchAll">
        <el-tab-pane label="技能" name="skill">
          <el-table :data="skillList" v-loading="skillLoading" stripe class="modern-table">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column label="发布者" width="120">
              <template #default="{ row }"><span class="pub-name">{{ row.nickname || row.userName || '-' }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small" effect="dark" round>{{ row.status===1?'正常':'已下架' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status===1">
                  <el-button type="danger" size="small" plain @click="handleOffline('skill',row.id)">下架</el-button>
                  <el-button type="danger" size="small" @click="handleDelete('skill',row.id)">删除</el-button>
                </template>
                <span v-else class="handled-text">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="物品" name="goods">
          <el-table :data="goodsList" v-loading="goodsLoading" stripe class="modern-table">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column label="发布者" width="120">
              <template #default="{ row }"><span class="pub-name">{{ row.nickname || row.userName || '-' }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small" effect="dark" round>{{ row.status===1?'正常':'已下架' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status===1">
                  <el-button type="danger" size="small" plain @click="handleOffline('goods',row.id)">下架</el-button>
                  <el-button type="danger" size="small" @click="handleDelete('goods',row.id)">删除</el-button>
                </template>
                <span v-else class="handled-text">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="活动" name="activity">
          <el-table :data="activityList" v-loading="activityLoading" stripe class="modern-table">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column label="发布者" width="120">
              <template #default="{ row }"><span class="pub-name">{{ row.nickname || row.userName || '-' }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small" effect="dark" round>{{ row.status===1?'正常':'已下架' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status===1">
                  <el-button type="danger" size="small" plain @click="handleOffline('activity',row.id)">下架</el-button>
                  <el-button type="danger" size="small" @click="handleDelete('activity',row.id)">删除</el-button>
                </template>
                <span v-else class="handled-text">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const activeTab = ref('skill')
const searchKeyword = ref('')
const skillList = ref([]); const goodsList = ref([]); const activityList = ref([])
const skillLoading = ref(false); const goodsLoading = ref(false); const activityLoading = ref(false)
const skillTotal = ref(0); const goodsTotal = ref(0); const activityTotal = ref(0)
const totalCount = computed(() => skillTotal.value + goodsTotal.value + activityTotal.value)

function fetchAll() { fetchData(activeTab.value) }
async function fetchData(type) {
  const params = { pageNum:1, pageSize:100 }
  if (searchKeyword.value) params.keyword = searchKeyword.value
  if (type === 'skill') { skillLoading.value = true; try { const r = await adminApi.getSkills(params); skillList.value = r.data?.records || r.data?.data?.records || []; skillTotal.value = r.data?.total || r.data?.data?.total || 0 } catch {} finally { skillLoading.value = false } }
  if (type === 'goods') { goodsLoading.value = true; try { const r = await adminApi.getGoods(params); goodsList.value = r.data?.records || r.data?.data?.records || []; goodsTotal.value = r.data?.total || r.data?.data?.total || 0 } catch {} finally { goodsLoading.value = false } }
  if (type === 'activity') { activityLoading.value = true; try { const r = await adminApi.getActivities(params); activityList.value = r.data?.records || r.data?.data?.records || []; activityTotal.value = r.data?.total || r.data?.data?.total || 0 } catch {} finally { activityLoading.value = false } }
}
async function handleOffline(type, id) {
  try { await ElMessageBox.confirm('确定下架此内容？', '确认', { type: 'warning' }) } catch { return }
  try {
    if (type === 'skill') await adminApi.offlineSkill(id)
    if (type === 'goods') await adminApi.offlineGoods(id)
    if (type === 'activity') await adminApi.offlineActivity(id)
    ElMessage.success('已下架'); fetchData(type)
  } catch {}
}
async function handleDelete(type, id) {
  try { await ElMessageBox.confirm('确定永久删除此内容？不可恢复。', '确认删除', { type: 'error', confirmButtonText: '删除' }) } catch { return }
  try {
    if (type === 'skill') await adminApi.deleteSkill(id)
    if (type === 'goods') await adminApi.deleteGoods(id)
    if (type === 'activity') await adminApi.deleteActivity(id)
    ElMessage.success('已删除'); fetchData(type)
  } catch {}
}
onMounted(() => { fetchData('skill'); fetchData('goods'); fetchData('activity') })
</script>

<style scoped>
.admin-page { max-width: 100%; }
.ap-hero { margin-bottom: 20px; h2 { font-size: 22px; font-weight: 800; margin: 0; } p { font-size: 14px; color: #909399; margin: 4px 0 0; } }
.ap-card { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 20px 24px; }
.modern-tabs { :deep(.el-tabs__header) { margin-bottom: 12px; } }
.modern-table {
  :deep(th) { font-weight: 700; color: var(--text-primary); background: #f8f9fb; border: none; }
  :deep(td) { border-color: #f5f6f8; }
}
.pub-name { font-size: 13px; font-weight: 500; }
.handled-text { font-size: 13px; color: #b0b8c4; }
.audit-search { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.audit-count { font-size: 13px; color: #909399; }
</style>
