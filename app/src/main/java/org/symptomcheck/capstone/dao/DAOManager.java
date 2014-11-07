package org.symptomcheck.capstone.dao;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.IModelBuilder;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.Question;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.provider.ActiveContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igaglioti on 04/11/2014.
 */
public class DAOManager {
    private static DAOManager ourInstance = new DAOManager();

    public static DAOManager get() {
        return ourInstance;
    }

    private DAOManager() {
    }

    public List<Patient> getPatients(){
        return Patient.getAll();
    }

    public synchronized boolean saveUser(UserInfo userInfo){
        new ActiveHandler<UserInfo>().deleteItems(UserInfo.class);
        return userInfo.save() > 0;
    }

    /**
     * Doctor user saving patients
     */
    public synchronized void savePatients(List<Patient> patients, String userIdentification) {
        this.deletePatients(userIdentification);
        (new ActiveHandler<Patient>()).saveItems(patients);
    }

    /**
     * Doctor user saving patients
     */
    public synchronized void deletePatients(String userIdentification) {
        //delete the foreign key objects also
        (new ActiveHandler<Question>()).deleteItems(Question.class);
        (new ActiveHandler<CheckIn>()).deleteItems(CheckIn.class);
        (new ActiveHandler<Patient>()).deleteItems(Patient.class);
    }

    public synchronized void saveCheckIns(List<CheckIn> checkIns, String medicalRecordNumber, String userIdentification) {

        (new ActiveHandler<Question>()).deleteItems(Question.class);
        (new ActiveHandler<CheckIn>()).deleteItems(CheckIn.class);

       Patient patient = null;
        //retrieve Patient for foreign key relation
       List<Patient> patients = new Select().
                from(Patient.class)
                        .where(ActiveContract.PATIENT_COLUMNS.PATIENT_ID + " = ?", medicalRecordNumber)
                .execute();


        if(patients.size() > 0) {
            patient = patients.get(0);
            if(patient != null){
                List<Question> questions = new ArrayList<Question>(checkIns.size());
                for (CheckIn checkIn : checkIns){
                    checkIn.patient = patient;
                    for (Question question : checkIn.getQuestions()){
                        question.checkIn = checkIn;
                        questions.add(question);
                    }
                }
                new ActiveHandler<CheckIn>().saveItems(checkIns);
                new ActiveHandler<Question>().saveItems(questions);
            }
        }
    }

    public UserInfo getUser() {
        return new ActiveHandler<UserInfo>().getItem(UserInfo.class);
    }


    class ActiveHandler<T extends Model> {

        public T getItem(Class<T> objectType){
            return new Select().
                    from(objectType)
                            //.where("CustomerUniqueId = ?", Ancodice)
                    .executeSingle();
        }

          public List<T> getItems(Class<T> objectType){
            return new Select().
                    from(objectType)
                            //.where("CustomerUniqueId = ?", Ancodice)
                    .execute();
        }

        public void deleteItems(Class<T> objectType){
            new Delete().
                    from(objectType)
                    //.where("CustomerUniqueId = ?", Ancodice)
                    .execute();
        }


        public void saveItems(List<T> items){
            ActiveAndroid.beginTransaction();
            try{
                for (T item : items) {
                    if(item instanceof IModelBuilder)
                        ((IModelBuilder)item).buildInternalArray();
                    item.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            }
            finally {
                ActiveAndroid.endTransaction();
            }
        }
    }
}
