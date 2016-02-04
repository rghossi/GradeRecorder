package edu.rosehulman.graderecorderfirebase.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rosehulman.graderecorderfirebase.R;
import edu.rosehulman.graderecorderfirebase.activities.GradeRecorderActivity;
import edu.rosehulman.graderecorderfirebase.adapters.GradeEntryAdapter;
import edu.rosehulman.graderecorderfirebase.models.Assignment;
import edu.rosehulman.graderecorderfirebase.utils.Utils;

public class GradeEntryFragment extends Fragment implements Utils.FragmentWithToolbar {

    private static final String ARG_CURRENT_ASSIGNMENT = "CURRENT_ASSIGNMENT";

    private Assignment mCurrentAssignment;
    private GradeEntryAdapter mAdapter;
    private Toolbar mToolbar;

    public GradeEntryFragment() {
        // Required empty public constructor
    }

    public static GradeEntryFragment newInstance(Assignment assignment) {
        GradeEntryFragment fragment = new GradeEntryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CURRENT_ASSIGNMENT, assignment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentAssignment = getArguments().getParcelable(ARG_CURRENT_ASSIGNMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_grade_entry, container, false);

        FloatingActionButton fab = ((GradeRecorderActivity) getActivity()).getFab();
        fab.setImageResource(android.R.drawable.ic_menu_save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.saveChangesFirebase();
                Snackbar.make(view, "Grades saved successfully!", Snackbar.LENGTH_SHORT).show();
            }
        });
        fab.setVisibility(View.VISIBLE);

        mToolbar = ((GradeRecorderActivity) getActivity()).getToolbar();
        Utils.getCurrentCourseNameForToolbar(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.grade_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        registerForContextMenu(recyclerView);
        mAdapter = new GradeEntryAdapter(mCurrentAssignment);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void setToolbarTitle(String courseName) {
        if (courseName == null || courseName.isEmpty()) {
            mToolbar.setTitle("Choose a course");
        } else {
            mToolbar.setTitle("Grade entries for " + mCurrentAssignment.getName() + " of " + courseName);
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        FloatingActionButton fab = ((GradeRecorderActivity) getActivity()).getFab();
        fab.setImageResource(R.drawable.ic_add);
    }
}
