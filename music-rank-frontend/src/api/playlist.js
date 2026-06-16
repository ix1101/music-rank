import apiClient from './request'

/**
 * 歌单相关 API
 */
export function getPlaylists() {
  return apiClient.get('/playlist/list')
}

export function deletePlaylist(id) {
  return apiClient.delete(`/playlist/${id}`)
}

export function createPlaylist(name, coverUrl = '') {
  return apiClient.post('/playlist', { name, coverUrl })
}
