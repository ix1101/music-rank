package com.musicrank.controller;

import com.musicrank.common.Result;
import com.musicrank.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
