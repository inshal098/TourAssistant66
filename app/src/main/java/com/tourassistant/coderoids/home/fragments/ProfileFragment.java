package com.tourassistant.coderoids.home.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.InterestSelectionAdapter;
import com.tourassistant.coderoids.adapters.IntrestsAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    TextView tvEditProfile;
    TextView tvFollowingCount, tvFollowersCount, tvPostCount, tvName,tvWebsite,tvDescription;
    ImageButton ibAddPreferences;
    CircleImageView circleImageView;
    ProgressDialog dialog;
    GridView intests;
    ActionBar actionBar;
    JSONArray intrestArray = new JSONArray();
    IntrestsAdapter intrestsAdapter;
    List<DocumentSnapshot> interests;
    private boolean rowState[] = new boolean[0];

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
        tvName = v.findViewById(R.id.user_name);
        circleImageView = v.findViewById(R.id.profile_photo);
        tvEditProfile = v.findViewById(R.id.textEditProfile);
        tvFollowersCount = v.findViewById(R.id.tv_followers_count);
        tvFollowingCount = v.findViewById(R.id.tv_following_count);
        tvPostCount = v.findViewById(R.id.tv_post_count);
        tvWebsite = v.findViewById(R.id.website);
        tvDescription = v.findViewById(R.id.description);
        ibAddPreferences = v.findViewById(R.id.addIntrest);
        dialog = new ProgressDialog(getActivity());
        intests = v.findViewById(R.id.intests); // init GridView
        actionBar = ((AppCompatActivity)
                requireActivity()).getSupportActionBar();

        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.editProfileFragment);
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
                if (profileList.getInterests() != null && !profileList.getInterests().matches("")) {
                    JSONArray jsonArray = new JSONArray(profileList.getInterests());
                    intrestArray = jsonArray;
                    intrestsAdapter = new IntrestsAdapter(getActivity(), intrestArray);
                    intests.setAdapter(intrestsAdapter);
                }
                if (profileList.getProfileImage() != null && !profileList.getProfileImage().toString().matches("")) {
                    byte[] bytes = profileList.getProfileImage().toBytes();
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    circleImageView.setImageBitmap(bmp);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }



        ibAddPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }







}