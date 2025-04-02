package com.exam.scheduler.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String roomNumber;
    private int seatingCapacity;
    private boolean isAvailable;

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }    
    
    @OneToOne(mappedBy = "room")
    private ExamSlot currentBooking;
}