package com.musicrank.dto;

import com.musicrank.entity.MusicRank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分页查询结果 DTO
 * 合并 Page 数据 + 聚合统计（total / avgRating）
 * 将原来的两次 HTTP 请求合并为一次，消除网络往返延迟
 */
@Data
public class MusicPageResult {

    /** 当前页歌曲列表 */
    private List<MusicRank> records;

    /** 全部匹配歌曲总数 */
    private long total;

    /** 当前页码 */
    private long current;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    /** 全部匹配歌曲的平均评分 */
    private BigDecimal avgRating;
}
