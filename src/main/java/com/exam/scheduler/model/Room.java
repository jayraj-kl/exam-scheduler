package com.exam.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String roomNumber;
    private String building;
    private String floor;
    private int seatingCapacity;
    private String roomType;
    private boolean isAvailable;

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }    
    
    @OneToMany(mappedBy = "room")
    @JsonIgnore
    private List<ExamSlot> examSlots;
}