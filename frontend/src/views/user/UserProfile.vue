<template>
  <div class="user-profile page-container">
    <!-- 顶部卡片 -->
    <div class="profile-hero">
      <div class="ph-bg"></div>
      <div class="ph-content">
        <div class="ph-left">
          <div class="avatar-wrap" @click="triggerAvatarUpload">
            <el-avatar :size="72" :src="userInfo.avatar" class="ph-avatar">{{ userInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="avatar-mask"><el-icon :size="20"><Camera /></el-icon></div>
          </div>
          <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
          <div class="ph-info">
            <h2>{{ userInfo.nickname }}</h2>
            <span class="ph-username">@{{ userInfo.username }}</span>
            <div class="ph-meta">
              <span v-if="editForm.city"><el-icon :size="14"><Location /></el-icon> {{ editForm.city }}</span>
              <span v-if="editForm.age">{{ editForm.age }}岁</span>
              <span v-if="editForm.gender">{{ genderLabel(editForm.gender) }}</span>
            </div>
          </div>
        </div>
        <div class="ph-right">
          <div class="ph-stat">
            <span class="phs-num">{{ myPosts.length }}</span>
            <span class="phs-lbl">发布</span>
          </div>
          <div class="ph-stat">
            <span class="phs-num">{{ myRecords.length }}</span>
            <span class="phs-lbl">互助</span>
          </div>
          <div class="ph-stat">
            <span class="phs-num credit-score">{{ userInfo.creditScore || 0 }}</span>
            <span class="phs-lbl">信用分</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体 -->
    <div class="profile-body">
      <div class="profile-side">
        <!-- 编辑资料 -->
        <div class="ps-card">
          <div class="psc-head">编辑资料</div>
          <el-form :model="editForm" label-position="top" size="default">
            <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="editForm.gender">
                <el-radio-button :value="0">保密</el-radio-button>
                <el-radio-button :value="1">男</el-radio-button>
                <el-radio-button :value="2">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="年龄"><el-input-number v-model="editForm.age" :min="1" :max="120" style="width:100%" /></el-form-item>
            <el-form-item label="城市"><el-input v-model="editForm.city" placeholder="所在城市" /></el-form-item>
            <el-form-item label="简介"><el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己..." /></el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="saving" style="width:100%">保存修改</el-button>
          </el-form>
        </div>

        <!-- 技能标签 -->
        <div class="ps-card">
          <div class="psc-head">技能标签</div>
          <div class="tag-group">
            <span class="tag-label">我能教</span>
            <div class="tag-row">
              <el-tag v-for="t in teachTags" :key="t.id" closable @close="removeTag(t.id)" type="success" effect="light" round>{{ t.tagName }}</el-tag>
              <el-button size="small" round @click="addTag('teach')">+</el-button>
            </div>
          </div>
          <div class="tag-group">
            <span class="tag-label">我想学</span>
            <div class="tag-row">
              <el-tag v-for="t in learnTags" :key="t.id" closable @close="removeTag(t.id)" effect="light" round>{{ t.tagName }}</el-tag>
              <el-button size="small" round @click="addTag('learn')">+</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-main">
        <!-- 我的发布 -->
        <div class="pm-section">
          <h3>我的发布 <span class="count-badge">{{ myPosts.length }}</span></h3>
          <div v-loading="postsLoading" class="posts-grid">
            <div v-for="post in myPosts" :key="post.id" class="post-card" @click="goToPost(post)">
              <span class="post-type-tag" :class="postTagClass(post)">
                {{ post.type === 'teach' ? '能教' : post.type === 'learn' ? '想学' : '' }}
                {{ post.exchangeType ? exchangeLabel(post.exchangeType) : '' }}
                {{ post.type === 'meal' || post.type === 'travel' ? typeLabel(post.type) : '' }}
                {{ !post.type && !post.exchangeType ? '帖子' : '' }}
              </span>
              <h4>{{ post.title }}</h4>
              <span class="post-time">{{ formatDate(post.createdAt) }}</span>
            </div>
            <div v-if="!postsLoading && myPosts.length === 0" class="empty-sm">还没有发布内容</div>
          </div>
        </div>

        <!-- 我的互助 -->
        <div class="pm-section">
          <div class="pm-head">
            <h3>我的互助 <span class="count-badge">{{ myRecords.length }}</span></h3>
            <el-select v-model="recordFilter" placeholder="筛选状态" clearable size="small" @change="loadRecords" style="width:120px">
              <el-option label="全部" value="" /><el-option label="待确认" value="pending" />
              <el-option label="进行中" value="ongoing" /><el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </div>
          <div v-loading="recordsLoading" class="records-list">
            <div v-for="r in myRecords" :key="r.id" class="record-item" @click="$router.push(`/mutual/${r.id}`)">
              <div class="ri-left">
                <span class="ri-type">{{ typeMap[r.type] || r.type }}</span>
                <span class="ri-msg">{{ r.requestMessage || '无留言' }}</span>
              </div>
              <div class="ri-right">
                <el-tag :type="statusType[r.status]" size="small" effect="plain">{{ statusMap[r.status] }}</el-tag>
                <span class="ri-time">{{ r.createdAt }}</span>
              </div>
            </div>
            <div v-if="!recordsLoading && myRecords.length === 0" class="empty-sm">暂无互助记录</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加标签对话框 -->
    <el-dialog v-model="tagDialogVisible" :title="`添加${tagType === 'teach' ? '能教' : '想学'}标签`" width="420px">
      <el-input v-model="newTagName" placeholder="输入标签名称，如：吉他、摄影" size="large" @keyup.enter="confirmAddTag" />
      <template #footer>
        <el-button @click="tagDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="confirmAddTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { userApi, mutualApi, uploadApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Location, Camera } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})
const activeTab = ref('posts')
const saving = ref(false); const postsLoading = ref(false); const recordsLoading = ref(false)
const myPosts = ref([]); const myRecords = ref([]); const skillTags = ref([])
const tagDialogVisible = ref(false); const tagType = ref('teach'); const newTagName = ref('')
const recordFilter = ref(''); const avatarInput = ref(null)

const typeMap = { skill: '技能交换', goods: '物品共享', activity: '临时搭伴' }
const statusMap = { pending: '待确认', ongoing: '进行中', completed: '已完成', cancelled: '已取消' }
const statusType = { pending: 'warning', ongoing: 'primary', completed: 'success', cancelled: 'info' }

const editForm = ref({ nickname: '', gender: 0, age: null, city: '', bio: '' })
const teachTags = computed(() => skillTags.value.filter(t => t.type === 'teach'))
const learnTags = computed(() => skillTags.value.filter(t => t.type === 'learn'))

function genderLabel(g) { const m={0:'保密',1:'男',2:'女'}; return m[g]||'' }
function typeLabel(t) { const m={meal:'拼饭',exhibition:'拼展',travel:'拼旅行',other:'其他'}; return m[t]||t }
function exchangeLabel(t) { const m={borrow:'借用',gift:'赠送',exchange:'交换'}; return m[t]||t }
function formatDate(d) { if(!d) return ''; const t=new Date(d); return `${t.getMonth()+1}月${t.getDate()}日` }
function postTagClass(p) {
  if (p.type==='teach') return 'tag-teach'; if (p.type==='learn') return 'tag-learn'
  if (p.exchangeType) return 'tag-goods'; if (p.type==='meal'||p.type==='travel') return 'tag-activity'
  return ''
}

onMounted(async () => {
  await userStore.fetchUserInfo()
  editForm.value = { nickname: userInfo.value.nickname||'', gender: userInfo.value.gender||0, age: userInfo.value.age, city: userInfo.value.city||'', bio: userInfo.value.bio||'' }
  loadData()
})
async function loadData() {
  postsLoading.value=true; recordsLoading.value=true
  try {
    const [p,r,t] = await Promise.all([
      userApi.getUserPosts(userInfo.value.id,1,20).catch(()=>({data:{records:[]}})),
      mutualApi.getMyRecords({pageNum:1,pageSize:20,status:recordFilter.value||undefined}).catch(()=>({data:{records:[]}})),
      userApi.getUserSkillTags(userInfo.value.id).catch(()=>({data:[]})),
    ])
    myPosts.value = p.data?.records||p.data?.data?.records||[]
    myRecords.value = r.data?.records||r.data?.data?.records||[]
    skillTags.value = t.data||t.data?.data||[]
  } catch {} finally { postsLoading.value=false; recordsLoading.value=false }
}
async function loadRecords() {
  recordsLoading.value=true
  try { const r=await mutualApi.getMyRecords({pageNum:1,pageSize:20,status:recordFilter.value||undefined}); myRecords.value=r.data?.records||r.data?.data?.records||[] }
  catch {} finally { recordsLoading.value=false }
}
async function saveProfile() {
  saving.value=true
  try { await userApi.updateProfile(editForm.value); await userStore.fetchUserInfo(); ElMessage.success('保存成功') }
  catch {} finally { saving.value=false }
}
function triggerAvatarUpload() { avatarInput.value?.click() }
async function handleAvatarChange(e) {
  const f=e.target.files[0]; if(!f) return
  if(f.size>5*1024*1024){ElMessage.warning('头像不能超过5MB');return}
  try { const r=await uploadApi.uploadImage(f); await userApi.updateProfile({avatar:r.data?.data||r.data}); await userStore.fetchUserInfo(); ElMessage.success('头像更新成功') }
  catch{ElMessage.error('上传失败')}
  e.target.value=''
}
function goToPost(p) {
  if(p.postType==='goods') router.push(`/goods/${p.id}`)
  else if(p.postType==='activity') router.push(`/activity/${p.id}`)
  else router.push(`/skill/${p.id}`)
}
function addTag(t){tagType.value=t;newTagName.value='';tagDialogVisible.value=true}
async function confirmAddTag(){if(!newTagName.value.trim())return;skillTags.value.push({type:tagType.value,tagName:newTagName.value.trim()});tagDialogVisible.value=false;await saveTags()}
async function removeTag(id){skillTags.value=skillTags.value.filter(t=>t.id!==id);await saveTags()}
async function saveTags(){
  try{await userApi.updateSkillTags(skillTags.value);ElMessage.success('标签已更新')}catch{ElMessage.error('保存失败')}
}
</script>

<style scoped>
.user-profile { max-width: 1000px; }

/* ====== Hero ====== */
.profile-hero {
  position: relative; border-radius: 22px; overflow: hidden;
  padding: 36px 36px 32px; margin-bottom: 24px;
  background: #fff; border: 1px solid #edf0f4;
}
.ph-bg {
  position: absolute; top: 0; left: 0; right: 0; height: 100px;
  background: linear-gradient(135deg, #f0f5ff 0%, #f8f9fd 50%, #fef9f0 100%);
  border-radius: 22px 22px 0 0;
}
.ph-content { position: relative; z-index: 1; display: flex; justify-content: space-between; align-items: center; padding-top: 30px; }
.ph-left { display: flex; align-items: center; gap: 20px; }

.avatar-wrap { position: relative; cursor: pointer; flex-shrink: 0; }
.ph-avatar { border: 4px solid #fff; box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.avatar-mask {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;
  color: #fff; opacity: 0; transition: opacity 0.3s;
}
.avatar-wrap:hover .avatar-mask { opacity: 1; }

.ph-info {
  h2 { font-size: 22px; font-weight: 800; margin: 0; color: var(--text-primary); }
  .ph-username { font-size: 13px; color: #909399; display: block; margin: 4px 0 8px; }
  .ph-meta { display: flex; gap: 16px; font-size: 13px; color: #6b7280;
    span { display: flex; align-items: center; gap: 4px; }
  }
}

.ph-right { display: flex; gap: 0; }
.ph-stat { text-align: center; padding: 0 28px;
  &+& { border-left: 1px solid #edf0f4; }
  .phs-num { display: block; font-size: 28px; font-weight: 900; color: var(--text-primary); }
  .phs-lbl { font-size: 12px; color: #909399; }
  .credit-score { color: var(--primary-color); }
}

/* ====== Body ====== */
.profile-body { display: flex; gap: 24px; align-items: flex-start; }
.profile-side { width: 280px; flex-shrink: 0; position: sticky; top: 76px; }
.profile-main { flex: 1; min-width: 0; }

.ps-card { background: #fff; border: 1px solid #edf0f4; border-radius: 18px; padding: 22px; margin-bottom: 20px; }
.psc-head { font-size: 15px; font-weight: 700; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #f5f6f8; color: var(--text-primary); }

.tag-group { margin-bottom: 16px; &:last-child { margin-bottom: 0; } }
.tag-label { font-size: 13px; color: #909399; display: block; margin-bottom: 8px; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }

/* Posts */
.pm-section { margin-bottom: 28px; }
.pm-section h3 { font-size: 16px; font-weight: 700; margin: 0 0 16px; color: var(--text-primary); }
.pm-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h3 { margin: 0; }
}
.count-badge { font-size: 12px; font-weight: 600; color: #909399; background: #f5f6f8; padding: 2px 8px; border-radius: 8px; margin-left: 6px; }
.posts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.post-card {
  padding: 18px; background: #fff; border: 1px solid #edf0f4; border-radius: 14px;
  cursor: pointer; transition: all 0.3s;
  &:hover { border-color: rgba(64,158,255,0.2); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.04); }
  h4 { font-size: 15px; font-weight: 600; margin: 10px 0 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
.post-type-tag { display: inline-block; padding: 2px 8px; border-radius: 5px; font-size: 11px; font-weight: 700;
  &.tag-teach { background: rgba(103,194,58,0.07); color: #52b818; }
  &.tag-learn { background: rgba(64,158,255,0.07); color: #409eff; }
  &.tag-goods { background: rgba(103,194,58,0.07); color: #52b818; }
  &.tag-activity { background: rgba(230,162,60,0.07); color: #e6a23c; }
}
.post-time { font-size: 12px; color: #b0b8c4; }

/* Records */
.records-list { display: flex; flex-direction: column; }
.record-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 0; cursor: pointer; transition: background 0.2s;
  &+& { border-top: 1px solid #f5f6f8; }
  &:hover { background: #f8f9fb; margin: 0 -12px; padding: 14px 12px; border-radius: 8px; border-color: transparent; }
}
.ri-left { display: flex; flex-direction: column; gap: 4px; }
.ri-type { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.ri-msg { font-size: 13px; color: #909399; }
.ri-right { display: flex; align-items: center; gap: 12px; }
.ri-time { font-size: 12px; color: #b0b8c4; }

.empty-sm { text-align: center; padding: 32px 0; font-size: 13px; color: #b0b8c4; }

@media (max-width: 768px) {
  .profile-body { flex-direction: column; }
  .profile-side { width: 100%; position: static; }
  .posts-grid { grid-template-columns: 1fr; }
  .ph-content { flex-direction: column; gap: 20px; }
}
</style>
