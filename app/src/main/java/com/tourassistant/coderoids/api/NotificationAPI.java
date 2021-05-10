package com.tourassistant.coderoids.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tourassistant.coderoids.helpers.AppConstants;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationAPI {
    Context context;
    String userId;
    String notificaitonPoolId;

    public NotificationAPI(Context context, String userId, String notificaitonPoolId) {
        this.context = context;
        this.userId = userId;
        this.notificaitonPoolId = notificaitonPoolId;
    }

    public void sendNotification() {
        String url = AppConstants.NOTIFICATION_URL;
        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter("user_id", "" + userId);
        builder.appendQueryParameter("noti_pool_id", "" + notificaitonPoolId);
        new WebAPISecureRequest("HomeFragment",AppConstants.NOTIFICATION_API,url,builder,context).execute();

        //        String url = AppConstants.NOTIFICATION_URL;
//        Map<String, String> params = new HashMap<String, String>();
//                params.put("user_id", userId);
//                params.put("noti_pool_id", notificaitonPoolId);
//                return params;
//        RequestQueue queue = Volley.newRequestQueue(context);
//        StringRequest sr = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.e("HttpClient", "success! response: " + response.toString());
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        error.printStackTrace();
//                        Log.e("HttpClient", "error: " + error.toString());
//                    }
//                })
//        {
//            @Nullable
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("user_id", userId);
//                params.put("noti_pool_id", notificaitonPoolId);
//                return params;
//            }
//
//            @Override
//            public String getUrl() {
//                return super.getUrl();
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("Accept","application/json");
//                params.put("Content-Type","application/json");
//                return params;
//            }
//        };
//        queue.add(sr);
    }
}
