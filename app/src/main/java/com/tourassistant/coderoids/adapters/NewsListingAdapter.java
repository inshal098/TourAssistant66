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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.NewsFeed;

import java.util.Date;
import java.util.List;

public class NewsListingAdapter extends RecyclerView.Adapter<NewsListingAdapter.ViewHolder> {
    Context context;
    PlacesClient placesClient;
    LayoutInflater inflter;
    List<List<PhotoMetadata>> metadata;
    String[] destinationStringArr;
    List<DocumentSnapshot> documentSnapshots;

    public NewsListingAdapter(Context applicationContext, List<DocumentSnapshot> documentSnapshots) {
        this.context = applicationContext;
        this.placesClient = placesClient;
        this.metadata = metadata;
        this.destinationStringArr = destinationStringArr;
        this.documentSnapshots = documentSnapshots;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @NonNull
    @Override
    public NewsListingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_news_list, viewGroup, false);
        return new NewsListingAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsListingAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            NewsFeed newsFeed = documentSnapshots.get(position).toObject(NewsFeed.class);
            viewHolder.title.setText(newsFeed.getTitle());
            viewHolder.newsDescription.setText(newsFeed.getDescription());
            viewHolder.postedBy.setText("By : "+newsFeed.getUserName());
            long timieInMillis = Long.parseLong(newsFeed.getDateInMillis());
            String duration = durationFromNow(timieInMillis);
            viewHolder.newsTime.setText(duration +" ago");
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
        TextView title, newsDescription, newsTime, postedBy;
        ImageView ivPlaceImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceImage = itemView.findViewById(R.id.iv_news_image);
            title = itemView.findViewById(R.id.news_title);
            newsDescription = itemView.findViewById(R.id.news_description);
            newsTime = itemView.findViewById(R.id.news_time);
            postedBy = itemView.findViewById(R.id.posted_by);
        }
    }
}


