package org.symptomcheck.capstone.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.alarms.SymptomAlarmRequest;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.gcm.GcmRegistrationService;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.network.DownloadHelper;
import org.symptomcheck.capstone.network.SymptomManagerSvcApi;
import org.symptomcheck.capstone.utils.NotificationHelper;
import org.symptomcheck.capstone.preference.UserPreferencesManager;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends Activity{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextView mErrorLoginMsg;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mErrorFormView;
    private CheckBox mCheckInRememberMe;
    private static final String TAG = "LoginActivity";


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //GoogleCloudMessaging gcm;
    //private String regid;

    private int mNextActivityToLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mNextActivityToLaunch = getIntent().getIntExtra(NotificationHelper.NEXT_ACTIVITY_TO_LAUNCH, NotificationHelper.GO_TO_MAIN);


        setContentView(R.layout.activity_login);
        // Set up the login form.
        mErrorLoginMsg = (TextView) findViewById(R.id.txt_login_error);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mCheckInRememberMe = (CheckBox) findViewById(R.id.checkBoxRememberMe);
        mCheckInRememberMe.setChecked(UserPreferencesManager.get().getLoginRememberMe(this));

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.layout_login_progress);
        mErrorFormView = findViewById(R.id.layout_login_error);

        if(UserPreferencesManager.get().getLoginRememberMe(this)
                && (DAOManager.get().getUser() !=  null)
                && (DAOManager.get().getUser().getLogged()))
            attemptLogin();
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email;
        final String password;
        boolean skipCheckField = false;
        if(UserPreferencesManager.get().getLoginRememberMe(this)
                && UserPreferencesManager.get().IsLogged(this)
                //&& (DAOManager.get().getUser() !=  null)
                //&& (DAOManager.get().getUser().getLogged())
                        )
                    {
            email = "";
            password = "";
            skipCheckField = true;
            //email = UserPreferencesManager.get().getLoginUsername(this);
            //password = UserPreferencesManager.get().getLoginPassword(this);
        }else{
            email = mEmailView.getText().toString();
            password = mPasswordView.getText().toString();
        }

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)
                && !skipCheckField) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)
                && !skipCheckField) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }/* else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true,false);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
/*
            DownloadHelper.get().setPassword(password).setUserName(email)
                    .withRetrofitClient()
                    .verifyUser(new Callback<UserInfo>() {
                        @Override
                        public void success(UserInfo userInfo, Response response) {
                            userInfo.setLogged(true);
                            DownloadHelper.get().setUser(userInfo);
                            DAOManager.get().saveUser(userInfo);
                            handleAfterLoginAttempt(true,email,password);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            handleAfterLoginAttempt(false,email,password);
                        }
                    });
                    */
        }
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, final boolean errorOnLogin) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
            mErrorFormView.setVisibility(errorOnLogin ? View.VISIBLE : View.GONE);
            mErrorFormView.animate().setDuration(shortAnimTime).alpha(
                    errorOnLogin ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mErrorFormView.setVisibility(errorOnLogin ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mErrorFormView.setVisibility(errorOnLogin ? View.VISIBLE : View.GONE);
        }
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, ErrorLogin> {

        private final String mEmail;
        private final String mPassword;
        private UserInfo userInfo;
        final private ErrorLogin errorLogin = new ErrorLogin();

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected ErrorLogin doInBackground(Void... params) {
            // Attempt authentication against a network service.
            errorLogin.onSuccess = true;
            final String token = UserPreferencesManager.get().getBearerToken(getApplicationContext());
            try {
                // Simulate network access.
                //Thread.sleep(2000);


                SymptomManagerSvcApi client;
                if(mEmail.isEmpty()
                        || mPassword.isEmpty()){
                    client = DownloadHelper.get()
                            .setAccessToken(token)
                            .withRetrofitClient(getApplicationContext());
                }else{
                    client = DownloadHelper.get().
                            setUserName(mEmail).
                            setPassword(mPassword).
                            withRetrofitClient(getApplicationContext());
                }

                userInfo = client.verifyUser();
                userInfo.setLogged(true);

                DAOManager.get().saveUser(userInfo);

            } catch (Exception e) {
                errorLogin.onSuccess = false;
                errorLogin.error = e;
                Log.e(TAG,String.format("Error on verifyUser:%s; User:%s Pw:%s Token:%s. ", e.getMessage(),mEmail,mPassword,token));
            }

           // TODO: register the new account here.
            return errorLogin;
        }

        @Override
        protected void onPostExecute(final ErrorLogin login) {
            mAuthTask = null;

            if(login.onSuccess) {
                handleGCMRegistrationRequest(getApplicationContext());
            }
            handleAfterLoginAttempt(login,mEmail,mPassword);
        }


        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false,false);
        }
    }



    public void handleGCMRegistrationRequest(Context context) {
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices(context)) {
            GcmRegistrationService.startDeviceRegistration(context);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }



    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     * @param context
     */
    private boolean checkPlayServices(final Context context) {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                if(context instanceof Activity){
                    ((Activity)context).runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    GooglePlayServicesUtil.getErrorDialog(resultCode,  ((Activity)context),
                                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                                }
                            }
                    );
                }else{
                    Log.e(TAG, "checkPlayServices: " + GooglePlayServicesUtil.getErrorString(resultCode));
                }
            } else {
                Log.i(TAG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }

    public void handleAfterLoginAttempt(ErrorLogin result, String username, String password) {

        final Context context = getApplicationContext();
        boolean showFormError = !result.onSuccess;
        String errorMsg = "";
        if (result.onSuccess) {
            //SyncUtils.TriggerRefreshPartialLocal(ActiveContract.SYNC_ALL);
            SymptomAlarmRequest.get().setAlarm(context, SymptomAlarmRequest.AlarmRequestedType.ALARM_REMINDER);
            UserPreferencesManager.get().setLoginRememberMe(context,mCheckInRememberMe.isChecked());
            //UserPreferencesManager.get().setLoginUsername(context,username);
            //UserPreferencesManager.get().setLoginPassword(context,password);

            finish();

            Intent intent = new Intent(getApplicationContext(),getNextActivityToLaunch());
            startActivity(intent);

        } else {
            final Throwable errorCause = result.error.getCause();
            if(errorCause.getClass().equals(RetrofitError.class)){
                final Response response = ((RetrofitError)(errorCause)).getResponse();
                if(response.getStatus() == DownloadHelper.HTTP_UNAUTHORIZED){
                    showFormError = false;
                    UserPreferencesManager.get().setLogged(getApplicationContext(),false);
                    UserPreferencesManager.get().setBearerToken(getApplicationContext(), "");
                    mPasswordView.setError(getString(R.string.error_incorrect_credentials));
                    mPasswordView.requestFocus();

                    errorMsg =  String.format(getString(R.string.error_login_header),String.valueOf(response.getStatus()));
                }
            }else{
                errorMsg =  String.format(getString(R.string.error_login_header),errorCause.getMessage());
            }
            mErrorLoginMsg.setText(errorMsg);
            showProgress(false,showFormError);
        }

    }

    private Class<?> getNextActivityToLaunch() {
        Class<?> activityToLaunch;
        if(mNextActivityToLaunch == NotificationHelper.GO_TO_CHECK_IN){
            activityToLaunch = CheckInFlowActivity.class;
        }else if(mNextActivityToLaunch == NotificationHelper.GO_TO_MAIN){
            activityToLaunch = MainActivity.class;
        }else {
            activityToLaunch = MainActivity.class;
        }
        return activityToLaunch;
    }

    public static void startLogin(Context context/*, String param1, String param2*/) {
        Intent intent = new Intent(context, LoginActivity.class);
        //intent.setAction(ACTION_GCM_DEVICE_REGISTRATION);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    private static class ErrorLogin {
        public Throwable error;
        public boolean onSuccess;
    }

}



