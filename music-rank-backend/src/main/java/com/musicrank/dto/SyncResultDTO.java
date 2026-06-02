package com.musicrank.dto;

import lombok.Data;

/**
 * 同步结果 DTO
 * 封装导入结果信息
 */
@Data
public class SyncResultDTO {

    /** 歌单名称 */
    private String playlistName;

    /** 新增歌曲数量 */
    private int newMusicCount;

    /** 添加到歌单的歌曲数量 */
    private int addedToPlaylistCount;

    public SyncResultDTO(String playlistName, int newMusicCount, int addedToPlaylistCount) {
        this.playlistName = playlistName;
        this.newMusicCount = newMusicCount;
        this.addedToPlaylistCount = addedToPlaylistCount;
    }
}
