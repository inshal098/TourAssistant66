package com.tourassistant.coderoids.profilefriends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.tourassistant.coderoids.adapters.PersonalPicturesUploads;
import com.tourassistant.coderoids.adapters.PortfolioAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.LocationHelper;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.models.TripCurrentLocation;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsProfileActivity extends AppCompatActivity {
    CircleImageView sivProfileImage;
    TextView tvName,tvLocationStatus ,tvFollowersCount ,tvFollowingCount,tvPostCount,tvIntrest ,tvDescription ,website ,tvPersonalPictures ,tvTripsPicture;
    RadioGroup rgContentGroup;
    RadioButton rbtnLocation , rbtnPosts;
    RecyclerView rvNews ,rvTripPhoto;
    LinearLayoutManager llmNews;
    GoogleMap map;
    LinearLayout mapLayout;
    List<TripCurrentLocation> tripCurrentLocations;
    Button locationCheckMap ,reviewUser;
    ImageButton ibCross;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);
        sivProfileImage = findViewById(R.id.profile_photo);
        rvNews = findViewById(R.id.rv_news_feed);
        tvFollowersCount = findViewById(R.id.tv_followers_count);
        reviewUser = findViewById(R.id.review_user);
        tvFollowingCount = findViewById(R.id.tv_following_count);
        tvPostCount = findViewById(R.id.tv_post_count);
        locationCheckMap = findViewById(R.id.location_check_map);
        tvIntrest = findViewById(R.id.prefs);
        tvDescription = findViewById(R.id.description);
        website = findViewById(R.id.website);
        tvLocationStatus = findViewById(R.id.location_status);

        llmNews = new LinearLayoutManager(this);
        llmNews.setOrientation(LinearLayoutManager.VERTICAL);
        tvName = findViewById(R.id.user_name);
        tvPersonalPictures = findViewById(R.id.pt_);
        tvTripsPicture = findViewById(R.id.pt_trip);
        rvTripPhoto = findViewById(R.id.rv_trip_photos);
        mapLayout = findViewById(R.id.map_layout);
        ibCross = findViewById(R.id.ib_cross);
       // rbtnPosts = findViewById(R.id.radio_posts);
        String userId = getIntent().getStringExtra("userId");
        fetchUserInformation(userId);

        tvPersonalPictures.setBackgroundColor(getResources().getColor(R.color.appTheme2));
        tvPersonalPictures.setTextColor(getResources().getColor(R.color.white));
        rvNews.setVisibility(View.VISIBLE);
        rvTripPhoto.setVisibility(View.GONE);
        tvPersonalPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPersonalPictures.setBackgroundColor(getResources().getColor(R.color.appTheme2));
                tvPersonalPictures.setTextColor(getResources().getColor(R.color.white));

                tvTripsPicture.setBackgroundColor(getResources().getColor(R.color.white));
                tvTripsPicture.setTextColor(getResources().getColor(R.color.black));
                rvNews.setVisibility(View.VISIBLE);
                rvTripPhoto.setVisibility(View.GONE);
            }
        });

        tvTripsPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTripsPicture.setBackgroundColor(getResources().getColor(R.color.appTheme2));
                tvTripsPicture.setTextColor(getResources().getColor(R.color.white));
                tvPersonalPictures.setBackgroundColor(getResources().getColor(R.color.white));
                tvPersonalPictures.setTextColor(getResources().getColor(R.color.black));
                rvNews.setVisibility(View.GONE);
                rvTripPhoto.setVisibility(View.VISIBLE);
            }
        });
        mapLayout.setVisibility(View.GONE);
        locationCheckMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapLayout.setVisibility(View.VISIBLE);
            }
        });

        ibCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapLayout.setVisibility(View.GONE);
            }
        });

        reviewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
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
                    try {
                        Profile profile = task.getResult().toObject(Profile.class);
                        if(profile.getProfileImage() != null){
                            byte [] bytes=   profile.getProfileImage().toBytes();
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            sivProfileImage.setImageBitmap(bmp);
                            tvName.setText(profile.getDisplayName());
                            tvPostCount.setText(profile.getTotalPosts());
                            tvFollowersCount.setText(profile.getFollowers());
                            tvFollowingCount.setText(profile.getFollowing());
                            tvDescription.setText(profile.getAboutDescription());
                            website.setText(profile.getWebsite());
                            if(profile.getInterests() != null && !profile.getInterests().toString().matches(""))
                                tvIntrest.setText(AppHelper.getUserIntrests(new JSONArray(profile.getInterests())));
                        }
                    } catch (JSONException ex){
                        ex.printStackTrace();
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
                        PersonalPicturesUploads newsListingAdapter = new PersonalPicturesUploads(FriendsProfileActivity.this, documentSnapshots);
                        rvNews.setAdapter(newsListingAdapter);
                        rvNews.setLayoutManager(new GridLayoutManager(FriendsProfileActivity.this, 3));;
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
                    }
                }
            }
        });

        rootRef.collection("Trips").document(userId)
                .collection("UserTrips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (documentSnapshots != null) {
                        PortfolioAdapter adapter = new PortfolioAdapter(FriendsProfileActivity.this, documentSnapshots);
                        rvTripPhoto.setAdapter(adapter);
                        rvTripPhoto.setLayoutManager(new GridLayoutManager(FriendsProfileActivity.this, 3));
//                            documentSnapshots.size();
//
                    }
                }
            }
        });
    }
}