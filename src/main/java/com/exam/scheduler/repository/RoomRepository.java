package com.exam.scheduler.repository;

import com.exam.scheduler.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByIsAvailableTrue();
    
    @Query("SELECT DISTINCT r FROM Room r " +
           "WHERE r.seatingCapacity >= :requiredCapacity " +
           "AND r.isAvailable = true " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM ExamSlot e " +
           "    WHERE e.room = r " +
           "    AND e.examDate = :examDate " +
           "    AND e.isMorningSlot = :isMorningSlot" +
           ")")
    List<Room> findAvailableRoomsWithCapacity(
        @Param("requiredCapacity") int requiredCapacity,
        @Param("examDate") LocalDate examDate,
        @Param("isMorningSlot") boolean isMorningSlot
    );
}