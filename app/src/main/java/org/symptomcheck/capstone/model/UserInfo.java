package org.symptomcheck.capstone.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Users", id = BaseColumns._ID)
public class UserInfo extends Model implements IModelBuilder {

    @Column
	private UserType userType = UserType.UNKNOWN;

    @Column
	private boolean isLogged;

    @Column
	private boolean isAnagPresent;

    @Column
	private String userIdentification;

    @Column
	private String firstName;

    @Column
	private String lastName;

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}
	
	public boolean isLogged() {
		return isLogged;
	}

	public void setLogged(boolean isLogged) {
		this.isLogged = isLogged;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new  StringBuilder();
		return sb.append(this.firstName).append("-")
				.append(this.lastName).append("-")
				.append(this.getUserIdentification()).append("-")
                .append(this.getAnagPresent()).append("-")
				.append(this.isLogged)
				.toString();
	}

	public String getUserIdentification() {
		return userIdentification;
	}

	public void setUserIdentification(String userIdentification) {
		this.userIdentification = userIdentification;
	}

	public boolean getAnagPresent() {
		return isAnagPresent;
	}

	public void setAnagPresent(boolean isAnagPresent) {
		this.isAnagPresent = isAnagPresent;
	}

    @Override
    public void buildInternalArray() {

    }
}
