package org.symptomcheck.capstone.provider;

/**
 * Created by igaglioti on 04/11/2014.
 */
public class ActiveContract {
    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "org.symptomcheck.capstone";

    public static class PATIENT_COLUMNS{
        public static final String PATIENT_ID = "patientId";
        public static final String DOCTOR_ID = "doctorId";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
    }
}
