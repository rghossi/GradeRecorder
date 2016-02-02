package edu.rosehulman.graderecorderfirebase.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.firebase.client.Firebase;

import edu.rosehulman.graderecorderfirebase.Constants;
import edu.rosehulman.graderecorderfirebase.R;
import edu.rosehulman.graderecorderfirebase.fragments.AssignmentListFragment;
import edu.rosehulman.graderecorderfirebase.fragments.CourseListFragment;
import edu.rosehulman.graderecorderfirebase.fragments.LoginFragment;
import edu.rosehulman.graderecorderfirebase.fragments.OwnerListFragment;
import edu.rosehulman.graderecorderfirebase.fragments.StudentListFragment;
import edu.rosehulman.graderecorderfirebase.models.Assignment;
import edu.rosehulman.graderecorderfirebase.models.Course;
import edu.rosehulman.graderecorderfirebase.utils.SharedPreferencesUtils;
import edu.rosehulman.graderecorderfirebase.utils.Utils;

public class GradeRecorderActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LoginFragment.OnLoginListener,
        AssignmentListFragment.OnAssignmentSelectedListener,
        CourseListFragment.OnCourseSelectedListener,
        OwnerListFragment.OnThisOwnerRemovedListener {

    private FloatingActionButton mFab;
    private Toolbar mToolbar;

    private Firebase mFirebaseRef;

    public FloatingActionButton getFab() {
        return mFab;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_recorder);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Skipped during rotation, but Firebase settings should persist.
        if (savedInstanceState == null) {
            initializeFirebase();
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            Firebase.setAndroidContext(this);
        }
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
        if (mFirebaseRef.getAuth() == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, new LoginFragment());
            ft.commit();
        } else {
            onLoginComplete();
        }
    }

    private void initializeFirebase() {
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
        mFirebaseRef.keepSynced(true);
    }

    @Override
    public void onLoginComplete() {
        Log.d(Constants.TAG, "User is authenticated");
        String uid = mFirebaseRef.getAuth().getUid();
        SharedPreferencesUtils.setCurrentUser(this, uid);

        // Check if they have a current course
        String currentCourseKey = SharedPreferencesUtils.getCurrentCourseKey(this);
        Fragment switchTo;
        if (currentCourseKey == null || currentCourseKey.isEmpty()) {
            switchTo = new CourseListFragment();
        } else {
            switchTo = AssignmentListFragment.newInstance(currentCourseKey);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, switchTo);
        ft.commit();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String currentCourseKey = SharedPreferencesUtils.getCurrentCourseKey(this);
        int id = item.getItemId();
        Fragment switchTo = null;


// TODO: May be useful if I implement return to the chosen fragment after choosing a course.
        if (id == R.id.nav_sign_out) {
            Utils.signOut(this);
            switchTo = new LoginFragment();
        } else if (id == R.id.nav_courses || currentCourseKey == null) {
            switchTo = new CourseListFragment();
        } else if (id == R.id.nav_assignments) {
            switchTo = AssignmentListFragment.newInstance(currentCourseKey);
        } else if (id == R.id.nav_students) {
            switchTo = new StudentListFragment();
        } else if (id == R.id.nav_owners) {
            switchTo = new OwnerListFragment();
        }

        if (switchTo != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, switchTo);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onAssignmentSelected(Assignment assignment) {
        // TODO: go to grade entry fragment


    }

    @Override
    public void onCourseSelected(Course selectedCourse) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, AssignmentListFragment.newInstance(selectedCourse.getKey()));
        ft.addToBackStack("course_fragment");
        ft.commit();
    }

    @Override
    public void onThisOwnerRemoved() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, new CourseListFragment());
        ft.commit();
    }
}
