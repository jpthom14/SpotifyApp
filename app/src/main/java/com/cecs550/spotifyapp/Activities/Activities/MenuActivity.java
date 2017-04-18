package com.cecs550.spotifyapp.Activities.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.cecs550.spotifyapp.Activities.Classes.UserProfile;
import com.cecs550.spotifyapp.R;


public class MenuActivity extends AppCompatActivity {
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("token");

        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToCreatePlaylist(accessToken);
            }
        });

        Button joinButton = (Button) findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToJoinPlaylist(accessToken);
            }
        });

}

    private void GoToCreatePlaylist(String token)
    {
        Intent intent = new Intent(this, ActivityCreate.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    private void GoToJoinPlaylist(String token)
    {
        Intent intent = new Intent(this, JoinActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }


}
