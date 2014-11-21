package org.symptomcheck.capstone.model;

public enum ExperienceType {

	SEVERE("SEVERE"),
	SEVERE_OR_MODERATE("SEVERE_OR_MODERATE"),
	CANNOT_EAT("CANNOT_EAT"),
	UNKNOWN("UNKNOWN");

    private final String text;
    private ExperienceType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
