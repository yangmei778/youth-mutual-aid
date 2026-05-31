<template>
  <div class="user-profile page-container">
    <el-row :gutter="20">
      <!-- 左侧个人信息 -->
      <el-col :span="8">
        <el-card>
          <div class="profile-header">
            <div class="avatar-wrapper" @click="triggerAvatarUpload">
              <el-avatar :size="80" :src="userInfo.avatar">{{ userInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="avatar-overlay"><span>更换头像</span></div>
            </div>
            <input ref="avatarInput" type="file" accept="image/*" style="display: none" @change="handleAvatarChange" />
            <h3>{{ userInfo.nickname }}</h3>
            <p class="username">@{{ userInfo.username }}</p>
            <CreditBadge :score="userInfo.creditScore || 0" />
          </div>

          <el-divider />

          <el-form :model="editForm" label-width="80px" size="default">
            <el-form-item label="昵称">
              <el-input v-model="editForm.nickname" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="editForm.gender">
                <el-option label="未知" :value="0" />
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="年龄">
              <el-input-number v-model="editForm.age" :min="1" :max="120" />
            </el-form-item>
            <el-form-item label="城市">
              <el-input v-model="editForm.city" />
            </el-form-item>
            <el-form-item label="简介">
              <el-input v-model="editForm.bio" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="我的发布" name="posts">
            <div v-if="postsLoading" v-loading="true" style="min-height: 200px" />
            <el-empty v-else-if="myPosts.length === 0" description="暂无发布" />
            <el-row :gutter="12" v-else>
              <el-col :span="12" v-for="post in myPosts" :key="post.id">
                <el-card shadow="hover" class="post-card" @click="goToPost(post)">
                  <div class="post-title">{{ post.title }}</div>
                  <el-tag :type="post.type === 'teach' ? 'success' : 'primary'" size="small">
                    {{ post.type === 'teach' ? '能教' : '想学' }}
                  </el-tag>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="我的互助" name="mutual">
            <div class="mutual-toolbar" v-if="myRecords.length > 0">
              <el-select v-model="recordFilter" placeholder="按状态筛选" clearable style="width: 140px" @change="loadRecords">
                <el-option label="全部" value="" />
                <el-option label="待确认" value="pending" />
                <el-option label="进行中" value="ongoing" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </div>
            <div v-if="recordsLoading" v-loading="true" style="min-height: 200px" />
            <el-empty v-else-if="myRecords.length === 0" description="暂无互助记录" />
            <el-table :data="myRecords" v-else stripe>
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  {{ typeMap[row.type] || row.type }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="requestMessage" label="留言" show-overflow-tooltip />
              <el-table-column prop="createdAt" label="时间" width="170" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="$router.push(`/mutual/${row.id}`)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="技能标签" name="tags">
            <div class="tags-section">
              <h4>我能教</h4>
              <div class="tag-list">
                <el-tag v-for="tag in teachTags" :key="tag.id" closable @close="removeTag(tag.id)"
                  type="success" class="skill-tag">{{ tag.tagName }}</el-tag>
                <el-button size="small" @click="addTag('teach')">+ 添加</el-button>
              </div>

              <h4 style="margin-top: 16px;">我想学</h4>
              <div class="tag-list">
                <el-tag v-for="tag in learnTags" :key="tag.id" closable @close="removeTag(tag.id)"
                  type="primary" class="skill-tag">{{ tag.tagName }}</el-tag>
                <el-button size="small" @click="addTag('learn')">+ 添加</el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <!-- 添加标签对话框 -->
    <el-dialog v-model="tagDialogVisible" :title="`添加${tagType === 'teach' ? '能教' : '想学'}标签`" width="400px">
      <el-input v-model="newTagName" placeholder="输入标签名称" @keyup.enter="confirmAddTag" />
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { userApi, skillApi, mutualApi, uploadApi } from '@/api'
import { ElMessage } from 'element-plus'
import CreditBadge from '@/components/CreditBadge.vue'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})
const activeTab = ref('posts')
const saving = ref(false)
const postsLoading = ref(false)
const recordsLoading = ref(false)
const myPosts = ref([])
const myRecords = ref([])
const skillTags = ref([])
const tagDialogVisible = ref(false)
const tagType = ref('teach')
const newTagName = ref('')
const recordFilter = ref('')
const avatarInput = ref(null)

const typeMap = { skill: '技能交换', goods: '物品共享', activity: '临时搭伴' }
const statusMap = { pending: '待确认', ongoing: '进行中', completed: '已完成', cancelled: '已取消' }
const statusType = { pending: 'warning', ongoing: 'primary', completed: 'success', cancelled: 'info' }

const editForm = ref({
  nickname: '',
  gender: 0,
  age: null,
  city: '',
  bio: '',
})

const teachTags = computed(() => skillTags.value.filter(t => t.type === 'teach'))
const learnTags = computed(() => skillTags.value.filter(t => t.type === 'learn'))

onMounted(async () => {
  await userStore.fetchUserInfo()
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    gender: userInfo.value.gender || 0,
    age: userInfo.value.age,
    city: userInfo.value.city || '',
    bio: userInfo.value.bio || '',
  }
  loadData()
})

async function loadData() {
  postsLoading.value = true
  recordsLoading.value = true
  try {
    const [postsRes, recordsRes, tagsRes] = await Promise.all([
      userApi.getUserPosts(userInfo.value.id, 1, 20).catch(() => ({ data: { records: [] } })),
      mutualApi.getMyRecords({ pageNum: 1, pageSize: 20, status: recordFilter.value || undefined }).catch(() => ({ data: { records: [] } })),
      userApi.getUserSkillTags(userInfo.value.id).catch(() => ({ data: [] })),
    ])
    myPosts.value = postsRes.data?.records || postsRes.data?.data?.records || []
    myRecords.value = recordsRes.data?.records || recordsRes.data?.data?.records || []
    skillTags.value = tagsRes.data || tagsRes.data?.data || []
  } catch (e) {
    ElMessage.error('数据加载失败')
  } finally {
    postsLoading.value = false
    recordsLoading.value = false
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const params = { pageNum: 1, pageSize: 20 }
    if (recordFilter.value) params.status = recordFilter.value
    const res = await mutualApi.getMyRecords(params)
    myRecords.value = res.data?.records || res.data?.data?.records || []
  } catch (e) {
    ElMessage.error('加载互助记录失败')
  } finally {
    recordsLoading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await userApi.updateProfile(editForm.value)
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像文件不能超过 5MB')
    return
  }
  try {
    // 先通过通用上传接口上传文件
    const uploadRes = await uploadApi.uploadImage(file)
    const avatarUrl = uploadRes.data?.data || uploadRes.data || ''
    // 再更新个人资料中的头像
    await userApi.updateProfile({ avatar: avatarUrl })
    await userStore.fetchUserInfo()
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
  // 清空 input 以允许重复选择同一文件
  e.target.value = ''
}

function goToPost(post) {
  // 根据 post 来源判断跳转（由于后端只返回 skill 类型，暂时都跳转 skill 详情）
  // 如果后续支持 goods/activity 类型，可根据 post.postType 区分
  if (post.postType === 'goods') {
    router.push(`/goods/${post.id}`)
  } else if (post.postType === 'activity') {
    router.push(`/activity/${post.id}`)
  } else {
    router.push(`/skill/${post.id}`)
  }
}

function addTag(type) {
  tagType.value = type
  newTagName.value = ''
  tagDialogVisible.value = true
}

async function confirmAddTag() {
  if (!newTagName.value.trim()) return
  skillTags.value.push({ type: tagType.value, tagName: newTagName.value.trim() })
  tagDialogVisible.value = false
  await saveTags()
}

async function removeTag(id) {
  skillTags.value = skillTags.value.filter(t => t.id !== id)
  await saveTags()
}

async function saveTags() {
  try {
    await userApi.updateSkillTags(skillTags.value)
    ElMessage.success('标签已更新')
  } catch (e) {
    ElMessage.error('标签更新失败')
  }
}
</script>

<style scoped>
.profile-header {
  text-align: center;
  padding: 20px 0;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.profile-header h3 {
  margin: 12px 0 4px;
}

.username {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 8px;
}

.post-card {
  cursor: pointer;
  margin-bottom: 12px;
}

.post-title {
  font-weight: 500;
  margin-bottom: 8px;
}

.mutual-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

.tags-section h4 {
  margin-bottom: 8px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  font-size: 14px;
}
</style>
