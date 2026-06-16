package com.musicrank.controller;

import com.musicrank.common.Result;
import com.musicrank.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 歌单控制器
 */
@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    /**
     * 获取所有歌单及其歌曲数量
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getAllPlaylists() {
        return Result.success(playlistService.getAllPlaylists());
    }

    /**
     * 删除歌单
     * 逻辑：如果歌曲只属于此歌单则同时删除歌曲；
     * 如果歌曲还属于其他歌单则只移除关联
     */
    @DeleteMapping("/{id}")
    public Result<?> deletePlaylist(@PathVariable Long id) {
        int deletedSongs = playlistService.deletePlaylist(id);
        return Result.success("歌单已删除，同时删除 " + deletedSongs + " 首关联歌曲");
    }

    /**
     * 创建或获取歌单（按名称查找，不存在则创建）
     */
    @PostMapping
    public Result<java.util.Map<String, Object>> createPlaylist(@RequestBody java.util.Map<String, String> body) {
        String name = body.get("name");
        String coverUrl = body.getOrDefault("coverUrl", "");
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("歌单名称不能为空");
        }
        Long id = playlistService.getOrCreatePlaylist(name.trim(), coverUrl);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", id);
        result.put("name", name.trim());
        return Result.success("歌单已就绪", result);
    }
}
