package com.example.cygoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.cygoapp.helper.CalendarHelper;
import com.example.cygoapp.models.Ride;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class activity_set_ride_details extends AppCompatActivity implements Serializable, View.OnClickListener {

    //activity_set_ride ride data
    private List<HashMap<String,Double>> selectedPoints;
    private String startAddress, endAddress, duration, distance, startCity, endCity;

    private Calendar mC;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private int pickedYear, pickedMonth, pickedDate, pickedHour, pickedMinute;
    private String strDate, strTime;
    private long leaveTime;

    private float price = 0.00f;
    int pickUpDistance = 10;
    int passengers = 1;
    int minRangeInt = 20;

    Double doubleDistance;
    int intDistance;

    private CheckBox checkBox_time, checkBox_luggage, checkBox_pets;
    private Boolean pets;

    private HashMap<String,Double> bounds;


    FirebaseAuth authProfile;
    FirebaseUser firebaseUser;


    TextView priceTxt, examplePriceTxt, fetchRange, rangeValueTextView;
    EditText txtDate, txtTime, lahtoaikaSanallinen, matkatavaraSanallinen;
    private String departureTimeTxt, luggageTxt;
    NumberPicker numberPicker;
    SeekBar seekBar3, seekBar2, seekBar;
    Button confirmBtn;
    ImageButton timeBtn, luggageBtn, picUpBtn, rangeBtn, priceBtn;

    private Calendar calendar;
    private long systemTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride_details);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mC = new GregorianCalendar();

        initCalendarTimes();
        //get data from activity_drive
        if(savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
            } else {
                startAddress = extras.getString("STRSTART");
                endAddress = extras.getString("STRDEST");
                startCity = extras.getString("STARTCITY");
                endCity = extras.getString("ENDCITY");
                distance = extras.getString("DISTANCE");
                duration = extras.getString("DURATION");
                selectedPoints = (List<HashMap<String, Double>>) extras.getSerializable("POINTS");
                bounds = (HashMap<String,Double>) extras.getSerializable("BOUNDS");
            }
        }else {
            startAddress = (String) savedInstanceState.getSerializable ("STRSTART");
            endAddress = (String) savedInstanceState.getSerializable ("STRDEST");
            startCity = (String) savedInstanceState.getSerializable("STARTCITY");
            endCity = (String) savedInstanceState.getSerializable("ENDCITY");
            distance = (String) savedInstanceState.getSerializable ("DISTANCE");
            duration = (String) savedInstanceState.getSerializable ("DURATION");
            selectedPoints = (List<HashMap<String,Double>>) savedInstanceState.getSerializable("POINTS");
            bounds = (HashMap<String, Double>) savedInstanceState.getSerializable("BOUNDS");
        }

        //Muuttaa string distancen eri muuttujatyypeiksi
        if(distance != null && !distance.isEmpty()){
            doubleDistance = Double.valueOf(distance);
            intDistance = Integer.valueOf(doubleDistance.intValue());
        }

        //CheckBoxit
        checkBox_time = (CheckBox) findViewById(R.id.setRideDetails_checkBox_aika);
        checkBox_luggage = (CheckBox) findViewById(R.id.setRideDetails_checkBox_matkatavarat);
        checkBox_pets = (CheckBox) findViewById(R.id.setRideDetails_checkBox_lemmikit);
        checkBox_time.setOnClickListener(this);
        checkBox_luggage.setOnClickListener(this);
        checkBox_pets.setOnClickListener(this);

        //Buttonit
        confirmBtn = (Button) findViewById(R.id.setRideDetails_button_vahvista);
        confirmBtn.setOnClickListener(this);
        confirmBtn.setEnabled(false);

        //ImageButtonit
        timeBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_lahtoaika);
        luggageBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_tavaratila);
        picUpBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_nouto);
        rangeBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_matka);
        priceBtn = (ImageButton) findViewById(R.id.setRideDetails_imageBtn_hinta);
        timeBtn.setOnClickListener(this);
        luggageBtn.setOnClickListener(this);
        picUpBtn.setOnClickListener(this);
        rangeBtn.setOnClickListener(this);
        priceBtn.setOnClickListener(this);

        //Editorit
        txtDate = (EditText) findViewById(R.id.setRideDetails_editText_date);
        txtTime = (EditText) findViewById(R.id.setRideDetails_editText_time);
        lahtoaikaSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenAika);
        matkatavaraSanallinen = (EditText)findViewById(R.id.setRideDetails_editText_sanallinenTavaratila);
        txtDate.setOnClickListener(this);
        txtTime.setOnClickListener(this);

        //TextWatcherit varmistaa että lähtöpäivä ja -aika on määritetty
        txtDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!txtTime.getText().toString().equals(""))
                {
                    confirmBtn.setEnabled(true);
                }else{
                    confirmBtn.setEnabled(false);
                }
            }
        });
        txtTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!txtDate.getText().toString().equals(""))
                {
                    confirmBtn.setEnabled(true);
                }else{
                    confirmBtn.setEnabled(false);
                }
            }
        });

        //TextViewit
        priceTxt = (TextView) findViewById(R.id.setRideDetails_textView_hinta);
        examplePriceTxt = (TextView) findViewById(R.id.setRideDetails_textView_examplePrice);
        fetchRange = (TextView) findViewById(R.id.setRideDetails_textView_noutoEtaisyys);
        rangeValueTextView = (TextView) findViewById(R.id.setRideDetails_textView_minmatka);

        examplePriceTxt.setText(getString(R.string.setridedetails_distance_example) + " " +  distance + " km \n"
                + getString(R.string.setridedetails_price_example) + String.format("%.2f", doubleDistance * 0.00) + " LKR");
        rangeValueTextView.setText(getString(R.string.setridedetails_ride_length_text) + " " +  minRangeInt + "km");
        fetchRange.setText(getString(R.string.setridedetails_max_pickup_dist_text) + " " +  pickUpDistance + "km");
        priceTxt.setText(String.format(getString(R.string.setridedetails_price_example) + " " +  "%.2f", price) + getString(R.string.setridedetails_km_example));

        //Number Picker
        numberPicker = findViewById(R.id.setRideDetails_numberPicker_passengers);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                passengers = newVal;
            }
        });


        //Seekbar onBarChangeListener
        seekBar = (SeekBar) findViewById(R.id.setRideDetails_seekbar_nouto);
        seekBar2 = (SeekBar) findViewById(R.id.setRideDetails_seekbar_matka);
        seekBar3 = (SeekBar) findViewById(R.id.setRideDetails_seekbar_hinta);

        //Seek Bar(pickUpDistance)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float tempDist = ((50 / 100.00f) * progress);
                pickUpDistance = (int) tempDist;
                //Log.d("####matka3####", range + ", " + intMatka + ", " + progress + ", " + (intMatka / 100.00f) * progress);
                fetchRange.setText(getString(R.string.setridedetails_max_pickup_dist_text) + " " + pickUpDistance + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Seek Bar(TravelDistance)
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minRangeInt = (int) ((intDistance / 100.00f) * progress);
                DecimalFormat df = new DecimalFormat("#.##");
                rangeValueTextView.setText(getString(R.string.setridedetails_ride_length_text) + " " +  minRangeInt + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Seekbar (Price)
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                price = ((float) progress / 1);
                priceTxt.setText(getString(R.string.setridedetails_price_hint) + " " + String.format("%.2f", price) + getString(R.string.setridedetails_km_example));
                examplePriceTxt.setText(getString(R.string.setridedetails_distance_example) + " " +  distance + " km \n" + getString(R.string.setridedetails_price_example) + " " +  String.format("%.2f", doubleDistance * price) + " LKR");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    private void initCalendarTimes()
    {
        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        strDate = String.valueOf(CalendarHelper.getDayString(systemTime) + "-" + CalendarHelper.getMonthString(systemTime) +
                "-" + CalendarHelper.getYearString(systemTime));

        strTime = String.valueOf(CalendarHelper.getHourString(systemTime) + ":" + CalendarHelper.getMinuteString(systemTime));


    }

    @Override
    public void onClick(View v) {
        //when you press DateEditText, it will open datePickerDialog where you can select date in dd-MM-yyyy
        if(v == txtDate){
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity_set_ride_details.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(year, month, dayOfMonth);
                    strDate = new SimpleDateFormat("dd-MM-yyyy").format(calendar.getTime());
                    txtDate.setText(strDate);
                    pickedYear = calendar.get(Calendar.YEAR);
                    pickedMonth = calendar.get(Calendar.MONTH);
                    pickedDate = calendar.get(Calendar.DAY_OF_MONTH);
                }
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        //when you click TimeText, it will popup timePickerDialog, where you set hours and minutes
        else if (v == txtTime){
            TimePickerDialog timePickerDialog = new TimePickerDialog(activity_set_ride_details.this, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;

                    String format = "%1$02d";
                    String estHour = String.format(format, hourOfDay);
                    String estMinute = String.format(format, minute);
                    strTime = estHour + ":" + estMinute;
                    txtTime.setText(strTime);
                }
            }, mHour, mMinute, true);
            timePickerDialog.show();
        }

        else if (v == timeBtn)
        {
            AlertDialog.Builder time = new AlertDialog.Builder(activity_set_ride_details.this);
            time.setTitle("Departure Time may Change");
            time.setMessage("\n" +
                    "Etkö tiedä lähtöaikaasi, ei hätää! Voit merkata lähtöaika kenttään karkean arvion lähtöajastasi ja kirjoittaa huomio kenttään viestin, mikä näkyy matkustajille kyytejä etsiessä\n" +
                    "Don't know your departure time, no problem! You can mark a rough estimate of your departure time in the Departure time field and write a message in the note field, which will be visible to passengers when looking for rides");
            time.setCancelable(true);

            time.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = time.create();
            alert11.show();
        }
        else if (v == luggageBtn)
        {
            AlertDialog.Builder luggage = new AlertDialog.Builder(activity_set_ride_details.this);
            luggage.setTitle("Limited Luggage Space");
            luggage.setMessage("If your luggage space is limited, you can write a message in the field for people booking rides, for example: The ride can only accommodate luggage that can be carried on your lap");
            luggage.setCancelable(true);

            luggage.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = luggage.create();
            alert11.show();
        }
        else if (v == picUpBtn)
        {
            AlertDialog.Builder time = new AlertDialog.Builder(activity_set_ride_details.this);
            time.setTitle("Pickup Distance");
            time.setMessage("Choose how much you can deviate from the route when picking up a passenger.");
            time.setCancelable(true);

            time.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = time.create();
            alert11.show();
        }
        else if (v == rangeBtn)
        {
            AlertDialog.Builder range = new AlertDialog.Builder(activity_set_ride_details.this);
            range.setTitle("Travel distance");
            range.setMessage("You can set a kilometer limit which passenger must travel with you at least.");
            range.setCancelable(true);

            range.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = range.create();
            alert11.show();
        }
        else if (v == priceBtn)
        {
            AlertDialog.Builder price = new AlertDialog.Builder(activity_set_ride_details.this);
            price.setTitle("Price per kilometer");
            price.setMessage("Specify the price per kilometer you want. The total price of the trip is determined by the length of the rider's trip and the price per kilometer. In the example box, you can see how much the trip for one rider would cost for the entire length of the trip you indicated. NOTE! Please specify the price only for fuel distribution.");
            price.setCancelable(true);

            price.setNeutralButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert11 = price.create();
            alert11.show();
        }

        if(checkBox_time.isChecked()) {
            lahtoaikaSanallinen.setVisibility(View.VISIBLE);
        } else {
            lahtoaikaSanallinen.setVisibility(View.GONE);
            lahtoaikaSanallinen.setText("");
            departureTimeTxt = "";
        }
        if(checkBox_luggage.isChecked()) {
            matkatavaraSanallinen.setVisibility(View.VISIBLE);
        } else {
            matkatavaraSanallinen.setVisibility(View.GONE);
            matkatavaraSanallinen.setText("");
            luggageTxt = "";
        }
        if(checkBox_pets.isChecked()) {
            pets = true;
        } else {
            pets = false;
        }
        if(v.getId() == R.id.setRideDetails_button_vahvista)
        {

            mC.set(pickedYear, pickedMonth, pickedDate, pickedHour, pickedMinute);
            leaveTime = mC.getTimeInMillis();

            departureTimeTxt = lahtoaikaSanallinen.getText().toString();
            luggageTxt = matkatavaraSanallinen.getText().toString();

            String departure = "";
            String luggage = "";
            String sPets = "";
            if(departureTimeTxt.length() == 0){ departureTimeTxt = null; }else {
                departure = "Departure time message: " + departureTimeTxt;
            }
            if(luggageTxt.length() == 0) { luggageTxt = null; }else {
                luggage =  "Baggage message: " + luggageTxt;
            }
            if(pets == true){
                sPets = "Allowable";
            }else{
                sPets = "Not allowed";
            }

                AlertDialog.Builder confirm = new AlertDialog.Builder(activity_set_ride_details.this);
                confirm.setTitle("Check the information and confirm");
                confirm.setMessage("Departure date: " + strDate + "\nDeparture time: " + strTime + "\nAvailable seats: " + passengers + "\nPickup distance: " + pickUpDistance + " km" +
                        "\nTravel distance: " + minRangeInt + " km" + "\nPrice per km: " + price + "\nPets Allowed: " + sPets + "\n" + departure + "\n" + luggage + "\n\nIs the information correct? " );
                confirm.setCancelable(true);

                confirm.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CREATE_RIDE_DEMO();
                            }
                        }
                );
                confirm.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                );
                AlertDialog alert11 = confirm.create();
                alert11.show();

        }
    }

    // now only creates rides with proper points and user but otherwise random values
    public void CREATE_RIDE_DEMO()
    {
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        Ride r = new Ride(firebaseUser.getUid(), duration, leaveTime,
                startAddress, endAddress, passengers, price, doubleDistance,
                selectedPoints, bounds, new ArrayList<String>(),
                new ArrayList<String>(), pickUpDistance, startCity, endCity, departureTimeTxt, luggageTxt, pets);

            FirebaseFirestore.getInstance().collection("rides").add(r).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful())
                {
                    // Ride creation successful
                    showCustomDialogSuccessful();
                }
                else {
                    // Ride create failed
                    showCustomDialogFailed();
                }
            }
        });

    }

    private void showCustomDialogSuccessful() {
        ViewGroup viewGroup =  findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.my_alertdialog, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        Button okBtn = (Button) dialogView.findViewById(R.id.my_alertdialog_buttonOk);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), activity_home.class);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }

    private void showCustomDialogFailed() {
        ViewGroup viewGroup2 =  findViewById(android.R.id.content);
        View dialogView2 = LayoutInflater.from(this).inflate(R.layout.my_alertdialog2, viewGroup2, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView2);
        AlertDialog alertDialog2 = builder.create();
        Button okBtn2 = (Button) dialogView2.findViewById(R.id.my_alertdialog2_buttonOk);
        okBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), activity_home.class);
                startActivity(intent);
            }
        });
        alertDialog2.show();
    }
}