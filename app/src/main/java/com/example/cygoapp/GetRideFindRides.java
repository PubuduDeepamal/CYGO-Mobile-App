package com.example.cygoapp;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cygoapp.models.Ride;
import com.example.cygoapp.models.RideUser;
import com.example.cygoapp.models.User;
import com.example.cygoapp.util.AppMath;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

interface GetRideFindRideInterface {
    void FindRidesResult(ArrayList<RideUser> result);
    void FindRidesFailed(String report);
}

class GetRideFindRides {
    private static float startLat, startLng, destinationLat, destinationLng;
    private static long date1, date2;
    private static GetRideFindRideInterface getRideFindRideInterface;
    private final CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private final CollectionReference userReference = FirebaseFirestore.getInstance().collection("customers");
    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private static final String TAG = "FindRides";
    private static final int queryLimit = 100;
    private DocumentSnapshot lastVisible;
    private boolean foundRide = false;

    /**
     * Uses two constructors, the first one with only two params is for loop the database search if the array list size
     * is lower than we wanted. The array list contains all the ride and user data for each matching ride.
     * Second constructor is when the FindRides function is called first time. We get the user info like start point, destination and
     * date between two times. these are saved into static variables for the db search.
     * <p>
     * function findRides() is the deciding are we using getFirstQuery() or getNextQuery() function. It chooses the correct function based
     * on lastVisible variables (last seem document in database) and array list size.
     * <p>
     * The search() function is where the database search, algorithm and saving matching rides to array list happens.
     * First it takes query size to countOfTask integer, which is used to count all the documents.
     * After is match a ride, it fetches the user from database to that ride
     * if route is not in range, in bound, doesn't have free seat or task fails
     * is will reduce the count of task by 1 and then it will go to isDone() function
     * and that function is checking is all the tasks done and what the program
     * should do next, new db search or exit the search and show the results in UI
     */

    //use this call if you are looping
    GetRideFindRides(ArrayList<RideUser> rideUserArrayList, DocumentSnapshot lastVisible) {
        this.lastVisible = lastVisible;
        this.rideUserArrayList = rideUserArrayList;
    }

    //use when called first time to save algorithm objects
    GetRideFindRides(float startLat, float startLng, float destinationLat, float destinationLng,
                     long date1, long date2, GetRideFindRideInterface getRideFindRideInterface) {
        GetRideFindRides.startLat = startLat;
        GetRideFindRides.startLng = startLng;
        GetRideFindRides.destinationLat = destinationLat;
        GetRideFindRides.destinationLng = destinationLng;
        GetRideFindRides.date1 = date1;
        GetRideFindRides.date2 = date2;
        GetRideFindRides.getRideFindRideInterface = getRideFindRideInterface;
    }

    //function called when using the db search
    void findRides() {
        //first it will go to else condition to get the first query.
        //the first query will get data to lastVisible and after that
        //program will use getNextQuery, where we take next queryLimit much
        //data from database.
        if (lastVisible != null) {
            search(getNextQuery(lastVisible));
        } else {
            search(getFirstQuery());
        }

    }

    //the first query
    private Query getFirstQuery() {
        return rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime")
                .limit(queryLimit);
    }

    //the "next" query which uses startAfter, lastVisible was the last document seen in db last search
    private Query getNextQuery(final DocumentSnapshot lastVisible) {
        return rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime")
                .startAfter(lastVisible)
                .limit(queryLimit);
    }

    //Database search, algorithm to matching routes(use of appMath class) and listener if there is
    //new rides found in query
    private int countOfTasks;
    private void search(final Query query) {
        Log.d(TAG, "search, start latlng + destination latlng: " + startLat + " " + startLng + ", " + destinationLat + " " + destinationLng);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    final QuerySnapshot querySnapshot = task.getResult();
                    countOfTasks = task.getResult().size();
                    for(final QueryDocumentSnapshot rideDoc : task.getResult()){
                        try{
                            if((long) rideDoc.get("freeSlots") > 0){
                                if(AppMath.areCoordinatesWithinBounds(startLat, startLng, destinationLat, destinationLng, (HashMap<String, Double>) rideDoc.get("bounds"))) {
                                    if (AppMath.isRouteInRange((long) rideDoc.get("pickUpDistance"), startLat, startLng, destinationLat, destinationLng, (ArrayList<HashMap<String, Double>>) rideDoc.get("points"))) {
                                        final Ride ride = rideDoc.toObject(Ride.class);
                                        final String rideId = rideDoc.getId();
                                        Log.d(TAG, "onComplete: after first db");
                                        userReference.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot userDoc = task.getResult();
                                                    if (userDoc.exists()) {
                                                        User user = userDoc.toObject(User.class);
                                                        rideUserArrayList.add(new RideUser(ride, user, rideId));
                                                    } else {
                                                        //Doc doesn't exist
                                                    }
                                                } else {
                                                    //Task is not successful
                                                }
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                countOfTasks -= 1;
                                                isDone(querySnapshot, countOfTasks);
                                                Log.d(TAG, "onComplete: doc task complete: " + countOfTasks);
                                            }
                                        });
                                    } else {
                                        //Ride's route is not in range
                                        countOfTasks -= 1;
                                        isDone(querySnapshot, countOfTasks);
                                        Log.d(TAG, "Route is not in range: " + countOfTasks);
                                    }
                                } else {
                                    //Ride's route is not in bounds
                                    countOfTasks -= 1;
                                    isDone(querySnapshot, countOfTasks);
                                    Log.d(TAG, "Route is not in bounds: " + countOfTasks);
                                }
                            }
                            else{
                                //no free slots in that ride
                                countOfTasks -= 1;
                                isDone(querySnapshot, countOfTasks);
                                Log.d(TAG, "No free slots in ride: " + countOfTasks);
                            }
                        }
                        catch (Exception e){
                            //Exception
                            countOfTasks -= 1;
                            isDone(querySnapshot, countOfTasks);
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    if(getRideFindRideInterface != null){
                        getRideFindRideInterface.FindRidesFailed(task.getException().toString());
                    }
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().size() == 0){
                    isDone(task.getResult(), 0);
                }
            }
        });
    }

    private void isDone(QuerySnapshot qs, int count){
        //if there is no documents (rides) in query
        if(qs.size() == 0){
            Log.d(TAG, "isDone: qs size");
            GetRideFindRidesDone getRideFindRidesDone = new GetRideFindRidesDone(rideUserArrayList, getRideFindRideInterface, lastVisible, true);
            getRideFindRidesDone.execute();
        }
        //if there is documents (rides) and all the tasks are wrought
        else if(qs.size() != 0 && count == 0){
            Log.d(TAG, "isDone: qs.size != 0, count == 0");
            lastVisible = qs.getDocuments().get(qs.size() -1);
            if(!foundRide){
                GetRideFindRides getRideFindRides = new GetRideFindRides(rideUserArrayList, lastVisible);
                getRideFindRides.findRides();
            }
        }
        //if something fails
        else if(count == 0){
            Log.d(TAG, "isDone: count");
            GetRideFindRidesDone getRideFindRidesDone = new GetRideFindRidesDone(rideUserArrayList, getRideFindRideInterface, lastVisible, false);
            getRideFindRidesDone.execute();
        }
        //do nothing if any condition above is not succeeded
        else{
            //do nothing
            Log.d(TAG, "isDone: else");
        }
    }

}

class GetRideFindRidesDone extends AsyncTask<Void, Void, Boolean>{

    private ArrayList<RideUser> rideUserArrayList;
    private static final String TAG = "FindRideDone";
    private Boolean hasDone;
    private GetRideFindRideInterface getRideFindRideInterface;
    private DocumentSnapshot lastVisible;

    /** Use the arrayListMaxSize integer for the minimum array list size
     * Use value 1 if you don't want your db to get many search per time
     * and change queryLimit from GetRideFindRides class to match documents in db
     */

    private final int arrayListMinSize = 50;

    GetRideFindRidesDone(ArrayList<RideUser> rideUserArrayList, GetRideFindRideInterface getRideFindRideInterface, DocumentSnapshot lastVisible, boolean hasDone)
    {
        this.hasDone = hasDone;
        this.rideUserArrayList = rideUserArrayList;
        this.getRideFindRideInterface = getRideFindRideInterface;
        this.lastVisible = lastVisible;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        //if array list size is smaller than array list minimum size, or query size is 0 (hasDone will be true is query size is 0)
        //change hasDone to true, so we can pass data to activity using interface.
        //if the condition are not met, it will do the db search again.
        if(rideUserArrayList.size() >= arrayListMinSize || hasDone)
        {
            //array list size is bigger than minimum size or all the data from database has been wrought
            hasDone = true;
        }
        else
        {
            //There is need to run database search again
            GetRideFindRides getRideFindRides = new GetRideFindRides(rideUserArrayList, lastVisible);
            getRideFindRides.findRides();
        }
        return hasDone;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        super.onPostExecute(result);
        //if all the db data is ran through algorithm or the array list size is bigger than minimum value, do this.
        if(result)
        {
            if(getRideFindRideInterface != null)
            {
                Log.d(TAG, "onPostExecute: " + rideUserArrayList.size());
                getRideFindRideInterface.FindRidesResult(rideUserArrayList);
            }
        }
    }
}
