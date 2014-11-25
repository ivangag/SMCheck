/*
 * ******************************************************************************
 *   Copyright (c) 2014-2015 Ivan Gaglioti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */
package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.symptomcheck.capstone.utils.Costants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import hirondelle.date4j.DateTime;

@Table(name = "CheckInsOnline", id = BaseColumns._ID)
public class CheckInOnlineWrapper extends Model implements IModelBuilder {

    @Column
    private PainLevel issuePainLevel = PainLevel.UNKNOWN;
    @Column
    private FeedStatus issueFeedStatus = FeedStatus.UNKNOWN;
    @Column
    private String issueDateTime;
    @Column
    private String patientMedicalNumber;

    @Column(unique = true)
    private String unitId;

    @Column
    private String imageUrl;

    private List<QuestionOnlineWrapper> questions = new ArrayList<QuestionOnlineWrapper>();

    // This method is optional, does not affect the foreign key creation.
    public List<QuestionOnlineWrapper> getItemsQuestion() {
        return getMany(QuestionOnlineWrapper.class, "CheckInOnline");
    }

    public CheckInOnlineWrapper() {
    }

    public CheckInOnlineWrapper(String date, PainLevel painLevel, FeedStatus feedStatus) {
        super();
        this.issueDateTime = date;
        this.issueFeedStatus = feedStatus;
        this.issuePainLevel = painLevel;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public PainLevel getIssuePainLevel() {
        return issuePainLevel;
    }

    public void setIssuePainLevel(PainLevel issuePainLevel) {
        this.issuePainLevel = issuePainLevel;
    }

    public FeedStatus getIssueFeedStatus() {
        return issueFeedStatus;
    }

    public void setIssueFeedStatus(FeedStatus issueFeedStatus) {
        this.issueFeedStatus = issueFeedStatus;
    }

    public String getIssueDateTime() {
        return issueDateTime;
    }

    public void setIssueDateTime(String issueDateTime) {
        this.issueDateTime = issueDateTime;
    }

    public String getPatientMedicalNumber() {
        return patientMedicalNumber;
    }

    public void setPatientMedicalNumber(String patientMedicalNumber) {
        this.patientMedicalNumber = patientMedicalNumber;
    }

    public List<QuestionOnlineWrapper> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionOnlineWrapper> questions) {
        this.questions = questions;
    }

    public void addQuestions(QuestionOnlineWrapper question) {
        this.questions.add(question);
    }


    @Override
    public void buildInternalArray() {

    }

    public static CheckInOnlineWrapper createCheckIn(PainLevel painLevel,
                                        FeedStatus feedStatus,
                                        Map<PainMedication,String> Medications) {

        //final Calendar calendar = Calendar.getInstance();
        Long timestamp = DateTime.now(TimeZone.getTimeZone(Costants.TIME.GMT00)).getMilliseconds(TimeZone.getTimeZone(Costants.TIME.GMT00));
        //Long timestamp = calendar.getTimeInMillis();
        CheckInOnlineWrapper checkIn = new CheckInOnlineWrapper(timestamp.toString(), painLevel, feedStatus);
        checkIn.setUnitId(UUID.randomUUID().toString());
        for (PainMedication medication : Medications.keySet()) {
            QuestionOnlineWrapper question = new QuestionOnlineWrapper(String.format("Did you Take %s ?",
                    medication.getMedicationName()),
                    Medications.get(medication),
                    QuestionType.Medication, medication.getLastTakingDateTime());
            checkIn.addQuestions(question);
        }
        return checkIn;
    }


    public static String getDetailedInfo(CheckInOnlineWrapper checkInOnline){
        StringBuilder sb = new StringBuilder();
        sb.append("Pain Level: ").append(checkInOnline.getIssuePainLevel())
                .append("\n----------------------------\n")
                .append("Feed Status: ").append(checkInOnline.getIssueFeedStatus())
        .append("\n----------------------------\n");

        //List<QuestionOnlineWrapper> questions = useStoredQuestions ? checkIn.getItemsQuestion() : checkIn.getQuestions();
        List<QuestionOnlineWrapper> questions = checkInOnline.getItemsQuestion();
        for (QuestionOnlineWrapper question : questions) {
            final String time = question.getMedicationTime();
            sb.append(time)
                    .append(time.equals(Costants.STRINGS.EMPTY) ? Costants.STRINGS.EMPTY : "\n")
                    .append(question.getQuestion()).append(" ").append(question.getResponse())
                    .append("\n----------------------------\n");

        }

        return sb.toString();
    }


    public static CheckInOnlineWrapper getByUnitId(String unitId) {
        // This is how you execute a query
        return new Select()
                .from(CheckInOnlineWrapper.class)
                        .where("unitId = ?", unitId)
                        //.orderBy("Name ASC")
                .executeSingle();
    }

    public static List<CheckInOnlineWrapper> getAll() {
        // This is how you execute a query
        return new Select()
                .from(CheckInOnlineWrapper.class)
                        //.where("Category = ?", category.getId())
                        //.orderBy("Name ASC")
                .execute();
    }

    public static List<CheckInOnlineWrapper> getAllToSync() {
        // This is how you execute a query
        return new Select()
                .from(CheckInOnlineWrapper.class)
                        .where("needSync = ?", 1)
                        //.orderBy("Name ASC")
                .execute();
    }
    public String getIssueDateTimeClear() {
        final String savedTime = this.getIssueDateTime();
        String medicationTime;
        if(savedTime != null &&
                !savedTime.isEmpty()){
            medicationTime = DateTime.forInstant(Long.valueOf(savedTime),
                    TimeZone.getDefault()).format("YYYY-MM-DD hh:mm");
        }
        else{
            medicationTime = Costants.STRINGS.EMPTY;
        }
        return medicationTime;
    }

    public static List<CheckInOnlineWrapper> getAllByPatient(Patient patient) {
        // This is how you execute a query
        return new Select()
                .from(CheckInOnlineWrapper.class)
                        .where("Patient = ?", patient.getId())
                        .orderBy("issueDateTime DESC")
                .execute();
    }

    public static int getCountAllFromDate(String time){
        return new Select()
                .from(CheckInOnlineWrapper.class)
                .where("issueDateTime >= ?", time)
                .execute().size();
    }

    public static int getCountInThisDay(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long millisecondsFromMidNight = calendar.getTimeInMillis();
        return getCountAllFromDate(String.valueOf(millisecondsFromMidNight));
    }
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }
}
