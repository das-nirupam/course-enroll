package com.coursewaitlist.cli;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseRegistrationCliTest {
    @Test
    void runsEnrollmentAndDisplaysStatus() {
        String commands = "1\nA\ny\n3\n4\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        CourseRegistrationCli.run(new String[]{"1", "2"}, new Scanner(commands),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        String transcript = output.toString(StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Prerequisite satisfied? (y/n): "));
        assertTrue(transcript.contains("Enrollment result: ENROLLED"));
        assertTrue(transcript.contains("Enrolled (1): [A]"));
        assertTrue(transcript.contains("Goodbye."));
    }

    @Test
    void doesNotEnrollWhenPrerequisiteIsNotSatisfied() {
        String commands = "1\nA\nn\n3\n4\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        CourseRegistrationCli.run(new String[]{"1", "2"}, new Scanner(commands),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        String transcript = output.toString(StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Enrollment result: PREREQUISITE_FAILED"));
        assertTrue(transcript.contains("Enrolled (0): []"));
        assertTrue(transcript.contains("Waitlisted (0): []"));
    }

    @Test
    void rejectsNegativeWaitlistCapacity() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        CourseRegistrationCli.run(new String[]{"1", "-1"}, new Scanner(""),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        assertTrue(output.toString(StandardCharsets.UTF_8)
                .contains("Capacity cannot be negative."));
    }

    @Test
    void blankLinesRepeatCurrentPromptWithoutRedrawingMenu() {
        String commands = "\n1\n\nA\n\ny\n3\n4\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        CourseRegistrationCli.run(new String[]{"1", "2"}, new Scanner(commands),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        String transcript = output.toString(StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Enrollment result: ENROLLED"));
        assertTrue(transcript.contains("Learner ID cannot be empty."));
        assertFalse(transcript.contains("Invalid option."));
        assertFalse(transcript.contains("Enter y or n."));
        assertEquals(3, transcript.split("1\\. Enroll learner", -1).length - 1);
        assertTrue(transcript.indexOf("Learner ID: A")
                < transcript.indexOf("Prerequisite satisfied? (y/n): "));
        assertTrue(transcript.indexOf("Prerequisite satisfied? (y/n): ")
                < transcript.indexOf("Enrollment result: ENROLLED"));
    }

    @Test
    void blankInteractiveCapacityRepeatsWithoutAnError() {
        String commands = "\n1\n4\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        CourseRegistrationCli.run(new String[0], new Scanner(commands),
                new PrintStream(output, true, StandardCharsets.UTF_8));

        String transcript = output.toString(StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Course registration started"));
        assertFalse(transcript.contains("Capacity must be a non-negative whole number."));
        assertTrue(transcript.contains("Goodbye."));
    }

    @Test
    void showsTheLearnerPromptAgainBeforeWaitingForMoreInput() throws Exception {
        PipedInputStream input = new PipedInputStream();
        PipedOutputStream commands = new PipedOutputStream(input);
        SynchronizedOutputStream transcript = new SynchronizedOutputStream();
        PrintStream output = new PrintStream(transcript, true, StandardCharsets.UTF_8);
        Thread cli = new Thread(() -> CourseRegistrationCli.run(
                new String[]{"1", "2"}, new Scanner(input), output));

        try {
            cli.start();
            transcript.awaitOccurrences("Choose an option: ", 1);
            commands.write("1\n".getBytes(StandardCharsets.UTF_8));
            commands.flush();
            transcript.awaitOccurrences("Learner ID: ", 1);

            commands.write("\n".getBytes(StandardCharsets.UTF_8));
            commands.flush();
            String afterBlankId = transcript.awaitOccurrences("Learner ID: ", 2);
            assertTrue(afterBlankId.contains("Learner ID cannot be empty."));

            commands.write("A\ny\n4\n".getBytes(StandardCharsets.UTF_8));
            commands.flush();
            cli.join(2_000);

            assertFalse(cli.isAlive());
            assertTrue(transcript.toString().contains("Enrollment result: ENROLLED"));
        } finally {
            commands.close();
            cli.join(2_000);
        }
    }

    private static final class SynchronizedOutputStream extends OutputStream {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        @Override
        public synchronized void write(int value) {
            bytes.write(value);
            notifyAll();
        }

        synchronized String awaitOccurrences(String value, int expectedCount)
                throws InterruptedException {
            long deadline = System.nanoTime() + 2_000_000_000L;
            while (countOccurrences(bytes.toString(StandardCharsets.UTF_8), value) < expectedCount) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    throw new AssertionError("Timed out waiting for prompt: " + value);
                }
                wait(Math.max(1, remaining / 1_000_000L));
            }
            return bytes.toString(StandardCharsets.UTF_8);
        }

        private int countOccurrences(String text, String value) {
            int count = 0;
            int index = 0;
            while ((index = text.indexOf(value, index)) >= 0) {
                count++;
                index += value.length();
            }
            return count;
        }

        @Override
        public synchronized String toString() {
            return bytes.toString(StandardCharsets.UTF_8);
        }
    }
}
