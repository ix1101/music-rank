<template>
  <div class="action-bar">
    <el-input v-model="kw" placeholder="🔍 搜索歌名或歌手..." clearable @input="onInput" class="search-input" />
    <div class="btn-group">
      <template v-if="!isLoggedIn">
        <el-button type="primary" plain @click="$emit('login-click')">🔐 登录酷狗</el-button>
      </template>
      <template v-else>
        <el-button text @click="$emit('logout-click')">🚪 退出</el-button>
      </template>
      <el-button type="primary" @click="$emit('sync-click')" :loading="syncing">🔄 同步歌单</el-button>
      <el-button @click="$emit('add-click')">＋ 添加</el-button>
      <el-button @click="$emit('import-click')">📝 导入</el-button>
      <el-button type="danger" plain :disabled="selectedCount===0" @click="$emit('batch-delete-click')">🗑 {{ selectedCount||'' }}</el-button>
      <el-button @click="$emit('export-click')">📋 导出</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
const props = defineProps({ keyword: String, isLoggedIn: Boolean, syncing: Boolean, selectedCount: Number })
const emit = defineEmits(['update:keyword','search','login-click','logout-click','sync-click','add-click','import-click','batch-delete-click','export-click'])
const kw = ref(props.keyword)
let t = null
watch(() => props.keyword, v => { kw.value = v })
function onInput() { emit('update:keyword', kw.value); clearTimeout(t); t = setTimeout(() => emit('search'), 300) }
onBeforeUnmount(() => clearTimeout(t))
</script>

<style scoped>
.action-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.search-input { flex: 1; min-width: 200px; max-width: 340px; }
.btn-group { display: flex; gap: 6px; flex-wrap: wrap; }
@media (max-width: 768px) { .action-bar { flex-direction: column; } .search-input { max-width: 100%; width: 100%; } .btn-group .el-button { flex: 1 1 auto; font-size: 12px; } }
</style>
