package com.musicrank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicrank.entity.Playlist;
import com.musicrank.mapper.PlaylistMapper;
import com.musicrank.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 歌单服务实现
 */
@Service
public class PlaylistServiceImpl implements PlaylistService {

    @Autowired
    private PlaylistMapper playlistMapper;

    @Override
    public List<Map<String, Object>> getAllPlaylists() {
        return playlistMapper.selectAllWithCount();
    }

    @Override
    public Long getOrCreatePlaylist(String name, String coverUrl) {
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Playlist::getName, name);
        Playlist playlist = playlistMapper.selectOne(wrapper);

        if (playlist == null) {
            // 不存在则创建
            playlist = new Playlist();
            playlist.setName(name);
            playlist.setCoverUrl(coverUrl != null ? coverUrl : "");
            playlist.setCreateTime(LocalDateTime.now());
            playlistMapper.insert(playlist);
        } else {
            // 存在且有新封面则更新
            if (coverUrl != null && !coverUrl.trim().isEmpty()) {
                playlist.setCoverUrl(coverUrl);
                playlistMapper.updateById(playlist);
            }
        }

        return playlist.getId();
    }
}
