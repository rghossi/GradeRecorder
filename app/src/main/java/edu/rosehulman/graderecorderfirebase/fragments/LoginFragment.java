package edu.rosehulman.graderecorderfirebase.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import edu.rosehulman.graderecorderfirebase.Constants;
import edu.rosehulman.graderecorderfirebase.R;
import edu.rosehulman.graderecorderfirebase.activities.GradeRecorderActivity;
import edu.rosehulman.graderecorderfirebase.models.Owner;
import edu.rosehulman.graderecorderfirebase.utils.SharedPreferencesUtils;
import edu.rosehulman.rosefire.RosefireAuth;


public class LoginFragment extends Fragment {

    private static final boolean SHOW_EMAIL_PASSWORD = true;
    private EditText mPasswordView;
    private EditText mEmailView;
    private View mLoginForm;
    private View mProgressSpinner;
    private boolean mLoggingIn;
    private OnLoginListener mOnLoginListener;
    private Firebase mFirebaseRef;
    private String mUid;
    private OwnerValueEventListener mOwnerValueEventListener;
    private Firebase mOwnerRef;

    public LoginFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoggingIn = false;
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        final FloatingActionButton fab = ((GradeRecorderActivity) getActivity()).getFab();
        fab.setVisibility(View.GONE);

        mEmailView = (EditText) rootView.findViewById(R.id.email);
        mPasswordView = (EditText) rootView.findViewById(R.id.password);
        mLoginForm = rootView.findViewById(R.id.login_form);
        mProgressSpinner = rootView.findViewById(R.id.login_progress);
        View loginButton = rootView.findViewById(R.id.email_sign_in_button);
        View rosefireLoginButton = rootView.findViewById(R.id.rosefire_sign_in_button);

        if (SHOW_EMAIL_PASSWORD) {
            // Feel free to set defaults here to speed your testing
            mEmailView.setText("some username");
            mPasswordView.setText("password");
        } else {
            mEmailView.setText("your roseusername");
            mPasswordView.setHint("password");
            loginButton.setVisibility(View.GONE);
        }

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        rosefireLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithRosefire();
            }
        });
        return rootView;
    }

    private void loginWithRosefire() {
        if (mLoggingIn) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();

        String password = mPasswordView.getText().toString();

        boolean cancelLogin = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.invalid_password));
            focusView = mPasswordView;
            cancelLogin = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.field_required));
            focusView = mEmailView;
            cancelLogin = true;
        } else if (!email.endsWith("@rose-hulman.edu")) {
            email += "@rose-hulman.edu";
        }

        if (cancelLogin) {
            // error in login
            focusView.requestFocus();
        } else {
            // show progress spinner, and start background task to login
            showProgress(true);
            mLoggingIn = true;
            RosefireAuth roseAuth = new RosefireAuth(mFirebaseRef, Constants.ROSEFIRE_REGISTRY_TOKEN);
            roseAuth.authWithRoseHulman(email, password, new RosefireAuthResultHandler());
            hideKeyboard();
        }

    }


    public void login() {
        if (mLoggingIn) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancelLogin = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.invalid_password));
            focusView = mPasswordView;
            cancelLogin = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.field_required));
            focusView = mEmailView;
            cancelLogin = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.invalid_email));
            focusView = mEmailView;
            cancelLogin = true;
        }

        if (cancelLogin) {
            // error in login
            focusView.requestFocus();
        } else {
            // show progress spinner, and start background task to login
            showProgress(true);
            mLoggingIn = true;
            mFirebaseRef.authWithPassword(email, password, new EmailPasswordAuthResultHandler());
            hideKeyboard();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
    }

    public void onLoginError(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.login_error))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();

        showProgress(false);
        mLoggingIn = false;
    }

    private void showProgress(boolean show) {
        mProgressSpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mOnLoginListener = (OnLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoginListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mOwnerValueEventListener != null) {
            mOwnerRef.removeEventListener(mOwnerValueEventListener);
        }
        mOwnerValueEventListener = null;
    }

    private void checkLogin() {
        mOwnerRef = new Firebase(Constants.OWNERS_PATH + "/" + mUid);

        if (mOwnerValueEventListener != null) {
            mOwnerRef.removeEventListener(mOwnerValueEventListener);
        }
        Log.d(Constants.TAG, "Adding OwnerValueListener for " + mOwnerRef.toString());
        mOwnerValueEventListener = new OwnerValueEventListener();
        mOwnerRef.addValueEventListener(mOwnerValueEventListener);
    }

    @SuppressLint("InflateParams")
    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Rose username");
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_get_rose_username, null);
        builder.setView(view);
        final EditText roseUsernameEditText = (EditText) view
                .findViewById(R.id.dialog_get_rose_username);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String roseUsername = roseUsernameEditText.getText().toString();
                        mOwnerRef = new Firebase(Constants.OWNERS_PATH + "/" + mUid);
                        mOwnerRef.child(Owner.USERNAME).setValue(roseUsername);
                        mOnLoginListener.onLoginComplete();
                    }
                }
        );
        builder.create().show();
    }


    public interface OnLoginListener {
        void onLoginComplete();
    }

    class EmailPasswordAuthResultHandler implements Firebase.AuthResultHandler {
        @Override
        public void onAuthenticated(AuthData authData) {
            Log.d(Constants.TAG, "User is authenticated");
            mUid = authData.getUid();
            SharedPreferencesUtils.setCurrentUser(getContext(), mUid);
            checkLogin();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Toast.makeText(getContext(), "User failed authentication: " + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    class RosefireAuthResultHandler implements Firebase.AuthResultHandler {
        @Override
        public void onAuthenticated(AuthData authData) {
            // Show logged in UI
            mUid = authData.getUid();
            SharedPreferencesUtils.setCurrentUser(getContext(), mUid);
            mOwnerRef = new Firebase(Constants.OWNERS_PATH + "/" + mUid);
            mOwnerRef.child(Owner.USERNAME).setValue(mUid);
            mOnLoginListener.onLoginComplete();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            // Show Login Error
            onLoginError("Rosefire authentication failed: " + firebaseError.getMessage());
        }
    }

    class OwnerValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String username = (String) dataSnapshot.child(Owner.USERNAME).getValue();
            Log.d(Constants.TAG, "Rose username in LoginActivity: " + username);
            if (username == null) {
                showUsernameDialog();
            } else {
                if (mOwnerValueEventListener != null) {

                }
                mOnLoginListener.onLoginComplete();
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.d(Constants.TAG, "OwnerValueListener cancelled: " + firebaseError);
        }
    }
}
