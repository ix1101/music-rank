<template>
  <el-dialog v-model="visible" title="📝 复制笔记文本一键识别" width="90%" @close="rawText = ''">
    <p class="import-hint">支持识别格式：🌟 5星 或 4.5星 作标题，下面紧跟 "- 歌手 - 歌名"</p>
    <el-input
      v-model="rawText"
      type="textarea"
      :rows="12"
      placeholder="请把你的音乐排名笔记复制粘贴到这里..."
    />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="parsing" @click="handleParse">解析并导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'import'])

const visible = ref(false)
const rawText = ref('')
const parsing = ref(false)

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

async function handleParse() {
  if (!rawText.value.trim()) {
    return ElMessage.warning('请输入文本')
  }

  const lines = rawText.value.split('\n')
  const musicList = []
  let currentStar = 0.0

  const starRegex = /([0-9.]+)\s*星/
  const songRegex = /^\s*-\s*(.+?)\s*-\s*(.+)$/

  lines.forEach((line) => {
    line = line.trim()
    if (!line) return

    const starMatch = line.match(starRegex)
    if (starMatch) {
      currentStar = parseFloat(starMatch[1])
      return
    }

    const songMatch = line.match(songRegex)
    if (songMatch && currentStar > 0) {
      musicList.push({
        artist: songMatch[1].trim(),
        title: songMatch[2].trim(),
        starRating: currentStar,
        album: '',
        notes: ''
      })
    }
  })

  if (musicList.length === 0) {
    return ElMessage.error('未能识别出有效歌曲，请检查文本格式是否包含 "数字星" 和 "- 歌手 - 歌名"')
  }

  try {
    await emit('import', musicList)
    visible.value = false
    rawText.value = ''
  } catch {
    // 错误由父组件处理
  }
}
</script>

<style scoped>
.import-hint {
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}
</style>
