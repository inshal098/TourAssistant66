package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.plantrip.PlanTrip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TripsDestinationAdapter extends RecyclerView.Adapter<TripsDestinationAdapter.ViewHolder> {
    Context context;
    JSONArray destinations;

    public TripsDestinationAdapter(Context applicationContext, JSONArray destinations) {
        this.context = applicationContext;
        this.destinations = destinations;
    }

    @NonNull
    @Override
    public TripsDestinationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_trip_destinations, viewGroup, false);
        return new TripsDestinationAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final TripsDestinationAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            JSONObject destObj = destinations.getJSONObject(position);
            int index = position +1;
            viewHolder.tripDestName.setText(index +" : "+destObj.getString("destName"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return destinations.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tripDestName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripDestName = itemView.findViewById(R.id.trip_dest_name);
        }
    }
}


