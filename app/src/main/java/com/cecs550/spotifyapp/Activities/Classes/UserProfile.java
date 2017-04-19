package com.cecs550.spotifyapp.Activities.Classes;

import android.os.AsyncTask;
import android.view.Menu;

import com.cecs550.spotifyapp.Activities.Activities.MenuActivity;

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
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Result;
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
    private SpotifyApi hostApi;
    private SpotifyService spotify;
    private SpotifyService hostSpotify;
    private final int Limit = 20;

    public String GuestID;
    public String HostID;
    public String HostToken;
    public String GuestToken;
    public ArrayList<SavedTrack> SavedTrackArrayList;
    public ArrayList<PlaylistSimple> PlaylistArrayList;
    public ArrayList<Track> RecommendedTracks;
    public ArrayList<SavedTrack> HostSavedTracks;
    public String PlaylistID;
    public String PlaylistTitle;

    public UserProfile(String host, String guest)
    {
        //Set up for api use
        HostToken = host;
        GuestToken = guest;
        api = new SpotifyApi();
        api.setAccessToken(guest);
        spotify = api.getService();

        hostApi = new SpotifyApi();
        hostApi.setAccessToken(host);
        hostSpotify = hostApi.getService();

        //Inititalize properties
        SavedTrackArrayList = new ArrayList<>();
        PlaylistArrayList = new ArrayList<>();
        RecommendedTracks = new ArrayList<>();
        HostSavedTracks = new ArrayList<>();
    }

    public void SetupProfiles(String title){
        PlaylistTitle = title;
        spotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                GuestID = userPrivate.id;
                SetPlaylists(0);
            }

            @Override
            public void failure(RetrofitError error) {
                String ecror = error.getMessage();

            }
        });
    }

    private void GuestFollowPlaylist(){

        spotify.followPlaylist(HostID, PlaylistID, new Callback<Result>() {
            @Override
            public void success(Result result, Response response) {
                AddTracksToPlaylist();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void CreatePlaylist(){
        Map<String, Object> options = new HashMap<>();
        options.put("name", PlaylistTitle);

        hostSpotify.createPlaylist(HostID, options, new Callback<Playlist>() {
            @Override
            public void success(Playlist playlist, Response response) {
                PlaylistID = playlist.id;
                GuestFollowPlaylist();
            }

            @Override
            public void failure(RetrofitError error) {
                String test = "";
            }
        });
    }

    private void AddTracksToPlaylist(){
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> body = new HashMap<>();

        ArrayList<String> tracksToAdd = new ArrayList<>();

        for(int i =0; i < RecommendedTracks.size(); i++){
            Track track = RecommendedTracks.get(i);
            tracksToAdd.add(track.uri);
        }

        body.put("uris", tracksToAdd.toArray());

        hostSpotify.addTracksToPlaylist(HostID, PlaylistID, query, body, new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                MenuActivity.statusText.setText("Playlist Created");
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

    private void SetHost(){
        hostSpotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                HostID = userPrivate.id;
                SetHostSavedTracks(0);
            }

            @Override
            public void failure(RetrofitError error) {
                String ecror = error.getMessage();

            }
        });
    }

    private void SetHostSavedTracks(int offset){
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.LIMIT, Limit);
        options.put(SpotifyService.OFFSET, offset);

        hostSpotify.getMySavedTracks(options, new SpotifyCallback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                // handle successful response
                for (int i=0; i<savedTrackPager.items.size(); i++) {
                    HostSavedTracks.add(savedTrackPager.items.get(i));
                }
                if(savedTrackPager.next != null){
                    SetHostSavedTracks(savedTrackPager.offset + Limit);
                    return;
                }
                SetHostRecommendations();
            }

            @Override
            public void failure(SpotifyError error) {
                // handle error
                String text = error.getMessage();
            }
        });
    }

    private void SetHostRecommendations(){
        Map<String, Object> options = new HashMap<>();

        String trackList = "";

        //for some reason this doesn't work with comma seperated stuff, but it works with single track
        for(int i = 0; i < 1; i++){
            SavedTrack track = HostSavedTracks.get(i);
            String id = track.track.id;

            if(i == 0) trackList += id;
            else{
                trackList+= ", " + id;
            }
        }

        options.put("seed_tracks", trackList);

        //you can put stuff like _target danceability in the options i think

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

    private void SetRecommendations(){
        Map<String, Object> options = new HashMap<>();

        String trackList = "";

        //for some reason this doesn't work with comma seperated stuff, but it works with single track
        for(int i = 0; i < 1; i++){
            SavedTrack track = SavedTrackArrayList.get(i);
            String id = track.track.id;

            if(i == 0) trackList += id;
            else{
                trackList+= ", " + id;
            }
        }

        options.put("seed_tracks", trackList);

        //you can put stuff like _target danceability in the options i think

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
                SetHost();
            }
        });
    }


}
