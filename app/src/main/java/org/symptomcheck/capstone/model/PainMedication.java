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

    @Column
    private transient int needSync = 1;

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

    public static PainMedication getById(int id) {
        // This is how you execute a query
        return new Select()
                .from(PainMedication.class)
                .where("_id = ?", id)
                        //.orderBy("Name ASC")
                .executeSingle();
    }

    public static String getDetailedInfo(PainMedication painMedication) {
        final Patient patient = Patient.getByMedicalNumber(painMedication.getPatientMedicalNumber());
        StringBuilder sb = new StringBuilder();

        sb.append("Name: ").append(painMedication.getMedicationName());
        sb.append("\n----------------------------\n");
        sb.append("Patient")
                .append("\n")
                .append(patient != null ? patient.toString() : "NA");

        sb.append("\n----------------------------\n");

        return sb.toString();
    }

    public int getNeedSync() {
        return needSync;
    }

    public void setNeedSync(int needSync) {
        this.needSync = needSync;
    }

    public static class Builder{
        private String medicationName;
        private String lastTakingDateTime;
        private String patientMedicalNumber;
        public Builder setMedicationName(String medicationName){
            this.medicationName = medicationName;
            return this;
        }
        public Builder setPatientMedicalNumber(String patientMedicalNumber){
            this.patientMedicalNumber = patientMedicalNumber;
            return this;
        }

        public Builder setLastTakingDateTime(String lastTakingDateTime){
            this.lastTakingDateTime = lastTakingDateTime;
            return this;
        }

        public PainMedication Build(){
            return new PainMedication(this.medicationName,this.lastTakingDateTime,this.patientMedicalNumber);
        }
    }

    @Override
    public void buildInternalArray() {

    }

    public static List<PainMedication> getAllToSync() {
        // This is how you execute a query
        return new Select()
                .from(PainMedication.class)
                .where("needSync = ?", 1)
                .execute();
    }

    public static List<PainMedication> getAll(String patientMedicalNumber) {
        // This is how you execute a query
        return new Select()
                .from(PainMedication.class)
                .where("patientMedicalNumber = ?", patientMedicalNumber)
                .execute();
    }
}
