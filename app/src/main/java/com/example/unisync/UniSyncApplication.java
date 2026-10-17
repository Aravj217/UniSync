package com.example.unisync;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * UniSync Application class — initializes Firebase and global settings.
 */
public class UniSyncApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Configure Firestore settings
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)  // Enable offline persistence
                .build();
        db.setFirestoreSettings(settings);

        // Initialize unified DataManager
        com.example.unisync.utils.DataManager.init(this);
    }
}
