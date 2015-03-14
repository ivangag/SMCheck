package org.symptomcheck.capstone.utils;

import android.graphics.Color;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by igaglioti on 27/01/2015.
 */
public class CheckInUtils {

    //public static int COLOR_CHART_GREEN = Color.


    private static CheckInUtils ourInstance = new CheckInUtils();
    public static CheckInUtils getInstance() {
        return ourInstance;
    }
    public void ResetValuesToDefault(){
        IsGeneralMedicinesQuestionChecked = false;
        ReportMedicationsResponse = new HashMap<String, String>(){};
        ReportMedicationsTakingTime = new HashMap<String, String>(){};
        ReportPainLevel = PainLevel.UNKNOWN;
        ReportFeedStatus = FeedStatus.UNKNOWN;
    }

    private CheckInUtils() {
    }

    public boolean IsGeneralMedicinesQuestionChecked;
    public Map<String,String> ReportMedicationsResponse = new HashMap<String, String>(){};
    public Map<String,String> ReportMedicationsTakingTime = new HashMap<String, String>(){};
    public PainLevel ReportPainLevel = PainLevel.UNKNOWN;
    public FeedStatus ReportFeedStatus = FeedStatus.UNKNOWN;


/*    public static final int[] SM_CHECKIN_COLORS = {
            Color.rgb(0, 200, 83)*//*green_700*//*, Color.rgb(255, 171, 0)*//*amber_700*//*,Color.rgb(213, 0, 0)*//*red_700*//*
    }; */
    public static final int[] SM_CHECKIN_COLORS = {
            Color.rgb(0, 200, 83)/*green_700*/, Color.rgb(255, 171, 0)/*amber_700*/,Color.rgb(213, 0, 0)/*red_700*/
    };
}
