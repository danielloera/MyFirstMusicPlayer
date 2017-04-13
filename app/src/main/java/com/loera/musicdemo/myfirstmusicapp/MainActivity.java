package com.loera.musicdemo.myfirstmusicapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import ir.sohreco.androidfilechooser.ExternalStorageNotAvailableException;
import ir.sohreco.androidfilechooser.FileChooserDialog;


public class MainActivity extends AppCompatActivity implements FileChooserDialog.ChooserListener{

    private boolean playing, localPlayback;
    private TextView artistAlbumText, songTitleText;
    private ImageView albumArt;
    private ImageButton playbackButton;

    private MediaPlayer mediaPlayer;

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
        playbackButton = (ImageButton) findViewById(R.id.playback_button);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Ask for permissions to storage for local playback.
        askFilePermission();
        //Setup our Spotify API.

    }

    private void askFilePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 25);
        } else {
            // Your app already has the permission to access files and folders // so you can simply open FileChooser here.
        }


    }

    public void newSongButtonClicked(View view) {

        if(localPlayback) {
            // need to open file explorer to choose new file.
            getMusicFile();
        } else {
            // need to open Spotify search to choose new song.
        }

    }

    private void getMusicFile() {
        FileChooserDialog.Builder builder = new FileChooserDialog.Builder(FileChooserDialog.ChooserType.FILE_CHOOSER, this);
        try {
            builder.build().show(getSupportFragmentManager(), null);
        } catch (ExternalStorageNotAvailableException e) {
            e.printStackTrace();
        }
    }

    //selecting music file
    @Override
    public void onSelect(String path) {
        playNewSong(path);
        updatePlayerUI(path);
    }

    private void updatePlayerUI(String path) {
        MediaMetadataRetriever mm = new MediaMetadataRetriever();
        mm.setDataSource(path);

        String albumName = mm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = mm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String songTitle = mm.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        songTitleText.setText(songTitle);
        artistAlbumText.setText(artist + " - " + albumName);

        byte[] artBytes =  mm.getEmbeddedPicture();
        if(artBytes!=null)
        {
            //     InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
            Bitmap bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            albumArt.setImageBitmap(bm);
        }
        else
        {
            albumArt.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void playNewSong(String path) {
        mediaPlayer.stop();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            playbackButtonClicked(playbackButton);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void playbackButtonClicked(View view) {
        ImageButton playbackButton = (ImageButton) view;
        int iconId = 0;
        if(localPlayback) {
            // need to pause or play local media player.
            if(playing) {
                iconId = R.drawable.ic_play_button;
                mediaPlayer.pause();
            } else {
                iconId = R.drawable.ic_pause_button;
                mediaPlayer.start();
            }

        } else {
            // need to pause or play Spotify player.
        }
        playing = !playing;
        playbackButton.setImageResource(iconId);
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
