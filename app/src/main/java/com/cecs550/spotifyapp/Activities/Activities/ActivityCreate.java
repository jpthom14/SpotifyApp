package com.cecs550.spotifyapp.Activities.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cecs550.spotifyapp.Activities.Classes.NsdHelper;
import com.cecs550.spotifyapp.Activities.Classes.PlaylistConnection;
import com.cecs550.spotifyapp.Activities.Classes.UserProfile;
import com.cecs550.spotifyapp.R;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ActivityCreate extends AppCompatActivity {

    /**
     * Created by John on 4/18/2017.
     */
    private String hostToken;
    private ArrayList<String> Tokens;

    NsdHelper nsdHelper;

    private Handler handler;

    public static final String TAG = "NsdChat";

    PlaylistConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Tokens = new ArrayList<>();

        Intent intent = getIntent();
        hostToken = intent.getStringExtra("token");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
            }
        };

        Button finalCreateButton = (Button) findViewById(R.id.make_playlist);
        finalCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatePopup();
            }
        });

        Button cancelCreateButton = (Button) findViewById(R.id.cancel_create_playlist);
        cancelCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToMenu(hostToken);
            }
        });

        GetOthersUsersTokens();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");
        connection = new PlaylistConnection(handler);
        nsdHelper = new NsdHelper(this);
        nsdHelper.initializeNsd();

        if(connection.getLocalPort() > -1) {
            nsdHelper.registerService(connection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }

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

    private void GetOthersUsersTokens() {

    }

    private void SetNumberConnectedText(int i){
        TextView numUsersText = (TextView) findViewById(R.id.users_joined_text);
        numUsersText.setText(Integer.toString(i));
    }

    private void showCreatePopup() {
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
                UserProfile hostProfile = new UserProfile(hostToken);

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

    private void GoToMenu(String token) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "Being stopped.");
        connection.tearDown();
        nsdHelper = null;
        connection = null;
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        super.onDestroy();
    }
}
