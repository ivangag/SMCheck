package org.symptomcheck.capstone.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.symptomcheck.capstone.R;
import org.symptomcheck.capstone.SyncUtils;
import org.symptomcheck.capstone.adapters.DrawerItem;
import org.symptomcheck.capstone.adapters.DrawerItemAdapter;
import org.symptomcheck.capstone.bus.DownloadEvent;
import org.symptomcheck.capstone.dao.DAOManager;
import org.symptomcheck.capstone.fragments.CheckInFragment;
import org.symptomcheck.capstone.fragments.DoctorFragment;
import org.symptomcheck.capstone.fragments.ICardEventListener;
import org.symptomcheck.capstone.fragments.IFragmentListener;
import org.symptomcheck.capstone.fragments.MedicinesFragment;
import org.symptomcheck.capstone.fragments.PatientsFragment;
import org.symptomcheck.capstone.model.CheckIn;
import org.symptomcheck.capstone.model.FeedStatus;
import org.symptomcheck.capstone.model.PainLevel;
import org.symptomcheck.capstone.model.PainMedication;
import org.symptomcheck.capstone.model.Patient;
import org.symptomcheck.capstone.model.Question;
import org.symptomcheck.capstone.model.UserInfo;
import org.symptomcheck.capstone.model.UserType;
import org.symptomcheck.capstone.provider.ActiveContract;
import org.symptomcheck.capstone.preference.UserPreferencesManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;
import hirondelle.date4j.DateTime;


public class MainActivity extends Activity implements ICardEventListener {


    private final String TAG = MainActivity.this.getClass().getSimpleName();


    ImageView mImageView;
    private String[] mFragmentTitles = new String[]{};
    private int[] mDrawerImagesResources;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private TextView mTextViewHeaderUser;
    private TextView mTextViewUserDetails;
    private ShareActionProvider mShareActionProvider;
    private Fragment mCurrentFragment;
    private Fragment mPreviousFragment;
    private int mSelectedFragmentPosition = -1;
    private ShowFragmentType mSelectedFragmentType;

    public enum ShowFragmentType{
        DOCTOR_PATIENTS,
        SETTINGS,
        PATIENT_CHECKINS,
        PATIENT_DOCTORS,
        PATIENT_MEDICINES,
        LOGOUT,
    }

    private static final int CASE_SHOW_DOCTOR_PATIENTS = 0;
    private static final int CASE_SHOW_DOCTOR_SETTINGS = 1;
    private static final int CASE_SHOW_DOCTOR_LOGOUT = 2;
    private static final int CASE_SHOW_PATIENT_CHECKINS = 0;
    private static final int CASE_SHOW_PATIENT_DOCTORS = 1;
    private static final int CASE_SHOW_PATIENT_MEDICINES = 2 ;
    private static final int CASE_SHOW_PATIENT_SETTINGS = 3;
    private static final int CASE_SHOW_PATIENT_LOGOUT = 4;

    private UserInfo user;

    private static int mFragmentBackStackCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView)findViewById(R.id.imageChartApi);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mTextViewHeaderUser = (TextView) findViewById(R.id.txt_header_user);
        mTextViewUserDetails = (TextView) findViewById(R.id.txt_header_user_details);

        mTitle = mDrawerTitle = getTitle();

        user = DAOManager.get().getUser();

        if(user != null) {
            initUserResource();
            getFragmentManager().
                    addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                        @Override
                        public void onBackStackChanged() {
                            /*if(getFragmentManager().getBackStackEntryCount() > 0) {
                                FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
                                String str = backEntry.getName();
                                Fragment fragment = getFragmentManager().findFragmentByTag(str);
                                ;
                                if(!(mCurrentFragment instanceof DialogFragment)){

                                }
                            }*/
                            if(mFragmentBackStackCount > getFragmentManager().getBackStackEntryCount()){
                                // fragment removed
                                Log.d("MainActivity", "Fragment Removed");
                                mCurrentFragment = mPreviousFragment;
                            }else{
                                Log.d("MainActivity", "Fragment Added");
                            }
                            mFragmentBackStackCount = getFragmentManager().getBackStackEntryCount();
                        }
                    });

            //mDrawerList.addHeaderView(mTextViewHeaderUser);

            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            // Set the adapter for the list view
            List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
            for (int idx = 0; idx < mFragmentTitles.length; idx++) {
                drawerItems.add(new DrawerItem(mFragmentTitles[idx], mDrawerImagesResources[idx]));
            }
            final DrawerItemAdapter mDrawerItemAdapter = new DrawerItemAdapter(getApplicationContext(), drawerItems);

            //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
            //R.layout.drawer_list_item, mFragmentTitles));
            mDrawerList.setAdapter(mDrawerItemAdapter);
            // Set the list's click listener
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

            // enable ActionBar app icon to behave as action to toggle nav drawer
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.drawer_open, R.string.drawer_close) {

                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    getActionBar().setTitle(mTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            if (user.getUserType().equals(UserType.DOCTOR)) {
                selectDrawerItem(CASE_SHOW_DOCTOR_PATIENTS,-1);
            } else if (user.getUserType().equals(UserType.PATIENT)) {
                selectDrawerItem(CASE_SHOW_PATIENT_CHECKINS,-1);
            }
        }else{
            Toast.makeText(this,"User not more Logged!!!!!",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initUserResource() {

        if(user != null) {
            final UserType userType = user.getUserType();
            String detailUser = "";

            detailUser = user.getFirstName()
                    + " " + user.getLastName();
            try {
                if (userType.equals(UserType.DOCTOR)) {
                    mFragmentTitles = getResources().getStringArray(R.array.doctor_fragments_array);
                    mDrawerImagesResources = new int[]{R.drawable.ic_patient, R.drawable.ic_action_settings,
                            R.drawable.ic_logout};
                    Picasso.with(this).load(R.drawable.ic_doctor)
                            //.resize(96, 96)
                            //.centerCrop()
                            .into(mImageView);
                } else if (userType.equals(UserType.PATIENT)) {
                    mFragmentTitles = getResources().getStringArray(R.array.patient_fragments_array);
                    mDrawerImagesResources = new int[]{
                            R.drawable.ic_check_in,
                            R.drawable.ic_doctor,
                            R.drawable.ic_medicine,
                            R.drawable.ic_action_settings,
                            R.drawable.ic_logout};
                    Picasso.with(this).load(R.drawable.ic_patient)
                            //.resize(96, 96)
                            //.centerCrop()
                            .into(mImageView);

                }
            } catch (Exception e) {
                Toast.makeText(this, "Picasso error:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            mTextViewHeaderUser.setText(userType.toString().toUpperCase()
                    + "\n"
                    + "[" + user.getUserIdentification() + "]");
            mTextViewUserDetails.setText(
                    user.getUserType().toString().toUpperCase()
                            + "\n"
                            + detailUser);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action getItemsQuestion related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        MenuItem menuCheckInTest = menu.findItem(R.id.action_test);
        menuCheckInTest.setVisible(false);
        /*
        if((menuCheckInTest != null)
                && (user != null)) {
            menuCheckInTest.setVisible(user.getUserType().equals(UserType.PATIENT));
        }*/
        return super.onPrepareOptionsMenu(menu);

    }


    private Fragment selectFragment(ShowFragmentType fragmentType, long ownerId) {

        Fragment fragment = null;
                switch (fragmentType) {
                    case DOCTOR_PATIENTS:
                        fragment = new PatientsFragment();
                        break;
                    case PATIENT_CHECKINS:
                        fragment = CheckInFragment.newInstance(ownerId);
                        break;
                    case PATIENT_DOCTORS:
                        fragment = new DoctorFragment();
                        break;
                    case PATIENT_MEDICINES:
                        fragment = MedicinesFragment.newInstance(ownerId);
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
    private Fragment selectFragment(int position, long ownerId) {

        Fragment fragment = null;
        switch (user.getUserType()) {
            case DOCTOR:
                switch (position) {
                    case CASE_SHOW_DOCTOR_PATIENTS:
                        fragment = new PatientsFragment();
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
                        fragment = CheckInFragment.newInstance(ownerId);
                        break;
                    case CASE_SHOW_PATIENT_DOCTORS:
                        fragment = new DoctorFragment();
                        break;
                    case CASE_SHOW_PATIENT_MEDICINES:
                        fragment = MedicinesFragment.newInstance(ownerId);
                        break;
                    case CASE_SHOW_PATIENT_SETTINGS:
                        openSettings();
                        break;
                    case CASE_SHOW_PATIENT_LOGOUT:
                        fragment =  AlertLogoutFragment.newInstance();
                        break;
                }
                break;
            default:
                break;
        }
        return fragment;
    }


    private void openSettings(){
        //SettingsActivity.startSettingActivity(getApplicationContext());
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    private void askForLogout(DialogFragment logoutFragment){
//        DialogFragment newFragment = AlertLogoutFragment
//                .newInstance();
        logoutFragment.show(getFragmentManager(), "logout_dialog");
    }
    private void doLogout(){

        DAOManager.get().wipeAllData();
        UserPreferencesManager.get().setLogged(this,false);
        finish();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
    }

    private void openFragment(Fragment fragment, boolean addToBackStack) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(addToBackStack) {
            fragmentTransaction
                    .addToBackStack(null)
                    .replace(R.id.content_frame, fragment);
        }else{
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
        // Pass any configuration change to the drawer toggls
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
        //this.setTitle("MainActivity");
    }

    public void onEvent(DownloadEvent downloadEvent){
        final String msgEvent = downloadEvent.toString();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"EventBus downloadEvent: " + msgEvent + " at " + TAG,Toast.LENGTH_LONG).show();
            }
        });

    }
    public void onEventMainThread(DownloadEvent downloadEvent){
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
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider)shareItem.getActionProvider();
        mShareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

        /*
        mShareActionProvider.setShareIntent(getDefaultIntent());
        if(mIsShareIntentPending)
            updateShareIntentWithText();
            */
        mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(getActivity().getApplicationContext(), "onQueryTextSubmit:" + query, Toast.LENGTH_SHORT).show();
                //mNetAdapter.update(query.toUpperCase());
                IFragmentListener notifier = getCurrentDisplayedFragment();
                if (notifier != null)
                    notifier.OnFilterData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(getActivity().getApplicationContext(), "onQueryTextChange:" + newText,Toast.LENGTH_SHORT).show();
                IFragmentListener notifier = getCurrentDisplayedFragment();
                if(notifier != null)
                    notifier.OnFilterData(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //mNetAdapter.getFilter().filter(getResources().getString(R.string.filterALL));
                return true;
            }
        });
    }


    String urlPicassoTest = "http://chart.apis.google.com/chart?cht=p3&chs=500x200&chd=e:TNTNTNGa&chts=000000,16&chtt=A+Better+Web&chl=Hello|Hi|anas|Explorer&chco=FF5533,237745,9011D3,335423&chdl=Apple|Mozilla|Google|Microsoft";

    String getUrlPicassoTest2 = "http://i.imgur.com/DvpvklR.png";
    String urlDoctorTest ="https://cdn0.iconfinder.com/data/icons/customicondesign-office6-shadow/256/doctor.png";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_test){
            /*
            try {
                if(DAOManager.get().getUser().getUserType().equals(UserType.DOCTOR)) {
                    Picasso.with(this).load(R.drawable.ic_doctor)
                            //.resize(96, 96)
                            //.centerCrop()
                            .into(mImageView);
                }else if(DAOManager.get().getUser().getUserType().equals(UserType.PATIENT)) {
                    Picasso.with(this).load(R.drawable.ic_patient)
                            //.resize(96, 96)
                            //.centerCrop()
                            .into(mImageView);
                }
                }catch (Exception e){
                Toast.makeText(this, "Picasso error:" + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
            testAddCheckIn();
            */
        }
        /*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        /*
        if (id == R.id.action_opencards) {
            openFragment();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public IFragmentListener getCurrentDisplayedFragment() {
        return (mCurrentFragment instanceof IFragmentListener ? (IFragmentListener) mCurrentFragment : null);
    }

    @Override
    public void OnCheckInOpenRequired(long patientId) {
        final ShowFragmentType fragmentType = ShowFragmentType.PATIENT_CHECKINS;
        mPreviousFragment = mCurrentFragment;
        mCurrentFragment = selectFragment(fragmentType,patientId);
        mSelectedFragmentType = fragmentType;
        if (mCurrentFragment != null)
            openFragment(mCurrentFragment,true);
    }

    @Override
    public void OnMedicinesOpenRequired(long patientId) {
        final ShowFragmentType fragmentType = ShowFragmentType.PATIENT_MEDICINES;
        mPreviousFragment = mCurrentFragment;
        mCurrentFragment = selectFragment(fragmentType,patientId);
        mSelectedFragmentType = fragmentType;
        if (mCurrentFragment != null)
            openFragment(mCurrentFragment,true);
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectDrawerItem(position,-1);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectDrawerItem(int position, long ownerId) {
        if(mSelectedFragmentPosition != position) {
            mPreviousFragment = mCurrentFragment;
            mCurrentFragment = selectFragment(position,ownerId);
            if(!(mCurrentFragment instanceof DialogFragment)) {
                if (mCurrentFragment != null){
                    mSelectedFragmentPosition = position;
                    openFragment(mCurrentFragment,false);
                }
            }
        }

        if(mCurrentFragment != null) {
            if (!(mCurrentFragment instanceof DialogFragment)) {
                // Highlight the selected item, update the title, and close the drawer
                mDrawerList.setItemChecked(position, true);
                //setTitle(mFragmentTitles[position]);
            } else {
                askForLogout((DialogFragment) mCurrentFragment);
            }
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if(getActionBar() != null)
            getActionBar().setTitle(mTitle);
    }

    public static class AlertLogoutFragment extends DialogFragment {

        public static AlertLogoutFragment newInstance() {
            AlertLogoutFragment frag = new AlertLogoutFragment();
            Bundle args = new Bundle();
            //args.putInt("title", title);
            //args.putString("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //final int title = getArguments().getInt("title");
            //final String message = getArguments().getString("message");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_logout)
                    .setMessage(getString(R.string.logout_question))
                    .setTitle(getString(R.string.title_activity_main))
                    .setPositiveButton(R.string.alert_dialog_ok,
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

}
