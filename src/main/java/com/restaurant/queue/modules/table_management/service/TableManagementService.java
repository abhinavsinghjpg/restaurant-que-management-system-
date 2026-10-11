package com.restaurant.queue.modules.table_management.service;

import com.restaurant.queue.common.constants.TableStatus;
import com.restaurant.queue.common.exception.TableNotAvailableException;
import com.restaurant.queue.modules.table_management.model.RestaurantTable;
import com.restaurant.queue.modules.table_management.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for restaurant table management:
 * - View all tables
 * - Get available tables for a party size
 * - Assign a table (AVAILABLE → OCCUPIED)
 * - Release a table (OCCUPIED → AVAILABLE)
 */
@Service
public class TableManagementService {

    private final RestaurantTableRepository tableRepository;

    public TableManagementService(RestaurantTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    /** Returns all tables for a specific restaurant. */
    public List<RestaurantTable> getAllTables(Long restaurantId) {
        if (restaurantId == null) {
            return getAllTables();
        }
        return tableRepository.findByRestaurantId(restaurantId);
    }

    /** Returns all tables. */
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    /** Returns available tables with sufficient capacity for the party in a specific restaurant. */
    public List<RestaurantTable> getAvailableTablesForParty(Long restaurantId, int partySize) {
        if (restaurantId == null) {
            return getAvailableTablesForParty(partySize);
        }
        return tableRepository.findByRestaurantIdAndCapacityGreaterThanEqualAndStatus(restaurantId, partySize, TableStatus.AVAILABLE);
    }

    /** Returns available tables with sufficient capacity for the party. */
    public List<RestaurantTable> getAvailableTablesForParty(int partySize) {
        return tableRepository.findByCapacityGreaterThanEqualAndStatus(partySize, TableStatus.AVAILABLE);
    }

    /** Assigns a table (marks it OCCUPIED) with restaurant isolation. */
    @Transactional
    public void assignTable(Long restaurantId, Long tableId) {
        RestaurantTable table = (restaurantId != null)
                ? tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                        .orElseThrow(() -> new TableNotAvailableException("Table not found with ID: " + tableId))
                : tableRepository.findById(tableId)
                        .orElseThrow(() -> new TableNotAvailableException("Table not found with ID: " + tableId));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new TableNotAvailableException("Table " + table.getTableNumber() + " is already occupied.");
        }

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);
    }

    @Transactional
    public void assignTable(Long tableId) {
        assignTable(null, tableId);
    }

    /** Releases a table (marks it AVAILABLE) with restaurant isolation. */
    @Transactional
    public void releaseTable(Long restaurantId, Long tableId) {
        RestaurantTable table = (restaurantId != null)
                ? tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                        .orElseThrow(() -> new TableNotAvailableException("Table not found with ID: " + tableId))
                : tableRepository.findById(tableId)
                        .orElseThrow(() -> new TableNotAvailableException("Table not found with ID: " + tableId));

        if (table.getStatus() != TableStatus.OCCUPIED) {
            throw new TableNotAvailableException("Table " + table.getTableNumber() + " is not currently occupied.");
        }

        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);
    }

    @Transactional
    public void releaseTable(Long tableId) {
        releaseTable(null, tableId);
    }
}
