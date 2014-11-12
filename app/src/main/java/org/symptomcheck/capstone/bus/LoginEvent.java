package org.symptomcheck.capstone.bus;

/**
 * Created by igaglioti on 12/11/2014.
 */
public class LoginEvent {

    public LoginEvent(){}
    private boolean isLogged = false;
    private String  userName = "";

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
