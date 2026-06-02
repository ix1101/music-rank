package com.musicrank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicrank.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
    // 查询所有歌单及其歌曲数量
    @Select("SELECT p.id, p.name, p.cover_url AS coverUrl, COUNT(pm.music_id) AS trackCount " +
            "FROM playlist p " +
            "LEFT JOIN playlist_music pm ON p.id = pm.playlist_id " +
            "GROUP BY p.id, p.name, p.cover_url, p.create_time " + // 把所有非聚合字段都加进来
            "ORDER BY p.create_time DESC")
    List<Map<String, Object>> selectAllWithCount();
}