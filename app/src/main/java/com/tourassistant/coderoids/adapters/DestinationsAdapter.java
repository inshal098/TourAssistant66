package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.plantrip.PlanTrip;

import java.util.ArrayList;

public class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.ViewHolder> {
    Context context;
    ArrayList<PlacesModel> places;
    boolean[] rowState;
    String type;

    public DestinationsAdapter(Context applicationContext, ArrayList<PlacesModel> places, boolean[] rowState , String type) {
        this.context = applicationContext;
        this.places = places;
        this.rowState = rowState;
        this.type = type;
    }

    @NonNull
    @Override
    public DestinationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_places, viewGroup, false);
        return new DestinationsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final DestinationsAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            try {
                PlacesModel placesModel = places.get(position);
                viewHolder.mtvDestinationName.setText(placesModel.getDestinationName());
                if(placesModel.getBlob() != null){
                   byte [] bytes=   placesModel.getBlob().toBytes();
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    viewHolder.siV.setImageBitmap(bmp);
                }
                String rating = placesModel.getDestinationRating();
                Double aDouble = Double.parseDouble(rating);
                viewHolder.ratingBar.setRating(aDouble.floatValue());
                viewHolder.mtvBussinessStatus.setBackground(context.getResources().getDrawable(R.drawable.cell));
                if (type.matches("D"))
                    viewHolder.mtvBussinessStatus.setText("Add to Trip");
                else
                    viewHolder.mtvBussinessStatus.setText("Plan A Trip");

                viewHolder.mtvBussinessStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type.matches("D")) {
                            if(AppHelper.tripEntityList.getDestinationId() != null && !AppHelper.tripEntityList.getDestinationId().matches("")){
                                if( AppHelper.tripEntityList.getDestinationId().contains(placesModel.getDestinationId())) {
                                    Toast.makeText(context, "Destination is Already Added", Toast.LENGTH_SHORT).show();
                                } else {
                                    String destinationId = AppHelper.tripEntityList.getDestinationId() + "," + placesModel.getDestinationId();
                                    AppHelper.tripEntityList.setDestinationId(destinationId);
                                    String destinationName = AppHelper.tripEntityList.getDestination() + "," + placesModel.getDestinationName();
                                    AppHelper.tripEntityList.setDestination(destinationName);
                                    Navigation.findNavController(v).navigate(R.id.editTripFragment);
                                }
                            } else {
                                AppHelper.tripEntityList.setDestination(placesModel.getDestinationName());
                                AppHelper.tripEntityList.setDestinationId(placesModel.getDestinationId());
                                Navigation.findNavController(v).navigate(R.id.editTripFragment);
                            }
                        } else {
                            context.startActivity(new Intent(context, PlanTrip.class));
                        }
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

