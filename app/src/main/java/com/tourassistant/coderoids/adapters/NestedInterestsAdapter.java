package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;

import org.json.JSONArray;

import java.util.List;

public class NestedInterestsAdapter extends RecyclerView.Adapter<NestedInterestsAdapter.ViewHolder> {
    Context context;
    List<DocumentSnapshot> interest;
    boolean[] rowState;
    String type;
    JSONArray intrestArray;
    public NestedInterestsAdapter(Context context, List<DocumentSnapshot> interest, boolean[] interestRowState, JSONArray intrestArray) {
        this.context = context;
        this.interest = interest;
        this.rowState = interestRowState;
        this.intrestArray = intrestArray;
    }

    @NonNull
    @Override
    public NestedInterestsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_interest_list, viewGroup, false);
        return new NestedInterestsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NestedInterestsAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            viewHolder.interestName.setText(interest.get(position).getString("interestName"));
            int finalPosition = position;
            if(intrestArray != null && intrestArray.toString().contains(interest.get(position).getId())){
                rowState[finalPosition] = true;
                viewHolder.interestState.setChecked(true);
            } else {
                viewHolder.interestState.setChecked(false);
                rowState[finalPosition] = false;
            }
            viewHolder.interestState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    rowState[finalPosition] = isChecked;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return interest.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView interestName;
        CheckBox interestState;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            interestName  = itemView.findViewById(R.id.interestTag);
            interestState  = itemView.findViewById(R.id.check_Box);
        }
    }
}



