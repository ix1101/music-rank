package com.musicrank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * SQLite 生产环境优化
 * - 开启 WAL 模式：允许读写并发，避免读写锁阻塞
 * - 创建必要索引：加速 singer 筛选查询
 *
 * 仅在 prod 配置下生效（开发环境使用 MySQL，不需要这些优化）
 */
@Component
@Profile("prod")
public class SqliteOptimizer {

    private static final Logger log = LoggerFactory.getLogger(SqliteOptimizer.class);

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void optimize() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // WAL 模式：写操作不阻塞读，读操作不阻塞写
            // SQLite 默认的 DELETE 模式会导致写时锁整个库
            stmt.execute("PRAGMA journal_mode=WAL");
            log.info("SQLite WAL 模式已开启");

            // singer 列索引：加速按歌手筛选和 singer 下拉列表查询
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_music_rank_artist ON music_rank(artist)");
            log.info("SQLite 索引 idx_music_rank_artist 已就绪");

            // 增大缓存到 64MB（默认 2MB），减少磁盘 I/O
            stmt.execute("PRAGMA cache_size = -65536");
            log.info("SQLite cache_size 已设为 64MB");

        } catch (Exception e) {
            log.warn("SQLite 优化执行失败（不影响启动）: {}", e.getMessage());
        }
    }
}
