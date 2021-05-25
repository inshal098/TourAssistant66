package com.tourassistant.coderoids.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.PersonalPicturesUploads;
import com.tourassistant.coderoids.helpers.AppHelper;

public class TripPhotosActivity extends AppCompatActivity {
    RecyclerView rvAllPhotos;
    ImageButton ibCross;
    LinearLayoutManager llmNews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_photos);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        rvAllPhotos = findViewById(R.id.rv_trip_all_photos);
        ibCross = findViewById(R.id.ib_cross);
        llmNews = new LinearLayoutManager(this);
        llmNews.setOrientation(LinearLayoutManager.VERTICAL);

        if(AppHelper.newsListCurrent != null && AppHelper.newsListCurrent.size()>0){
            PersonalPicturesUploads newsListingAdapter = new PersonalPicturesUploads(this, AppHelper.newsListCurrent);
            rvAllPhotos.setAdapter(newsListingAdapter);
            rvAllPhotos.setLayoutManager(new GridLayoutManager(this, 3));
        }

        ibCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}