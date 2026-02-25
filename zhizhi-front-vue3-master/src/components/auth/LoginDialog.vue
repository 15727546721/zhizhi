<template>
  <div v-if="dialogVisible" class="modal-overlay" @click.self="handleClose">
    <div class="custom-dialog">
      <div class="close-btn" @click="handleClose">
        <el-icon class="close-icon"><Close /></el-icon>
      </div>

      <div class="auth-container">
        <div class="auth-header">
          <h2 class="auth-title">{{ currentTitle }}</h2>
          <p class="auth-subtitle">{{ currentSubtitle }}</p>
        </div>

        <div class="auth-tabs">
          <div
              class="tab-item"
              :class="{ active: isLoginMode === 'login' }"
              @click="switchToLogin"
          >
            登录/注册
          </div>
          <div
              class="tab-item"
              :class="{ active: isLoginMode === 'forget' }"
              @click="switchToForget"
          >
            忘记密码
          </div>
        </div>

        <div class="auth-content">
          <!-- 登录表单 -->
          <el-form
              v-if="isLoginMode === 'login'"
              :model="formData"
              :rules="formRules"
              ref="formRef"
              class="auth-form"
          >
            <!-- 登录方式切换 -->
            <div class="login-type-switch">
              <span 
                  :class="{ active: loginType === 'code' }" 
                  @click="loginType = 'code'"
              >验证码登录</span>
              <span class="divider">|</span>
              <span 
                  :class="{ active: loginType === 'password' }" 
                  @click="loginType = 'password'"
              >密码登录</span>
            </div>

            <!-- 登录安全提示 -->
            <div v-if="loginErrorInfo" class="login-error-tip">
              {{ loginErrorInfo }}
              <span v-if="remainingAttempts !== null" class="remaining">
                （剩余 {{ remainingAttempts }} 次尝试机会）
              </span>
            </div>
            
            <el-form-item prop="email">
              <div class="form-label">邮箱</div>
              <el-input
                  v-model="formData.email"
                  placeholder="请输入邮箱"
                  :prefix-icon="Message"
              />
            </el-form-item>

            <!-- 验证码登录 -->
            <el-form-item v-if="loginType === 'code'" prop="verifyCode">
              <div class="form-label">验证码</div>
              <div class="verify-code-input">
                <el-input
                    v-model="formData.verifyCode"
                    placeholder="请输入验证码"
                    maxlength="6"
                />
                <el-button 
                    type="primary" 
                    :disabled="sendCodeDisabled || !formData.email"
                    @click="handleSendCode"
                    class="send-code-btn"
                >
                  {{ sendCodeText }}
                </el-button>
              </div>
            </el-form-item>

            <!-- 密码登录 -->
            <el-form-item v-else prop="password">
              <div class="form-label">密码</div>
              <el-input
                  v-model="formData.password"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" class="submit-btn" @click="handleLogin">
                {{ loginType === 'code' ? '登录 / 注册' : '登录' }}
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <div class="form-options">
                <el-checkbox v-if="loginType === 'password'" v-model="formData.rememberMe">记住我</el-checkbox>
                <span v-else class="code-tip">新用户将自动注册</span>
                <el-link type="primary" :underline="false" @click="switchToForget">忘记密码?</el-link>
              </div>
            </div>
          </el-form>

          <!-- 忘记密码表单 -->
          <el-form
              v-else-if="isLoginMode === 'forget'"
              :model="formData"
              :rules="formRules"
              ref="formRef"
              class="auth-form"
          >
            <el-form-item prop="email">
              <div class="form-label">邮箱</div>
              <el-input
                  v-model="formData.email"
                  placeholder="请输入注册邮箱"
                  :prefix-icon="Message"
              />
            </el-form-item>

            <el-form-item prop="verifyCode">
              <div class="form-label">验证码</div>
              <div class="verify-code-input">
                <el-input
                    v-model="formData.verifyCode"
                    placeholder="请输入验证码"
                    maxlength="6"
                />
                <el-button 
                    type="primary" 
                    :disabled="sendCodeDisabled || !formData.email"
                    @click="handleSendResetCode"
                    class="send-code-btn"
                >
                  {{ sendCodeText }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="form-label">新密码</div>
              <el-input
                  v-model="formData.password"
                  type="password"
                  placeholder="请输入新密码"
                  :prefix-icon="Lock"
                  show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <div class="form-label">确认密码</div>
              <el-input
                  v-model="formData.confirmPassword"
                  type="password"
                  placeholder="请确认新密码"
                  :prefix-icon="Lock"
                  show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" class="submit-btn" @click="handleResetPassword">
                重置密码
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <el-link type="primary" :underline="false" @click="switchToLogin">返回登录</el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/module/user'
import { forgetPassword, resetPassword, sendVerifyCode } from '@/api/auth'
import { validateEmail, validatePassword } from '@/utils/validation'
import { Close, Message, Lock } from '@element-plus/icons-vue'

type LoginMode = 'login' | 'forget'
type LoginType = 'password' | 'code'

interface FormData {
  email: string
  password: string
  confirmPassword: string
  verifyCode: string
  rememberMe: boolean
}

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'login-success': []
}>()

const userStore = useUserStore()
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

watch(dialogVisible, (newVal) => {
  if (newVal) {
    document.body.classList.add('modal-open')
  } else {
    document.body.classList.remove('modal-open')
  }
})

const isLoginMode = ref<LoginMode>('login')
const loginType = ref<LoginType>('code')
const formRef = ref<FormInstance | null>(null)

const formData = reactive<FormData>({
  email: '',
  password: '',
  confirmPassword: '',
  verifyCode: '',
  rememberMe: false
})

const sendCodeDisabled = ref(false)
const sendCodeCountdown = ref(0)
const countdownTimer = ref<ReturnType<typeof setInterval> | null>(null)
const sendCodeText = computed(() => {
  return sendCodeCountdown.value > 0 ? `${sendCodeCountdown.value}s后重发` : '发送验证码'
})

const loginErrorInfo = ref('')
const remainingAttempts = ref<number | null>(null)

const formRules: FormRules = {
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value) {
          const result = validateEmail(value)
          result.valid ? callback() : callback(new Error(result.message))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value) {
          const result = validatePassword(value, true)
          result.valid ? callback() : callback(new Error(result.message))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        value !== formData.password ? callback(new Error('两次输入密码不一致')) : callback()
      },
      trigger: 'blur'
    }
  ]
}

const handleClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
  resetForm()
  isLoginMode.value = 'login'
}

const resetForm = () => {
  formData.email = ''
  formData.password = ''
  formData.confirmPassword = ''
  formData.verifyCode = ''
  loginErrorInfo.value = ''
  remainingAttempts.value = null
}

const switchToLogin = () => {
  isLoginMode.value = 'login'
  resetForm()
}

const switchToForget = () => {
  isLoginMode.value = 'forget'
  resetForm()
}

const currentTitle = computed(() => {
  switch (isLoginMode.value) {
    case 'login':
      return '欢迎使用'
    case 'forget':
      return '重置密码'
    default:
      return '欢迎使用'
  }
})

const currentSubtitle = computed(() => {
  switch (isLoginMode.value) {
    case 'login':
      return loginType.value === 'code' ? '使用邮箱验证码登录或注册' : '使用邮箱密码登录'
    case 'forget':
      return '通过邮箱验证码重置密码'
    default:
      return ''
  }
})

const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    loginErrorInfo.value = ''
    remainingAttempts.value = null

    let result
    if (loginType.value === 'password') {
      result = await userStore.loginAction({
        email: formData.email,
        password: formData.password
      })
    } else {
      result = await userStore.loginWithCodeAction({
        email: formData.email,
        verifyCode: formData.verifyCode
      })
    }

    if (result.success) {
      ElMessage.success('登录成功')
      handleClose()
      emit('login-success')
    } else {
      loginErrorInfo.value = result.error || '登录失败'
      if (result.remainingAttempts !== undefined && result.remainingAttempts !== null) {
        remainingAttempts.value = result.remainingAttempts
      }
      ElMessage.error(result.error || '登录失败')
    }
  } catch (error: any) {
    // 登录失败
    ElMessage.error(error.message || '登录失败')
  }
}

const handleResetPassword = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const response = await resetPassword({
      email: formData.email,
      verifyCode: formData.verifyCode,
      password: formData.password,
      confirmPassword: formData.confirmPassword
    })

    if (response.code === 20000) {
      ElMessage.success('密码重置成功，请登录')
      switchToLogin()
    } else {
      ElMessage.error(response.info || '重置失败')
    }
  } catch (error: any) {
    console.error('重置密码失败:', error)
    ElMessage.error(error.message || '重置密码失败')
  }
}

const startCountdown = () => {
  sendCodeCountdown.value = 60
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  countdownTimer.value = setInterval(() => {
    sendCodeCountdown.value--
    if (sendCodeCountdown.value <= 0) {
      clearInterval(countdownTimer.value!)
      countdownTimer.value = null
      sendCodeDisabled.value = false
    }
  }, 1000)
}

const handleSendResetCode = async () => {
  if (!formData.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  const emailResult = validateEmail(formData.email)
  if (!emailResult.valid) {
    ElMessage.warning(emailResult.message)
    return
  }

  try {
    sendCodeDisabled.value = true
    const response = await forgetPassword({ email: formData.email })

    if (response.code === 20000) {
      ElMessage.success('验证码已发送，请查收邮箱')
      startCountdown()
    } else {
      sendCodeDisabled.value = false
      ElMessage.error(response.info || '发送失败')
    }
  } catch (error: any) {
    sendCodeDisabled.value = false
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '发送验证码失败')
  }
}

const handleSendCode = async () => {
  if (!formData.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  const emailResult = validateEmail(formData.email)
  if (!emailResult.valid) {
    ElMessage.warning(emailResult.message)
    return
  }

  try {
    sendCodeDisabled.value = true
    const response = await sendVerifyCode({
      email: formData.email,
      scene: 'login'
    })

    if (response.code === 20000) {
      ElMessage.success('验证码已发送，请查收邮箱')
      startCountdown()
    } else {
      sendCodeDisabled.value = false
      ElMessage.error(response.info || '发送失败')
    }
  } catch (error: any) {
    sendCodeDisabled.value = false
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '发送验证码失败')
  }
}

onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
})
</script>


<style>
body.modal-open {
  overflow: hidden;
}
.el-message {
  z-index: 2002 !important;
}
</style>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.custom-dialog {
  position: relative;
  width: 420px;
  max-width: 90%;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: dialogFadeIn 0.3s ease;
}

@keyframes dialogFadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-container {
  padding: 32px 40px;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.close-btn:hover {
  transform: rotate(90deg);
}

.close-icon {
  font-size: 20px;
  color: #999;
}

.auth-header {
  text-align: center;
  margin-bottom: 16px;
}

.auth-title {
  font-size: 24px;
  font-weight: 600;
  color: #000;
  margin: 0 0 4px;
}

.auth-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.auth-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.tab-item {
  font-size: 15px;
  color: #666;
  padding: 0 24px 12px;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
}

.tab-item.active {
  color: #000;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #000;
}

.login-type-switch {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.login-type-switch span {
  cursor: pointer;
  color: #909399;
  transition: color 0.3s;
}

.login-type-switch span.active {
  color: #000;
  font-weight: 500;
}

.login-type-switch span:not(.divider):hover {
  color: #333;
}

.login-type-switch .divider {
  margin: 0 12px;
  color: #dcdfe6;
  cursor: default;
}

.login-error-tip {
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  padding: 10px 12px;
  margin-bottom: 16px;
  color: #f56c6c;
  font-size: 13px;
}

.login-error-tip .remaining {
  font-weight: 600;
  color: #e6a23c;
}

.form-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.auth-form :deep(.el-input__wrapper) {
  box-shadow: none;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 40px;
  background-color: #f5f7fa;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: #000;
  background-color: #fff;
}

.verify-code-input {
  display: flex;
  gap: 12px;
  width: 100%;
}

.verify-code-input :deep(.el-input) {
  flex: 1;
  min-width: 0;
}

.verify-code-input :deep(.el-input__wrapper) {
  height: 40px;
  box-shadow: none;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.verify-code-input :deep(.el-input__wrapper.is-focus) {
  border-color: #000;
  background-color: #fff;
}

.send-code-btn {
  width: 120px;
  height: 40px;
  flex-shrink: 0;
  font-size: 14px;
}

.submit-btn {
  width: 100%;
  height: 42px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 4px;
  background: #000;
  border: none;
}

.submit-btn:hover {
  background: #333;
}

.form-footer {
  margin-top: 12px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.code-tip {
  color: #909399;
  font-size: 13px;
}

@media screen and (max-width: 768px) {
  .custom-dialog {
    width: 90%;
  }
  .auth-container {
    padding: 24px 20px;
  }
  .verify-code-input {
    flex-direction: column;
  }
  .send-code-btn {
    width: 100%;
  }
}
</style>
