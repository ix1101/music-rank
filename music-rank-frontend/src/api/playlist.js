import apiClient from './request'

/**
 * 歌单相关 API
 */
export function getPlaylists() {
  return apiClient.get('/playlist/list')
}
