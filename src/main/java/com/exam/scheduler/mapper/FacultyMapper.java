package com.exam.scheduler.mapper;

import com.exam.scheduler.model.Faculty;

import java.util.HashMap;
import java.util.Map;

public final class FacultyMapper {

    private FacultyMapper() {
    }

    public static Map<String, Object> toWorkloadSummary(Faculty faculty) {
        Map<String, Object> facultyStats = new HashMap<>();
        facultyStats.put("id", faculty.getId());
        facultyStats.put("name", faculty.getName());
        facultyStats.put("department", faculty.getDepartment());
        facultyStats.put("currentWorkload", faculty.getCurrentWorkload());
        facultyStats.put("workloadCapacity", faculty.getWorkloadCapacity());
        facultyStats.put("usagePercentage",
            faculty.getWorkloadCapacity() > 0 ?
                (double) faculty.getCurrentWorkload() / faculty.getWorkloadCapacity() * 100 : 0);
        facultyStats.put("availabilitySlots", faculty.getAvailabilitySlots());
        facultyStats.put("isExamHead", faculty.isExamHead());
        facultyStats.put("isInvigilator", faculty.isInvigilator());
        return facultyStats;
    }
}
