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
                            + CHECKIN_COLUMNS.UNIT_ID + ","
                            + CHECKIN_COLUMNS.PAIN_LEVEL + ","
                            + CHECKIN_COLUMNS.FEED_STATUS + ","
                            + CHECKIN_COLUMNS.PATIENT + ","
                            + CHECKIN_COLUMNS.ISSUE_TIME
            };

    public static String[] MEDICINES_TABLE_PROJECTION = new String[]{
                    BaseColumns._ID + ","
                            + MEDICINES_COLUMNS.PRODUCT_ID + ","
                            + MEDICINES_COLUMNS.NAME + ","
                            + MEDICINES_COLUMNS.PATIENT + ","
                            + MEDICINES_COLUMNS.TAKING_TIME
            };
    public static String[] EXPERIENCES_TABLE_PROJECTION = new String[]{
                    BaseColumns._ID + ","
                            + EXPERIENCES_COLUMNS.PATIENT + ","
                            + EXPERIENCES_COLUMNS.START_EXPERIENCE_TIME + ","
                            + EXPERIENCES_COLUMNS.END_EXPERIENCE_TIME + ","
                            + EXPERIENCES_COLUMNS.SEEN_BY_DOCTOR + ","
                            + EXPERIENCES_COLUMNS.UNIT_ID + ","
                            + EXPERIENCES_COLUMNS.EXPERIENCE_DURATION + ","
                            + EXPERIENCES_COLUMNS.EXPERIENCE_TYPE
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
        public static final String PATIENT = "Patient";
        public static final String UNIT_ID = "unitId";
    }
    public static class MEDICINES_COLUMNS{
        public static final String NAME = "medicationName";
        public static final String TAKING_TIME = "lastTakingDateTime";
        public static final String PATIENT = "patientMedicalNumber";
        public static final String PRODUCT_ID = "productId";
    }
    public static class EXPERIENCES_COLUMNS{
        public static final String UNIT_ID = "experienceId";
        public static final String START_EXPERIENCE_TIME = "startExperienceTime";
        public static final String END_EXPERIENCE_TIME = "endExperienceTime";
        public static final String EXPERIENCE_DURATION = "experienceDuration";
        public static final String EXPERIENCE_TYPE = "experienceType";
        public static final String PATIENT = "patientId";
        public static final String SEEN_BY_DOCTOR = "checkedByDoctor";
    }
}
