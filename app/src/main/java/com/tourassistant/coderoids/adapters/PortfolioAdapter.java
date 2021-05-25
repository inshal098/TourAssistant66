package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.home.TripPhotosActivity;
import com.tourassistant.coderoids.models.NewsFeedModel;

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
                .inflate(R.layout.row_portfolio, viewGroup, false);
        return new PortfolioAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PortfolioAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            int finalPosition = position;
            rootRef.collection("PublicTrips").document(documentSnapshots.get(position).getId()).collection("NewsFeed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> newsList = task.getResult().getDocuments();
                            if(newsList != null){
                                viewHolder.imagesCount.setText(newsList.size() +"+");
                                viewHolder.ivTripTitle.setText(documentSnapshots.get(finalPosition).getString("tripTitle"));
                            }
                        }
                    }
                });

            viewHolder.materialCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootRef.collection("PublicTrips").document(documentSnapshots.get(finalPosition).getId()).collection("NewsFeed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> newsList = task.getResult().getDocuments();
                               AppHelper.newsListCurrent = newsList;
                               context.startActivity(new Intent(context, TripPhotosActivity.class));
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return documentSnapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView ivTripTitle, imagesCount;
        MaterialCardView materialCardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTripTitle = itemView.findViewById(R.id.trip_title);
            imagesCount = itemView.findViewById(R.id.iv_portf_image);
            materialCardView = itemView.findViewById(R.id.card_trip);
        }
    }
}




