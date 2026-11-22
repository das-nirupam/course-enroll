package com.coursewaitlist.cli;

import java.io.PrintStream;
import java.util.Scanner;

import com.coursewaitlist.domain.Course;
import com.coursewaitlist.domain.CourseOffering;
import com.coursewaitlist.domain.Learner;
import com.coursewaitlist.repository.InMemoryCourseRegistrationRepository;
import com.coursewaitlist.service.CourseRegistrationService;

public final class CourseRegistrationCli {
    private CourseRegistrationCli() {
    }

    public static void main(String[] args) {
        run(args, new Scanner(System.in), System.out);
    }

    public static void run(String[] args, Scanner input, PrintStream output) {
        if (args.length > 2) {
            output.println("Usage: gradlew run --args=\"<capacity> [waitlist-capacity]\"");
            return;
        }

        Integer capacity;
        if (args.length > 0) {
            capacity = parseCapacity(args[0], output);
        } else {
            capacity = promptForCapacity(input, output);
        }
        if (capacity == null) {
            return;
        }

        Integer waitlistCapacity = Integer.valueOf(Integer.MAX_VALUE);
        if (args.length == 2) {
            waitlistCapacity = parseCapacity(args[1], output);
        }
        if (waitlistCapacity == null) {
            return;
        }

        Course course = new Course("COURSE-1", "CourseWaitlist Demo",
                "Single-offering enrollment demonstration", java.util.Set.of());
        CourseOffering offering = new CourseOffering(
                "OFFERING-1", course, capacity, waitlistCapacity);
        CourseRegistrationService service = new CourseRegistrationService(
                offering, new InMemoryCourseRegistrationRepository());
        output.println("Course registration started for " + course.getCourseName()
                + " (capacity: " + capacity + ", waitlist capacity: "
                + displayWaitlistCapacity(waitlistCapacity) + ").");

        boolean running = true;
        while (running) {
            printMenu(output);
            String choice = readMenuChoice(input, output);
            if (choice == null) {
                break;
            }
            switch (choice) {
                case "1":
                    enroll(input, output, service);
                    break;
                case "2":
                    drop(input, output, service);
                    break;
                case "3":
                    showStatus(output, service);
                    break;
                case "4":
                    output.println("Goodbye.");
                    running = false;
                    break;
                default:
                    output.println("Invalid option. Enter a number from 1 to 4.");
            }
        }
    }

    private static Integer parseCapacity(String value, PrintStream output) {
        try {
            int capacity = Integer.parseInt(value);
            if (capacity < 0) {
                output.println("Capacity cannot be negative.");
                return null;
            }
            return capacity;
        } catch (NumberFormatException exception) {
            output.println("Capacity must be a non-negative whole number.");
            return null;
        }
    }

    private static Integer promptForCapacity(Scanner input, PrintStream output) {
        while (true) {
            output.print("Enter course capacity: ");
            output.flush();
            if (!input.hasNextLine()) {
                return null;
            }
            String value = input.nextLine().trim();
            if (value.isEmpty()) {
                output.println();
                continue;
            }
            Integer capacity = parseCapacity(value, output);
            if (capacity != null) {
                return capacity;
            }
        }
    }

    private static void printMenu(PrintStream output) {
        output.println("1. Enroll learner");
        output.println("2. Drop learner");
        output.println("3. Show course status");
        output.println("4. Exit");
        output.print("Choose an option: ");
        output.flush();
    }

    private static String readMenuChoice(Scanner input, PrintStream output) {
        while (true) {
            if (!input.hasNextLine()) {
                return null;
            }
            String choice = input.nextLine().trim();
            if (!choice.isEmpty()) {
                return choice;
            }
            output.println();
            output.print("Choose an option: ");
            output.flush();
        }
    }

    private static void enroll(
            Scanner input, PrintStream output, CourseRegistrationService service) {
        String learner = readLearnerId(input, output);
        if (learner == null) {
            return;
        }
        Boolean prerequisiteSatisfied = readPrerequisiteStatus(input, output);
        if (prerequisiteSatisfied == null) {
            return;
        }
        service.updateLearner(new Learner(learner, prerequisiteSatisfied));
        output.println("Enrollment result: " + service.enroll(learner));
    }

    private static void drop(
            Scanner input, PrintStream output, CourseRegistrationService service) {
        String learner = readLearnerId(input, output);
        if (learner != null) {
            output.println("Drop result: " + service.drop(learner));
        }
    }

    private static String readLearnerId(Scanner input, PrintStream output) {
        while (true) {
            output.print("Learner ID: ");
            output.flush();
            if (!input.hasNextLine()) {
                return null;
            }
            String learner = input.nextLine().trim();
            if (!learner.isEmpty()) {
                return learner;
            }
            output.println("Learner ID cannot be empty.");
        }
    }

    private static Boolean readPrerequisiteStatus(Scanner input, PrintStream output) {
        while (true) {
            output.print("Prerequisite satisfied? (y/n): ");
            output.flush();
            if (!input.hasNextLine()) {
                return null;
            }
            String answer = input.nextLine().trim();
            if (answer.isEmpty()) {
                output.println();
                continue;
            }
            if (answer.equalsIgnoreCase("y")) {
                return true;
            }
            if (answer.equalsIgnoreCase("n")) {
                return false;
            }
            output.println("Enter y or n.");
        }
    }

    private static void showStatus(PrintStream output, CourseRegistrationService service) {
        CourseOffering offering = service.offering();
        output.println("Offering: " + offering.getOfferingId() + " - "
                + offering.getCourse().getCourseName());
        output.println("Capacity: " + offering.getCapacity());
        output.println("Enrolled (" + service.enrolledCount() + "): "
                + service.enrolledLearners());
        output.println("Waitlist capacity: "
                + displayWaitlistCapacity(offering.getWaitlistCapacity()));
        output.println("Waitlisted (" + service.waitlistCount() + "): "
                + service.waitlistedLearners());
    }

    private static String displayWaitlistCapacity(int capacity) {
        return capacity == Integer.MAX_VALUE ? "unlimited" : Integer.toString(capacity);
    }
}
