<template>
  <!-- 底部弹出层 (Sheet) -->
  <div :class="['sheet-overlay', { open: visible }]" @click.self="closeSheet">
    <div class="sheet-panel">
      <div class="sheet-handle"></div>
      <div class="sheet-title">{{ editing ? '编辑歌曲' : '添加歌曲' }}</div>

      <form @submit.prevent="submit">
        <!-- 歌手 + 歌名 -->
        <div class="field-row">
          <div class="field-group">
            <label class="field-label">歌手</label>
            <input class="field-input" v-model="form.artist" placeholder="例如: 周杰伦" maxlength="100" required />
          </div>
          <div class="field-group">
            <label class="field-label">歌名</label>
            <input class="field-input" v-model="form.title" placeholder="例如: 晴天" maxlength="100" required />
          </div>
        </div>

        <!-- 专辑 -->
        <div class="field-group">
          <label class="field-label">专辑</label>
          <input class="field-input" v-model="form.album" placeholder="专辑名称（可选）" maxlength="100" />
        </div>

        <!-- 评分 — 5颗星点击 + 精确数字输入 -->
        <div class="field-group">
          <label class="field-label">评分（点击星星或直接输入数字）</label>
          <div class="star-area">
            <div
              class="star-pick"
              ref="starRowRef"
              @click="onStarClick"
              @mousemove="onStarMove"
              @mouseleave="starHover = null"
            >
              <span
                v-for="n in 5" :key="n"
                class="star-char"
                :style="starStyle(n)"
              >★</span>
            </div>
            <input
              class="star-num"
              type="number"
              min="0" max="5" step="0.1"
              :value="starNum || ''"
              @input="onStarInput"
              @blur="onStarBlur"
              placeholder="0"
            />
            <span class="star-unit">星</span>
            <button v-if="starNum > 0" type="button" class="star-clear" @click="clearStars" title="清除">✕</button>
          </div>
        </div>

        <!-- 歌单 — 多选标签 -->
        <div class="field-group">
          <label class="field-label">所属歌单</label>
          <div class="tag-input-wrap" @click="focusTagInput">
            <span v-for="(pl, i) in selectedPlaylists" :key="'pl-'+pl.id" class="tag-chip playlist-chip">
              {{ pl.name }}
              <span class="tag-close" @click.stop="removePlaylist(i)">×</span>
            </span>
            <input
              ref="playlistInput"
              class="tag-input-inline"
              v-model="playlistInputText"
              placeholder="输入歌单名称，回车添加..."
              @keydown.enter.prevent="addPlaylist"
              @keydown.comma.prevent="addPlaylist"
              @keydown.backspace="handlePlaylistBackspace"
            />
          </div>
          <!-- 已有歌单快捷选择 -->
          <div v-if="availablePlaylists.length" class="quick-select">
            <span
              v-for="pl in availablePlaylists"
              :key="'av-'+pl.id"
              :class="['quick-chip', { on: selectedPlaylistIds.includes(pl.id) }]"
              @click="togglePlaylist(pl)"
            >{{ pl.name }}</span>
          </div>
        </div>

        <!-- 备注 — 标签输入 -->
        <div class="field-group">
          <label class="field-label">备注标签</label>
          <div class="tag-input-wrap" @click="focusNoteInput">
            <span v-for="(tag, i) in noteTags" :key="'nt-'+i" class="tag-chip note-chip">
              {{ tag }}
              <span class="tag-close" @click.stop="removeNoteTag(i)">×</span>
            </span>
            <input
              ref="noteInput"
              class="tag-input-inline"
              v-model="noteInputText"
              placeholder="输入标签，回车/空格/逗号分隔..."
              @keydown.enter.prevent="addNoteTag"
              @keydown.space.prevent="addNoteTag"
              @keydown.comma.prevent="addNoteTag"
              @keydown.backspace="handleNoteBackspace"
            />
          </div>
        </div>

        <!-- 按钮行 -->
        <div class="btn-row">
          <button type="button" class="btn ghost" @click="closeSheet">取消</button>
          <button type="submit" class="btn primary" :disabled="submitting">
            {{ editing ? '保存修改' : '确认添加' }}
          </button>
        </div>

        <!-- 删除按钮（编辑模式） -->
        <div v-if="editing" class="btn-row">
          <button type="button" class="btn danger" @click="$emit('delete-click')">删除这首歌</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  song: { type: Object, default: null },
  playlists: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'update:song', 'submit', 'delete-click'])

const visible = ref(false)
const submitting = ref(false)
const editing = computed(() => !!props.song && !!props.song.id)

// 表单
const defaultForm = () => ({
  artist: '',
  title: '',
  album: '',
  starRating: 0,
  notes: ''
})
const form = reactive(defaultForm())

// 星级（0~5，支持 0.1 精度）
const starNum = ref(0)
const starHover = ref(null)
const starRowRef = ref(null)

// 歌单多选
const selectedPlaylists = ref([])
const playlistInputText = ref('')
const playlistInput = ref(null)

// 备注标签
const noteTags = ref([])
const noteInputText = ref('')
const noteInput = ref(null)

// 可用歌单（供快捷选择）
const availablePlaylists = computed(() => props.playlists || [])

const selectedPlaylistIds = computed(() => selectedPlaylists.value.map(p => p.id))

// ==== 双向绑定 visible ====
watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

// ==== 预填编辑数据 ====
watch(() => props.song, (val) => {
  if (val && val.id) {
    form.artist = val.artist || ''
    form.title = val.title || ''
    form.album = val.album || ''
    // 保留原始评分
    starNum.value = Number(val.starRating) || 0
    // 解析备注为标签数组
    noteTags.value = parseNotesToTags(val.notes || '')
    // 歌单信息将在父组件传入后预填
    selectedPlaylists.value = []
  }
}, { immediate: true })

// 新建时重置
watch(visible, (val) => {
  if (val) {
    if (!props.song || !props.song.id) {
      Object.assign(form, defaultForm())
      starNum.value = 0
      noteTags.value = []
      selectedPlaylists.value = []
    }
  }
})

/** 外部设置歌单（由父组件在获取 song playlists 后调用） */
function setSongPlaylists(pls) {
  selectedPlaylists.value = pls || []
}

// ==== 歌单操作 ====
function addPlaylist() {
  const name = playlistInputText.value.trim()
  if (!name) return
  // 检查重复
  if (selectedPlaylists.value.some(p => p.name === name)) {
    playlistInputText.value = ''
    return
  }
  // 查找是否已有此歌单
  const existing = availablePlaylists.value.find(p => p.name === name)
  if (existing) {
    selectedPlaylists.value.push(existing)
  } else {
    // 新建歌单（只有名称）
    selectedPlaylists.value.push({ id: null, name, _new: true })
  }
  playlistInputText.value = ''
}

function removePlaylist(idx) {
  selectedPlaylists.value.splice(idx, 1)
}

function togglePlaylist(pl) {
  const idx = selectedPlaylists.value.findIndex(p => p.id === pl.id)
  if (idx >= 0) selectedPlaylists.value.splice(idx, 1)
  else selectedPlaylists.value.push(pl)
}

function handlePlaylistBackspace() {
  if (!playlistInputText.value && selectedPlaylists.value.length) {
    selectedPlaylists.value.pop()
  }
}

function focusTagInput() {
  nextTick(() => playlistInput.value?.focus())
}

// ==== 备注标签操作 ====
function addNoteTag() {
  const tag = noteInputText.value.trim()
  if (!tag) return
  if (!noteTags.value.includes(tag)) {
    noteTags.value.push(tag)
  }
  noteInputText.value = ''
}

function removeNoteTag(idx) {
  noteTags.value.splice(idx, 1)
}

function handleNoteBackspace() {
  if (!noteInputText.value && noteTags.value.length) {
    noteTags.value.pop()
  }
}

function focusNoteInput() {
  nextTick(() => noteInput.value?.focus())
}

// ==== 星级操作 ====

/** 根据当前评分计算第 n 颗星的填充样式 */
function starStyle(n) {
  const val = starHover.value ?? starNum.value
  if (val >= n) return { color: 'var(--star)' }
  if (val <= n - 1) return { color: 'var(--star-off)' }
  // 部分填充：使用渐变裁切
  const pct = (val - (n - 1)) * 100
  return {
    background: `linear-gradient(to right, var(--star) ${pct}%, var(--star-off) ${pct}%)`,
    '-webkit-background-clip': 'text',
    '-webkit-text-fill-color': 'transparent',
    backgroundClip: 'text'
  }
}

/** 根据鼠标/触摸位置计算星级（0.1 精度） */
function calcStarFromEvent(e) {
  const row = starRowRef.value
  if (!row) return null
  const stars = row.querySelectorAll('.star-char')
  for (const star of stars) {
    const rect = star.getBoundingClientRect()
    if (e.clientX >= rect.left && e.clientX <= rect.right) {
      const idx = [...stars].indexOf(star) + 1
      const frac = (e.clientX - rect.left) / rect.width
      const raw = idx - 1 + frac
      return Math.max(0, Math.min(5, Math.round(raw * 10) / 10))
    }
  }
  const lastRect = stars[stars.length - 1]?.getBoundingClientRect()
  if (lastRect && e.clientX > lastRect.right) return 5
  const firstRect = stars[0]?.getBoundingClientRect()
  if (firstRect && e.clientX < firstRect.left) return 0
  return null
}

function onStarClick(e) {
  const val = calcStarFromEvent(e)
  if (val !== null) starNum.value = val
}

function onStarMove(e) {
  starHover.value = calcStarFromEvent(e)
}

function onStarInput(e) {
  let val = parseFloat(e.target.value)
  if (isNaN(val) || val < 0) val = 0
  if (val > 5) val = 5
  starNum.value = Math.round(val * 10) / 10
}

function onStarBlur(e) {
  const val = starNum.value
  if (val > 0 && val <= 5) {
    e.target.value = val.toFixed(1)
  }
}

function clearStars() {
  starNum.value = 0
}

// ==== 工具 ====
function parseNotesToTags(notes) {
  if (!notes || !notes.trim()) return []
  // 按中文/英文逗号、中文/英文分号、空格分隔
  return notes.split(/[,，;；\s]+/).filter(Boolean)
}

// ==== 关闭 ====
function closeSheet() {
  visible.value = false
  emit('update:song', null)
}

// ==== 提交 ====
async function submit() {
  if (!form.artist.trim() || !form.title.trim()) {
    return ElMessage.warning('歌手和歌名不能为空')
  }

  // 自动转化输入框中剩余的文字为标签
  const remaining = noteInputText.value.trim()
  if (remaining) {
    if (!noteTags.value.includes(remaining)) {
      noteTags.value.push(remaining)
    }
    noteInputText.value = ''
  }

  submitting.value = true
  try {
    const payload = {
      ...form,
      starRating: starNum.value,
      notes: noteTags.value.join(' ')
    }
    if (props.song && props.song.id) {
      payload.id = props.song.id
    }
    // 携带歌单信息
    payload._playlists = selectedPlaylists.value.map(p => ({
      id: p.id,
      name: p.name,
      isNew: !!p._new
    }))
    await emit('submit', payload)
    visible.value = false
  } catch {
    // 错误由父组件处理
  } finally {
    submitting.value = false
  }
}

defineExpose({ setSongPlaylists })
</script>

<style scoped>
/* 歌单快捷选择 */
.quick-select {
  display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px;
}
.quick-chip {
  font-size: 12px; padding: 4px 12px; border-radius: 14px;
  background: var(--surface); color: var(--text-secondary);
  border: 1px solid var(--border-light); cursor: pointer;
  transition: all 0.2s; user-select: none;
}
.quick-chip.on, .quick-chip:active {
  background: var(--primary-bg); color: var(--primary);
  border-color: var(--primary); font-weight: 500;
}

.playlist-chip { background: var(--primary-bg); color: var(--primary); }
.note-chip { background: var(--tag-bg); color: var(--text-secondary); }

@media (min-width: 768px) {
  .sheet-panel {
    max-width: 520px;
  }
}
</style>
