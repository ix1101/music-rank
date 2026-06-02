package com.musicrank.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicrank.common.TokenManager;
import com.musicrank.dto.SyncResultDTO;
import com.musicrank.entity.MusicRank;
import com.musicrank.entity.PlaylistMusic;
import com.musicrank.mapper.MusicRankMapper;
import com.musicrank.mapper.PlaylistMusicMapper;
import com.musicrank.service.KugouSyncService;
import com.musicrank.service.PlaylistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 酷狗同步服务实现
 * 通过调用本地 KuGouMusicApi (localhost:3000) 实现歌单同步
 */
@Service
public class KugouSyncServiceImpl implements KugouSyncService {

    private static final Logger log = LoggerFactory.getLogger(KugouSyncServiceImpl.class);

    /** KuGouMusicApi 地址 */
    private static final String KUGOU_API_BASE = "http://localhost:3000";

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private MusicRankMapper musicRankMapper;

    @Autowired
    private PlaylistMusicMapper playlistMusicMapper;

    @Autowired
    private PlaylistService playlistService;

    /**
     * 构建带认证 Cookie 的请求头
     * KuGouMusicApi 的模块从 cookie 中读取 userid 和 token
     */
    private Map<String, String> buildAuthHeaders() {
        String token = tokenManager.getToken();
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        // 持久设备指纹 + token
        headers.put("Cookie", "userid=" + TokenManager.KUGOU_USER_ID
            + "; token=" + (token != null ? token : "")
            + "; dfid=" + TokenManager.DFID
            + "; mid=" + TokenManager.MID);
        if (token != null) {
            headers.put("Authorization", token);
        }
        return headers;
    }

    /**
     * 调用 KuGouMusicApi 获取数据
     */
    private JSONObject callKugouApi(String path, Map<String, Object> params) {
        String url = KUGOU_API_BASE + path;

        HttpRequest request = HttpRequest.get(url)
                .addHeaders(buildAuthHeaders());

        if (params != null) {
            request.form(params);
        }

        try (HttpResponse response = request.execute()) {
            String body = response.body();
            if (response.getStatus() != 200) {
                log.error("酷狗API请求失败: {} -> status={}", url, response.getStatus());
                throw new RuntimeException("酷狗API请求失败: HTTP " + response.getStatus());
            }
            return JSONUtil.parseObj(body);
        } catch (Exception e) {
            log.error("调用酷狗API异常: {} -> {}", url, e.getMessage());
            throw new RuntimeException("酷狗API调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> fetchKugouPlaylists() {
        Map<String, Object> params = new HashMap<>();
        params.put("userid", TokenManager.KUGOU_USER_ID);

        JSONObject result = callKugouApi("/user/playlist", params);

        // 检查响应状态：status=1 成功，error_code 非 0 表示失败
        int errorCode = result.getInt("error_code", 0);
        if (errorCode != 0) {
            String errMsg = errorCode == 20010 || errorCode == 20017
                ? "酷狗登录已过期，请重新登录获取新 token"
                : "酷狗API错误 (error_code=" + errorCode + ")";
            log.error("获取酷狗歌单失败: {} (error_code={})", errMsg, errorCode);
            throw new RuntimeException(errMsg);
        }

        // 数据可能在 data 字段或直接返回
        JSONArray playlists = result.getJSONArray("data");
        if (playlists == null) {
            // 尝试从 data.info 读取（某些版本的酷狗API返回格式）
            JSONObject dataObj = result.getJSONObject("data");
            if (dataObj != null) {
                playlists = dataObj.getJSONArray("info");
            }
        }
        if (playlists == null || playlists.isEmpty()) {
            return new ArrayList<>();
        }

        // 只返回用户自建歌单（list_create_userid 匹配）
        List<Map<String, Object>> filteredList = new ArrayList<>();
        for (int i = 0; i < playlists.size(); i++) {
            JSONObject pl = playlists.getJSONObject(i);
            // 多种可能的字段名兼容
            String createUserId = pl.getStr("list_create_userid", pl.getStr("userid", ""));
            boolean isMine = pl.getInt("is_mine", 0) == 1;
            if (isMine || createUserId.equals(TokenManager.KUGOU_USER_ID)) {
                Map<String, Object> item = new HashMap<>();
                // 多种可能的 ID 字段名
                item.put("id", pl.getStr("global_collection_id", pl.getStr("id", "")));
                item.put("name", pl.getStr("name", ""));
                // 封面图 URL 处理
                String cover = pl.getStr("pic", pl.getStr("imgurl", ""));
                if (!cover.isEmpty()) {
                    cover = cover.replace("{size}", "200");
                }
                item.put("coverUrl", cover);
                item.put("trackCount", pl.getInt("count", 0));
                item.put("description", pl.getStr("intro", ""));
                filteredList.add(item);
            }
        }

        log.info("获取到 {} 个酷狗歌单（过滤后 {} 个）", playlists.size(), filteredList.size());
        return filteredList;
    }

    @Override
    public List<MusicRank> fetchPlaylistTracks(String playlistId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", playlistId);

        JSONObject result = callKugouApi("/playlist/track/all", params);

        if (result.getInt("status") != 1 && result.getInt("error_code") != 0) {
            String errMsg = result.getStr("error_msg", "未知错误");
            throw new RuntimeException("获取歌单歌曲失败: " + errMsg);
        }

        JSONArray tracks = result.getJSONArray("data");
        if (tracks == null || tracks.isEmpty()) {
            return new ArrayList<>();
        }

        List<MusicRank> musicList = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            JSONObject track = tracks.getJSONObject(i);
            MusicRank music = new MusicRank();
            music.setTitle(track.getStr("name", "未知歌曲"));
            music.setArtist(track.getStr("singer", "未知歌手"));
            music.setAlbum(track.getStr("album", ""));
            music.setCoverUrl(track.getStr("cover", ""));
            // 默认0分（未评分）
            music.setStarRating(BigDecimal.ZERO);
            musicList.add(music);
        }

        log.info("获取到歌单 {} 中的 {} 首歌曲", playlistId, musicList.size());
        return musicList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncResultDTO batchImportSongs(List<MusicRank> songs, String category, String playlistCover) {
        int newMusicCount = 0;
        int addedToPlaylistCount = 0;

        // 1. 获取或创建歌单
        Long localPlaylistId = playlistService.getOrCreatePlaylist(category, playlistCover);

        // 2. 逐个处理歌曲
        for (MusicRank song : songs) {
            // 检查歌曲是否已存在（歌手+歌名唯一）
            LambdaQueryWrapper<MusicRank> musicWrapper = new LambdaQueryWrapper<>();
            musicWrapper.eq(MusicRank::getTitle, song.getTitle())
                        .eq(MusicRank::getArtist, song.getArtist());
            MusicRank existingMusic = musicRankMapper.selectOne(musicWrapper);

            Long musicId;
            if (existingMusic == null) {
                // 新歌曲，插入
                if (song.getCreateTime() == null) {
                    song.setCreateTime(LocalDateTime.now());
                }
                if (song.getStarRating() == null) {
                    song.setStarRating(BigDecimal.ZERO);
                }
                musicRankMapper.insert(song);
                musicId = song.getId();
                newMusicCount++;
            } else {
                musicId = existingMusic.getId();
            }

            // 3. 建立歌单-歌曲关联
            LambdaQueryWrapper<PlaylistMusic> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(PlaylistMusic::getPlaylistId, localPlaylistId)
                           .eq(PlaylistMusic::getMusicId, musicId);

            if (playlistMusicMapper.selectCount(relationWrapper) == 0) {
                PlaylistMusic relation = new PlaylistMusic();
                relation.setPlaylistId(localPlaylistId);
                relation.setMusicId(musicId);
                relation.setCreateTime(LocalDateTime.now());
                playlistMusicMapper.insert(relation);
                addedToPlaylistCount++;
            }
        }

        SyncResultDTO result = new SyncResultDTO(category, newMusicCount, addedToPlaylistCount);
        log.info("同步完成: 歌单={}, 新增歌曲={}, 添加关联={}", category, newMusicCount, addedToPlaylistCount);
        return result;
    }

    // ==================== Token 管理 ====================

    @Override
    public String refreshToken() {
        String currentToken = tokenManager.getToken();
        if (currentToken == null || currentToken.isEmpty()) {
            log.warn("Token 为空，跳过刷新");
            return null;
        }

        log.info("正在刷新 token...");
        String url = KUGOU_API_BASE + "/login/token";
        Map<String, Object> params = new HashMap<>();
        params.put("userid", TokenManager.KUGOU_USER_ID);

        try (HttpResponse response = HttpRequest.get(url)
                .addHeaders(buildAuthHeaders())
                .form(params)
                .execute()) {

            JSONObject result = JSONUtil.parseObj(response.body());
            if (result.getInt("error_code", -1) == 0 || result.getInt("status", 0) == 1) {
                String newToken = result.getJSONObject("data") != null
                    ? result.getJSONObject("data").getStr("token")
                    : result.getStr("token");
                if (newToken != null && !newToken.isEmpty()) {
                    tokenManager.updateToken(newToken);
                    log.info("Token 刷新成功");
                    return newToken;
                }
            } else {
                log.warn("Token 刷新失败: error_code={}", result.getInt("error_code"));
            }
        } catch (Exception e) {
            log.error("Token 刷新异常: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void updateToken(String newToken) {
        tokenManager.updateToken(newToken);
    }

    @Override
    public Map<String, Object> getTokenStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("valid", tokenManager.isValid());
        status.put("lastUpdate", tokenManager.getLastUpdate().toString());
        status.put("hasToken", tokenManager.getToken() != null && !tokenManager.getToken().isEmpty());
        return status;
    }
}
