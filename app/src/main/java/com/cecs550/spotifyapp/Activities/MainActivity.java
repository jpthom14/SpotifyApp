package com.cecs550.spotifyapp.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cecs550.spotifyapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private String Username;
    private String Password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoLoginStuff();
            }
        });
    }

    private void DoLoginStuff() {
        EditText user = (EditText) findViewById(R.id.username_id);
        EditText pass = (EditText) findViewById(R.id.password_id);
        Username = user.getText().toString();
        Password = pass.getText().toString();
        String token = getSpotifyAccessToken();
    }


    private String getAccessTokenFromJsonStr(String spotifyJsonStr) throws JSONException {
        final String OWM_ACCESS_TOKEN = "access_token";
        String accessToken = "";

        try {
            JSONObject spotifyJson = new JSONObject(spotifyJsonStr);
            accessToken = spotifyJson.getString(OWM_ACCESS_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    private String getSpotifyAccessToken() {
        String response = "";
        String accessToken = "";
        try {
            String serviceURL = "https://accounts.spotify.com/api/token";
            URL myURL = new URL(serviceURL);

            HttpsURLConnection myURLConnection = (HttpsURLConnection) myURL.openConnection();

            String userCredentials = Username + ":" + Password;
            int flags = Base64.NO_WRAP | Base64.URL_SAFE;
            byte[] encodedString = Base64.encode(userCredentials.getBytes(), flags);
            String basicAuth = "Basic " + new String(encodedString);
            myURLConnection.setRequestProperty("Authorization", basicAuth);

            myURLConnection.setRequestMethod("POST");
            myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            myURLConnection.setUseCaches(false);
            myURLConnection.setDoInput(true);
            myURLConnection.setDoOutput(true);
            System.setProperty("http.agent", "");

            HashMap postDataParams = new HashMap<String, String>();
            postDataParams.put("grant_type", "client_credentials");
            OutputStream os = myURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            response = "";
            int responseCode = myURLConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
                String errLine;
                String errResponse = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(myURLConnection.getErrorStream()));
                while ((errLine = br.readLine()) != null) {
                    errResponse += errLine;
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        String accessTokenJsonStr = response.toString();
        try {
            accessToken = getAccessTokenFromJsonStr(accessTokenJsonStr);
            return accessToken;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
