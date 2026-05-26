<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2>🤝 注册账号</h2>
      <p class="register-subtitle">加入互助青年，开始互助之旅</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="设置用户名（3-30位字符）" maxlength="30" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="设置密码（6-20位）" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="设置昵称" maxlength="20" />
        </el-form-item>

        <el-form-item prop="city">
          <el-select v-model="form.city" placeholder="选择所在城市" filterable style="width: 100%">
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
            <el-option label="广州" value="广州" />
            <el-option label="深圳" value="深圳" />
            <el-option label="杭州" value="杭州" />
            <el-option label="成都" value="成都" />
            <el-option label="武汉" value="武汉" />
            <el-option label="南京" value="南京" />
            <el-option label="重庆" value="重庆" />
            <el-option label="西安" value="西安" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" class="register-btn">
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  city: '',
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请设置用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '用户名长度3-30个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: '请设置昵称', trigger: 'blur' },
    { min: 2, max: 50, message: '昵称长度为2-50位', trigger: 'blur' },
  ],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
}

async function handleRegister() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      city: form.city,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 420px;
  padding: 20px;
  border-radius: 12px;

  h2 {
    text-align: center;
    font-size: 24px;
    margin-bottom: 4px;
  }

  .register-subtitle {
    text-align: center;
    color: var(--text-secondary);
    margin-bottom: 24px;
  }
}

.register-btn {
  width: 100%;
}

.register-footer {
  text-align: center;
  color: var(--text-secondary);
  margin-top: 16px;

  a {
    color: var(--primary-color);
  }
}
</style>
