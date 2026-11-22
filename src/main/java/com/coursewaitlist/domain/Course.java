package com.coursewaitlist.domain;

import java.util.Objects;
import java.util.Set;

public final class Course {
    private final String courseId;
    private final String courseName;
    private final String description;
    private final Set<String> tags;

    public Course(String courseId, String courseName, String description, Set<String> tags) {
        this.courseId = requireText(courseId, "courseId");
        this.courseName = requireText(courseName, "courseName");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.tags = Set.copyOf(Objects.requireNonNull(tags, "tags cannot be null"));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Course)) {
            return false;
        }
        Course course = (Course) other;
        return courseId.equals(course.courseId)
                && courseName.equals(course.courseName)
                && description.equals(course.description)
                && tags.equals(course.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, courseName, description, tags);
    }

    @Override
    public String toString() {
        return "Course{courseId='" + courseId + "', courseName='" + courseName
                + "', description='" + description + "', tags=" + tags + '}';
    }
}