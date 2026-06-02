<template>
  <div class="app-shell">
    <!-- 顶部 -->
    <header class="app-header">
      <div class="header-inner">
        <div class="header-brand">
          <span class="brand-icon">🎵</span>
          <h1 class="brand-title">我的音乐精选</h1>
        </div>
        <div class="header-meta">
          <span class="meta-badge">📀 {{ total }}</span>
          <span class="meta-badge">📁 {{ playlistCount }}</span>
          <span class="meta-badge" v-if="avgRating > 0">⭐ {{ avgRating }}</span>
        </div>
      </div>
    </header>

    <!-- 面包屑（查看歌单时） -->
    <div v-if="currentPlaylistName" class="breadcrumb-bar">
      <div class="breadcrumb-inner">
        <el-button text class="breadcrumb-back" @click="backToAll">
          ← 返回
        </el-button>
        <span class="breadcrumb-sep">/</span>
        <span class="breadcrumb-current">📁 {{ currentPlaylistName }}</span>
        <span class="breadcrumb-count">{{ total }} 首</span>
      </div>
    </div>

    <!-- 主体 -->
    <main class="app-main">
      <el-tabs v-model="activeTab" class="main-tabs" @tab-change="handleTabChange">
        <el-tab-pane name="all">
          <template #label><span class="tab-label">🎧 全部音乐</span></template>
          <div class="tab-content">
            <div class="toolbar-row">
              <FilterBar v-model="filter" :singers="singerList" @filter-change="fetchData" />
              <ActionBar
                v-model:keyword="keyword"
                :is-logged-in="isLoggedIn"
                :syncing="syncing"
                :selected-count="selectedIds.length"
                @search="fetchData"
                @login-click="loginDialogVisible = true"
                @logout-click="handleLogout"
                @sync-click="openSyncDialog"
                @add-click="addDialogVisible = true"
                @import-click="importDialogVisible = true"
                @batch-delete-click="handleBatchDelete"
                @export-click="handleExport"
              />
            </div>
            <MusicTable
              :data="tableData"
              :total="total"
              :selected-count="selectedIds.length"
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              @page-change="fetchData"
              @selection-change="handleSelectionChange"
              @batch-delete-click="handleBatchDelete"
            />
          </div>
        </el-tab-pane>
        <el-tab-pane name="playlists">
          <template #label><span class="tab-label">📁 我的歌单</span></template>
          <PlaylistGrid :playlists="playlists" @select="goToPlaylist" />
        </el-tab-pane>
      </el-tabs>
    </main>

    <!-- 弹窗 -->
    <AddMusicDialog v-model="addDialogVisible" @submit="handleAddMusic" />
    <TextImportDialog v-model="importDialogVisible" @import="handleTextImport" />
    <LoginDialog v-model="loginDialogVisible" @login-success="handleLoginSuccess" />
    <KugouSyncDialog
      v-model="syncDialogVisible"
      :playlists="kugouPlaylists"
      :loading="loadingPlaylists"
      :syncing="syncing"
      @sync="handleKugouSync"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FilterBar from '../components/FilterBar.vue'
import ActionBar from '../components/ActionBar.vue'
import MusicTable from '../components/MusicTable.vue'
import PlaylistGrid from '../components/PlaylistGrid.vue'
import AddMusicDialog from '../components/AddMusicDialog.vue'
import TextImportDialog from '../components/TextImportDialog.vue'
import LoginDialog from '../components/LoginDialog.vue'
import KugouSyncDialog from '../components/KugouSyncDialog.vue'
import { getMusicPage, getMusicList, addMusic, batchImportMusic, batchDeleteMusic } from '../api/music'
import { getPlaylists } from '../api/playlist'
import { getKugouPlaylists, batchImportToPlaylist } from '../api/kugou'
import axios from 'axios'

// ===== 状态 =====
const isLoggedIn = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const selectedIds = ref([])
const filter = ref({ singer: '', starRating: null, hasStar: null })
const playlists = ref([])
const currentPlaylistId = ref(null)
const currentPlaylistName = ref('')
const addDialogVisible = ref(false)
const importDialogVisible = ref(false)
const loginDialogVisible = ref(false)
const syncDialogVisible = ref(false)
const syncing = ref(false)
const loadingPlaylists = ref(false)
const kugouPlaylists = ref([])

// ===== 计算属性 =====
const singerList = computed(() => {
  const singers = new Set()
  tableData.value.forEach(i => singers.add(i.artist))
  return [...singers].sort()
})
const playlistCount = computed(() => playlists.value.length || 0)
const avgRating = computed(() => {
  const rated = tableData.value.filter(i => i.starRating > 0)
  if (!rated.length) return 0
  return (rated.reduce((s, i) => s + Number(i.starRating), 0) / rated.length).toFixed(1)
})

// ===== 数据 =====
async function fetchData() {
  try {
    const res = await getMusicPage({
      current: currentPage.value, size: pageSize.value,
      keyword: keyword.value || undefined,
      playlistId: currentPlaylistId.value || undefined,
      singer: filter.value.singer || undefined,
      starRating: filter.value.starRating ?? undefined,
      hasStar: filter.value.hasStar !== null ? filter.value.hasStar : undefined
    })
    const b = res.data
    tableData.value = b.data?.records || b.records || []
    total.value = b.data?.total || b.total || 0
  } catch (e) { ElMessage.error(e.message || '加载失败，请检查后端是否启动') }
}

async function fetchPlaylists() {
  try { const res = await getPlaylists(); playlists.value = res.data?.data || res.data || [] } catch {}
}

function handleTabChange() {
  if (activeTab.value === 'playlists') fetchPlaylists()
  else { resetFilters(); fetchData() }
}

function goToPlaylist(p) {
  resetFilters()
  currentPlaylistId.value = Number(p.id)
  currentPlaylistName.value = p.name
  activeTab.value = 'all'
  fetchData()
}

function backToAll() { currentPlaylistName.value = ''; resetFilters(); fetchData() }

function resetFilters() {
  filter.value = { singer: '', starRating: null, hasStar: null }
  keyword.value = ''
  currentPlaylistId.value = null
}

// ===== 歌曲操作 =====
async function handleAddMusic(song) {
  try { await addMusic(song); ElMessage.success('添加成功'); fetchData() }
  catch (e) { ElMessage.error(e.message || '添加失败') }
}

async function handleTextImport(musicList) {
  try {
    const res = await batchImportMusic(musicList)
    ElMessage.success(res.data?.message || `成功导入 ${musicList.length} 首`)
    fetchData(); fetchPlaylists()
  } catch (e) { ElMessage.error(e.message || '导入失败'); throw e }
}

function handleSelectionChange(s) { selectedIds.value = s.map(i => i.id) }

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`删除 ${selectedIds.value.length} 首歌曲？`, '确认', { type: 'warning' })
    await batchDeleteMusic(selectedIds.value)
    ElMessage.success('已删除'); selectedIds.value = []
    fetchData(); fetchPlaylists()
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message) }
}

// ===== 导出 =====
async function handleExport() {
  try {
    let songs
    if (selectedIds.value.length) songs = tableData.value.filter(i => selectedIds.value.includes(i.id))
    else {
      const res = await getMusicList({
        keyword: keyword.value || undefined, singer: filter.value.singer || undefined,
        starRating: filter.value.starRating ?? undefined, hasStar: filter.value.hasStar ?? undefined
      })
      songs = res.data?.data || res.data || []
    }
    if (!songs.length) return ElMessage.warning('无数据')
    const out = songs.map(s => `⭐ ${s.starRating > 0 ? s.starRating + '星' : '未打星'} | ${s.artist} - ${s.title}`).join('\n')
    await navigator.clipboard.writeText(out)
    ElMessage.success(`已复制 ${songs.length} 首`)
  } catch (e) { ElMessage.error(e.message || '复制失败') }
}

// ===== 登录 =====
function handleLoginSuccess(token) { isLoggedIn.value = true; axios.defaults.headers.common['X-Kugou-Token'] = token }
function handleLogout() { localStorage.removeItem('kugou_token'); delete axios.defaults.headers.common['X-Kugou-Token']; isLoggedIn.value = false }
function checkLogin() { const t = localStorage.getItem('kugou_token'); if (t) { isLoggedIn.value = true; axios.defaults.headers.common['X-Kugou-Token'] = t } }

// ===== 酷狗同步 =====
async function openSyncDialog() {
  if (!isLoggedIn.value) { ElMessage.warning('请先登录'); loginDialogVisible.value = true; return }
  syncDialogVisible.value = true; loadingPlaylists.value = true
  try {
    const kugouRes = await axios.get('/kugou/user/playlist')
    if (kugouRes.data?.error_code === 0 || kugouRes.data?.status === 1) {
      const uid = '2150217007'
      kugouPlaylists.value = (kugouRes.data?.data?.info || kugouRes.data?.data || [])
        .filter(p => String(p.list_create_userid || p.userid) === uid)
        .map(p => ({ id: p.global_collection_id || p.id, name: p.name, coverUrl: (p.pic || '').replace('{size}', '200'), trackCount: p.count || 0 }))
    } else if (kugouRes.data?.error_code === 20010 || kugouRes.data?.error_code === 20017) {
      ElMessage.error('登录已过期，请重新登录'); handleLogout(); syncDialogVisible.value = false; return
    }
  } catch { ElMessage.error('获取酷狗歌单失败'); syncDialogVisible.value = false; return }
  loadingPlaylists.value = false
  kugouPlaylists.value.length ? ElMessage.success(`获取到 ${kugouPlaylists.value.length} 个歌单`) : ElMessage.warning('无自建歌单')
}

async function handleKugouSync(selectedIds) {
  syncing.value = true; let totalImported = 0
  try {
    for (const pid of selectedIds) {
      const pl = kugouPlaylists.value.find(p => p.id === pid)
      if (!pl) continue
      ElMessage.info(`导入: ${pl.name}...`)
      await new Promise(r => setTimeout(r, 800))
      const r = await axios.get(`/kugou/playlist/track/all?id=${pid}`)
      if (r.data?.error_code === 20010 || r.data?.error_code === 20017) { ElMessage.error('登录过期'); handleLogout(); return }
      const songsData = r.data?.data?.songs || r.data?.songs || r.data?.data || []
      if (!songsData.length) continue
      const songs = songsData.map(s => {
        const si = s.singerinfo?.[0] || {}
        return { title: (s.name || '').replace(`${si.name} - `, ''), artist: si.name || '未知', album: s.albuminfo?.name || '', coverUrl: (s.cover || '').replace('{size}', '200') }
      })
      const ir = await batchImportToPlaylist(songs, pl.name, pl.coverUrl)
      totalImported += ir.data?.data?.newMusicCount || songs.length
    }
    ElMessage.success(`同步完成！导入 ${totalImported} 首`)
    syncDialogVisible.value = false; fetchData(); fetchPlaylists()
  } catch (e) { ElMessage.error('同步失败: ' + (e.message || '')) }
  finally { syncing.value = false }
}

onMounted(() => { checkLogin(); fetchData() })
</script>

<style scoped>
.app-shell { min-height: 100vh; background: var(--bg); }

/* 头部 */
.app-header {
  background: #ffffff;
  border-bottom: 1px solid var(--border);
  position: sticky; top: 0; z-index: 100;
}
.header-inner {
  max-width: 1200px; margin: 0 auto; padding: 16px 24px;
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
}
.header-brand { display: flex; align-items: center; gap: 10px; }
.brand-icon { font-size: 24px; }
.brand-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--text); letter-spacing: -0.5px; }
.header-meta { display: flex; gap: 8px; }
.meta-badge {
  font-size: 13px; color: var(--text-secondary); background: var(--bg);
  padding: 4px 12px; border-radius: 20px; font-weight: 500;
}

/* 面包屑 */
.breadcrumb-bar {
  background: #fff; border-bottom: 1px solid var(--border);
}
.breadcrumb-inner {
  max-width: 1200px; margin: 0 auto; padding: 10px 24px;
  display: flex; align-items: center; gap: 8px; font-size: 14px;
}
.breadcrumb-back { color: var(--primary); font-weight: 500; padding: 4px 8px; }
.breadcrumb-sep { color: var(--text-muted); }
.breadcrumb-current { font-weight: 600; color: var(--text); }
.breadcrumb-count { color: var(--text-muted); font-size: 13px; margin-left: 4px; }

/* 主体 */
.app-main { max-width: 1200px; margin: 0 auto; padding: 20px 24px 40px; }
.main-tabs { background: transparent; }
.main-tabs :deep(.el-tabs__header) { margin-bottom: 0; background: #fff; border-radius: var(--radius) var(--radius) 0 0; padding: 0 16px; border: 1px solid var(--border); border-bottom: none; }
.main-tabs :deep(.el-tabs__nav-wrap) { padding-top: 8px; }
.main-tabs :deep(.el-tabs__item) { font-size: 15px; height: 44px; line-height: 44px; }
.tab-content { background: #fff; border: 1px solid var(--border); border-top: none; border-radius: 0 0 var(--radius) var(--radius); padding: 20px; }
.toolbar-row { display: flex; flex-direction: column; gap: 12px; margin-bottom: 4px; }

@media (max-width: 768px) {
  .header-inner { padding: 12px 16px; }
  .brand-title { font-size: 17px; }
  .app-main { padding: 12px 8px 30px; }
  .tab-content { padding: 12px; }
}
</style>
