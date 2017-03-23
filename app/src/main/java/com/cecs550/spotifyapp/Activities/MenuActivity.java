package com.cecs550.spotifyapp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cecs550.spotifyapp.R;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import retrofit.client.Response;

public class MenuActivity extends AppCompatActivity {

    private static String accessToken;
    private static SpotifyApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("token");
        api = new SpotifyApi();
        api.setAccessToken(accessToken);

        GetSavedTracks();

    }

    private void GetSavedTracks(){
        SpotifyService spotify = api.getService();

        spotify.getMySavedTracks(new SpotifyCallback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                // handle successful response
                //put a breakpoint in here and you will be able to see your saved tracks
                for (int i=0; i<savedTrackPager.items.size(); i++) {
                    SavedTrack track = savedTrackPager.items.get(i);
                    String trackName = track.track.name;
                }
            }

            @Override
            public void failure(SpotifyError error) {
                // handle error
                String test = "test";

            }
        });

    }
}
