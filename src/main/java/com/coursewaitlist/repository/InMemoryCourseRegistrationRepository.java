package com.coursewaitlist.repository;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.coursewaitlist.domain.Learner;

public final class InMemoryCourseRegistrationRepository implements CourseRegistrationRepository {
    private final Map<String, Learner> learners = new HashMap<>();
    private final Set<String> enrolled = new LinkedHashSet<>();
    private final Queue<String> waitlist = new ArrayDeque<>();

    @Override
    public Learner findLearner(String learnerId) {
        return learners.get(learnerId);
    }

    @Override
    public void saveLearner(Learner learner) {
        learners.put(learner.getLearnerId(), learner);
    }

    @Override
    public boolean isEnrolled(String learnerId) {
        return enrolled.contains(learnerId);
    }

    @Override
    public boolean isWaitlisted(String learnerId) {
        return waitlist.contains(learnerId);
    }

    @Override
    public boolean addEnrolled(String learnerId) {
        return !waitlist.contains(learnerId) && enrolled.add(learnerId);
    }

    @Override
    public boolean removeEnrolled(String learnerId) {
        return enrolled.remove(learnerId);
    }

    @Override
    public boolean addWaitlisted(String learnerId) {
        if (enrolled.contains(learnerId) || waitlist.contains(learnerId)) {
            return false;
        }
        return waitlist.add(learnerId);
    }

    @Override
    public boolean removeWaitlisted(String learnerId) {
        return waitlist.remove(learnerId);
    }

    @Override
    public String pollWaitlisted() {
        return waitlist.poll();
    }

    @Override
    public int enrolledCount() {
        return enrolled.size();
    }

    @Override
    public int waitlistCount() {
        return waitlist.size();
    }

    @Override
    public List<String> enrolledLearners() {
        return List.copyOf(enrolled);
    }

    @Override
    public List<String> waitlistedLearners() {
        return List.copyOf(waitlist);
    }
}
