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
import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.plantrip.PlanTrip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TripDetailAdapter extends RecyclerView.Adapter<TripDetailAdapter.ViewHolder> {
    Context context;
    JSONArray tripDetailArr;

    public TripDetailAdapter(Context applicationContext, JSONArray tripDetailArr) {
        this.context = applicationContext;
        this.tripDetailArr = tripDetailArr;
    }

    @NonNull
    @Override
    public TripDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.trip_detail_row, viewGroup, false);
        return new TripDetailAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final TripDetailAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
        viewHolder.tvDetail.setText(tripDetailArr.getString(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tripDetailArr.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDetail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDetail = itemView.findViewById(R.id.detail);
        }
    }
}


