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
package org.symptomcheck.capstone.dao;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.IModelBuilder;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.PatientExperience;
import org.symptomcheck.capstone.model.Question;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.provider.ActiveContract;

import java.lang.reflect.Type;
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

    public synchronized void wipeAllData(){
        this.deleteUser();
        this.deleteDoctors();
        this.deletePatients();
        this.deleteMedicines();
    }

    private synchronized void deleteUser() {
        new ActiveHandler<UserInfo>().deleteItems(UserInfo.class);
    }

    public synchronized boolean saveUser(UserInfo userInfo){
        this.deleteUser();
        return userInfo.save() > 0;
    }

    /**
     *
     * @param doctors
     * @param userIdentification
     */
    public synchronized void rebuildDoctors(List<Doctor> doctors, String userIdentification) {
        this.deleteDoctors();
        (new ActiveHandler<Doctor>()).saveItems(doctors);
    }

    /**
     * Doctor user saving patients
     */
    public synchronized void rebuildPatients(List<Patient> patients, String userIdentification) {
        this.deletePatients();
        (new ActiveHandler<Patient>()).saveItems(patients);
    }

    /**
     * Doctor user saving patients
     */
    private synchronized void deletePatients() {
        //delete the foreign key objects also
        (new ActiveHandler<Question>()).deleteItems(Question.class);
        (new ActiveHandler<CheckIn>()).deleteItems(CheckIn.class);
        (new ActiveHandler<Patient>()).deleteItems(Patient.class);
    }

    private synchronized void deleteDoctors() {
        //delete the foreign key objects also
        (new ActiveHandler<Doctor>()).deleteItems(Doctor.class);
    }
    public synchronized void deleteCheckIns() {
        //delete the foreign key objects also
        (new ActiveHandler<Question>()).deleteItems(Question.class);
        (new ActiveHandler<CheckIn>()).deleteItems(CheckIn.class);
    }

/*

    public synchronized boolean saveCheckIns(List<CheckIn> checkIns, String medicalRecordNumber,
                                          String userIdentification, boolean needSync) {

        boolean result = false;

        Patient patient = null;
        //retrieve Patient for foreign key relation
        List<Patient> patients = new Select().
                from(Patient.class)
                .where(ActiveContract.PATIENT_COLUMNS.PATIENT + " = ?", medicalRecordNumber)
                .execute();


        if (patients.size() > 0) {
            patient = patients.get(0);
            if (patient != null) {
                List<Question> questions = new ArrayList<Question>(checkIns.size());
                for (CheckIn checkIn : checkIns) {
                    checkIn.patient = patient;
                    checkIn.setNeedSync(needSync ? 1 : 0);
                    for (Question question : checkIn.getQuestions()) {
                        question.checkIn = checkIn;
                        questions.add(question);
                    }
                }
                final long countCheckIns =  (new ActiveHandler<CheckIn>().saveItems(checkIns));
                final long countQuestions = (new ActiveHandler<Question>().saveItems(questions));
                result = needSync ? (countCheckIns > 0) : ((countCheckIns > 0) && (countQuestions > 0));
            }
        }
        return result;
    }
*/

    public synchronized boolean saveCheckIns(List<CheckIn> checkIns, String medicalRecordNumber,
                                          String userIdentification) {

        boolean result = false;

        Patient patient = null;
        //retrieve Patient for foreign key relation
        List<Patient> patients = new Select().
                from(Patient.class)
                .where(ActiveContract.PATIENT_COLUMNS.PATIENT_ID + " = ?", medicalRecordNumber)
                .execute();


        if (patients.size() > 0) {
            patient = patients.get(0);
            if (patient != null) {
                List<Question> questions = new ArrayList<Question>(checkIns.size());
                for (CheckIn checkIn : checkIns) {
                    checkIn.patient = patient;
                    //checkIn.setNeedSync(needSync ? 1 : 0);
                    for (Question question : checkIn.getQuestions()) {
                        question.checkIn = checkIn;
                        questions.add(question);
                    }
                }
                final long countCheckIns =  (new ActiveHandler<CheckIn>().saveItems(checkIns));
                final long countQuestions = (new ActiveHandler<Question>().saveItems(questions));
                //result = needSync ? (countCheckIns > 0) : ((countCheckIns > 0) && (countQuestions > 0));
                result = countCheckIns > 0;
            }
        }
        return result;
    }



    public UserInfo getUser() {
        return new ActiveHandler<UserInfo>().getItem(UserInfo.class);
    }

    public synchronized void deletePatientExperiences() {
        //delete the foreign key objects also
        new ActiveHandler<PatientExperience>().deleteItems(PatientExperience.class);
    }
    public synchronized void deleteMedicines() {
        //delete the foreign key objects also
        new ActiveHandler<PainMedication>().deleteItems(PainMedication.class);
    }

    public synchronized boolean savePainMedications(List<PainMedication> medications,
                                                    String medicalRecordNumber, boolean needSync) {

        for(PainMedication medication : medications){
            medication.setNeedSync(needSync ? 1 : 0);
        }
        return ((new ActiveHandler<PainMedication>().saveItems(medications)) > 0);
    }

    public synchronized boolean savePatientExperiences(List<PatientExperience> patientExperiences) {
        return (new ActiveHandler<PatientExperience>()).saveItems(patientExperiences) > 0;
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



        public long saveItems(List<T> items){
            long count = 0;
            ActiveAndroid.beginTransaction();
            try{
                for (T item : items) {
                    if(item instanceof IModelBuilder)
                        ((IModelBuilder)item).buildInternalArray();
                    count = item.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            }
            finally {
                ActiveAndroid.endTransaction();
            }
            return count;
        }
    }
}
