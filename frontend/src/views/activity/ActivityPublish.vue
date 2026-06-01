<template>
  <div class="activity-publish page-container">
    <h2>发布活动</h2>
    <p>发起一次临时搭伴，寻找志同道合的伙伴</p>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
        size="large"
      >
        <el-form-item label="活动类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="meal"><el-icon :size="16"><KnifeFork /></el-icon> 拼饭</el-radio>
            <el-radio value="exhibition"><el-icon :size="16"><PictureFilled /></el-icon> 拼展</el-radio>
            <el-radio value="travel"><el-icon :size="16"><MapLocation /></el-icon> 拼旅行</el-radio>
            <el-radio value="other"><el-icon :size="16"><MoreFilled /></el-icon> 其他</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请描述活动详情"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动时间" prop="activityTime">
          <el-date-picker
            v-model="form.activityTime"
            type="datetime"
            placeholder="选择活动时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="报名截止" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择报名截止时间（可选）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="活动地区" prop="area" required>
          <el-cascader
            v-model="form.area"
            :options="areaOptions"
            placeholder="请选择省/市/区"
            style="width: 100%"
            clearable
          />
        </el-form-item>

        <el-form-item label="详细地点" prop="location">
          <el-input v-model="form.detailAddr" placeholder="如：XX路XX号XX大厦3楼" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="人数上限" prop="maxMembers">
          <el-input-number v-model="form.maxMembers" :min="2" :max="100" />
        </el-form-item>

        <el-form-item label="费用说明" prop="costDesc">
          <el-input v-model="form.costDesc" placeholder="如：AA制约50元/人" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布活动</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityApi } from '@/api'
import { KnifeFork, PictureFilled, MapLocation, MoreFilled } from '@element-plus/icons-vue'
import { areaData } from '@/utils/china-area'

const router = useRouter()

const areaOptions = areaData
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  type: 'meal',
  title: '',
  description: '',
  activityTime: '',
  endTime: '',
  area: [],
  detailAddr: '',
  location: '',
  maxMembers: 10,
  costDesc: '',
})

const rules = {
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  detailAddr: [{ required: true, message: '请输入详细地点', trigger: 'blur' }],
  maxMembers: [{ required: true, message: '请设置人数上限', trigger: 'change' }],
}

// 将级联选择的结果转为完整地址字符串
function areaToText(area) {
  if (!area || !area.length) return ''
  // 获取标签路径
  return area.map(v => {
    for (const p of areaData) {
      if (p.value === v) return p.label
      for (const c of (p.children || [])) {
        if (c.value === v) return c.label
        for (const d of (c.children || [])) {
          if (d.value === v) return d.label
        }
      }
    }
    return v
  }).join('')
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 组合完整地址
  form.location = areaToText(form.area) + (form.detailAddr || '')

  submitting.value = true
  try {
    await activityApi.publish({ ...form })
    ElMessage.success('活动发布成功')
    router.push('/activity')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.activity-publish { max-width: 700px; margin: 0 auto; }

.form-card {
  border-radius: 16px !important; border: 1px solid #edf0f4 !important;

  :deep(.el-card__body) { padding: 32px 36px; }

  :deep(.el-form-item__label) { font-weight: 600; color: var(--text-primary); }
  :deep(.el-radio) { margin-right: 20px; }
  :deep(.el-cascader) { width: 100%; }
}

@media (max-width: 500px) {
  .form-card :deep(.el-card__body) { padding: 20px 16px; }
}
</style>
