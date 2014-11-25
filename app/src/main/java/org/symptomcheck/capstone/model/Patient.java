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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

import hirondelle.date4j.DateTime;

/**
 * Store Patient's data
 *
 * Based on application role
 * - (for PATIENTS_ROLE) store only the current patient Data
 * - (for DOCTORS_ROLE) store all the doctor's patients Data
 */
@Table(name = "Patients", id = BaseColumns._ID)
public class Patient extends Model implements IModelBuilder{


    @Column(name = "patientId", unique = true)
	private String medicalRecordNumber;
	
	@Column
	private String firstName;
	
	@Column
	private String lastName;

	private List<String> gcmRegistrationIds = Lists.newArrayList();
	
	@Column
	private String birthDate;

    @Column
    private String email;

    @Column
    private String phoneNumber;

    @Column
    private String[] doctorsList = new String[]{};
    private boolean birthDateClear;

    public Patient() {
	}
	
	public Patient(String medicalRecordNumber, String firstName,
			String lastName) {
		super();
		this.medicalRecordNumber = medicalRecordNumber;
		this.lastName = lastName;
		this.firstName = firstName;
	}

    public Patient(String medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }

    public String getMedicalRecordNumber(){
		return this.medicalRecordNumber;
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	
	public String getLastName(){
		return this.lastName;
	}
	
	public void setMedicalRecordNumber(String medicalRecordNumber){
		this.medicalRecordNumber = medicalRecordNumber;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public Set<String> getDoctors(){
		return this.doctors;
	}
	
	public void setDoctors(List<String> doctors){
		this.doctors = new HashSet<String>(doctors);
	}

	public void addDoctor(String doctor) {	  
		if(!this.doctors.contains(doctor))
			this.doctors.add(doctor);
    }

  	private Set<String> doctors = new HashSet<String>();
  	
	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	
	public void addGcmRegistrationId(String gcmRegistrationId) {
		if(!this.gcmRegistrationIds.contains(gcmRegistrationId))
			this.gcmRegistrationIds.add(gcmRegistrationId);
	}
	

	public List<String> getGcmRegistrationIds() {
		return gcmRegistrationIds;
	}

	public void setGcmRegistrationIds(List<String> gcmRegistrationIds) {
		this.gcmRegistrationIds = gcmRegistrationIds;
	}	
   
	
	@Override
	public String toString() {
		return "Name: " + this.firstName + " "+ this.lastName +
                " - Medical Number; " + this.medicalRecordNumber;
	}

	/**
	 * Two Object will generate the same hashcode if they have exactly the same
	 * values for their properties
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(medicalRecordNumber, this.firstName, this.lastName);
	}

    // This method is optional, does not affect the foreign key creation.
    public List<CheckIn> getItemsCheckIns() {
        return getMany(CheckIn.class, "Patient");
    }

	/**
	 * Two Object are considered equal if they have exactly the same values for
	 * their properties
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Patient) {
			Patient other = (Patient) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(medicalRecordNumber, other.medicalRecordNumber)
					&& Objects.equal(firstName, other.firstName)
					&& lastName == other.lastName;
		} else {
			return false;
		}
	}
    @Override
    public void buildInternalArray() {
        this.doctorsList = this.doctors.toArray(new String[this.doctors.size()]);
    }

    public static List<Patient> getAll() {
        // This is how you execute a query
        return new Select()
                .from(Patient.class)
                        //.where("Category = ?", category.getId())
                        //.orderBy("Name ASC")
                .execute();
    }

    public static Patient getByMedicalNumber(String medicalRecordNumber) {
        // This is how you execute a query
        return new Select()
                .from(Patient.class)
                        .where("patientId = ?", medicalRecordNumber)
                        //.orderBy("Name ASC")
                .executeSingle();
    }

    public static Patient getById(Long id) {
        // This is how you execute a query
        return new Select()
                .from(Patient.class)
                .where("_id = ?", id)
                        //.orderBy("Name ASC")
                .executeSingle();
    }

    public static String getDetailedInfo(Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(patient.getFirstName())
                .append("\n----------------------------\n")
                .append("LastName: ").append(patient.getLastName())
                .append("\n----------------------------\n")
                .append("MedicalNumber: ").append(patient.getMedicalRecordNumber())
                .append("\n----------------------------\n")
                .append("BirthDate: ").append(DateTimeUtils.convertEpochToHumanTime(patient.getBirthDate(), "DD/MM/YYYY"))
                .append("\n----------------------------\n")
                .append("Email: ").append(patient.getEmail())
                .append("\n----------------------------\n")
                .append("PhoneNumber: ").append(patient.getPhoneNumber())
                .append("\n----------------------------\n");
        return sb.toString();
    }

    public String getBirthDateClear() {
        final String savedTime = this.getBirthDate();
        String birthDateTime;
        if(savedTime != null &&
                !savedTime.isEmpty()){
            birthDateTime = DateTime.forInstant(Long.valueOf(savedTime),
                    TimeZone.getDefault()).format("YYYY-MM-DD hh:mm");
        }
        else{
            birthDateTime = Constants.STRINGS.EMPTY;
        }
        return birthDateTime;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
