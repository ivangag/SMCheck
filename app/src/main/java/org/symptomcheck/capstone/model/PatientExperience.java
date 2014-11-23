package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.common.collect.Lists;

import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.utils.Costants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by igaglioti on 21/11/2014.
 */
@Table(name = "PatientExperiences", id = BaseColumns._ID)
public class PatientExperience extends Model implements IModelBuilder{

    final static PainLevel painLevelPrimaryToCheck = PainLevel.SEVERE;
    final static PainLevel painLevelSecondaryToCheck = PainLevel.MODERATE;
    final static FeedStatus feedStatusToCheck = FeedStatus.CANNOT_EAT;

    public PatientExperience(){}

    @Column
    private String startExperienceTime;
    @Column
    private String endExperienceTime;
    @Column(unique = true)
    private String experienceId;
    @Column
    private String patientId;
    @Column
    private int experienceDuration;
    @Column
    private int checkedByDoctor = 0;
    @Column
    private ExperienceType experienceType = ExperienceType.UNKNOWN;

    @Override
    public void buildInternalArray() {

    }

    public String getStartExperienceTime() {
        return startExperienceTime;
    }

    public void setStartExperienceTime(String startExperienceTime) {
        this.startExperienceTime = startExperienceTime;
    }

    public String getEndExperienceTime() {
        return endExperienceTime;
    }

    public void setEndExperienceTime(String endExperienceTime) {
        this.endExperienceTime = endExperienceTime;
    }

    public String getExperienceId() {
        return experienceId;
    }

    public void setExperienceId(String experienceId) {
        this.experienceId = experienceId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public int getExperienceDuration() {
        return experienceDuration;
    }

    public void setExperienceDuration(int experienceDuration) {
        this.experienceDuration = experienceDuration;
    }

    public int getCheckedByDoctor() {
        return checkedByDoctor;
    }

    public void setCheckedByDoctor(int checkedByDoctor) {
        this.checkedByDoctor = checkedByDoctor;
    }

    public ExperienceType getExperienceType() {
        return experienceType;
    }

    public void setExperienceType(ExperienceType experienceType) {
        this.experienceType = experienceType;
    }

    public static PatientExperience getByUniqueId(String experienceId) {
        return new Select()
                .from(PatientExperience.class)
                .where("experienceId = ?", experienceId)
                        //.orderBy("Name ASC")
                .executeSingle();
    }


    public static List<PatientExperience> getAllNotSeen() {
        return new Select()
                .from(PatientExperience.class)
                .where("checkedByDoctor = ?", 0)
                .orderBy("experienceDuration DESC")
                .execute();
    }


    public static List<PatientExperience> getByPatient(String patientMedicalNumber) {
        return new Select()
                .from(PatientExperience.class)
                .where("patientId = ?", patientMedicalNumber)
                .orderBy("experienceDuration DESC")
                .execute();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb
                .append("PatientId: ").append(this.patientId)
                .append("\n-------------------------\n")
                .append("Type: ").append(this.experienceType)
                .append("\n-------------------------\n")
                .append("Duration: ").append(this.experienceDuration).append(" hours")
                .append("\n-------------------------\n")
                .append("Start: ").append(DateTimeUtils.convertEpochToHumanTime(startExperienceTime, Costants.TIME.DEFAULT_FORMAT))
                .append("\n-------------------------\n")
                .append("End: ").append(DateTimeUtils.convertEpochToHumanTime(endExperienceTime,Costants.TIME.DEFAULT_FORMAT))
                .append("\n-------------------------\n")
                .append("seenByDoctor? ").append(this.checkedByDoctor == 0 ? "NO" : "YES")
                .append("\n-------------------------\n");
        return sb.toString();
    }


    public static String getDetailedInfo(PatientExperience patientExperience) {
        return patientExperience.toString();
    }
    public static List<PatientExperience> checkBadExperiences() {
        HashMap<Patient, List<CheckIn>> painLevelOnlySevereWarningCheck = new HashMap<Patient, List<CheckIn>>();
        HashMap<Patient, List<CheckIn>> painLevelModerateOrSevereWarningCheck = new HashMap<Patient, List<CheckIn>>();
        HashMap<Patient, List<CheckIn>> feedStatusWarningCheck = new HashMap<Patient, List<CheckIn>>();
        List<CheckIn> painPartialLevelPrimaryWarningList = Lists.newArrayList();
        List<CheckIn> painPartialLevelSecondaryWarningList = Lists.newArrayList();
        List<CheckIn> feedPartialLevelWarningList = Lists.newArrayList();
        boolean checkPainLevelPrimaryWarning;
        boolean checkPainLevelSecondaryWarning;
        boolean checkFeedStatusWarning;
        List<Patient> patients = Patient.getAll();
        PainLevel painLevel;
        FeedStatus feedStatus;
        for (Patient patient : patients) {
            painPartialLevelPrimaryWarningList.clear();
            feedPartialLevelWarningList.clear();
            painPartialLevelSecondaryWarningList.clear();

            final Patient keyPatient = new Patient(patient.getMedicalRecordNumber());
            final List<CheckIn> checkIns = CheckIn.getAllByPatient(patient);
            if (checkIns.size() > 1) {
                for (int idx = 0; idx < checkIns.size(); idx++) {
                    painLevel = checkIns.get(idx).getIssuePainLevel();
                    feedStatus = checkIns.get(idx).getIssueFeedStatus();
                    checkPainLevelPrimaryWarning = painLevel.equals(painLevelPrimaryToCheck);
                    // check only severe pain
                    if(checkPainLevelPrimaryWarning){
                        painPartialLevelPrimaryWarningList.add(checkIns.get(idx));
                    }else{
                        if(painPartialLevelPrimaryWarningList.size() > 1)
                            painLevelOnlySevereWarningCheck.put(keyPatient, Lists.newArrayList(painPartialLevelPrimaryWarningList));
                        painPartialLevelPrimaryWarningList.clear();
                    }
                    //check severe or moderate pain
                    checkPainLevelSecondaryWarning = painLevel.equals(painLevelPrimaryToCheck) || painLevel.equals(painLevelSecondaryToCheck);
                    if(checkPainLevelSecondaryWarning){
                        painPartialLevelSecondaryWarningList.add(checkIns.get(idx));
                    }else{
                        if(painPartialLevelSecondaryWarningList.size() > 1)
                            painLevelModerateOrSevereWarningCheck.put(keyPatient, Lists.newArrayList(painPartialLevelSecondaryWarningList));
                        painPartialLevelSecondaryWarningList.clear();
                    }

                    // check cannot_eat feed status
                    checkFeedStatusWarning = feedStatus.equals(feedStatusToCheck);
                    if(checkFeedStatusWarning){
                        feedPartialLevelWarningList.add(checkIns.get(idx));
                    }else{
                        if(feedPartialLevelWarningList.size() > 1)
                            feedStatusWarningCheck.put(keyPatient, Lists.newArrayList(feedPartialLevelWarningList));
                        feedPartialLevelWarningList.clear();
                    }

                }
                if(painPartialLevelPrimaryWarningList.size() > 1)
                    painLevelOnlySevereWarningCheck.put(keyPatient, Lists.newArrayList(painPartialLevelPrimaryWarningList));
                if(painPartialLevelSecondaryWarningList.size() > 1)
                    painLevelModerateOrSevereWarningCheck.put(keyPatient, Lists.newArrayList(painPartialLevelSecondaryWarningList));
                if(feedPartialLevelWarningList.size() > 1)
                    feedStatusWarningCheck.put(keyPatient, Lists.newArrayList(feedPartialLevelWarningList));
            }
        }
        List<PatientExperience> patientExperiences = Lists.newArrayList();
        for(Patient patient : painLevelOnlySevereWarningCheck.keySet()){
            final int sizeList = painLevelOnlySevereWarningCheck.get(patient).size();
            if( sizeList > 1){
                CheckIn newestCheckIn = painLevelOnlySevereWarningCheck.get(patient).get(0);
                CheckIn oldestCheckIn = painLevelOnlySevereWarningCheck.get(patient).get(sizeList - 1);
                long diffTime = Long.valueOf(newestCheckIn.getIssueDateTime())
                        - Long.valueOf(oldestCheckIn.getIssueDateTime());
                long hourDiff = diffTime / 3600 / 1000;
                if(hourDiff >= 12){
                    // Raise Alert!!!!
                    PatientExperience experience = new PatientExperience();
                    String uniqueId = patient.getMedicalRecordNumber()
                            + "_" + newestCheckIn.getIssueDateTime()
                            //+ "_" + oldestCheckIn.getIssueDateTime()
                            + "_" + ExperienceType.SEVERE.toString();
                    PatientExperience patientExperience = PatientExperience.getByUniqueId(uniqueId);
                    if(patientExperience == null) { // first time this experience is computed
                        experience.setCheckedByDoctor(0);
                        experience.setEndExperienceTime(newestCheckIn.getIssueDateTime());
                        experience.setStartExperienceTime(oldestCheckIn.getIssueDateTime());
                        experience.setExperienceDuration((int) hourDiff);
                        experience.setExperienceType(ExperienceType.SEVERE);
                        experience.setPatientId(patient.getMedicalRecordNumber());
                        experience.setExperienceId(uniqueId);
                        patientExperiences.add(experience);
                    } else { // experience already exists
                        // update only end time if changed
                        if(!patientExperience.endExperienceTime.equals(newestCheckIn.getIssueDateTime())) {
                            (new Update(PatientExperience.class))
                                    .set("endExperienceTime = " + newestCheckIn.getIssueDateTime()
                                            + ","
                                            + "checkedByDoctor = 0")
                                    .where("_id = ?", patientExperience.getId())
                                    .execute();
                        }
                        //PatientExperience.delete(PatientExperience.class,patientExperience.getId());
                    }
                }
            }
        }
        for(Patient patient : painLevelModerateOrSevereWarningCheck.keySet()){
            final int sizeList = painLevelModerateOrSevereWarningCheck.get(patient).size();
            if( sizeList > 1){
                CheckIn newestCheckIn = painLevelModerateOrSevereWarningCheck.get(patient).get(0);
                CheckIn oldestCheckIn = painLevelModerateOrSevereWarningCheck.get(patient).get(sizeList - 1);
                long diffTime = Long.valueOf(newestCheckIn.getIssueDateTime())
                        - Long.valueOf(oldestCheckIn.getIssueDateTime());
                long hourDiff = diffTime / 3600 / 1000;
                if(hourDiff >= 16){
                    // Raise Alert!!!!
                    PatientExperience experience = new PatientExperience();
                    String uniqueId = patient.getMedicalRecordNumber()
                            + "_" + newestCheckIn.getIssueDateTime()
                            //+ "_" + oldestCheckIn.getIssueDateTime()
                            + "_" + ExperienceType.SEVERE_OR_MODERATE.toString();
                    PatientExperience patientExperience = PatientExperience.getByUniqueId(uniqueId);
                    if(patientExperience == null) { // first time this experience is computed
                        experience.setCheckedByDoctor(0);
                        experience.setEndExperienceTime(newestCheckIn.getIssueDateTime());
                        experience.setStartExperienceTime(oldestCheckIn.getIssueDateTime());
                        experience.setExperienceDuration((int) hourDiff);
                        experience.setExperienceType(ExperienceType.SEVERE_OR_MODERATE);
                        experience.setPatientId(patient.getMedicalRecordNumber());
                        experience.setExperienceId(uniqueId);
                        patientExperiences.add(experience);
                    } else { // experience already exists
                        // update only end time if changed
                        if(!patientExperience.endExperienceTime.equals(newestCheckIn.getIssueDateTime())) {
                            (new Update(PatientExperience.class))
                                    .set("endExperienceTime = " + newestCheckIn.getIssueDateTime()
                                            + ","
                                            + "checkedByDoctor = 0")
                                    .where("_id = ?", patientExperience.getId())
                                    .execute();
                        }
                        //PatientExperience.delete(PatientExperience.class,patientExperience.getId());
                    }
                }
            }
        }
        for(Patient patient : feedStatusWarningCheck.keySet()){
            final int sizeList = feedStatusWarningCheck.get(patient).size();
            if( sizeList > 1){
                CheckIn newestCheckIn = feedStatusWarningCheck.get(patient).get(0);
                CheckIn oldestCheckIn = feedStatusWarningCheck.get(patient).get(sizeList - 1);
                long diffTime = Long.valueOf(newestCheckIn.getIssueDateTime())
                        - Long.valueOf(oldestCheckIn.getIssueDateTime());
                long hourDiff = diffTime / 3600 / 1000;
                if(hourDiff >= 12){
                    // Raise Feed Alert!!!!
                    PatientExperience experience = new PatientExperience();
                    String uniqueId = patient.getMedicalRecordNumber()
                            + "_" + newestCheckIn.getIssueDateTime()
                            //+ "_" + oldestCheckIn.getIssueDateTime()
                            + "_" + ExperienceType.CANNOT_EAT.toString();
                    PatientExperience patientExperience = PatientExperience.getByUniqueId(uniqueId);
                    if(patientExperience == null) { // first time this experience is computed
                        experience.setCheckedByDoctor(0);
                        experience.setEndExperienceTime(newestCheckIn.getIssueDateTime());
                        experience.setStartExperienceTime(oldestCheckIn.getIssueDateTime());
                        experience.setExperienceDuration((int) hourDiff);
                        experience.setExperienceType(ExperienceType.CANNOT_EAT);
                        experience.setPatientId(patient.getMedicalRecordNumber());
                        experience.setExperienceId(uniqueId);
                        patientExperiences.add(experience);
                    } else { // experience already exists
                        // update only end time if changed
                        if(!patientExperience.endExperienceTime.equals(newestCheckIn.getIssueDateTime())) {
                            (new Update(PatientExperience.class))
                                    .set("endExperienceTime = " + newestCheckIn.getIssueDateTime()
                                            + ","
                                            + "checkedByDoctor = 0")
                                    .where("_id = ?", patientExperience.getId())
                                    .execute();
                        }
                        //PatientExperience.delete(PatientExperience.class,patientExperience.getId());
                    }
                }
            }
        }
        return patientExperiences;
    }

}
