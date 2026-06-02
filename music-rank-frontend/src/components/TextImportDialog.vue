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
  const text = rawText.value.trim()
  if (!text) return ElMessage.warning('请输入文本')

  const lines = text.split('\n')
  const musicList = []
  let currentStar = 0.0

  // 更宽松的星级匹配
  const starRegex = /(?:⭐|🌟)?\s*([0-5](?:\.[05])?)\s*星?/
  // 支持多种分隔方式：- 歌手 - 歌名 / 歌手 - 歌名 / 1. 歌手 - 歌名 / 歌名 - 歌手
  const songRegex = /^\s*(?:[-–—]|\d+[.、．]|\*)\s*(.+?)\s*[-–—]\s*(.+)$/

  lines.forEach((line) => {
    line = line.trim()
    if (!line || line.startsWith('#')) return

    // 检测星级标题行
    const starMatch = line.match(starRegex)
    if (starMatch && line.length < 20) {
      const s = parseFloat(starMatch[1])
      if (!isNaN(s)) currentStar = s
      return
    }

    // 检测歌曲行
    const songMatch = line.match(songRegex)
    if (songMatch && currentStar > 0) {
      musicList.push({
        artist: songMatch[1].trim(),
        title: songMatch[2].trim(),
        starRating: currentStar,
        album: '',
        notes: ''
      })
      return
    }

    // 兜底：尝试用空格/分隔符智能拆分
    const parts = line.split(/[-–—]/)
    if (parts.length >= 2 && currentStar > 0) {
      const a = parts[0].trim()
      const b = parts[1].trim()
      if (a.length > 1 && b.length > 1) {
        musicList.push({
          artist: a,
          title: b,
          starRating: currentStar,
          album: '',
          notes: ''
        })
      }
    }
  })

  if (musicList.length === 0) {
    return ElMessage.error('未能识别出有效歌曲，支持格式：\n🌟 5星\n- 歌手 - 歌名\n或\n5星\n歌手 - 歌名')
  }

  try {
    await emit('import', musicList)
    visible.value = false
    rawText.value = ''
    ElMessage.success(`成功识别 ${musicList.length} 首歌曲`)
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
