package org.symptomcheck.capstone.provider;

import android.provider.BaseColumns;

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
    //public static final String SYNC_CLOUD_CHECK_IN = "SYNC_CLOUD_CHECK_IN";
    public static final String SYNC_CHECK_IN = "SYNC_CHECK_IN";
    public static final String SYNC_PATIENTS = "SYNC_PATIENTS";
    //public static final String SYNC_CLOUD_PATIENTS = "SYNC_REMOTE_PATIENTS";
    public static final String SYNC_DOCTORS = "SYNC_DOCTORS";
    //public static final String SYNC_CLOUD_DOCTORS = "SYNC_REMOTE_DOCTORS";
    public static final String SYNC_MEDICINES = "SYNC_MEDICINES";

    //public static final String SYNC_CLOUD_MEDICINES = "SYNC_REMOTE_MEDICINES";

    public static final String[] DOCTOR_TABLE_PROJECTION = new String[]{
            BaseColumns._ID + ","
                    + DOCTORS_COLUMNS.DOCTOR_ID + ","
                    + DOCTORS_COLUMNS.FIRST_NAME + ","
                    + DOCTORS_COLUMNS.LAST_NAME
    };

    public static String[] PATIENT_TABLE_PROJECTION = new String[]{
                    BaseColumns._ID + ","
                    + PATIENT_COLUMNS.PATIENT_ID + ","
                            + PATIENT_COLUMNS.FIRST_NAME + ","
                            + PATIENT_COLUMNS.LAST_NAME + ","
                            + PATIENT_COLUMNS.BIRTH_DATE
            };

    public static String[] CHECK_IN_TABLE_PROJECTION = new String[]{
                    BaseColumns._ID + ","
                            + CHECKIN_COLUMNS.PAIN_LEVEL + ","
                            + CHECKIN_COLUMNS.FEED_STATUS + ","
                            + CHECKIN_COLUMNS.ISSUE_TIME
            };

    public static String[] MEDICINES_TABLE_PROJECTION = new String[]{
                    BaseColumns._ID + ","
                            + MEDICINES_COLUMNS.NAME + ","
                            + MEDICINES_COLUMNS.PATIENT_ID + ","
                            + MEDICINES_COLUMNS.TAKING_TIME
            };

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

    public static class CHECKIN_COLUMNS{
        public static final String PAIN_LEVEL = "issuePainLevel";
        public static final String FEED_STATUS = "issueFeedStatus";
        public static final String ISSUE_TIME = "issueDateTime";
    }
    public static class MEDICINES_COLUMNS{
        public static final String NAME = "medicationName";
        public static final String TAKING_TIME = "lastTakingDateTime";
        public static final String PATIENT_ID = "patientMedicalNumber";
    }
}
