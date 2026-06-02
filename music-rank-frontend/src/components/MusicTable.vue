<template>
  <div class="music-table-wrapper">
    <!-- 选中提示条 -->
    <div v-if="selectedCount > 0" class="selection-bar">
      已选择 <strong>{{ selectedCount }}</strong> 首歌曲
      <el-button type="danger" size="small" plain @click="$emit('batch-delete-click')">
        批量删除
      </el-button>
    </div>

    <el-table
      :data="data"
      style="width: 100%"
      stripe
      :header-cell-style="headerStyle"
      highlight-current-row
      @selection-change="(val) => $emit('selection-change', val)"
    >
      <el-table-column type="selection" width="45" />
      <el-table-column label="评分" width="110" align="center">
        <template #default="{ row }">
          <span v-if="row.starRating > 0" class="star-rated">
            <span v-for="n in Math.floor(row.starRating)" :key="n">⭐</span>
            <span v-if="row.starRating % 1 !== 0" style="opacity:0.5">⭐</span>
            <span class="star-num">{{ row.starRating }}</span>
          </span>
          <span v-else class="star-none">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="artist" label="歌手" min-width="130">
        <template #default="{ row }">
          <span class="artist-name">{{ row.artist }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="歌名" min-width="160">
        <template #default="{ row }">
          <span class="song-title">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="歌单" width="110" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.category" size="small" type="info" effect="plain">
            {{ row.category }}
          </el-tag>
          <span v-else class="no-category">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="notes" label="描述" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="notes-text">{{ row.notes || '—' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <span class="total-info">共 {{ total }} 首歌曲</span>
      <el-pagination
        v-model:current-page="currentPageModel"
        v-model:page-size="pageSizeModel"
        :page-sizes="[10, 20, 50, 100]"
        :pager-count="7"
        layout="sizes, prev, pager, next, jumper"
        :total="total"
        @current-change="emitPage"
        @size-change="emitPage"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  selectedCount: { type: Number, default: 0 }
})

const emit = defineEmits([
  'update:currentPage', 'update:pageSize',
  'page-change', 'selection-change', 'batch-delete-click'
])

const headerStyle = {
  background: '#fafafa',
  fontWeight: 600,
  color: '#303133',
  fontSize: '14px'
}

const currentPageModel = computed({
  get: () => props.currentPage,
  set: (val) => emit('update:currentPage', val)
})

const pageSizeModel = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
})

function emitPage() {
  emit('page-change')
}
</script>

<style scoped>
.music-table-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 0;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.selection-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #ecf5ff;
  border-bottom: 1px solid #d9ecff;
  font-size: 13px;
  color: #409eff;
}

.star-rated {
  color: #f7ba2a;
  font-weight: 600;
  white-space: nowrap;
}

.star-num {
  margin-left: 4px;
  font-size: 13px;
  color: #e6a23c;
}

.star-none {
  color: #c0c4cc;
  font-size: 14px;
}

.artist-name {
  font-weight: 500;
  color: #606266;
}

.song-title {
  color: #303133;
}

.no-category, .notes-text {
  color: #c0c4cc;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 8px;
}

.total-info {
  font-size: 13px;
  color: #909399;
}
</style>
