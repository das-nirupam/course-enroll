package com.coursewaitlist.service;

import java.util.List;

import com.coursewaitlist.domain.CourseOffering;
import com.coursewaitlist.domain.Learner;
import com.coursewaitlist.repository.CourseRegistrationRepository;

public final class CourseRegistrationService {
    private final CourseOffering offering;
    private final CourseRegistrationRepository repository;

    public CourseRegistrationService(
            CourseOffering offering, CourseRegistrationRepository repository) {
        if (offering == null) {
            throw new IllegalArgumentException("offering cannot be null");
        }
        if (repository == null) {
            throw new IllegalArgumentException("repository cannot be null");
        }
        this.offering = offering;
        this.repository = repository;
    }

    public synchronized void updateLearner(Learner learner) {
        if (learner == null || !isValidLearner(learner.getLearnerId())) {
            throw new IllegalArgumentException("Learner and learner ID must be valid");
        }
        String learnerId = learner.getLearnerId();
        repository.saveLearner(learner);
        if (!learner.isPrerequisiteSatisfied() && repository.removeEnrolled(learnerId)) {
            promote();
        }
    }

    public synchronized EnrollmentResult enroll(String learnerId) {
        if (!isValidLearner(learnerId)) {
            return EnrollmentResult.INVALID_REQUEST;
        }
        if (repository.isEnrolled(learnerId) || repository.isWaitlisted(learnerId)) {
            return EnrollmentResult.DUPLICATE;
        }
        Learner learner = repository.findLearner(learnerId);
        if (learner == null || !learner.isPrerequisiteSatisfied()) {
            return EnrollmentResult.PREREQUISITE_FAILED;
        }
        if (repository.enrolledCount() < offering.getCapacity()) {
            return repository.addEnrolled(learnerId)
                    ? EnrollmentResult.ENROLLED : EnrollmentResult.DUPLICATE;
        }
        if (repository.waitlistCount() >= offering.getWaitlistCapacity()) {
            return EnrollmentResult.WAITLIST_FULL;
        }
        return repository.addWaitlisted(learnerId)
                ? EnrollmentResult.WAITLISTED : EnrollmentResult.DUPLICATE;
    }

    public synchronized DropResult drop(String learnerId) {
        if (!isValidLearner(learnerId)) {
            return DropResult.INVALID_REQUEST;
        }
        if (repository.removeEnrolled(learnerId)) {
            promote();
            return DropResult.DROPPED;
        }
        if (repository.removeWaitlisted(learnerId)) {
            return DropResult.REMOVED_WAITLIST;
        }
        return DropResult.NOT_FOUND;
    }

    private void promote() {
        while (repository.enrolledCount() < offering.getCapacity()
                && repository.waitlistCount() > 0) {
            String learnerId = repository.pollWaitlisted();
            Learner learner = repository.findLearner(learnerId);
            if (learner != null && learner.isPrerequisiteSatisfied()) {
                if (repository.addEnrolled(learnerId)) {
                    return;
                }
            }
        }
    }

    public CourseOffering offering() {
        return offering;
    }

    public synchronized int enrolledCount() {
        return repository.enrolledCount();
    }

    public synchronized int waitlistCount() {
        return repository.waitlistCount();
    }

    public synchronized List<String> enrolledLearners() {
        return List.copyOf(repository.enrolledLearners());
    }

    public synchronized List<String> waitlistedLearners() {
        return List.copyOf(repository.waitlistedLearners());
    }

    private static boolean isValidLearner(String learnerId) {
        return learnerId != null && !learnerId.isBlank();
    }
}
