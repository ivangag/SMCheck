package org.symptomcheck.capstone.network;



import com.activeandroid.Model;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.symptomcheck.capstone.converter.JacksonConverter;
import org.symptomcheck.capstone.model.UserInfo;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.MainThreadExecutor;
import retrofit.client.ApacheClient;
import retrofit.converter.GsonConverter;

/**
 * Created by igaglioti on 30/10/2014.
 */
public class DownloadHelper {


    private String userName;
    private String password;
    private static DownloadHelper downloadHelper = new DownloadHelper();
    private UserInfo userInfo;

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

    public DownloadHelper setUserName(String userName) {
        invalidateClient();
        this.userName = userName;
        return this;
    }

    private void invalidateClient() {
        symptomManagerSvcClient = null;
    }


    public DownloadHelper setPassword(String password) {
        invalidateClient();
        this.password = password;
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
            .setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
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


    public SymptomManagerSvcApi withRetrofitClient() {
        if(symptomManagerSvcClient == null) {
            symptomManagerSvcClient =
                    builder
                            .setUsername(this.userName)
                            .setPassword(this.password)
                            .setErrorHandler(error)
                            //.setExecutors(executor,null)
                            .setConverter(new GsonConverter(GSON))
                            .build()
                            .create(SymptomManagerSvcApi.class);
        }
        return symptomManagerSvcClient;
    }


/*
    public static class WithRetrofitClient {
        private String userName;
        private String password;

        public WithRetrofitClient setUserName(String userName){
            this.userName = userName;
            return this;
        }
        public WithRetrofitClient setPassword(String password){
            this.password = password;
            return this;
        }

        public SymptomManagerSvcApi Build(){

            return makeRetrofitClient(userName,password);
        }
    }*/
}
