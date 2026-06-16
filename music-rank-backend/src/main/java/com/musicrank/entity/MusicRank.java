package com.musicrank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("music_rank")
public class MusicRank {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String artist;
    private BigDecimal starRating;
    private String album;
    private String coverUrl;
    private String notes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 临时字段：用于存储这首歌所属的歌单名称（单个，兼容旧逻辑）
    @TableField(exist = false)
    private String category;

    // 临时字段：用于存储这首歌所属的所有歌单名称列表（多歌单支持）
    @TableField(exist = false)
    private java.util.List<String> playlistNames;
}