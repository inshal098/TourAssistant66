package com.tourassistant.coderoids.home.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.NewsListingAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.starttrip.ForecastTripActivity;

import java.util.List;


public class NewsFeed extends Fragment {
    RecyclerView rvView;
    LinearLayoutManager llNewsMan;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        rvView = view.findViewById(R.id.rv_news_feed);
        llNewsMan = new LinearLayoutManager(getContext());
        llNewsMan.setOrientation(RecyclerView.VERTICAL);
        fetchTripNews();

    }
    private void fetchTripNews() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("NewsFeed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (documentSnapshots != null) {
                        documentSnapshots.size();
                        NewsListingAdapter newsListingAdapter = new NewsListingAdapter(getActivity(), documentSnapshots);
                        rvView.setAdapter(newsListingAdapter);
                        rvView.setLayoutManager(llNewsMan);
                    }
                }
            }
        });
    }
}