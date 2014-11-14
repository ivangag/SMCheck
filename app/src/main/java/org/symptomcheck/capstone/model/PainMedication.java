package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;


@Table(name = "PainMedications", id = BaseColumns._ID)
public class PainMedication extends Model implements IModelBuilder {


    @Column
	private String medicationName;

    @Column
	private String lastTakingDateTime;

    @Column
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

    public static List<PainMedication> getAll(String patientMedicalNumber) {
        // This is how you execute a query
        return new Select()
                .from(PainMedication.class)
                .where("patientMedicalNumber = ?", patientMedicalNumber)
                        //.orderBy("Name ASC")
                .execute();
    }
}
