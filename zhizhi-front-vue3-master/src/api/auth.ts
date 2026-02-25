import request from '@/utils/request'
import type { ApiResponse, User, LoginResponse } from '@/types'

interface SendVerifyCodeData {
  email: string
  scene?: 'register' | 'forgot' | 'bind'
}

interface RegisterData {
  email: string
  password: string
  confirmPassword: string
  username: string
  verifyCode: string
}

interface LoginData {
  email: string
  password: string
}

interface LoginWithCodeData {
  email: string
  verifyCode: string
}

interface ResetPasswordData {
  email: string
  verifyCode: string
  password: string
  confirmPassword: string
}

interface ChangePasswordData {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export function sendVerifyCode(data: SendVerifyCodeData): Promise<ApiResponse> {
  return request({
    url: '/user/send-verify-code',
    method: 'post',
    data: {
      email: data.email,
      scene: data.scene || 'register'
    }
  })
}

export function register(data: RegisterData): Promise<ApiResponse<User>> {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function login(data: LoginData): Promise<ApiResponse<LoginResponse>> {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}


export function loginWithCode(data: LoginWithCodeData): Promise<ApiResponse<LoginResponse>> {
  return request({
    url: '/user/login-with-code',
    method: 'post',
    data
  })
}

export function logout(): Promise<ApiResponse> {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

export function forgetPassword(data: { email: string }): Promise<ApiResponse> {
  return request({
    url: '/user/forgot-password',
    method: 'post',
    data
  })
}

export function resetPassword(data: ResetPasswordData): Promise<ApiResponse> {
  return request({
    url: '/user/reset-password-by-code',
    method: 'post',
    params: {
      email: data.email,
      code: data.verifyCode,
      newPassword: data.password,
      confirmPassword: data.confirmPassword
    }
  })
}

export function changePassword(data: ChangePasswordData): Promise<ApiResponse> {
  return request({
    url: '/user/change-password',
    method: 'post',
    data
  })
}
