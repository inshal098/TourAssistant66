package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.models.NewsFeed;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {
    Context context;
    LayoutInflater inflter;
    List<DocumentSnapshot> documentSnapshots;

    public PortfolioAdapter(Context applicationContext, List<DocumentSnapshot> documentSnapshots) {
        this.context = applicationContext;
        this.documentSnapshots = documentSnapshots;
    }

    @NonNull
    @Override
    public PortfolioAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_portfolio_, viewGroup, false);
        return new PortfolioAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PortfolioAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            NewsFeed newsFeed = documentSnapshots.get(position).toObject(NewsFeed.class);
            if (newsFeed.getNewsThumbNail() != null) {
                byte[] bytes = newsFeed.getNewsThumbNail().toBytes();
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                viewHolder.ivPlaceImage.setImageBitmap(bmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String durationFromNow(long currentTime) {

        long different = System.currentTimeMillis() - currentTime;

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        String output = "";
        if (elapsedDays > 0) {
            if(elapsedDays >1)
                output += elapsedDays + " days ";
            else
                output += elapsedDays + " day ";

            return output;
        }
        if (elapsedDays > 0 || elapsedHours > 0) {
            output += elapsedHours + " hours ";
            return output;
        }
        if (elapsedHours > 0 || elapsedMinutes > 0) {
            output += elapsedMinutes + " minutes ";
            return output;

        }
        if (elapsedMinutes > 0 || elapsedSeconds > 0) output += elapsedSeconds + " seconds";

        return output;
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceImage = itemView.findViewById(R.id.iv_portf_image);
        }
    }
}



