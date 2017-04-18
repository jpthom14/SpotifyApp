package com.cecs550.spotifyapp.Activities.Activities;

import android.content.DialogInterface;
import android.content.Intent;
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

public class ActivityCreate extends AppCompatActivity {

    /**
     * Created by John on 4/18/2017.
     */
        private String accessToken;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create);

            Intent intent = getIntent();
            accessToken = intent.getStringExtra("token");

            //receive from other devices

            Button finalCreateButton = (Button) findViewById(R.id.make_playlist);
            finalCreateButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    showCreatePopup();
                }
            });

            Button cancelCreateButton = (Button) findViewById(R.id.cancel_create_playlist);
            cancelCreateButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    GoToMenu(accessToken);
                }
            });
        }

    private void showCreatePopup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Title for Playlist");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; thi
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                UserProfile hostProfile = new UserProfile(accessToken);

                hostProfile.SetupProfile(title);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    private void GoToMenu(String token)
    {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }
}
