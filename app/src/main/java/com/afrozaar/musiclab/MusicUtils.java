package com.afrozaar.musiclab;

import android.content.ContentResolver;
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

        public SongData(long id, String title){
            mId = id;
            mTitle = title;
        }
    }

    private void setupLibrary() {
        if (mContext != null) {
            ContentResolver cR = mContext.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = cR.query(uri, null, null, null, null);

            if (cursor == null) {
            } else if (!cursor.moveToFirst()) {
                Log.d("DEBUG", "no media found on device");
            } else {
                Log.d("DEBUG", "Library cursor isnt null");
                int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    Log.d("DEBUG", "DATA ADDED: " + thisId + " AND " + thisTitle);
                    if (thisTitle != null) {
                        mSongData.add(new SongData(thisId, thisTitle));
                    }
                }
            }
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
