<template>
  <div class="login-container">
    <div class="lang-corner"><LanguageSwitcher /></div>
    <div class="login-box">
      <div class="login-header">
        <h2>{{ t('brand.name') }}</h2>
        <p>{{ t('login.welcome') }}</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
      >
        <el-form-item :label="t('login.account')" prop="account">
          <el-input
            v-model="loginForm.account"
            :placeholder="t('login.accountPlaceholder')"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item :label="t('login.password')" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            :placeholder="t('login.passwordPlaceholder')"
            prefix-icon="Lock"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ t('login.submit') }}
          </el-button>
          <div class="login-footer">
            <span>{{ t('login.noAccount') }}</span>
            <el-button type="text" @click="$router.push('/register')">{{ t('login.registerNow') }}</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/user'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const loginRules = computed(() => ({
  account: [
    { required: true, message: t('login.accountRequired'), trigger: 'blur' },
    { min: 2, max: 20, message: t('login.accountLen'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('login.passwordLen'), trigger: 'blur' }
  ]
}))

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        localStorage.setItem('token', res.data.tokenValue)
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        ElMessage.success(t('login.success'))
        router.push('/dashboard')
      } catch (error) {
        console.error('login failed:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  background: url('@/assets/login-bg.png') no-repeat center center / cover;
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

.login-box {
  width: 450px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: border-color 0.4s ease, box-shadow 0.4s ease;
}

.login-box:hover {
  border-color: rgba(255, 255, 255, 0.7);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2), 0 0 25px rgba(255, 255, 255, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 10px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.login-form {
  width: 100%;
}
.login-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  font-weight: 600;
}

.login-btn {
  width: 100%;
  margin-bottom: 15px;
}

.login-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}
</style>
