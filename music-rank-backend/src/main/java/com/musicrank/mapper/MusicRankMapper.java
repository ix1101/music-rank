package com.musicrank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicrank.entity.MusicRank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MusicRankMapper extends BaseMapper<MusicRank> {
    // 手动添加批量插入方法
    int insertBatch(@Param("list") List<MusicRank> list);

    // 新增：根据歌单ID分页查询歌曲
    Page<MusicRank> selectByPlaylistId(
            Page<MusicRank> page,
            @Param("playlistId") Long playlistId,
            @Param("keyword") String keyword
    );
}