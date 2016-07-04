package com.dtv.killerradio;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class FakeCallLogFragment extends Fragment {

    private static final String TAG = "FakeCallLogFragment";

    private static final int RANDOM_DURATION_MIN_SECONDS = 10;
    private static final int RANDOM_DURATION_MAX_SECONDS = 300;

    private static EditText mTimeOfCall;
    private static EditText mDateOfCall;

    private static int year;
    private static int month;
    private static int day;
    private static int hourOfDay; // Hour of day always - 24 hours
    private static int minute;
    private static int am_pm;
    private EditText mPhoneNumber;
    private String cNumber;
    private EditText mCallDuration;
    private EditText mCallType;
    private int callType;
    private String randomDurationText;

    public FakeCallLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fake_call_log, container, false);
        mPhoneNumber = (EditText) rootView.findViewById(R.id.phone_number);
        mCallDuration = (EditText) rootView.findViewById(R.id.call_duration);
        randomDurationText = getResources().getString(R.string.random_duration);
        mCallDuration.setText(randomDurationText);

        mCallDuration.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mCallDuration.getText().toString().equals(randomDurationText))
                {
                    mCallDuration.setText("");
                }
                return false;
            }
        });

        mCallDuration.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if (mCallDuration.getText().toString().trim().equals(""))
                    {
                        mCallDuration.setText(randomDurationText);
                    }
                }
                else
                {
                    if (mCallDuration.getText().toString().equals(randomDurationText))
                    {
                        mCallDuration.setText("");
                    }
                    v.performClick();
                }

            }
        });

        mDateOfCall = (EditText) rootView.findViewById(R.id.date_of_call);
        mDateOfCall.setInputType(InputType.TYPE_NULL);
        mDateOfCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        mTimeOfCall = (EditText) rootView.findViewById(R.id.time_of_call);
        mTimeOfCall.setInputType(InputType.TYPE_NULL);
        mTimeOfCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
            }
        });

        initTimeOfCallWithCurrentTime();

        ImageButton contactButton = (ImageButton) rootView.findViewById(R.id.contact_icon);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectContact();
            }
        });

        mCallType = (EditText) rootView.findViewById(R.id.call_type);
        mCallType.setInputType(InputType.TYPE_NULL);
        final String[] menuArray = getResources().getStringArray(R.array.call_types);
        mCallType.setText(menuArray[callType]);
        mCallType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Call Type:");
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.getListView();
                builder.setSingleChoiceItems(menuArray, callType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("SurveyList", "User selected " + which);
                        callType = which;
                        mCallType.setText(menuArray[which]);
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        Button submitButton = (Button) rootView.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDataSubmit();
            }
        });
        return rootView;
    }

    private void initTimeOfCallWithCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        updateDateOfCall(calendar);
        updateTimeOfCall(calendar);
    }

    static final int PICK_CONTACT = 1;

    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    //code
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final EditText phoneInput = mPhoneNumber; // (EditText) findViewById(R.id.phoneNumberInput);
            Cursor cursor = null;
            String phoneNumber = "";
            List<String> allNumbers = new ArrayList<String>();
            int phoneIdx = 0;
            try {
                Uri result = data.getData();
                String id = result.getLastPathSegment();
                cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        phoneNumber = cursor.getString(phoneIdx);
                        allNumbers.add(phoneNumber);
                        cursor.moveToNext();
                    }
                } else {
                    Toast.makeText(getActivity(), "There are no phone number associated with this contact [1]", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Some error happened while getting details for this contact.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }

                final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose a number");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedNumber = items[item].toString();
                        selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", "").replace(" ", "");
                        phoneInput.setText(selectedNumber);
                    }
                });
                AlertDialog alert = builder.create();
                if (allNumbers.size() > 1) {
                    alert.show();
                } else {
                    String selectedNumber = phoneNumber.toString();
                    selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", "").replace(" ", "");
                    phoneInput.setText(selectedNumber);
                }

                if (phoneNumber.length() == 0) {
                    Toast.makeText(getActivity(), "There are no phone number associated with this contact [2]", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        year = month = day = hourOfDay = minute = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        initTimeOfCallWithCurrentTime();
    }

    private static void updateDateOfCall(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        mDateOfCall.setText(dateFormatter.format(calendar.getTime()));
    }

    private static void updateTimeOfCall(Calendar calendar) {
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        am_pm = calendar.get(Calendar.AM_PM);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        mTimeOfCall.setText(dateFormatter.format(calendar.getTime()));
    }

    private void attemptDataSubmit() {
        // Reset errors.
        mPhoneNumber.setError(null);

        // Store values at the time of the login attempt.
        String phoneNumber = mPhoneNumber.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid name, if the user entered one.
        if (TextUtils.isEmpty(phoneNumber.trim())) {
            mPhoneNumber.setError("The phone number is empty");
            focusView = mPhoneNumber;
            cancel = true;
        }

        int duration = 0;
        if (!cancel) {
            if (mCallDuration.getText().toString().equals(randomDurationText)) {
                Random r = new Random();
                duration = r.nextInt(RANDOM_DURATION_MAX_SECONDS - RANDOM_DURATION_MIN_SECONDS) + RANDOM_DURATION_MIN_SECONDS;
            } else {
                try {
                    Log.d(TAG, "mCallDuration.getText() = " + mCallDuration.getText());
                    duration = Integer.parseInt(mCallDuration.getText().toString().trim());
                } catch (NumberFormatException e) {
                    mPhoneNumber.setError("The duration should be a number");
                    focusView = mCallDuration;
                    cancel = true;
                }
            }
        }

        if (cancel) {
            // There was an error; don't attempt submit and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        } else {
            Calendar calendar = Calendar.getInstance();
            Log.d(TAG, "Schedule : Cached values - year = " + year + ", month = " + month
                    + ", day = " + day + ", hourOfDay = " + hourOfDay + ", minute = " + minute);
            calendar.set(year, month, day, hourOfDay, minute);
            Log.d(TAG, "Calendar for schedule = " + calendar);

            int callTypeToSet;
            switch (callType)
            {
                case 0:
                    callTypeToSet = CallLog.Calls.INCOMING_TYPE;
                    break;
                case 1:
                    callTypeToSet = CallLog.Calls.OUTGOING_TYPE;
                    break;
                case 2:
                    callTypeToSet = CallLog.Calls.MISSED_TYPE;
                    break;
                default:
                    callTypeToSet = CallLog.Calls.INCOMING_TYPE;
            }

            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.NUMBER, phoneNumber);
            values.put(CallLog.Calls.DATE, calendar.getTimeInMillis());
            values.put(CallLog.Calls.DURATION, duration);
            values.put(CallLog.Calls.TYPE, callTypeToSet);
            values.put(CallLog.Calls.NEW, 1);
            values.put(CallLog.Calls.CACHED_NAME, "");
            values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
            values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
            Log.d(TAG, "Inserting call log placeholder for " + phoneNumber);
            getActivity().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
            Toast.makeText(getActivity(), "The fake call log is successfully added!", Toast.LENGTH_SHORT).show();
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker if there is no date already set in DatePicker
            final Calendar c = Calendar.getInstance();
            if (year > 0 && month > 0 && day > 0) {
                c.set(year, month, day);
            }

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            FakeCallLogFragment.updateDateOfCall(newDate);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            if (hourOfDay > 0 && minute > 0) {
                c.set(year, month, day, hourOfDay, minute);
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day, hourOfDay, minute);
            FakeCallLogFragment.updateTimeOfCall(newDate);
        }
    }
}
