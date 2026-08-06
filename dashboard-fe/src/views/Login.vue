<template>
  <div class="login-container">
        <div class="login-box">
      <div class="login-header">
        <h2>DSSP</h2>
        <p>欢迎登录</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="请输入账号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
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
            登录
          </el-button>
          <div class="login-footer">
            <span>还没有账号？</span>
            <el-button type="text" @click="$router.push('/register')">立即注册</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/user'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 2, max: 20, message: '账号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        localStorage.setItem('token', res.data.tokenValue)
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } catch (error) {
        console.error('登录失败:', error)
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
