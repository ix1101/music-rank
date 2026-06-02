package com.musicrank.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicrank.common.Result;
import com.musicrank.dto.MusicQueryDTO;
import com.musicrank.entity.MusicRank;
import com.musicrank.service.MusicRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 音乐排行控制器
 * 负责歌曲的增删改查 API
 */
@RestController
@RequestMapping("/api/music")
public class MusicRankController {

    @Autowired
    private MusicRankService musicRankService;

    /**
     * 分页查询歌曲（支持歌单筛选、关键词搜索、歌手筛选、星级筛选）
     */
    @GetMapping("/page")
    public Result<Page<MusicRank>> getPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long playlistId,
            @RequestParam(required = false) String singer,
            @RequestParam(required = false) java.math.BigDecimal starRating,
            @RequestParam(required = false) Boolean hasStar) {

        MusicQueryDTO query = new MusicQueryDTO();
        query.setCurrent(current);
        query.setSize(size);
        query.setKeyword(keyword);
        query.setPlaylistId(playlistId);
        query.setSinger(singer);
        query.setStarRating(starRating);
        query.setHasStar(hasStar);

        return Result.success(musicRankService.getPage(query));
    }

    /**
     * 获取歌曲列表（支持筛选，用于导出）
     * 注意：不传 hasStar 时不设默认过滤，返回所有歌曲
     */
    @GetMapping("/list")
    public Result<List<MusicRank>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String singer,
            @RequestParam(required = false) java.math.BigDecimal starRating,
            @RequestParam(required = false) Boolean hasStar) {

        MusicQueryDTO query = new MusicQueryDTO();
        query.setKeyword(keyword);
        query.setSinger(singer);
        query.setStarRating(starRating);
        query.setHasStar(hasStar);

        return Result.success(musicRankService.getList(query));
    }

    /**
     * 添加单首歌曲
     */
    @PostMapping("/add")
    public Result<MusicRank> addMusic(@RequestBody MusicRank music) {
        MusicRank saved = musicRankService.addMusic(music);
        return Result.success("添加成功", saved);
    }

    /**
     * 批量导入歌曲（文本解析后使用）
     */
    @PostMapping("/batch-import")
    public Result<String> batchImport(@RequestBody List<MusicRank> list) {
        if (list == null || list.isEmpty()) {
            return Result.badRequest("导入列表不能为空");
        }
        int count = musicRankService.batchImport(list);
        return Result.success("成功导入 " + count + " 条记录");
    }

    /**
     * 批量删除歌曲
     */
    @PostMapping("/batch-delete")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        int deleted = musicRankService.batchDelete(ids);
        return Result.success("成功删除 " + deleted + " 条记录");
    }
}
