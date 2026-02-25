/**
 * 用户数据验证工具
 */

interface ValidationResult {
  valid: boolean
  message: string
}

interface ValidationErrors {
  [key: string]: string
}

interface RegistrationValidationResult {
  valid: boolean
  errors: ValidationErrors
}

interface UserRegistrationData {
  username: string
  email: string
  password: string
  confirmPassword: string
  nickname?: string
}

interface UserProfileData {
  nickname?: string
  email?: string
  phone?: string
  description?: string
}

export function validateUsername(username: string | undefined): ValidationResult {
  if (!username) {
    return { valid: false, message: '用户名不能为空' }
  }

  if (username.length < 4 || username.length > 20) {
    return { valid: false, message: '用户名长度必须在4-20个字符之间' }
  }

  const usernameRegex = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/
  if (!usernameRegex.test(username)) {
    return { valid: false, message: '用户名只能包含字母、数字、下划线和中文' }
  }

  return { valid: true, message: '用户名格式正确' }
}

export function validateEmail(email: string | undefined): ValidationResult {
  if (!email) {
    return { valid: false, message: '邮箱不能为空' }
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    return { valid: false, message: '请输入正确的邮箱地址' }
  }

  return { valid: true, message: '邮箱格式正确' }
}

export function validatePassword(password: string | undefined, strict = false): ValidationResult {
  if (!password) {
    return { valid: false, message: '密码不能为空' }
  }

  if (password.length < 6) {
    return { valid: false, message: '密码长度不能少于6位' }
  }

  if (strict) {
    const hasUpperCase = /[A-Z]/.test(password)
    const hasLowerCase = /[a-z]/.test(password)
    const hasNumbers = /\d/.test(password)

    if (!hasUpperCase || !hasLowerCase || !hasNumbers) {
      return { valid: false, message: '密码必须包含大写字母、小写字母和数字' }
    }
  }

  return { valid: true, message: '密码格式正确' }
}


export function validatePhone(phone: string | undefined): ValidationResult {
  if (!phone) {
    return { valid: false, message: '手机号码不能为空' }
  }

  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phone)) {
    return { valid: false, message: '请输入正确的手机号码' }
  }

  return { valid: true, message: '手机号码格式正确' }
}

export function validateNickname(nickname: string | undefined): ValidationResult {
  if (!nickname) {
    return { valid: false, message: '昵称不能为空' }
  }

  if (nickname.length < 2 || nickname.length > 12) {
    return { valid: false, message: '昵称长度必须在2-12个字符之间' }
  }

  return { valid: true, message: '昵称格式正确' }
}

export function validateDescription(description: string | undefined): ValidationResult {
  if (description && description.length > 200) {
    return { valid: false, message: '个人简介不能超过200个字符' }
  }

  return { valid: true, message: '个人简介格式正确' }
}

export function validateUserRegistration(userData: UserRegistrationData): RegistrationValidationResult {
  const errors: ValidationErrors = {}

  const usernameResult = validateUsername(userData.username)
  if (!usernameResult.valid) {
    errors.username = usernameResult.message
  }

  const emailResult = validateEmail(userData.email)
  if (!emailResult.valid) {
    errors.email = emailResult.message
  }

  const passwordResult = validatePassword(userData.password, true)
  if (!passwordResult.valid) {
    errors.password = passwordResult.message
  }

  if (userData.password !== userData.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
  }

  if (userData.nickname) {
    const nicknameResult = validateNickname(userData.nickname)
    if (!nicknameResult.valid) {
      errors.nickname = nicknameResult.message
    }
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors
  }
}

export function validateUserProfile(userData: UserProfileData): RegistrationValidationResult {
  const errors: ValidationErrors = {}

  if (userData.nickname) {
    const nicknameResult = validateNickname(userData.nickname)
    if (!nicknameResult.valid) {
      errors.nickname = nicknameResult.message
    }
  }

  if (userData.email) {
    const emailResult = validateEmail(userData.email)
    if (!emailResult.valid) {
      errors.email = emailResult.message
    }
  }

  if (userData.phone) {
    const phoneResult = validatePhone(userData.phone)
    if (!phoneResult.valid) {
      errors.phone = phoneResult.message
    }
  }

  if (userData.description !== undefined) {
    const descriptionResult = validateDescription(userData.description)
    if (!descriptionResult.valid) {
      errors.description = descriptionResult.message
    }
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors
  }
}
