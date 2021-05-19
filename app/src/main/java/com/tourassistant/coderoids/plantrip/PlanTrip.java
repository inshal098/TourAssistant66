package com.tourassistant.coderoids.plantrip;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.tourassistant.coderoids.BaseActivity;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.appdb.AppDatabase;
import com.tourassistant.coderoids.appdb.DatabaseClient;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanTrip extends BaseActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        ImageButton ibCross = findViewById(R.id.ib_cross);
        Button ibCreateTrip = findViewById(R.id.create_trip);
        TextInputEditText etTripName = findViewById(R.id.et_name_trip);
        ibCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ibCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripName = etTripName.getText().toString();
                if (TextUtils.isEmpty(tripName)) {
                    Toast.makeText(PlanTrip.this, "You Must Enter a Trip Name", Toast.LENGTH_SHORT).show();
                } else {
                    saveTrip(tripName);
                    finish();
                }
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_plan_trip;
    }

    private void saveTrip(String tripName) {
        try {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = users.getUid();
                        TripEntity tripEntity = new TripEntity();
                        tripEntity.setTripTitle(tripName);
                        tripEntity.setFirebaseUserId(uid);
                        tripEntity.setCreatorName(users.getDisplayName());
                        tripEntity.setTripLocationTracking("0");
                        long tripId = AppDatabase.getAppDatabase(getApplicationContext()).tripDao().insertTrip(tripEntity);
                        String tripNameUnique = tripId +"_"+System.currentTimeMillis()+"_"+users.getEmail()+"_"+tripName;
                        tripEntity.setFirebaseId(tripNameUnique);
                        tripEntity.setId(tripId);
                        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                        rootRef.collection("Trips").document(uid).collection("UserTrips").document(tripNameUnique).set(tripEntity);
                    } catch (Exception ex){
                            ex.printStackTrace();
                    }

                }
            });
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void RequestFinished(String fragmentName, String apiName, String responseString) {

    }

    @Override
    public void RequestSecureFinished(String fragmentName, String apiName, String responseString) {

    }
}