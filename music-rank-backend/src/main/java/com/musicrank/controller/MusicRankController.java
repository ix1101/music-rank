package com.musicrank.controller;

import com.musicrank.common.Result;
import com.musicrank.dto.MusicPageResult;
import com.musicrank.dto.MusicQueryDTO;
import com.musicrank.entity.MusicRank;
import com.musicrank.service.MusicRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
     * 分页查询歌曲（支持歌单筛选、关键词搜索、歌手筛选、星级筛选、专辑筛选）
     * 响应中合并了聚合统计（total / avgRating），前端无需再单独请求 /stats
     */
    @GetMapping("/page")
    public Result<MusicPageResult> getPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long playlistId,
            @RequestParam(required = false) String singer,
            @RequestParam(required = false) java.math.BigDecimal starRatingMin,
            @RequestParam(required = false) java.math.BigDecimal starRatingMax,
            @RequestParam(required = false) Boolean hasStar,
            @RequestParam(required = false) String album) {

        MusicQueryDTO query = new MusicQueryDTO();
        query.setCurrent(current);
        query.setSize(size);
        query.setKeyword(keyword);
        query.setPlaylistId(playlistId);
        query.setSinger(singer);
        query.setStarRatingMin(starRatingMin);
        query.setStarRatingMax(starRatingMax);
        query.setHasStar(hasStar);
        query.setAlbum(album);

        // 合并分页数据和聚合统计，减少一次 HTTP 往返
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MusicRank> page = musicRankService.getPage(query);
        Map<String, Object> stats = musicRankService.getStats(query);

        MusicPageResult result = new MusicPageResult();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        // SQLite JDBC 返回 Double，MySQL 返回 BigDecimal，做安全转换
        Object avgObj = stats.get("avgRating");
        BigDecimal avgRating = BigDecimal.ZERO;
        if (avgObj instanceof BigDecimal bd) {
            avgRating = bd;
        } else if (avgObj instanceof Double d) {
            avgRating = BigDecimal.valueOf(d);
        } else if (avgObj instanceof Number n) {
            avgRating = BigDecimal.valueOf(n.doubleValue());
        }
        result.setAvgRating(avgRating);

        return Result.success(result);
    }

    /**
     * 获取歌曲列表（支持筛选，用于导出）
     */
    @GetMapping("/list")
    public Result<List<MusicRank>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String singer,
            @RequestParam(required = false) java.math.BigDecimal starRatingMin,
            @RequestParam(required = false) java.math.BigDecimal starRatingMax,
            @RequestParam(required = false) Boolean hasStar,
            @RequestParam(required = false) String album) {

        MusicQueryDTO query = new MusicQueryDTO();
        query.setKeyword(keyword);
        query.setSinger(singer);
        query.setStarRatingMin(starRatingMin);
        query.setStarRatingMax(starRatingMax);
        query.setHasStar(hasStar);
        query.setAlbum(album);

        return Result.success(musicRankService.getList(query));
    }

    /**
     * 获取聚合统计（匹配条件的总歌曲数 + 已打星均分）
     * 用于首页头部数字展示，计算全部匹配歌曲的均分而非仅当前页
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String singer,
            @RequestParam(required = false) java.math.BigDecimal starRatingMin,
            @RequestParam(required = false) java.math.BigDecimal starRatingMax,
            @RequestParam(required = false) Boolean hasStar,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Long playlistId) {

        MusicQueryDTO query = new MusicQueryDTO();
        query.setKeyword(keyword);
        query.setSinger(singer);
        query.setStarRatingMin(starRatingMin);
        query.setStarRatingMax(starRatingMax);
        query.setHasStar(hasStar);
        query.setAlbum(album);
        query.setPlaylistId(playlistId);

        return Result.success(musicRankService.getStats(query));
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

    /**
     * 更新歌曲信息
     */
    @PutMapping("/{id}")
    public Result<MusicRank> updateMusic(@PathVariable Long id, @RequestBody MusicRank music) {
        MusicRank updated = musicRankService.updateMusic(id, music);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除单首歌曲
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteMusic(@PathVariable Long id) {
        musicRankService.deleteMusic(id);
        return Result.success("删除成功");
    }

    /**
     * 快速更新星级评分
     */
    @PatchMapping("/{id}/star")
    public Result<MusicRank> updateStarRating(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> body) {
        BigDecimal starRating = body.get("starRating");
        if (starRating == null) {
            return Result.badRequest("starRating 不能为空");
        }
        MusicRank updated = musicRankService.updateStarRating(id, starRating);
        return Result.success("评分更新成功", updated);
    }

    /**
     * 获取歌曲所属的所有歌单
     */
    @GetMapping("/{id}/playlists")
    public Result<List<Map<String, Object>>> getSongPlaylists(@PathVariable Long id) {
        return Result.success(musicRankService.getSongPlaylists(id));
    }

    /**
     * 更新歌曲的歌单归属（替换全部）
     */
    @PutMapping("/{id}/playlists")
    public Result<?> updateSongPlaylists(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> playlistIds = body.get("playlistIds");
        musicRankService.updateSongPlaylists(id, playlistIds);
        return Result.success("歌单更新成功");
    }
}
