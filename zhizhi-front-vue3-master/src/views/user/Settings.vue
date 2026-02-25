<template>
  <div class="settings-container">
    <div class="settings-header">
      <h2>账户设置</h2>
      <p>管理您的账户安全和隐私设置</p>
    </div>

    <el-card class="settings-section">
      <template #header>
        <div class="card-header">
          <span>安全设置</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>修改密码</h4>
          <p>定期修改密码可以保护您的账户安全</p>
        </div>
        <el-button type="primary" @click="showPasswordDialog = true">
          修改密码
        </el-button>
      </div>

      <el-divider />

      <div class="setting-item">
        <div class="setting-info">
          <h4>邮箱验证</h4>
          <p>验证您的邮箱地址以获得更好的安全保护</p>
        </div>
        <el-button @click="handleVerifyEmail" :loading="emailLoading">
          {{ userStore.userInfo?.email ? '重新验证' : '验证邮箱' }}
        </el-button>
      </div>
    </el-card>

    <el-card class="settings-section">
      <template #header>
        <div class="card-header">
          <span>隐私设置</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>个人资料可见性</h4>
          <p>控制您的个人资料对其他用户的可见程度</p>
        </div>
        <el-select 
          v-model="privacySettings.profileVisibility" 
          placeholder="请选择"
          @change="handlePrivacySettingsChange"
          :loading="privacySettingsLoading"
        >
          <el-option label="公开" :value="1" />
          <el-option label="仅关注者可见" :value="2" />
          <el-option label="私密" :value="3" />
        </el-select>
      </div>

      <el-divider />

      <div class="setting-item">
        <div class="setting-info">
          <h4>活动状态</h4>
          <p>是否显示您的在线状态</p>
        </div>
        <el-switch 
          v-model="privacySettings.showOnlineStatus" 
          @change="handlePrivacySettingsChange"
          :loading="privacySettingsLoading"
        />
      </div>
    </el-card>

    <el-card class="settings-section">
      <template #header>
        <div class="card-header">
          <span>通知设置</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>邮件通知</h4>
          <p>接收系统邮件通知</p>
        </div>
        <el-switch 
          v-model="notificationSettings.emailNotification" 
          @change="handleNotificationSettingsChange"
          :loading="notificationSettingsLoading"
        />
      </div>

      <el-divider />

      <div class="setting-item">
        <div class="setting-info">
          <h4>浏览器通知</h4>
          <p>接收浏览器推送通知</p>
        </div>
        <el-switch 
          v-model="notificationSettings.browserNotification" 
          @change="handleNotificationSettingsChange"
          :loading="notificationSettingsLoading"
        />
      </div>

      <el-divider />

      <div class="setting-item">
        <div class="setting-info">
          <h4>消息提示音</h4>
          <p>新消息时播放提示音</p>
        </div>
        <el-switch 
          v-model="notificationSettings.soundNotification" 
          @change="handleNotificationSettingsChange"
          :loading="notificationSettingsLoading"
        />
      </div>
    </el-card>

    <el-card class="settings-section">
      <template #header>
        <div class="card-header">
          <span>私信设置</span>
        </div>
      </template>

      <div class="setting-item">
        <div class="setting-info">
          <h4>谁可以给我发私信</h4>
          <p>设置允许哪些用户向您发送私信</p>
        </div>
        <el-select 
          v-model="messageSettings.dmPermission" 
          placeholder="请选择"
          @change="handleMessageSettingsChange"
          :loading="messageSettingsLoading"
        >
          <el-option label="所有人" value="all" />
          <el-option label="我关注的人" value="following" />
          <el-option label="互相关注" value="mutual" />
        </el-select>
      </div>

      <el-divider />

      <div class="setting-item">
        <div class="setting-info">
          <h4>允许陌生人私信</h4>
          <p>关闭后，陌生人只能发送一条打招呼消息</p>
        </div>
        <el-switch 
          v-model="messageSettings.allowStrangerMessage" 
          @change="handleMessageSettingsChange"
          :loading="messageSettingsLoading"
        />
      </div>

      <el-divider />

      <div class="setting-item">
        <div class="setting-info">
          <h4>新消息通知</h4>
          <p>收到新私信时显示通知</p>
        </div>
        <el-switch 
          v-model="messageSettings.dmNotification" 
          @change="handleMessageSettingsChange"
          :loading="messageSettingsLoading"
        />
      </div>
    </el-card>

    <el-card class="settings-section">
      <template #header>
        <div class="card-header">
          <span>账户操作</span>
        </div>
      </template>

      <div class="setting-item danger">
        <div class="setting-info">
          <h4>注销账户</h4>
          <p>永久删除您的账户和所有数据，此操作不可撤销</p>
        </div>
        <el-button type="danger" @click="showDeleteDialog = true">
          注销账户
        </el-button>
      </div>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="500px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 注销账户确认对话框 -->
    <el-dialog
      v-model="showDeleteDialog"
      title="确认注销账户"
      width="500px"
    >
      <div class="delete-warning">
        <el-alert
          title="警告"
          description="注销账户将永久删除您的所有数据，包括帖子、评论、关注关系等。此操作不可撤销！"
          type="warning"
          :closable="false"
        />
        <div class="confirm-text">
          <p>请在下方输入您的密码以确认注销：</p>
          <el-input
            v-model="deleteConfirmPassword"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDeleteDialog = false">取消</el-button>
          <el-button type="danger" @click="handleDeleteAccount" :loading="deleteLoading">
            确认注销
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/module/user'
import { 
  changePassword, 
  getUserSettings, 
  updatePrivacySettings, 
  updateNotificationSettings,
  sendEmailVerify,
  verifyEmail,
  deleteAccount
} from '@/api/user'
import { getUserMessageSettings, updateUserMessageSettings } from '@/api/message'
import { validateApiResponse } from '@/utils/typeGuards'

// 类型定义
interface PasswordForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

interface PrivacySettings {
  profileVisibility: number
  showOnlineStatus: boolean
}

interface NotificationSettings {
  emailNotification: boolean
  browserNotification: boolean
  soundNotification: boolean
}

interface MessageSettings {
  allowStrangerMessage: boolean
  dmPermission: 'all' | 'following' | 'mutual'
  dmNotification: boolean
}

// API 响应类型
interface MessageSettingsResponse {
  allowStrangerMessage: boolean
  dmPermission: 'all' | 'following' | 'mutual'
  dmNotification: boolean
}

interface UserSettingsResponse {
  privacySettings?: PrivacySettings
  notificationSettings?: NotificationSettings
}

const router = useRouter()
const userStore = useUserStore()

// 对话框状态
const showPasswordDialog = ref<boolean>(false)
const showDeleteDialog = ref<boolean>(false)

// 加载状态
const passwordLoading = ref<boolean>(false)
const emailLoading = ref<boolean>(false)
const deleteLoading = ref<boolean>(false)

// 表单引用
const passwordFormRef = ref<FormInstance | null>(null)

// 密码表单
const passwordForm = reactive<PasswordForm>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 确认删除密码
const deleteConfirmPassword = ref<string>('')

// 隐私设置
const privacySettings = ref<PrivacySettings>({
  profileVisibility: 1, // 1: 公开, 2: 仅关注者, 3: 私密
  showOnlineStatus: true
})

// 通知设置
const notificationSettings = ref<NotificationSettings>({
  emailNotification: true,
  browserNotification: true,
  soundNotification: false
})

// 私信设置
const messageSettings = ref<MessageSettings>({
  allowStrangerMessage: true,
  dmPermission: 'all',
  dmNotification: true
})

// 私信设置加载状态
const messageSettingsLoading = ref<boolean>(false)
// 防抖定时器
const saveSettingsTimer = ref<ReturnType<typeof setTimeout> | null>(null)

// 隐私设置和通知设置加载状态
const privacySettingsLoading = ref<boolean>(false)
const notificationSettingsLoading = ref<boolean>(false)
const privacySettingsTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const notificationSettingsTimer = ref<ReturnType<typeof setTimeout> | null>(null)

// 密码验证规则
const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: (error?: Error) => void) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 修改密码
const handleChangePassword = async (): Promise<void> => {
  if (!passwordFormRef.value) return

  passwordLoading.value = true

  try {
    await passwordFormRef.value.validate()

    const response = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })

    const result = validateApiResponse<{ success: boolean }>(response)
    
    if (result) {
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  } catch (error) {
    // 修改密码失败
  } finally {
    passwordLoading.value = false
  }
}

// 验证邮箱
const handleVerifyEmail = async (): Promise<void> => {
  if (!userStore.userInfo?.email) {
    ElMessage.warning('请先设置邮箱')
    return
  }

  emailLoading.value = true

  try {
    const response = await sendEmailVerify(userStore.userInfo.email)
    const result = validateApiResponse<{ success: boolean }>(response)
    
    if (result) {
      ElMessage.success('验证邮件已发送，请检查您的邮箱')
    }
  } catch (error: any) {
    if (error.response?.data?.info) {
      ElMessage.error(error.response.data.info)
    } else {
      ElMessage.error('发送验证邮件失败')
    }
  } finally {
    emailLoading.value = false
  }
}

// 注销账户
const handleDeleteAccount = async (): Promise<void> => {
  if (!deleteConfirmPassword.value) {
    ElMessage.error('请输入密码')
    return
  }

  deleteLoading.value = true

  try {
    const response = await deleteAccount(deleteConfirmPassword.value)
    const result = validateApiResponse<{ success: boolean }>(response)
    
    if (result) {
      ElMessage.success('账户注销成功')
      showDeleteDialog.value = false
      deleteConfirmPassword.value = ''

      // 退出登录并跳转到首页
      await userStore.logoutAction()
      router.push('/')
    }
  } catch (error: any) {
    if (error.response?.data?.info) {
      ElMessage.error(error.response.data.info)
    } else {
      ElMessage.error('注销账户失败')
    }
  } finally {
    deleteLoading.value = false
  }
}

// 加载用户私信设置
const loadMessageSettings = async (): Promise<void> => {
  messageSettingsLoading.value = true
  try {
    const response = await getUserMessageSettings()
    const data = validateApiResponse<MessageSettingsResponse>(response)
    
    if (data) {
      messageSettings.value = {
        allowStrangerMessage: data.allowStrangerMessage ?? true,
        dmPermission: data.dmPermission ?? 'all',
        dmNotification: data.dmNotification ?? true
      }
    }
  } catch (error) {
    // 加载失败
  } finally {
    messageSettingsLoading.value = false
  }
}

// 保存用户私信设置（带防抖和乐观更新）
const handleMessageSettingsChange = (): void => {
  // 立即设置loading状态，防止用户重复点击
  messageSettingsLoading.value = true
  
  // 保存旧值用于回滚
  const oldValue = { ...messageSettings.value }
  
  // 清除之前的定时器
  if (saveSettingsTimer.value) {
    clearTimeout(saveSettingsTimer.value)
  }
  
  // 设置新的定时器，300ms后执行保存
  saveSettingsTimer.value = setTimeout(async () => {
    try {
      const response = await updateUserMessageSettings({
        allowStrangerMessage: messageSettings.value.allowStrangerMessage,
        dmPermission: messageSettings.value.dmPermission,
        dmNotification: messageSettings.value.dmNotification
      })
      
      const result = validateApiResponse<{ success: boolean }>(response)
      
      if (result) {
        ElMessage.success('设置已保存')
      } else {
        // 回滚到旧值
        messageSettings.value = oldValue
      }
    } catch (error) {
      // 回滚到旧值
      messageSettings.value = oldValue
    } finally {
      messageSettingsLoading.value = false
      saveSettingsTimer.value = null
    }
  }, 300)
}

// 保存隐私设置（带防抖和乐观更新）
const handlePrivacySettingsChange = (): void => {
  // 立即设置loading状态
  privacySettingsLoading.value = true
  
  // 保存旧值用于回滚
  const oldValue = { ...privacySettings.value }
  
  // 清除之前的定时器
  if (privacySettingsTimer.value) {
    clearTimeout(privacySettingsTimer.value)
  }
  
  // 设置新的定时器，300ms后执行保存
  privacySettingsTimer.value = setTimeout(async () => {
    try {
      const response = await updatePrivacySettings({
        profileVisibility: privacySettings.value.profileVisibility,
        showOnlineStatus: privacySettings.value.showOnlineStatus
      })
      
      const result = validateApiResponse<{ success: boolean }>(response)
      
      if (result) {
        ElMessage.success('设置已保存')
      } else {
        // 回滚到旧值
        privacySettings.value = oldValue
      }
    } catch (error) {
      // 回滚到旧值
      privacySettings.value = oldValue
    } finally {
      privacySettingsLoading.value = false
      privacySettingsTimer.value = null
    }
  }, 300)
}

// 保存通知设置（带防抖和乐观更新）
const handleNotificationSettingsChange = (): void => {
  // 立即设置loading状态
  notificationSettingsLoading.value = true
  
  // 保存旧值用于回滚
  const oldValue = { ...notificationSettings.value }
  
  // 清除之前的定时器
  if (notificationSettingsTimer.value) {
    clearTimeout(notificationSettingsTimer.value)
  }
  
  // 设置新的定时器，300ms后执行保存
  notificationSettingsTimer.value = setTimeout(async () => {
    try {
      const response = await updateNotificationSettings({
        emailNotification: notificationSettings.value.emailNotification,
        browserNotification: notificationSettings.value.browserNotification,
        soundNotification: notificationSettings.value.soundNotification
      })
      
      const result = validateApiResponse<{ success: boolean }>(response)
      
      if (result) {
        ElMessage.success('设置已保存')
      } else {
        // 回滚到旧值
        notificationSettings.value = oldValue
      }
    } catch (error) {
      // 回滚到旧值
      notificationSettings.value = oldValue
    } finally {
      notificationSettingsLoading.value = false
      notificationSettingsTimer.value = null
    }
  }, 300)
}

// 加载用户设置
const loadUserSettings = async (): Promise<void> => {
  try {
    const response = await getUserSettings()
    const data = validateApiResponse<UserSettingsResponse>(response)
    
    if (data) {
      // 更新隐私设置
      if (data.privacySettings) {
        privacySettings.value = {
          profileVisibility: data.privacySettings.profileVisibility ?? 1,
          showOnlineStatus: data.privacySettings.showOnlineStatus ?? true
        }
      }
      
      // 更新通知设置
      if (data.notificationSettings) {
        notificationSettings.value = {
          emailNotification: data.notificationSettings.emailNotification ?? true,
          browserNotification: data.notificationSettings.browserNotification ?? true,
          soundNotification: data.notificationSettings.soundNotification ?? false
        }
      }
    }
  } catch (error) {
    // 不显示错误消息，使用默认值
  }
}

// 页面加载时检查登录状态并加载设置
onMounted(async () => {
  if (!userStore.isAuthenticated) {
    router.push('/')
    return
  }
  
  // 加载用户设置
  await loadUserSettings()
  
  // 加载私信设置
  await loadMessageSettings()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (saveSettingsTimer.value) {
    clearTimeout(saveSettingsTimer.value)
    saveSettingsTimer.value = null
  }
  if (privacySettingsTimer.value) {
    clearTimeout(privacySettingsTimer.value)
    privacySettingsTimer.value = null
  }
  if (notificationSettingsTimer.value) {
    clearTimeout(notificationSettingsTimer.value)
    notificationSettingsTimer.value = null
  }
})
</script>

<style scoped>
.settings-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.settings-header {
  text-align: center;
  margin-bottom: 40px;
}

.settings-header h2 {
  font-size: 32px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px;
}

.settings-header p {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
}

.settings-section {
  margin-bottom: 24px;
}

.card-header {
  font-weight: 600;
  color: #2c3e50;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.setting-item.danger {
  border-left: 4px solid #f56c6c;
  padding-left: 16px;
  margin: 16px 0;
}

.setting-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
}

.setting-info p {
  margin: 0;
  font-size: 14px;
  color: #7f8c8d;
}

.delete-warning {
  margin-bottom: 20px;
}

.confirm-text {
  margin-top: 20px;
}

.confirm-text p {
  margin: 0 0 12px;
  color: #666;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-select) {
  width: 200px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #2c3e50;
}

@media screen and (max-width: 768px) {
  .settings-container {
    padding: 0 16px;
    margin: 20px auto;
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  :deep(.el-select) {
    width: 100%;
  }
}
</style>
