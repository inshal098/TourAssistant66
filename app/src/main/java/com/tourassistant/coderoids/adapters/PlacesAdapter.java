package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.models.PlacesModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {
    Context context;
    ArrayList<Place> places;
    boolean[] rowState;
    PlacesClient placesClient;

    public PlacesAdapter(Context applicationContext, ArrayList<Place> places, boolean[] rowState, PlacesClient placesClient) {
        this.context = applicationContext;
        this.places = places;
        this.rowState = rowState;
        this.placesClient = placesClient;
    }

    @NonNull
    @Override
    public PlacesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_places, viewGroup, false);
        return new PlacesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlacesAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            try {
                Place.BusinessStatus bussenessStatus = places.get(position).getBusinessStatus();
                viewHolder.mtvDestinationName.setText(places.get(position).getName());
                if(bussenessStatus != null)
                    viewHolder.mtvBussinessStatus.setText(bussenessStatus+"");
                else
                    viewHolder.mtvBussinessStatus.setText("Status Not Available");
                if(places.get(position).getRating() != null) {
                    float aFloat = places.get(position).getRating().floatValue();
                    viewHolder.ratingBar.setRating(aFloat);
                } else {
                    viewHolder.ratingBar.setRating(3);
                }
                final PhotoMetadata photoMetadata = places.get(position).getPhotoMetadatas().get(0);
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(300) // Optional.
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bmp = fetchPhotoResponse.getBitmap();
                    viewHolder.siV.setImageBitmap(bmp);
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView mtvDestinationName, mtvBussinessStatus;
        ShapeableImageView siV;
        AppCompatRatingBar ratingBar;
        MaterialButton btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mtvDestinationName = itemView.findViewById(R.id.destination_tag);
            siV = itemView.findViewById(R.id.iv_destingation);
            ratingBar = itemView.findViewById(R.id.rating);
            mtvBussinessStatus = itemView.findViewById(R.id.tv_bussnes);
        }
    }
}
