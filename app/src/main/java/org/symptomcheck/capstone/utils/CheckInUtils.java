package org.symptomcheck.capstone.utils;

import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by igaglioti on 27/01/2015.
 */
public class CheckInUtils {
    private static CheckInUtils ourInstance = new CheckInUtils();


    public static CheckInUtils getInstance() {
        return ourInstance;
    }

    private CheckInUtils() {
    }

    public boolean IsGeneralMedicinesQuestionChecked;
    public Map<String,String> ReportMedicationsResponse = new HashMap<String, String>(){};
    public Map<String,String> ReportMedicationsTakingTime = new HashMap<String, String>(){};
    public PainLevel ReportPainLevel = PainLevel.UNKNOWN;
    public FeedStatus ReportFeedStatus = FeedStatus.UNKNOWN;
}
