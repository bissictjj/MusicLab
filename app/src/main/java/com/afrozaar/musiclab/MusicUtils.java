package com.afrozaar.musiclab;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jay on 12/29/14.
 */
public class MusicUtils {

    public Context mContext = null;
    public List<SongData> mSongData = new ArrayList<SongData>();
    public List<Long> mPosIdList = new ArrayList<>();
    public int currentPos = 0;

    private static final String LOG_TAG = MusicService.class.getName();

    public MusicUtils(Context context){
        mContext = context;
    }

    public static class SongData{
        long mId;
        String mTitle;
        Uri mUri;
        long mAlbumArtId;

        public SongData(long id, String title, Uri uri,long artId ){
            mId = id;
            mTitle = title;
            mUri = uri;
            mAlbumArtId = artId;
        }

        public long getId(){
            return mId;
        }
    }

    private void setupLibrary() {
        if (mContext != null) {
            ContentResolver cR = mContext.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
            Cursor cursor = cR.query(uri, null, where, null, null);
            Log.d(LOG_TAG, "Uri : " + uri.toString());
            if (cursor == null) {
            } else if (!cursor.moveToFirst()) {
                Log.d(LOG_TAG, "no media found on device");
            } else {
                int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                int imgColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    long thisArtId = cursor.getLong(imgColumn);
                    Uri thisUri = getSongUri(thisId);
                    Log.d(LOG_TAG, "DATA ADDED: " + thisId + " AND " + thisTitle + " AND "+thisArtId);
                    if (thisTitle != null) {
                        mSongData.add(new SongData(thisId, thisTitle, thisUri,thisArtId));
                        mPosIdList.add(thisId);
                    }
                }
            }
        }
    }

    public Uri getSongUri(long id){
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        return contentUri;

    }

    public Uri getAlbumArt(long id){
        Uri contentUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), id);
        return contentUri;
    }

    public Uri getFirstSongUri(){
        if(!mSongData.isEmpty()) {
            long id = (mSongData.get(0)).getId();
            Uri contentUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            return contentUri;
        }else{
            return null;
        }
    }

    public long getSongId(int position){
        long tempId = mSongData.get(position).getId();
        return tempId;
    }

    public Uri getRandomSongUri(){
        if(!mSongData.isEmpty()) {
            Random temp = new Random(System.currentTimeMillis());
            int rand = temp.nextInt(mSongData.size());
            currentPos = rand;
            long id = (mSongData.get(rand)).getId();
            Uri contentUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            return contentUri;
        }else{
            return null;
        }
    }

    public int getCurrSongPosition(){
        return currentPos;
    }

    public int getPlaylistSize(){
        return mSongData.size();
    }

    public int getSongPosition(long id){
        int pos = 0;
        if(id > 0){
            pos = mPosIdList.indexOf(id);
            return pos;
        }else{
            return 0;
        }
    }

    public List<SongData> getSongData(){
        setupLibrary();
        if(mSongData != null){
            return mSongData;
        }else
        {
            return null;
        }
    }
}
