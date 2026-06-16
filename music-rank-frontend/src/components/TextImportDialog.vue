<template>
  <el-dialog v-model="visible" title="📝 批量导入歌曲" width="90%" @close="rawText = ''; activeTab = 'import'">
    <!-- 标签切换：导入 / 模板 -->
    <div class="import-tabs">
      <button :class="['itab', { on: activeTab === 'import' }]" @click="activeTab = 'import'">粘贴导入</button>
      <button :class="['itab', { on: activeTab === 'template' }]" @click="activeTab = 'template'">复制模板</button>
    </div>

    <!-- 导入面板 -->
    <div v-show="activeTab === 'import'">
      <p class="import-hint">
        支持三种格式自动识别。专辑和备注均为可选。中英文标点通用，⭐ 可省略，5星/5分/五星等价。
      </p>
      <el-input
        v-model="rawText"
        type="textarea"
        :rows="12"
        placeholder="粘贴你的音乐笔记文本..."
      />
    </div>

    <!-- 模板面板 -->
    <div v-show="activeTab === 'template'" class="template-panel">
      <div class="template-section">
        <h4>格式一：横杠风格</h4>
        <pre class="tpl-code" @click="copyTpl('tpl1')">5星
- 邓紫棋 - 一路逆风 - 新的心跳 "好听的歌"
- 杨丞琳 - 带我走 - 半熟宣言 "电视剧歌曲"</pre>
      </div>
      <div class="template-section">
        <h4>格式二：竖线风格</h4>
        <pre class="tpl-code" @click="copyTpl('tpl2')">⭐ 5星 | 邓紫棋 | 一路逆风 | 新的心跳 | "好听的歌"
⭐ 4星 | 杨丞琳 | 带我走 | 半熟宣言 | "电视剧歌曲"</pre>
      </div>
      <div class="template-section">
        <h4>格式三：文字风格</h4>
        <pre class="tpl-code" @click="copyTpl('tpl3')">5星
歌手：邓紫棋，歌名：一路逆风，专辑：新的心跳，备注：好听的歌
歌手：杨丞琳，歌名：带我走，专辑：半熟宣言，备注：电视剧歌曲</pre>
      </div>
      <p class="template-note">
        提示：点击模板自动复制。专辑和备注均可单独省略。⭐ 可省略，5星/5分/五星通用，中英文标点通用。
      </p>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-if="activeTab === 'import'" type="primary" :loading="parsing" @click="handleParse">解析并导入</el-button>
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
const activeTab = ref('import')
const parsing = ref(false)

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

// ===== 模板复制 =====
const tplCode = {
  tpl1: '5星\n- 邓紫棋 - 一路逆风 - 新的心跳 "好听的歌"\n- 杨丞琳 - 带我走 - 半熟宣言 "电视剧歌曲"',
  tpl2: '⭐ 5星 | 邓紫棋 | 一路逆风 | 新的心跳 | "好听的歌"\n⭐ 4星 | 杨丞琳 | 带我走 | 半熟宣言 | "电视剧歌曲"',
  tpl3: '5星\n歌手：邓紫棋，歌名：一路逆风，专辑：新的心跳，备注：好听的歌\n歌手：杨丞琳，歌名：带我走，专辑：半熟宣言，备注：电视剧歌曲'
}

async function copyTpl(key) {
  try {
    await navigator.clipboard.writeText(tplCode[key])
    ElMessage.success('模板已复制，切换到"粘贴导入"使用')
  } catch {
    ElMessage.warning('复制失败，请手动选中复制')
  }
}

// ===== 解析引擎 =====

/** 将中文数字转换为阿拉伯数字 */
function chineseToNumber(str) {
  const map = { '一': 1, '二': 2, '三': 3, '四': 4, '五': 5, '六': 6, '七': 7, '八': 8, '九': 9, '十': 10, '零': 0 }
  return map[str] ?? null
}

/** 从文本中提取星级 */
function extractStarRating(line) {
  const normalized = line
    .replace(/：/g, ':')
    .replace(/，/g, ',')
    .replace(/“/g, '"')
    .replace(/”/g, '"')

  const patterns = [
    /(?:⭐|🌟)?\s*([0-5](?:\.[05])?)\s*(?:星|分)/,
    /([一二三四五])\s*星/,
    /^[⭐🌟]?\s*([0-5])\s*$/,
  ]

  for (const re of patterns) {
    const m = normalized.match(re)
    if (m) {
      const val = chineseToNumber(m[1]) ?? parseFloat(m[1])
      if (!isNaN(val) && val >= 0 && val <= 5) return val
    }
  }
  return null
}

/** 解析整段文本 */
function parseText(text) {
  const lines = text.split('\n').map(l => l.trim()).filter(Boolean)
  const songs = []
  let currentStar = 0

  for (const line of lines) {
    if (line.startsWith('#') || line.startsWith('//')) continue

    // 尝试提取星级标题行
    const starFromLine = extractStarRating(line)
    if (starFromLine !== null && line.length < 25) {
      currentStar = starFromLine
      continue
    }

    // 格式三：文字风格
    const textSong = parseTextFormat(line)
    if (textSong) {
      if (currentStar > 0 && !textSong.starRating) textSong.starRating = currentStar
      songs.push(textSong)
      continue
    }

    // 格式一/二：横杠或竖线
    const dpSong = parseDashPipeFormat(line)
    if (dpSong) {
      if (currentStar > 0 && !dpSong.starRating) dpSong.starRating = currentStar
      songs.push(dpSong)
      continue
    }

    // 兜底
    const simple = parseSimpleFormat(line)
    if (simple && currentStar > 0) {
      simple.starRating = currentStar
      songs.push(simple)
    }
  }

  return songs
}

/** 文字风格: 歌手：xxx，歌名：xxx */
function parseTextFormat(line) {
  let l = line.replace(/：/g, ':').replace(/，/g, ',').replace(/；/g, ';')

  const artistM = l.match(/(?:歌手|artist|作者)\s*[:：]\s*(.+?)(?:\s*[,，;；]\s*(?:歌名|歌曲|title|专辑|album|备注|note|notes)\s*[:：]|\s*$)/)
  const titleM = l.match(/(?:歌名|歌曲|title)\s*[:：]\s*(.+?)(?:\s*[,，;；]\s*(?:歌手|artist|专辑|album|备注|note|notes)\s*[:：]|\s*$)/)
  const albumM = l.match(/(?:专辑|album)\s*[:：]\s*(.+?)(?:\s*[,，;；]\s*(?:歌手|artist|歌名|歌曲|title|备注|note|notes)\s*[:：]|\s*$)/)
  const notesM = l.match(/(?:备注|note|notes)\s*[:：]\s*(.+?)(?:\s*[,，;；]\s*(?:歌手|artist|歌名|歌曲|title|专辑|album)\s*[:：]|\s*$)/)

  if (artistM && titleM) {
    return {
      artist: artistM[1].trim(),
      title: titleM[1].trim(),
      album: (albumM?.[1] || '').trim(),
      notes: (notesM?.[1] || '').trim(),
      starRating: 0
    }
  }
  return null
}

/** 横杠/竖线风格: artist - title - album "notes" 或 artist | title | album | "notes" */
function parseDashPipeFormat(line) {
  let l = line
    .replace(/：/g, ':')
    .replace(/，/g, ',')
    .replace(/“/g, '"')
    .replace(/”/g, '"')

  // 提取末尾引号备注 "..."（不要求方括号包裹）
  let notes = ''
  l = l.replace(/\s*"([^"]*)"\s*$/, (_, n) => { notes = n.trim(); return '' })

  // 去掉行首 ⭐ 星级声明
  l = l.replace(/^⭐?\s*[0-5一二三四五](?:\.\d)?\s*(?:星|分)?\s*[|｜]\s*/, '')
  // 去掉行首横杠/数字序号
  l = l.replace(/^(?:[-–—]|\d+[.、．)]|\*)\s*/, '')

  // 确定分隔符
  const usePipe = /[|｜]/.test(l)
  const parts = l.split(usePipe ? /[|｜]/ : /[-–—]/).map(s => s.trim()).filter(Boolean)

  if (parts.length < 2) return null

  const artist = parts[0]
  const title = parts[1]
  // 第三个字段：横杠格式为专辑，竖线格式也可能是专辑
  const album = parts.length >= 3 ? parts[2] : ''
  // 第四个字段（仅竖线格式可能出现）
  if (!notes && parts.length >= 4) notes = parts[3]

  if (artist && title && artist.length > 1 && title.length > 1) {
    return { artist, title, album, notes, starRating: 0 }
  }
  return null
}

/** 简单兜底: 歌手 - 歌名 */
function parseSimpleFormat(line) {
  let l = line.replace(/^(?:[-–—]|\d+[.、．)]|\*)\s*/, '')
  // 提取末尾引号
  let notes = ''
  l = l.replace(/\s*"([^"]*)"\s*$/, (_, n) => { notes = n.trim(); return '' })
  const parts = l.split(/[-–—]/)
  if (parts.length >= 2) {
    const artist = parts[0].trim()
    const title = parts[1].trim()
    const album = parts.length >= 3 ? parts[2].trim() : ''
    if (artist.length > 1 && title.length > 1) {
      return { artist, title, album, notes, starRating: 0 }
    }
  }
  return null
}

// ===== 处理导入 =====
async function handleParse() {
  const text = rawText.value.trim()
  if (!text) return ElMessage.warning('请输入文本')

  parsing.value = true
  const songs = parseText(text)

  if (songs.length === 0) {
    parsing.value = false
    return ElMessage.error('未能识别出有效歌曲。请切换到"复制模板"查看支持的格式。')
  }

  // emit 成功后父组件会关闭弹窗并显示成功消息
  emit('import', songs)
  visible.value = false
  rawText.value = ''
  parsing.value = false
}
</script>

<style scoped>
.import-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 12px;
  line-height: 1.6;
}

.import-tabs {
  display: flex; gap: 0; margin-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}
.itab {
  padding: 8px 20px; font-size: 13px;
  background: none; border: none; cursor: pointer;
  color: var(--text-muted); font-family: inherit;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.itab.on {
  color: var(--primary); border-bottom-color: var(--primary);
  font-weight: 500;
}

.template-panel { padding: 4px 0; }
.template-section { margin-bottom: 16px; }
.template-section h4 {
  font-size: 13px; font-weight: 500; color: var(--text-secondary);
  margin-bottom: 8px;
}
.tpl-code {
  background: var(--bg-warm); border: 1px solid var(--border-light);
  border-radius: var(--radius-sm); padding: 12px 16px;
  font-size: 13px; color: var(--text); line-height: 1.8;
  font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;
  white-space: pre-wrap; word-break: break-all;
  cursor: pointer; transition: all 0.2s;
  user-select: all;
}
.tpl-code:hover {
  border-color: var(--primary);
  background: var(--primary-bg);
}
.template-note {
  font-size: 12px; color: var(--text-muted);
  margin-top: 16px; line-height: 1.5;
}

@media (max-width: 767px) {
  .tpl-code { font-size: 11px; padding: 10px 12px; }
}
</style>
