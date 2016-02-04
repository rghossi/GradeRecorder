package edu.rosehulman.graderecorderfirebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.rosehulman.graderecorderfirebase.Constants;
import edu.rosehulman.graderecorderfirebase.R;
import edu.rosehulman.graderecorderfirebase.models.Assignment;
import edu.rosehulman.graderecorderfirebase.models.GradeEntry;
import edu.rosehulman.graderecorderfirebase.models.Student;

/**
 * Created by rodrigr1 on 2/3/2016.
 */
public class GradeEntryAdapter extends RecyclerView.Adapter<GradeEntryAdapter.ViewHolder> {

    private List<GradeEntry> mGradeEntries;
    private Assignment mAssignment;
    private Firebase mGradeEntriesRef;

    public GradeEntryAdapter(Assignment assignment){
        mAssignment = assignment;
        mGradeEntries = new ArrayList<>();
        mGradeEntriesRef = new Firebase(Constants.GRADE_ENTRIES_PATH);

        Query query = mGradeEntriesRef.orderByChild("assignmentKey").equalTo(mAssignment.getKey());
        query.addChildEventListener(new GradeEntriesChildEventListener());
    }

    public void saveChangesFirebase(){
        for (GradeEntry g : mGradeEntries){
            mGradeEntriesRef.child(g.getKey()).setValue(g);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view_grades, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Firebase studentsRef = new Firebase(Constants.STUDENTS_PATH);
        Query q = studentsRef.child(mGradeEntries.get(position).getStudentKey());
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Student student = dataSnapshot.getValue(Student.class);
                holder.mUsernameTextView.setText(student.getRoseUsername());
                String grade = String.valueOf(mGradeEntries.get(position).getGrade());
                String feedback = mGradeEntries.get(position).getFeedback();
                if (grade != null)
                    holder.mGradeEditText.setText(grade);
                if (feedback != null)
                    holder.mFeedbackEditText.setText(feedback);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        TextWatcher gradeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) return;
                mGradeEntries.get(position).setGrade(Integer.valueOf(s.toString()));
            }
        };
        TextWatcher feedbackTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mGradeEntries.get(position).setFeedback(s.toString());
            }
        };

        holder.mGradeEditText.addTextChangedListener(gradeTextWatcher);
        holder.mFeedbackEditText.addTextChangedListener(feedbackTextWatcher);
    }

    @Override
    public int getItemCount() {
        return mGradeEntries.size();
    }

    private class GradeEntriesChildEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            GradeEntry grade = dataSnapshot.getValue(GradeEntry.class);
            grade.setKey(dataSnapshot.getKey());
            mGradeEntries.add(grade);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(Constants.TAG, "onChildMoved: " + s);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.d(Constants.TAG, firebaseError.getMessage());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mUsernameTextView;
        EditText mGradeEditText,
                 mFeedbackEditText;

        public ViewHolder(View itemView) {
            super(itemView);
            mUsernameTextView = (TextView) itemView.findViewById(R.id.rose_username_text_view);
            mGradeEditText = (EditText) itemView.findViewById(R.id.student_grade_edit_text);
            mFeedbackEditText = (EditText) itemView.findViewById(R.id.grader_comment_edit_text);
        }
    }
}
