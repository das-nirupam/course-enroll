package com.coursewaitlist.domain;

import java.util.Objects;

public final class Learner {
    private final String learnerId;
    private final boolean prerequisiteSatisfied;

    public Learner(String learnerId, boolean prerequisiteSatisfied) {
        if (learnerId == null || learnerId.isBlank()) {
            throw new IllegalArgumentException("learnerId cannot be null or blank");
        }
        this.learnerId = learnerId;
        this.prerequisiteSatisfied = prerequisiteSatisfied;
    }

    public String getLearnerId() {
        return learnerId;
    }

    public boolean isPrerequisiteSatisfied() {
        return prerequisiteSatisfied;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Learner)) {
            return false;
        }
        Learner learner = (Learner) other;
        return prerequisiteSatisfied == learner.prerequisiteSatisfied
                && learnerId.equals(learner.learnerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(learnerId, prerequisiteSatisfied);
    }

    @Override
    public String toString() {
        return "Learner{learnerId='" + learnerId + "', prerequisiteSatisfied="
                + prerequisiteSatisfied + '}';
    }
}