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

    @Column(unique = true)
    private String productId;

    public PainMedication(){}

    public PainMedication(String medicationName, String lastTakingDateTime){
        this.medicationName = medicationName;
        this.lastTakingDateTime = lastTakingDateTime;
    }

    public PainMedication(String medicationName, String lastTakingDateTime,
    		String patientMedicalNumber,String productId){
    	this.medicationName = medicationName;
    	this.lastTakingDateTime = lastTakingDateTime;
    	this.patientMedicalNumber = patientMedicalNumber;
    	this.productId = productId;
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

        sb.append("Medicine Name: ").append(painMedication.getMedicationName());
        sb.append("\n----------------------------\n");
        sb.append("Patient details")
                .append("\n")
                .append(patient != null ? Patient.getDetailedInfo(patient) : "NA");

        sb.append("\n----------------------------\n");

        return sb.toString();
    }

    public int getNeedSync() {
        return needSync;
    }

    public void setNeedSync(int needSync) {
        this.needSync = needSync;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public static class Builder{
        private String medicationName;
        private String lastTakingDateTime;
        private String productId;
        private String patientMedicalNumber;
        public Builder setMedicationName(String medicationName){
            this.medicationName = medicationName;
            return this;
        }
        public Builder setPatientMedicalNumber(String patientMedicalNumber){
            this.patientMedicalNumber = patientMedicalNumber;
            return this;
        }
        public Builder setProductId(String productId){
            this.productId = productId;
            return this;
        }

        public Builder setLastTakingDateTime(String lastTakingDateTime){
            this.lastTakingDateTime = lastTakingDateTime;
            return this;
        }

        public PainMedication Build(){
            return new PainMedication(this.medicationName,this.lastTakingDateTime,this.patientMedicalNumber,this.productId);
        }
    }

    @Override
    public void buildInternalArray() {

    }

    public static PainMedication getByProductId(String productId) {
        // This is how you execute a query
        return new Select()
                .from(PainMedication.class)
                .where("productId = ?", productId)
                .executeSingle();
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
