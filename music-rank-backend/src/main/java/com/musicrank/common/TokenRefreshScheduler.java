package com.musicrank.common;

import com.musicrank.service.KugouSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Token 自动刷新定时任务
 * 每 7 天调用一次 /login/token 刷新酷狗登录状态
 */
@Component
public class TokenRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshScheduler.class);

    @Autowired
    private KugouSyncService kugouSyncService;

    @Autowired
    private TokenManager tokenManager;

    /**
     * 每 7 天执行一次 token 刷新
     * cron: 每周一凌晨 3:00
     */
    @Scheduled(cron = "0 0 3 * * 1")
    public void refreshTokenWeekly() {
        log.info("=== 定时刷新 token ===");
        try {
            String newToken = kugouSyncService.refreshToken();
            if (newToken != null) {
                log.info("Token 自动刷新成功");
            } else {
                log.warn("Token 自动刷新失败，可能需要重新登录");
            }
        } catch (Exception e) {
            log.error("Token 自动刷新异常: {}", e.getMessage());
        }
    }

    /**
     * 应用启动后 30 秒执行一次 token 状态检查
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    public void checkTokenOnStartup() {
        if (tokenManager.isValid()) {
            log.info("Token 状态正常");
        } else {
            log.warn("Token 可能已过期，请重新登录");
        }
    }
}
