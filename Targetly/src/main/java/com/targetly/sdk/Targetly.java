package com.targetly.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.targetly.sdk.network.RequestTask;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class Targetly {

    private static String accessToken;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String KEY = "device_token_for_targetly";

    // Initialize the Targetly SDK
    public static void initialize(String accessToken) {
        Targetly.accessToken = accessToken;
    }

    // Set Device Token for Push Notifications
    public  static void setDeviceToken(String deviceToken, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY, deviceToken);
        editor.apply();
        try {
            JSONObject parameters = new JSONObject()
                    .put("device_token", deviceToken);
            new RequestTask(parameters, accessToken).execute(RequestTask.kDeviceTokenEndpoint);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Send Feedback
    public static void sendFeedback(Double score, String eventName, String action_id, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String deviceToken = sharedPreferences.getString(KEY, "");
        if (deviceToken == null || deviceToken == "") {
            Log.e("Error:", "Set Device Token by calling Targetly.setDeviceToken(deviceToken: deviceToken)");
            return;
        }
        try {
            JSONObject parameters = new JSONObject()
                    .put("score", score)
                    .put("event_name", eventName)
                    .put("action_id", action_id)
                    .put("device_token", deviceToken);
            new RequestTask(parameters, accessToken).execute(RequestTask.kFeedbackEndpoint);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
