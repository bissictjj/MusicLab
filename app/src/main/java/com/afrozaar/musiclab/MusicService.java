package com.afrozaar.musiclab;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by jay on 12/23/14.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {

    private static final String ACTION_PLAY = "com.example.action.PLAY";

    private static final String LOG_TAG = MusicService.class.getName();

    private static final String SERVICE_PREFIX = "com.afrozaar.musiclab.";

    public static final String SERVICE_CHANGE_NAME = SERVICE_PREFIX + "CHANGE";
    public static final String SERVICE_CLOSE_NAME = SERVICE_PREFIX + "CLOSE";
    public static final String SERVICE_UPDATE_NAME = SERVICE_PREFIX + "UPDATE";
    public static final String SERVICE_ERROR_NAME = SERVICE_PREFIX + "ERROR";

    public static final String SERVICE_PREPARE_PLAYER = SERVICE_PREFIX + "PREPARE_PLAYER";
    public static final String SERVICE_PLAY_SINGLE = SERVICE_PREFIX +
            "PLAY_SINGLE";
    public static final String SERVICE_PLAY_ENTRY = SERVICE_PREFIX + "PLAY_ENTRY";
    public static final String SERVICE_TOGGLE_PLAY = SERVICE_PREFIX +
            "TOGGLE_PLAY";
    public static final String SERVICE_RESUME_PLAYING = SERVICE_PREFIX +
            "RESUME_PLAYING";
    public static final String SERVICE_PAUSE = SERVICE_PREFIX + "PAUSE";
    public static final String SERVICE_SEEK_TO = SERVICE_PREFIX + "SEEK_TO";
    public static final String SERVICE_PLAY_NEXT = SERVICE_PREFIX + "PLAYNEXT";
    public static final String SERVICE_PLAY_PREVIOUS = SERVICE_PREFIX +
            "PLAYPREVIOUS";
    public static final String SERVICE_STOP_PLAYBACK = SERVICE_PREFIX + "STOP_PLAYBACK";
    public static final String SERVICE_STATUS = SERVICE_PREFIX + "STATUS";
    public static final String SERVICE_CLEAR_PLAYER = SERVICE_PREFIX +
            "CLEAR_PLAYER";

    public static final String EXTRA_DOWNLOADED = SERVICE_PREFIX + "DOWNLOADED";
    public static final String EXTRA_DURATION = SERVICE_PREFIX + "DURATION";
    public static final String EXTRA_POSITION = SERVICE_PREFIX + "POSITION";
    public static final String EXTRA_SEEK_TO = SERVICE_PREFIX + "SEEK_TO";
    public static final String EXTRA_IS_PLAYING = SERVICE_PREFIX + "IS_PLAYING";
    public static final String EXTRA_IS_PREPARED = SERVICE_PREFIX + "IS_PREPARED";
    public static final String EXTRA_KEEP_AUDIO_FOCUS = SERVICE_PREFIX + "KEEP_AUDIO_FOCUS";

    public static final String EXTRA_ERROR = SERVICE_PREFIX + "ERROR";

    private static final int NOTIFY_ID=1;

    public String songTitle;


    public static enum PLAYBACK_SERVICE_ERROR {Connection, Playback, InvalidPlayable}

    private WifiManager.WifiLock wifiLock;
    private int startId;
    private MediaPlayer mMediaPlayer = null;
    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;

    private int seekToPosition;
    private int songPosition = 0;
    private boolean isPrepared = false;
    private boolean isPausedInCall = false;
    private boolean mediaPlayerHasStarted = false;
    private String currentAction;

    // Amount of time to rewind playback when resuming after call
    private final static int RESUME_REWIND_TIME = 3000;
    private final static int ERROR_RETRY_COUNT = 3;
    private final static int RETRY_SLEEP_TIME = 30000;



    private List<MusicUtils.SongData> mPlaylist;
    private MusicUtils musicUtils;

    private final IBinder mBinder = new LocalBinder();
    private final Random mGenerator = new Random();

    public class LocalBinder extends Binder{
        MusicService getService(){
            //This returns the MusicService so clients can call public methods linked to this class.
            return MusicService.this;
        }
    }

    //This creates a worker thread so that the UI isn't stalled if the music service is taking long to do the work it needs to.
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(LOG_TAG,"Handle msg called");
            startId = msg.arg1;
            onHandleIntent((Intent) msg.obj);
        }
    }

    public MusicService() {

    }

    @Override
    public void onCreate() {
        Log.d("DEBUG","OnCreate() in service called " + LOG_TAG);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //put appropriate msg here
        }
        musicUtils = new MusicUtils(getApplicationContext());
        mPlaylist = musicUtils.getSongData();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);

        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // Create a PhoneStateListener to watch for off-hook and idle events
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        // Phone going off-hook or ringing, pause the player.
                        if (isPlaying()) {
                            pause();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Rewind a couple of seconds and start playing.
                        if (isPausedInCall) {
                            isPausedInCall = false;
                            seekTo(Math.max(0, getPosition() - RESUME_REWIND_TIME));
                            play();
                        }
                        break;
                }
            }
        };

        // Register the listener with the telephony manager.
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        HandlerThread thread = new HandlerThread("MusicService:WorkerThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    synchronized public boolean isPlaying() {
        return isPrepared && mMediaPlayer.isPlaying();
    }

    synchronized public void play() {
        if (!isPrepared) {
            Log.e(LOG_TAG, "play - not prepared");
            return;
        } else {
            mMediaPlayer.start();
            mediaPlayerHasStarted = true;

            Intent notIntent = new Intent(this, HomeActivity.class);
            notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                    notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(this);
            songTitle = musicUtils.getSongTitle(musicUtils.getCurrSongPosition());
            builder.setContentIntent(pendInt)
                   // .setSmallIcon(R.drawable.play)
                    .setTicker(songTitle)
                    .setOngoing(true)
                    .setContentTitle("HELLO")
            .setContentText(songTitle);
            Notification not = builder.build();

            startForeground(NOTIFY_ID, not);
        }
    }

    synchronized public int getPosition() {
        if (isPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    synchronized public void seekRelative(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + pos);
        }
    }

    synchronized public void playNext(){
        if(isPrepared && (songPosition+1) <= musicUtils.getPlaylistSize()){

            try {
                songPosition++;
                prepareThenPlay(musicUtils.getSongId(songPosition),false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void playPrevious(){
        if(isPrepared && songPosition > 0){
            try {
                songPosition--;
                prepareThenPlay(musicUtils.getSongId(songPosition),false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    synchronized public void seekTo(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mMediaPlayer.seekTo(pos);
        }
    }

    synchronized public void pause() {
        Log.d(LOG_TAG, "pause");
        mMediaPlayer.pause();
        /*if (isPrepared) {
            isPrepared = false;
            mMediaPlayer.stop();
        } else {
            mMediaPlayer.pause();
        }*/
    }

    synchronized public void stop() {
        Log.d(LOG_TAG, "stop");
        if (isPrepared) {
            isPrepared = false;
            mMediaPlayer.stop();
        }
    }

    public boolean getPlayState(){
        return isPlaying();
    }

    private void resumePlaying() {
        if (isPrepared) {
            play();
        } else {
            Log.d(LOG_TAG, "nothing to resume");
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
            Message message = serviceHandler.obtainMessage();
            message.arg1 = startId;
            message.obj = intent;
            serviceHandler.sendMessage(message);
        return 0;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared called");
        synchronized (this) {
            if (mMediaPlayer != null) {
                isPrepared = true;
                play();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        wifiLock.release();

        synchronized (this) {
            if (mMediaPlayer != null) {
                if (mediaPlayerHasStarted) {
                    mMediaPlayer.release();
                } else {
                    mMediaPlayer.setOnBufferingUpdateListener(null);
                    mMediaPlayer.setOnCompletionListener(null);
                    mMediaPlayer.setOnErrorListener(null);
                    mMediaPlayer.setOnInfoListener(null);
                    mMediaPlayer.setOnPreparedListener(null);
                    mMediaPlayer.setOnSeekCompleteListener(null);
                }
                mMediaPlayer = null;
            }
        }
        stopForeground(true);
        serviceLooper.quit();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    protected void onHandleIntent(Intent intent){
        Log.d(LOG_TAG,"SONG POSITION: "+songPosition);
        if (intent == null || intent.getAction() == null) {
            Log.d(LOG_TAG, "Null intent received");
            return;
        }
        String action = intent.getAction();

        Log.d(LOG_TAG, "Playback service action received: " + action);
        currentAction = action;
        if(action.equals(SERVICE_PREPARE_PLAYER))
        {
            Log.d(LOG_TAG,"MusicService connection established");
        }
        if (action.equals(SERVICE_TOGGLE_PLAY)) {
            if (isPrepared) {
                if (isPlaying()) {
                    pause();
                    // Get rid of the toggle intent, since we don't want it redelivered
                    // on restart
                    Intent emptyIntent = new Intent(intent);
                    emptyIntent.setAction("");
                    startService(emptyIntent);
                } else {
                    Intent emptyIntent = new Intent(intent);
                    emptyIntent.setAction("");
                    startService(emptyIntent);
                    play();
                }
            } else {

                try {
                    prepareThenPlay(0, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else if (action.equals(SERVICE_RESUME_PLAYING)) {
            resumePlaying();
        } else if (action.equals(SERVICE_PAUSE)) {
            if (isPlaying()) {
                pause();
            }
        } else if (action.equals(SERVICE_SEEK_TO)) {
            seekTo(intent.getIntExtra(EXTRA_SEEK_TO, 0));
        } else if (action.equals(SERVICE_PLAY_NEXT)) {
            seekToPosition = 0;
            playNext();
        } else if (action.equals(SERVICE_PLAY_PREVIOUS)) {
            seekToPosition = 0;
            playPrevious();
        }else if(action.equals(SERVICE_PLAY_SINGLE)){
            long tId;

            if(intent.getLongExtra("SongId",0) != 0){
                 tId = intent.getLongExtra("SongId",0);
            }else{
                 tId = musicUtils.getSongId(intent.getIntExtra("SongPosition",0));
            }


            try {
                prepareThenPlay(tId,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
         /*else if (action.equals(SERVICE_STOP_PLAYBACK)) {
            stopSelfResult(startId);
        } else if (action.equals(SERVICE_STATUS)) {
            updateProgress();
        } else if (action.equals(SERVICE_CLEAR_PLAYER)) {
            if (!isPlaying()) {
                stopSelfResult(startId);
            }
        }*/
        }

    }

    public int getCurrentPos()
    {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDur()
    {
        return mMediaPlayer.getDuration();

    }

    public void prepareThenPlay(long id, boolean stream)
            throws IllegalArgumentException, IllegalStateException, IOException {
        // First, clean up any existing audio.
        stop();
        Uri currentSong;

        if(id == 0){
            currentSong = musicUtils.getRandomSongUri();
            songPosition = musicUtils.getCurrSongPosition();
        }else{
            currentSong = musicUtils.getSongUri(id);
            songPosition = musicUtils.getSongPosition(id);
        }

        /*if (isPlaylist(url)) {
            new downloadPlaylist().execute(url);
            return;
        }*/

        synchronized (this) {
            Log.d(LOG_TAG, "reset: " + currentSong.toString());
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(getApplicationContext(),currentSong);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.d(LOG_TAG, "Preparing: " + currentSong.toString());
            mMediaPlayer.prepareAsync();
            Log.d(LOG_TAG, "Waiting for prepare");
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) try {
                    prepareThenPlay(0, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if(mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    break;
                }
            break;
        }
    }
}
