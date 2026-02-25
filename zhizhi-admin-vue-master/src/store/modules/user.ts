import { loginApi, logoutApi } from "@/api/auth";
import {
  getUserInfoApi,
  updateUserInfo as updateUserInfoApi,
} from "@/api/user";
import { resetRouter } from "@/router";
import { store } from "@/store";

import { LoginData } from "@/api/auth/types";
import { UserInfo } from "@/api/user/types";

export const useUserStore = defineStore("user", () => {
  const user = ref<UserInfo>({
    roles: [],
    perms: [],
    roleId: null,
    intro: null,
  });

  /**
   * 登录
   *
   * @param {LoginData}
   * @returns
   */
  function login(loginData: LoginData) {
    return new Promise<void>((resolve, reject) => {
      loginApi(loginData)
        .then((response) => {
          const { data } = response;
          localStorage.setItem("accessToken", data);
          resolve();
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  // 获取信息(用户昵称、头像、角色集合、权限集合)
  function getUserInfo() {
    return new Promise<UserInfo>((resolve, reject) => {
      getUserInfoApi()
        .then(({ data }) => {
          if (!data) {
            ElMessage.error("验证失败，请重新登录");
            return;
          }
          if (!data.roles) {
            ElMessage.error("获取用户信息: 角色不能为空数组!");
            return;
          }
          Object.assign(user.value, { ...data });
          resolve(data);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  // user logout
  function logout() {
    return new Promise<void>((resolve, reject) => {
      logoutApi()
        .then(() => {
          localStorage.setItem("accessToken", "");
          resolve();
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  // remove token
  function resetToken() {
    return new Promise<void>((resolve) => {
      localStorage.setItem("accessToken", "");
      resetRouter();
      resolve();
    });
  }

  // 在actions中添加
  const updateUserInfo = async (userInfo: any) => {
    try {
      await updateUserInfoApi(userInfo);
      user.value = { ...user.value, ...userInfo };
      return Promise.resolve();
    } catch (error) {
      return Promise.reject(error);
    }
  };

  return {
    user,
    login,
    getUserInfo,
    logout,
    resetToken,
    updateUserInfo,
  };
});

// 非setup
export function useUserStoreHook() {
  return useUserStore(store);
}
