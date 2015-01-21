package com.afrozaar.musiclab;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 12/29/14.
 */
public class MusicUtils {

    public Context mContext = null;
    public List<SongData> mSongData = new ArrayList<SongData>();

    public MusicUtils(Context context){
        mContext = context;
    }

    public static class SongData{
        long mId;
        String mTitle;
        String mPath;
        String mImagePath;

        public SongData(long id, String title){
            mId = id;
            mTitle = title;
            //mPath = path;
        }

        public long getId(){
            return mId;
        }
    }

    private void setupLibrary() {
        if (mContext != null) {
            ContentResolver cR = mContext.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = cR.query(uri, null, null, null, null);
            Log.d("DEBUG", "Uri : " + uri.toString());
            if (cursor == null) {
            } else if (!cursor.moveToFirst()) {
                Log.d("DEBUG", "no media found on device");
            } else {
                int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);

                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    //String thisPath = cursor.getString(pathColumn);
                    Log.d("DEBUG", "DATA ADDED: " + thisId + " AND " + thisTitle);
                    if (thisTitle != null) {
                        mSongData.add(new SongData(thisId, thisTitle));
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
