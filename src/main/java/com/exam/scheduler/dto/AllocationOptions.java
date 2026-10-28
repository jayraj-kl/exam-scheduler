package com.exam.scheduler.dto;

import java.util.Map;

/**
 * Which resources to allocate to an exam slot. Every option defaults to {@code true}.
 */
public record AllocationOptions(boolean assignRoom, boolean assignFaculty, boolean assignInvigilators) {

    public static AllocationOptions from(Map<String, Boolean> options) {
        boolean assignFaculty = options.getOrDefault("assignFaculty", true);
        boolean assignRoom = options.getOrDefault("assignRoom", true);
        boolean assignInvigilators = options.getOrDefault("assignInvigilators", true);
        return new AllocationOptions(assignRoom, assignFaculty, assignInvigilators);
    }
}
