package com.afrozaar.musiclab;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

/**
 * Created by jay on 12/23/14.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String ACTION_PLAY = "com.example.action.PLAY";

    MediaPlayer mMediaPlayer = null;

    public MusicService(){

    }

    @Override
    public void onCreate (){
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId){

        if(intent.getAction().equals(ACTION_PLAY)){
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(this,);

            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
