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
package org.symptomcheck.capstone.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.symptomcheck.capstone.App;
import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.fragments.ExperiencesFragment;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.PatientExperience;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;

import java.util.List;
//TODO#BPR_3 Patient Bad Experience Activity
//TODO#BPR_6
//TODO#FDAR_13
public class PatientExperiencesActivity extends Activity {

    public final static String PATIENT_ID = "patient_id";
    public final static String TAG = "PatientExperiencesActivity";
    public final static String ACTION_NEW_PATIENT_BAD_EXPERIENCE = "new_bad_experience_patient";
    View viewIntroNewExperienceInfo;
    TextView textViewDetails;
    Button btnGoToAllExperiences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_experiences);
        viewIntroNewExperienceInfo = findViewById(R.id.viewIntroExperience);
        textViewDetails = (TextView) findViewById(R.id.txt_view_bad_experience_test);
        btnGoToAllExperiences = (Button) findViewById(R.id.btn_go_to_all_experiences);
        Log.d(TAG, "Enter PatientExperienceActivity");
        if (savedInstanceState == null) {
            if((getIntent().getAction() != null)
                    &&  getIntent().getAction().equals(ACTION_NEW_PATIENT_BAD_EXPERIENCE)) {
                Log.d(TAG,"PatientExperienceActivity ACTION_NEW_PATIENT_BAD_EXPERIENCE");
                viewIntroNewExperienceInfo.setVisibility(View.VISIBLE);
                List<PatientExperience> patientExperiences = PatientExperience.getAllNotSeen();
                if(!patientExperiences.isEmpty()){
                    Log.d(TAG,"PatientExperienceActivity experiences not seen: " + patientExperiences.size());
                    textViewDetails.setText(Constants.STRINGS.EMPTY);
                    Patient patient;
                    for (PatientExperience patientExperience : patientExperiences){
                        patient = Patient.getByMedicalNumber(patientExperience.getPatientId());
                        String headStartTime = "[" + DateTimeUtils.convertEpochToHumanTime(patientExperience.getEndExperienceTime(),"YYYY-MM-DD hh:mm") + "]";
                        headStartTime += "\n";
                        String patientInfo = String.format(String.format("The Patient %s reported a bad experience claiming %d hours of %s",
                                patient.getFirstName() + " " + patient.getLastName(), patientExperience.getExperienceDuration(),
                                App.getPatientExperienceTranslation(patientExperience.getExperienceType())));
                        headStartTime += patientInfo + "\n";
                        textViewDetails.append(headStartTime);
                        textViewDetails.append("------------------------------\n");
                    }
                }else {
                    showAllExperiencesList();
                }

               // show details
            }else {

                showAllExperiencesList();
            }
        }
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        if(btnGoToAllExperiences != null){
            btnGoToAllExperiences.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewIntroNewExperienceInfo.setVisibility(View.GONE);
                    showAllExperiencesList();
                }
            });
        }
    }

    private void showAllExperiencesList() {
        viewIntroNewExperienceInfo.setVisibility(View.GONE);
        btnGoToAllExperiences.setVisibility(View.GONE);
        getFragmentManager().beginTransaction()
                .add(R.id.container, ExperiencesFragment.newInstance(Constants.STRINGS.EMPTY))
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient_experiences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_patient_experiences, container, false);
            return rootView;
        }
    }
}
