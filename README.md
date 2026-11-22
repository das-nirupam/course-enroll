# CourseWaitlist

An in-memory Java application for enrollment and FIFO waitlist promotion in one course offering.
It enforces supplied prerequisite eligibility, enrollment capacity, waitlist capacity, duplicate
prevention, and clear drop/enrollment outcomes.

## Requirements

Java 17 or newer.

## Run the application

On Windows:

```powershell
.\gradlew.bat run --args="3 2"
```

On macOS/Linux:

```sh
./gradlew run --args="3 2"
```

The arguments are enrollment capacity and optional waitlist capacity. If omitted, waitlist
capacity defaults to `Integer.MAX_VALUE`. Omit all arguments to enter the enrollment capacity
interactively.

Each enrollment request asks for a learner ID and whether the prerequisite is satisfied. A `no`
answer fails the request without enrolling or waitlisting the learner. The CLI supports dropping
learners, viewing offering status, and exiting. When a seat opens, the earliest eligible
waitlisted learner is promoted; ineligible waiters are skipped and removed.

## Executable JAR

Build the executable JAR:

```powershell
.\gradlew.bat jar
```

Run it with enrollment capacity and optional waitlist capacity:

```powershell
java -jar build/libs/course-waitlist.jar 3 2
```

On macOS/Linux, use `./gradlew jar` to build the same artifact.

## Sample session

This example uses enrollment capacity `1` and waitlist capacity `2`. Menu options and user input
are shown together; the CLI prints the menu before each choice.

```text
> java -jar build/libs/course-waitlist.jar 1 2
Course registration started for CourseWaitlist Demo (capacity: 1, waitlist capacity: 2).

1. Enroll learner
2. Drop learner
3. Show course status
4. Exit
Choose an option: 1
Learner ID: A
Prerequisite satisfied? (y/n): y
Enrollment result: ENROLLED

1. Enroll learner
2. Drop learner
3. Show course status
4. Exit
Choose an option: 3
Offering: OFFERING-1 - CourseWaitlist Demo
Capacity: 1
Enrolled (1): [A]
Waitlist capacity: 2
Waitlisted (0): []

1. Enroll learner
2. Drop learner
3. Show course status
4. Exit
Choose an option: 4
Goodbye.
```

In the interactive CLI, enter each value on its own line. The transcript omits build output for
readability.

## Run tests

On Windows, run `.\gradlew.bat test`; on macOS/Linux, run `./gradlew test`.

## Project documentation

- [Design specification](docs/Design-Spec.md)
- [AI prompt and use record](docs/AI-Usage-Record.md)
- [Limitations and follow-up risks](docs/LIMITATIONS.md)
- [OpenSpec capability specification](openspec/specs/course-waitlist/spec.md)
