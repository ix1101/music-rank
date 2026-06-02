package com.musicrank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("playlist_music")
public class PlaylistMusic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long playlistId;
    private Long musicId;
    private LocalDateTime createTime;
}