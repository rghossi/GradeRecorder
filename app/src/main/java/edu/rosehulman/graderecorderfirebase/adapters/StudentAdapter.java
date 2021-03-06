package edu.rosehulman.graderecorderfirebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.rosehulman.graderecorderfirebase.Constants;
import edu.rosehulman.graderecorderfirebase.R;
import edu.rosehulman.graderecorderfirebase.fragments.StudentListFragment;
import edu.rosehulman.graderecorderfirebase.models.Student;

public final class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private final StudentListFragment mStudentListFragment;
    private String mCourseKey;
    private Firebase mStudentsRef;
    private List<Student> mStudents = new ArrayList<>();

    public StudentAdapter(StudentListFragment studentListFragment, String courseKey) {
        mStudentListFragment = studentListFragment;
        mCourseKey = courseKey;
        mStudentsRef = new Firebase(Constants.STUDENTS_PATH);
        // NOTE: only listen to students in this course.
        Query studentsForCourseRef = mStudentsRef.orderByChild(Student.COURSE_KEY).equalTo(courseKey);
        studentsForCourseRef.addChildEventListener(new StudentsChildEventListener());
    }

    public void firebasePush(String firstName, String lastName, String roseUsername, String team) {
        Student student = new Student(mCourseKey, firstName, lastName, roseUsername, team);
        mStudentsRef.push().setValue(student);
    }

    public void firebaseEdit(Student student, String firstName, String lastName, String roseUsername, String team) {
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setRoseUsername(roseUsername);
        student.setTeam(team);
        mStudentsRef.child(student.getKey()).setValue(student);
    }

    public void firebaseRemove(Student studentToRemove) {
        mStudentsRef.child(studentToRemove.getKey()).removeValue();
    }

    // For swipe to delete
    public Student hide(int position) {
        Student student = mStudents.remove(position);
        notifyItemRemoved(position);
        return student;
    }

    public void undoHide(Student student, int position) {
        mStudents.add(position, student);
        notifyItemInserted(position);
    }

    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StudentAdapter.ViewHolder holder, int position) {
        final Student student = mStudents.get(position);
        String name = student.getFirstName() + " " + student.getLastName();
        holder.mNameTextView.setText(name);
        holder.mRoseUsernameTextView.setText(student.getRoseUsername());
        if (student.getTeam().isEmpty()) {
            holder.mTeamTextView.setVisibility(View.GONE);
        } else {
            holder.mTeamTextView.setVisibility(View.VISIBLE);
            holder.mTeamTextView.setText(student.getTeam());
        }
    }

    @Override
    public int getItemCount() {
        return mStudents.size();
    }

    private class StudentsChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
            add(dataSnapshot);
            // We think using notifyItemInserted can cause crashes due to animation race condition.
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
            remove(dataSnapshot.getKey());
            add(dataSnapshot);
            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            remove(dataSnapshot.getKey());
            notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {
            // empty
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.e(Constants.TAG, "Error: " + firebaseError.getMessage());
        }

        private void add(DataSnapshot dataSnapshot) {
            Student student = dataSnapshot.getValue(Student.class);
            student.setKey(dataSnapshot.getKey());
            mStudents.add(student);
            Collections.sort(mStudents);
        }

        private void remove(String key) {
            for (Student s : mStudents) {
                if (s.getKey().equals(key)) {
                    mStudents.remove(s);
                    break;
                }
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private TextView mNameTextView;
        private TextView mRoseUsernameTextView;
        private TextView mTeamTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
            mRoseUsernameTextView = (TextView) itemView.findViewById(R.id.rose_username_text_view);
            mTeamTextView = (TextView) itemView.findViewById(R.id.team_text_view);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            Student student = mStudents.get(getAdapterPosition());
            mStudentListFragment.showStudentDialog(student);
            return true;
        }
    }
}
