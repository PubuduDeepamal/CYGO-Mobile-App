package com.example.cygoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;

import android.os.Bundle;

import android.os.Handler;
import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import android.view.LayoutInflater;

import android.view.View;

import com.example.cygoapp.components.MainActivityFragments;
import com.example.cygoapp.models.RatingGiver;
import com.example.cygoapp.models.Ride;
import com.example.cygoapp.models.RideUser;
import com.example.cygoapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;


import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class activity_home extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    // NavigationDrawer menu
    DrawerLayout drawer;
    NavigationView navigationView;
    ProgressBar progressBar;
    TextView tv_rating, reviewAmount;

    RatingBar ratingBar;

    private ViewPager ridesViewPager;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private LinearLayout getRideBtn, offerRideBtn;
    private CollectionReference mRidesColRef = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference mUsersColRef = FirebaseFirestore.getInstance().collection("customers");
    private ArrayList<RideUser> bookedRideUserArrayList = new ArrayList<>();
    private ArrayList<RideUser> offeredRideUserArrayList = new ArrayList<>();
    private boolean complete;

    ImageView bookBackground;
    ImageView offerbackground;
    ImageView userAcc;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.mHandler = new Handler();
        m_Runnable.run();

        //NavigationDrawer
        drawer =  findViewById(R.id.drawer_layout);
        navigationView =  findViewById(R.id.nav_view);
        userAcc = findViewById(R.id.imgUsrAcc);
        progressBar = findViewById(R.id.progressBar);
        reviewAmount =findViewById(R.id.main_reviewsText);
        ratingBar = findViewById(R.id.main_ratingBar);

//        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        View headerView = navigationView.inflateHeaderView(R.layout.navi_header);
        TextView naviUser = (TextView) headerView.findViewById(R.id.navi_header_text);
        TextView naviEmail = (TextView) headerView.findViewById(R.id.navi_header_emailtext);
        TextView main_nameText = findViewById(R.id.main_nameText);


        progressBar.setVisibility(View.VISIBLE);

        Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(userAcc);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("customers").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        reviewAmount.setText("Reviews -"+user.getRatingAmount()+"-");
                        ratingBar.setRating(user.getRating());
                        complete = user.isProfileCreated();
                    }
                }
            }
        });

            main_nameText.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[0]);
            naviUser.setText(getString(R.string.navi_header_hello) + " " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            naviEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

            //Check if is some unreviewed ride and put red notification badge to menu icon
            RatingGiver.GetAmountOfReviews(FirebaseAuth.getInstance().getUid(), new RatingGiver.ReviewAmountCallback() {
                @Override
                public void doAfterGettingAmount(int amount) {
                    Log.d("NAVI", "onCreate: "  + amount);
                    if(amount > 0){
                        TextView badgeNumber = (TextView) findViewById(R.id.main_menu_badge);
                        badgeNumber.setText(String.valueOf(amount));
                        badgeNumber.setVisibility(View.VISIBLE);

                        LayoutInflater li = LayoutInflater.from(activity_home.this);
                        tv_rating = (TextView)li.inflate(R.layout.rating_badge,null);
                        navigationView.getMenu().findItem(R.id.nav_rating).setActionView(tv_rating);


                        tv_rating.setText(String.valueOf(amount));
                    }
                }
            });

        navigationView.setNavigationItemSelectedListener(this);


        //setting up buttons
        getRideBtn = findViewById(R.id.main_btnGetRide);
        offerRideBtn = findViewById(R.id.main_btnOfferRide);

        bookBackground = findViewById(R.id.bookBackground);
        offerbackground = findViewById(R.id.offerBackground);

        bookBackground.setImageAlpha(128);

        bookedRideUserArrayList.clear();
        offeredRideUserArrayList.clear();

        FirebaseAuth.AuthStateListener als = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // If user has logged in
                    Log.d("TAG", "onAuthStateChanged: true");
                    // Checking that user has created a profile
                    // Sends user to profile edit if not
                    //start loading rides to view pager if logged in.
                    loadBookedRides();
                } else {
                    // If user has logged out
                    Log.d("TAG", "onAuthStateChanged: false");
                    //skips the ride loading if not signed in.
                    initMainLayoutItems();
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(als);


    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            activity_home.this.mHandler.postDelayed(m_Runnable, 5000);
        }

    };//runnable


    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(m_Runnable);
        finish();

    }

    public void AppSettings(View v) {

        drawer.openDrawer(Gravity.RIGHT);
    }

    public void SelectBookedTrips(View v) {
        bookBackground.setImageAlpha(255);
        offerbackground.setImageAlpha(128);
        ridesViewPager.setCurrentItem(1);
    }

    public void SelectOfferedTrips(View v) {
        bookBackground.setImageAlpha(128);
        offerbackground.setImageAlpha(255);
        ridesViewPager.setCurrentItem(0);
    }

    public void SelectGetARide(View v) {
        if(complete == true){
            Intent GetRideIntent = new Intent(activity_home.this, activity_go.class);
            startActivity(GetRideIntent);
        }else{
            Toast.makeText(activity_home.this, "Please complete the profile in profile", Toast.LENGTH_LONG).show();
        }
    }

    public void SelectOfferARide(View v) {
        if(complete == true){
            Intent SetRideIntent = new Intent(activity_home.this, activity_drive.class);
            startActivity(SetRideIntent);
        }else{
            Toast.makeText(activity_home.this, "Please complete the profile in profile", Toast.LENGTH_LONG).show();
        }
    }

    public void SelectProfile(View v) {
        Intent SetRideIntent = new Intent(activity_home.this, activity_user_details.class);
        startActivity(SetRideIntent);
    }

    public void SelectRating(View v) {
        if(complete == true){
            Intent RatingIntent = new Intent(activity_home.this, activity_rating.class);
            startActivity(RatingIntent);
        }else{
            Toast.makeText(activity_home.this, "Please complete the profile in profile", Toast.LENGTH_LONG).show();
        }
    }



    //first get rideId's from current user
    //then get rides
    //then get users

    private int NUMBER_OF_BOOKED_TASKS = 0;
    private int bookedCounter = 0;

    private void loadBookedRides() {
        final ArrayList<String> curUserRides = new ArrayList<>();
        Task<DocumentSnapshot> curUserTask = mUsersColRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    try{
                        curUserRides.addAll((ArrayList<String>) task.getResult().get("bookedRides"));
                        NUMBER_OF_BOOKED_TASKS = curUserRides.size();
                        Log.d("TAG", "12313123: ");
                        progressBar.setVisibility(View.GONE);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        loadOfferedRides();
                        progressBar.setVisibility(View.GONE);
                    }
                }
                else{
                    //task is not successful
                    loadOfferedRides();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        Tasks.whenAll(curUserTask).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    try{
                        if(curUserRides.size() > 0){
                            for(int i = 0; i < curUserRides.size(); i++){
                                mRidesColRef.document(curUserRides.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            final Ride ride = task.getResult().toObject(Ride.class);
                                            final String rideId = task.getResult().getId();
                                            mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()) {
                                                        User user = task.getResult().toObject(User.class);
                                                        bookedRideUserArrayList.add(new RideUser(ride, user, rideId));
                                                        taskCompletedBookedRides();
                                                    }
                                                    else {
                                                        //task is not successful
                                                        loadOfferedRides();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            //task is not successful
                                            loadOfferedRides();
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            //no booked rides
                            loadOfferedRides();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        loadOfferedRides();
                    }
                }
                else{
                    //task is not successful
                    loadOfferedRides();
                }
            }
        });
    }

    private synchronized void taskCompletedBookedRides() {
        bookedCounter ++;
        if(bookedCounter == NUMBER_OF_BOOKED_TASKS){
            loadOfferedRides();
        }
    }

    private static int NUMBER_OF_OFFERED_TASKS = 0;
    private int offeredCounter = 0;

    private void loadOfferedRides() {
        mRidesColRef.whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    try {
                        NUMBER_OF_OFFERED_TASKS = task.getResult().size();
                        if (NUMBER_OF_OFFERED_TASKS > 0) {
                            Log.d("TAG", "onComplete task size: " + NUMBER_OF_OFFERED_TASKS);
                            try {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    final Ride ride = doc.toObject(Ride.class);
                                    final String rideId = doc.getId();

                                    mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot doc = task.getResult();
                                                User user = doc.toObject(User.class);
                                                offeredRideUserArrayList.add(new RideUser(ride, user, rideId));
                                                Log.d("TAG", "onComplete: ");
                                                taskCompletedOfferedRides();
                                            } else {
                                                //task is not successful
                                                initMainLayoutItems();
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                taskCompletedOfferedRides();
                            }
                        } else {
                            initMainLayoutItems();
                            //No offered rides
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        initMainLayoutItems();
                    }
                }
                else{
                    initMainLayoutItems();
                    //task is not successful
                }
            }
        });
    }

    private synchronized void taskCompletedOfferedRides() {
        offeredCounter ++;
        if(NUMBER_OF_OFFERED_TASKS == offeredCounter) {
            initMainLayoutItems();
        }
    }

    private void initMainLayoutItems() {
        //setting up viewpager, viewpager header and adapter
        Log.d("TAG", "initMainLayoutItems: ");
        ridesViewPager = findViewById(R.id.main_viewPager);
        fragmentPagerAdapter = new RidesViewPagerAdapter(getSupportFragmentManager());
        fragmentPagerAdapter.notifyDataSetChanged();
        ridesViewPager.setAdapter(fragmentPagerAdapter);
        //page change listener is for header layout background color change
        ridesViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0)
                {
                    bookBackground.setImageAlpha(128);
                    offerbackground.setImageAlpha(255);
                }
                else{
                    bookBackground.setImageAlpha(255);
                    offerbackground.setImageAlpha(128);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //Exits application or navigationDrawer when pressed back
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.RIGHT)){
            drawer.closeDrawer(Gravity.RIGHT);
        }else {
            super.onBackPressed();
            finishAffinity();
        }
    }

    // NavigationDrawer items functionality
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()){
            case R.id.nav_profile:
                Log.d("NAVI", "onNavigationItemSelected: PROFILE");
                    drawer.closeDrawer(Gravity.RIGHT);
                    SelectProfile(null);
                break;

            case R.id.nav_rating:
                Log.d("NAVI", "onNavigationItemSelected: RATING ");

                    drawer.closeDrawer(Gravity.RIGHT);
                    SelectRating(null);
                break;

            case R.id.nav_sign:
                drawer.closeDrawer(Gravity.RIGHT);

                    Log.d("NAVI", "onNavigationItemSelected: LOGOUT?? ");
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(activity_home.this, "Logged Out",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(activity_home.this,activity_sign_in.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                break;

        }
        return false;
    }

    //viewpager for rides
    public class RidesViewPagerAdapter extends FragmentPagerAdapter {
        //page count is 2, booked and offered rides.
        private static final int PAGE_COUNT = 2;
        View v;
        //tabs titles will always be booked rides and offered rides.
        private final String[] tabTitles = new String[]{getResources().getString(R.string.main_fragment_tab_booked), getResources().getString(R.string.main_fragment_tab_offered)};

        //constructor for viewpager
        public RidesViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem (int position){
            /**
             * switch case in viewpager to know which data to show.
             * case 0 = booked rides.
             * case 1 = offered rides.
             * The integer is there to tell fragments what to print if the array list size is 0 in
             * MainActivityFragments.java
             */

            Log.d("TAG", "getItem: " + bookedRideUserArrayList.size() + " " + offeredRideUserArrayList.size());

            switch (position) {
                case 0:
                    return MainActivityFragments.newInstance(bookedRideUserArrayList, offeredRideUserArrayList, 0);

                case 1:
                    return MainActivityFragments.newInstance(bookedRideUserArrayList, offeredRideUserArrayList, 1);

                default:
                    return null;
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle ( int position){
            return tabTitles[position];
        }

        @Override
        public int getCount () {
            return PAGE_COUNT;
        }
    }
}