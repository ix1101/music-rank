<template>
  <el-dialog
    v-model="visible"
    title="选择要导入的酷狗歌单"
    width="550px"
    @close="resetDialog"
  >
    <div class="dialog-header">
      <el-checkbox v-model="isAllSelected" @change="handleSelectAll">全选</el-checkbox>
      <span class="selected-count">
        已选择 <span class="count-highlight">{{ selectedPlaylists.length }}</span> / {{ playlists.length }} 个歌单
      </span>
    </div>

    <div class="playlist-list">
      <el-skeleton :loading="loading" :rows="5" animated>
        <el-checkbox-group v-model="selectedPlaylists">
          <label
            v-for="playlist in playlists"
            :key="playlist.id"
            class="playlist-item"
            :class="{ 'is-checked': selectedPlaylists.includes(playlist.id) }"
          >
            <el-checkbox :label="playlist.id" class="item-checkbox" />
            <div class="item-cover">
              <img
                :src="playlist.coverUrl || 'https://picsum.photos/200/200'"
                alt="封面"
                loading="lazy"
              />
            </div>
            <div class="item-text">
              <div class="item-name">{{ playlist.name }}</div>
              <div class="item-count">{{ playlist.trackCount }} 首歌曲</div>
            </div>
          </label>
        </el-checkbox-group>

        <div v-if="playlists.length === 0 && !loading" class="empty-state">
          没有找到任何自建歌单
        </div>
      </el-skeleton>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="syncing" :disabled="selectedPlaylists.length === 0" @click="handleSync">
        开始导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  playlists: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  syncing: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'sync'])

const visible = ref(false)
const selectedPlaylists = ref([])
const isAllSelected = ref(false)

watch(() => props.modelValue, (v) => { visible.value = v })
watch(visible, (v) => { emit('update:modelValue', v) })

watch(selectedPlaylists, (v) => {
  isAllSelected.value = v.length === props.playlists.length && props.playlists.length > 0
}, { deep: true })

function handleSelectAll(checked) {
  selectedPlaylists.value = checked ? props.playlists.map(i => i.id) : []
}

function resetDialog() {
  selectedPlaylists.value = []
  isAllSelected.value = false
}

function handleSync() {
  emit('sync', [...selectedPlaylists.value])
}
</script>

<style scoped>
/* ---- 顶部 ---- */
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding-bottom: 12px; border-bottom: 1px solid #ebeef5; margin-bottom: 4px;
}
.selected-count { font-size: 13px; color: #606266; }
.count-highlight { color: #165DFF; font-weight: 600; }

/* ---- 列表容器 ---- */
.playlist-list {
  max-height: 460px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 4px;
}
.playlist-list::-webkit-scrollbar { width: 4px; }
.playlist-list::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 2px; }
.playlist-list::-webkit-scrollbar-track { background: transparent; }

/* ---- 列表项 ---- */
.playlist-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.15s;
}
.playlist-item:last-child { margin-bottom: 0; }

/* hover / 选中 — 整行蓝色背景 */
.playlist-item:hover { background: #f0f5ff; }
.playlist-item.is-checked { background: #eef2ff; }

/* 复选框 — 固定不 shrink */
.item-checkbox {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.item-checkbox :deep(.el-checkbox__label) { display: none; }

/* 封面 — 固定容器，禁止溢出 */
.item-cover {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f1f5f9;
}
.item-cover img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 文本 */
.item-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
}
.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-count {
  font-size: 12px;
  color: #909399;
}

/* 空状态 */
.empty-state {
  text-align: center; padding: 40px 0; color: #909399; font-size: 14px;
}
</style>
