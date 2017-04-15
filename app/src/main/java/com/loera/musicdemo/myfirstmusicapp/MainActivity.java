package com.loera.musicdemo.myfirstmusicapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import ir.sohreco.androidfilechooser.ExternalStorageNotAvailableException;
import ir.sohreco.androidfilechooser.FileChooserDialog;


public class MainActivity extends AppCompatActivity implements FileChooserDialog.ChooserListener{

    private final int SPOTIFY_REQUEST_CODE = 22;
    private final String CLIENT_ID = "2624d481fecf46369311bd0dd62c1473";
    private final String TAG = "MainActivity";

    private boolean playing, localPlayback;
    private TextView artistAlbumText, songTitleText;
    private ImageView albumArt;
    private ImageButton playbackButton, sourceButton;

    private SharedPreferences sharedPreferences;

    private String access_token;
    private boolean spotifyPlayerInitialized;

    private MediaPlayer mediaPlayer;
    private SpotifyPlayer spotifyPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("default", MODE_PRIVATE);
        // Gather player components.
        playing = false;
        localPlayback = true;
        artistAlbumText = (TextView) findViewById(R.id.artist_album_text);
        songTitleText = (TextView) findViewById(R.id.song_title);
        albumArt = (ImageView) findViewById(R.id.album_art);
        playbackButton = (ImageButton) findViewById(R.id.playback_button);
        sourceButton = (ImageButton) findViewById(R.id.source_button);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Ask for permissions to storage for local playback.
        askFilePermission();
        //Setup our Spotify API.
        access_token = sharedPreferences.getString("access_token", null);
        if(access_token == null)
            authenticateSpotify();
        else
            setupSpotifyPlayer();

        Intent intent = getIntent();
        if(intent.getStringExtra("uri") != null) {
            playSpotifySong(intent);
            sourceButtonClicked(null);
            playing = false;
            playbackButtonClicked(null);
        }
    }

    private void playSpotifySong(Intent intent) {
        String trackTitle = intent.getStringExtra("track");
        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String albumURL = intent.getStringExtra("album_art");
        String uri = intent.getStringExtra("uri");

        songTitleText.setText(trackTitle);
        artistAlbumText.setText(artist + " - " + album);
        Picasso.with(getApplicationContext()).load(albumURL).into(albumArt);
        spotifyPlayer.playUri(uri, 0, 0);
        playing = true;

    }

    private void authenticateSpotify() {

        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, "https://www.google.com")
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();
        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == SPOTIFY_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    access_token = response.getAccessToken();
                    sharedPreferences.edit().putString("access_token", access_token).apply();
                    setupSpotifyPlayer();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.i(TAG, "Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.i(TAG, "auth flow cancelled.");
            }
        }
    }

    private void setupSpotifyPlayer() {
        if (spotifyPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), access_token, CLIENT_ID);
            spotifyPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    spotifyPlayerInitialized = true;
                }

                @Override
                public void onError(Throwable error) {
                    Log.i(TAG, "Error in initialization: " + error.getMessage());
                    authenticateSpotify();
                }

            });
        } else {
            spotifyPlayer.login(access_token);
        }
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
            Intent intent = new Intent(getApplicationContext(), SpotifySearchActivity.class);
            intent.putExtra("access_token", access_token);
            startActivity(intent);
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
            if(playing) {
                iconId = R.drawable.ic_play_button;
                spotifyPlayer.pause();
            } else {
                iconId = R.drawable.ic_pause_button;
                spotifyPlayer.resume();
            }
        }
        playing = !playing;
        playbackButton.setImageResource(iconId);
    }

    public void sourceButtonClicked(View view) {
        // State has changed.
        localPlayback = !localPlayback;

        // Display new source icon.
        int iconId = localPlayback ? R.drawable.ic_folder : R.drawable.ic_spotify;
        sourceButton.setImageResource(iconId);
    }


}
