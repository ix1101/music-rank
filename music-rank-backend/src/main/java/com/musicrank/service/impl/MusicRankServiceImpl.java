package com.musicrank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicrank.dto.MusicQueryDTO;
import com.musicrank.entity.MusicRank;
import com.musicrank.mapper.MusicRankMapper;
import com.musicrank.service.MusicRankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 音乐排行服务实现
 */
@Service
public class MusicRankServiceImpl implements MusicRankService {

    private static final Logger log = LoggerFactory.getLogger(MusicRankServiceImpl.class);

    @Autowired
    private MusicRankMapper musicRankMapper;

    @Override
    public Page<MusicRank> getPage(MusicQueryDTO query) {
        Page<MusicRank> page = new Page<>(query.getCurrent(), query.getSize());

        // 如果指定了歌单ID，走歌单关联查询
        if (query.getPlaylistId() != null) {
            return musicRankMapper.selectByPlaylistId(page, query.getPlaylistId(), query.getKeyword());
        }

        // 构建通用查询条件
        LambdaQueryWrapper<MusicRank> wrapper = buildFilterWrapper(query);
        wrapper.orderByDesc(MusicRank::getStarRating)
               .orderByDesc(MusicRank::getCreateTime);
        return musicRankMapper.selectPage(page, wrapper);
    }

    @Override
    public List<MusicRank> getList(MusicQueryDTO query) {
        LambdaQueryWrapper<MusicRank> wrapper = buildFilterWrapper(query);
        wrapper.orderByDesc(MusicRank::getStarRating)
               .orderByDesc(MusicRank::getCreateTime);
        return musicRankMapper.selectList(wrapper);
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

    /**
     * 根据查询参数构建 MyBatis-Plus 查询条件
     * 抽取公共方法，避免 getPage 和 getList 中的重复代码
     */
    private LambdaQueryWrapper<MusicRank> buildFilterWrapper(MusicQueryDTO query) {
        LambdaQueryWrapper<MusicRank> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（歌名或歌手）
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w
                .like(MusicRank::getTitle, query.getKeyword())
                .or()
                .like(MusicRank::getArtist, query.getKeyword())
            );
        }

        // 按歌手精确筛选
        if (query.getSinger() != null && !query.getSinger().trim().isEmpty()) {
            wrapper.eq(MusicRank::getArtist, query.getSinger());
        }

        // 按星级精确筛选
        if (query.getStarRating() != null) {
            wrapper.eq(MusicRank::getStarRating, query.getStarRating());
        }

        // 按是否打星筛选
        if (query.getHasStar() != null) {
            if (query.getHasStar()) {
                wrapper.gt(MusicRank::getStarRating, 0);
            } else {
                wrapper.eq(MusicRank::getStarRating, 0);
            }
        }

        return wrapper;
    }
}
