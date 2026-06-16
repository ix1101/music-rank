package com.musicrank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicrank.entity.MusicRank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MusicRankMapper extends BaseMapper<MusicRank> {
    // 手动添加批量插入方法
    int insertBatch(@Param("list") List<MusicRank> list);

    // 根据歌单ID分页查询歌曲
    Page<MusicRank> selectByPlaylistId(
            Page<MusicRank> page,
            @Param("playlistId") Long playlistId,
            @Param("keyword") String keyword
    );

    // 聚合统计：总数 + 均分（仅计已打分的歌曲）
    Map<String, Object> selectStats(
            @Param("keyword") String keyword,
            @Param("singer") String singer,
            @Param("starRatingMin") java.math.BigDecimal starRatingMin,
            @Param("starRatingMax") java.math.BigDecimal starRatingMax,
            @Param("hasStar") Boolean hasStar,
            @Param("album") String album,
            @Param("playlistId") Long playlistId
    );

    // 批量查询歌曲所属的歌单名称
    List<Map<String, Object>> selectPlaylistNamesByMusicIds(@Param("musicIds") List<Long> musicIds);
}
