package com.tourassistant.coderoids.api;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.widget.Toast;

import com.tourassistant.coderoids.BaseActivity;
import com.tourassistant.coderoids.interfaces.RequestInterface;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WebAPISecureRequest extends AsyncTask<Object, Void, String> {


    private String url;
    private Context context;
    private RequestInterface requestInterface;
    private String requestType = "";
    private String apiName;
    private String mainUrl;
    private String fragmentName;
    private boolean isShowDialog;
    private Uri.Builder uriBuilder;
    private JSONObject requestBody;
    private final Handler dialogHandler = new Handler();
    private SharedPreferences prefLoginDetail;
    private String accessToken = "";
    private boolean isJSONRequest = false;
    Map<String, String> queryParam = new HashMap<>();
    Uri.Builder postBuilderParams;


    public WebAPISecureRequest(String fragmentName,String apiName,String url,Uri.Builder params,Context context){
        this.url = url;
        this.postBuilderParams = params;
        this.context = context;
        this.requestInterface = (RequestInterface) BaseActivity.baseActivityInstance;
        this.fragmentName = fragmentName;
        this.apiName = apiName;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override

    protected String doInBackground(Object... params) {
        String result = "";
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE);
            JSONObject responseObject;
            if (isNetworkAvailable()) {
                responseObject = Parser.Get(url+apiName+postBuilderParams);
                result = responseObject != null ? responseObject.toString() : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override

    protected void onPostExecute(String responseString) {
        try {
            if (responseString != null && !responseString.matches("")) {
                JSONObject result = new JSONObject(responseString);
                requestInterface.RequestSecureFinished(apiName, fragmentName, responseString);

            } else {
                Toast.makeText(context, "Unable To Resolve Host", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        }
    }

    private boolean isNetworkAvailable() {
        boolean flag = false;
        ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cwjManager.getActiveNetworkInfo() != null)
            flag = cwjManager.getActiveNetworkInfo().isAvailable();
        return flag;
    }

}

