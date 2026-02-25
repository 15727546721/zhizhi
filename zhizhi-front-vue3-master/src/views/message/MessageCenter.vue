<template>
  <div class="message-center">
    <div class="message-container">
      <!-- 左侧导航 -->
      <div class="message-nav">
        <el-menu :default-active="activeTab" class="message-menu" @select="handleSelect">
          <el-menu-item index="my-message">
            <el-icon><Message /></el-icon>
            <span>我的消息</span>
          </el-menu-item>
          <el-menu-item index="replies">
            <el-icon><ChatDotRound /></el-icon>
            <span>回复我的</span>
          </el-menu-item>
          <el-menu-item index="at">
            <el-icon><UserFilled /></el-icon>
            <span>@ 我的</span>
          </el-menu-item>
          <el-menu-item index="received">
            <el-icon><Present /></el-icon>
            <span>收到的赞</span>
          </el-menu-item>
          <el-menu-item index="system">
            <el-icon><Bell /></el-icon>
            <span>系统通知</span>
          </el-menu-item>
          <el-menu-item index="private-message">
            <el-icon><ChatLineSquare /></el-icon>
            <span>私信</span>
          </el-menu-item>
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon>
            <span>消息设置</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容区 -->
      <div class="message-content">
        <!-- 标题栏（私信页面不显示） -->
        <div v-if="activeTab !== 'private-message'" class="message-header">
          <h2>{{ currentTitle }}</h2>
        </div>

        <!-- 私信 -->
        <template v-if="activeTab === 'private-message'">
          <PrivateMessagePanel />
        </template>

        <!-- 消息设置 -->
        <template v-else-if="activeTab === 'settings'">
          <div class="settings-form">
            <el-form :model="settings" label-width="120px">
              <el-form-item label="邮件通知">
                <el-switch v-model="settings.emailNotification" />
              </el-form-item>
              <el-form-item label="浏览器通知">
                <el-switch v-model="settings.browserNotification" />
              </el-form-item>
              <el-form-item label="消息提示音">
                <el-switch v-model="settings.messageSound" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateSettings">保存设置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>

        <!-- 消息列表 -->
        <template v-else>
          <div v-loading="loading">
            <div class="message-list" v-if="messages.length > 0">
              <div v-for="message in messages" :key="message.id" class="message-item">
                <div class="message-avatar">
                  <el-avatar :size="40" :src="message.avatar" />
                </div>
                <div class="message-info">
                  <div class="message-title">
                    <span class="username">{{ message.username }}</span>
                    <span class="action">{{ message.action }}</span>
                    <span class="target">{{ message.target }}</span>
                  </div>
                  <div class="message-preview" v-if="message.content">
                    {{ message.content }}
                  </div>
                  <div class="message-footer">
                    <span class="message-time">{{ message.time }}</span>
                    <div class="message-actions">
                      <el-button
                        v-if="!message.isRead"
                        link
                        size="small"
                        @click="handleMarkAsRead(message.id)"
                      >
                        标记已读
                      </el-button>
                      <el-button link size="small" @click="handleDelete(message.id)">
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else class="empty-state">
              <el-empty description="暂无消息" />
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Message, ChatDotRound, Bell, Setting, Present, UserFilled, ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PrivateMessagePanel from '@/components/message/PrivateMessagePanel.vue'

const activeTab = ref('my-message')
const messages = ref([])
const loading = ref(false)
const settings = ref({
  emailNotification: true,
  browserNotification: true,
  messageSound: true
})

// 标题映射
const titleMap = {
  'my-message': '我的消息',
  replies: '回复我的',
  at: '@ 我的',
  received: '收到的赞',
  system: '系统通知',
  'private-message': '私信',
  settings: '消息设置'
}

// 计算当前标题
const currentTitle = computed(() => titleMap[activeTab.value])

// 处理菜单选择
const handleSelect = (key) => {
  activeTab.value = key
  if (key === 'settings') {
    loadSettings()
  } else if (key !== 'private-message') {
    loadMessages(key)
  }
}

// 加载消息
const loadMessages = async (type) => {
  try {
    loading.value = true
    // 模拟API调用延迟，减少到200ms
    await new Promise((resolve) => setTimeout(resolve, 200))

    // 模拟不同类型的消息数据
    const mockMessages = {
      'my-message': [
        {
          id: 1,
          username: '胡秀英说号卡',
          action: '回复了你的评论',
          target: '',
          content: '下月2号会上架一个正龙卡套餐，月租跟瑞龙卡一样，但是流量升级到120G',
          time: '2024-11-30 16:10',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: false
        },
        {
          id: 2,
          username: '系统通知',
          action: '你的B站2024年度报告来啦！',
          target: '',
          content:
            '珍贵的回忆已经为你打包完毕！今年你登录B站站多少天？谁成为了你的最爱？戴着接收回顾2024，查收UP主送来的惊喜彩蛋 >>',
          time: '2024-12-20 20:00',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: true
        }
      ],
      replies: [
        {
          id: 3,
          username: '流量卡大忽悠',
          action: '回复了你的评论',
          target:
            '具体得看运营商申核时间的兄弟，运营商一般会在1-3天内完成申核的，申核通过后就会发货了，一般3-5天能收到卡，可以耐心等待一下看看',
          content: '感谢分享，这个信息很有帮助！',
          time: '2024-11-30 15:43',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: false
        }
      ],
      at: [
        {
          id: 4,
          username: '这这这小可爱',
          action: '@了你',
          target: '',
          content: '那怎么有人会阻这个类都写进简历[doge]',
          time: '2024-08-11 14:45',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: true
        }
      ],
      received: [
        {
          id: 5,
          username: '赢默',
          action: '赞了你的评论',
          target: '我还可以教你吗',
          time: '2025-01-03 07:34',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: false
        },
        {
          id: 6,
          username: '萌音瑜',
          action: '赞了你的评论',
          target: '',
          time: '2024-11-23 19:58',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: true
        }
      ],
      system: [
        {
          id: 7,
          username: '系统通知',
          action: '稿件举报结果反馈',
          target: '',
          content:
            '您好，我们已收到您对稿件【BV1vp421S7sf】的举报，将会重点关注该账号，感谢您对社区秩序的维护。',
          time: '2024-12-12 06:20',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: false
        },
        {
          id: 8,
          username: '系统通知',
          action: '正在直播S14总决赛：BLG vs T1!',
          target: '',
          content:
            'BLG战队首度闯入S赛总决赛舞台，时隔11年，全华班再次与T1在决赛舞台相遇，谁将加冕为王？11月2日22点，上6号直播间看S14总决赛，天选好礼送不停，高能观赛团更是惊喜不断，25万现金红包等你来抢！',
          time: '2024-11-02 21:00',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          isRead: true
        }
      ]
    }

    messages.value = mockMessages[type] || []
  } catch (error) {
    // 获取失败
  } finally {
    loading.value = false
  }
}

// 标记消息为已读
const handleMarkAsRead = async (messageId) => {
  try {
    // 模拟API调用延迟，减少到200ms
    await new Promise((resolve) => setTimeout(resolve, 200))
    ElMessage.success('已标记为已读')
    // 直接在前端更新消息状态
    const message = messages.value.find((m) => m.id === messageId)
    if (message) {
      message.isRead = true
    }
  } catch (error) {
    // 标记失败
  }
}

// 删除消息
const handleDelete = async (messageId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 模拟API调用延迟，减少到200ms
    await new Promise((resolve) => setTimeout(resolve, 200))
    // 直接在前端删除消息
    messages.value = messages.value.filter((m) => m.id !== messageId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 加载消息设置
const loadSettings = async () => {
  try {
    // 模拟API调用延迟，减少到200ms
    await new Promise((resolve) => setTimeout(resolve, 200))
    // 使用默认设置
    settings.value = {
      emailNotification: true,
      browserNotification: true,
      messageSound: true
    }
  } catch (error) {
    // 获取失败
  }
}

// 更新消息设置
const handleUpdateSettings = async () => {
  try {
    // 模拟API调用延迟
    await new Promise((resolve) => setTimeout(resolve, 500))
    ElMessage.success('设置已更新')
  } catch (error) {
    // 更新失败
  }
}

// 初始加载
loadMessages('my-message')
</script>

<style scoped>
.message-center {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.message-container {
  display: flex;
  gap: 20px;
  min-height: 600px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.message-nav {
  width: 200px;
  border-right: 1px solid #e6e6e6;
}

.message-menu {
  border-right: none;
}

.message-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* 私信面板占满内容区 */
.message-content :deep(.private-message-panel) {
  flex: 1;
  margin: -20px;
  margin-top: 0;
}

.message-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.message-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.message-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.message-info {
  flex: 1;
}

.message-title {
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  color: #333;
}

.action {
  color: #666;
  margin: 0 5px;
}

.target {
  color: #1890ff;
}

.message-preview {
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.message-time {
  color: #999;
  font-size: 12px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.message-actions {
  display: flex;
  gap: 12px;
}

.settings-form {
  max-width: 500px;
  margin: 20px auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}
</style>
