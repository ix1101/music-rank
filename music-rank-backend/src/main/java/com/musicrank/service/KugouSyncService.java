package com.musicrank.service;

import com.musicrank.dto.SyncResultDTO;
import com.musicrank.entity.MusicRank;

import java.util.List;
import java.util.Map;

/**
 * 酷狗同步服务接口
 */
public interface KugouSyncService {

    /**
     * 获取用户的酷狗歌单列表
     */
    List<Map<String, Object>> fetchKugouPlaylists();

    /**
     * 获取指定酷狗歌单中的所有歌曲
     */
    List<MusicRank> fetchPlaylistTracks(String playlistId);

    /**
     * 批量导入歌曲到指定歌单
     */
    SyncResultDTO batchImportSongs(List<MusicRank> songs, String category, String playlistCover);

    /**
     * 刷新登录 token（调用 /login/token 续期）
     * @return 新的 token，失败返回 null
     */
    String refreshToken();

    /**
     * 更新 token（由前端登录后同步）
     */
    void updateToken(String newToken);

    /**
     * 获取当前 token 状态信息
     */
    Map<String, Object> getTokenStatus();
}
