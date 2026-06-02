<template>
  <div class="app-shell">
    <!-- 顶部导航 -->
    <header class="app-header">
      <div class="header-content">
        <h1 class="app-title">🎵 我的音乐精选排行</h1>
        <div class="header-stats">
          <span class="stat-item">📀 {{ total }} 首</span>
          <span class="stat-item">📁 {{ playlistCount }} 个歌单</span>
          <span class="stat-item" v-if="avgRating > 0">⭐ {{ avgRating }} 均分</span>
        </div>
      </div>
    </header>

    <!-- 主体 -->
    <main class="app-main">
      <el-tabs v-model="activeTab" class="main-tabs" @tab-change="handleTabChange">
        <el-tab-pane name="all">
          <template #label>
            <span class="tab-label">🎧 全部音乐</span>
          </template>

          <div class="content-card">
            <FilterBar
              v-model="filter"
              :singers="singerList"
              @filter-change="fetchData"
            />
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
          <template #label>
            <span class="tab-label">📁 我的歌单</span>
          </template>
          <PlaylistGrid
            :playlists="playlists"
            @select="goToPlaylist"
          />
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
import { ref, onMounted, computed } from 'vue'
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
  tableData.value.forEach((item) => singers.add(item.artist))
  return Array.from(singers).sort()
})

const playlistCount = computed(() => playlists.value.length || 0)

const avgRating = computed(() => {
  const rated = tableData.value.filter((i) => i.starRating > 0)
  if (!rated.length) return 0
  const sum = rated.reduce((s, i) => s + Number(i.starRating), 0)
  return (sum / rated.length).toFixed(1)
})

// ===== 数据获取 =====
async function fetchData() {
  try {
    const res = await getMusicPage({
      current: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      playlistId: currentPlaylistId.value || undefined,
      singer: filter.value.singer || undefined,
      starRating: filter.value.starRating ?? undefined,
      hasStar: filter.value.hasStar !== null ? filter.value.hasStar : undefined
    })
    const body = res.data
    tableData.value = body.data?.records || body.records || []
    total.value = body.data?.total || body.total || 0
  } catch (e) {
    ElMessage.error(e.message || '获取数据失败，请检查后端是否启动')
  }
}

async function fetchPlaylists() {
  try {
    const res = await getPlaylists()
    playlists.value = res.data?.data || res.data || []
  } catch {}
}

function handleTabChange() {
  if (activeTab.value === 'playlists') fetchPlaylists()
  else { resetFilters(); fetchData() }
}

function goToPlaylist(playlist) {
  resetFilters()
  currentPlaylistId.value = Number(playlist.id)
  activeTab.value = 'all'
  fetchData()
  ElMessage.info(`正在查看歌单：${playlist.name}`)
}

function resetFilters() {
  filter.value = { singer: '', starRating: null, hasStar: null }
  keyword.value = ''
  currentPlaylistId.value = null
}

// ===== 歌曲操作 =====
async function handleAddMusic(song) {
  try {
    await addMusic(song)
    ElMessage.success('添加成功')
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}

async function handleTextImport(musicList) {
  try {
    const res = await batchImportMusic(musicList)
    ElMessage.success(res.data?.message || '导入成功')
    fetchData(); fetchPlaylists()
  } catch (e) {
    ElMessage.error(e.message || '导入失败')
    throw e
  }
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map((item) => item.id)
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 首歌曲吗？`,
      '确认删除',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await batchDeleteMusic(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    fetchData(); fetchPlaylists()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

// ===== 导出 =====
async function handleExport() {
  try {
    let songs
    if (selectedIds.value.length > 0) {
      songs = tableData.value.filter((item) => selectedIds.value.includes(item.id))
    } else {
      const res = await getMusicList({
        keyword: keyword.value || undefined,
        singer: filter.value.singer || undefined,
        starRating: filter.value.starRating ?? undefined,
        hasStar: filter.value.hasStar ?? undefined
      })
      songs = res.data?.data || res.data || []
    }
    if (!songs.length) return ElMessage.warning('没有可复制的歌曲')

    const output = songs
      .map((item) => `⭐ ${item.starRating > 0 ? item.starRating + '星' : '未打星'} | ${item.artist} - ${item.title}`)
      .join('\n')

    await navigator.clipboard.writeText(output)
    ElMessage.success(`已复制 ${songs.length} 首歌曲`)
  } catch (e) {
    ElMessage.error(e.message || '复制失败')
  }
}

// ===== 登录 =====
function handleLoginSuccess(token) {
  isLoggedIn.value = true
  axios.defaults.headers.common['X-Kugou-Token'] = token
}

function handleLogout() {
  localStorage.removeItem('kugou_token')
  delete axios.defaults.headers.common['X-Kugou-Token']
  isLoggedIn.value = false
  ElMessage.success('已退出登录')
}

function checkLoginStatus() {
  const token = localStorage.getItem('kugou_token')
  if (token) {
    isLoggedIn.value = true
    axios.defaults.headers.common['X-Kugou-Token'] = token
  }
}

// ===== 酷狗同步 =====
async function openSyncDialog() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录酷狗账号')
    loginDialogVisible.value = true
    return
  }
  syncDialogVisible.value = true
  loadingPlaylists.value = true
  try {
    // 直接走 Vite 代理调用 KuGouMusicApi（更可靠，不走后端）
    const kugouRes = await axios.get('/kugou/user/playlist')
    if (kugouRes.data?.error_code === 0 || kugouRes.data?.status === 1) {
      const myUserId = '2150217007'
      const playlistData = kugouRes.data?.data?.info || kugouRes.data?.data || []
      kugouPlaylists.value = playlistData
        .filter((pl) => String(pl.list_create_userid || pl.userid) === myUserId)
        .map((pl) => ({
          id: pl.global_collection_id || pl.id,
          name: pl.name,
          coverUrl: pl.pic?.replace?.('{size}', '200') || pl.imgurl || '',
          trackCount: pl.count || 0
        }))
    } else if (kugouRes.data?.error_code === 20010 || kugouRes.data?.error_code === 20017) {
      ElMessage.error('登录已失效，请重新登录')
      handleLogout()
      syncDialogVisible.value = false
      return
    }
  } catch {
    ElMessage.error('获取酷狗歌单失败，请确认 KuGouMusicApi 已启动 (localhost:3000)')
    syncDialogVisible.value = false
    return
  }
  loadingPlaylists.value = false
  if (kugouPlaylists.value.length === 0) {
    ElMessage.warning('没有找到任何自建歌单')
  } else {
    ElMessage.success(`获取到 ${kugouPlaylists.value.length} 个歌单`)
  }
}

async function handleKugouSync(selectedIds) {
  syncing.value = true
  let totalImported = 0
  try {
    for (const playlistId of selectedIds) {
      const playlist = kugouPlaylists.value.find((p) => p.id === playlistId)
      if (!playlist) continue
      ElMessage.info(`正在导入：${playlist.name}...`)

      // 通过 Vite 代理获取歌单歌曲
      await new Promise((r) => setTimeout(r, 1000))
      const kugouRes = await axios.get(`/kugou/playlist/track/all?id=${playlistId}`)
      if (kugouRes.data?.error_code === 20010 || kugouRes.data?.error_code === 20017) {
        ElMessage.error('登录已失效，请重新登录')
        handleLogout()
        return
      }
      const songsData = kugouRes.data?.data?.songs || kugouRes.data?.songs || kugouRes.data?.data || []
      if (!songsData.length) continue
      const songs = songsData.map((s) => {
        const si = s.singerinfo?.[0] || {}
        return {
          title: s.name?.replace(`${si.name} - `, '') || s.name,
          artist: si.name || '未知歌手',
          album: s.albuminfo?.name || '',
          coverUrl: s.cover?.replace?.('{size}', '200') || ''
        }
      })
      // 发送到后端存储
      const importRes = await batchImportToPlaylist(songs, playlist.name, playlist.coverUrl)
      totalImported += importRes.data?.data?.newMusicCount || songs.length
    }
    ElMessage.success(`同步完成！共导入 ${totalImported} 首新歌`)
    syncDialogVisible.value = false
    fetchData(); fetchPlaylists()
  } catch (e) {
    ElMessage.error('同步失败：' + (e.message || '未知错误'))
  } finally {
    syncing.value = false
  }
}

onMounted(() => {
  checkLoginStatus()
  fetchData()
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f0f2f5;
}

/* 顶部 */
.app-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: #fff;
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.app-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  letter-spacing: 1px;
}

.header-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  font-size: 13px;
  opacity: 0.85;
  background: rgba(255,255,255,0.1);
  padding: 4px 12px;
  border-radius: 20px;
}

/* 主体 */
.app-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 20px 40px;
}

.main-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.main-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  height: 44px;
  line-height: 44px;
}

.tab-label {
  font-size: 15px;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

/* 移动端 */
@media (max-width: 768px) {
  .app-header { padding: 14px 0; }
  .app-title { font-size: 18px; }
  .header-content { padding: 0 12px; }
  .header-stats { gap: 8px; }
  .stat-item { font-size: 12px; padding: 3px 8px; }
  .app-main { padding: 10px 8px 30px; }
  .content-card { padding: 10px; }
}
</style>
