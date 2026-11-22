package com.coursewaitlist.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.coursewaitlist.domain.Course;
import com.coursewaitlist.domain.CourseOffering;
import com.coursewaitlist.domain.Learner;
import com.coursewaitlist.repository.InMemoryCourseRegistrationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseRegistrationServiceTest {
    @Test
    void enrollsWithAvailableCapacityAndRejectsDuplicate() {
        CourseRegistrationService service = service(2, 2);
        service.updateLearner(eligible("A"));

        assertEquals(EnrollmentResult.ENROLLED, service.enroll("A"));
        assertEquals(EnrollmentResult.DUPLICATE, service.enroll("A"));
        assertEquals(List.of("A"), service.enrolledLearners());
    }

    @Test
    void rejectsMissingPrerequisite() {
        CourseRegistrationService service = service(1, 1);

        assertEquals(EnrollmentResult.PREREQUISITE_FAILED, service.enroll("A"));
        assertEquals(0, service.enrolledCount());
        assertEquals(0, service.waitlistCount());
    }

    @Test
    void rejectsNullBlankAndUnknownPrerequisiteRequestsConsistently() {
        CourseRegistrationService service = service(1, 1);

        assertEquals(EnrollmentResult.INVALID_REQUEST, service.enroll(null));
        assertEquals(EnrollmentResult.INVALID_REQUEST, service.enroll(" "));
        assertEquals(EnrollmentResult.PREREQUISITE_FAILED, service.enroll("unknown"));
        assertEquals(0, service.enrolledCount());
        assertEquals(0, service.waitlistCount());
    }

    @Test
    void promotesWaitlistInFifoOrder() {
        CourseRegistrationService service = service(1, 2);
        service.updateLearner(eligible("A"));
        service.updateLearner(eligible("B"));
        service.updateLearner(eligible("C"));

        assertEquals(EnrollmentResult.ENROLLED, service.enroll("A"));
        assertEquals(EnrollmentResult.WAITLISTED, service.enroll("B"));
        assertEquals(EnrollmentResult.WAITLISTED, service.enroll("C"));
        assertEquals(DropResult.DROPPED, service.drop("A"));
        assertEquals(List.of("B"), service.enrolledLearners());
        assertEquals(List.of("C"), service.waitlistedLearners());
    }

    @Test
    void rejectsEnrollmentWhenWaitlistIsFull() {
        CourseRegistrationService service = service(1, 1);
        service.updateLearner(eligible("A"));
        service.updateLearner(eligible("B"));
        service.updateLearner(eligible("C"));

        assertEquals(EnrollmentResult.ENROLLED, service.enroll("A"));
        assertEquals(EnrollmentResult.WAITLISTED, service.enroll("B"));
        assertEquals(EnrollmentResult.WAITLIST_FULL, service.enroll("C"));
        assertEquals(List.of("B"), service.waitlistedLearners());
    }

    @Test
    void duplicateWaitlistRequestDoesNotConsumeCapacity() {
        CourseRegistrationService service = service(1, 1);
        service.updateLearner(eligible("A"));
        service.updateLearner(eligible("B"));

        service.enroll("A");
        assertEquals(EnrollmentResult.WAITLISTED, service.enroll("B"));
        assertEquals(EnrollmentResult.DUPLICATE, service.enroll("B"));
        assertEquals(1, service.waitlistCount());
    }

    @Test
    void skipsIneligibleWaiterWhenPromoting() {
        CourseRegistrationService service = service(1, 2);
        service.updateLearner(eligible("A"));
        service.updateLearner(eligible("B"));
        service.updateLearner(eligible("C"));
        service.enroll("A");
        service.enroll("B");
        service.enroll("C");

        service.updateLearner(new Learner("B", false));
        assertEquals(DropResult.DROPPED, service.drop("A"));

        assertEquals(List.of("C"), service.enrolledLearners());
        assertEquals(0, service.waitlistCount());
    }

    @Test
    void revokingEnrolledLearnerPromotesNextEligibleWaiter() {
        CourseRegistrationService service = service(1, 1);
        service.updateLearner(eligible("A"));
        service.updateLearner(eligible("B"));
        service.enroll("A");
        service.enroll("B");

        service.updateLearner(new Learner("A", false));

        assertEquals(List.of("B"), service.enrolledLearners());
        assertEquals(0, service.waitlistCount());
    }

    @Test
    void invalidAndRepeatedDropsDoNotChangeState() {
        CourseRegistrationService service = service(1, 1);
        service.updateLearner(eligible("A"));

        assertEquals(DropResult.INVALID_REQUEST, service.drop(null));
        assertEquals(DropResult.INVALID_REQUEST, service.drop(" "));
        assertEquals(DropResult.NOT_FOUND, service.drop("missing"));
        assertEquals(EnrollmentResult.ENROLLED, service.enroll("A"));
        assertEquals(DropResult.DROPPED, service.drop("A"));
        assertEquals(DropResult.NOT_FOUND, service.drop("A"));
        assertEquals(0, service.enrolledCount());
        assertEquals(0, service.waitlistCount());
    }

    @Test
    void droppingWaitlistedLearnerRemovesOnlyThatEntry() {
        CourseRegistrationService service = service(1, 2);
        service.updateLearner(eligible("A"));
        service.updateLearner(eligible("B"));
        service.updateLearner(eligible("C"));
        service.enroll("A");
        service.enroll("B");
        service.enroll("C");

        assertEquals(DropResult.REMOVED_WAITLIST, service.drop("B"));
        assertEquals(List.of("C"), service.waitlistedLearners());
    }

    @Test
    void zeroCapacityCanStillWaitlist() {
        CourseRegistrationService service = service(0, 1);
        service.updateLearner(eligible("A"));

        assertEquals(EnrollmentResult.WAITLISTED, service.enroll("A"));
        assertEquals(0, service.enrolledCount());
        assertEquals(1, service.waitlistCount());
    }

    @Test
    void rejectsNegativeOfferingCapacities() {
        Course course = course();

        assertThrows(IllegalArgumentException.class,
                () -> new CourseOffering("offering-1", course, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new CourseOffering("offering-1", course, 1, -1));
    }

    @Test
    void registrationSnapshotsAreImmutable() {
        CourseRegistrationService service = service(1, 1);
        service.updateLearner(eligible("A"));
        service.updateLearner(eligible("B"));
        service.enroll("A");
        service.enroll("B");

        assertThrows(UnsupportedOperationException.class,
                () -> service.enrolledLearners().add("C"));
        assertThrows(UnsupportedOperationException.class,
                () -> service.waitlistedLearners().add("C"));
    }

    @Test
    void threeArgumentOfferingUsesMaximumWaitlistCapacity() {
        CourseOffering offering = new CourseOffering("offering-1", course(), 1);

        assertEquals(Integer.MAX_VALUE, offering.getWaitlistCapacity());
    }

    @Test
    void concurrentEnrollmentNeverExceedsEitherCapacity() throws Exception {
        int learnerCount = 24;
        int enrollmentCapacity = 3;
        int waitlistCapacity = 10;
        CourseRegistrationService service = service(enrollmentCapacity, waitlistCapacity);
        ExecutorService executor = Executors.newFixedThreadPool(learnerCount);
        CountDownLatch ready = new CountDownLatch(learnerCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<EnrollmentResult>> results = new ArrayList<>();

        try {
            for (int i = 0; i < learnerCount; i++) {
                String learnerId = "learner-" + i;
                service.updateLearner(eligible(learnerId));
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return service.enroll(learnerId);
                }));
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            int enrolled = 0;
            int waitlisted = 0;
            int rejected = 0;
            for (Future<EnrollmentResult> result : results) {
                switch (result.get(5, TimeUnit.SECONDS)) {
                    case ENROLLED:
                        enrolled++;
                        break;
                    case WAITLISTED:
                        waitlisted++;
                        break;
                    case WAITLIST_FULL:
                        rejected++;
                        break;
                    default:
                        throw new AssertionError("Unexpected enrollment result");
                }
            }

            assertEquals(enrollmentCapacity, enrolled);
            assertEquals(waitlistCapacity, waitlisted);
            assertEquals(learnerCount - enrollmentCapacity - waitlistCapacity, rejected);
            assertEquals(enrollmentCapacity, service.enrolledCount());
            assertEquals(waitlistCapacity, service.waitlistCount());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private static CourseRegistrationService service(int capacity, int waitlistCapacity) {
        return new CourseRegistrationService(
                new CourseOffering("offering-1", course(), capacity, waitlistCapacity),
                new InMemoryCourseRegistrationRepository());
    }

    private static Course course() {
        return new Course("COURSE-1", "Course", "Test course", Set.of());
    }

    private static Learner eligible(String learnerId) {
        return new Learner(learnerId, true);
    }
}
