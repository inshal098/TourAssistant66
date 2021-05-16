package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.starttrip.ReplanTrip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DestinationsListEditAdater extends RecyclerView.Adapter<DestinationsListEditAdater.ViewHolder> {
    Context context;
    JSONArray currentDestinationIds;


    public DestinationsListEditAdater(Context context, JSONArray currentDestinationIds) {
        this.context = context;
        this.currentDestinationIds = currentDestinationIds;
    }

    @NonNull
    @Override
    public DestinationsListEditAdater.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_destinations_list, viewGroup, false);
        return new DestinationsListEditAdater.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final DestinationsListEditAdater.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            JSONObject jsonObject = currentDestinationIds.getJSONObject(position);
            viewHolder.tvDestinationName.setText("Destination Name :"+jsonObject.getString("destName"));
            int finalPosition = position;
            viewHolder.btnRemoveTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReplanTrip.instance.removeDestination(finalPosition);
                }
            });
            if(jsonObject.has("address"))
                viewHolder.tvGeopoint.setText(jsonObject.getString("address"));

            viewHolder.btnNewPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReplanTrip.instance.findNewDestination(finalPosition);
                }
            });

            viewHolder.btnNearbyPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReplanTrip.instance.findAddress(finalPosition);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return currentDestinationIds.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGeopoint ,tvDestinationName;
        Button btnRemoveTrip ,btnNewPlace ,btnNearbyPlace,btnSave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGeopoint = itemView.findViewById(R.id.tl_geo_point);
            tvDestinationName = itemView.findViewById(R.id.destination_name);
            btnNewPlace = itemView.findViewById(R.id.replace_place_with_new_place);
            btnNearbyPlace = itemView.findViewById(R.id.replace_place_with_nearby);
            btnRemoveTrip = itemView.findViewById(R.id.remove);
        }
    }


}


