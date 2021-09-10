package com.targetly.sdk.network;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.targetly.sdk.BuildConfig;

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
import java.net.URL;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class RequestTask extends AsyncTask<String, String, String>{

    public static String kDeviceTokenEndpoint = "/api/customer/token";
    public static String kFeedbackEndpoint = "/api/personalizer/feedback";
    String kBaseUrl = "https://targetly.ai";
    JSONObject parameters;
    String accessToken;

    public RequestTask(JSONObject parameters, String accessToken) {
        this.parameters = parameters;
        this.accessToken = accessToken;
    }

    /// frequent request attributes
    JSONObject attributes;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        try {
            attributes = new JSONObject()
                    .put("X-Device-Platform", "Android")
                    .put("X-Device-Model", Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL)
                    .put("X-Device-OS-Version", Build.VERSION.RELEASE)
                    .put("X-Device-Time_Zone", TimeZone.getDefault().getDisplayName())
                    .put("X-Device-Time_Stamp", System.currentTimeMillis()/1000);
            Log.d("attributes:", attributes.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... uri) {
        if (accessToken == null || accessToken == "") {
            Log.e("Error:", "Initialize Targetly Sdk with Access Token");
            return "Error: Initialize Targetly Sdk with Access Token";
        }
        String responseString = null;
        try {
            URL url = new URL(kBaseUrl+uri[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("x-api-key", accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            parameters.put("attrs", attributes);
            String query = parameters.toString();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

            if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK){
                // Do normal input or output stream reading
                responseString = readStream(conn.getInputStream());
                Log.d("Response:", responseString);
            }else {
                Log.d("Error:", conn.getResponseMessage());
            }
        } catch (IOException | JSONException e) {
            //TODO Handle problems..
            Log.d("Error:", e.getMessage());
            e.printStackTrace();
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
        if (result != null) {
            Log.d("Response:", result);
        }
    }

    public static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}