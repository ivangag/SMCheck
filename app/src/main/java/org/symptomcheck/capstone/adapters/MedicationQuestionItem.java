package org.symptomcheck.capstone.adapters;

import com.google.common.collect.Lists;

import org.symptomcheck.capstone.model.Question;

import java.util.List;

/**
 * Created by igaglioti on 16/02/2015.
 */
public class MedicationQuestionItem {
    private String medicationName;
    private String medicationTakingTime;
    private boolean isTaken;

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getMedicationTakingTime() {
        return medicationTakingTime;
    }

    public void setMedicationTakingTime(String medicationTakingTime) {
        this.medicationTakingTime = medicationTakingTime;
    }

    public boolean IsTaken() {
        return isTaken;
    }

    public void setIsTaken(boolean IsTaken) {
        this.isTaken = IsTaken;
    }


    public static List<MedicationQuestionItem> makeItemByCheckinQuestions(List<Question> CheckInQuestions){
        List<MedicationQuestionItem> questionItems = Lists.newArrayList();
        for(Question question:CheckInQuestions){
            final String resp = question.getResponse();
            MedicationQuestionItem item = new MedicationQuestionItem();
            item.setIsTaken(resp.equals("YES"));
            item.setMedicationTakingTime(question.getMedicationTime());
            item.setMedicationName(question.getQuestion());
            questionItems.add(item);
        }        
        return  questionItems;
    }

}