import apiClient from './request'

/**
 * 酷狗同步相关 API
 * 直接调用后端，由后端代理访问 KuGouMusicApi
 */
export function getKugouPlaylists() {
  return apiClient.get('/sync/kugou/playlists')
}

export function syncKugouPlaylist(playlistId, category, playlistCover) {
  return apiClient.post(`/sync/kugou/playlist/${playlistId}`, null, {
    params: { category, playlistCover }
  })
}

export function batchImportToPlaylist(songs, category, playlistCover) {
  return apiClient.post('/sync/batch', songs, {
    params: { category, playlistCover }
  })
}

/**
 * 将登录后的 token 同步到后端
 */
export function syncTokenToBackend(token) {
  return apiClient.post('/sync/token', { token })
}

/**
 * 获取后端 token 状态
 */
export function getTokenStatus() {
  return apiClient.get('/sync/token/status')
}
