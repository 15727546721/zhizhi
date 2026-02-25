<template>
  <div class="flex items-center">
    <template v-if="!isMobile">
      <!--搜索 -->
      <div class="setting-item" @click="showMenuSearch">
        <MenuSearch ref="menuSearchRef" />
      </div>
      <!--全屏 -->
      <div class="setting-item" @click="toggle">
        <svg-icon
          :icon-class="isFullscreen ? 'fullscreen-exit' : 'fullscreen'"
        />
      </div>
      <!-- 布局大小 -->
      <el-tooltip
        :content="$t('sizeSelect.tooltip')"
        effect="dark"
        placement="bottom"
      >
        <size-select class="setting-item" />
      </el-tooltip>

      <!-- 语言选择 -->
      <lang-select class="setting-item" />
    </template>

    <!-- 设置 -->
    <template v-if="defaultSettings.showSettings">
      <div class="setting-item" @click="settingStore.settingsVisible = true">
        <el-icon class="nav-icon"><Setting /></el-icon>
      </div>
    </template>

    <!-- 用户头像 -->
    <el-dropdown class="setting-item" trigger="click">
      <div class="avatar-container">
        <img
          :src="userStore.user.avatar + '?imageView2/1/w/80/h/80'"
          class="user-avatar"
        />
        <span class="username">{{ userStore.user.username }}</span>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="toProfile">
            <div class="dropdown-item">
              <el-icon><User /></el-icon>
              <span>{{ $t("navbar.profile") }}</span>
            </div>
          </el-dropdown-item>
          <a target="_blank" href="https://gitee.com/xu-wq/zhizhi">
            <el-dropdown-item>
              <div class="dropdown-item">
                <el-icon><Link /></el-icon>
                <span>{{ $t("navbar.gitee") }}</span>
              </div>
            </el-dropdown-item>
          </a>
          <el-dropdown-item divided @click="openDialog">
            <div class="dropdown-item">
              <el-icon><EditPen /></el-icon>
              <span>{{ $t("navbar.password") }}</span>
            </div>
          </el-dropdown-item>
          <el-dropdown-item divided @click="logout">
            <div class="dropdown-item">
              <el-icon><SwitchButton /></el-icon>
              <span>{{ $t("navbar.logout") }}</span>
            </div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>
<script setup lang="ts">
import {
  useAppStore,
  useTagsViewStore,
  useUserStore,
  useSettingsStore,
} from "@/store";
import defaultSettings from "@/settings";
import { DeviceEnum } from "@/enums/DeviceEnum";
import { PasswordData } from "@/api/user/types";
import { updateUserPassword } from "@/api/user";
import MenuSearch from "@/components/MenuSearch/index.vue";
import {
  Setting,
  User,
  SwitchButton,
  Link,
  EditPen
} from '@element-plus/icons-vue'

const appStore = useAppStore();
const tagsViewStore = useTagsViewStore();
const userStore = useUserStore();
const settingStore = useSettingsStore();

const router = useRouter();

const isMobile = computed(() => appStore.device === DeviceEnum.MOBILE);

const { isFullscreen, toggle } = useFullscreen();

const dialog = ref({
  visible: false,
});
const formRef = ref(ElForm);
const formData = reactive<PasswordData>({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const rules = reactive({
  oldPassword: [{ required: true, message: "旧密码不能为空", trigger: "blur" }],
  confirmPassword: [
    { required: true, message: "确认密码不能为空", trigger: "blur" },
  ],
  newPassword: [{ required: true, message: "新密码不能为空", trigger: "blur" }],
});

const menuSearchRef = ref();

/**
 * 打开表单弹窗
 */

function openDialog() {
  dialog.value.visible = true;
}

/**
 * 修改密码
 */
function submitForm() {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      1;
      updateUserPassword(formData)
        .then(() => {
          ElMessage.success("修改成功");
        })
        .finally(() => {
          dialog.value.visible = false;
          formData.oldPassword = "";
          formData.newPassword = "";
        });
    }
  });
}

/**
 * 注销
 */
function logout() {
  ElMessageBox.confirm("确定注销并退出系统吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
    lockScroll: false,
  }).then(() => {
    userStore
      .logout()
      .then(() => {
        tagsViewStore.delAllViews();
      })
      .then(() => {
        router.push(`/login`);
      });
  });
}

function showMenuSearch() {
  menuSearchRef.value.showMenuSearch();
}

function toProfile() {
  router.push("/profile");
}
</script>
<style lang="scss" scoped>
.setting-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: $navbar-height;
  padding: 0 10px;
  color: var(--el-text-color);
  cursor: pointer;

  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }
}

.nav-icon {
  font-size: 18px;
  color: #595959;
}

.layout-top,
.layout-mix {
  .setting-item,
  .nav-icon {
    color: var(--el-color-white);
  }
}

.dark .setting-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

.avatar-container {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 100%;
  cursor: pointer;
  
  &:hover {
    background: transparent;
  }
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
}

.username {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  margin-right: 4px;
}

:deep(.el-dropdown-menu) {
  padding: 4px 0;
  min-width: 120px;
}

:deep(.el-dropdown-menu__item) {
  padding: 0;
  line-height: 1;
  
  &:hover {
    background-color: #f5f5f5;
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  
  .el-icon {
    margin-right: 10px;
    font-size: 16px;
    color: #595959;
  }
  
  span {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.85);
  }
}

:deep(.el-dropdown-menu__item--divided) {
  margin-top: 4px;
  border-top: 1px solid #f0f0f0;
  
  &::before {
    height: 4px;
    background-color: #fff;
  }
}

a {
  display: block;
  
  &:hover {
    text-decoration: none;
  }
}
</style>
