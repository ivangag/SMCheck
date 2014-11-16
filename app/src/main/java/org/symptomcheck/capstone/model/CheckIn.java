package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

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
    @Column(name = "Patient")
    public transient Patient patient;
    @Column
    public transient int needSync = 1;
    /*
        //@Column
        //private String throatImageEncoded;


        public String getThroatImageEncoded() {
            return throatImageEncoded;
        }
        public void setThroatImageEncoded(String throatImageEncoded) {
            this.throatImageEncoded = throatImageEncoded;
        }
        */
    private List<Question> questions = new ArrayList<Question>();

    // This method is optional, does not affect the foreign key creation.
    public List<Question> getItemsQuestion() {
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

    public static CheckIn createCheckIn(PainLevel painLevel,
                                        FeedStatus feedStatus,
                                        Map<PainMedication,String> Medications) {

        final Calendar calendar = Calendar.getInstance();
        Long timestamp = calendar.getTimeInMillis();
        CheckIn checkIn = new CheckIn(timestamp.toString(), painLevel, feedStatus);
        for (PainMedication medication : Medications.keySet()) {
            Question question = new Question(String.format("Did you Take %s ?",
                    medication.getMedicationName()),
                    Medications.get(medication),
                    QuestionType.Medication, medication.getLastTakingDateTime());
            checkIn.addQuestions(question);
        }
        return checkIn;
    }


    public static CheckIn getById(int id) {
        // This is how you execute a query
        return new Select()
                .from(CheckIn.class)
                        .where("_id = ?", id)
                        //.orderBy("Name ASC")
                .executeSingle();
    }

    public static List<CheckIn> getAll() {
        // This is how you execute a query
        return new Select()
                .from(CheckIn.class)
                        //.where("Category = ?", category.getId())
                        //.orderBy("Name ASC")
                .execute();
    }

    public static List<CheckIn> getAllToSync() {
        // This is how you execute a query
        return new Select()
                .from(CheckIn.class)
                        .where("needSync = ?", 1)
                        //.orderBy("Name ASC")
                .execute();
    }

}
