package com.afrozaar.musiclab;

import android.content.Context;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 3/30/15.
 */
public class MusicSearchLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;

    public static final String QUERY_KEY = "music_query";

    public static final String TAG = MusicSearchLoaderCallbacks.class.getName();

    private List<MusicUtils.SongData> mSongDataList = new ArrayList<>();

    public MusicSearchLoaderCallbacks (Context context){

        mContext = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String query = args.getString(QUERY_KEY);
        /*Uri uri = Uri.withAppendedPath(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, query);*/

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.ARTIST + " LIKE '%" + query.trim() + "%'";

        String sortBy = MediaStore.Audio.Media.ARTIST;

        return new CursorLoader(mContext, uri, null,selection,null,sortBy);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() == 0){
            return;
        }

        Log.d(TAG, "WE GOT DATA! : " + cursor.getCount());
        cursor.moveToFirst();

        int artistNameIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int artistIDIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int artistAlbumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int artistTitleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);


        do{
            Log.d(TAG,"\nArtist : "+ cursor.getString(artistNameIndex) + "\nArtistID : "+cursor.getInt(artistIDIndex) + "\nNum of tracks : "+cursor.getInt(artistAlbumIndex));
            mSongDataList.add(new MusicUtils.SongData(cursor.getInt(artistIDIndex),cursor.getString(artistTitleIndex),MusicUtils.getSongUri(cursor.getLong(artistIDIndex)),cursor.getLong(artistAlbumIndex)));
        }while (cursor.moveToNext());


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
