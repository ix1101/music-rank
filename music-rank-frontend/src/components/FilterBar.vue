<template>
  <div class="filter-bar">
    <span class="filter-label">筛选：</span>
    <el-select
      v-model="localFilter.singer"
      placeholder="按歌手"
      clearable
      @change="emitFilter"
      size="default"
      style="width: 160px"
    >
      <el-option v-for="singer in singers" :key="singer" :label="singer" :value="singer" />
    </el-select>

    <el-select
      v-model="localFilter.starRating"
      placeholder="按星级"
      clearable
      @change="emitFilter"
      size="default"
      style="width: 130px"
    >
      <el-option label="⭐ 5 星" :value="5" />
      <el-option label="⭐ 4.5 星" :value="4.5" />
      <el-option label="⭐ 4 星" :value="4" />
      <el-option label="⭐ 3.5 星" :value="3.5" />
      <el-option label="⭐ 3 星" :value="3" />
      <el-option label="— 未打分" :value="0" />
    </el-select>

    <el-select
      v-model="localFilter.hasStar"
      placeholder="打分状态"
      clearable
      @change="emitFilter"
      size="default"
      style="width: 120px"
    >
      <el-option label="已打星" :value="true" />
      <el-option label="未打星" :value="false" />
    </el-select>

    <el-tag v-if="hasActiveFilter" type="warning" closable @close="clearAll" size="small">
      已筛选
    </el-tag>
  </div>
</template>

<script setup>
import { reactive, computed, watch } from 'vue'

const props = defineProps({
  singers: { type: Array, default: () => [] },
  modelValue: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'filter-change'])

const localFilter = reactive({
  singer: props.modelValue?.singer || '',
  starRating: props.modelValue?.starRating ?? null,
  hasStar: props.modelValue?.hasStar ?? null
})

watch(() => props.modelValue, (newVal) => {
  if (newVal) Object.assign(localFilter, newVal)
}, { deep: true })

const hasActiveFilter = computed(() => {
  return localFilter.singer || localFilter.starRating !== null || localFilter.hasStar !== null
})

function emitFilter() {
  emit('update:modelValue', { ...localFilter })
  emit('filter-change')
}

function clearAll() {
  localFilter.singer = ''
  localFilter.starRating = null
  localFilter.hasStar = null
  emitFilter()
}
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 13px;
  color: #909399;
  margin-right: 4px;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-bar .el-select {
    width: 100% !important;
  }
}
</style>
