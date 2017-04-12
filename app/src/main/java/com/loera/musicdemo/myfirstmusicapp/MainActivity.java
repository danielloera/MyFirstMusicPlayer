package com.loera.musicdemo.myfirstmusicapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean playing, localPlayback;
    private TextView artistAlbumText, songTitleText;
    private ImageView albumArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gather player components.
        playing = false;
        localPlayback = true;
        artistAlbumText = (TextView) findViewById(R.id.artist_album_text);
        songTitleText = (TextView) findViewById(R.id.song_title);
        albumArt = (ImageView) findViewById(R.id.album_art);

        //Ask for permissions to storage for local playback.

        //Setup our Spotify API.

    }

    public void newSongButtonClicked(View view) {

        if(localPlayback) {
            // need to open file explorer to choose new file.
        } else {
            // need to open Spotify search to choose new song.
        }

    }

    public void playbackButtonClicked(View view) {

        if(localPlayback) {
            // need to pause or play local media player.
        } else {
            // need to pause or play Spotify player.
        }

    }

    public void sourceButtonClicked(View view) {
        // State has changed.
        localPlayback = !localPlayback;

        // Display new source icon.
        ImageButton sourceButton = (ImageButton) view;
        int iconId = localPlayback ? R.drawable.ic_folder : R.drawable.ic_spotify;
        sourceButton.setImageResource(iconId);
    }
}
