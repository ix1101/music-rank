package com.musicrank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicrank.dto.MusicQueryDTO;
import com.musicrank.dto.SyncResultDTO;
import com.musicrank.entity.MusicRank;

import java.util.List;

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
}
