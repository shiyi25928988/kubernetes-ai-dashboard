<template>
  <div class="register-container">
    <div class="lang-corner"><LanguageSwitcher /></div>
    <div class="register-box">
      <div class="register-header">
        <h2>{{ t('brand.name') }}</h2>
        <p>{{ t('register.subtitle') }}</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        class="register-form"
      >
        <el-form-item :label="t('register.account')" prop="account">
          <el-input
            v-model="registerForm.account"
            :placeholder="t('register.accountPlaceholder')"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item :label="t('register.username')" prop="username">
          <el-input
            v-model="registerForm.username"
            :placeholder="t('register.usernamePlaceholder')"
            prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>
        <el-form-item :label="t('register.avatar')">
          <div class="avatar-group">
            <el-avatar :size="60">
              <img v-if="registerForm.avatar" :src="registerForm.avatar" :alt="t('register.avatar')" style="width: 100%; height: 100%; object-fit: cover;">
              <span v-else>{{ t('common.loading') }}</span>
            </el-avatar>
            <el-button type="primary" size="small" @click="handleGetRandomAvatar">{{ t('register.changeAvatar') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item :label="t('register.password')" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            :placeholder="t('register.passwordPlaceholder')"
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-form-item :label="t('register.confirmPassword')" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            :placeholder="t('register.confirmPlaceholder')"
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-form-item :label="t('register.email')" prop="email">
          <el-input
            v-model="registerForm.email"
            :placeholder="t('register.emailPlaceholder')"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item :label="t('register.phone')" prop="phone">
          <el-input
            v-model="registerForm.phone"
            :placeholder="t('register.phonePlaceholder')"
            prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            {{ t('register.submit') }}
          </el-button>
          <div class="register-footer">
            <span>{{ t('register.hasAccount') }}</span>
            <el-button type="text" @click="$router.push('/login')">{{ t('register.loginNow') }}</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { User, Lock, UserFilled, Message, Phone } from '@element-plus/icons-vue'
import { registerUser } from '@/api/user'
import { generateRandomAvatar } from '@/utils/avatar'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

// 页面加载时自动获取随机头像
onMounted(() => {
  handleGetRandomAvatar()
})

const registerForm = reactive({
  account: '',
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  avatar: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error(t('register.passwordMismatch')))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (value && !emailReg.test(value)) {
    callback(new Error(t('register.emailInvalid')))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (value && !phoneReg.test(value)) {
    callback(new Error(t('register.phoneInvalid')))
  } else {
    callback()
  }
}

const registerRules = computed(() => ({
  account: [
    { required: true, message: t('register.accountRequired'), trigger: 'blur' },
    { min: 2, max: 20, message: t('register.accountLen'), trigger: 'blur' }
  ],
  username: [
    { required: true, message: t('register.usernameRequired'), trigger: 'blur' },
    { min: 2, max: 20, message: t('register.usernameLen'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('register.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('register.passwordLen'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('register.confirmRequired'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}))

// 获取随机头像（前端 dicebear 本地生成）
const handleGetRandomAvatar = () => {
  registerForm.avatar = generateRandomAvatar()
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 移除confirmPassword字段，后端不需要，空字段也可以不传
        const { confirmPassword, email, phone, ...registerData } = registerForm
        // 邮箱和手机号有值才传
        if (email) registerData.email = email
        if (phone) registerData.phone = phone
        await registerUser(registerData)
        ElMessage.success(t('register.success'))
        router.push('/login')
      } catch (error) {
        console.error('register failed:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  width: 100vw;
  min-height: 100vh;
  background: url('@/assets/login-bg.png') no-repeat center center / cover;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow-x: hidden;
}

.lang-corner {
  position: absolute;
  top: 20px;
  right: 24px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.6);
  padding: 6px 12px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.register-box {
  width: 500px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  margin: 20px 0;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
}

.register-header p {
  color: #666;
  font-size: 14px;
}

.register-form {
  width: 100%;
}

.register-btn {
  width: 100%;
  margin-bottom: 15px;
}

.register-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.avatar-group {
  display: flex;
  align-items: center;
  gap: 15px;
}
</style>
