package com.musicrank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicrank.dto.MusicQueryDTO;
import com.musicrank.entity.MusicRank;
import com.musicrank.entity.Playlist;
import com.musicrank.entity.PlaylistMusic;
import com.musicrank.mapper.MusicRankMapper;
import com.musicrank.mapper.PlaylistMapper;
import com.musicrank.mapper.PlaylistMusicMapper;
import com.musicrank.service.MusicRankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 音乐排行服务实现
 */
@Service
public class MusicRankServiceImpl implements MusicRankService {

    private static final Logger log = LoggerFactory.getLogger(MusicRankServiceImpl.class);

    @Autowired
    private MusicRankMapper musicRankMapper;

    @Autowired
    private PlaylistMusicMapper playlistMusicMapper;

    @Autowired
    private PlaylistMapper playlistMapper;

    @Override
    public Page<MusicRank> getPage(MusicQueryDTO query) {
        Page<MusicRank> page = new Page<>(query.getCurrent(), query.getSize());

        // 如果指定了歌单ID，走歌单关联查询
        if (query.getPlaylistId() != null) {
            Page<MusicRank> result = musicRankMapper.selectByPlaylistId(page, query.getPlaylistId(), query.getKeyword());
            // 填充多歌单名称
            populatePlaylistNames(result.getRecords());
            return result;
        }

        // 构建通用查询条件
        LambdaQueryWrapper<MusicRank> wrapper = buildFilterWrapper(query);
        wrapper.orderByDesc(MusicRank::getStarRating)
               .orderByDesc(MusicRank::getCreateTime);
        Page<MusicRank> result = musicRankMapper.selectPage(page, wrapper);
        // 填充多歌单名称
        populatePlaylistNames(result.getRecords());
        return result;
    }

    @Override
    public List<MusicRank> getList(MusicQueryDTO query) {
        LambdaQueryWrapper<MusicRank> wrapper = buildFilterWrapper(query);
        wrapper.orderByDesc(MusicRank::getStarRating)
               .orderByDesc(MusicRank::getCreateTime);
        List<MusicRank> list = musicRankMapper.selectList(wrapper);
        populatePlaylistNames(list);
        return list;
    }

    @Override
    public Map<String, Object> getStats(MusicQueryDTO query) {
        return musicRankMapper.selectStats(
            query.getKeyword(),
            query.getSinger(),
            query.getStarRatingMin(),
            query.getStarRatingMax(),
            query.getHasStar(),
            query.getAlbum(),
            query.getPlaylistId()
        );
    }

    @Override
    public MusicRank addMusic(MusicRank music) {
        if (music.getCreateTime() == null) {
            music.setCreateTime(LocalDateTime.now());
        }
        musicRankMapper.insert(music);
        return music;
    }

    @Override
    public int batchImport(List<MusicRank> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        // 设置创建时间
        LocalDateTime now = LocalDateTime.now();
        list.forEach(m -> {
            if (m.getCreateTime() == null) {
                m.setCreateTime(now);
            }
        });

        int successCount = 0;
        List<MusicRank> toInsert = new ArrayList<>();

        for (MusicRank music : list) {
            try {
                // 先检查是否已存在
                LambdaQueryWrapper<MusicRank> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(MusicRank::getTitle, music.getTitle())
                       .eq(MusicRank::getArtist, music.getArtist());
                MusicRank existing = musicRankMapper.selectOne(wrapper);

                if (existing == null) {
                    toInsert.add(music);
                    successCount++;
                } else {
                    log.debug("跳过重复数据: {} - {}", music.getArtist(), music.getTitle());
                }
            } catch (Exception e) {
                log.warn("检查歌曲时出错: {} - {}, 错误: {}", music.getArtist(), music.getTitle(), e.getMessage());
            }
        }

        // 使用真正的批量插入（如果列表不为空）
        if (!toInsert.isEmpty()) {
            try {
                musicRankMapper.insertBatch(toInsert);
            } catch (Exception e) {
                // 批量插入失败时，逐条重试
                log.warn("批量插入失败，改为逐条插入: {}", e.getMessage());
                int fallbackCount = 0;
                for (MusicRank music : toInsert) {
                    try {
                        musicRankMapper.insert(music);
                        fallbackCount++;
                    } catch (DuplicateKeyException ignored) {
                        successCount--;
                    }
                }
                log.info("逐条插入完成: {} / {}", fallbackCount, toInsert.size());
            }
        }

        return successCount;
    }

    @Override
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("删除ID列表不能为空");
        }
        return musicRankMapper.deleteBatchIds(ids);
    }

    @Override
    public MusicRank updateMusic(Long id, MusicRank music) {
        if (id == null) {
            throw new IllegalArgumentException("歌曲ID不能为空");
        }
        MusicRank existing = musicRankMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("歌曲不存在");
        }
        music.setId(id);
        music.setUpdateTime(LocalDateTime.now());
        musicRankMapper.updateById(music);
        return musicRankMapper.selectById(id);
    }

    @Override
    public int deleteMusic(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("歌曲ID不能为空");
        }
        MusicRank existing = musicRankMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("歌曲不存在");
        }
        return musicRankMapper.deleteById(id);
    }

    @Override
    public MusicRank updateStarRating(Long id, BigDecimal starRating) {
        if (id == null) {
            throw new IllegalArgumentException("歌曲ID不能为空");
        }
        if (starRating == null || starRating.compareTo(BigDecimal.ZERO) < 0 || starRating.compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("星级评分必须在 0.0 ~ 5.0 之间");
        }
        MusicRank existing = musicRankMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("歌曲不存在");
        }
        MusicRank update = new MusicRank();
        update.setId(id);
        update.setStarRating(starRating);
        update.setUpdateTime(LocalDateTime.now());
        musicRankMapper.updateById(update);
        return musicRankMapper.selectById(id);
    }

    @Override
    public List<Map<String, Object>> getSongPlaylists(Long musicId) {
        LambdaQueryWrapper<PlaylistMusic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusic::getMusicId, musicId);
        List<PlaylistMusic> mappings = playlistMusicMapper.selectList(wrapper);
        if (mappings.isEmpty()) return Collections.emptyList();

        // 查询对应的歌单名称
        List<Long> playlistIds = mappings.stream().map(PlaylistMusic::getPlaylistId).collect(Collectors.toList());
        List<Playlist> playlists = playlistMapper.selectBatchIds(playlistIds);

        return playlists.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateSongPlaylists(Long musicId, List<Long> playlistIds) {
        // 删除旧关联
        LambdaQueryWrapper<PlaylistMusic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistMusic::getMusicId, musicId);
        playlistMusicMapper.delete(wrapper);

        // 添加新关联
        if (playlistIds != null && !playlistIds.isEmpty()) {
            for (Long playlistId : playlistIds) {
                PlaylistMusic pm = new PlaylistMusic();
                pm.setMusicId(musicId);
                pm.setPlaylistId(playlistId);
                pm.setCreateTime(LocalDateTime.now());
                playlistMusicMapper.insert(pm);
            }
        }
    }

    @Override
    public void populatePlaylistNames(List<MusicRank> songs) {
        if (songs == null || songs.isEmpty()) return;

        List<Long> musicIds = songs.stream().map(MusicRank::getId).collect(Collectors.toList());
        List<Map<String, Object>> mappings = musicRankMapper.selectPlaylistNamesByMusicIds(musicIds);

        // 按 musicId 分组
        Map<Long, List<String>> grouped = new HashMap<>();
        for (Map<String, Object> row : mappings) {
            Long musicId = ((Number) row.get("musicId")).longValue();
            String name = (String) row.get("playlistName");
            grouped.computeIfAbsent(musicId, k -> new ArrayList<>()).add(name);
        }

        // 设置到每个歌曲
        for (MusicRank song : songs) {
            List<String> names = grouped.getOrDefault(song.getId(), Collections.emptyList());
            song.setPlaylistNames(names);
            // 同时设置 category 为第一个歌单名（兼容旧逻辑）
            song.setCategory(names.isEmpty() ? null : names.get(0));
        }
    }

    /**
     * 根据查询参数构建 MyBatis-Plus 查询条件
     * 抽取公共方法，避免 getPage 和 getList 中的重复代码
     */
    private LambdaQueryWrapper<MusicRank> buildFilterWrapper(MusicQueryDTO query) {
        LambdaQueryWrapper<MusicRank> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（歌名、歌手或专辑）
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w
                .like(MusicRank::getTitle, query.getKeyword())
                .or()
                .like(MusicRank::getArtist, query.getKeyword())
                .or()
                .like(MusicRank::getAlbum, query.getKeyword())
            );
        }

        // 按歌手精确筛选
        if (query.getSinger() != null && !query.getSinger().trim().isEmpty()) {
            wrapper.eq(MusicRank::getArtist, query.getSinger());
        }

        // 按星级区间筛选（含下限，含上限）
        if (query.getStarRatingMin() != null) {
            wrapper.ge(MusicRank::getStarRating, query.getStarRatingMin());
        }
        if (query.getStarRatingMax() != null) {
            wrapper.le(MusicRank::getStarRating, query.getStarRatingMax());
        }

        // 按是否打星筛选
        if (query.getHasStar() != null) {
            if (query.getHasStar()) {
                wrapper.gt(MusicRank::getStarRating, 0);
            } else {
                wrapper.eq(MusicRank::getStarRating, 0);
            }
        }

        // 按专辑筛选
        if (query.getAlbum() != null && !query.getAlbum().trim().isEmpty()) {
            wrapper.like(MusicRank::getAlbum, query.getAlbum());
        }

        return wrapper;
    }
}
