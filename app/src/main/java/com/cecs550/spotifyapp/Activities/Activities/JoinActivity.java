package com.cecs550.spotifyapp.Activities.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cecs550.spotifyapp.Activities.Classes.NsdHelper;

import com.cecs550.spotifyapp.Activities.Classes.UserProfile;
import com.cecs550.spotifyapp.R;

public class JoinActivity extends AppCompatActivity {
    private String accessToken;

    NsdHelper nsdHelper;

    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("token");

        //connect to host

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
            }
        };

        Button cancelButton = (Button) findViewById(R.id.cancel_join_playlist);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                GoToMenu(accessToken);
            }
        });
    }


    private void GoToMenu(String token)
    {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }


}
