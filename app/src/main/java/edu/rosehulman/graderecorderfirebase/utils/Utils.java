package edu.rosehulman.graderecorderfirebase.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import edu.rosehulman.graderecorderfirebase.Constants;
import edu.rosehulman.graderecorderfirebase.models.Assignment;
import edu.rosehulman.graderecorderfirebase.models.Course;
import edu.rosehulman.graderecorderfirebase.models.GradeEntry;
import edu.rosehulman.graderecorderfirebase.models.Owner;
import edu.rosehulman.graderecorderfirebase.models.Student;

/**
 * Created by Matt Boutell on 10/2/2015. Utility class that handles bookkeeping and one-off
 * access to Firebase.
 */
public class Utils {
    public static void removeCourse(Context context, Course course) {
        // MB: Moved to first to try to speed up UI. Test for race conditions.
        // Removes from list of courses
        Firebase courseRef = new Firebase(Constants.COURSES_PATH + "/" + course.getKey());
        courseRef.removeValue();

        // Remove this course from all its owners.
        Firebase ownersRef = new Firebase(Constants.OWNERS_PATH);
        for (String uid : course.getOwners().keySet()) {
            ownersRef.child(uid).child(Owner.COURSES).child(course.getKey()).removeValue();
        }

        // CONSIDER: Remove all students associated with this course


        // Remove all assignments
        final Firebase assignmentsRef = new Firebase(Constants.ASSIGNMENTS_PATH);
        Query assignmentsForCourseRef = assignmentsRef.orderByChild(Assignment.COURSE_KEY).equalTo(course.getKey());
        assignmentsForCourseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    assignmentsRef.child(snapshot.getKey()).removeValue();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d(Constants.TAG, "Cancelled");
            }
        });

        // CONSIDER: Remove all grade entries associated with this course


        // Remove from SharedPrefs
        // MB: CONSIDER What if we aren't removing the current course?
        SharedPreferencesUtils.removeCurrentCourseKey(context);
    }

    public static void signOut(Context context) {
        // Remove UID and active course from shared prefs
        SharedPreferencesUtils.removeCurrentCourseKey(context);
        SharedPreferencesUtils.removeCurrentUser(context);

        // Log out
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        ref.unauth();
    }


    public static void createGradeEntriesForAssignment(String courseKey, final String assignmentKey) {
        Firebase ref = new Firebase(Constants.STUDENTS_PATH);
        Query query = ref.orderByChild("courseKey").equalTo(courseKey);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Firebase firebase = new Firebase(Constants.GRADE_ENTRIES_PATH);
                GradeEntry gradeEntry = new GradeEntry();
                gradeEntry.setAssignmentKey(assignmentKey);
                gradeEntry.setStudentKey(dataSnapshot.getKey());
                firebase.push().setValue(gradeEntry);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public static void getCurrentCourseNameForToolbar(final FragmentWithToolbar fragment) {
        String currentCourseKey = SharedPreferencesUtils.getCurrentCourseKey(((Fragment) fragment).getContext());

        // If no course, then flag it as such
        if (currentCourseKey == null || currentCourseKey.isEmpty()) {
            fragment.setToolbarTitle("");
        }

        // Otherwise, get the course name from Firebase
        Firebase courseRef = new Firebase(Constants.COURSES_PATH + "/" + currentCourseKey);
        Log.d(Constants.TAG, "Adding listener for course key: " + currentCourseKey + " for path " + courseRef.child("name").toString());
        courseRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = (String) dataSnapshot.getValue();
                fragment.setToolbarTitle(title);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // empty
            }
        });
    }

    public interface FragmentWithToolbar {
        void setToolbarTitle(String courseName);
    }
}
