<template>
  <el-dialog v-model="visible" title="添加新歌曲" width="500px" @close="resetForm">
    <el-form :model="form" label-width="80px">
      <el-form-item label="歌手">
        <el-input v-model="form.artist" placeholder="请输入歌手名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="歌名">
        <el-input v-model="form.title" placeholder="请输入歌曲名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="专辑">
        <el-input v-model="form.album" placeholder="请输入专辑名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="评分">
        <el-rate v-model="form.starRating" :max="5" :step="0.5" show-score />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="请输入备注信息" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认添加</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const visible = ref(false)
const submitting = ref(false)

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

const defaultForm = () => ({
  artist: '',
  title: '',
  album: '',
  starRating: 0,
  notes: ''
})

const form = reactive(defaultForm())

function resetForm() {
  Object.assign(form, defaultForm())
}

async function submit() {
  if (!form.artist.trim() || !form.title.trim()) {
    return ElMessage.warning('歌手和歌名不能为空')
  }
  try {
    await emit('submit', { ...form })
    visible.value = false
  } catch {
    // 错误由父组件处理
  }
}
</script>
