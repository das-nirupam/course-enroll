package com.coursewaitlist.repository;

import java.util.List;

import com.coursewaitlist.domain.Learner;

public interface CourseRegistrationRepository {
    Learner findLearner(String learnerId);

    void saveLearner(Learner learner);

    boolean isEnrolled(String learnerId);

    boolean isWaitlisted(String learnerId);

    boolean addEnrolled(String learnerId);

    boolean removeEnrolled(String learnerId);

    boolean addWaitlisted(String learnerId);

    boolean removeWaitlisted(String learnerId);

    String pollWaitlisted();

    int enrolledCount();

    int waitlistCount();

    List<String> enrolledLearners();

    List<String> waitlistedLearners();
}
