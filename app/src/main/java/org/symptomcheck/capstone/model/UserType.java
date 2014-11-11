package org.symptomcheck.capstone.model;

public enum UserType {

	PATIENT("PATIENT"),
	DOCTOR("DOCTOR"),
	ADMIN("ADMIN"),
    UNKNOWN("UNKNOWN");

    private final String text;
    private UserType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
