package com.exam.scheduler.repository;

import com.exam.scheduler.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByIsAvailableTrue();
    
    @Query("SELECT r FROM Room r WHERE r.isAvailable = true AND r.seatingCapacity >= :requiredCapacity ORDER BY r.seatingCapacity ASC")
    List<Room> findAvailableRoomsWithCapacity(int requiredCapacity);
}