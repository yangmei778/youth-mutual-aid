<template>
  <div class="admin-page">
    <div class="ap-hero">
      <div class="aph-left">
        <h2>用户管理</h2>
        <p>共 <strong>{{ total }}</strong> 名注册用户</p>
      </div>
      <div class="aph-right">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称..." clearable :prefix-icon="Search" style="width:260px" @keyup.enter="fetchUsers" @clear="fetchUsers" />
      </div>
    </div>

    <div class="ap-card">
      <el-table :data="userList" v-loading="loading" stripe class="modern-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32">{{ row.nickname?.charAt(0) }}</el-avatar>
              <div>
                <span class="uc-name">{{ row.nickname }}</span>
                <span class="uc-uname">@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="creditScore" label="信用分" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small" effect="dark" round>{{ row.status===1?'正常':'封禁' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status===1" type="danger" size="small" plain @click="handleUpdateStatus(row.id,0)">封禁</el-button>
            <el-button v-else type="success" size="small" plain @click="handleUpdateStatus(row.id,1)">解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="ap-footer" v-if="total>0">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" background @size-change="fetchUsers" @current-change="fetchUsers" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const keyword = ref(''); const userList = ref([]); const total = ref(0)
const loading = ref(false); const page = ref(1); const pageSize = ref(10)

async function fetchUsers() {
  loading.value = true
  try {
    const r = await adminApi.getUsers({ keyword: keyword.value, pageNum: page.value, pageSize: pageSize.value })
    userList.value = r.data?.data?.records || r.data?.data || r.data?.records || []
    total.value = r.data?.data?.total || r.data?.total || 0
  } catch {} finally { loading.value = false }
}
async function handleUpdateStatus(id, status) {
  try { await ElMessageBox.confirm(`确定${status===0?'封禁':'解封'}此用户？`, '确认', { type: 'warning' }) } catch { return }
  try { await adminApi.updateUserStatus(id, status); ElMessage.success('操作成功'); fetchUsers() } catch {}
}
onMounted(() => fetchUsers())
</script>

<style scoped>
.admin-page { max-width: 100%; }
.ap-hero { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  h2 { font-size: 22px; font-weight: 800; margin: 0; }
  p { font-size: 14px; color: #909399; margin: 4px 0 0; strong { color: var(--primary-color); } }
}
.ap-card { background: #fff; border: 1px solid #edf0f4; border-radius: 16px; padding: 20px 24px; }
.modern-table {
  :deep(th) { font-weight: 700; color: var(--text-primary); background: #f8f9fb; border: none; }
  :deep(td) { border-color: #f5f6f8; }
}
.user-cell { display: flex; align-items: center; gap: 10px; }
.uc-name { display: block; font-size: 14px; font-weight: 600; }
.uc-uname { display: block; font-size: 12px; color: #909399; }
.ap-footer { display: flex; justify-content: center; margin-top: 20px; }
</style>
