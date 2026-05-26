<template>
  <div class="user-manage page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="search-bar">
            <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable @clear="fetchUsers" @keyup.enter="fetchUsers" style="width: 250px">
              <template #append>
                <el-button @click="fetchUsers" :icon="Search" />
              </template>
            </el-input>
          </div>
        </div>
      </template>
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="city" label="城市" width="120" />
        <el-table-column prop="creditScore" label="信用分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'active'"
              type="danger"
              size="small"
              @click="handleUpdateStatus(row.id, 'banned')"
            >封禁</el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleUpdateStatus(row.id, 'active')"
            >解封</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const keyword = ref('')
const userList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function fetchUsers() {
  loading.value = true
  try {
    const res = await adminApi.getUsers({
      keyword: keyword.value,
      page: currentPage.value,
      size: pageSize.value,
    })
    const data = res.data?.data || res.data || {}
    userList.value = data.records || data || []
    total.value = data.total || userList.value.length
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

async function handleUpdateStatus(id, status) {
  const action = status === 'banned' ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定${action}该用户？`, '提示', { type: 'warning' })
    await adminApi.updateUserStatus(id, status)
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
