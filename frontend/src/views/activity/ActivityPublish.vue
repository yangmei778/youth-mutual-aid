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
            <el-radio value="meal">🍜 拼饭</el-radio>
            <el-radio value="exhibition">🎨 拼展</el-radio>
            <el-radio value="travel">✈️ 拼旅行</el-radio>
            <el-radio value="other">🤝 其他</el-radio>
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

        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
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

const router = useRouter()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  type: 'meal',
  title: '',
  description: '',
  activityTime: '',
  endTime: '',
  location: '',
  maxMembers: 10,
  costDesc: '',
})

const rules = {
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  maxMembers: [{ required: true, message: '请设置人数上限', trigger: 'change' }],
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

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
.activity-publish {
  h2 {
    font-size: 24px;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  p {
    color: var(--text-secondary);
    margin-bottom: 20px;
  }
}

.form-card {
  max-width: 680px;
}
</style>
