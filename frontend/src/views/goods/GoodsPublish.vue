<template>
  <div class="goods-publish page-container">
    <div class="page-header">
      <h2>发布物品</h2>
      <p>分享你的闲置物品，让它们发挥价值</p>
    </div>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="top"
        size="large"
      >
        <el-form-item label="物品名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入物品名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="物品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="详细描述物品的使用情况、特点等"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物品分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新旧程度" prop="conditionLevel">
              <div class="condition-slider">
                <el-slider v-model="form.conditionLevel" :min="1" :max="10" :step="1" show-stops />
                <span class="condition-value">{{ form.conditionLevel }} / 10</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="交换方式" prop="exchangeType">
          <el-radio-group v-model="form.exchangeType">
            <el-radio value="borrow">借用</el-radio>
            <el-radio value="gift">赠送</el-radio>
            <el-radio value="exchange">交换</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.exchangeType === 'exchange'" label="期望交换物品" prop="expectedItems">
          <el-input
            v-model="form.expectedItems"
            placeholder="请描述你希望交换的物品"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="form.exchangeType === 'borrow'" label="可借用天数" prop="borrowDays">
          <el-input-number v-model="form.borrowDays" :min="1" :max="365" :step="1" />
          <span style="margin-left: 8px; color: var(--text-secondary)">天</span>
        </el-form-item>

        <el-form-item label="物品图片" prop="images">
          <el-upload
            v-model:file-list="fileList"
            action=""
            :http-request="handleUpload"
            list-type="picture-card"
            :on-remove="handleRemove"
            accept="image/*"
            :limit="9"
            :on-exceed="() => ElMessage.warning('最多上传9张图片')"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
            发布物品
          </el-button>
          <el-button @click="$router.back()" size="large">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi, uploadApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()

const categories = ['数码', '书籍', '家居', '服饰', '运动', '食品', '其他']

const formRef = ref(null)
const fileList = ref([])
const submitting = ref(false)

const form = reactive({
  title: '',
  description: '',
  category: '',
  conditionLevel: 7,
  exchangeType: 'borrow',
  expectedItems: '',
  borrowDays: 7,
  images: [],
})

const rules = {
  title: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度2-50个字符', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入物品描述', trigger: 'blur' },
  ],
  category: [
    { required: true, message: '请选择物品分类', trigger: 'change' },
  ],
  exchangeType: [
    { required: true, message: '请选择交换方式', trigger: 'change' },
  ],
  expectedItems: [
    { required: true, message: '请描述期望交换的物品', trigger: 'blur' },
  ],
  borrowDays: [
    { required: true, message: '请输入可借用天数', trigger: 'blur' },
  ],
}

async function handleUpload(options) {
  try {
    const res = await uploadApi.uploadImage(options.file)
    const url = res.data
    form.images.push(url)
    options.onSuccess({ url })
  } catch {
    options.onError(new Error('上传失败'))
    ElMessage.error('图片上传失败')
  }
}

function handleRemove(uploadFile) {
  const url = uploadFile?.response?.url || uploadFile?.url
  const idx = form.images.indexOf(url)
  if (idx > -1) {
    form.images.splice(idx, 1)
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const data = { ...form }
    // 非交换类型时清除无关字段
    if (data.exchangeType !== 'exchange') {
      delete data.expectedItems
    }
    if (data.exchangeType !== 'borrow') {
      delete data.borrowDays
    }
    await goodsApi.publish(data)
    ElMessage.success('发布成功')
    router.push('/goods')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 24px;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  p {
    color: var(--text-secondary);
    font-size: 14px;
  }
}

.form-card {
  max-width: 720px;

  :deep(.el-card__body) {
    padding: 24px 32px;
  }
}

.condition-slider {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;

  .el-slider {
    flex: 1;
  }

  .condition-value {
    font-size: 14px;
    color: var(--text-secondary);
    white-space: nowrap;
  }
}
</style>
