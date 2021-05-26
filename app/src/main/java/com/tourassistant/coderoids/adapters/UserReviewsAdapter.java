package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.models.ReviewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserReviewsAdapter extends RecyclerView.Adapter<UserReviewsAdapter.ViewHolder> {
    Context context;
    List<DocumentSnapshot> reviewModels;

    public UserReviewsAdapter(Context applicationContext, List<DocumentSnapshot> reviewModels) {
        this.context = applicationContext;
        this.reviewModels = reviewModels;
    }

    @NonNull
    @Override
    public UserReviewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_reviews_user, viewGroup, false);
        return new UserReviewsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserReviewsAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            ReviewModel reviewModel = reviewModels.get(position).toObject(ReviewModel.class);
            Profile profile = AppHelper.getUserProfileObj(reviewModel.getReviewerId());
            if(reviewModel != null) {
                viewHolder.reviewrName.setText("By : "+profile.getDisplayName());
                viewHolder.reviewMessage.setText(reviewModel.getReviewMessage());
                String rating = reviewModel.getRatingCount();
                viewHolder.ratingBar.setEnabled(false);
                if (!rating.matches("")) {
                    float ratingCount = Float.parseFloat(rating);
                    viewHolder.ratingBar.setRating(ratingCount);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return reviewModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView reviewMessage ,reviewrName;
        RatingBar ratingBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewMessage = itemView.findViewById(R.id.review_message);
            reviewrName = itemView.findViewById(R.id.reviewr_name);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}


