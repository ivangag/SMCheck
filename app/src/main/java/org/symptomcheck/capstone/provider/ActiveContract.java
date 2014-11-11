package org.symptomcheck.capstone.provider;

/**
 * Created by igaglioti on 04/11/2014.
 */
public class ActiveContract {
    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "org.symptomcheck.capstone";

    // LOCAL suffix means update local data by fetching the "fresh"  from the cloud DB to local DB (Download)
    // CLOUD suffix means update cloud data by uploading the "fresh" from the local DB to remote DB (Upload)
    public static final String SYNC_NONE = "SYNC_NONE";
    public static final String SYNC_ALL = "SYNC_ALL";
    public static final String SYNC_CLOUD_CHECK_IN = "SYNC_CLOUD_CHECK_IN";
    public static final String SYNC_LOCAL_CHECK_IN = "SYNC_LOCAL_CHECK_IN";
    public static final String SYNC_LOCAL_PATIENTS = "SYNC_LOCAL_PATIENTS";
    public static final String SYNC_CLOUD_PATIENTS = "SYNC_REMOTE_PATIENTS";
    public static final String SYNC_LOCAL_DOCTORS = "SYNC_LOCAL_DOCTORS";
    public static final String SYNC_CLOUD_DOCTORS = "SYNC_REMOTE_DOCTORS";
    public static final String SYNC_LOCAL_MEDICINES = "SYNC_LOCAL_MEDICINES";
    public static final String SYNC_CLOUD_MEDICINES = "SYNC_REMOTE_MEDICINES";



    public static class PATIENT_COLUMNS{
        public static final String PATIENT_ID = "patientId";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
        public static final String BIRTH_DATE = "birthDate";
    }
    public static class DOCTORS_COLUMNS{
        public static final String DOCTOR_ID = "doctorId";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
    }
}
