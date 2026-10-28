package com.exam.scheduler.service;

import com.exam.scheduler.exception.ResourceNotFoundException;
import com.exam.scheduler.model.Room;
import com.exam.scheduler.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByIsAvailableTrue();
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        Room room = getRoomById(id);

        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setBuilding(roomDetails.getBuilding());
        room.setFloor(roomDetails.getFloor());
        room.setSeatingCapacity(roomDetails.getSeatingCapacity());
        room.setRoomType(roomDetails.getRoomType());
        room.setIsAvailable(roomDetails.isAvailable());

        return roomRepository.save(room);
    }

    public Room setAvailability(Room room, boolean isAvailable) {
        room.setIsAvailable(isAvailable);
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.delete(getRoomById(id));
    }

    public long countRooms() {
        return roomRepository.count();
    }

    public int getTotalCapacity() {
        return totalCapacity(roomRepository.findAll());
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Room> allRooms = roomRepository.findAll();
        List<Room> availableRooms = roomRepository.findByIsAvailableTrue();

        int totalRooms = allRooms.size();
        int availableRoomCount = availableRooms.size();

        stats.put("totalRooms", totalRooms);
        stats.put("availableRooms", availableRoomCount);
        stats.put("bookedRooms", totalRooms - availableRoomCount);
        stats.put("totalCapacity", totalCapacity(allRooms));

        return stats;
    }

    private static int totalCapacity(List<Room> rooms) {
        return rooms.stream().mapToInt(Room::getSeatingCapacity).sum();
    }
}
