package com.coursewaitlist.domain;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseAndLearnerTest {
    @Test
    void courseCopiesAndProtectsItsTags() {
        Set<String> tags = new HashSet<>(Set.of("science"));
        Course course = new Course("BIO101", "Biology", "Introductory biology", tags);
        tags.add("changed");

        assertEquals(Set.of("science"), course.getTags());
        assertThrows(UnsupportedOperationException.class, () -> course.getTags().add("new"));
    }

    @Test
    void learnerExposesValuesAndHasValueEquality() {
        Learner learner = new Learner("learner-1", true);
        Learner equivalent = new Learner("learner-1", true);

        assertEquals("learner-1", learner.getLearnerId());
        assertTrue(learner.isPrerequisiteSatisfied());
        assertEquals(learner, equivalent);
        assertEquals(learner.hashCode(), equivalent.hashCode());
    }

    @Test
    void domainObjectsRejectBlankIdentifiers() {
        assertThrows(IllegalArgumentException.class,
                () -> new Course(" ", "Biology", "Introductory biology", Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Learner(" ", true));
        assertThrows(IllegalArgumentException.class,
                () -> new CourseOffering(" ", new Course("BIO101", "Biology", "Intro", Set.of()),
                        1, 1));
    }
}
