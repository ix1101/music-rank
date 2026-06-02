package com.musicrank.controller;

import com.musicrank.common.Result;
import com.musicrank.dto.SyncResultDTO;
import com.musicrank.entity.MusicRank;
import com.musicrank.service.KugouSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 酷狗同步控制器
 * 负责从酷狗音乐获取歌单并同步到本地数据库
 */
@RestController
@RequestMapping("/api/sync")
public class KugouSyncController {

    private static final Logger log = LoggerFactory.getLogger(KugouSyncController.class);

    @Autowired
    private KugouSyncService kugouSyncService;

    /**
     * 获取用户的酷狗歌单列表（服务端直接调用 KuGouMusicApi）
     * 前端可调用此接口获取歌单列表供用户选择
     */
    @GetMapping("/kugou/playlists")
    public Result<List<Map<String, Object>>> getKugouPlaylists() {
        List<Map<String, Object>> playlists = kugouSyncService.fetchKugouPlaylists();
        return Result.success(playlists);
    }

    /**
     * 同步指定酷狗歌单到本地（一键同步）
     * 服务端完成：获取歌单歌曲 → 存入数据库 → 建立关联
     */
    @PostMapping("/kugou/playlist/{playlistId}")
    public Result<SyncResultDTO> syncKugouPlaylist(
            @PathVariable String playlistId,
            @RequestParam String category,
            @RequestParam(required = false) String playlistCover) {

        log.info("开始同步酷狗歌单: playlistId={}, category={}", playlistId, category);

        // 从酷狗获取歌曲列表
        List<MusicRank> tracks = kugouSyncService.fetchPlaylistTracks(playlistId);

        if (tracks.isEmpty()) {
            return Result.success("歌单为空，无需同步", new SyncResultDTO(category, 0, 0));
        }

        // 导入到本地数据库
        SyncResultDTO syncResult = kugouSyncService.batchImportSongs(tracks, category, playlistCover);
        return Result.success("同步完成", syncResult);
    }

    /**
     * 批量导入歌曲到指定歌单（前端传已获取的歌曲列表）
     * 保留此接口以兼容前端直接传歌单数据的场景
     */
    @PostMapping("/batch")
    public Result<SyncResultDTO> batchImportSongs(
            @RequestBody List<MusicRank> songs,
            @RequestParam String category,
            @RequestParam(required = false) String playlistCover) {

        if (songs == null || songs.isEmpty()) {
            return Result.badRequest("歌曲列表不能为空");
        }

        SyncResultDTO syncResult = kugouSyncService.batchImportSongs(songs, category, playlistCover);
        return Result.success(syncResult);
    }

    // ==================== Token 管理 ====================

    /**
     * 获取 token 状态
     */
    @GetMapping("/token/status")
    public Result<Map<String, Object>> getTokenStatus() {
        return Result.success(kugouSyncService.getTokenStatus());
    }

    /**
     * 前端登录后同步 token 到后端
     * 这样后端就能使用最新的 token 调用酷狗 API
     */
    @PostMapping("/token")
    public Result<String> updateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isEmpty()) {
            return Result.badRequest("token 不能为空");
        }
        kugouSyncService.updateToken(token);
        log.info("Token 已从前端同步更新");
        return Result.success("Token 已更新");
    }

    /**
     * 手动刷新 token
     */
    @PostMapping("/token/refresh")
    public Result<String> refreshToken() {
        String newToken = kugouSyncService.refreshToken();
        if (newToken != null) {
            return Result.success("Token 刷新成功", newToken);
        }
        return Result.error(401, "Token 刷新失败，请重新登录");
    }
}
