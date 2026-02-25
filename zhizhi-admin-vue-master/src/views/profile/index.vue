<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="user-card" shadow="hover" v-loading="loading">
          <div class="avatar-wrapper">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="customUpload"
            >
              <el-avatar
                v-if="userInfo.avatar"
                :size="120"
                :src="userInfo.avatar"
                class="avatar"
              />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              <div class="upload-tip">点击上传头像</div>
            </el-upload>
          </div>

          <ul class="user-info-list">
            <li class="info-item">
              <span class="label">用户名</span>
              <span class="value">{{ userInfo.username }}</span>
            </li>
            <li class="info-item">
              <span class="label">注册时间</span>
              <el-tooltip :content="userInfo.createTime" placement="top">
                <span class="value">{{ userInfo.createTime }}</span>
              </el-tooltip>
            </li>
            <li class="info-item">
              <span class="label">状态</span>
              <span class="value">
                <el-tag
                  :type="userInfo.status === '1' ? 'success' : 'danger'"
                  size="small"
                >
                  {{ userInfo.status === "1" ? "正常" : "禁用" }}
                </el-tag>
              </span>
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :span="18" :xs="24">
        <el-card class="info-card" shadow="hover" v-loading="submitLoading">
          <template #header>
            <div class="card-header">
              <span class="title">基本资料</span>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="userInfo"
            :rules="rules"
            label-width="100px"
            :disabled="submitLoading"
          >
            <el-form-item label="用户昵称" prop="nickname">
              <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="submitLoading"
                @click="submitForm"
              >
                {{ submitLoading ? "保存中..." : "保存修改" }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card
          class="password-card"
          shadow="hover"
          v-loading="passwordLoading"
        >
          <template #header>
            <div class="card-header">
              <span class="title">修改密码</span>
            </div>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            :disabled="passwordLoading"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
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
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="passwordLoading"
                @click="handleUpdatePassword"
              >
                {{ passwordLoading ? "修改中..." : "修改密码" }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from "@/store";
import { ElMessage } from "element-plus";
import type { UploadProps, FormInstance, FormItemRule } from "element-plus";
import { uploadFile } from "@/api/file/index";
import { queryUserInfo, updateUserInfo, uploadAvatar } from "@/api/user";
import type { UserInfo, PasswordData } from "@/api/user/types";
import { debounce } from "lodash-es";

const userStore = useUserStore();
const loading = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();
const passwordLoading = ref(false);

const userInfo = reactive<UserInfo>({
  id: 0,
  username: "",
  nickname: "",
  avatar: "",
  email: "",
  createTime: "",
  status: "1",
});

const passwordForm = reactive<PasswordData>({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const rules: Partial<Record<string, FormItemRule[]>> = {
  nickname: [
    { required: true, message: "请输入用户昵称", trigger: "blur" },
    { min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur" },
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]{2,20}$/,
      message: "昵称只能包含中文、字母、数字、下划线和横杠",
      trigger: "blur",
    },
  ],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    {
      type: "email" as const,
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],
};

const passwordRules = {
  oldPassword: [
    { required: true, message: "请输入当前密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/,
      message: "密码必须包含大小写字母和数字",
      trigger: "blur",
    },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

// 获取用户信息
const getUserInfo = async () => {
  try {
    loading.value = true;
    const { data } = await queryUserInfo();
    Object.assign(userInfo, data);
  } catch (error: any) {
    ElMessage.error(error.message || "获取用户信息失败");
  } finally {
    loading.value = false;
  }
};

// 头像上传前校验
const beforeAvatarUpload: UploadProps["beforeUpload"] = (file) => {
  const isImage = /^image\/(jpeg|png|jpg|gif)$/.test(file.type);
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 修改自定义上传函数
const customUpload = async (params: any) => {
  try {
    loading.value = true;
    // 创建 FormData
    const formData = new FormData();
    formData.append("file", params.file);

    // 直接调用上传头像接口
    const { data } = await uploadAvatar(formData);
    userInfo.avatar = data;
    ElMessage.success("头像上传成功");

    // 更新store中的用户信息
    await userStore.getUserInfo();
  } catch (error: any) {
    ElMessage.error(error.message || "头像上传失败");
  } finally {
    loading.value = false;
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitLoading.value = true;
    await doUpdateUserInfo();
    ElMessage.success("保存成功");
    await getUserInfo(); // 刷新用户信息
  } catch (error: any) {
    ElMessage.error(error.message || "保存失败");
  } finally {
    submitLoading.value = false;
  }
};

// 修改更新用户信息的函数
const doUpdateUserInfo = async () => {
  try {
    await updateUserInfo({
      nickname: userInfo.nickname,
      email: userInfo.email,
      avatar: userInfo.avatar,
    });
  } catch (error: any) {
    throw error;
  }
};

// 创建防抖的提交函数
const debouncedSubmit = debounce(async () => {
  await submitForm();
}, 300);

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return;

  try {
    await passwordFormRef.value.validate();
    passwordLoading.value = true;
    await userStore.updatePassword(passwordForm);
    ElMessage.success("密码修改成功，请重新登录");
    // 清空表单
    passwordFormRef.value.resetFields();
    // 退出登录
    await userStore.logout();
  } catch (error: any) {
    if (error?.message) {
      ElMessage.error(error.message);
    }
  } finally {
    passwordLoading.value = false;
  }
};

onMounted(() => {
  getUserInfo();
});

// 组件卸载前清理
onBeforeUnmount(() => {
  debouncedSubmit.cancel();
});
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  background: #f5f7f9;
  min-height: calc(100vh - 84px);

  .user-card {
    border-radius: 8px;

    .avatar-wrapper {
      text-align: center;
      padding: 20px 0;

      .avatar-uploader {
        cursor: pointer;
        position: relative;
        display: inline-block;

        &:hover {
          .upload-tip {
            opacity: 1;
          }

          .avatar {
            filter: brightness(80%);
          }
        }

        .avatar {
          transition: all 0.3s ease;
        }

        .avatar-uploader-icon {
          font-size: 28px;
          color: #8c939d;
          width: 120px;
          height: 120px;
          border: 2px dashed var(--el-border-color);
          border-radius: 50%;
          display: flex;
          justify-content: center;
          align-items: center;
        }

        .upload-tip {
          position: absolute;
          bottom: -24px;
          left: 50%;
          transform: translateX(-50%);
          color: #909399;
          font-size: 12px;
          opacity: 0;
          transition: opacity 0.3s ease;
        }
      }
    }

    .user-info-list {
      padding: 0;
      margin: 20px 0 0;
      list-style: none;

      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 20px;
        border-bottom: 1px solid var(--el-border-color-lighter);

        &:last-child {
          border-bottom: none;
        }

        .label {
          color: #606266;
          font-weight: 500;
        }

        .value {
          color: #909399;
          cursor: default;
        }
      }
    }
  }

  .info-card,
  .password-card {
    border-radius: 8px;

    & + .password-card {
      margin-top: 20px;
    }

    .card-header {
      .title {
        font-size: 16px;
        font-weight: 500;
      }
    }

    :deep(.el-form) {
      max-width: 460px;
      margin: 0 auto;

      .el-form-item__label {
        font-weight: 500;
      }

      .el-input {
        .el-input__wrapper {
          box-shadow: 0 0 0 1px #dcdfe6 inset;

          &:hover {
            box-shadow: 0 0 0 1px var(--el-color-primary) inset;
          }
        }
      }

      .el-button {
        width: 120px;

        &.is-loading {
          background-color: var(--el-color-primary-light-3);
        }
      }
    }
  }
}
</style>
