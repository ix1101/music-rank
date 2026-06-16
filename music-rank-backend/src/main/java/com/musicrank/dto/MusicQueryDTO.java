package com.musicrank.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 音乐查询参数 DTO
 * 用于统一接收前端传来的各种查询参数
 */
@Data
public class MusicQueryDTO {

    /** 当前页码（从1开始） */
    private Long current = 1L;

    /** 每页条数 */
    private Long size = 10L;

    /** 关键词搜索（歌名或歌手） */
    private String keyword;

    /** 歌单ID筛选 */
    private Long playlistId;

    /** 按歌手精确筛选 */
    private String singer;

    /** 星级下限（含） */
    private BigDecimal starRatingMin;

    /** 星级上限（含） */
    private BigDecimal starRatingMax;

    /**
     * 是否已打星
     * true: 只显示已打分（star_rating > 0）
     * false: 只显示未打分（star_rating = 0）
     * null: 不筛选
     */
    private Boolean hasStar;

    /** 按专辑筛选（模糊匹配） */
    private String album;
}
