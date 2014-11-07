package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Table(name = "CheckIns", id = BaseColumns._ID)
public class CheckIn extends Model implements IModelBuilder {

    @Column
    private PainLevel issuePainLevel = PainLevel.UNKNOWN;
    @Column
    private FeedStatus issueFeedStatus = FeedStatus.UNKNOWN;
    @Column
    private String issueDateTime;
    @Column
    private String patientMedicalNumber;

    private List<Question> questions = new ArrayList<Question>();

    @Column(name = "Patient")
    public transient Patient patient;

    // This method is optional, does not affect the foreign key creation.
    public List<Question> items() {
        return getMany(Question.class, "CheckIn");
    }

    public CheckIn() {
    }

    public CheckIn(String date, PainLevel painLevel, FeedStatus feedStatus) {
        super();
        this.issueDateTime = date;
        this.issueFeedStatus = feedStatus;
        this.issuePainLevel = painLevel;
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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestions(Question question) {
        this.questions.add(question);
    }


    @Override
    public void buildInternalArray() {

    }

    public static CheckIn createDummyCheckIn(PainLevel painLevel,
                                             FeedStatus feedStatus,
                                             Map<String, String> Medications) {

        Calendar calendar = Calendar.getInstance();
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        CheckIn checkIn = new CheckIn(timestamp.toString(), painLevel, feedStatus);
        for (String medication : Medications.keySet()) {
            Question question = new Question(String.format("Did you Take %s ?", medication), Medications.get(medication),
                    QuestionType.Medication, timestamp.toString());
            checkIn.addQuestions(question);
        }
        return checkIn;
    }
}
