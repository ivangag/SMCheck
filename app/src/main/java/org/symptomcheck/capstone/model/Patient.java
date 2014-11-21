package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

import org.symptomcheck.capstone.utils.Costants;

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
		return "Name: " + this.firstName + " "+ this.lastName + " - Medical Number; " + this.medicalRecordNumber;
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
        sb.append("Name: ").append(patient.getFirstName());
        sb.append("\n----------------------------\n");
        sb.append("LastName: ").append(patient.getLastName());
        sb.append("\n----------------------------\n");
        sb.append("MedicalNumber: ").append(patient.getMedicalRecordNumber());
        sb.append("\n----------------------------\n");
        sb.append("BirthDate: ").append(patient.getBirthDateClear());
        sb.append("\n----------------------------\n");

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
            birthDateTime = Costants.STRINGS.EMPTY;
        }
        return birthDateTime;
    }


    public static void checkExperienceStatus() {
        HashMap<String, List<CheckIn>> painLevelWarningCheck = new HashMap<String, List<CheckIn>>();
        HashMap<String, List<CheckIn>> feedStatusWarningCheck = new HashMap<String, List<CheckIn>>();
        List<CheckIn> painPartialLevelWarningList = Lists.newArrayList();
        List<CheckIn> feedPartialLevelWarningList = Lists.newArrayList();
        PainLevel painLevelToCheck = PainLevel.WELL_CONTROLLED;
        FeedStatus feedStatusToCheck = FeedStatus.CANNOT_EAT;
        boolean checkPainLevelWarning;
        boolean checkFeedStatusWarning;
        List<Patient> patients = getAll();
        PainLevel painLevel1,painLevel2;
        FeedStatus feedStatus1,feedStatus2;
        for (Patient patient : patients) {
            List<CheckIn> checkIns = CheckIn.getAllByPatient(patient);
            painPartialLevelWarningList.clear();
            feedPartialLevelWarningList.clear();
            if (checkIns.size() > 1) {
                for (int idx = 0; idx < checkIns.size(); idx++) {
                    painLevel1 = checkIns.get(idx).getIssuePainLevel();
                    feedStatus1 = checkIns.get(idx).getIssueFeedStatus();
                    if (idx < checkIns.size() - 1) {
                        painLevel2 = checkIns.get(idx + 1).getIssuePainLevel();
                        feedStatus2 = checkIns.get(idx + 1).getIssueFeedStatus();
                    } else {
                        painLevel2 = checkIns.get(idx - 1).getIssuePainLevel();
                        feedStatus2 = checkIns.get(idx - 1).getIssueFeedStatus();
                    }
                    checkPainLevelWarning = painLevel1.equals(painLevelToCheck) && painLevel1.equals(painLevel2);
                    if (checkPainLevelWarning) {
                        painPartialLevelWarningList.add(checkIns.get(idx));
                    }
                    checkFeedStatusWarning = feedStatus1.equals(feedStatusToCheck) && feedStatus1.equals(feedStatus2);
                    if (checkFeedStatusWarning) {
                        feedPartialLevelWarningList.add(checkIns.get(idx));
                    }

                }
                painLevelWarningCheck.put(patient.getMedicalRecordNumber(), Lists.newArrayList(painPartialLevelWarningList));
                feedStatusWarningCheck.put(patient.getMedicalRecordNumber(), Lists.newArrayList(feedPartialLevelWarningList));
            }
        }
        for(String patient : painLevelWarningCheck.keySet()){
            final int sizeList = painLevelWarningCheck.get(patient).size();
            if( sizeList > 0){
                CheckIn newestCheckIn = painLevelWarningCheck.get(patient).get(0);
                CheckIn oldestCheckIn = painLevelWarningCheck.get(patient).get(sizeList - 1);
                long diffTime = Long.valueOf(newestCheckIn.getIssueDateTime())
                        - Long.valueOf(oldestCheckIn.getIssueDateTime());
                long hourDiff = diffTime / 3600 / 1000;
                if(hourDiff >= 12){
                    // Raise Alert!!!!
                }
            }
        }
        for(String patient : feedStatusWarningCheck.keySet()){
            final int sizeList = feedStatusWarningCheck.get(patient).size();
            if( sizeList > 0){
                CheckIn newestCheckIn = feedStatusWarningCheck.get(patient).get(0);
                CheckIn oldestCheckIn = feedStatusWarningCheck.get(patient).get(sizeList - 1);
                long diffTime = Long.valueOf(newestCheckIn.getIssueDateTime())
                        - Long.valueOf(oldestCheckIn.getIssueDateTime());
                long hourDiff = diffTime / 3600 / 1000;
                if(hourDiff >= 12){
                    // Raise Feed Alert!!!!
                }
            }
        }
    }
}
