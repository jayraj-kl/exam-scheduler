package com.exam.scheduler.controller;

import com.exam.scheduler.model.Room;
import com.exam.scheduler.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Room>> getAvailableRooms() {
        return ResponseEntity.ok(roomService.getAvailableRooms());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(Map.of("deleted", Boolean.TRUE));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRoomStats() {
        return ResponseEntity.ok(roomService.getStatistics());
    }

    @GetMapping("/stats/total-rooms")
    public ResponseEntity<Map<String, Long>> getTotalRooms() {
        return ResponseEntity.ok(Map.of("totalRooms", roomService.countRooms()));
    }

    @GetMapping("/stats/total-capacity")
    public ResponseEntity<Map<String, Integer>> getTotalCapacity() {
        return ResponseEntity.ok(Map.of("totalCapacity", roomService.getTotalCapacity()));
    }

    @GetMapping("/stats/available-rooms")
    public ResponseEntity<Map<String, Integer>> getAvailableRoomsCount() {
        return ResponseEntity.ok(Map.of("availableRooms", roomService.getAvailableRooms().size()));
    }

    /**
     * Body: {@code {"isAvailable": true}}.
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<Room> toggleRoomAvailability(@PathVariable Long id, @RequestBody Map<String, Boolean> availability) {
        Room room = roomService.getRoomById(id);

        Boolean isAvailable = availability.get("isAvailable");
        if (isAvailable == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isAvailable field is required");
        }

        return ResponseEntity.ok(roomService.setAvailability(room, isAvailable));
    }
}
