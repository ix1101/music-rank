<template>
  <div class="filter-bar">
    <el-select v-model="f.singer" placeholder="歌手" clearable @change="emitF" size="default" style="width:150px">
      <el-option v-for="s in singers" :key="s" :label="s" :value="s" />
    </el-select>
    <el-select v-model="f.starRating" placeholder="星级" clearable @change="emitF" size="default" style="width:120px">
      <el-option label="⭐ 5" :value="5" /><el-option label="⭐ 4.5" :value="4.5" />
      <el-option label="⭐ 4" :value="4" /><el-option label="⭐ 3.5" :value="3.5" />
      <el-option label="⭐ 3" :value="3" /><el-option label="未打分" :value="0" />
    </el-select>
    <el-select v-model="f.hasStar" placeholder="状态" clearable @change="emitF" size="default" style="width:110px">
      <el-option label="已打星" :value="true" /><el-option label="未打星" :value="false" />
    </el-select>
    <el-tag v-if="active" type="warning" closable @close="clearAll" size="small" round>已筛选</el-tag>
  </div>
</template>

<script setup>
import { reactive, computed, watch } from 'vue'
const props = defineProps({ singers: Array, modelValue: Object })
const emit = defineEmits(['update:modelValue', 'filter-change'])
const f = reactive({ singer: '', starRating: null, hasStar: null, ...props.modelValue })
watch(() => props.modelValue, v => { if (v) Object.assign(f, v) }, { deep: true })
const active = computed(() => f.singer || f.starRating !== null || f.hasStar !== null)
function emitF() { emit('update:modelValue', { ...f }); emit('filter-change') }
function clearAll() { f.singer = ''; f.starRating = null; f.hasStar = null; emitF() }
</script>

<style scoped>
.filter-bar { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
@media (max-width: 768px) { .filter-bar { flex-direction: column; align-items: stretch; } .filter-bar .el-select { width: 100% !important; } }
</style>
