<template>
  <div class="song-list-outer">
    <!-- 选中操作条 -->
    <div v-if="selectedCount > 0" class="sel-bar">
      <span>已选 <b>{{ selectedCount }}</b> 首</span>
      <button class="sel-del-btn" @click="$emit('batch-delete-click')">批量删除</button>
    </div>

    <!-- 表头行：全选 + 备注切换 -->
    <div class="list-header">
      <label class="header-check" @click.stop>
        <input
          type="checkbox"
          :checked="allChecked"
          :indeterminate="someChecked && !allChecked"
          @change="toggleAll"
        />
        <span class="check-mark">
          <svg v-if="allChecked" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
          <svg v-else-if="someChecked" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="3"><line x1="5" y1="12" x2="19" y2="12"/></svg>
        </span>
      </label>
      <span class="header-idx">#</span>
      <span class="header-score">评分</span>
      <span class="header-info">歌曲信息</span>
      <!-- 备注显示切换 -->
      <button class="notes-toggle" @click="notesAsTags = !notesAsTags" :title="notesAsTags ? '切换为文字备注' : '切换为标签备注'">
        <svg v-if="notesAsTags" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
        <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="4 7 4 4 20 4 20 7"/><line x1="9" y1="20" x2="15" y2="20"/><line x1="12" y1="4" x2="12" y2="20"/></svg>
      </button>
    </div>

    <!-- 歌曲列表 — 卡片行 -->
    <div class="song-list">
      <div
        v-for="(row, idx) in data"
        :key="row.id"
        class="song-row"
      >
        <!-- 勾选 -->
        <label class="row-check" @click.stop>
          <input
            type="checkbox"
            :checked="isChecked(row)"
            @change="toggleOne(row)"
          />
          <span class="check-mark">
            <svg v-if="isChecked(row)" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
          </span>
        </label>

        <!-- 序号 -->
        <span class="row-idx">{{ (currentPage - 1) * pageSize + idx + 1 }}</span>

        <!-- 评分 — 大字 + 小星星 -->
        <div class="row-score">
          <span :class="['score-big', { rated: Number(row.starRating) > 0 }]">
            {{ Number(row.starRating) > 0 ? Number(row.starRating).toFixed(1) : '—' }}
          </span>
          <!-- 小星星（非交互） -->
          <div v-if="Number(row.starRating) > 0" class="score-stars">
            <span v-for="s in 5" :key="s" :class="['mini-star', { fill: s <= Math.floor(Number(row.starRating)), half: s === Math.ceil(Number(row.starRating)) && Number(row.starRating) % 1 !== 0 }]">
              <template v-if="s <= Math.floor(Number(row.starRating))">★</template>
              <template v-else-if="s === Math.ceil(Number(row.starRating)) && Number(row.starRating) % 1 !== 0">★</template>
              <template v-else>☆</template>
            </span>
          </div>
        </div>

        <!-- 歌曲信息 -->
        <div class="row-info">
          <div class="info-title">{{ row.title }}</div>
          <div class="info-artist">
            {{ row.artist }}
            <template v-if="row.album"> · {{ row.album }}</template>
          </div>
          <!-- 歌单标签（始终显示） -->
          <div class="info-tags">
            <span
              v-for="pl in (row.playlistNames && row.playlistNames.length ? row.playlistNames : (row.category ? [row.category] : []))"
              :key="'pl-'+pl"
              class="meta-tag playlist-tag"
            >{{ pl }}</span>
            <!-- 标签模式的备注 -->
            <template v-if="notesAsTags">
              <span
                v-for="(note, ni) in parseNotes(row.notes)"
                :key="'note-'+ni"
                class="meta-tag note-tag"
              >{{ note }}</span>
            </template>
          </div>
        </div>

        <!-- 文字模式：备注在右侧 -->
        <div v-if="!notesAsTags && row.notes" class="row-notes-text">{{ row.notes }}</div>

        <!-- 操作按钮 — hover 显示 -->
        <div class="row-actions" @click.stop>
          <button class="act-btn edit-btn" @click="$emit('row-click', row)" title="编辑">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          </button>
          <button class="act-btn del-btn" @click="$emit('row-delete', row)" title="删除">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!data.length" class="empty-state">
        <div class="empty-icon">🎵</div>
        <div class="empty-title">没有匹配的歌曲</div>
        <div class="empty-desc">试试调整筛选条件或添加新歌曲</div>
      </div>
    </div>

    <!-- 分页 — 极简 -->
    <div class="song-pager">
      <button :disabled="currentPage <= 1" @click="$emit('update:currentPage', currentPage - 1); $emit('page-change')">‹</button>
      <template v-for="p in totalPages" :key="p">
        <button v-if="p === 1 || p === totalPages || Math.abs(p - currentPage) <= 1" :class="{ on: p === currentPage }" @click="$emit('update:currentPage', p); $emit('page-change')">{{ p }}</button>
        <span v-else-if="p === 2 || p === totalPages - 1" class="pager-dots">…</span>
      </template>
      <button :disabled="currentPage >= totalPages" @click="$emit('update:currentPage', currentPage + 1); $emit('page-change')">›</button>
      <span class="pager-info">共 {{ total }} 首</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  selectedIds: { type: Array, default: () => [] },
  selectedCount: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 8 }
})

const emit = defineEmits([
  'update:currentPage', 'update:pageSize',
  'page-change', 'selection-change', 'batch-delete-click',
  'star-change', 'row-delete', 'row-click', 'select-all'
])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

// 备注显示模式：true = 标签，false = 文字
const notesAsTags = ref(true)

const allChecked = computed(() => {
  return props.data.length > 0 && props.data.every(row => props.selectedIds.includes(row.id))
})
const someChecked = computed(() => {
  return props.data.some(row => props.selectedIds.includes(row.id))
})

function isChecked(row) {
  return props.selectedIds.includes(row.id)
}

function toggleOne(row) {
  const ids = [...props.selectedIds]
  const idx = ids.indexOf(row.id)
  if (idx >= 0) ids.splice(idx, 1)
  else ids.push(row.id)
  emit('selection-change', ids)
}

function toggleAll() {
  if (allChecked.value) {
    // 取消全选
    emit('selection-change', [])
  } else {
    // 全选：通知父组件获取全部匹配歌曲ID
    emit('select-all')
  }
}

/** 解析备注为标签数组（按空格和逗号分隔） */
function parseNotes(notes) {
  if (!notes || !notes.trim()) return []
  return notes.split(/[,，\s]+/).filter(Boolean)
}
</script>

<style scoped>
.song-list-outer {
  /* container */
}

/* 选中操作条 */
.sel-bar {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px; margin-bottom: 8px;
  background: var(--primary-bg); color: var(--primary);
  font-size: 13px; font-weight: 500;
  border-radius: var(--radius-sm);
}
.sel-bar b { font-weight: 700; }
.sel-del-btn {
  margin-left: auto; border: none; background: transparent;
  color: var(--danger); font-size: 13px; font-weight: 500;
  cursor: pointer; padding: 4px 8px; border-radius: 12px;
  transition: background 0.15s;
}
.sel-del-btn:hover { background: var(--danger-light); }

/* === 表头 === */
.list-header {
  display: flex; align-items: center; gap: 12px;
  padding: 8px 0 6px;
  border-bottom: 2px solid var(--border-light);
}
.header-check {
  width: 28px; height: 28px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; position: relative;
}
.header-check input { position: absolute; opacity: 0; width: 0; height: 0; }
.header-idx {
  width: 28px; text-align: center; font-size: 11px;
  color: var(--text-muted); font-weight: 500; flex-shrink: 0;
}
.header-score {
  width: 44px; text-align: center; font-size: 11px;
  color: var(--text-muted); font-weight: 500; flex-shrink: 0;
}
.header-info {
  flex: 1; font-size: 11px; color: var(--text-muted); font-weight: 500;
}
.notes-toggle {
  flex-shrink: 0; width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  border: none; background: transparent;
  color: var(--text-muted); cursor: pointer;
  border-radius: 50%; transition: all 0.15s;
}
.notes-toggle:hover { color: var(--primary); background: var(--primary-bg); }

/* 勾选标记（共用） */
.check-mark {
  width: 20px; height: 20px; border-radius: 50%;
  border: 1.5px solid var(--border); display: flex;
  align-items: center; justify-content: center;
  transition: all 0.2s;
}
input:checked ~ .check-mark {
  border-color: var(--primary); background: var(--primary-bg);
}

/* 歌曲行 */
.song-list { display: flex; flex-direction: column; }
.song-row {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 0; border-bottom: 1px solid var(--border-light);
  transition: background 0.2s, transform 0.12s;
}
.song-row:first-child { border-top: 1px solid var(--border-light); }
.song-row:hover { background: linear-gradient(90deg, transparent, #FFFDF9, transparent); }

/* 勾选 */
.row-check {
  width: 28px; height: 28px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; position: relative;
}
.row-check input { position: absolute; opacity: 0; width: 0; height: 0; }

/* 序号 */
.row-idx {
  width: 28px; text-align: center; font-size: 12px;
  color: var(--text-muted); font-weight: 300; flex-shrink: 0;
}

/* 评分 */
.row-score { width: 44px; text-align: center; flex-shrink: 0; }
.score-big {
  font-size: 20px; font-weight: 300; color: var(--text-muted); line-height: 1;
}
.score-big.rated { color: var(--star); font-weight: 400; }

/* 小星星 */
.score-stars {
  display: flex; justify-content: center; gap: 1px;
  margin-top: 2px; line-height: 1;
}
.mini-star {
  font-size: 10px; color: var(--star-off);
  transition: color 0.2s;
}
.mini-star.fill { color: var(--star); }
.mini-star.half {
  background: linear-gradient(90deg, var(--star) 50%, var(--star-off) 50%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 信息 */
.row-info { flex: 1; min-width: 0; }
.info-title {
  font-size: 15px; font-weight: 400; line-height: 1.4;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  color: var(--text);
}
.info-artist {
  font-size: 13px; color: var(--text-secondary); margin-top: 1px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.info-tags {
  display: flex; gap: 6px; align-items: center; margin-top: 3px;
  flex-wrap: wrap;
}
.meta-tag {
  font-size: 11px; padding: 1px 8px; border-radius: 8px;
  font-weight: 400; white-space: nowrap;
}
.playlist-tag { background: var(--primary-bg); color: var(--primary); }
.note-tag { background: var(--tag-bg); color: var(--text-secondary); }

/* 文字备注 — 歌曲信息右侧，自动换行 */
.row-notes-text {
  flex-shrink: 1; max-width: 220px;
  font-size: 12px; color: var(--text-muted);
  font-style: italic; line-height: 1.5;
  margin-left: 20px; padding-top: 3px;
  align-self: flex-start;
  word-break: break-word;
  overflow-wrap: break-word;
}

/* 操作按钮 */
.row-actions { display: flex; gap: 2px; flex-shrink: 0; opacity: 0; transition: opacity 0.2s; }
.song-row:hover .row-actions { opacity: 1; }
.act-btn {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: 50%; cursor: pointer; color: var(--text-muted);
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.act-btn:active { background: #F0EBE2; color: var(--text); }
.edit-btn:hover { color: var(--primary); background: var(--primary-bg); }
.del-btn:hover { color: var(--danger); background: var(--danger-light); }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 20px; }
.empty-icon { font-size: 40px; margin-bottom: 12px; opacity: 0.4; }
.empty-title { font-size: 14px; font-weight: 400; color: var(--text-secondary); margin-bottom: 4px; }
.empty-desc { font-size: 12px; color: var(--text-muted); }

/* 分页 */
.song-pager {
  display: flex; align-items: center; justify-content: center;
  gap: 6px; padding: 32px 0 0;
}
.song-pager button, .song-pager .pager-dots {
  height: 32px; min-width: 32px; display: flex;
  align-items: center; justify-content: center;
  border: 1px solid var(--border-light); border-radius: 50%;
  font-size: 13px; background: var(--surface); color: var(--text-secondary);
  cursor: pointer; transition: all 0.15s; user-select: none;
  font-family: inherit;
}
.song-pager button.on {
  background: var(--text); color: #fff; border-color: var(--text); font-weight: 500;
}
.song-pager button:disabled { opacity: 0.2; pointer-events: none; }
.song-pager button:hover:not(:disabled):not(.on) { background: var(--bg-warm); }
.pager-dots { border: none !important; background: transparent !important; cursor: default; color: var(--text-muted); }
.pager-info {
  border: none; background: transparent; color: var(--text-muted);
  font-size: 12px; padding: 0 8px; cursor: default;
}

/* 移动端：操作按钮始终可见（触摸设备无 hover） */
@media (max-width: 767px) {
  .row-actions { opacity: 1; }
  .song-row { gap: 6px; padding: 12px 0; }
  .row-check { width: 24px; height: 24px; }
  .row-idx { width: 20px; font-size: 10px; }
  .row-score { width: 34px; }
  .score-big { font-size: 17px; }
  .mini-star { font-size: 8px; }
  .list-header { gap: 6px; }
  .header-check { width: 24px; height: 24px; }
  .header-idx { width: 20px; }
  .header-score { width: 34px; }
  /* 备注：手机端缩小，保持在行内 */
  .row-notes-text {
    max-width: 72px; margin-left: 6px;
    font-size: 10px; line-height: 1.4;
    padding-top: 1px;
  }
  /* 操作按钮缩小 */
  .act-btn { width: 28px; height: 28px; }
  .act-btn svg { width: 12px; height: 12px; }
}
</style>
