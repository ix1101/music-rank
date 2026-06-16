<template>
  <div class="app-shell">
    <!-- === 头部 — 大留白 === -->
    <header class="app-header">
      <div class="header-top">
        <h1>我的音乐精选</h1>
        <span class="header-sub">私人收藏库</span>
      </div>
      <div class="stats-line">
        <div class="st-item">
          <span class="st-num">{{ statsTotal }}</span>
          <span class="st-lbl">首歌曲</span>
        </div>
        <div class="st-item">
          <span class="st-num">{{ playlistCount }}</span>
          <span class="st-lbl">个歌单</span>
        </div>
        <div class="st-item">
          <span class="st-num">{{ statsAvg }}</span>
          <span class="st-lbl">均分</span>
        </div>
      </div>
    </header>

    <!-- === 搜索 — 柔和圆角 === -->
    <div class="search-wrap">
      <input
        type="search"
        class="search-input"
        v-model="keyword"
        placeholder="搜索歌名、歌手或专辑..."
        autocomplete="off"
        @input="onSearchDebounce"
      />
      <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
      </svg>
    </div>

    <!-- === 面包屑（歌单内） === -->
    <div v-if="currentPlaylistName" class="breadcrumb-bar">
      <button class="bread-back" @click="backToAll">← 返回全部</button>
      <span class="bread-sep">/</span>
      <span class="bread-cur">{{ currentPlaylistName }}</span>
      <span class="bread-cnt">{{ statsTotal }} 首</span>
    </div>

    <!-- === 标签切换 === -->
    <nav class="app-tabs">
      <button :class="['tab-btn', { active: activeTab === 'all' }]" @click="switchTo('all')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>
        全部音乐
        <span class="tab-cnt">{{ statsTotal }}</span>
      </button>
      <button :class="['tab-btn', { active: activeTab === 'playlists' }]" @click="switchTo('playlists')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
        我的歌单
        <span class="tab-cnt">{{ playlistCount }}</span>
      </button>
    </nav>

    <!-- === 全部音乐 === -->
    <div v-show="activeTab === 'all'">
      <!-- 工具栏 -->
      <div class="toolbar">
        <!-- 左组：登录/退出 + 同步 -->
        <span class="tb-group">
          <template v-if="!isLoggedIn">
            <el-button class="tbtn-ghost" size="small" @click="loginDialogVisible = true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
              登录
            </el-button>
          </template>
          <template v-else>
            <el-button class="tbtn-ghost" size="small" @click="handleLogout">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              退出
            </el-button>
            <el-button class="tbtn-ghost" size="small" @click="openSyncDialog" :loading="syncing">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
              同步
            </el-button>
          </template>
        </span>
        <!-- 中：添加 -->
        <el-button class="tbtn-primary" size="small" @click="openAddDialog">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          添加
        </el-button>
        <!-- 右组：导入 + 导出 -->
        <span class="tb-group">
          <el-button class="tbtn-ghost" size="small" @click="importDialogVisible = true">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            导入
          </el-button>
          <div class="export-wrap" :class="{ open: exportOpen }">
            <el-button class="tbtn-ghost" size="small" @click="exportOpen = !exportOpen">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              导出
            </el-button>
            <div v-if="exportOpen" class="export-drop">
              <div class="export-opts">
                <label class="exp-opt"><input type="checkbox" v-model="expAlbum"> 包含专辑</label>
                <label class="exp-opt"><input type="checkbox" v-model="expNotes"> 包含备注</label>
              </div>
              <button @click="handleExport('dash'); exportOpen = false">横杠 (- 歌手 - 歌名)</button>
              <button @click="handleExport('pipe'); exportOpen = false">竖线 (⭐ 5星 | 歌手 | 歌名)</button>
              <button @click="handleExport('text'); exportOpen = false">文字 (歌手：…，歌名：…)</button>
              <button @click="handleExport('json'); exportOpen = false">JSON (结构化数据)</button>
            </div>
          </div>
        </span>
        <div v-if="exportOpen" class="fselect-backdrop" @click="exportOpen = false" />
      </div>

      <!-- 筛选 -->
      <FilterBar v-model="filter" :singers="singerList" @filter-change="onFilterChange" />

      <!-- 歌曲列表 -->
      <div style="margin-top:8px">
        <MusicTable
          :data="tableData"
          :total="statsTotal"
          :selected-ids="selectedIds"
          :selected-count="selectedIds.length"
          :current-page="currentPage"
          :page-size="pageSize"
          @update:current-page="currentPage = $event"
          @page-change="fetchData"
          @selection-change="handleSelectionChange"
          @select-all="handleSelectAll"
          @batch-delete-click="handleBatchDelete"
          @star-change="handleStarChange"
          @row-delete="handleRowDelete"
          @row-click="handleRowClick"
        />
      </div>
    </div>

    <!-- === 我的歌单 === -->
    <div v-show="activeTab === 'playlists'">
      <PlaylistGrid
        :playlists="playlists"
        @select="goToPlaylist"
        @delete-playlist="handlePlaylistDelete"
      />
    </div>

    <!-- === 弹窗 === -->
    <!-- 底部编辑 Sheet -->
    <AddMusicDialog
      ref="editSheetRef"
      v-model="addDialogVisible"
      v-model:song="editingSong"
      :playlists="playlists"
      @submit="handleAddOrEdit"
      @delete-click="handleDialogDelete"
    />

    <!-- 其他弹窗（保留 el-dialog） -->
    <TextImportDialog v-model="importDialogVisible" @import="handleTextImport" />
    <LoginDialog v-model="loginDialogVisible" @login-success="handleLoginSuccess" />
    <KugouSyncDialog
      v-model="syncDialogVisible"
      :playlists="kugouPlaylists" :loading="loadingPlaylists" :syncing="syncing"
      @sync="handleKugouSync"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FilterBar from '../components/FilterBar.vue'
import MusicTable from '../components/MusicTable.vue'
import PlaylistGrid from '../components/PlaylistGrid.vue'
import AddMusicDialog from '../components/AddMusicDialog.vue'
import TextImportDialog from '../components/TextImportDialog.vue'
import LoginDialog from '../components/LoginDialog.vue'
import KugouSyncDialog from '../components/KugouSyncDialog.vue'
import {
  getMusicPage, getMusicList, getMusicStats,
  addMusic, batchImportMusic, batchDeleteMusic,
  updateMusic, deleteMusic, updateStarRating,
  getSongPlaylists, updateSongPlaylists
} from '../api/music'
import { getPlaylists, deletePlaylist, createPlaylist } from '../api/playlist'
import { getTokenStatus, syncTokenToBackend } from '../api/kugou'
import axios from 'axios'

// ===== 状态 =====
const isLoggedIn = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const statsTotal = ref(0)
const statsAvg = ref('0')
const currentPage = ref(1)
const pageSize = ref(8)
const keyword = ref('')
const selectedIds = ref([])
const filter = ref({ singer: '', starRatingMin: null, starRatingMax: null, hasStar: null })
const playlists = ref([])
const currentPlaylistId = ref(null)
const currentPlaylistName = ref('')
const addDialogVisible = ref(false)
const editingSong = ref(null)
const editSheetRef = ref(null)
const importDialogVisible = ref(false)
const loginDialogVisible = ref(false)
const syncDialogVisible = ref(false)
const syncing = ref(false)
const loadingPlaylists = ref(false)
const kugouPlaylists = ref([])
const exportOpen = ref(false)
const expAlbum = ref(true)
const expNotes = ref(true)

// ===== 计算 =====
const singerList = ref([])  // 从全部歌曲中获取，不依赖当前筛选
const playlistCount = computed(() => playlists.value.length || 0)

// 加载全部歌手列表（不受筛选影响）
const SINGER_CACHE_KEY = 'music_rank_singers'

async function fetchSingerList() {
  try {
    const r = await getMusicList({})
    const all = r.data?.data || r.data || []
    const s = new Set(); all.forEach(i => { if (i.artist) s.add(i.artist) })
    const sorted = [...s].sort()
    singerList.value = sorted
    try { localStorage.setItem(SINGER_CACHE_KEY, JSON.stringify(sorted)) } catch {}
  } catch {}
}

function loadSingersFromCache() {
  try {
    const cached = localStorage.getItem(SINGER_CACHE_KEY)
    if (cached) {
      const arr = JSON.parse(cached)
      if (Array.isArray(arr) && arr.length) singerList.value = arr
    }
  } catch {}
}

// ===== 搜索防抖 =====
let st = null
function onSearchDebounce() {
  clearTimeout(st)
  st = setTimeout(() => { currentPage.value = 1; fetchData() }, 200)
}

// ===== 数据获取 =====
async function fetchData() {
  try {
    const res = await getMusicPage({
      current: currentPage.value, size: pageSize.value,
      keyword: keyword.value || undefined,
      playlistId: currentPlaylistId.value || undefined,
      singer: filter.value.singer || undefined,
      starRatingMin: filter.value.starRatingMin ?? undefined,
      starRatingMax: filter.value.starRatingMax ?? undefined,
      hasStar: filter.value.hasStar !== null ? filter.value.hasStar : undefined
    })
    // 响应中已合并 total + avgRating，一次请求替代原来的 fetchData + fetchStats
    const result = res.data?.data || res.data || {}
    tableData.value = result.records || []
    statsTotal.value = result.total || 0
    const avg = result.avgRating || 0
    statsAvg.value = Number(avg) > 0 ? Number(avg).toFixed(1) : '0'
  } catch (e) { ElMessage.error(e.message || '加载失败') }
}

async function fetchStats() {
  try {
    const res = await getMusicStats({
      keyword: keyword.value || undefined,
      singer: filter.value.singer || undefined,
      starRatingMin: filter.value.starRatingMin ?? undefined,
      starRatingMax: filter.value.starRatingMax ?? undefined,
      hasStar: filter.value.hasStar !== null ? filter.value.hasStar : undefined,
      playlistId: currentPlaylistId.value || undefined
    })
    const d = res.data?.data || res.data || {}
    statsTotal.value = d.total || 0
    const avg = d.avgRating || 0
    statsAvg.value = Number(avg) > 0 ? Number(avg).toFixed(1) : '0'
  } catch {}
}

async function fetchPlaylists() {
  try {
    const res = await getPlaylists()
    playlists.value = res.data?.data || res.data || []
  } catch {}
}

// ===== 标签 / 歌单导航 =====
function switchTo(tab) {
  activeTab.value = tab
  if (tab === 'playlists') fetchPlaylists()
  else { fetchData() }
}

function goToPlaylist(p) {
  resetFilters()
  currentPlaylistId.value = Number(p.id)
  currentPlaylistName.value = p.name
  activeTab.value = 'all'
  fetchData()
}

function backToAll() {
  currentPlaylistName.value = ''
  currentPlaylistId.value = null
  resetFilters()
  fetchData()
}

function resetFilters() {
  filter.value = { singer: '', starRatingMin: null, starRatingMax: null, hasStar: null }
  keyword.value = ''
  currentPlaylistId.value = null
}

function onFilterChange() {
  currentPage.value = 1
  selectedIds.value = []
  fetchData()
}

// ===== 歌曲操作 =====
function handleSelectionChange(ids) { selectedIds.value = ids }

async function handleSelectAll() {
  try {
    // 获取所有匹配筛选条件的歌曲ID
    let allSongs
    if (currentPlaylistId.value) {
      // 歌单内：用分页接口拉取全部
      const r = await getMusicPage({
        current: 1, size: 9999,
        playlistId: currentPlaylistId.value,
        keyword: keyword.value || undefined,
        singer: filter.value.singer || undefined,
        starRatingMin: filter.value.starRatingMin ?? undefined,
      starRatingMax: filter.value.starRatingMax ?? undefined,
        hasStar: filter.value.hasStar !== null ? filter.value.hasStar : undefined
      })
      allSongs = r.data?.data?.records || r.data?.records || []
    } else {
      const r = await getMusicList({
        keyword: keyword.value || undefined,
        singer: filter.value.singer || undefined,
        starRatingMin: filter.value.starRatingMin ?? undefined,
      starRatingMax: filter.value.starRatingMax ?? undefined,
        hasStar: filter.value.hasStar ?? undefined
      })
      allSongs = r.data?.data || r.data || []
    }
    selectedIds.value = allSongs.map(s => s.id)
    ElMessage.success(`已选 ${selectedIds.value.length} 首`)
  } catch (e) {
    ElMessage.error('获取全量歌曲失败')
  }
}

function openAddDialog() {
  editingSong.value = null
  addDialogVisible.value = true
}

async function handleRowClick(row) {
  editingSong.value = { ...row }
  addDialogVisible.value = true
  // 获取歌曲的歌单归属
  try {
    const res = await getSongPlaylists(row.id)
    const pls = res.data?.data || res.data || []
    // 延迟设置，等 sheet 渲染完成
    setTimeout(() => {
      if (editSheetRef.value) {
        editSheetRef.value.setSongPlaylists(pls)
      }
    }, 100)
  } catch {}
}

async function handleAddOrEdit(payload) {
  try {
    let songId = payload.id

    if (songId) {
      // 编辑模式
      await updateMusic(songId, {
        artist: payload.artist,
        title: payload.title,
        album: payload.album,
        starRating: payload.starRating,
        notes: payload.notes
      })
      ElMessage.success('已保存')
    } else {
      // 新建模式
      const res = await addMusic(payload)
      const saved = res.data?.data || res.data
      songId = saved.id
      ElMessage.success('已添加')
    }

    // 处理歌单归属
    if (songId && payload._playlists && payload._playlists.length) {
      const playlistIds = []
      for (const pl of payload._playlists) {
        if (pl.isNew) {
          // 创建新歌单
          const cr = await createPlaylist(pl.name)
          const created = cr.data?.data || cr.data
          playlistIds.push(created.id)
        } else if (pl.id) {
          playlistIds.push(pl.id)
        }
      }
      if (playlistIds.length) {
        await updateSongPlaylists(songId, playlistIds)
      }
    } else if (songId && (!payload._playlists || !payload._playlists.length)) {
      // 清空歌单归属
      await updateSongPlaylists(songId, [])
    }

    editingSong.value = null
    fetchData()
    fetchPlaylists()
  } catch (e) {
    ElMessage.error(e.message || (payload.id ? '更新失败' : '添加失败'))
    throw e
  }
}

async function handleStarChange(row, newRating) {
  try {
    await updateStarRating(row.id, newRating)
    row.starRating = newRating
    fetchData()
  } catch (e) { ElMessage.error(e.message) }
}

async function handleRowDelete(row) {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(
      `删除「${row.artist} - ${row.title}」？`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await deleteMusic(row.id)
    ElMessage.success('已删除')
    // 清理已选列表中被删除的歌曲
    selectedIds.value = selectedIds.value.filter(id => id !== row.id)
    addDialogVisible.value = false
    editingSong.value = null
    fetchData()
    fetchPlaylists()
    fetchSingerList()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message)
  }
}

function handleDialogDelete() {
  if (!editingSong.value?.id) return
  handleRowDelete(editingSong.value)
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      `删除选中的 ${selectedIds.value.length} 首？`,
      '确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await batchDeleteMusic(selectedIds.value)
    ElMessage.success('已删除')
    selectedIds.value = []
    fetchData()
    fetchPlaylists()
    fetchSingerList()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message)
  }
}

// ===== 歌单删除 =====
async function handlePlaylistDelete(playlist) {
  try {
    await ElMessageBox.confirm(
      `删除歌单「${playlist.name}」？\n仅属于此歌单的歌曲将被一并删除。`,
      '确认删除歌单',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await deletePlaylist(playlist.id)
    ElMessage.success('歌单已删除')
    fetchPlaylists()
    fetchData()
    fetchSingerList()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message)
  }
}

// ===== 导入 / 导出 =====
async function handleTextImport(list) {
  try {
    const r = await batchImportMusic(list)
    const msg = r.data?.message
    ElMessage.success((msg && msg !== 'success' && msg !== '操作成功') ? msg : `已导入 ${list.length} 首`)
    fetchData()
    fetchPlaylists()
    fetchSingerList()
  } catch (e) {
    ElMessage.error(e.message || '导入失败')
    throw e
  }
}

async function handleExport(fmt) {
  try {
    let songs
    if (selectedIds.value.length) {
      // 有选中时，先拉取全量（不受分页限制），再按选中的 ID 过滤
      const r = await getMusicList({
        keyword: keyword.value || undefined,
        singer: filter.value.singer || undefined,
        starRatingMin: filter.value.starRatingMin ?? undefined,
        starRatingMax: filter.value.starRatingMax ?? undefined,
        hasStar: filter.value.hasStar ?? undefined,
      })
      const all = r.data?.data || r.data || []
      songs = all.filter(i => selectedIds.value.includes(i.id))
    } else {
      const r = await getMusicList({
        keyword: keyword.value || undefined,
        singer: filter.value.singer || undefined,
        starRatingMin: filter.value.starRatingMin ?? undefined,
        starRatingMax: filter.value.starRatingMax ?? undefined,
        hasStar: filter.value.hasStar ?? undefined,
      })
      songs = r.data?.data || r.data || []
    }
    if (!songs.length) return ElMessage.warning('无数据')

    // 按评分降序排列
    songs.sort((a, b) => (Number(b.starRating) || 0) - (Number(a.starRating) || 0))

    let out = ''
    if (fmt === 'pipe') {
      let currentStar = -1
      out = songs.map(s => {
        const star = Number(s.starRating) || 0
        const starLine = star !== currentStar ? (currentStar = star, `⭐ ${star > 0 ? star + '星' : '未评分'}\n`) : ''
        const album = (expAlbum.value && s.album) ? ` | ${s.album}` : ''
        const notes = (expNotes.value && s.notes) ? ` | "${s.notes}"` : ''
        return starLine + `${s.artist} | ${s.title}${album}${notes}`
      }).join('\n')
    } else if (fmt === 'text') {
      let currentStar = -1
      out = songs.map(s => {
        const star = Number(s.starRating) || 0
        const starLine = star !== currentStar ? (currentStar = star, `${star > 0 ? star + '星' : '未评分'}\n`) : ''
        const album = (expAlbum.value && s.album) ? `，专辑：${s.album}` : ''
        const notes = (expNotes.value && s.notes) ? `，备注：${s.notes}` : ''
        return starLine + `歌手：${s.artist}，歌名：${s.title}${album}${notes}`
      }).join('\n')
    } else if (fmt === 'json') {
      // JSON 结构化导出
      out = JSON.stringify(songs.map(s => ({
        title: s.title,
        artist: s.artist,
        starRating: Number(s.starRating) || 0,
        album: s.album || '',
        notes: s.notes || '',
        coverUrl: s.coverUrl || ''
      })), null, 2)
    } else {
      // 横杠风格
      let currentStar = -1
      out = songs.map(s => {
        const star = Number(s.starRating) || 0
        const starLine = star !== currentStar ? (currentStar = star, `${star > 0 ? star + '星' : '未评分'}\n`) : ''
        const album = (expAlbum.value && s.album) ? ` - ${s.album}` : ''
        const notes = (expNotes.value && s.notes) ? ` "${s.notes}"` : ''
        return starLine + `- ${s.artist} - ${s.title}${album}${notes}`
      }).join('\n')
    }

    await navigator.clipboard.writeText(out.trim())
    ElMessage.success(`已复制 ${songs.length} 首 (${fmt === 'pipe' ? '竖线' : fmt === 'text' ? '文字' : fmt === 'json' ? 'JSON' : '横杠'}风格)`)
  } catch (e) { ElMessage.error(e.message) }
}

// ===== 登录 =====
function handleLoginSuccess(t) { applyToken(t) }
function handleLogout() {
  localStorage.removeItem('kugou_token')
  delete axios.defaults.headers.common['X-Kugou-Token']
  document.cookie = 'token=; path=/; max-age=0'
  isLoggedIn.value = false
}
function applyToken(t) {
  localStorage.setItem('kugou_token', t)
  isLoggedIn.value = true
  axios.defaults.headers.common['X-Kugou-Token'] = t
  document.cookie = `token=${t}; path=/; max-age=2592000; SameSite=Lax`
  document.cookie = `userid=2150217007; path=/; max-age=2592000; SameSite=Lax`
  document.cookie = `dfid=6ef29622fa1a716815dce182ef0356d5; path=/; max-age=2592000; SameSite=Lax`
  document.cookie = `mid=39d79bd96d1ef95ba28f2bcee4c199ae; path=/; max-age=2592000; SameSite=Lax`
}
async function checkLogin() {
  try {
    const r = await getTokenStatus()
    const bt = r.data?.data?.token || r.data?.token
    if (bt) { applyToken(bt); return }
  } catch {}
  // 后端没有 token，尝试从本地读取
  const lt = localStorage.getItem('kugou_token')
  if (lt) {
    applyToken(lt)
    // 本地有但后端没有 → 自动同步，让其他设备也能获取 token
    syncTokenToBackend(lt).catch(() => {})
  }
}

// ===== 酷狗同步 =====

/** 检查酷狗响应是否成功或 token 过期 */
function checkKugouData(data) {
  if (!data) return 'empty'
  const ec = data.error_code
  if (ec === 0 || data.status === 1) return 'ok'
  if (ec === 20010 || ec === 20017) {
    ElMessage.error('酷狗登录已过期，请重新登录')
    handleLogout()
    return 'expired'
  }
  if (ec) {
    ElMessage.error('酷狗API错误 (error_code=' + ec + ')')
    return 'error'
  }
  return 'ok'
}

/** 从 axios 错误中提取响应数据并检查 */
function checkKugouError(e) {
  const data = e.response?.data
  const result = checkKugouData(data)
  if (result !== 'empty') return result
  // 真正的网络错误
  const status = e.response?.status
  if (status === 502 || !e.response) {
    ElMessage.error('无法连接酷狗服务，请确认 KuGouMusicApi 已启动 (端口3000)')
  } else {
    ElMessage.error('获取歌单失败')
  }
  return 'error'
}

async function openSyncDialog() {
  if (!isLoggedIn.value) { ElMessage.warning('请先登录'); loginDialogVisible.value = true; return }
  syncDialogVisible.value = true; loadingPlaylists.value = true
  try {
    const r = await axios.get('/kugou/user/playlist')
    const status = checkKugouData(r.data)
    if (status !== 'ok') { syncDialogVisible.value = false; loadingPlaylists.value = false; return }

    kugouPlaylists.value = (r.data?.data?.info || r.data?.data || [])
      .filter(p => {
        const uid = String(p.list_create_userid || p.userid || '')
        return p.is_mine === 1 || uid === '2150217007'
      })
      .map(p => ({
        id: p.global_collection_id || p.id,
        name: p.name,
        coverUrl: (p.pic || '').replace('{size}', '200'),
        trackCount: p.count || 0
      }))
  } catch (e) {
    checkKugouError(e)
    syncDialogVisible.value = false; loadingPlaylists.value = false; return
  }
  loadingPlaylists.value = false
  kugouPlaylists.value.length
    ? ElMessage.success(`获取 ${kugouPlaylists.value.length} 个歌单`)
    : ElMessage.warning('无自建歌单')
}

async function handleKugouSync(ids) {
  syncing.value = true; let n = 0
  let expired = false
  try {
    for (const pid of ids) {
      const pl = kugouPlaylists.value.find(p => p.id === pid); if (!pl) continue
      ElMessage.info(`导入: ${pl.name}...`); await new Promise(r => setTimeout(r, 800))
      try {
        const r = await axios.get(`/kugou/playlist/track/all?id=${pid}`)
        const status = checkKugouData(r.data)
        if (status === 'expired') { expired = true; return }
        if (status !== 'ok') continue

        const sd = r.data?.data?.songs || r.data?.songs || r.data?.data || []
        if (!sd.length) continue
        const tracks = sd.map(s => {
          const si = s.singerinfo?.[0] || {}
          return {
            title: (s.name || '').replace(`${si.name} - `, ''),
            artist: si.name || '未知',
            album: s.albuminfo?.name || '',
            coverUrl: (s.cover || '').replace('{size}', '200')
          }
        })
        const axiosRes = await axios.post('/api/sync/batch', tracks, {
          params: { category: pl.name, playlistCover: pl.coverUrl }
        })
        n += axiosRes.data?.data?.newMusicCount || tracks.length
      } catch (e) {
        const status = checkKugouError(e)
        if (status === 'expired') { expired = true; return }
      }
    }
    ElMessage.success(`同步完成！导入 ${n} 首`)
    syncDialogVisible.value = false
    fetchData(); fetchPlaylists(); fetchSingerList()
  } catch (e) {
    if (!expired) ElMessage.error('同步失败')
  } finally { syncing.value = false }
}

// ===== 初始化 =====
onMounted(() => {
  checkLogin()
  fetchData()
  fetchPlaylists()
  fetchSingerList()  // 获取全部歌手（不受筛选影响）
})
</script>

<style scoped>
.app-shell {
  min-height: 100dvh;
  background: var(--bg);
  max-width: 480px;
  margin: 0 auto;
  padding: 0 20px calc(80px + var(--safe));
}

/* === 头部 === */
.app-header { padding: 32px 0 20px; }
.header-top { display: flex; align-items: baseline; justify-content: space-between; }
.header-top h1 {
  font-size: 24px; font-weight: 300; letter-spacing: 1px; color: var(--text);
  margin: 0; line-height: 1.3;
}
.header-sub { font-size: 13px; color: var(--text-muted); font-weight: 400; }
.stats-line { display: flex; gap: 24px; margin-top: 16px; }
.st-item { display: flex; align-items: baseline; gap: 6px; }
.st-num {
  font-size: 28px; font-weight: 200; color: var(--text);
  letter-spacing: -0.5px; line-height: 1;
}
.st-lbl { font-size: 12px; color: var(--text-muted); font-weight: 400; }

/* === 搜索 === */
.search-wrap {
  position: relative; margin-bottom: 20px;
}
.search-input {
  width: 100%; height: 48px; padding: 0 20px 0 42px;
  background: var(--surface); border: 1px solid var(--border-light);
  border-radius: 24px; font-size: 15px; color: var(--text);
  outline: none; transition: all 0.25s; font-family: inherit;
}
.search-input::placeholder { color: var(--text-muted); }
.search-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px var(--primary-bg);
}
.search-icon {
  position: absolute; left: 16px; top: 50%;
  transform: translateY(-50%); pointer-events: none;
}

/* === 面包屑 === */
.breadcrumb-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 0; margin-bottom: 12px;
  border-bottom: 1px solid var(--border-light); font-size: 13px;
}
.bread-back {
  color: var(--primary); font-weight: 500;
  background: none; border: none; cursor: pointer;
  font-size: 13px; padding: 4px 0; font-family: inherit;
}
.bread-sep, .bread-cnt { color: var(--text-muted); }
.bread-cur { font-weight: 500; }

/* === 标签切换 === */
.app-tabs {
  display: flex; gap: 28px; margin-bottom: 18px;
  border-bottom: 1px solid var(--border-light); padding-bottom: 10px;
}
.tab-btn {
  font-size: 14px; font-weight: 400; color: var(--text-muted);
  cursor: pointer; border: none; background: none;
  padding: 4px 0; transition: color 0.2s; position: relative;
  display: flex; align-items: center; gap: 6px; font-family: inherit;
}
.tab-btn.active { color: var(--text); font-weight: 500; }
.tab-btn.active::after {
  content: ''; position: absolute; bottom: -11px; left: 0; right: 0;
  height: 2px; background: var(--primary); border-radius: 1px;
}
.tab-cnt { font-size: 11px; color: var(--text-muted); margin-left: 4px; font-weight: 400; }

/* === 工具栏 === */
.toolbar {
  display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; align-items: center;
}
.tb-group {
  display: flex; gap: 6px; align-items: center;
}
:deep(.tbtn-primary) {
  height: 36px !important; padding: 0 14px !important;
  background: var(--primary) !important; color: #fff !important;
  border: none !important; border-radius: 18px !important;
  font-weight: 600 !important; font-size: 13px !important;
}
:deep(.tbtn-primary:hover) { background: var(--primary-hover) !important; }
:deep(.tbtn-ghost) {
  height: 36px !important; padding: 0 14px !important;
  background: var(--surface) !important; color: var(--text-secondary) !important;
  border: 1px solid var(--border-light) !important; border-radius: 18px !important;
  font-size: 13px !important;
}
:deep(.tbtn-ghost:hover) { background: var(--bg-warm) !important; }

/* === 导出下拉 === */
.export-wrap {
  position: relative; display: inline-flex;
}
.export-drop {
  position: absolute; top: calc(100% + 4px); right: 0;
  min-width: 200px;
  background: var(--surface); border: 1px solid var(--border-light);
  border-radius: 12px; box-shadow: var(--shadow-md);
  padding: 6px; z-index: 60;
  display: flex; flex-direction: column; gap: 2px;
}
.export-drop button {
  display: block; width: 100%; padding: 8px 14px; border: none;
  background: transparent; border-radius: 8px;
  font-size: 13px; color: var(--text-secondary); cursor: pointer;
  text-align: left; font-family: inherit; white-space: nowrap;
  transition: all 0.12s;
}
.export-drop button:hover { background: var(--bg-warm); color: var(--text); }
.export-opts {
  display: flex; gap: 12px; padding: 6px 10px 8px;
  border-bottom: 1px solid var(--border-light); margin-bottom: 2px;
}
.exp-opt {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--text-secondary); cursor: pointer;
  user-select: none;
}
.exp-opt input[type="checkbox"] {
  accent-color: var(--primary); width: 14px; height: 14px;
}
.fselect-backdrop { position: fixed; inset: 0; z-index: 40; }

/* ===== 响应式 ===== */

/* 移动端 (<768px) */
@media (max-width: 767px) {
  .app-shell { padding: 0 16px calc(60px + var(--safe)); }
  .app-header { padding: 24px 0 16px; }
  .header-top h1 { font-size: 20px; }
  .stats-line { gap: 18px; }
  .st-num { font-size: 24px; }
  .toolbar { gap: 5px; flex-wrap: nowrap; justify-content: space-between; padding: 0 2px; }
	  .tb-group { gap: 5px; }
	  :deep(.tbtn-primary), :deep(.tbtn-ghost) {
	    gap: 5px !important;
	    padding: 0 10px !important; font-size: 12px !important; height: 32px !important;
	  }
  /* 导出下拉：移动端左对齐 */
  .export-drop { right: 0; left: auto; min-width: 170px; }
}

/* 平板 (≥768px) */
@media (min-width: 768px) {
  .app-shell { max-width: 600px; padding: 0 32px calc(80px + var(--safe)); }
}

/* 桌面端 (≥1024px) */
@media (min-width: 1024px) {
  .app-shell { max-width: 720px; }
  .header-top h1 { font-size: 28px; }
  .stats-line { gap: 36px; }
  .st-num { font-size: 32px; }
  .st-lbl { font-size: 13px; }
}
</style>
