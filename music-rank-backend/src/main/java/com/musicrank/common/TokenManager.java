package com.musicrank.common;

import com.musicrank.entity.SystemConfig;
import com.musicrank.mapper.SystemConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Token 管理器
 * 持久化到 DB，重启不丢失
 */
@Component
public class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);
    private static final String CONFIG_KEY = "kugou_token";

    /** 当前 token */
    private volatile String token;

    /** token 最后更新时间 */
    private volatile Instant lastUpdate;

    /** 用户 ID（固定） */
    public static final String KUGOU_USER_ID = "2150217007";

    /** 持久设备指纹 */
    public static final String DFID = "6ef29622fa1a716815dce182ef0356d5";
    public static final String MID  = "39d79bd96d1ef95ba28f2bcee4c199ae";

    @Autowired
    private SystemConfigMapper configMapper;

    public TokenManager(@Value("${kugou.token}") String ymlToken) {
        // 优先读 DB，没有就用 yml 默认值并写入 DB
        String dbToken = loadFromDb();
        if (dbToken != null && !dbToken.isEmpty()) {
            this.token = dbToken;
            this.lastUpdate = Instant.now();
            log.info("TokenManager 从 DB 加载 token, 长度={}", dbToken.length());
        } else if (ymlToken != null && !ymlToken.isEmpty()) {
            this.token = ymlToken;
            this.lastUpdate = Instant.now();
            // 写入 DB 供后续使用
            saveToDb(ymlToken);
            log.info("TokenManager 从 yml 加载 token 并写入 DB, 长度={}", ymlToken.length());
        } else {
            this.token = "";
            this.lastUpdate = Instant.now();
            log.warn("TokenManager 初始化: token 为空!");
        }
    }

    public String getToken() {
        return token;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    /**
     * 更新 token（前端登录或刷新后调用）
     * 同时更新内存和 DB
     */
    public void updateToken(String newToken) {
        if (newToken != null && !newToken.isEmpty() && !newToken.equals(this.token)) {
            this.token = newToken;
            this.lastUpdate = Instant.now();
            saveToDb(newToken);
            log.info("Token 已更新并持久化到 DB, 长度={}", newToken.length());
        }
    }

    public boolean isValid() {
        return token != null && !token.isEmpty()
            && lastUpdate.plusSeconds(25 * 86400).isAfter(Instant.now());
    }

    // ========== DB 读写 ==========

    private String loadFromDb() {
        try {
            SystemConfig config = configMapper.selectById(CONFIG_KEY);
            return config != null ? config.getConfigValue() : null;
        } catch (Exception e) {
            log.warn("从 DB 加载 token 失败: {}", e.getMessage());
            return null;
        }
    }

    private void saveToDb(String value) {
        try {
            SystemConfig config = configMapper.selectById(CONFIG_KEY);
            if (config != null) {
                config.setConfigValue(value);
                configMapper.updateById(config);
            } else {
                config = new SystemConfig();
                config.setConfigKey(CONFIG_KEY);
                config.setConfigValue(value);
                configMapper.insert(config);
            }
        } catch (Exception e) {
            log.error("持久化 token 到 DB 失败: {}", e.getMessage());
        }
    }
}
