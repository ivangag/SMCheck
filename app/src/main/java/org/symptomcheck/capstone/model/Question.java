package org.symptomcheck.capstone.model;


import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

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
}

