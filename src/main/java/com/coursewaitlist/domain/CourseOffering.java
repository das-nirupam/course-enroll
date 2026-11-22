package com.coursewaitlist.domain;

import java.util.Objects;

public final class CourseOffering {
    private final String offeringId;
    private final Course course;
    private final int capacity;
    private final int waitlistCapacity;

    public CourseOffering(String offeringId, Course course, int capacity, int waitlistCapacity) {
        if (offeringId == null || offeringId.isBlank()) {
            throw new IllegalArgumentException("offeringId cannot be null or blank");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
        if (waitlistCapacity < 0) {
            throw new IllegalArgumentException("waitlistCapacity cannot be negative");
        }
        this.offeringId = offeringId;
        this.course = Objects.requireNonNull(course, "course cannot be null");
        this.capacity = capacity;
        this.waitlistCapacity = waitlistCapacity;
    }

    public CourseOffering(String offeringId, Course course, int capacity) {
        this(offeringId, course, capacity, Integer.MAX_VALUE);
    }

    public String getOfferingId() {
        return offeringId;
    }

    public Course getCourse() {
        return course;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getWaitlistCapacity() {
        return waitlistCapacity;
    }
}
