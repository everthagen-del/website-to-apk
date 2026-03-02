package com.treinverstoringen.app;
import com.treinverstoringen.app.R;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.media.app.NotificationCompat.MediaStyle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    
    private MediaPlayer mediaPlayer;
    private List<String> playlist = new ArrayList<>();
    private int currentTrack = 0;
    private static final String CHANNEL_ID = "media_playback_channel";
    private static final int NOTIFICATION_ID = 1;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        
        createNotificationChannel();
        setupMediaSession();
    }
    
    // VERPLICHTE onBind METHODE VOOR SERVICE
    @Override
    public IBinder onBind(Intent intent) {
        return null;  // We gebruiken geen binding, dus return null
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "PLAY":
                        play();
                        break;
                    case "PAUSE":
                        pause();
                        break;
                    case "NEXT":
                        next();
                        break;
                    case "PREVIOUS":
                        previous();
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }
    
    private void setupMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MediaPlaybackService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                play();
            }
            
            @Override
            public void onPause() {
                pause();
            }
            
            @Override
            public void onSkipToNext() {
                next();
            }
            
            @Override
            public void onSkipToPrevious() {
                previous();
            }
        });
    }
    
    public void setPlaylist(List<String> urls) {
        playlist.clear();
        playlist.addAll(urls);
        currentTrack = 0;
    }
    
    public void play() {
        if (playlist.isEmpty()) return;
        
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(playlist.get(currentTrack));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification(false);
        }
    }
    
    public void next() {
        if (currentTrack < playlist.size() - 1) {
            currentTrack++;
            play();
        }
    }
    
    public void previous() {
        if (currentTrack > 0) {
            currentTrack--;
            play();
        }
    }
    
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        updateNotification(true);
    }
    
    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }
    
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private void updateNotification(boolean isPlaying) {
        Intent playIntent = new Intent(this, MediaPlaybackService.class);
        playIntent.setAction(isPlaying ? "PAUSE" : "PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Intent nextIntent = new Intent(this, MediaPlaybackService.class);
        nextIntent.setAction("NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 1, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Intent prevIntent = new Intent(this, MediaPlaybackService.class);
        prevIntent.setAction("PREVIOUS");
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 2, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Media Speler")
                .setContentText("Nummertje " + (currentTrack + 1))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(new Action(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent))
                .addAction(new Action(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying ? "Pause" : "Play", playPendingIntent))
                .addAction(new Action(android.R.drawable.ic_media_next, "Next", nextPendingIntent))
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));
        
        if (isPlaying) {
            builder.setOngoing(true);
            startForeground(NOTIFICATION_ID, builder.build());
        } else {
            stopForeground(false);
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
    }
}
