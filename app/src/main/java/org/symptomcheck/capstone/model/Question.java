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

import org.symptomcheck.capstone.utils.Constants;

import java.util.List;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

//TODO#FDAR_3 Questions Response associated to a Chec-In unit data
@Table(name = "Questions", id = BaseColumns._ID)
public class Question extends Model implements IModelBuilder{
	
	@Column
	private String question;
	@Column
	private String response;

    @Column
	private String medicatationTakingTime;

    @Column
	private QuestionType questionType;

    @Column(name = "CheckIn")
    public transient CheckIn checkIn;


    public Question(){}
	public Question(String question, String response, QuestionType questionType, String medicatationTakingTime){
		this.question = question;
		this.response = response;
		this.questionType = questionType;
		this.medicatationTakingTime = medicatationTakingTime;
	}



    public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getMedicatationTakingTime() {
		return medicatationTakingTime;
	}
	public void setMedicatationTakingTime(String medicatationTakingTime) {
		this.medicatationTakingTime = medicatationTakingTime;
	}
	public QuestionType getQuestionType() {
		return questionType;
	}
	public void setQuestionType(QuestionType questionType) {
		this.questionType = questionType;
	}


    @Override
    public void buildInternalArray() {

    }

    /**
     *
     * @return all stored Question
     */
    public static List<Question> getAll() {
        // This is how you execute a query
        return new Select()
                .from(Question.class)
                        //.where("Category = ?", category.getId())
                        //.orderBy("Name ASC")
                .execute();
    }

    /**
     *
     * @param checkIn CheckIn owner of Question(s)
     * @return all stored Question owned by Checkin
     */
    public static List<Question> getAll(CheckIn checkIn) {
        // This is how you execute a query
        return new Select()
                .from(Question.class)
                        .where("CheckIn = ?", checkIn.getId())
                        //.orderBy("Name ASC")
                .execute();
    }

    public String getMedicationTime() {
        final String savedTime = this.getMedicatationTakingTime();
        String medicationTime;
        if(savedTime != null &&
                !savedTime.isEmpty()){
            medicationTime = DateTime.forInstant(Long.valueOf(savedTime), TimeZone.getTimeZone(Constants.TIME.GMT00)).format("YYYY-MM-DD hh:mm");
        }
        else{
            medicationTime = Constants.STRINGS.EMPTY;
        }
        return medicationTime;
    }
}

