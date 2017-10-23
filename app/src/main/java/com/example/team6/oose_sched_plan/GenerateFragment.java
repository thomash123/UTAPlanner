package com.example.team6.oose_sched_plan;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GenerateFragment extends Fragment {


    @InjectView(R.id.spin_term)
    Spinner spinTerm;
    @InjectView(R.id.spin_year)
    Spinner spinYear;
    @InjectView(R.id.list_available)
    ListView listAvailable;
    @InjectView(R.id.c_info_dep_number_field)
    TextView cInfoDepNumberField;
    @InjectView(R.id.c_info_coursename_field)
    TextView cInfoCoursenameField;
    @InjectView(R.id.c_info_descript_field)
    TextView cInfoDescriptField;
    @InjectView(R.id.c_info_coursecredit_field)
    TextView cInfoCoursecreditField;
    @InjectView(R.id.button_add)
    Button buttonAdd;
    @InjectView(R.id.list_current)
    ListView listCurrent;

    private Spinner spinner_term, spinner_year;
    private ArrayAdapter<String> dataAdapter;
    private ListView list_available, list_current;
    private Button button_submit, button_add;
    public String degree_plan;
    private TextView cInfo_DepartmentNumber, cInfo_CourseName, cInfo_Description, cInfo_credit;
    private View row;

    // This list_dynam_Available is used to store data to be passed between methods
    //  This will be replaced with a class
    private ArrayList<HashMap<String, String>> list_dynam_Available = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> list_dynam_Current = new ArrayList<>();

    //TODO USING COURSE CLASS
    //private ArrayList<Course> list_dynamic_available;
    //private ArrayList<Course> list_dynamic_current;

    //store selected items
    private HashMap<String,String> selected_item;
    private int selected_position;

    SharedPreferences sharedPreferences;
    DegreePlanAdapter.FeedReaderDbHelper mDbHelper;
    SQLiteDatabase db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_generate, container, false);
        ButterKnife.inject(this, view);
        sharedPreferences = view.getContext().getSharedPreferences(Config.SHARED_PREF_NAME, view.getContext().MODE_PRIVATE);
        addItemsOnSpinTerm();
        addItemsOnSpinYear();
        addItemsOnAvailable(view);
        addItemsOnCurrentSchedule();
        button_add = buttonAdd;
        button_add.setClickable(true);
        button_add.setBackgroundResource(R.drawable.b_button_deselected);
        alertDegreePlan(view);

        return view;
    }

    /* ================================================================
    ||  TERM - Gets info and populates "term" dropdown spinner.
    ||  - Currently added from list_dynam_Available and not database
    ||=================================================================*/
    public void addItemsOnSpinTerm() {
        spinner_term = spinTerm;
        List<String> list_terms = new ArrayList<String>();
        list_terms.add("Spring");
        list_terms.add("Summer");
        list_terms.add("Fall");
        dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, list_terms);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_term.setAdapter(dataAdapter);
    }

    /* ================================================================
    ||  TERM YEAR - Gets info and populates "Year" dropdown spinner.
    ||  - Currently added from list_dynam_Available and not database
    ||=================================================================*/
    public void addItemsOnSpinYear() {
        spinner_year = spinYear;
        List<String> list_years = new ArrayList<String>();
        list_years.add("2017");
        list_years.add("2018");
        list_years.add("2019");
        list_years.add("2020");
        list_years.add("2021");
        dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, list_years);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(dataAdapter);
    }

    /* ================================================================
    ||  AVAILABLE - Gets info and populates "Available courses" list_dynam_Available. Allows user to select Course to update COURSE INFO;
    ||  - Currently added from list_dynam_Available and not database
    ||  - Only department/#, course name implemented
    ||=================================================================*/
    public void addItemsOnAvailable(final View view) {
        list_available = listAvailable;

        String[] from = new String[]{"course", "name"};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        int nativeLayout = android.R.layout.two_line_list_item;
        list_available.setAdapter(new SimpleAdapter(this.getContext(), list_dynam_Available, nativeLayout, from, to));
        list_available.setClickable(true);
        list_available.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                addItemsOnCourseInfo(list_dynam_Available, position);
                selected_item = list_dynam_Available.get(position);
                selected_position = position;
                if (row != null) {
                    row.setBackgroundResource(R.color.alpha);
                }
                row = v;
                v.setBackgroundResource(R.drawable.b_selected);
                addListenerOnAddButton();
            }
        });
    }

    /* ================================================================
    ||  COURSE INFO - Gets info from course selected,
    ||=================================================================*/
    public void addItemsOnCourseInfo(ArrayList<HashMap<String, String>> list, int position) {
        String department = list.get(position).get("course");
        String name = list.get(position).get("name");

        cInfo_DepartmentNumber = cInfoDepNumberField;
        cInfo_CourseName = cInfoCoursenameField;
        cInfo_Description = cInfoDescriptField;
        cInfo_credit = cInfoCoursecreditField;

        cInfo_DepartmentNumber.setText(department);
        cInfo_CourseName.setText(name);
    }

    /* ================================================================
    ||  ADD Button - gets the SELECTED ITEM and puts it into the current schedule.
    ||  - does not do any validation currently
    ||=================================================================*/
    public void addListenerOnAddButton( ) {
        button_add = buttonAdd;
        button_add.setText(R.string.button_add);
        button_add.setBackgroundResource(R.drawable.b_button_selected);
        button_add.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View arg0) {
                if (selected_item != null) {
                    list_dynam_Current.add(selected_item);
                    list_dynam_Available.remove(selected_position);
                    String[] from = new String[]{"course","name"};
                    int nativeLayout = android.R.layout.two_line_list_item;
                    int[] to = new int[]{android.R.id.text1, android.R.id.text2};
                    list_current.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Current, nativeLayout, from, to));
                    list_available.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Available, nativeLayout, from, to));
                    list_current.setClickable(true);
                    selected_item = null;
                    button_add.setBackgroundResource(R.drawable.b_button_deselected);
                }
            }
        });
    }

    public void addListenerOnRemoveButton( ) {
        button_add = buttonAdd;
        button_add.setText(R.string.button_remove);
        button_add.setBackgroundResource(R.drawable.b_button_remove);
        button_add.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View arg0) {
                if (selected_item != null) {
                    list_dynam_Available.add(selected_item);
                    list_dynam_Current.remove(selected_position);
                    String[] from = new String[]{"course","name"};
                    int nativeLayout = android.R.layout.two_line_list_item;
                    int[] to = new int[]{android.R.id.text1, android.R.id.text2};
                    list_current.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Current, nativeLayout, from, to));
                    list_available.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Available, nativeLayout, from, to));
                    list_current.setClickable(true);
                    selected_item = null;
                    button_add.setBackgroundResource(R.drawable.b_button_deselected);

                }

            }
        });
    }

    /* ================================================================
    ||  ADD TO CURRENT - initializes values and has listener for list item clicks
    ||=================================================================*/
    public void addItemsOnCurrentSchedule() {
        list_current = listCurrent;
        String[] from = new String[]{"course","name"};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        int nativeLayout = android.R.layout.two_line_list_item;

        //list_dynam_Current = new ArrayList<HashMap<String, String>>();

        list_current.setAdapter(new SimpleAdapter(this.getContext(), list_dynam_Current, nativeLayout, from, to));
        list_current.setClickable(true);

        list_current.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                addItemsOnCourseInfo(list_dynam_Current, position);
                selected_item = list_dynam_Current.get(position);
                selected_position = position;
                if (row != null) {
                    row.setBackgroundResource(R.color.alpha);
                }
                row = v;
                v.setBackgroundResource(R.drawable.b_selected);
                addListenerOnRemoveButton();
            }
        });
    }

    /* ================================================================
    ||  DEGREE PLAN - Alerts user with popup for Degree Plans.
    ||      TODO this needs to use savedPreferences to avoid reloading of courses
    ||=================================================================*/
    public void alertDegreePlan(final View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this.getContext());
        builderSingle.setTitle("Select a Degree Plan: ");
        final ArrayAdapter<String> list_majors = new ArrayAdapter<String>(this.getContext(), android.R.layout.select_dialog_singlechoice);
        list_majors.add("CSE - Computer Science Engineering");
        list_majors.add("SE - Software Engineering");
        list_majors.add("CPE - Computer Engineering");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(list_majors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Delete all entries to avoid duplicates
                DegreePlanInfo.DeleteAllEntries(view);

                mDbHelper = new DegreePlanAdapter.FeedReaderDbHelper(view.getContext());
                db = mDbHelper.getWritableDatabase();
                DegreePlanInfo.PopulateDatabase(db);


                //String strName = list_majors.getItem(which);
                list_dynam_Available = DegreePlanInfo.QueryDegreePlans(view.getContext(), mDbHelper,db, list_dynam_Available );

                //Update list
                String[] from = new String[]{"course","name"};
                int[] to = new int[]{android.R.id.text1, android.R.id.text2};
                int nativeLayout = android.R.layout.two_line_list_item;
                list_available.setAdapter(new SimpleAdapter(view.getContext(), list_dynam_Available, nativeLayout, from, to));

                /*
                switch (which)
                {
                    case 1:
                        list_dynam_Available = DegreePlanInfo.QueryDegreePlans(view.getContext(), mDbHelper,db, list_dynam_Available );
                }
                */

                mDbHelper.close();
            }
        });
        builderSingle.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
