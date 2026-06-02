<template>
  <div class="music-table-wrapper">
    <div v-if="selectedCount > 0" class="selection-bar">
      已选 <strong>{{ selectedCount }}</strong> 首
      <el-button size="small" type="danger" plain @click="$emit('batch-delete-click')">删除</el-button>
    </div>
    <el-table :data="data" stripe highlight-current-row style="width:100%"
      @selection-change="v => $emit('selection-change', v)">
      <el-table-column type="selection" width="42" />
      <el-table-column label="评分" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.starRating > 0" class="star-cell">
            <span class="star-icons">{{ '⭐'.repeat(Math.floor(row.starRating)) }}</span>
            <span class="star-num">{{ row.starRating }}</span>
          </span>
          <span v-else class="no-star">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="artist" label="歌手" min-width="130">
        <template #default="{ row }"><span class="artist-cell">{{ row.artist }}</span></template>
      </el-table-column>
      <el-table-column prop="title" label="歌名" min-width="160">
        <template #default="{ row }"><span class="title-cell">{{ row.title }}</span></template>
      </el-table-column>
      <el-table-column label="歌单" width="110" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.category" size="small" round>{{ row.category }}</el-tag>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="描述" min-width="140" show-overflow-tooltip>
        <template #default="{ row }"><span class="muted">{{ row.notes || '—' }}</span></template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper">
      <span class="total-info">共 {{ total }} 首</span>
      <el-pagination
        v-model:current-page="pg" v-model:page-size="ps"
        :page-sizes="[10, 20, 50, 100]"
        :pager-count="7" layout="sizes, prev, pager, next, jumper"
        :total="total" @current-change="emitPg" @size-change="emitPg" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  data: Array, total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 }, pageSize: { type: Number, default: 10 },
  selectedCount: { type: Number, default: 0 }
})
const emit = defineEmits(['update:currentPage', 'update:pageSize', 'page-change', 'selection-change', 'batch-delete-click'])
const pg = computed({ get: () => props.currentPage, set: v => emit('update:currentPage', v) })
const ps = computed({ get: () => props.pageSize, set: v => emit('update:pageSize', v) })
function emitPg() { emit('page-change') }
</script>

<style scoped>
.music-table-wrapper { background: transparent; }
.selection-bar {
  display: flex; align-items: center; gap: 10px; padding: 8px 14px;
  background: #eef2ff; border-radius: 8px; margin-bottom: 12px;
  font-size: 13px; color: var(--primary);
}
.star-cell { display: inline-flex; align-items: center; gap: 2px; }
.star-icons { font-size: 12px; line-height: 1; }
.star-num { font-weight: 700; color: #d97706; font-size: 13px; margin-left: 4px; }
.no-star, .muted { color: var(--text-muted); font-size: 13px; }
.artist-cell { font-weight: 600; color: var(--text); }
.title-cell { color: var(--text); }
.pagination-wrapper {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 0 0; flex-wrap: wrap; gap: 8px;
}
.total-info { font-size: 13px; color: var(--text-muted); }
</style>
