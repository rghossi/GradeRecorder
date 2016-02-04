package edu.rosehulman.graderecorderfirebase.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by rodrigr1 on 2/3/2016.
 */
public class GradeEntry {

    @JsonIgnore
    private String key;
    private String studentKey;
    private String assignmentKey;
    private String feedback;
    private int grade;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStudentKey() {
        return studentKey;
    }

    public void setStudentKey(String studentKey) {
        this.studentKey = studentKey;
    }

    public String getAssignmentKey() {
        return assignmentKey;
    }

    public void setAssignmentKey(String assignmentKey) {
        this.assignmentKey = assignmentKey;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }
}
