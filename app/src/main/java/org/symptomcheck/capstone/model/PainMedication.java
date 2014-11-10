package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;


@Table(name = "PainMedications", id = BaseColumns._ID)
public class PainMedication extends Model implements IModelBuilder {


	private String medicationName;

	private String lastTakingDateTime;

    private String patientMedicalNumber;
    
    public PainMedication(){}

    public PainMedication(String medicationName, String lastTakingDateTime){
        this.medicationName = medicationName;
        this.lastTakingDateTime = lastTakingDateTime;
    }

    public PainMedication(String medicationName, String lastTakingDateTime,
    		String patientMedicalNumber){
    	this.medicationName = medicationName;
    	this.lastTakingDateTime = lastTakingDateTime;
    	this.patientMedicalNumber = patientMedicalNumber;
    }
    
	public PainMedication(String medicationName) {
		this.medicationName = medicationName;
	}

	public String getMedicationName() {
		return medicationName;
	}
	public void setMedicationName(String medicationName) {
		this.medicationName = medicationName;
	}
	public String getLastTakingDateTime() {
		return lastTakingDateTime;
	}
	public void setLastTakingDateTime(String lastTakingDateTime) {
		this.lastTakingDateTime = lastTakingDateTime;
	}
	public String getPatientMedicalNumber() {
		return patientMedicalNumber;
	}
	public void setPatientMedicalNumber(String patientMedicalNumber) {
		this.patientMedicalNumber = patientMedicalNumber;
	}


    @Override
    public void buildInternalArray() {

    }
}
