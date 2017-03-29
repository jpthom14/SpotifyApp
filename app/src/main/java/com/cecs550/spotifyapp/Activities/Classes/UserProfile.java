package com.cecs550.spotifyapp.Activities.Classes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Jeremy on 3/28/2017.
 */

public class UserProfile {

    private SpotifyApi api;
    private SpotifyService spotify;
    private final int Limit = 20;

    public volatile boolean IsReady = false;

    public String User;
    public String AccessToken;
    public ArrayList<SavedTrack> SavedTrackArrayList;
    public ArrayList<PlaylistSimple> PlaylistArrayList;
    public ArrayList<Track> RecommendedTracks;

    public UserProfile(String token)
    {
        //Set up for api use
        AccessToken = token;
        api = new SpotifyApi();
        api.setAccessToken(AccessToken);
        spotify = api.getService();

        //Inititalize properties
        SavedTrackArrayList = new ArrayList<>();
        PlaylistArrayList = new ArrayList<>();
        RecommendedTracks = new ArrayList<>();


    }

    public void SetupProfile(){
        spotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                User = userPrivate.id;
                SetPlaylists(0);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }

    private void CreatePlaylist(){
        Map<String, Object> options = new HashMap<>();
        options.put("name", "it actually worked!");

        spotify.createPlaylist(User, options, new Callback<Playlist>() {
            @Override
            public void success(Playlist playlist, Response response) {
                String name = playlist.name;
            }

            @Override
            public void failure(RetrofitError error) {
                String test = "";
            }
        });
    }

    private void SetPlaylists(int offset){
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.LIMIT, Limit);
        options.put(SpotifyService.OFFSET, offset);

        spotify.getMyPlaylists(options, new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                //handle error
                String error = spotifyError.getMessage();
            }

            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                for (int i=0; i<playlistSimplePager.items.size(); i++) {
                    PlaylistArrayList.add(playlistSimplePager.items.get(i));
                }
                if(playlistSimplePager.next != null){
                    SetPlaylists(playlistSimplePager.offset + Limit);
                    return;
                }
                SetSavedTracks(0);
            }
        });
    }

    private void SetSavedTracks(int offset){
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.LIMIT, Limit);
        options.put(SpotifyService.OFFSET, offset);

        spotify.getMySavedTracks(options, new SpotifyCallback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                // handle successful response
                for (int i=0; i<savedTrackPager.items.size(); i++) {
                    SavedTrackArrayList.add(savedTrackPager.items.get(i));
                }
                if(savedTrackPager.next != null){
                    SetSavedTracks(savedTrackPager.offset + Limit);
                    return;
                }
                SetRecommendations();
            }

            @Override
            public void failure(SpotifyError error) {
                // handle error
                String text = error.getMessage();
            }
        });
    }

    private void SetRecommendations(){
        Map<String, Object> options = new HashMap<>();

        //right now this is a random seed artist i found, should be
        //changed to use seeds from the Spotify User's favorite artists, tracks, and albums
        options.put("seed_artists", "4cJKxS7uOPhwb5UQ70sYpN,6UUrUCIZtQeOf8tC0WuzRy");

        spotify.getRecommendations(options, new SpotifyCallback<Recommendations>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                String error = spotifyError.getMessage();
            }

            @Override
            public void success(Recommendations recommendations, Response response) {
                // handle successful response
                for (int i=0; i<recommendations.tracks.size(); i++){
                    RecommendedTracks.add(recommendations.tracks.get(i));
                }
                CreatePlaylist();
            }
        });
    }
}
