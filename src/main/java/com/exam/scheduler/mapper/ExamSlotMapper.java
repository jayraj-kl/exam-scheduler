package com.exam.scheduler.mapper;

import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Faculty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExamSlotMapper {

    private ExamSlotMapper() {
    }

    /**
     * Flattens a slot into a summary of when/where it runs and which staff are assigned to it.
     */
    public static Map<String, Object> toStaffAssignmentSummary(ExamSlot slot) {
        Map<String, Object> slotInfo = new HashMap<>();
        slotInfo.put("id", slot.getId());
        slotInfo.put("examDate", slot.getExamDate());
        slotInfo.put("startTime", slot.getStartTime());
        slotInfo.put("endTime", slot.getEndTime());
        slotInfo.put("subject", slot.getSubject().getName());
        slotInfo.put("room", slot.getRoom() != null ? slot.getRoom().getRoomNumber() : "Not assigned");
        slotInfo.put("faculty", slot.getFaculty() != null ? facultyWithWorkload(slot.getFaculty()) : null);
        slotInfo.put("examHead", slot.getExamHead() != null ? staffMember(slot.getExamHead()) : null);

        List<Map<String, Object>> invigilatorsInfo = new ArrayList<>();
        if (slot.getInvigilators() != null) {
            for (Faculty invigilator : slot.getInvigilators()) {
                invigilatorsInfo.add(staffMember(invigilator));
            }
        }
        slotInfo.put("invigilators", invigilatorsInfo);

        return slotInfo;
    }

    private static Map<String, Object> facultyWithWorkload(Faculty faculty) {
        Map<String, Object> facultyInfo = new HashMap<>();
        facultyInfo.put("id", faculty.getId());
        facultyInfo.put("name", faculty.getName());
        facultyInfo.put("department", faculty.getDepartment());
        facultyInfo.put("workload", faculty.getCurrentWorkload());
        return facultyInfo;
    }

    private static Map<String, Object> staffMember(Faculty faculty) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", faculty.getId());
        info.put("name", faculty.getName());
        info.put("department", faculty.getDepartment());
        return info;
    }
}
