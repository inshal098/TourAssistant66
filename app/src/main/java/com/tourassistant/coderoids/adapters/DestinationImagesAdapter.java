package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.tourassistant.coderoids.R;

import org.w3c.dom.Text;

import java.util.List;

public class DestinationImagesAdapter extends RecyclerView.Adapter<DestinationImagesAdapter.ViewHolder> {
    Context context;
    PlacesClient placesClient;
    LayoutInflater inflter;
    List<List<PhotoMetadata>> metadata;
    String[] destinationStringArr;

    public DestinationImagesAdapter(Context applicationContext, PlacesClient placesClient, List<List<PhotoMetadata>> metadata, String[] destinationStringArr) {
        this.context = applicationContext;
        this.placesClient = placesClient;
        this.metadata = metadata;
        this.destinationStringArr = destinationStringArr;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @NonNull
    @Override
    public DestinationImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_images_destination, viewGroup, false);
        return new DestinationImagesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final DestinationImagesAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            LinearLayoutManager rvMang = new LinearLayoutManager(context);
            rvMang.setOrientation(LinearLayoutManager.HORIZONTAL);
            try {
                viewHolder.title.setText("Images Gallery of : " +destinationStringArr[position]);
                ImagesAdapter imagesAdapter = new ImagesAdapter(context,placesClient,metadata.get(position));
                viewHolder.ivPlaceImage.setAdapter(imagesAdapter);
                viewHolder.ivPlaceImage.setLayoutManager(rvMang);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return metadata.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView ivPlaceImage;
        TextView title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceImage =  itemView.findViewById(R.id.destinations_images);
            title =  itemView.findViewById(R.id.title);
        }
    }
}


