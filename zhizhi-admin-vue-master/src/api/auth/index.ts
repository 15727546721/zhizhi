import request from "@/utils/request";
import { LoginData } from "./types";

// 登录API
export function loginApi(data: LoginData) {
  return request({
    url: "/api/system/login",
    method: "post",
    data,
  });
}

// 注销API
export function logoutApi() {
  return request({
    url: "/api/system/logout",
    method: "get",
  });
}
