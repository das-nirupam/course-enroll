package com.example.courseregistration;
import java.util.*;
public class CourseRegistrationService {
 private final int capacity; private final Set<String> enrolled=new LinkedHashSet<>(); private final Queue<String> waitlist=new ArrayDeque<>(); private final Set<String> completed=new HashSet<>();
 public CourseRegistrationService(int capacity){this.capacity=capacity;}
 public void markPrerequisiteComplete(String learner){completed.add(learner);}
 public String enroll(String learner){ if(!completed.contains(learner)) return "PREREQ_FAILED"; if(enrolled.contains(learner)||waitlist.contains(learner)) return "DUPLICATE"; if(enrolled.size()<capacity){enrolled.add(learner); return "ENROLLED";} waitlist.add(learner); return "WAITLISTED";}
 public String drop(String learner){ if(enrolled.remove(learner)){ promote(); return "DROPPED";} if(waitlist.remove(learner)) return "REMOVED_WAITLIST"; return "NOT_FOUND";}
 private void promote(){ while(!waitlist.isEmpty()){String l=waitlist.poll(); if(completed.contains(l)){ enrolled.add(l); break;}} }
 public int enrolledCount(){return enrolled.size();}
}
