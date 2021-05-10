package com.tourassistant.coderoids.starttrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.PreDashBoardActivity;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.ForecastTripWeatherAdapter;
import com.tourassistant.coderoids.adapters.NewsListingAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.home.fragments.FilterPublicTrips;
import com.visuality.f32.temperature.Temperature;
import com.visuality.f32.temperature.TemperatureUnit;
import com.visuality.f32.weather.data.entity.Forecast;
import com.visuality.f32.weather.data.entity.Weather;
import com.visuality.f32.weather.manager.WeatherManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ForecastTripActivity extends AppCompatActivity {
    ForecastTripWeatherAdapter forecastTripWeatherAdapter;
    RecyclerView rvWeatherForecast, rvNewsSection;
    LinearLayoutManager llForecastMan, llNewsMan;
    LatLng destinationLatLng;
    TextView tvAddWeather;
    private int AUTOCOMPLETE_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_trip);

        rvWeatherForecast = findViewById(R.id.weather_forecast);
        rvNewsSection = findViewById(R.id.news_section);
        tvAddWeather = findViewById(R.id.weather_forecast_add);
        llForecastMan = new LinearLayoutManager(getApplicationContext());
        llForecastMan.setOrientation(RecyclerView.HORIZONTAL);

        llNewsMan = new LinearLayoutManager(getApplicationContext());
        llNewsMan.setOrientation(RecyclerView.VERTICAL);

        destinationLatLng = AppHelper.tripRoomPlace.get(0).getLatLng();

        fetchWeather();

        fetchTripNews();
        tvAddWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                        Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                        Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);
                List<String> countriesArr = new ArrayList<>();
                countriesArr.add("PK");
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setCountries(countriesArr)
                        .build(ForecastTripActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });
    }

    private void fetchWeather() {
        new WeatherManager(getResources().getString(R.string.weather_forecast_id)).getFiveDayForecastByCoordinates(
                destinationLatLng.latitude, // latitude
                destinationLatLng.longitude, // longitude
                new WeatherManager.ForecastHandler() {
                    @Override
                    public void onReceivedForecast(WeatherManager manager, Forecast forecast) {
                        List<Double> list = new ArrayList<>();
                        List<Weather> weathers = new ArrayList<>();
                        for (int i = 0; i < 5; i++) {
                            long timestamp = forecast.getTimestampByIndex(i + 3);
                            Weather weatherForTimestamp = forecast.getWeatherForTimestamp(timestamp);
                            weathers.add(weatherForTimestamp);
                            Temperature tempMini = weatherForTimestamp.getTemperature().getMinimum();
                            double temperatureInCelcius = tempMini.getValue(TemperatureUnit.CELCIUS);
                            list.add(temperatureInCelcius);

                        }
                        ForecastTripWeatherAdapter forecastTripWeatherAdapter = new ForecastTripWeatherAdapter(ForecastTripActivity.this, weathers);
                        rvWeatherForecast.setAdapter(forecastTripWeatherAdapter);
                        rvWeatherForecast.setLayoutManager(llForecastMan);
                        int minIndex = list.indexOf(Collections.min(list));
                        Log.v("Weather MINI", "Température mini : " + list.get(minIndex));
                        //Toast.makeText(context, "Température mini: " + list.get(minIndex), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailedToReceiveForecast(WeatherManager manager) {

                    }
                });
    }

    private void fetchTripNews() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("PublicTrips").document(AppHelper.tripEntityList.getFirebaseId()).collection("NewsFeed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    documentSnapshots.size();
                    NewsListingAdapter newsListingAdapter = new NewsListingAdapter(ForecastTripActivity.this, documentSnapshots);
                    rvNewsSection.setAdapter(newsListingAdapter);
                    rvNewsSection.setLayoutManager(llNewsMan);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                destinationLatLng = place.getLatLng();
                fetchWeather();
            }
        }

    }
}