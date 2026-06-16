package com.musicrank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicrank.dto.MusicQueryDTO;
import com.musicrank.entity.MusicRank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 音乐排行服务接口
 */
public interface MusicRankService {

    /**
     * 分页查询歌曲（支持多条件筛选）
     */
    Page<MusicRank> getPage(MusicQueryDTO query);

    /**
     * 获取歌曲列表（支持筛选，用于导出）
     */
    List<MusicRank> getList(MusicQueryDTO query);

    /**
     * 获取聚合统计（匹配条件的总数 + 已打星的均分）
     */
    Map<String, Object> getStats(MusicQueryDTO query);

    /**
     * 添加单首歌曲
     */
    MusicRank addMusic(MusicRank music);

    /**
     * 批量导入歌曲（使用真正的批量插入）
     */
    int batchImport(List<MusicRank> list);

    /**
     * 批量删除歌曲
     */
    int batchDelete(List<Long> ids);

    /**
     * 更新歌曲信息（部分更新，只更新非null字段）
     */
    MusicRank updateMusic(Long id, MusicRank music);

    /**
     * 删除单首歌曲
     */
    int deleteMusic(Long id);

    /**
     * 仅更新歌曲星级评分
     */
    MusicRank updateStarRating(Long id, BigDecimal starRating);

    /**
     * 获取歌曲所属的所有歌单
     */
    List<Map<String, Object>> getSongPlaylists(Long musicId);

    /**
     * 更新歌曲的歌单归属（替换全部）
     */
    void updateSongPlaylists(Long musicId, List<Long> playlistIds);

    /**
     * 批量填充歌曲的 playlistNames 字段
     */
    void populatePlaylistNames(List<MusicRank> songs);
}
