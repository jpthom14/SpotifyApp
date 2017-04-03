package com.cecs550.spotifyapp.Activities.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cecs550.spotifyapp.Activities.Classes.UserProfile;
import com.cecs550.spotifyapp.R;


public class MenuActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        String accessToken = intent.getStringExtra("token");

        UserProfile hostProfile = new UserProfile(accessToken);

        hostProfile.SetupProfile();

    }


}
