package com.musicrank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicrank.entity.Playlist;
import com.musicrank.entity.PlaylistMusic;
import com.musicrank.mapper.MusicRankMapper;
import com.musicrank.mapper.PlaylistMapper;
import com.musicrank.mapper.PlaylistMusicMapper;
import com.musicrank.service.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 歌单服务实现
 */
@Service
public class PlaylistServiceImpl implements PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistServiceImpl.class);

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private PlaylistMusicMapper playlistMusicMapper;

    @Autowired
    private MusicRankMapper musicRankMapper;

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

    @Override
    @Transactional
    public int deletePlaylist(Long id) {
        // 查找该歌单下的所有歌曲关联
        LambdaQueryWrapper<PlaylistMusic> pmWrapper = new LambdaQueryWrapper<>();
        pmWrapper.eq(PlaylistMusic::getPlaylistId, id);
        List<PlaylistMusic> mappings = playlistMusicMapper.selectList(pmWrapper);

        int deletedSongs = 0;

        for (PlaylistMusic pm : mappings) {
            // 检查该歌曲是否还属于其他歌单
            LambdaQueryWrapper<PlaylistMusic> otherWrapper = new LambdaQueryWrapper<>();
            otherWrapper.eq(PlaylistMusic::getMusicId, pm.getMusicId());
            long otherCount = playlistMusicMapper.selectCount(otherWrapper);

            if (otherCount <= 1) {
                // 歌曲只属于此歌单 → 删除歌曲
                musicRankMapper.deleteById(pm.getMusicId());
                deletedSongs++;
            }
            // 无论如何删除 playlist_music 关联
            playlistMusicMapper.deleteById(pm.getId());
        }

        // 删除歌单本身
        playlistMapper.deleteById(id);

        log.info("删除歌单 id={}, 删除歌曲 {} 首, 移除关联 {} 条", id, deletedSongs, mappings.size());
        return deletedSongs;
    }
}
