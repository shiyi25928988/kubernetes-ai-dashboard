<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>MPI调度平台</h2>
        <p>用户注册</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        class="register-form"
      >
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="registerForm.account"
            placeholder="请输入账号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-group">
            <el-avatar :size="60">
              <img v-if="registerForm.avatar" :src="registerForm.avatar" alt="头像" style="width: 100%; height: 100%; object-fit: cover;">
              <span v-else>加载中...</span>
            </el-avatar>
            <el-button type="primary" size="small" @click="handleGetRandomAvatar">换一个头像</el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
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
            注册
          </el-button>
          <div class="register-footer">
            <span>已有账号？</span>
            <el-button type="text" @click="$router.push('/login')">立即登录</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Message, Phone } from '@element-plus/icons-vue'
import { registerUser } from '@/api/user'
import { generateRandomAvatar } from '@/utils/avatar'

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
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (value && !emailReg.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (value && !phoneReg.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

const registerRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 2, max: 20, message: '账号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

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
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
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
