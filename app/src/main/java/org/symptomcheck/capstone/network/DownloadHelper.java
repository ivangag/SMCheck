package org.symptomcheck.capstone.network;



import org.symptomcheck.capstone.model.UserInfo;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;

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

    public void setUser(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUser() {
        return userInfo;
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

    public SymptomManagerSvcApi withRetrofitClient() {
        if(symptomManagerSvcClient == null) {
            symptomManagerSvcClient =
                    builder
                            .setUsername(this.userName)
                            .setPassword(this.password)
                            .setErrorHandler(error)
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
