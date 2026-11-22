# AI Assistance and Verification Record

## Summary

AI was used to explore the CourseWaitlist design, draft the initial implementation, and help
refine the domain model, service, CLI, tests, and documentation. The candidate challenged design
assumptions, constrained the work to the assessment scope, and reviewed suggestions against the
requirements. The initial model draft used records; the approved class-based design was applied
in the implementation.

## Design interactions and decisions

| Design question and human review | AI suggestion considered | Decision and implementation evidence |
|---|---|---|
| Is `courseId` alone enough to identify and display a course? Course names are needed for meaningful display and likely reporting. | Keep the model minimal with only an ID. | **Modified:** `Course` includes ID, name, description, and tags. Search and reporting features remain outside this assessment. |
| Should a course be distinct from a scheduled batch such as morning or evening? | Use a course offering concept. | **Accepted within scope:** `CourseOffering` distinguishes an offering from its `Course`; one service instance manages only one offering. No multiple-offering manager was added. |
| Should waitlists have a capacity? Real registrations may limit them, not only enrollment. | Initial design omitted a waitlist limit. | **Modified:** `CourseOffering` validates both limits and the service returns `WAITLIST_FULL`. The CLI defaults the limit to `Integer.MAX_VALUE`; there is no separate configurable policy engine. |
| Should prerequisite eligibility be computed from completed-course dependencies? That would define prerequisite business rules beyond this service slice. | Track prerequisite course IDs and learner completion history. | **Rejected:** `Learner` carries a supplied boolean; the service enforces it and updates the learner's eligibility without calculating dependencies. |
| Should learners be able to search a catalog before enrolling? | A basic catalog lookup could support discovery. | **Deferred/out of scope:** the assessment explicitly excludes a course catalog. No `CourseCatalogService`, search, or ranking was added. |
| Which validation and tests are important? Negative capacities and duplicate, invalid, FIFO, and prerequisite edge cases can corrupt state if missed. | Test the stated acceptance criteria and consider more production architecture. | **Accepted and focused:** validate model identifiers and capacities; test waitlist-full, duplicates, FIFO, eligibility changes, invalid/repeated drops, model behavior, and CLI flow. Persistence and event-driven infrastructure were not added. |
| How should source organization and run instructions be improved? The original package roots were inconsistent, and the README lacked a visible interaction example. | Separate domain, service, and CLI code and provide a sample run. | **Accepted:** packages now share `com.coursewaitlist` roots, and the README includes a CLI transcript verified with scripted input. |
| How can the design be agreed before code changes and checked afterward? | Use OpenSpec proposal, capability spec, design, and task artifacts. | **Accepted:** created the change artifacts before implementation, validated them strictly, completed the tasks, and archived the capability contract under `openspec/specs/course-waitlist/`. |
| Should storage be behind a DAO/repository interface so the service owns business rules and storage can be replaced later? | A repository adapter can separate persistence operations from enrollment decisions. | **Accepted with a boundary:** added `CourseRegistrationRepository` and an in-memory implementation; capacity, eligibility, duplicate, and FIFO policy remain in the service. A future database still needs transaction/locking work. |
| Should prerequisite eligibility be asked when enrollment is requested? | Keep prerequisite state as an eligibility input. | **Accepted:** the CLI now asks `y/n` during each enrollment request and only then submits to the service; a `no` answer produces prerequisite failure with no enrollment/waitlist entry. |
| Should the deliverable be directly executable as a JAR? | Use the CLI main class as the JAR entry point. | **Accepted:** Gradle creates `course-waitlist.jar`; the documented `java -jar` interaction was tested with scripted input. |

## Implementation disposition

- **Accepted:** FIFO promotion, synchronized service state transitions with a focused concurrent
  capacity test, a course/offering distinction for the single managed offering, a class-based
  domain model, an in-memory repository seam, an enrollment-time prerequisite prompt, and an
  executable JAR.
- **Modified:** Explicit waitlist capacity with a CLI default of `Integer.MAX_VALUE`; a
  `CourseOffering` model without multi-offering management; a supplied prerequisite boolean
  instead of a prerequisite dependency engine; and a repository seam without adding a database.
- **Rejected or deferred:** Course catalog/search, multiple-offering management, persistence,
  event-driven processing, and automated prerequisite calculation. These are outside the bounded
  assignment scope.

## Verification

- `.\gradlew.bat test` passed after implementation: **24 tests, 0 failures, 0 errors**. The suite
  covers capacity validation,
  available/full enrollment, waitlist-full behavior, duplicate enrollment and waitlisting,
  prerequisite failure and eligibility changes, null/blank enrollment requests, FIFO promotion,
  waitlist removal, invalid/repeated drops,
  concurrent capacity enforcement, model validation, and CLI interaction.
- `java -jar build/libs/course-waitlist.jar 1 2` was exercised with scripted input to enter
  learner A, answer `y` to the prerequisite prompt, display status, and exit. The output included
  `Enrollment result: ENROLLED`, `Enrolled (1): [A]`, and `Goodbye.`; the same interaction is in
  the README.
- During implementation, a test exposed null unboxing for an invalid waitlist-capacity argument.
  The CLI validation was changed to explicit branches, and the tests were rerun successfully.
