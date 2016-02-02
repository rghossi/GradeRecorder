package edu.rosehulman.graderecorderfirebase.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * Created by Matt Boutell on 9/4/2015.
 */
public class Owner implements Comparable<Owner> {


    public static final String USERNAME = "username";
    public static final String COURSES = "courses";

    @JsonIgnore
    private String key;

    private String username;
    private Map<String, Boolean> courses;

    // Required by Firebase when deserializing json
    public Owner() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Boolean> getCourses() {
        return courses;
    }

    public void setCourses(Map<String, Boolean> courses) {
        this.courses = courses;
    }

    @Override
    public String toString() {
        return username;
    }

    public boolean containsCourse(String courseKey) {
        return courses != null && courses.containsKey(courseKey);
    }

    @Override
    public int compareTo(Owner another) {
        return username.compareTo(another.username);
    }

}
