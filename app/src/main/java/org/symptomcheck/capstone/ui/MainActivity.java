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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Update;
import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogCancelListener;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.google.common.collect.Lists;
import com.heinrichreimersoftware.materialdrawer.DrawerView;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerProfile;
import com.makeramen.RoundedTransformationBuilder;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.squareup.picasso.Transformation;

import org.symptomcheck.capstone.App;
import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.alarms.SymptomAlarmRequest;
import org.symptomcheck.capstone.bus.DownloadEvent;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.fragments.CheckInFragment;
import org.symptomcheck.capstone.fragments.CheckInOnlineFragment;
import org.symptomcheck.capstone.fragments.DoctorFragment;
import org.symptomcheck.capstone.fragments.ExperiencesFragment;
import org.symptomcheck.capstone.fragments.ICardEventListener;
import org.symptomcheck.capstone.fragments.IFragmentListener;
import org.symptomcheck.capstone.fragments.MedicinesFragment;
import org.symptomcheck.capstone.fragments.PatientsFragment;
import org.symptomcheck.capstone.model.Doctor;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.PatientExperience;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.preference.UserPreferencesManager;
import org.symptomcheck.capstone.utils.Constants;
import org.symptomcheck.capstone.utils.DateTimeUtils;
import org.symptomcheck.capstone.utils.NotificationHelper;

import java.util.List;

import de.greenrobot.event.EventBus;

//import android.widget.SearchView;

//TODO#BPR_3 Main Screen Activity
//TODO#BPR_6
public class MainActivity extends ActionBarActivity implements ICardEventListener,ISimpleDialogListener,ISimpleDialogCancelListener {

    private final String TAG = MainActivity.this.getClass().getSimpleName();
    ImageView mToolBarImageView;
    private String[] mFragmentTitles = new String[]{};
    private List<DrawerItemHelper> mDrawerItemTitles = Lists.newArrayList();

    private int[] mDrawerImagesResources;

    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private TextView mTextViewHeaderUser;
    private TextView mTextViewUserDetails;
    private Fragment mCurrentFragment;
    private Fragment mPreviousFragment;
    private int mSelectedFragmentPosition = -1;
    private ShowFragmentType mSelectedFragmentType;

    @Override
    public void onCancelled(int i) {

    }

    @Override
    public void onPositiveButtonClicked(int i) {

    }

    @Override
    public void onNegativeButtonClicked(int i) {

    }

    @Override
    public void onNeutralButtonClicked(int i) {

    }

    public enum ShowFragmentType {
        DOCTOR_PATIENTS,
        DOCTOR_PATIENTS_EXPERIENCES,
        SETTINGS,
        PATIENT_CHECKINS,
        PATIENT_ONLINE_CHECKINS,
        PATIENT_DOCTORS,
        PATIENT_MEDICINES,
        LOGOUT,
    }

    private static final int CASE_SHOW_DOCTOR_PATIENTS = 0;
    private static final int CASE_SHOW_DOCTOR_PATIENTS_EXPERIENCES = 1;
    private static final int CASE_SHOW_DOCTOR_PATIENTS_ONLINE_CHECKINS = 2;
    private static final int CASE_SHOW_DOCTOR_SETTINGS = 3;
    private static final int CASE_SHOW_DOCTOR_LOGOUT = 4;
    private static final int CASE_SHOW_PATIENT_CHECKINS = 0;
    private static final int CASE_SHOW_PATIENT_DOCTORS = 1;
    private static final int CASE_SHOW_PATIENT_MEDICINES = 2;
    private static final int CASE_SHOW_PATIENT_SETTINGS = 3;
    private static final int CASE_SHOW_PATIENT_LOGOUT = 4;

    private UserInfo user;

    private static int mFragmentBackStackCount = 0;

    private Toolbar toolbar;
    private TextView toolbarTitle;
    NavigationDrawerFragment mDrawerFragment;
    private View mFloatingActionButton;

    private DrawerLayout mDrawerLayout;
    private DrawerView mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_materialdrawer);
        mToolBarImageView = (ImageView) findViewById(R.id.imageToolBar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawer = (DrawerView) findViewById(R.id.drawer_material);

        mFloatingActionButton = (View) findViewById(R.id.fab_main);

        mTitle = mDrawerTitle = getTitle();

        user = DAOManager.get().getUser();

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) findViewById(R.id.txt_toolbar_title);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ){

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(mDrawer);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //App.hideSoftKeyboard(MainActivity.this);
        // TODO#BPR_2 activate functionality only if user is logged
        if (user != null) {
            initMaterialResource();
            initUserResource();
            updateDrawer();
            getFragmentManager().
                    addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                        @Override
                        public void onBackStackChanged() {
                            if (mFragmentBackStackCount > getFragmentManager().getBackStackEntryCount()) {
                                // fragment removed
                                Log.d("MainActivity", "Fragment Removed");
                                mCurrentFragment = mPreviousFragment;
                            } else {
                                Log.d("MainActivity", "Fragment Added");
                            }
                            mFragmentBackStackCount = getFragmentManager().getBackStackEntryCount();
                        }
                    });

            //TODO#BPR_1
            //TODO#BPR_2
            if (user.getUserType().equals(UserType.DOCTOR)) {
                selectDrawerItem(CASE_SHOW_DOCTOR_PATIENTS);
            } else if (user.getUserType().equals(UserType.PATIENT)) {
                selectDrawerItem(CASE_SHOW_PATIENT_CHECKINS);
            }
            Toast.makeText(this,"Welcome " + user.getFirstName().toUpperCase() + " "
                        + user.getLastName().toUpperCase(),Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "User not more Logged!!!!!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void updateDrawer() {
        mDrawer.clearItems();

        for(DrawerItemHelper item : mDrawerItemTitles){
            if(item.isNeedDivider()){
                mDrawer.addDivider();
            }
            mDrawer.addItem(new DrawerItem()
                            .setImage(getResources().getDrawable(item.getImage()))
                            .setTextPrimary(item.getmTitle())
                            .setTextSecondary(item.getExtra_info())
                            .setId(item.getPosition())
            );
        }

        Drawable avatar = null;
        if (user.getUserType().equals(UserType.DOCTOR)) {
            avatar = getResources().getDrawable(R.drawable.ic_doctor);
        }else {
            avatar = getResources().getDrawable(R.drawable.ic_patient);
        }
        Drawable background = getResources().getDrawable(R.drawable.mat2);

        mDrawer.setProfile(new DrawerProfile()
                        .setAvatar(avatar)
                        .setBackground(background)
                        .setName(user.getFirstName() + " " + user.getLastName())
                        .setDescription(user.getUserIdentification())
                        .setOnProfileClickListener(new DrawerProfile.OnProfileClickListener() {
                            @Override
                            public void onClick(DrawerProfile drawerProfile) {
                                Toast.makeText(getApplicationContext(),drawerProfile.getName() + "-" +drawerProfile.getDescription(),Toast.LENGTH_SHORT).show();
                            }
                        })
        );

        mDrawer.selectItem(1);
        mDrawer.setOnItemClickListener(new com.heinrichreimersoftware.materialdrawer.structure.DrawerItem.OnItemClickListener() {
            @Override
            public void onClick(DrawerItem item, int id, int position) {
                selectDrawerItem(id);
                mDrawer.selectItem(position);
                Toast.makeText(getApplicationContext(), "Clicked item #" + position + " id #" + id, Toast.LENGTH_SHORT).show();

            }
        });

    }


    public void updateDrawerTest() {
        mDrawer.clearItems();

        mDrawer.addItem(new com.heinrichreimersoftware.materialdrawer.structure.DrawerItem()
                        .setTextPrimary(getString(R.string.app_name))
                        .setTextSecondary(getString(R.string.item_1))
        );

        Drawable icon1 = getResources().getDrawable(R.drawable.ic_doctor);
        mDrawer.addItem(new com.heinrichreimersoftware.materialdrawer.structure.DrawerItem()
                        .setImage(icon1)
                        .setTextPrimary(getString(R.string.app_name))
                        .setTextSecondary(getString(R.string.item_1))
        );

        mDrawer.addDivider();

        Drawable icon2;
        if (Math.random() >= .5) {
            icon2 = getResources().getDrawable(R.drawable.ic_patient);
        } else {
            icon2 = getResources().getDrawable(R.drawable.ic_doctor);
        }
        mDrawer.addItem(new com.heinrichreimersoftware.materialdrawer.structure.DrawerItem()
                        .setImage(icon2, com.heinrichreimersoftware.materialdrawer.structure.DrawerItem.AVATAR)
                        .setTextPrimary(getString(R.string.app_name))
                        .setTextSecondary(getString(R.string.item_1))
        );

        Drawable icon3;
        if (Math.random() >= .5) {
            icon3 = getResources().getDrawable(R.drawable.ic_patient);
        } else {
            icon3 = getResources().getDrawable(R.drawable.ic_doctor);
        }
        mDrawer.addItem(new com.heinrichreimersoftware.materialdrawer.structure.DrawerItem()
                        .setImage(icon3)
                        .setTextPrimary(getString(R.string.app_name))
                        .setTextSecondary(getString(R.string.item_1), com.heinrichreimersoftware.materialdrawer.structure.DrawerItem.THREE_LINE)
        );
        Drawable avatar = getResources().getDrawable(R.drawable.ic_patient);

        Drawable background = getResources().getDrawable(R.drawable.mat2);

        mDrawer.setProfile(new DrawerProfile()
                .setAvatar(avatar)
                .setBackground(background)
                .setName(user.getFirstName() + " " + user.getLastName())
                .setDescription(user.getUserIdentification())
                .setOnProfileClickListener(new DrawerProfile.OnProfileClickListener() {
                    @Override
                    public void onClick(DrawerProfile drawerProfile) {
                        Toast.makeText(getApplicationContext(), drawerProfile.getName() + "-" + drawerProfile.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                })
        );

        mDrawer.selectItem(1);
        mDrawer.setOnItemClickListener(new com.heinrichreimersoftware.materialdrawer.structure.DrawerItem.OnItemClickListener() {
            @Override
            public void onClick(com.heinrichreimersoftware.materialdrawer.structure.DrawerItem item, int id, int position) {
                mDrawer.selectItem(position);
                Toast.makeText(getApplicationContext(), "Clicked item #" + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initUserResource() {
        final UserType userType = user.getUserType();
        String detailUser = "";
        detailUser = user.getFirstName()
                + " " + user.getLastName();
        try {
            //TODO#BPR_1
            //TODO#BPR_2
            if (userType.equals(UserType.DOCTOR)) {

                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.patients_header),
                                getResources().getString(R.string.patients_header_info),
                                R.drawable.ic_people_grey600_48dp,
                                CASE_SHOW_DOCTOR_PATIENTS,false));
                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.title_bad_experience_notification),
                                getResources().getString(R.string.title_bad_experience_notification_info),
                                R.drawable.ic_poll_grey600_48dp,
                                CASE_SHOW_DOCTOR_PATIENTS_EXPERIENCES,false));
                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.title_search_checkin_online),
                                getResources().getString(R.string.title_search_checkin_online_info),
                                R.drawable.ic_search_grey600_48dp,
                                CASE_SHOW_DOCTOR_PATIENTS_ONLINE_CHECKINS,false));

                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.action_settings),
                                getResources().getString(R.string.action_settings_info),
                                R.drawable.ic_action_settings,
                                CASE_SHOW_DOCTOR_SETTINGS,true));
                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.action_logout),
                                getResources().getString(R.string.action_logout_info),
                                R.drawable.ic_exit_to_app_grey600_48dp,
                                CASE_SHOW_DOCTOR_LOGOUT,false));

                detailUser += "\nID " + Doctor.getByDoctorNumber(user.getUserIdentification()).getUniqueDoctorId();
            } else if (userType.equals(UserType.PATIENT)) { //TODO#FDAR_1 show details of Patient on the a view in front of the main activity

                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.checkins_header),
                                getResources().getString(R.string.checkins_header_info),
                                R.drawable.ic_poll_grey600_48dp,
                                CASE_SHOW_PATIENT_CHECKINS,false));
                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.doctors_header),
                                getResources().getString(R.string.doctors_header_info),
                                R.drawable.ic_people_grey600_48dp,
                                CASE_SHOW_PATIENT_DOCTORS,false));
                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.medicines_header),
                                getResources().getString(R.string.medicines_header_info),
                                R.drawable.ic_list_grey600_48dp,
                                CASE_SHOW_DOCTOR_PATIENTS_ONLINE_CHECKINS,false));

                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.action_settings),
                                getResources().getString(R.string.action_settings_info),
                                R.drawable.ic_action_settings,
                                CASE_SHOW_PATIENT_SETTINGS,true));
                mDrawerItemTitles.add(
                        new DrawerItemHelper (getResources().getString(R.string.action_logout),
                                getResources().getString(R.string.action_logout_info),
                                R.drawable.ic_exit_to_app_grey600_48dp,
                                CASE_SHOW_PATIENT_LOGOUT,false));
                detailUser +=
                        "\nBorn on " + DateTimeUtils.convertEpochToHumanTime(Patient.getByMedicalNumber(user.getUserIdentification()).getBirthDate(), "DD/MM/YYYY")
                                + "\nMedical Number " + user.getUserIdentification()
                ;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Picasso error:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void initMaterialResource() {
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        // repeat many times:
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_camera));
        SubActionButton button1 = itemBuilder
                .setContentView(itemIcon)
                .setLayoutParams(new FrameLayout.LayoutParams(128,128))
                .build();


        ImageView itemIcon2 = new ImageView(this);
        itemIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_search));
        SubActionButton button2 = itemBuilder
                .setContentView(itemIcon2)
                .setLayoutParams(new FrameLayout.LayoutParams(128,128))
                .build();

        ImageView itemIcon3 = new ImageView(this);
        itemIcon.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_refresh));
        SubActionButton button3 = itemBuilder
                .setContentView(itemIcon3)
                .setLayoutParams(new FrameLayout.LayoutParams(128,128))
                .build();

        /*
        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                        // ...
                .attachTo(mFloatingActionButton)
                .build();*/

        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(this);
        ImageView rlIcon1 = new ImageView(this);
        ImageView rlIcon2 = new ImageView(this);
        ImageView rlIcon3 = new ImageView(this);
        ImageView rlIcon4 = new ImageView(this);

        rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_chat_white_24dp));
        rlIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_white_24dp));
        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_videocam_white_24dp));
        rlIcon4.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_white_24dp));

        SubActionButton subAct1 = rLSubBuilder.setContentView(rlIcon1)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.fab_background_color))
                .setLayoutParams(new FrameLayout.LayoutParams(128, 128)).build();
        subAct1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Clicked subAct1",Toast.LENGTH_SHORT).show();
            }
        });
        // Build the menu with default options: light theme, 90 degrees, 72dp radius.
        // Set 4 default SubActionButtons
        FloatingActionMenu rightLowerMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(subAct1)
                .addSubActionView(rLSubBuilder.setContentView(rlIcon2).setLayoutParams(new FrameLayout.LayoutParams(128,128)).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon3).setLayoutParams(new FrameLayout.LayoutParams(128,128)).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon4).setLayoutParams(new FrameLayout.LayoutParams(128,128)).build())
                .attachTo(mFloatingActionButton)
                .build();


    }

    // ----------------------------------------------------------------
    // apply rounding to image
    // see: https://github.com/vinc3m1/RoundedImageView
    // ----------------------------------------------------------------
    Transformation transformation = new RoundedTransformationBuilder()
//            .borderColor(getResources().getColor(R.color.primary))
//            .borderWidthDp(5)
//            .cornerRadiusDp(50)
//            .oval(false)
//            .build();
            .borderColor(Color.TRANSPARENT)
            .borderWidthDp(3)
            .cornerRadiusDp(30)
            .oval(true)
            .build();


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav mDrawer is open, hide action getItemsQuestion related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        MenuItem menuCheckInTest = menu.findItem(R.id.action_test);
        menuCheckInTest.setVisible(true);
        return super.onPrepareOptionsMenu(menu);

    }


    //TODO#BPR_6 select Screen Fragment depending on data to be monitored
    private Fragment selectFragment(ShowFragmentType fragmentType, String ownerId) {

        Fragment fragment = null;
        switch (fragmentType) {
            case DOCTOR_PATIENTS:
                fragment = new PatientsFragment();
                break;
            case PATIENT_CHECKINS: //TODO#FDAR_10
                fragment = CheckInFragment.newInstance(ownerId);
                break;
            case PATIENT_ONLINE_CHECKINS:
                fragment = CheckInOnlineFragment.newInstance();
                break;
            case PATIENT_DOCTORS:
                fragment = new DoctorFragment();
                break;
            case PATIENT_MEDICINES:
                fragment = MedicinesFragment.newInstance(ownerId);
                break;
            case DOCTOR_PATIENTS_EXPERIENCES:
                fragment = ExperiencesFragment.newInstance(Constants.STRINGS.EMPTY); // we want ALL the Experiences of all patients
                break;
            case SETTINGS:
                openSettings();
                break;
            case LOGOUT:
                fragment = AlertLogoutFragment.newInstance();
                break;
            default:
                break;
        }
        return fragment;
    }

    private Fragment selectFragment(int position) {


        Fragment fragment = null;
        //TODO#BPR_1
        //TODO#BPR_2
        switch (user.getUserType()) {
            case DOCTOR:
                switch (position) {
                    case CASE_SHOW_DOCTOR_PATIENTS:
                        //fragment = new PatientsFragment();
                        fragment = selectFragment(ShowFragmentType.DOCTOR_PATIENTS, user.getUserIdentification());
                        break;
                    case CASE_SHOW_DOCTOR_PATIENTS_EXPERIENCES:
                        //fragment = new PatientsFragment();
                        fragment = selectFragment(ShowFragmentType.DOCTOR_PATIENTS_EXPERIENCES, user.getUserIdentification());
                        break;
                    case CASE_SHOW_DOCTOR_PATIENTS_ONLINE_CHECKINS:
                        //fragment = new PatientsFragment();
                        fragment = selectFragment(ShowFragmentType.PATIENT_ONLINE_CHECKINS, user.getUserIdentification());
                        break;
                    case CASE_SHOW_DOCTOR_SETTINGS:
                        openSettings();
                        break;
                    case CASE_SHOW_DOCTOR_LOGOUT:
                        fragment = AlertLogoutFragment.newInstance();
                        break;
                }
                break;
            case PATIENT:
                switch (position) {
                    case CASE_SHOW_PATIENT_CHECKINS:
                        //fragment = CheckInFragment.newInstance(ownerId);
                        fragment = selectFragment(ShowFragmentType.PATIENT_CHECKINS, user.getUserIdentification());
                        break;
                    case CASE_SHOW_PATIENT_DOCTORS:
                        //fragment = new DoctorFragment();
                        fragment = selectFragment(ShowFragmentType.PATIENT_DOCTORS, user.getUserIdentification());
                        break;
                    case CASE_SHOW_PATIENT_MEDICINES:
                        //fragment = MedicinesFragment.newInstance(ownerId);
                        fragment = selectFragment(ShowFragmentType.PATIENT_MEDICINES, user.getUserIdentification());
                        break;
                    case CASE_SHOW_PATIENT_SETTINGS:
                        openSettings();
                        break;
                    case CASE_SHOW_PATIENT_LOGOUT:
                        fragment = AlertLogoutFragment.newInstance();
                        break;
                }
                break;
            case ADMIN:
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }
        return fragment;
    }


    //TODO#BPR_6 open Settings Screen Activity
    private void openSettings() {
        UserPreferencesManager.get().printAll(this);
        //SettingsActivity.startSettingActivity(getApplicationContext());
        Intent intent = new Intent(this, SettingsActivity.class);
        //startActivity(intent);
        startActivityForResult(intent, SettingsActivity.MODIFY_USER_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        UserPreferencesManager.get().printAll(this);
        if (requestCode == SettingsActivity.MODIFY_USER_SETTINGS) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "!!User Settings Modified!!");
                SymptomAlarmRequest.get().setAlarm(this, SymptomAlarmRequest.AlarmRequestedType.ALARM_CHECK_IN_REMINDER,false);
            }
        }
    }

    private void askForExit(DialogFragment exitFragment) {
        //exitFragment.show(getFragmentManager(), "exit");
        AlertMaterialExitFragment.show(this);
    }
    private void askForLogout(DialogFragment logoutFragment) {
        //logoutFragment.show(getFragmentManager(), "logout_dialog");
        AlertMaterialLogoutFragment.show(this);
    }

    public void doLogout() {
        DAOManager.get().wipeAllData();
        UserPreferencesManager.get().setLogged(this, false);
        UserPreferencesManager.get().setNextScheduledCheckin(getApplicationContext(), Constants.STRINGS.EMPTY);
        finish();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void openFragment(Fragment fragment, boolean addToBackStack) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (addToBackStack) {
            fragmentTransaction
                    .addToBackStack(null)
                    .replace(R.id.content_frame, fragment);
        } else {
            fragmentTransaction
                    .replace(R.id.content_frame, fragment);
        }
        fragmentTransaction.commit();

    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the mDrawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mDrawerLayout.closeDrawers();
        //this.setTitle("MainActivity");
    }

    public void onEvent(DownloadEvent downloadEvent) {
        final String msgEvent = downloadEvent.toString();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"EventBus downloadEvent: " + msgEvent + " at " + TAG,Toast.LENGTH_LONG).show();
            }
        });

    }

    public void onEventMainThread(DownloadEvent downloadEvent) {
        String msgEvent = downloadEvent.toString();
        //Toast.makeText(this,"EventBusMainThread downloadEvent: " + msgEvent + " at " + TAG,Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds getItemsQuestion to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        setupMenuActions(menu);
        return true;
    }

    private void setupMenuActions(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                IFragmentListener notifier = getCurrentDisplayedFragment();
                if (notifier != null) {
                    //notifier.OnFilterData(query);
                    App.hideSoftKeyboard(MainActivity.this);
                    notifier.OnSearchOnLine(query); //TODO#FDAR_11 Doctor confirm Patient FirstName & LastName used to search ONLINE Check-Ins data
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(getActivity().getApplicationContext(), "onQueryTextChange:" + newText,Toast.LENGTH_SHORT).show();
                IFragmentListener notifier = getCurrentDisplayedFragment();
                if (notifier != null)
                    notifier.OnFilterData(newText);
                return true;
            }
        });

    }


    String urlPicassoTest = "http://chart.apis.google.com/chart?cht=p3&chs=500x200&chd=e:TNTNTNGa&chts=000000,16&chtt=A+Better+Web&chl=Hello|Hi|anas|Explorer&chco=FF5533,237745,9011D3,335423&chdl=Apple|Mozilla|Google|Microsoft";
    String getUrlPicassoTest2 = "http://i.imgur.com/DvpvklR.png";
    String urlDoctorTest = "https://cdn0.iconfinder.com/data/icons/customicondesign-office6-shadow/256/doctor.png";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        // The action bar home/up action should open or close the mDrawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_test) {


            NotificationHelper.raiseCheckinReminderNotification(this, 1, getString(R.string.checkin_reminder_text));
            List<PatientExperience> newBadPatientExperiences = PatientExperience.computeBadExperiences();
            //List<PatientExperience> patientExperiences = PatientExperience.getByPatient("patient001");
            List<PatientExperience> patientExperiences = PatientExperience.getAll();
            if (patientExperiences.size() > 0) {
                //PatientExperience experience = patientExperiences.get(0);
                for(PatientExperience experience : patientExperiences) {
                    (new Update(PatientExperience.class))
                            .set("checkedByDoctor = 0")
                            .where("_id = ?", experience.getId())
                            .execute();
                }
                Bundle data = new Bundle();
                //data.putString("EXPERIENCE_ID", experience.getExperienceId());
                //data.putString(PatientExperiencesActivity.PATIENT_ID, experience.getPatientId());
                NotificationHelper.sendNotification(this, 3,
                        "Bad Patient Experience", "Experience of one or more Patients require your attention",
                        PatientExperiencesActivity.class, true, PatientExperiencesActivity.ACTION_NEW_PATIENT_BAD_EXPERIENCE, null);
            }

        }

        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public IFragmentListener getCurrentDisplayedFragment() {
        return (mCurrentFragment instanceof IFragmentListener ? (IFragmentListener) mCurrentFragment : null);
    }

    @Override
    public void OnCheckInOpenRequired(String patientId) {
        //TODO#FDAR_10 this method is called when a Doctor is logged and it wishes open and monitor the Check-Ins of a Patient
        final ShowFragmentType fragmentType = ShowFragmentType.PATIENT_CHECKINS;
        mPreviousFragment = mCurrentFragment;
        mCurrentFragment = selectFragment(fragmentType, patientId);
        mSelectedFragmentType = fragmentType;
        if (mCurrentFragment != null)
            openFragment(mCurrentFragment, true);
    }

    @Override
    public void OnMedicinesOpenRequired(String patientId) {
        final ShowFragmentType fragmentType = ShowFragmentType.PATIENT_MEDICINES;
        mPreviousFragment = mCurrentFragment;
        mCurrentFragment = selectFragment(fragmentType, patientId);
        mSelectedFragmentType = fragmentType;
        if (mCurrentFragment != null)
            openFragment(mCurrentFragment, true);
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectDrawerItem(int position) {
        if (mSelectedFragmentPosition != position) {
            mPreviousFragment = mCurrentFragment;
            mCurrentFragment = selectFragment(position);
            if (!(mCurrentFragment instanceof DialogFragment)) {
                if (mCurrentFragment != null) {
                    //mSelectedFragmentPosition = position;
                    openFragment(mCurrentFragment, false);
                }
            }
            mSelectedFragmentPosition = position;
        }

        if (mCurrentFragment != null) {
            if (!(mCurrentFragment instanceof DialogFragment)) {
                // Highlight the selected item, update the mTitle, and close the mDrawer
                mDrawer.selectItem(position);
                //setTitle(mFragmentTitles[position]);
            } else {
                askForLogout((DialogFragment) mCurrentFragment);
            }
        }

        mDrawerLayout.closeDrawers();
        //mDrawerFragment.closeDrawer();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(mTitle);
            this.toolbarTitle.setText(mTitle);
        }
    }

    public static class AlertLogoutFragment extends DialogFragment {

        public static AlertLogoutFragment newInstance() {
            AlertLogoutFragment frag = new AlertLogoutFragment();
            Bundle args = new Bundle();
            //args.putInt("mTitle", mTitle);
            //args.putString("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //final int mTitle = getArguments().getInt("mTitle");
            //final String message = getArguments().getString("message");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_logout)
                    .setMessage(getString(R.string.logout_question))
                    .setTitle(getString(R.string.title_activity_main))
                    .setPositiveButton(R.string.alert_dialog_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    ((MainActivity) getActivity())
                                            .doLogout();
                                }
                            })
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dismiss();
                                }
                            }).create();
        }
    }

    public static class AlertMaterialLogoutFragment extends SimpleDialogFragment {
        static String TAG = "AlertMaterialLogoutFragment";
        public static void show(FragmentActivity activity) {
            new AlertMaterialLogoutFragment().show(activity.getSupportFragmentManager(), TAG);
        }
        @Override
        public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
            builder.setTitle(getString(R.string.title_activity_main));
            builder.setMessage(getString(R.string.logout_question));
            builder
                    .setPositiveButton(getString(R.string.alert_dialog_yes), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainActivity) getActivity())
                                    .doLogout();
                        }
                    });
            builder
                    .setNegativeButton(getString(R.string.alert_dialog_no), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismiss();
                        }
                    });

            return builder;
        }
    }

    static boolean isConfirmedExit = true;
    public static class AlertExitFragment extends DialogFragment {

        public static AlertExitFragment newInstance() {
            AlertExitFragment frag = new AlertExitFragment();
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //final int mTitle = getArguments().getInt("mTitle");
            //final String message = getArguments().getString("message");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_logout)
                    .setMessage(getString(R.string.exit_question))
                    .setTitle(getString(R.string.title_activity_main))
                    .setPositiveButton(R.string.alert_dialog_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    isConfirmedExit = true;
                                    getActivity().finish();
                                }
                            })
                    .setNegativeButton(R.string.alert_dialog_no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    isConfirmedExit = false;
                                    dismiss();
                                }
                            }).create();
        }
    }

    public static class AlertMaterialExitFragment extends SimpleDialogFragment {
        static String TAG = "AlertMaterialExitFragment";
        public static void show(FragmentActivity activity) {
            new AlertMaterialExitFragment().show(activity.getSupportFragmentManager(), TAG);
        }
        @Override
        public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
            builder.setTitle(getString(R.string.title_activity_main));
            builder.setMessage(getString(R.string.exit_question));
            //builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.view_jayne_hat, null));
            builder
                    .setPositiveButton(getString(R.string.alert_dialog_yes), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            ISimpleDialogListener listener = getDialogListener();
//                            if (listener != null) {
//                                listener.onPositiveButtonClicked(0);
//                            }
//                            dismiss();
                            isConfirmedExit = true;
                            getActivity().finish();
                        }
                    });
            builder
                    .setNegativeButton(getString(R.string.alert_dialog_no), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isConfirmedExit = false;
                            dismiss();
                        }
                    });

            return builder;
        }
    }

    @Override
    public void onBackPressed() {
        /*
        if(mFragmentBackStackCount >= 1){
            super.onBackPressed();
        }else{
            askForExit(AlertExitFragment.newInstance());
        }

*/
        // initialize variables
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

// check to see if stack is empty
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            ft.commit();
        }
        else {
            askForExit(AlertExitFragment.newInstance());
        }
    }

    class DrawerItemHelper {

        private boolean mNeedDivider;
        private String mTitle;
        private String mExtra_info;
        private int mPosition;
        private int mImage;

        public DrawerItemHelper(String title, String extra_info, int image,int position,  boolean needDivider){
            mTitle = title;
            mExtra_info = extra_info;
            mPosition = position;
            mImage = image;
            mNeedDivider = needDivider;
        }

        public String getmTitle() {
            return mTitle;
        }

        public String getExtra_info() {
            return mExtra_info;
        }

        public int getPosition() {
            return mPosition;
        }

        public int getImage() {
            return mImage;
        }

        public boolean isNeedDivider() {
            return mNeedDivider;
        }
    }


}
