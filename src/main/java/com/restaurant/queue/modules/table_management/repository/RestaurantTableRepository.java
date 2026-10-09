package com.restaurant.queue.modules.table_management.repository;

import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByStatus(TableStatus status);

    List<RestaurantTable> findByCapacityGreaterThanEqualAndStatus(Integer minCapacity, TableStatus status);

    long countByStatus(TableStatus status);
}
