package com.cecs550.spotifyapp.Activities.Activities;

import android.content.Intent;
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
import com.cecs550.spotifyapp.Activities.Classes.PlaylistConnection;
import com.cecs550.spotifyapp.Activities.Classes.UserProfile;
import com.cecs550.spotifyapp.R;

public class JoinActivity extends AppCompatActivity {
    private String accessToken;

    NsdHelper nsdHelper;

    private Handler handler;

    public static final String TAG = "NsdJoin";

    PlaylistConnection connection;

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

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");
        connection = new PlaylistConnection(handler);
        nsdHelper = new NsdHelper(this);
        nsdHelper.initializeNsd();

        nsdHelper.discoverServices();

        NsdServiceInfo service = nsdHelper.getChosenServiceInfo();

        if(service != null) {
            Log.d(TAG, "Connecting.");
            connection.connectToServer(service.getHost(), service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }

        UserProfile prof = new UserProfile(accessToken);
        connection.sendProfile(prof);
        super.onStart();
    }
    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (nsdHelper != null) {
            nsdHelper.stopDiscovery();
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming.");
        super.onResume();
        if (nsdHelper != null) {
            nsdHelper.discoverServices();
        }
    }

    private void GoToMenu(String token)
    {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }
}
