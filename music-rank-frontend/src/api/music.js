import apiClient from './request'

/**
 * 歌曲相关 API
 */
export function getMusicPage(params) {
  return apiClient.get('/music/page', { params })
}

export function getMusicList(params) {
  return apiClient.get('/music/list', { params })
}

export function getMusicStats(params) {
  return apiClient.get('/music/stats', { params })
}

export function addMusic(data) {
  return apiClient.post('/music/add', data)
}

export function batchImportMusic(data) {
  return apiClient.post('/music/batch-import', data)
}

export function batchDeleteMusic(ids) {
  return apiClient.post('/music/batch-delete', ids)
}

export function updateMusic(id, data) {
  return apiClient.put(`/music/${id}`, data)
}

export function deleteMusic(id) {
  return apiClient.delete(`/music/${id}`)
}

export function updateStarRating(id, starRating) {
  return apiClient.patch(`/music/${id}/star`, { starRating })
}

export function getSongPlaylists(id) {
  return apiClient.get(`/music/${id}/playlists`)
}

export function updateSongPlaylists(id, playlistIds) {
  return apiClient.put(`/music/${id}/playlists`, { playlistIds })
}
