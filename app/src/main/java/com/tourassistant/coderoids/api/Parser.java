package com.tourassistant.coderoids.api;

import android.net.Uri;

import com.tourassistant.coderoids.helpers.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import cz.msebera.android.httpclient.client.HttpClient;

public class Parser {
    public static final int HTTP_TIMEOUT = 20 * 1000; // milliseconds
    private static HttpClient mHttpClient;

    public static String baseUrl="";

    public static JSONObject Get(String Url) throws Exception {
        String info = "";
        JSONObject result = null;
        BufferedReader reader = null;
        int responseCode = 0;
        try {
            URL url = new URL(Url);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(HTTP_TIMEOUT);
            //conn.setDoOutput(true);

//            if (!Url.contains("https://") && !Url.contains("http://") && !Url.contains("maps.googleapis.com")) {
//                Request request = Request.getInstance();
//                if (request != null) {
//                    Url = AppConstants.NOTIFICATION_URL;
//                } else {
//                    throw new IOException("Unable to resolve host ");
//                }
//            }

            HttpURLConnection httpURLConnection = (HttpURLConnection) conn;
            responseCode = httpURLConnection.getResponseCode();
            String responseMessage = httpURLConnection.getResponseMessage();
            InputStream is;

            // Check if server response is valid
            if (responseCode != 200 && responseCode != 500 && responseCode != 401
                    && responseCode != 404 && responseCode != 400 && responseCode != 503)
                throw new IOException("Invalid response from server: " + responseMessage+","+"{_"+responseCode+"_}");
            else if (responseCode == 400) {
                is = httpURLConnection.getErrorStream();
            } else {
                try {
                    is = httpURLConnection.getInputStream();
                } catch (Exception ex) {
                    is = httpURLConnection.getErrorStream();
                }

            }

            reader = new BufferedReader(new InputStreamReader(is));
            String  temp1 ="";
            while ((temp1 = reader.readLine()) != null){
                info += temp1;
            }
            if (!info.matches("")) {
                try {
                    result = new JSONObject(info);
                }
                catch(Throwable t) {
                    t.printStackTrace();
                    String message = info.matches("") ? "No Response From Server with statuscode "+ responseCode : "Request Failed with statusCode "
                            + responseCode + "& Data is " +info;
                    JSONObject objStatus = new JSONObject();
                    if(responseCode == 404){
                        objStatus.put("Result" , "Failed");
                        objStatus.put("Details", message);
                        objStatus.put("Error","404");
                        objStatus.put("ResponseCode", responseCode +"");
                    } else if(responseCode == 503){
                        objStatus.put("Result", "Failed");
                        objStatus.put("Details", "Unable to resolve host");
                        objStatus.put("Error", "Unable to resolve host");
                         objStatus.put("ResponseCode", responseCode +"");
                    } else {
                        objStatus.put("Result", "Failed");
                        objStatus.put("Details", message);
                        objStatus.put("Error", "JsonException");
                        objStatus.put("ResponseCode", responseCode +"");
                    }
                    result = objStatus;
                }
            }
            if(result == null)
                result = new JSONObject();
            if(!result.has("ResponseCode")){
                result.put("ResponseCode", responseCode +"");
            }
            httpURLConnection.disconnect();
            reader.close();
            is.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage() != null && e.getMessage().contains("Unable to resolve host")){
                JSONArray arrayStatus = new JSONArray();
                JSONObject objStatus = new JSONObject();
                objStatus.put("Result","Failed");
                objStatus.put("Details", "Unable to resolve host");
                objStatus.put("Error", "Unable to resolve host");
                objStatus.put("ResponseCode", responseCode +"");
                arrayStatus.put(objStatus);
                return objStatus;
            }
            else if(e.getMessage() != null && e.getMessage().contains("Connection to") && e.getMessage().contains("refused") || e.getMessage().contains("(Connection refused)")) {
                JSONObject objStatus = new JSONObject();
                try {
                    objStatus.put("Result","Failed");
                    objStatus.put("Details", "Unable to resolve host");
                    objStatus.put("Error", "Unable to resolve host");
                    objStatus.put("ResponseCode", responseCode +"");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                return objStatus;
            }
            else{
                JSONObject objStatus = new JSONObject();
                objStatus.put("Result","Failed");
                objStatus.put("Details", e.toString());
                objStatus.put("Error", "SomeThing Wrong with Server");
                objStatus.put("ResponseCode", responseCode +"");
                return objStatus;
            }
        }

    }


    public static JSONObject Post(String Url, Uri.Builder builder) throws Exception {
        String info = "";
        JSONObject result = null;
        BufferedReader reader = null;

        int responseCode = 0;
        try {
            URL url = new URL(Url);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(HTTP_TIMEOUT);
            conn.setDoOutput(true);
            HttpURLConnection httpURLConnection = (HttpURLConnection) conn;
            try {
                httpURLConnection.setRequestMethod("POST");
            }catch (ProtocolException ex){
                ex.printStackTrace();
            }
            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            String query  = builder.build().getEncodedQuery();
            if(query != null)
                writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            InputStream is = null;

            responseCode = httpURLConnection.getResponseCode();
            String responseMessage = httpURLConnection.getResponseMessage();


            // Check if server response is valid
            if (responseCode != 200 && responseCode != 500 && responseCode != 401 && responseCode != 404 && responseCode != 503) {
                throw new IOException("Invalid response from server: " + responseMessage+","+"{_"+responseCode+"_}");
            } else if (responseCode == 500 || responseCode == 503){
                is = httpURLConnection.getErrorStream();
            } else {
                is = httpURLConnection.getInputStream();
            }
            reader = new BufferedReader(new InputStreamReader(is));
            // Pull content stream from response
            String  temp1 ="";
            while ((temp1 = reader.readLine()) != null){
                info += temp1;
            }
            if (!info.matches("")) {
//				if (String.valueOf(info.charAt(0)).equals("{") && String.valueOf(info.charAt(info.length()-1)).equals("}"))
                try {
                    result = new JSONObject(info);
                }
                catch(Throwable t)
                {
                    String message = info.matches("") ? "No Response From Server with statuscode "+ responseCode : "Request Failed with statusCode "
                            + responseCode + "& Data is " + info;
                    JSONObject objStatus = new JSONObject();
                    if(responseCode == 404){
                        objStatus.put("Result" , "Failed");
                        objStatus.put("Details", message);
                        objStatus.put("ResponseCode", responseCode +"");
                        objStatus.put("Error","404");
                    }  else if(responseCode == 503){
                        objStatus.put("Result", "Failed");
                        objStatus.put("Details", "Unable to resolve host");
                        objStatus.put("Error", "Unable to resolve host");
                        objStatus.put("ResponseCode", responseCode +"");
                    } else {
                        objStatus.put("Result", "Failed");
                        objStatus.put("Details", message);
                        objStatus.put("ResponseCode", responseCode +"");
                        objStatus.put("Error", "JsonException");
                    }
                    result = objStatus;
                }
            }
            if(result == null)
                result = new JSONObject();
            if(!result.has("ResponseCode")){
                result.put("ResponseCode", responseCode +"");
            }
            httpURLConnection.disconnect();
            reader.close();
            is.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if(e.getMessage().contains("Unable to resolve host")){
                JSONObject objStatus = new JSONObject();
                objStatus.put("Result","Failed");
                objStatus.put("Details", "Unable to resolve host");
                objStatus.put("Error", "Unable to resolve host");
                objStatus.put("ResponseCode", responseCode +"");
                return objStatus;
            }else if(e.getMessage().contains("Connection to") && e.getMessage().contains("refused")  || e.getMessage().contains("(Connection refused)")) {
                JSONObject objStatus = new JSONObject();
                try {
                    objStatus.put("Result","Failed");
                    objStatus.put("Details", "Unable to resolve host");
                    objStatus.put("Error", "Unable to resolve host");
                    objStatus.put("ResponseCode", responseCode +"");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                return objStatus;
            }
            else {
                JSONObject objStatus = new JSONObject();
                objStatus.put("Result","Failed");
                objStatus.put("Details", e.toString());
                objStatus.put("ResponseCode", responseCode +"");
                objStatus.put("Error", "SomeThing Wrong with Server");
                return objStatus;
            }
        }

    }

}
