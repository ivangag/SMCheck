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
package org.symptomcheck.capstone.network;


import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.UserInfo;

import java.util.Collection;
import java.util.concurrent.Callable;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.HEAD;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

//TODO#BPR_4 Api Interface implemented by Retrofit and used to communicate via Http with remote Java Server Spring-based
//TODO#BPR_5 All Api use HTTP protocol and perform requests to a REST URL path
public interface SymptomManagerSvcApi {

    public static final String DOCTOR_SEARCH_BY_PATIENT_PATH = SymptomManagerSvcApi.DOCTOR_SVC_PATH + "/search/findByPatient";

    public static final String PATIENT_SVC_PATH = "/patient";

    public static final String DOCTOR_SVC_PATH = "/doctor";

    public static final String TOKEN_PATH = "/oauth/token";
    public static final String CHECKIN_SVC_PATH = "/checkin";

    public static final String GCM_SVC_PATH = "/userinfo/gcm";

    public static final String GCM_DELETE_PATH = GCM_SVC_PATH + "/clear";


    //----------------- ADMIN methods ----------------- //
    @POST(DOCTOR_SVC_PATH + "/{uniqueDoctorID}/patients")
    public Doctor addPatientToDoctor(@Path("uniqueDoctorID") String uniqueDoctorID, @Body Patient patient);

    @POST(PATIENT_SVC_PATH)
    public Patient addPatient(@Body Patient patient);

    @POST(DOCTOR_SVC_PATH)
    public Doctor addDoctor(@Body Doctor d);

    @GET(value="/userinfo")
    public UserInfo verifyUser();

    @GET(value="/userinfo")
    public void verifyUser(Callback<UserInfo> userInfoCallback);


    @GET(DOCTOR_SVC_PATH)
    public Collection<Doctor> getDoctorList();

    @GET(PATIENT_SVC_PATH)
    public Collection<Patient> getPatientList();



    //----------------- PATIENT methods ----------------- //
    @GET(PATIENT_SVC_PATH + "/{medicalRecordNumber}")
    public Patient findPatientByMedicalRecordNumber(@Path("medicalRecordNumber") String medicalCardNumber);

    @GET(PATIENT_SVC_PATH + "/{medicalRecordNumber}/doctors/search")
    public Collection<Doctor> findDoctorsByPatient(@Path("medicalRecordNumber") String medicalCardNumber);

    @POST(PATIENT_SVC_PATH + "/{medicalRecordNumber}/checkins")
    public CheckIn addCheckIn(@Path("medicalRecordNumber") String medicalCardNumber, @Body CheckIn checkIn);


    @GET(PATIENT_SVC_PATH + "/{medicalRecordNumber}/medications/search")
    public Collection<PainMedication> findPainMedicationsByPatient(
            @Path("medicalRecordNumber") String medicalCardNumber);


    //----------------- DOCTOR methods ----------------- //

    @POST(PATIENT_SVC_PATH + "/{medicalRecordNumber}/medications")
    public PainMedication addPainMedication( //TODO#FDAR_12 Used to add a new Patient Medication
            @Path("medicalRecordNumber") String medicalCardNumber,
            @Body PainMedication painMedication);

    @DELETE(PATIENT_SVC_PATH + "/{medicalRecordNumber}/medications/delete/")
    public boolean deletePainMedication( //TODO#FDAR_12 Used to delete Patient Medication
            @Path("medicalRecordNumber") String medicalCardNumber,
            @Query("medicineProductId") String medicineProductId);

    @GET(DOCTOR_SVC_PATH + "/{uniqueDoctorID}/patients/checkins/searchByPatientName")
    public Collection<CheckIn> findCheckInsByPatientName( //TODO#FDAR_11
            @Path("uniqueDoctorID") String uniqueDoctorID,
            @Query("firstName") String patientFirstName,
            @Query("lastName") String patientLastName);

    @GET(DOCTOR_SVC_PATH + "/{uniqueDoctorID}/patients/checkins/searchByPatientName")
    public void findCheckInsByPatientName( //TODO#FDAR_11 Async method
            @Path("uniqueDoctorID") String uniqueDoctorID,
            @Query("firstName") String patientFirstName,
            @Query("lastName") String patientLastName, Callback<Collection<CheckIn>> result);

    @GET(DOCTOR_SVC_PATH + "/{uniqueDoctorID}")
    public Doctor findDoctorByUniqueDoctorID(@Path("uniqueDoctorID") String uniqueDoctorID);

    @GET(DOCTOR_SVC_PATH + "/{uniqueDoctorID}/patients/search")
    public Collection<Patient> findPatientsByDoctor(@Path("uniqueDoctorID") String uniqueDoctorID);


    @GET(PATIENT_SVC_PATH + "/{medicalRecordNumber}/checkins/search")
    public Collection<CheckIn> findCheckInsByPatient(@Path("medicalRecordNumber") String medicalCardNumber);


    //----------------- GCM methods ----------------- //
    @POST(GCM_SVC_PATH)
    public boolean sendGCMRegistrationId(@Body String gcmRegistrationId);

    @DELETE(GCM_DELETE_PATH)
    public boolean clearGCMRegistration(@Body String gcmRegistrationId);
}