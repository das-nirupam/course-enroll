# Limitations and Follow-up Risks

- All offering, enrollment, waitlist, and prerequisite state is in memory and is lost on exit.
- The repository interface provides a storage seam, not a ready-made database adapter. A future
  database implementation still needs transaction and locking behavior for multi-step promotion.
- Synchronization protects calls made through one service instance in one process only; it does
  not coordinate multiple processes or persist transactions.
- Prerequisite eligibility is a manually supplied boolean, not calculated from course history or
  checked against an authoritative student record.
- The waitlist default is `Integer.MAX_VALUE`, a practical large bound rather than an actually
  unbounded queue.
- Ineligible waiters are skipped and removed from the waitlist queue at promotion time. They are
  not retained on the waitlist or in enrollment; their eligibility record may remain in the
  in-memory repository while the process is running. The CLI has no separate notice mechanism for
  that learner beyond displaying current queue status.
- The CLI is a local demonstration interface; it does not provide authentication, durable storage,
  catalog/search, or management of multiple offerings.
- The executable JAR launches the CLI but does not package or configure a database.

These are intentional boundaries for the assessment's single-offering service slice.
