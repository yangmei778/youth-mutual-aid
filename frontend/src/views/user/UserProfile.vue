<template>
  <div class="user-profile page-container">
    <!-- 顶部个人信息卡（全宽） -->
    <div class="pf-hero">
      <div class="pfh-bg"></div>
      <div class="pfh-body">
        <div class="pfh-left">
          <div class="avatar-wrap" @click="triggerAvatarUpload" title="更换头像">
            <el-avatar :size="80" :src="userInfo.avatar" class="pfh-avatar">{{ userInfo.nickname?.charAt(0) || 'U' }}</el-avatar>
            <div class="avatar-mask"><el-icon :size="22"><Camera /></el-icon><span>更换头像</span></div>
          </div>
          <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
          <div class="pfh-info">
            <div class="pfh-top-row">
              <h2>{{ userInfo.nickname || '用户' }}</h2>
              <el-button size="small" round @click="showEdit = !showEdit">{{ showEdit ? '收起编辑' : '编辑资料' }}</el-button>
            </div>
            <span class="pfh-username">@{{ userInfo.username }}</span>
            <p v-if="editForm.bio" class="pfh-bio">{{ editForm.bio }}</p>
            <div class="pfh-meta">
              <span v-if="editForm.city"><el-icon :size="14"><Location /></el-icon> {{ editForm.city }}</span>
              <span v-if="editForm.age">{{ editForm.age }}岁</span>
            </div>
          </div>
        </div>
        <div class="pfh-stats">
          <div class="phs-item" @click="scrollTo('posts')"><strong>{{ myPosts.length }}</strong><span>发布</span></div>
          <div class="phs-item" @click="scrollTo('mutual')"><strong>{{ myRecords.length }}</strong><span>互助</span></div>
          <div class="phs-item phs-credit"><strong>{{ userInfo.creditScore || 0 }}</strong><span>信用分</span></div>
          <div class="phs-item"><strong>{{ reviewsTotal }}</strong><span>好评</span></div>
        </div>
      </div>
    </div>

    <!-- 主体两栏 -->
    <div class="pf-body">
      <!-- 左栏：技能标签 + 编辑资料 -->
      <div class="pf-left">
        <!-- 技能标签（核心） -->
        <div class="pf-card">
          <div class="pfc-head">🎯 技能标签</div>
          <div class="skill-block">
            <div class="sb-label">我能教</div>
            <div class="sb-tags">
              <el-tag v-for="t in teachTags" :key="t.id" closable @close="removeTag(t.id)" type="success" effect="light" round size="large">{{ t.tagName }}</el-tag>
              <el-button size="small" round @click="addTag('teach')">+ 添加</el-button>
            </div>
            <div v-if="!teachTags.length" class="sb-empty">还没有添加能教的技能</div>
          </div>
          <div class="skill-block">
            <div class="sb-label">我想学</div>
            <div class="sb-tags">
              <el-tag v-for="t in learnTags" :key="t.id" closable @close="removeTag(t.id)" effect="light" round size="large">{{ t.tagName }}</el-tag>
              <el-button size="small" round @click="addTag('learn')">+ 添加</el-button></div>
            <div v-if="!learnTags.length" class="sb-empty">还没有添加想学的技能</div>
          </div>
        </div>

        <!-- 编辑资料（可折叠） -->
        <div class="pf-card" v-if="showEdit">
          <div class="pfc-head">编辑资料</div>
          <el-form :model="editForm" label-position="top" size="default" class="edit-form">
            <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="editForm.gender" class="gender-group">
                <el-radio-button :value="0">保密</el-radio-button>
                <el-radio-button :value="1">男</el-radio-button>
                <el-radio-button :value="2">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12"><el-form-item label="年龄"><el-input-number v-model="editForm.age" :min="1" :max="120" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="城市"><el-input v-model="editForm.city" placeholder="所在城市" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="简介"><el-input v-model="editForm.bio" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="介绍一下自己..." /></el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="saving" style="width:100%">保存修改</el-button>
          </el-form>
        </div>
      </div>

      <!-- 右栏：发布 + 互助 -->
      <div class="pf-right">
        <!-- 我的发布 -->
        <div class="pf-card" ref="postsRef">
          <div class="pfc-head">
            <span>我的发布 <span class="count-badge">{{ myPosts.length }}</span></span>
            <el-button v-if="myPosts.length" text size="small" type="danger" @click="postSelectMode = !postSelectMode">
              {{ postSelectMode ? '取消' : '管理' }}
            </el-button>
          </div>
          <!-- 批量操作栏 -->
          <div v-if="postSelectMode && selectedPosts.length" class="batch-bar">
            <span>已选 {{ selectedPosts.length }} 项</span>
            <el-button type="danger" size="small" @click="batchDeletePosts">批量删除</el-button>
          </div>
          <div v-if="!postsLoading && myPosts.length === 0" class="empty-block">
            <p>还没有发布任何内容</p>
            <span>分享你的技能和闲置，认识更多志同道合的朋友吧</span>
            <el-button type="primary" size="small" round @click="router.push('/skill/publish')">去发布</el-button>
          </div>
          <div v-else v-loading="postsLoading" class="posts-grid">
            <div v-for="post in myPosts" :key="post.id" class="post-card" :class="{ selected: selectedPosts.includes(post) }" @click="postSelectMode ? togglePost(post) : goToPost(post)">
              <el-checkbox v-if="postSelectMode" :model-value="selectedPosts.includes(post)" @click.stop class="pc-check" />
              <span class="pc-type" :class="postTagClass(post)">{{ postTagLabel(post) }}</span>
              <h4>{{ post.title }}</h4>
              <span class="pc-time">{{ formatDate(post.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 我的互助 -->
        <div class="pf-card" ref="mutualRef">
          <div class="pfc-head">
            <span>我的互助 <span class="count-badge">{{ myRecords.length }}</span></span>
            <el-select v-if="myRecords.length > 0" v-model="recordFilter" placeholder="筛选" clearable size="small" @change="loadRecords" style="width:110px">
              <el-option label="全部" value="" /><el-option label="待确认" value="pending" />
              <el-option label="进行中" value="ongoing" /><el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </div>
          <div v-if="!recordsLoading && myRecords.length === 0" class="empty-block">
            <p>还没有参与任何互助</p>
            <span>去发现身边有趣的小伙伴吧</span>
            <el-button size="small" round @click="router.push('/home')">去探索</el-button>
          </div>
          <div v-else v-loading="recordsLoading" class="records-list">
            <div v-for="r in myRecords" :key="r.id" class="record-item" @click="$router.push(`/mutual/${r.id}`)">
              <div class="ri-left"><span class="ri-type">{{ typeMap[r.type] || r.type }}</span><span class="ri-msg">{{ r.requestMessage || '无留言' }}</span></div>
              <div class="ri-right">
                <el-tag :type="statusType[r.status]" size="small" effect="plain">{{ statusMap[r.status] }}</el-tag>
                <span class="ri-time">{{ r.createdAt }}</span>
              </div>
            </div>
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
import { userApi, mutualApi, uploadApi, reviewApi, skillApi, goodsApi, activityApi } from '@/api'
import { showConfirm } from '@/utils/confirm'
import { ElMessage } from 'element-plus'
import { Location, Camera } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo || {})
const saving = ref(false); const postsLoading = ref(false); const recordsLoading = ref(false)
const myPosts = ref([]); const myRecords = ref([]); const skillTags = ref([])
const tagDialogVisible = ref(false); const tagType = ref('teach'); const newTagName = ref('')
const recordFilter = ref(''); const avatarInput = ref(null)
const showEdit = ref(false); const postFilter = ref('all'); const postSelectMode = ref(false); const selectedPosts = ref([])
function togglePost(post) { const i = selectedPosts.value.indexOf(post); if (i > -1) selectedPosts.value.splice(i, 1); else selectedPosts.value.push(post) }
async function batchDeletePosts() {
  try { await showConfirm(`确定批量删除 ${selectedPosts.value.length} 条内容？不可恢复`, '批量删除', 'danger') } catch { return }
  for (const p of selectedPosts.value) {
    try { if (p.postType === 'goods') await goodsApi.deleteGoods(p.id); else if (p.postType === 'activity') await activityApi.deleteActivity(p.id); else await skillApi.deleteSkill(p.id) } catch {}
  }
  ElMessage.success('删除完成'); selectedPosts.value = []; postSelectMode.value = false; loadData()
}
const reviewsTotal = ref(0); const postsRef = ref(null); const mutualRef = ref(null)

const typeMap = { skill: '技能交换', goods: '物品共享', activity: '临时搭伴' }
const statusMap = { pending: '待确认', ongoing: '进行中', completed: '已完成', cancelled: '已取消' }
const statusType = { pending: 'warning', ongoing: 'primary', completed: 'success', cancelled: 'info' }

const editForm = ref({ nickname: '', gender: 0, age: null, city: '', bio: '' })
const teachTags = computed(() => skillTags.value.filter(t => t.type === 'teach'))
const learnTags = computed(() => skillTags.value.filter(t => t.type === 'learn'))

function genderLabel(g) { const m={0:'保密',1:'男',2:'女'}; return m[g]||'' }
function formatDate(d) { if(!d) return ''; const t=new Date(d); return `${t.getMonth()+1}月${t.getDate()}日` }
function postTagClass(p) {
  if (p.type==='teach') return 'type-teach'; if (p.type==='learn') return 'type-learn'
  if (p.exchangeType) return 'type-goods'; return 'type-activity'
}
function postTagLabel(p) {
  if (p.type==='teach') return '能教'; if (p.type==='learn') return '想学'
  if (p.exchangeType) { const m={sell:'出售',borrow:'借用',gift:'赠送',exchange:'交换'}; return m[p.exchangeType]||'物品' }
  return '活动'
}
function scrollTo(refName) {
  const el = refName === 'posts' ? postsRef.value : mutualRef.value
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(async () => {
  await userStore.fetchUserInfo()
  editForm.value = { nickname: userInfo.value.nickname||'', gender: userInfo.value.gender||0, age: userInfo.value.age, city: userInfo.value.city||'', bio: userInfo.value.bio||'' }
  loadData()
})
async function loadData() {
  postsLoading.value=true; recordsLoading.value=true
  try {
    const [p,r,t,rv] = await Promise.all([
      userApi.getUserPosts(userInfo.value.id,1,50).catch(()=>({data:{records:[]}})),
      mutualApi.getMyRecords({pageNum:1,pageSize:50,status:recordFilter.value||undefined}).catch(()=>({data:{records:[]}})),
      userApi.getUserSkillTags(userInfo.value.id).catch(()=>({data:[]})),
      reviewApi.getReceivedReviews({pageNum:1,pageSize:1}).catch(()=>({data:{total:0}})),
    ])
    myPosts.value = p.data?.records||p.data?.data?.records||[]
    myRecords.value = r.data?.records||r.data?.data?.records||[]
    skillTags.value = t.data||t.data?.data||[]
    reviewsTotal.value = rv.data?.total||rv.data?.data?.total||0
  } catch {} finally { postsLoading.value=false; recordsLoading.value=false }
}
async function loadRecords() {
  recordsLoading.value=true
  try { const r=await mutualApi.getMyRecords({pageNum:1,pageSize:50,status:recordFilter.value||undefined}); myRecords.value=r.data?.records||r.data?.data?.records||[] }
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
  try { const r=await uploadApi.uploadImage(f); await userApi.updateProfile({avatar:r?.data?.data||r?.data}); await userStore.fetchUserInfo(); ElMessage.success('头像更新成功') }
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
.user-profile { max-width: 960px; }

/* 顶部信息卡 */
.pf-hero { position: relative; border-radius: 22px; padding: 32px 36px; margin-bottom: 24px; background: #fff; border: 1px solid #edf0f4; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.pfh-bg { position: absolute; top: 0; left: 0; right: 0; height: 90px; background: linear-gradient(135deg, #f0f5ff, #f8f9fd, #fef9f0); border-radius: 22px 22px 0 0; }
.pfh-body { position: relative; z-index: 1; display: flex; justify-content: space-between; align-items: center; }
.pfh-left { display: flex; gap: 20px; align-items: flex-start; }
.avatar-wrap { position: relative; cursor: pointer; flex-shrink: 0; }
.pfh-avatar { border: 3px solid #fff; box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.avatar-mask { position: absolute; inset: 0; border-radius: 50%; background: rgba(0,0,0,0.45); display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; font-size: 11px; opacity: 0; transition: opacity 0.3s; gap: 4px; }
.avatar-wrap:hover .avatar-mask { opacity: 1; }

.pfh-info { padding-top: 4px; }
.pfh-top-row { display: flex; align-items: center; gap: 12px; margin-bottom: 4px; }
.pfh-top-row h2 { font-size: 22px; font-weight: 800; margin: 0; }
.pfh-username { font-size: 13px; color: #909399; display: block; margin-bottom: 6px; }
.pfh-bio { font-size: 14px; color: #606266; line-height: 1.5; margin: 4px 0 8px; max-width: 400px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.pfh-meta { display: flex; gap: 16px; font-size: 13px; color: #6b7280; span { display: flex; align-items: center; gap: 4px; } }

.pfh-stats { display: flex; gap: 0; flex-shrink: 0; align-items: center; }
.phs-item { text-align: center; padding: 16px 24px; cursor: pointer; transition: all 0.2s; border-radius: 12px;
  &+& { border-left: 1px solid #edf0f4; }
  &:hover { background: rgba(64,158,255,0.04); transform: translateY(-2px); }
  strong { display: block; font-size: 26px; font-weight: 900; color: var(--text-primary); margin-bottom: 2px; }
  span { font-size: 12px; color: #909399; }
}
.phs-credit { background: linear-gradient(135deg, rgba(64,158,255,0.06), rgba(123,47,247,0.03)); border-radius: 14px; border-left: none !important;
  strong { background: linear-gradient(135deg, #409eff, #7b2ff7); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
}

/* 主体两栏 */
.pf-body { display: flex; gap: 24px; align-items: flex-start; }
.pf-left { width: 340px; flex-shrink: 0; position: sticky; top: 80px; }
.pf-right { flex: 1; min-width: 0; }

.pf-card { background: #fff; border: 1px solid #edf0f4; border-radius: 18px; padding: 22px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.pfc-head { font-size: 15px; font-weight: 700; padding-bottom: 14px; border-bottom: 2px solid #f5f6f8; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }
.pfc-filters { display: flex; gap: 4px; span { padding: 4px 12px; border-radius: 6px; font-size: 12px; cursor: pointer; color: #909399; &.active { background: rgba(64,158,255,0.06); color: var(--primary-color); font-weight: 600; } } }
.count-badge { font-size: 12px; color: #909399; background: #f5f6f8; padding: 2px 8px; border-radius: 8px; margin-left: 6px; font-weight: 400; }

/* 技能标签 */
.skill-block { margin-bottom: 14px; &:last-child { margin-bottom: 0; } }
.sb-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.sb-tags { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.sb-empty { font-size: 12px; color: #b0b8c4; padding: 4px 0; }

/* 编辑表单 */
.edit-form { :deep(.el-input__wrapper) { border-radius: 8px; box-shadow: 0 0 0 1px #d1d5db; } :deep(.el-textarea__inner) { border-radius: 8px; border-color: #d1d5db; } }
.gender-group { :deep(.el-radio-button__inner) { border-radius: 8px; padding: 6px 16px; } }

/* 发布卡片 */
.posts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.post-card { position: relative; padding: 16px; border: 1px solid #edf0f4; border-radius: 14px; cursor: pointer; transition: all 0.2s;
  &:hover { border-color: rgba(64,158,255,0.2); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,0.04); }
  &.selected { border-color: var(--primary-color); background: rgba(64,158,255,0.04); }
  h4 { font-size: 14px; font-weight: 600; margin: 8px 0 4px; }
}
.pc-check { position: absolute; top: 10px; right: 10px; z-index: 2; }
.batch-bar { display: flex; align-items: center; gap: 14px; padding: 10px 16px; margin-bottom: 12px; background: #fef0f0; border: 1px solid #fbc4c4; border-radius: 10px; font-size: 13px; font-weight: 600; }
.pc-type { display: inline-block; padding: 2px 8px; border-radius: 5px; font-size: 11px; font-weight: 700;
  &.type-teach { background: rgba(103,194,58,0.07); color: #52b818; }
  &.type-learn { background: rgba(64,158,255,0.07); color: #409eff; }
  &.type-goods { background: rgba(103,194,58,0.07); color: #52b818; }
  &.type-activity { background: rgba(230,162,60,0.07); color: #e6a23c; }
}
.pc-time { font-size: 12px; color: #b0b8c4; }

/* 互助记录 */
.records-list { display: flex; flex-direction: column; }
.record-item { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; cursor: pointer;
  &+& { border-top: 1px solid #f5f6f8; }
  &:hover { background: #f8f9fb; margin: 0 -12px; padding: 14px 12px; border-radius: 8px; border-color: transparent; }
}
.ri-left { display: flex; flex-direction: column; gap: 4px; }
.ri-type { font-size: 14px; font-weight: 600; }
.ri-msg { font-size: 13px; color: #909399; }
.ri-right { display: flex; align-items: center; gap: 12px; }
.ri-time { font-size: 12px; color: #b0b8c4; }

/* 空状态 */
.empty-block { text-align: center; padding: 60px 20px; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 200px;
  p { font-size: 15px; font-weight: 600; color: var(--text-primary); margin: 0 0 4px; }
  span { font-size: 13px; color: #b0b8c4; display: block; margin-bottom: 14px; }
}

@media (max-width: 768px) {
  .pf-body { flex-direction: column; }
  .pf-left { width: 100%; position: static; }
  .pfh-body { flex-direction: column; gap: 20px; }
  .pfh-stats { align-self: stretch; justify-content: space-around; }
}
</style>
