package com.restaurant.queue.config;

import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.modules.authentication.model.User;
import com.restaurant.queue.modules.authentication.repository.UserRepository;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the database with default users and restaurant tables on first run.
 * Checks for existing data to avoid duplicate inserts on restart.
 */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository,
                                          RestaurantTableRepository tableRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {

            // ========== Seed Users ==========
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                admin.setCreatedAt(LocalDateTime.now());

                User staff = new User();
                staff.setUsername("staff");
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setRole("STAFF");
                staff.setCreatedAt(LocalDateTime.now());

                userRepository.saveAll(List.of(admin, staff));
                System.out.println("✅ Seeded default users: admin / staff");
            }

            // ========== Seed Tables ==========
            if (tableRepository.count() == 0) {
                List<RestaurantTable> tables = List.of(
                    createTable("T1", 2, TableStatus.AVAILABLE),
                    createTable("T2", 2, TableStatus.AVAILABLE),
                    createTable("T3", 4, TableStatus.AVAILABLE),
                    createTable("T4", 4, TableStatus.AVAILABLE),
                    createTable("T5", 4, TableStatus.AVAILABLE),
                    createTable("T6", 6, TableStatus.AVAILABLE),
                    createTable("T7", 6, TableStatus.AVAILABLE),
                    createTable("T8", 8, TableStatus.AVAILABLE)
                );
                tableRepository.saveAll(tables);
                System.out.println("✅ Seeded 8 default restaurant tables");
            }
        };
    }

    private RestaurantTable createTable(String number, int capacity, TableStatus status) {
        RestaurantTable t = new RestaurantTable();
        t.setTableNumber(number);
        t.setCapacity(capacity);
        t.setStatus(status);
        return t;
    }
}
