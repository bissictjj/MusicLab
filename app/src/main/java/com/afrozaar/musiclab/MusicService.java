package com.afrozaar.musiclab;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created by jay on 12/23/14.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {

    private static final String ACTION_PLAY = "com.example.action.PLAY";

    private static final String LOG_TAG = MusicService.class.getName();

    private static final String SERVICE_PREFIX = "com.afrozaar.musiclab.";

    public static final String SERVICE_CHANGE_NAME = SERVICE_PREFIX + "CHANGE";
    public static final String SERVICE_CLOSE_NAME = SERVICE_PREFIX + "CLOSE";
    public static final String SERVICE_UPDATE_NAME = SERVICE_PREFIX + "UPDATE";
    public static final String SERVICE_ERROR_NAME = SERVICE_PREFIX + "ERROR";

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

    public static enum PLAYBACK_SERVICE_ERROR {Connection, Playback, InvalidPlayable}

    private WifiManager.WifiLock wifiLock;
    private int startId;
    private MediaPlayer mMediaPlayer = null;
    private List<MusicUtils.SongData> playList;
    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;

    private int seekToPosition;
    private boolean isPrepared = false;
    private boolean isPausedInCall = false;
    private boolean mediaPlayerHasStarted = false;
    private String currentAction;

    // Amount of time to rewind playback when resuming after call
    private final static int RESUME_REWIND_TIME = 3000;
    private final static int ERROR_RETRY_COUNT = 3;
    private final static int RETRY_SLEEP_TIME = 30000;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            startId = msg.arg1;
            onHandleIntent((Intent) msg.obj);
        }
    }

    public MusicService(){

    }

    @Override
    public void onCreate (){
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
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

        HandlerThread thread = new HandlerThread("PlaybackService:WorkerThread");
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    synchronized private boolean isPlaying() {
        return isPrepared && mMediaPlayer.isPlaying();
    }

    synchronized private void play() {
        if (!isPrepared) {
            Log.e(LOG_TAG, "play - not prepared");
            return;
        }else {
            mMediaPlayer.start();
            mediaPlayerHasStarted = true;
        }
    }

    synchronized private int getPosition() {
        if (isPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    synchronized private void seekRelative(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + pos);
        }
    }

    synchronized private void seekTo(int pos) {
        if (isPrepared) {
            seekToPosition = 0;
            mMediaPlayer.seekTo(pos);
        }
    }

    synchronized private void pause() {
        Log.d(LOG_TAG, "pause");
        if (isPrepared) {
                isPrepared = false;
                mMediaPlayer.stop();
            } else {
                mMediaPlayer.pause();
            }
        }

    synchronized private void stop() {
        Log.d(LOG_TAG, "stop");
        if (isPrepared) {
            isPrepared = false;
            mMediaPlayer.stop();
        }
    }

    private void resumePlaying() {
            if (isPrepared) {
                play();
            } else {
                Log.d(LOG_TAG, "nothing to resume");
            }
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        if(intent.getAction().equals(ACTION_PLAY)){

            Message message = serviceHandler.obtainMessage();
            message.arg1 = startId;
            message.obj = intent;
            serviceHandler.sendMessage(message);
        }
        return 0;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        synchronized (this) {
            if (mMediaPlayer != null) {
                isPrepared = true;
                play();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
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

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.d(LOG_TAG, "Null intent received");
            return;
        }
        String action = intent.getAction();
        Log.d(LOG_TAG, "Playback service action received: " + action);
        currentAction = action;
        if (action.equals(SERVICE_TOGGLE_PLAY) && isPrepared) {
            if (isPlaying()) {
                pause();
                // Get rid of the toggle intent, since we don't want it redelivered
                // on restart
                Intent emptyIntent = new Intent(intent);
                emptyIntent.setAction("");
                startService(emptyIntent);
            } else {
                ;

                Intent emptyIntent = new Intent(intent);
                emptyIntent.setAction("");
                startService(emptyIntent);
            }
        } else if (action.equals(SERVICE_RESUME_PLAYING)) {
            resumePlaying();
        } else if (action.equals(SERVICE_PAUSE)) {
            if (isPlaying()) {
                pause();
            }
        /*} else if (action.equals(SERVICE_SEEK_TO)) {
            seekTo(intent.getIntExtra(EXTRA_SEEK_TO, 0));
        } else if (action.equals(SERVICE_PLAY_NEXT)) {
            seekToPosition = 0;
            playNext();
        } else if (action.equals(SERVICE_PLAY_PREVIOUS)) {
            seekToPosition = 0;
            playPrevious();
        } else if (action.equals(SERVICE_STOP_PLAYBACK)) {
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

    private void prepareThenPlay(String url, boolean stream)
            throws IllegalArgumentException, IllegalStateException, IOException {
        // First, clean up any existing audio.
        stop();

        /*if (isPlaylist(url)) {
            new downloadPlaylist().execute(url);
            return;
        }*/

        /*synchronized (this) {
            Log.d(LOG_TAG, "reset: " + playUrl);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playUrl);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.d(LOG_TAG, "Preparing: " + playUrl);
            mMediaPlayer.prepareAsync();
            Log.d(LOG_TAG, "Waiting for prepare");
        }*/
    }


}
