package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;


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
    private String[] doctorsList = new String[]{};
    public Patient() {
	}
	
	public Patient(String medicalRecordNumber, String firstName,
			String lastName) {
		super();
		this.medicalRecordNumber = medicalRecordNumber;
		this.lastName = lastName;
		this.firstName = firstName;
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
		return "Patient " + this.firstName + " "+ this.lastName + " " + this.medicalRecordNumber;
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

    public static List<Patient> getAll() {
        // This is how you execute a query
        return new Select()
                .from(Patient.class)
                        //.where("Category = ?", category.getId())
                        //.orderBy("Name ASC")
                .execute();
    }

    @Override
    public void buildInternalArray() {
        this.doctorsList = this.doctors.toArray(new String[this.doctors.size()]);
    }
}
