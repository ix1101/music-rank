package com.musicrank.service;

import java.util.List;
import java.util.Map;

/**
 * 歌单服务接口
 */
public interface PlaylistService {

    /**
     * 获取所有歌单及其歌曲数量
     */
    List<Map<String, Object>> getAllPlaylists();

    /**
     * 根据名称查找歌单（不存在则创建）
     */
    Long getOrCreatePlaylist(String name, String coverUrl);
}
