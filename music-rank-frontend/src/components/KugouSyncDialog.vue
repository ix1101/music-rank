<template>
  <el-dialog
    v-model="visible"
    title="选择要导入的酷狗歌单"
    width="550px"
    @close="resetDialog"
  >
    <div class="dialog-header">
      <el-checkbox
        v-model="isAllSelected"
        @change="handleSelectAll"
        style="font-weight: 500"
      >
        全选
      </el-checkbox>
      <span class="selected-count">
        已选择
        <span class="count-highlight">{{ selectedPlaylists.length }}</span>
        / {{ playlists.length }} 个歌单
      </span>
    </div>

    <div class="playlist-list">
      <el-skeleton :loading="loading" :rows="5" animated>
        <el-checkbox-group v-model="selectedPlaylists">
          <div
            v-for="playlist in playlists"
            :key="playlist.id"
            class="playlist-item"
          >
            <el-checkbox :label="playlist.id" class="playlist-checkbox">
              <div class="playlist-content">
                <img
                  :src="playlist.coverUrl || 'https://picsum.photos/200/200'"
                  alt="封面"
                  class="playlist-cover"
                  loading="lazy"
                />
                <div class="playlist-info">
                  <div class="playlist-name">{{ playlist.name }}</div>
                  <div class="playlist-count">{{ playlist.trackCount }} 首歌曲</div>
                </div>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>

        <div v-if="playlists.length === 0 && !loading" class="empty-state">
          没有找到任何自建歌单
        </div>
      </el-skeleton>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="syncing"
        :disabled="selectedPlaylists.length === 0"
        @click="handleSync"
      >
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

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

// 监听选中变化，更新全选状态
watch(selectedPlaylists, (newVal) => {
  isAllSelected.value = newVal.length === props.playlists.length && props.playlists.length > 0
}, { deep: true })

function handleSelectAll(checked) {
  if (checked) {
    selectedPlaylists.value = props.playlists.map((item) => item.id)
  } else {
    selectedPlaylists.value = []
  }
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
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.selected-count {
  font-size: 13px;
  color: #606266;
}

.count-highlight {
  color: #165DFF;
  font-weight: 500;
}

.playlist-list {
  max-height: 500px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 12px;
}

.playlist-list::-webkit-scrollbar { width: 4px; }
.playlist-list::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 2px; }
.playlist-list:hover::-webkit-scrollbar-thumb { background: #c0c4cc; }
.playlist-list::-webkit-scrollbar-track { background: transparent; }

.playlist-item {
  margin-bottom: 12px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
}

.playlist-item:hover {
  background-color: #f0f5ff;
  border-color: #165DFF;
}

.playlist-checkbox {
  width: 100%;
  padding: 14px 16px;
}

.playlist-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.playlist-cover {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  background: #f5f7fa;
}

.playlist-info { flex: 1; min-width: 0; }

.playlist-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-count {
  font-size: 13px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}
</style>
