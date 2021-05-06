package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.google.android.material.textview.MaterialTextView;
import com.tourassistant.coderoids.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class IntrestsAdapter extends BaseAdapter {
    Context context;
    JSONArray intestArr;
    LayoutInflater inflter;
    public IntrestsAdapter(Context applicationContext, JSONArray intestArr) {
        this.context = applicationContext;
        this.intestArr = intestArr;
        inflter = (LayoutInflater.from(applicationContext));
    }
    @Override
    public int getCount() {
        return intestArr.length();
    }
    @Override
    public Object getItem(int i) {
        try {
            return intestArr.get(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.row_intrest, null); // inflate the layout
        Button materialTextView = view.findViewById(R.id.tv_intrest_tag);
        try {
            JSONObject jsonObject = intestArr.getJSONObject(position);
            materialTextView.setText(jsonObject.getString("interestName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}
