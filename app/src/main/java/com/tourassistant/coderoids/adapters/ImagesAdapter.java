package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.textview.MaterialTextView;
import com.tourassistant.coderoids.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ImagesAdapter extends BaseAdapter {
    Context context;
    PlacesClient placesClient;
    LayoutInflater inflter;
    List<PhotoMetadata> metadata;
    public ImagesAdapter(Context applicationContext, PlacesClient placesClient, List<PhotoMetadata> metadata) {
        this.context = applicationContext;
        this.placesClient = placesClient;
        this.metadata = metadata;
        inflter = (LayoutInflater.from(applicationContext));
    }
    @Override
    public int getCount() {
        return metadata.size();
    }
    @Override
    public Object getItem(int i) {
        try {
            return metadata.get(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.row_images, null); // inflate the layout
        ImageView ivPlaceImage = view.findViewById(R.id.iv_image);
        try {
                final PhotoMetadata photoMetadata = metadata.get(position);
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(300) // Optional.
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    ivPlaceImage.setImageBitmap(bitmap);
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                        Log.e("Status", "Place not found: " + exception.getMessage());
                        final int statusCode = apiException.getStatusCode();
                        // TODO: Handle error with given status code.
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
}

