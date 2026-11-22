# Design Specification

## Scope and service boundary

The implementation covers one course offering. `Course` holds descriptive course information;
`CourseOffering` holds one offering ID, its course, and enrollment and waitlist capacities. One
`CourseRegistrationService` instance owns the business workflow for that offering and delegates
state access to a `CourseRegistrationRepository`. The CLI is a thin adapter over the service.
There is no course catalog, account system, external integration, multi-offering manager, or
database persistence layer.

## Package structure

- `com.coursewaitlist.domain`: immutable `Course`, `Learner`, and `CourseOffering` models.
- `com.coursewaitlist.service`: registration behavior and typed enrollment/drop results.
- `com.coursewaitlist.repository`: repository interface and in-memory adapter.
- `com.coursewaitlist.cli`: command-line adapter.

## API and results

- `updateLearner(learner)` supplies a learner's prerequisite eligibility. Null learners and null or
  blank learner IDs are rejected with `IllegalArgumentException`.
- Enrollment requests in the CLI ask for prerequisite satisfaction before calling the service.
- `enroll(learnerId)` returns `INVALID_REQUEST`, `PREREQUISITE_FAILED`, `DUPLICATE`, `ENROLLED`,
  `WAITLISTED`, or `WAITLIST_FULL`.
- `drop(learnerId)` returns `INVALID_REQUEST`, `DROPPED`, `REMOVED_WAITLIST`, or `NOT_FOUND`.
- `enrolledLearners()` and `waitlistedLearners()` return immutable snapshots in enrollment and
  queue order.

## Assumptions and invariants

- A prerequisite is a single boolean eligibility condition supplied by the caller on `Learner`.
  The service enforces it but does not calculate prerequisite dependencies.
- If eligibility is revoked while a learner is enrolled, that learner is removed immediately and
  the newly open seat is offered using the same FIFO eligibility rule.
- Learner IDs identify learners within this offering, are case-sensitive, and cannot be blank.
- Enrollment and waitlist capacities are non-negative. Enrolled learners are unique, cannot also
  be waitlisted, and neither collection exceeds its offering limit.
- A full course places an eligible learner at the tail of the FIFO queue if waitlist capacity
  remains. The CLI defaults the waitlist capacity to `Integer.MAX_VALUE`; when full, enrollment
  returns `WAITLIST_FULL`.
- A repeated enrollment by an enrolled or waitlisted learner returns `DUPLICATE` without changing
  state.
- On a drop, the service polls from the head until it finds the earliest eligible learner.
  Learners whose prerequisite has become incomplete are skipped and removed. Dropping a waitlisted
  learner removes only that entry; unknown or repeated drops return `NOT_FOUND`.

## Concurrency and persistence

The repository interface isolates learner/enrollment/waitlist storage, and its in-memory adapter
loses state when the process exits. A repository instance is owned by one service instance.
Service operations that mutate or read registration state are synchronized on that service,
making enrollment, drop, prerequisite updates, and promotion atomic within this process. This
does not provide cross-process coordination or database transactions. A future database adapter
must add appropriate transaction and locking semantics; the interface alone does not provide them.
A focused concurrent-enrollment test verifies that the configured enrollment and waitlist limits
are preserved.

## Tradeoffs

`CourseOffering` represents the single offering bound to a service instance, preserving the
distinction between a course and its scheduled offering without implementing a multi-offering
manager. The model's descriptive fields support identification/display, but discovery and search
are outside this assignment. The CLI is a local demonstration interface, not a production
registration system. The executable JAR is a thin artifact with no bundled runtime dependencies.
