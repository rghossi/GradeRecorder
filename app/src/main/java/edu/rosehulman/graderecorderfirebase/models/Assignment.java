package edu.rosehulman.graderecorderfirebase.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Matt Boutell on 10/16/2015.
 */
public class Assignment implements Comparable<Assignment>, Parcelable {
    public static final String COURSE_KEY = "courseKey";

    @JsonIgnore
    private String key;

    private String courseKey;
    private String name;
    private double maxGrade;

    // Used by Firebase for deserializing JSON
    public Assignment() {
    }

    // Used when creating from scratch
    public Assignment(String courseKey, String name, double maxGrade) {
        this.courseKey = courseKey;
        this.name = name;
        this.maxGrade = maxGrade;
    }

    protected Assignment(Parcel in) {
        key = in.readString();
        courseKey = in.readString();
        name = in.readString();
        maxGrade = in.readDouble();
    }

    public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
        @Override
        public Assignment createFromParcel(Parcel in) {
            return new Assignment(in);
        }

        @Override
        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCourseKey() {
        return courseKey;
    }

    public void setCourseKey(String courseKey) {
        this.courseKey = courseKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(double maxGrade) {
        this.maxGrade = maxGrade;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Assignment another) {
        return name.compareTo(another.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(courseKey);
        dest.writeString(name);
        dest.writeDouble(maxGrade);
    }
}
