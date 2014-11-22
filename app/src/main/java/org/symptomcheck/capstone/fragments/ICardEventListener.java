package org.symptomcheck.capstone.fragments;

/**
 * Created by Ivan on 17/11/2014.
 */
public interface ICardEventListener {
    public void OnCheckInOpenRequired(String patientId);

    public void OnMedicinesOpenRequired(String patientId);
}
