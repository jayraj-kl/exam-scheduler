package com.exam.scheduler.service.scheduling;

import java.time.LocalTime;

/**
 * Fixed rules the schedule generator and resource allocator work with.
 */
public final class SchedulingRules {

    /** Upper bound of students seated in a single exam slot. */
    public static final int MAX_STUDENTS_PER_SLOT = 35;

    public static final LocalTime MORNING_SLOT_START = LocalTime.of(9, 0);
    public static final LocalTime AFTERNOON_SLOT_START = LocalTime.of(14, 0);
    public static final int EXAM_DURATION_HOURS = 3;
    /** How long before the start time staff and students must report. */
    public static final int REPORTING_TIME_MINUTES = 30;

    /** One invigilator per this many students (at least one per slot). */
    public static final int STUDENTS_PER_INVIGILATOR = 20;

    private SchedulingRules() {
    }
}
