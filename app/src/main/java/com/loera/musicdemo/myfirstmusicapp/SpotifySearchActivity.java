package com.loera.musicdemo.myfirstmusicapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifySearchActivity extends AppCompatActivity {

    private final String TAG = "SpotifySearchActivity";

    private String access_token;
    private SearchView searchView;
    private ListView listView;
    private SpotifyService spotifyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_search);

        searchView = (SearchView) findViewById(R.id.search_view);
        listView = (ListView) findViewById(R.id.results_list_view);

        Intent intent = getIntent();
        if(intent != null) {
            access_token = intent.getStringExtra("access_token");
            SpotifyApi spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(access_token);
            spotifyService = spotifyApi.getService();
            setupUI();
        } else {
            onBackPressed();
        }
    }

    private void setupUI() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                makeTrackSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void makeTrackSearch(String query) {
        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, 0);
        options.put(SpotifyService.LIMIT, 50);

        spotifyService.searchTracks(query, options, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                displayTracks(tracksPager.tracks.items);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(TAG, "failed search.");
            }
        });
    }

    private void displayTracks(final List<Track> items) {
        String[] trackStrings = new String[items.size()];
        for(int i = 0; i < trackStrings.length; i++) {
            Track currentTrack = items.get(i);
            trackStrings[i] = currentTrack.name + " - " + currentTrack.album.name;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                        R.layout.track_item, R.id.track_text_view, trackStrings);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendToPlayer(items.get(position));
            }
        });

        listView.setAdapter(adapter);
    }

    private void sendToPlayer(Track track) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("track", track.name);
        intent.putExtra("artist", track.artists.get(0).name);
        intent.putExtra("album", track.album.name);
        intent.putExtra("album_art", track.album.images.get(0).url);
        intent.putExtra("uri", track.uri);
        startActivity(intent);

    }
}
