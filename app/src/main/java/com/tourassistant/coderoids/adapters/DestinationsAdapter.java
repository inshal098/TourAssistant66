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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                if (type.matches("D")) {
                    viewHolder.mtvBussinessStatus.setText("Add to Trip");
                    viewHolder.tvEditDest.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.mtvBussinessStatus.setText("Plan A Trip");
                    viewHolder.tvEditDest.setVisibility(View.GONE);
                }
                viewHolder.mtvBussinessStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type.matches("D")) {
                            if(AppHelper.tripEntityList.getDestinationId() != null && !AppHelper.tripEntityList.getDestinationId().matches("")){
                                if( AppHelper.tripEntityList.getDestinationId().contains(placesModel.getDestinationId())) {
                                    Toast.makeText(context, "Destination is Already Added", Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        JSONArray jsonArray = new JSONArray(AppHelper.tripEntityList.getDestinationId());
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject = new JSONObject();
                                        jsonObject.put("destId",placesModel.getDestinationId());
                                        jsonObject.put("destName",placesModel.getDestinationName());
                                        jsonArray.put(jsonObject);
                                        AppHelper.tripEntityList.setDestinationId(jsonArray.toString());
                                        Navigation.findNavController(v).navigate(R.id.editTripFragment);
                                    } catch (JSONException ex){
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                try {
                                    JSONArray jsonArray = new JSONArray();
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("destId",placesModel.getDestinationId());
                                    jsonObject.put("destName",placesModel.getDestinationName());
                                    jsonArray.put(jsonObject);
                                    AppHelper.tripEntityList.setDestinationId(jsonArray+"");
                                }catch (JSONException ex){
                                    ex.printStackTrace();
                                }
                                //AppHelper.tripEntityList.setDestination(placesModel.getDestinationName());
                                Navigation.findNavController(v).navigate(R.id.editTripFragment);
                            }
                        } else {
                            context.startActivity(new Intent(context, PlanTrip.class));
                        }
                    }
                });

                int finalPosition = position;
                viewHolder.tvEditDest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppHelper.editDestModel = places.get(finalPosition);
                        Navigation.findNavController(v).navigate(R.id.editDestinationFragment); }
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
        MaterialTextView mtvDestinationName, mtvBussinessStatus ,tvEditDest;
        ShapeableImageView siV;
        AppCompatRatingBar ratingBar;
        MaterialButton btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mtvDestinationName = itemView.findViewById(R.id.destination_tag);
            siV = itemView.findViewById(R.id.iv_destingation);
            ratingBar = itemView.findViewById(R.id.rating);
            mtvBussinessStatus = itemView.findViewById(R.id.tv_bussnes);
            tvEditDest = itemView.findViewById(R.id.tv_edit_dest);
        }
    }
}

