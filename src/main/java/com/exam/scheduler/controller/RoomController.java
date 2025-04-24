package com.exam.scheduler.controller;

import com.exam.scheduler.model.Room;
import com.exam.scheduler.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with id: " + id));
        return ResponseEntity.ok(room);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Room>> getAvailableRooms() {
        return ResponseEntity.ok(roomRepository.findByIsAvailableTrue());
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomRepository.save(room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with id: " + id));
        
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setBuilding(roomDetails.getBuilding());
        room.setFloor(roomDetails.getFloor());
        room.setSeatingCapacity(roomDetails.getSeatingCapacity());
        room.setRoomType(roomDetails.getRoomType());
        room.setIsAvailable(roomDetails.isAvailable());
        
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteRoom(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with id: " + id));
        
        roomRepository.delete(room);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRoomStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Room> allRooms = roomRepository.findAll();
        List<Room> availableRooms = roomRepository.findByIsAvailableTrue();
        
        int totalRooms = allRooms.size();
        int availableRoomCount = availableRooms.size();
        int bookedRoomCount = totalRooms - availableRoomCount;
        int totalCapacity = allRooms.stream().mapToInt(Room::getSeatingCapacity).sum();
        
        stats.put("totalRooms", totalRooms);
        stats.put("availableRooms", availableRoomCount);
        stats.put("bookedRooms", bookedRoomCount);
        stats.put("totalCapacity", totalCapacity);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/total-rooms")
    public ResponseEntity<Map<String, Object>> getTotalRooms() {
        Map<String, Object> response = new HashMap<>();
        long totalRooms = roomRepository.count();
        response.put("totalRooms", totalRooms);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/total-capacity")
    public ResponseEntity<Map<String, Object>> getTotalCapacity() {
        Map<String, Object> response = new HashMap<>();
        List<Room> allRooms = roomRepository.findAll();
        int totalCapacity = allRooms.stream().mapToInt(Room::getSeatingCapacity).sum();
        response.put("totalCapacity", totalCapacity);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/available-rooms")
    public ResponseEntity<Map<String, Object>> getAvailableRoomsCount() {
        Map<String, Object> response = new HashMap<>();
        List<Room> availableRooms = roomRepository.findByIsAvailableTrue();
        response.put("availableRooms", availableRooms.size());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<Room> toggleRoomAvailability(@PathVariable Long id, @RequestBody Map<String, Boolean> availability) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with id: " + id));
        
        Boolean isAvailable = availability.get("isAvailable");
        if (isAvailable == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isAvailable field is required");
        }
        
        room.setIsAvailable(isAvailable);
        return ResponseEntity.ok(roomRepository.save(room));
    }
}