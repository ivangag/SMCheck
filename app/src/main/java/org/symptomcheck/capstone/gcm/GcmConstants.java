package org.symptomcheck.capstone.gcm;

public class GcmConstants {

	public final static String GCM_MESSAGING_URL = "https://android.googleapis.com/gcm";

    public final static String GCM_EXTRAS_KEY_ACTION = "action";
    public final static String GCM_EXTRAS_KEY_USERNAME = "userName";
    public final static String GCM_EXTRAS_KEY_USERTYPE = "userType";

	// A Patient uploaded a CheckIn
	public final static String GCM_ACTION_CHECKIN_UPDATE = "org.symptomcheck.CHECKIN_UPDATE";
	// A Doctor updated medicines' list
	public final static String GCM_ACTION_MEDICATION_UPDATE = "org.symptomcheck.MEDICATION_UPDATE";
}
