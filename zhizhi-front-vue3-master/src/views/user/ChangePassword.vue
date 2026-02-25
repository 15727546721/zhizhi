<template>
  <el-card class="change-password-container">
    <h2 class="title">修改密码</h2>
    <el-form 
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="password-form"
      @submit.prevent="submitForm"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入原密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码（6-20个字符）"
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item class="form-actions">
        <el-button type="primary" native-type="submit" :loading="submitting">确认修改</el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/module/user'
import { changePassword } from '@/api/user'

// 类型定义
interface PasswordForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance | null>(null)
const submitting = ref<boolean>(false)

const form = ref<PasswordForm>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证确认密码
const validateConfirmPassword = (rule: any, value: string, callback: (error?: Error) => void): void => {
  if (value !== form.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const submitForm = async (): Promise<void> => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const response = await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword,
      confirmPassword: form.value.confirmPassword
    }) as any
    
    if (response.code === 20000) {
      ElMessage.success('密码修改成功，请重新登录')
      // 清除登录状态
      userStore.clearUserState()
      // 跳转到首页并显示登录弹窗
      router.push('/')
      window.dispatchEvent(new CustomEvent('show-login-dialog'))
    } else {
      ElMessage.error(response.info || '密码修改失败')
    }
  } catch (error) {
    // 修改失败
  } finally {
    submitting.value = false
  }
}

const goBack = (): void => {
  router.back()
}
</script>

<style scoped>
.change-password-container {
  max-width: 500px;
  margin: 60px auto;
  padding: 32px;
  border-radius: 16px;
  background-color: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 32px;
  color: #2c3e50;
  text-align: center;
}

.password-form {
  max-width: 400px;
  margin: 0 auto;
}

.form-actions {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

:deep(.el-input) {
  --el-input-height: 44px;
}

:deep(.el-button) {
  min-width: 100px;
  height: 40px;
}
</style>
