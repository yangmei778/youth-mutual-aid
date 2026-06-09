<template>
  <div class="register-page">
    <!-- 动态背景粒子 -->
    <div class="particles">
      <span v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)"></span>
    </div>

    <!-- 左侧品牌面板 -->
    <div class="brand-panel">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
      </div>

      <div class="brand-content">
        <div class="brand-icon-wrap">
          <el-icon :size="48"><Connection /></el-icon>
        </div>
        <h1>互助青年</h1>
        <p class="brand-desc">青年同城互助平台</p>
        <div class="brand-features">
          <span><el-icon :size="14"><Collection /></el-icon> 技能交换</span>
          <span><el-icon :size="14"><Present /></el-icon> 闲置共享</span>
          <span><el-icon :size="14"><Connection /></el-icon> 临时搭伴</span>
        </div>
      </div>
      <p class="brand-footer">同城互助，让生活更美好</p>
    </div>

    <!-- 右侧注册表单 -->
    <div class="form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>加入互助青年，开启同城互助</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
          size="large"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="设置用户名（3-30位）"
              maxlength="30"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="设置密码（6-20位）"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              show-password
              :prefix-icon="CircleCheck"
            />
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="设置昵称"
              maxlength="20"
              :prefix-icon="Edit"
            />
          </el-form-item>

          <el-form-item prop="cityArea">
            <el-cascader
              v-model="form.cityArea"
              :options="areaOptions"
              placeholder="选择所在城市"
              style="width: 100%"
              class="city-select"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleRegister" :loading="loading" class="register-btn">
              <span v-if="!loading">注 册</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          已有账号？<router-link to="/login" class="login-link">立即登录 →</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'
import { showConfirm } from '@/utils/confirm'
import { Connection, User, Lock, CircleCheck, Edit, Collection, Present } from '@element-plus/icons-vue'
import { areaData } from '@/utils/china-area'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const areaOptions = areaData

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  cityArea: [],
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
  cityArea: [{ required: true, message: '请选择城市', trigger: 'change', validator: (r, v, cb) => { if (!v || v.length === 0) cb(new Error('请选择城市')); else cb() } }],
}

// 粒子随机样式
function particleStyle(i) {
  const size = 2 + Math.random() * 4
  return {
    width: size + 'px',
    height: size + 'px',
    left: (i * 5.7) % 100 + '%',
    top: (i * 13.7) % 100 + '%',
    animationDelay: (i * 0.7) + 's',
    animationDuration: (6 + Math.random() * 8) + 's',
    opacity: 0.15 + Math.random() * 0.35,
  }
}

async function handleRegister() {
  await formRef.value?.validate()
  // 级联选择值转为城市名（省/市/区 → 只取城市名）
  if (!form.cityArea || form.cityArea.length === 0) { ElMessage.warning('请选择城市'); return }
  let cityName = ''
  const province = areaData.find(p => p.value === form.cityArea[0])
  if (province) {
    if (form.cityArea.length >= 2) {
      const city = province.children?.find(c => c.value === form.cityArea[1])
      cityName = city?.label || province.label
    } else {
      cityName = province.label
    }
  }
  form.city = cityName || '未知'
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
  } catch {
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
  position: relative;
  overflow: hidden;
  animation: pageIn 0.6s ease-out;
}

@keyframes pageIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ====== 粒子背景 ====== */
.particles {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: var(--primary-color);
  animation: drift linear infinite;
}

@keyframes drift {
  0% { transform: translateY(0) translateX(0) scale(1); opacity: 0; }
  10% { opacity: 0.3; }
  90% { opacity: 0.3; }
  100% { transform: translateY(-100vh) translateX(40px) scale(0.5); opacity: 0; }
}

/* ====== 左侧品牌面板 ====== */
.brand-panel {
  flex: 0 0 44%;
  background: linear-gradient(135deg, #1a73e8 0%, #4a00e0 50%, #7b2ff7 100%);
  background-size: 300% 300%;
  animation: gradientShift 8s ease infinite;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.floating-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  animation: floatShape linear infinite;
}

.shape-1 {
  width: 400px; height: 400px;
  top: -120px; left: -80px;
  animation-duration: 20s;
}
.shape-2 {
  width: 200px; height: 200px;
  top: 60%; right: -40px;
  animation-duration: 15s;
  animation-delay: -5s;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
}
.shape-3 {
  width: 150px; height: 150px;
  bottom: 10%; left: 20%;
  animation-duration: 18s;
  animation-delay: -10s;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
}
.shape-4 {
  width: 100px; height: 100px;
  top: 30%; left: 50%;
  animation-duration: 12s;
  animation-delay: -3s;
  background: rgba(255, 255, 255, 0.08);
}
.shape-5 {
  width: 250px; height: 250px;
  top: -60px; right: 30%;
  animation-duration: 25s;
  animation-delay: -15s;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 40% 60% 65% 35% / 40% 45% 55% 60%;
}

@keyframes floatShape {
  0% { transform: translateY(0) rotate(0deg) scale(1); }
  33% { transform: translateY(-30px) rotate(120deg) scale(1.05); }
  66% { transform: translateY(20px) rotate(240deg) scale(0.95); }
  100% { transform: translateY(0) rotate(360deg) scale(1); }
}

.brand-content {
  text-align: center;
  color: #fff;
  position: relative;
  z-index: 2;
  animation: fadeUp 0.8s ease-out 0.2s both;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.brand-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 24px;
  color: #fff;
  animation: iconPulse 3s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255,255,255,0.2); }
  50% { box-shadow: 0 0 0 20px rgba(255,255,255,0); }
}

.brand-content h1 {
  font-size: 34px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 10px;
  letter-spacing: 3px;
}

.brand-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 40px;
  letter-spacing: 1px;
}

.brand-features {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 8px 18px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 24px;
    backdrop-filter: blur(4px);
    transition: all 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.15);
      border-color: rgba(255, 255, 255, 0.25);
    }
  }
}

.brand-footer {
  position: absolute;
  bottom: 32px;
  color: rgba(255, 255, 255, 0.35);
  font-size: 13px;
  z-index: 2;
  letter-spacing: 1px;
}

/* ====== 右侧表单面板 ====== */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fb;
  position: relative;
  z-index: 1;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: radial-gradient(circle, #e0e5ec 1px, transparent 1px);
    background-size: 24px 24px;
    opacity: 0.5;
    pointer-events: none;
  }
}

.form-wrapper {
  width: 420px;
  max-width: 90%;
  position: relative;
  z-index: 1;
  animation: fadeUp 0.8s ease-out 0.4s both;
}

.form-header {
  margin-bottom: 32px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 6px;
  }

  p {
    color: var(--text-secondary);
    font-size: 14px;
    margin: 0;
  }
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 6px 14px;
    background: #fff;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 0 0 1px #e8ecf1;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08), 0 0 0 1px rgba(64, 158, 255, 0.3);
    }

    &.is-focus {
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.12), 0 0 0 2px rgba(64, 158, 255, 0.25);
      transform: translateY(-1px);
    }
  }

  .city-select {
    :deep(.el-input__wrapper) {
      padding-left: 14px;
    }
  }
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  border-radius: 12px;
  margin-top: 12px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    transition: left 0.5s;
  }

  &:hover::after {
    left: 100%;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.35);
  }

  &:active {
    transform: translateY(0);
  }
}

.form-footer {
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 24px;

  .login-link {
    color: var(--primary-color);
    font-weight: 600;
    margin-left: 4px;
    transition: all 0.2s;

    &:hover {
      color: #337ecc;
      text-decoration: none;
      letter-spacing: 0.5px;
    }
  }
}

/* ====== 响应式 ====== */
@media (max-width: 768px) {
  .brand-panel { display: none; }
  .form-panel { flex: 1; padding: 24px; }
  .particles { display: none; }
}
</style>
