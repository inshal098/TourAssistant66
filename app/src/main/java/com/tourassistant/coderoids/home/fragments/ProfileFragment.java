package com.tourassistant.coderoids.home.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.InterestSelectionAdapter;
import com.tourassistant.coderoids.adapters.IntrestsAdapter;
import com.tourassistant.coderoids.adapters.NewsListingAdapter;
import com.tourassistant.coderoids.adapters.PortfolioAdapter;
import com.tourassistant.coderoids.chatmodule.ChatParentActivity;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.profilefriends.FriendsProfileActivity;
import com.tourassistant.coderoids.starttrip.ReportHazard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    TextView tvEditProfile;
    TextView tvFollowingCount, tvFollowersCount, tvPostCount, tvName,tvWebsite,tvDescription ,tvTrackingState ,tvIntrest;
    ImageButton ibAddPreferences;
    SwitchMaterial smTrackState;
    CircleImageView circleImageView;
    ProgressDialog dialog;
    GridView intests;
    ActionBar actionBar;
    private boolean rowState[] = new boolean[0];
    ExtendedFloatingActionButton actionButton;
    FloatingActionButton floatingChatIc;
    SharedPreferences.Editor editorLogin;
    SharedPreferences prefLogin;
    RecyclerView rvNews;
    LinearLayoutManager llmNews;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_view, container, false);
        intializeView(v);
        return v;
    }

    private void intializeView(View v) {
        prefLogin = getActivity().getSharedPreferences("logindata", Context.MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        editorLogin.apply();
        tvName = v.findViewById(R.id.user_name);
        circleImageView = v.findViewById(R.id.profile_photo);
        rvNews = v.findViewById(R.id.rv_news_feed);
        llmNews = new LinearLayoutManager(getContext());
        llmNews.setOrientation(LinearLayoutManager.VERTICAL);
        floatingChatIc = v.findViewById(R.id._chat);
        tvEditProfile = v.findViewById(R.id.textEditProfile);
        tvFollowersCount = v.findViewById(R.id.tv_followers_count);
        tvFollowingCount = v.findViewById(R.id.tv_following_count);
        tvPostCount = v.findViewById(R.id.tv_post_count);
        tvWebsite = v.findViewById(R.id.website);
        tvDescription = v.findViewById(R.id.description);
        ibAddPreferences = v.findViewById(R.id.addIntrest);
        smTrackState = v.findViewById(R.id.sw_tracking_state);
        tvTrackingState = v.findViewById(R.id.tracking_state_tv);
        actionButton = v.findViewById(R.id.create_a_post);
        tvIntrest = v.findViewById(R.id.prefs);
        dialog = new ProgressDialog(getActivity());
        actionBar = ((AppCompatActivity)
                requireActivity()).getSupportActionBar();

        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.editProfileFragment);
            }
        });
        if(AppHelper.currentProfileInstance != null) {

            if(prefLogin.getString("userTracking","0").matches("1")){
                tvTrackingState.setText("Location is Being Tracked");
                tvTrackingState.setBackgroundColor(getActivity().getResources().getColor(R.color.green));
                smTrackState.setChecked(true);
            } else {
                smTrackState.setChecked(false);
                tvTrackingState.setText("Location Tracking is Off");
                tvTrackingState.setBackgroundColor(getActivity().getResources().getColor(R.color.red));
            }
        }


        smTrackState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                DocumentReference uidRef = rootRef.collection("Users").document(AppHelper.currentProfileInstance.getUserId());
                if(smTrackState.isChecked()){
                    editorLogin.putString("userTracking" ,"1").apply();
                    uidRef.update("userTracking","1");
                    tvTrackingState.setText("Enabled , Your Location is Being Tracked");
                    tvTrackingState.setBackgroundColor(getActivity().getResources().getColor(R.color.green));
                } else {
                    uidRef.update("userTracking","0");
                    editorLogin.putString("userTracking" ,"0").apply();                                                
                    tvTrackingState.setText("Disabled , Your Location Tracking is Off");
                    tvTrackingState.setBackgroundColor(getActivity().getResources().getColor(R.color.red));
                }
            }
        });
        try {
            if(AppHelper.currentProfileInstance != null) {
                Profile profileList = AppHelper.currentProfileInstance;
                if (profileList.getUserName() != null)
                    actionBar.setTitle(profileList.getUserName());
                tvName.setText(profileList.getUserName());
                tvPostCount.setText(profileList.getTotalPosts());
                tvFollowersCount.setText(profileList.getFollowers());
                tvFollowingCount.setText(profileList.getFollowing());
                tvWebsite.setText(profileList.getWebsite());
                tvDescription.setText(profileList.getAboutDescription());
                tvIntrest.setText(AppHelper.getUserIntrests());
                if (profileList.getProfileImage() != null && !profileList.getProfileImage().toString().matches("")) {
                    byte[] bytes = profileList.getProfileImage().toBytes();
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    circleImageView.setImageBitmap(bmp);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
        }


        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ReportHazard.class));
            }
        });
        ibAddPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        floatingChatIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startActivity(new Intent(getActivity(), ChatParentActivity.class).putExtra("type" ,"Profile"));
            }
        });
        fetchUserPosts();
    }

    private void fetchUserPosts() {
        if(AppHelper.currentProfileInstance != null && AppHelper.currentProfileInstance.getUserId() != null) {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            rootRef.collection("Users").document(AppHelper.currentProfileInstance.getUserId()).collection("NewsFeed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                        if (documentSnapshots != null) {
                            documentSnapshots.size();
                            PortfolioAdapter newsListingAdapter = new PortfolioAdapter(getContext(), documentSnapshots);
                            rvNews.setAdapter(newsListingAdapter);
                            rvNews.setLayoutManager(new GridLayoutManager(getContext(), 3));
                        }
                    }
                }
            });
        }
    }
}