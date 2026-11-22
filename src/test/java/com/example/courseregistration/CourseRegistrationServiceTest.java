package com.example.courseregistration;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class CourseRegistrationServiceTest {
@Test void fifoPromotion(){ var s=new CourseRegistrationService(1); s.markPrerequisiteComplete("A"); s.markPrerequisiteComplete("B"); assertEquals("ENROLLED",s.enroll("A")); assertEquals("WAITLISTED",s.enroll("B")); s.drop("A"); assertEquals(1,s.enrolledCount()); }
}
