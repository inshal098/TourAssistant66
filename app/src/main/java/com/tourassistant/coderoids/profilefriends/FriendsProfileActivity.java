package com.tourassistant.coderoids.profilefriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.NewsListingAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.LocationHelper;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.models.TripCurrentLocation;

import java.util.ArrayList;
import java.util.List;

public class FriendsProfileActivity extends AppCompatActivity {
    ShapeableImageView sivProfileImage;
    TextView tvName,tvLocationStatus;
    RadioGroup rgContentGroup;
    RadioButton rbtnLocation , rbtnPosts;
    RecyclerView rvNews;
    LinearLayoutManager llmNews;
    GoogleMap map;
    LinearLayout mapLayout;
    List<TripCurrentLocation> tripCurrentLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);
        sivProfileImage = findViewById(R.id.profile_image);
        rvNews = findViewById(R.id.rv_news_feed);
        llmNews = new LinearLayoutManager(this);
        llmNews.setOrientation(LinearLayoutManager.VERTICAL);
        tvName = findViewById(R.id.tv_name);
        tvLocationStatus = findViewById(R.id.location_status);
        rgContentGroup = findViewById(R.id.radio_content);
        rbtnLocation = findViewById(R.id.radio_location);
        mapLayout = findViewById(R.id.map_layout);
        rbtnPosts = findViewById(R.id.radio_posts);
        String userId = getIntent().getStringExtra("userId");
        fetchUserInformation(userId);
        int checkedId = rgContentGroup.getCheckedRadioButtonId();
        tvLocationStatus.setVisibility(View.GONE);
        manageState(checkedId);
        rgContentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                manageState(checkedId);
            }
        });
    }

    private void manageState(int checkedId) {
        switch (checkedId){
            case R.id.radio_posts:
                mapLayout.setVisibility(View.GONE);
                rvNews.setVisibility(View.VISIBLE);
                break;
            case R.id.radio_location:
                if(tripCurrentLocations != null && tripCurrentLocations.size()>0) {
                    rvNews.setVisibility(View.GONE);
                    mapLayout.setVisibility(View.VISIBLE);
                } else{
                    mapLayout.setVisibility(View.GONE);
                    tvLocationStatus.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void manageMap() {
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.friends_map);
        if (map == null) {
            // Getting Map for the SupportMapFragment
            fm.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mGoogleMap) {
                    map = mGoogleMap;
                    populateMap();
                }
            });
        }
    }

    private void populateMap() {
        if(tripCurrentLocations != null && tripCurrentLocations.size()>0) {
            tvLocationStatus.setVisibility(View.GONE);
            int locationSize = tripCurrentLocations.size();
            TripCurrentLocation tripCurrentLocation = tripCurrentLocations.get(locationSize -1);
            LocationHelper locationManager = LocationHelper.getInstance();
            double latitude = Double.parseDouble(tripCurrentLocation.getLatitude());
            double longitude = Double.parseDouble(tripCurrentLocation.getLongitude());
            LatLng currentLocation = new LatLng(latitude, longitude);

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setRotateGesturesEnabled(true);
            map.getUiSettings().setZoomGesturesEnabled(true);
            currentLocation = new LatLng(latitude, longitude);
            //destination = AppHelper.tripRoomPlace.get(0).getLatLng();

            MarkerOptions marker = new MarkerOptions().position(currentLocation).title("Last Known Location");
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(marker.getPosition());
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen


            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 8));
            // addMarker(currentLocation,dest);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude)).zoom(15).build();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
            // addMarker();
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {

                }
            });
        } else
            tvLocationStatus.setVisibility(View.VISIBLE);
    }

    private void fetchUserInformation(String userId) {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isComplete()){
                    Profile profile = task.getResult().toObject(Profile.class);
                    if(profile.getProfileImage() != null){
                        byte [] bytes=   profile.getProfileImage().toBytes();
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        sivProfileImage.setImageBitmap(bmp);
                        tvName.setText(profile.getDisplayName());
                    }
                }
            }
        });

        rootRef.collection("Users").document(userId).collection("NewsFeed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isComplete()){
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (documentSnapshots != null) {
                        documentSnapshots.size();
                        NewsListingAdapter newsListingAdapter = new NewsListingAdapter(FriendsProfileActivity.this, documentSnapshots);
                        rvNews.setAdapter(newsListingAdapter);
                        rvNews.setLayoutManager(llmNews);
                    }
                }
            }
        });

        rootRef.collection("Users").document(userId).collection("locationTracking").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isComplete()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (documentSnapshots != null) {
                        tripCurrentLocations = new ArrayList<>();
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            TripCurrentLocation tripCurrentLocation = documentSnapshots.get(i).toObject(TripCurrentLocation.class);
                            tripCurrentLocations.add(tripCurrentLocation);
                        }
                        manageMap();
                    } else {
                        tvLocationStatus.setVisibility(View.VISIBLE);
                        mapLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}