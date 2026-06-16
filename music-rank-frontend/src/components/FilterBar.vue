<template>
  <div class="filter-bar">
    <!-- 快速筛选 chip + 下拉组合 -->
    <div class="filter-row">
      <!-- 全部清除 -->
      <button :class="['fchip', { on: !filterActive }]" @click="clearAll">全部</button>

      <!-- 评分状态下拉 -->
      <div class="fselect-wrap" :class="{ open: openHasStar }">
        <button class="fselect-btn" @click="openHasStar = !openHasStar">
          {{ hasStarLabel }}
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
        </button>
        <div v-if="openHasStar" class="fselect-drop">
          <button :class="{ on: f.hasStar === null }" @click="setHasStar(null)">全部</button>
          <button :class="{ on: f.hasStar === true }" @click="setHasStar(true)">已评分</button>
          <button :class="{ on: f.hasStar === false }" @click="setHasStar(false)">未评分</button>
        </div>
      </div>

      <!-- 星级区间下拉 -->
      <div class="fselect-wrap" :class="{ open: openStar }">
        <button class="fselect-btn" @click="openStar = !openStar">
          {{ starLabel }}
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
        </button>
        <div v-if="openStar" class="fselect-drop">
          <button :class="{ on: f.starRatingMin === null && f.starRatingMax === null }" @click="setStarRange(null, null)">全部</button>
          <button v-for="r in starRanges" :key="r.label"
            :class="{ on: f.starRatingMin === r.min && f.starRatingMax === r.max }"
            @click="setStarRange(r.min, r.max)"
          >{{ r.label }}</button>
        </div>
      </div>

      <!-- 歌手下拉 -->
      <div class="fselect-wrap" :class="{ open: openSinger }">
        <button class="fselect-btn" @click="openSinger = !openSinger">
          {{ f.singer || '歌手' }}
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
        </button>
        <div v-if="openSinger" class="fselect-drop fselect-drop--scroll">
          <button :class="{ on: !f.singer }" @click="setSinger('')">全部歌手</button>
          <button
            v-for="s in singers"
            :key="s"
            :class="{ on: f.singer === s }"
            @click="setSinger(s)"
          >{{ s }}</button>
        </div>
      </div>
    </div>

    <!-- 点击空白关闭下拉 -->
    <div v-if="openHasStar || openStar || openSinger" class="fselect-backdrop" @click="closeAll" />
  </div>
</template>

<script setup>
import { reactive, computed, ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  singers: { type: Array, default: () => [] },
  modelValue: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'filter-change'])

const f = reactive({
  singer: '',
  starRatingMin: null,
  starRatingMax: null,
  hasStar: null,
  ...props.modelValue
})

watch(() => props.modelValue, v => { if (v) Object.assign(f, v) }, { deep: true })

// 星级区间预设
const starRanges = [
  { label: '5.0 ~ 4.6', min: 4.6, max: 5.0 },
  { label: '4.5 ~ 4.1', min: 4.1, max: 4.5 },
  { label: '4.0 ~ 3.6', min: 3.6, max: 4.0 },
  { label: '3.5 ~ 3.1', min: 3.1, max: 3.5 },
  { label: '3.0 以下', min: null, max: 3.0 }
]

// 下拉开关
const openHasStar = ref(false)
const openStar = ref(false)
const openSinger = ref(false)

function closeAll() {
  openHasStar.value = false
  openStar.value = false
  openSinger.value = false
}

// 点击外部关闭
function onDocumentClick(e) {
  if (!e.target.closest('.fselect-wrap')) closeAll()
}
watch([openHasStar, openStar, openSinger], () => {
  if (openHasStar.value || openStar.value || openSinger.value) {
    document.addEventListener('click', onDocumentClick)
  } else {
    document.removeEventListener('click', onDocumentClick)
  }
})
onBeforeUnmount(() => document.removeEventListener('click', onDocumentClick))

const filterActive = computed(() =>
  f.singer || f.starRatingMin !== null || f.starRatingMax !== null || f.hasStar !== null
)

const hasStarLabel = computed(() => {
  if (f.hasStar === true) return '已评分'
  if (f.hasStar === false) return '未评分'
  return '评分'
})

const starLabel = computed(() => {
  if (f.starRatingMin === null && f.starRatingMax === null) return '星级'
  // 匹配预设
  const r = starRanges.find(r => r.min === f.starRatingMin && r.max === f.starRatingMax)
  if (r) return r.label
  // 自定义组合
  const parts = []
  if (f.starRatingMin !== null) parts.push('≥ ' + f.starRatingMin)
  if (f.starRatingMax !== null) parts.push('≤ ' + f.starRatingMax)
  return parts.join(' ')
})

function emitF() {
  emit('update:modelValue', {
    singer: f.singer,
    starRatingMin: f.starRatingMin,
    starRatingMax: f.starRatingMax,
    hasStar: f.hasStar
  })
  emit('filter-change')
}

function setHasStar(val) {
  f.hasStar = val
  openHasStar.value = false
  emitF()
}

function setStarRange(min, max) {
  f.starRatingMin = min
  f.starRatingMax = max
  openStar.value = false
  emitF()
}

function setSinger(val) {
  f.singer = val
  openSinger.value = false
  emitF()
}

function clearAll() {
  f.singer = ''
  f.starRatingMin = null
  f.starRatingMax = null
  f.hasStar = null
  closeAll()
  emitF()
}
</script>

<style scoped>
.filter-bar {
  position: relative;
  margin-bottom: 4px;
}

.filter-row {
  display: flex; gap: 6px; align-items: center;
  flex-wrap: wrap; padding-bottom: 2px;
}

/* === 快捷 chip === */
.fchip {
  flex-shrink: 0; height: 30px; padding: 0 14px; border-radius: 15px;
  font-size: 13px; font-weight: 400; color: var(--text-secondary);
  background: var(--surface); border: 1px solid var(--border-light);
  cursor: pointer; transition: all 0.2s; user-select: none;
  font-family: inherit; white-space: nowrap;
}
.fchip.on, .fchip:active {
  background: var(--primary-bg); color: var(--primary);
  border-color: var(--primary); font-weight: 500;
}

/* === 自定义下拉 === */
.fselect-wrap { position: relative; flex-shrink: 0; }
.fselect-btn {
  height: 30px; padding: 0 10px 0 14px; border-radius: 15px;
  font-size: 13px; font-weight: 400; color: var(--text-secondary);
  background: var(--surface); border: 1px solid var(--border-light);
  cursor: pointer; transition: all 0.2s; user-select: none;
  font-family: inherit; white-space: nowrap;
  display: flex; align-items: center; gap: 6px;
}
.fselect-btn:hover, .fselect-wrap.open .fselect-btn {
  background: var(--bg-warm);
  border-color: var(--border);
}
.fselect-btn svg { transition: transform 0.2s; flex-shrink: 0; opacity: 0.5; }
.fselect-wrap.open .fselect-btn svg { transform: rotate(180deg); }

.fselect-drop {
  position: absolute; top: calc(100% + 4px); left: 0;
  min-width: 120px;
  background: var(--surface); border: 1px solid var(--border-light);
  border-radius: 12px; box-shadow: var(--shadow-md);
  padding: 6px; z-index: 50;
  display: flex; flex-direction: column; gap: 2px;
}
.fselect-drop--scroll {
  max-height: 220px; overflow-y: auto;
}
.fselect-drop button {
  display: block; width: 100%; padding: 8px 14px; border: none;
  background: transparent; border-radius: 8px;
  font-size: 13px; color: var(--text-secondary); cursor: pointer;
  text-align: left; font-family: inherit; white-space: nowrap;
  transition: all 0.12s;
}
.fselect-drop button:hover { background: var(--bg-warm); }
.fselect-drop button.on {
  background: var(--primary-bg); color: var(--primary);
  font-weight: 500;
}

.fselect-backdrop { position: fixed; inset: 0; z-index: 40; }

/* 最后一个下拉（歌手）：右对齐，避免溢出屏幕 */
.fselect-wrap:last-child .fselect-drop { right: auto; left: 50%; transform: translateX(-50%); }

@media (min-width: 768px) {
  .fselect-drop--scroll { max-height: 300px; }
}
</style>
