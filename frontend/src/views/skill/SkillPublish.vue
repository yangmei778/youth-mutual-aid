<template>
  <div class="skill-publish page-container">
    <div class="page-header">
      <el-button text @click="router.push('/skill')">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <h2>发布技能</h2>
    </div>

    <el-card class="form-card form-card-shared" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
        size="large"
      >
        <el-form-item label="交换类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="teach">我能教</el-radio>
            <el-radio value="learn">我想学</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="技能标题 *" prop="title">
          <el-input v-model="form.title" placeholder="例如：Python入门教学、吉他弹唱" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="技能分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>

        <el-form-item label="技能描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="例如：擅长Python后端开发，有3年经验，可以教入门到进阶"
            :rows="5"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="可用时间" prop="availableTime">
          <el-input v-model="form.availableTime" placeholder="例如：周末全天 / 工作日19:00-22:00" maxlength="50" />
        </el-form-item>

        <el-form-item label="期望地点" prop="preferredLocation">
          <el-input v-model="form.preferredLocation" placeholder="例如：北京市海淀区咖啡馆 / 线上" maxlength="100" />
        </el-form-item>

        <el-form-item label="支持线上" prop="onlineSupport">
          <el-switch v-model="form.onlineSupport" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="发布方式">
          <el-switch v-model="form.isAnonymous" active-text="匿名发布" inactive-text="显示昵称" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            发布技能
          </el-button>
          <el-button @click="router.push('/skill')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { skillApi, configApi } from '@/api'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { SKILL_CATEGORIES } from '@/utils/constants'

const router = useRouter()

const categories = ref([...SKILL_CATEGORIES, '其他'])

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  type: 'teach',
  title: '',
  category: '',
  description: '',
  availableTime: '',
  preferredLocation: '',
  onlineSupport: false,
  isAnonymous: false,
})

const rules = {
  type: [{ required: true, message: '请选择交换类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入技能标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度2-50个字符', trigger: 'blur' },
  ],
  category: [{ required: true, message: '请选择技能分类', trigger: 'change' }],
  description: [
    { required: true, message: '请输入技能描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度10-500个字符', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const data = { ...form, onlineSupport: form.onlineSupport ? 1 : 0, isAnonymous: form.isAnonymous ? 1 : 0 }
    await skillApi.publish(data)
    ElMessage.success('发布成功')
    router.push('/skill')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.skill-publish {
  max-width: 720px;
  margin: 0 auto;
  --form-theme: #3b82f6;
}

.page-header {
  margin-bottom: 20px;
  h2 { font-size: 24px; color: var(--text-primary); margin-top: 8px; }
}

// 输入框/文本域聚焦色覆盖
.form-card-shared {
  .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 2px #3b82f6, 0 0 0 4px rgba(59,130,246,0.1);
  }
  .el-textarea__inner:focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
  }
}
</style>
