package org.symptomcheck.capstone.network;



import android.content.Context;
import android.util.Log;

import com.activeandroid.Model;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.ui.LoginActivity;
import org.symptomcheck.capstone.utils.Costants;
import org.symptomcheck.capstone.utils.NotificationHelper;
import org.symptomcheck.capstone.preference.UserPreferencesManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.converter.GsonConverter;

/**
 * Created by igaglioti on 30/10/2014.
 */
public class DownloadHelper {

    public final static int HTTP_UNAUTHORIZED = 401;
    public final static int HTTP_BAD_REQUEST = 403;
    public final static int HTTP_OK = 200;

    private static final String TAG = "DownloadHelper";
    private String userName;
    private String password;
    private static DownloadHelper downloadHelper = new DownloadHelper();
    private UserInfo userInfo;
    private String accessToken;

    private DownloadHelper() {
        if(userInfo == null)
            userInfo = new UserInfo();
    }

    public static DownloadHelper get(){
        return downloadHelper;
    }

    /*
    public void setUser(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUser() {
        return userInfo;
    }*/

    /**
     *
     * @param userName username for login
     * N.B. Calling this method invalidate the current client
     * @return DownloadHelper
     */
    public DownloadHelper setUserName(String userName) {
        invalidateClient();
        this.userName = userName;
        return this;
    }

    /**
     *
     */
    private void invalidateClient() {
        this.accessToken = "";
        symptomManagerSvcClient = null;
    }


    /**
     *
     * @param password
     * @return
     */
    public DownloadHelper setPassword(String password) {
        invalidateClient();
        this.password = password;
        return this;
    }

    public DownloadHelper setAccessToken(String accessToken) {
        invalidateClient();
        this.accessToken = accessToken;
        return this;
    }

    private static class ErrorRecorder implements ErrorHandler {

        private RetrofitError error;

        @Override
        public Throwable handleError(RetrofitError cause) {
            error = cause;
            return error.getCause();
        }

        public RetrofitError getError() {
            return error;
        }
    }
    private static final String GAE_URL_TRUSTED = "https://spring-mvc-capstone-test.appspot.com";
    private static final String TEST_URL_REMOTE_TRUSTED = "https://spring-mvc-capstone-test.appspot.com";

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "pass";
    private final String CLIENT_PATIENT_ID = "patient";
    private static final String CLIENT_DOCTOR_ID = "doctor";
    static ErrorRecorder error = new ErrorRecorder();

    private SymptomManagerSvcApi symptomManagerSvcClient;

    SecuredRestBuilder builder =  new SecuredRestBuilder()
            .setLoginEndpoint(GAE_URL_TRUSTED + SymptomManagerSvcApi.TOKEN_PATH)
            .setClientId(this.CLIENT_PATIENT_ID)
            .setClient(new ApacheClient(CustomHttpsClient.createUnsafeClient()))
            .setEndpoint(GAE_URL_TRUSTED)
            .setLogLevel(RestAdapter.LogLevel.FULL);

    Executor executor = Executors.newSingleThreadExecutor();
    //JacksonConverter converter = new JacksonConverter(new ObjectMapper());

    private static final Gson GSON = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getDeclaringClass().equals(Model.class);
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }).create();


    public synchronized SymptomManagerSvcApi withRetrofitClient(Context context) {
        if(symptomManagerSvcClient == null) {
            symptomManagerSvcClient =
                    builder
                            .setUsername(this.userName)
                            .setPassword(this.password)
                            .setAccessToken(this.accessToken)
                            .setErrorHandler(error)
                            //.setExecutors(executor,null)
                            .setConverter(new GsonConverter(GSON))
                            .setContext(context)
                            .build()
                            .create(SymptomManagerSvcApi.class);
        }
        return symptomManagerSvcClient;
    }

    public synchronized void handleRetrofitError(Context context, RetrofitError error){
        // unauthorized client
        Log.i(TAG,String.format("handleRetrofitError. Status:%d", error.getResponse().getStatus()));
        if(error.getResponse().getStatus() == 401){
            UserInfo user = DAOManager.get().getUser();
            if (user != null)
                user.delete();
            UserPreferencesManager.get().setLogged(context,false);
            UserPreferencesManager.get().setBearerToken(context, "");
            NotificationHelper.sendNotification(context, 2,
                    "Login", "Your session is expired. Please re-enter credential",
                    LoginActivity.class, true, Costants.STRINGS.EMPTY,null);
        }
    }
}
