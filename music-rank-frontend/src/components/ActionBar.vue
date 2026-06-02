<template>
  <div class="action-bar">
    <el-input
      v-model="localKeyword"
      placeholder="🔍 搜索歌名或歌手..."
      clearable
      @input="emitSearch"
      class="search-input"
      :prefix-icon="null"
    />
    <div class="btn-group">
      <el-button v-if="!isLoggedIn" type="info" plain @click="$emit('login-click')" size="default">
        🔐 登录酷狗
      </el-button>
      <el-button v-else type="success" plain @click="$emit('logout-click')" size="default">
        🚪 退出
      </el-button>
      <el-button type="primary" @click="$emit('sync-click')" :loading="syncing" size="default">
        🔄 同步酷狗歌单
      </el-button>
      <el-button type="success" @click="$emit('add-click')" size="default">＋ 添加歌曲</el-button>
      <el-button @click="$emit('import-click')" size="default">📝 文本导入</el-button>
      <el-button type="danger" plain :disabled="selectedCount === 0" @click="$emit('batch-delete-click')" size="default">
        🗑 删除 ({{ selectedCount }})
      </el-button>
      <el-button @click="$emit('export-click')" size="default">📋 复制导出</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  keyword: { type: String, default: '' },
  isLoggedIn: { type: Boolean, default: false },
  syncing: { type: Boolean, default: false },
  selectedCount: { type: Number, default: 0 }
})

const emit = defineEmits([
  'update:keyword', 'search',
  'login-click', 'logout-click', 'sync-click',
  'add-click', 'import-click', 'batch-delete-click', 'export-click'
])

const localKeyword = ref(props.keyword)
let debounceTimer = null

watch(() => props.keyword, (val) => { localKeyword.value = val })

function emitSearch() {
  emit('update:keyword', localKeyword.value)
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => emit('search'), 300)
}

onBeforeUnmount(() => clearTimeout(debounceTimer))
</script>

<style scoped>
.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 220px;
  max-width: 360px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.btn-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

@media (max-width: 768px) {
  .action-bar { flex-direction: column; gap: 8px; }
  .search-input { max-width: 100%; width: 100%; }
  .btn-group { width: 100%; }
  .btn-group .el-button { flex: 1 1 auto; min-width: 0; font-size: 12px; padding: 8px 10px; }
}
</style>
