<template>
  <el-dialog v-model="visible" title="🔐 酷狗音乐登录" width="450px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="手机号码">
        <el-input v-model="form.mobile" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="验证码">
        <div class="code-input">
          <el-input v-model="form.code" placeholder="请输入验证码" maxlength="6" />
          <el-button :disabled="!canSendCode || countdown > 0" @click="handleSendCode">
            {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleLogin">登录</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { syncTokenToBackend } from '../api/kugou'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'login-success'])

const visible = ref(false)
const submitting = ref(false)
const countdown = ref(0)
let timer = null

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

const form = reactive({
  mobile: '',
  code: ''
})

const canSendCode = computed(() => {
  return form.mobile.trim().length === 11
})

/**
 * 发送验证码
 * 使用 Vite 代理转发到 KuGouMusicApi
 */
async function handleSendCode() {
  if (!canSendCode.value) {
    return ElMessage.warning('请输入正确的11位手机号')
  }

  try {
    const res = await axios.get(`/kugou/captcha/sent?mobile=${form.mobile.trim()}`)
    if (res.data?.error_code === 0 || res.data?.status === 1) {
      ElMessage.success('验证码已发送，请注意查收短信')
      startCountdown()
    } else {
      ElMessage.error(res.data?.message || res.data?.error_msg || '发送失败，请稍后重试')
    }
  } catch {
    ElMessage.error('发送验证码失败，请检查网络连接')
  }
}

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

/**
 * 手机验证码登录
 */
async function handleLogin() {
  if (!canSendCode.value) {
    return ElMessage.warning('请输入正确的11位手机号')
  }
  if (form.code.trim().length !== 6) {
    return ElMessage.warning('请输入6位验证码')
  }

  submitting.value = true
  try {
    const res = await axios.get(`/kugou/login/cellphone?mobile=${form.mobile.trim()}&code=${form.code.trim()}`)

    if (res.data?.error_code === 0 || res.data?.status === 1) {
      const token = res.data?.data?.token || res.data?.token
      if (token) {
        localStorage.setItem('kugou_token', token)
        // 同步 token 到后端，方便多设备共享
        syncTokenToBackend(token).catch(() => {})
        emit('login-success', token)
        visible.value = false
        ElMessage.success('登录成功！Token 已同步到后端')
      } else {
        ElMessage.error('登录失败：未能获取到 token')
      }
    } else {
      ElMessage.error(res.data?.message || res.data?.error_msg || '登录失败')
    }
  } catch {
    ElMessage.error('登录失败，请检查网络连接')
  } finally {
    submitting.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}

.code-input .el-button {
  width: 120px;
  flex-shrink: 0;
}
</style>
