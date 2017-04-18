package com.cecs550.spotifyapp.Activities.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cecs550.spotifyapp.R;

public class JoinActivity extends AppCompatActivity {
    private String accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("token");

        Button cancelButton = (Button) findViewById(R.id.cancel_create_playlist);
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
