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

export function addMusic(data) {
  return apiClient.post('/music/add', data)
}

export function batchImportMusic(data) {
  return apiClient.post('/music/batch-import', data)
}

export function batchDeleteMusic(ids) {
  return apiClient.post('/music/batch-delete', ids)
}
