package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Store Patient's data
 *
 * Based on application role
 * - (for PATIENTS_ROLE) store all the patient's doctors Data
 * - (for DOCTORS_ROLE) store only the current doctor Data
 */
@Table(name = "Doctors", id = BaseColumns._ID)
public class Doctor extends Model implements IModelBuilder {

    @Column(name = "doctorId", unique = true)
	private String uniqueDoctorId;

    @Column
	private String firstName;

    @Column
	private String lastName;

    //@Column
	private List<String> gcmRegistrationIds = Lists.newArrayList();
	
	public Doctor(){}
	public Doctor(String uniqueDoctorId, String firstName, String lastName) {
		super();
		this.firstName = firstName;
		this.uniqueDoctorId = uniqueDoctorId;
		this.lastName=  lastName;
	}

	public String getUniqueDoctorId(){
		return this.uniqueDoctorId;
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	
	public String getLastName(){
		return this.lastName;
	}
	
	public void setUniqueDoctorId(String uniqueDoctorId){
		this.uniqueDoctorId = uniqueDoctorId;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	
	public Set<String> getPatients(){
		return this.patients;
	}
	
	public void setPatients(List<String> patients){
		this.patients = new HashSet<String>(patients);
	}

  	private Set<String> patients = new HashSet<String>();

    @Column
    private transient String[] patientsList = new String[]{};
  
	@Override
	public String toString() {

		return "Doctor " + this.firstName + " " + this.lastName + " " +  this.uniqueDoctorId;
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
    public void buildInternalArray() {
        this.patientsList = this.patients.toArray(new String[this.patients.size()]);
    }


    public static List<Doctor> getAll() {
        // This is how you execute a query
        return new Select()
                .from(Doctor.class)
                        //.where("Category = ?", category.getId())
                        //.orderBy("Name ASC")
                .execute();
    }
}