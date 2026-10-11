package com.restaurant.queue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DatabaseMigrationConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationConfig.class);

    @Bean
    public static BeanPostProcessor dataSourceMigrationPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSource dataSource) {
                    runMigrations(dataSource);
                }
                return bean;
            }
        };
    }

    private static void runMigrations(DataSource dataSource) {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {

            log.info("Running automatic SQLite schema migrations...");

            // 1. Create restaurants table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS restaurants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    slug VARCHAR(64) UNIQUE NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT 1,
                    created_at TIMESTAMP
                )
            """);

            // 2. Ensure default restaurant exists
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM restaurants WHERE id = 1")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("""
                        INSERT INTO restaurants (id, slug, name, active, created_at)
                        VALUES (1, 'default-restaurant', 'Main Branch', 1, CURRENT_TIMESTAMP)
                    """);
                    log.info("Inserted default restaurant (id=1, slug='default-restaurant')");
                }
            }

            // 3. Update restaurant_tables
            if (tableExists(stmt, "restaurant_tables")) {
                addColumnIfMissing(stmt, "restaurant_tables", "restaurant_id", "BIGINT DEFAULT 1");
                stmt.executeUpdate("UPDATE restaurant_tables SET restaurant_id = 1 WHERE restaurant_id IS NULL");
            }

            // 4. Update queue_entries
            if (tableExists(stmt, "queue_entries")) {
                addColumnIfMissing(stmt, "queue_entries", "restaurant_id", "BIGINT DEFAULT 1");
                stmt.executeUpdate("UPDATE queue_entries SET restaurant_id = 1 WHERE restaurant_id IS NULL");
            }

            // 5. Update users
            if (tableExists(stmt, "users")) {
                addColumnIfMissing(stmt, "users", "restaurant_id", "BIGINT");
                addColumnIfMissing(stmt, "users", "active", "BOOLEAN DEFAULT 1");
                stmt.executeUpdate("UPDATE users SET active = 1 WHERE active IS NULL");
                stmt.executeUpdate("UPDATE users SET restaurant_id = 1 WHERE (role = 'STAFF' OR role = 'ROLE_STAFF' OR UPPER(role) LIKE '%STAFF%') AND restaurant_id IS NULL");
            }

            // 6. Create indexes for restaurant-scoped queries
            if (tableExists(stmt, "queue_entries")) {
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_qe_restaurant_status ON queue_entries (restaurant_id, status)");
            }
            if (tableExists(stmt, "restaurant_tables")) {
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_rt_restaurant ON restaurant_tables (restaurant_id)");
            }
            if (tableExists(stmt, "users")) {
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_restaurant ON users (restaurant_id)");
            }

            // 7. Create reservations table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    restaurant_id BIGINT NOT NULL,
                    customer_name VARCHAR(100) NOT NULL,
                    mobile VARCHAR(20) NOT NULL,
                    party_size INTEGER NOT NULL,
                    reservation_time TIMESTAMP NOT NULL,
                    special_requests VARCHAR(500),
                    status VARCHAR(30) NOT NULL,
                    assigned_table_id BIGINT,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                )
            """);
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_res_restaurant_time ON reservations (restaurant_id, reservation_time)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_res_status ON reservations (status)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_res_table ON reservations (assigned_table_id)");

            log.info("SQLite schema migrations completed successfully.");

        } catch (SQLException e) {
            log.error("Failed to run SQLite schema migrations: {}", e.getMessage(), e);
            throw new RuntimeException("Database migration failed", e);
        }
    }

    private static void addColumnIfMissing(Statement stmt, String table, String column, String colDef) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase());
            }
        }
        if (!columns.isEmpty() && !columns.contains(column.toLowerCase())) {
            stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + colDef);
            log.info("Added column {}.{}", table, column);
        }
    }

    private static boolean tableExists(Statement stmt, String table) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + table + "'")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
