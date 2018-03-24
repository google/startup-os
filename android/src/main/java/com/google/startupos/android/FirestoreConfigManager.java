package com.google.startupos.android;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public final class FirestoreConfigManager {
    private static FirebaseOptions fromJson(JSONObject obj) throws JSONException {
        JSONObject client = obj.getJSONArray("client").getJSONObject(0);
        JSONObject projectInfo = obj.getJSONObject("project_info");

        return new FirebaseOptions.Builder()
                .setApplicationId(client.getJSONObject("client_info").getString("mobilesdk_app_id"))
                .setProjectId(projectInfo.getString("project_id"))
                .setApiKey(client.getJSONArray("api_key").getJSONObject(0).getString("current_key"))
                .setDatabaseUrl(projectInfo.getString("firebase_url"))
                .setStorageBucket(projectInfo.getString("storage_bucket")).build();
    }
    
    public static void initializeFromAssets(Context applicationContext) throws IOException, JSONException {
        initializeFromAssets(applicationContext, "google-services.json");
    }

    public static void initializeFromAssets(Context applicationContext, String assetFileName) throws 
            IOException, 
            JSONException {

        InputStream stream = applicationContext.getAssets().open(assetFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        JSONObject obj = new JSONObject(builder.toString());
        FirebaseApp.initializeApp(applicationContext, fromJson(obj));
    }

}

