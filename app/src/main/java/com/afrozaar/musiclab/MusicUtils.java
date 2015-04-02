package com.afrozaar.musiclab;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jay on 12/29/14.
 */
public class MusicUtils {

    public Context mContext = null;
    public List<SongData> mSongData = new ArrayList<SongData>();
    public List<PlaylistData> mPlaylistData = new ArrayList<>();
    public List<Long> mPosIdList = new ArrayList<>();
    public int currentPos = 0;

    public static final String PLAYLIST_PREF = "playlist_pref";
    public static final String PLAYLIST_PREF_SIZE = "playlist_pref_size";

    private static final String LOG_TAG = MusicUtils.class.getName();

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

        public long getAlbumArtId(){return mAlbumArtId;}

        public String getTitle(){return mTitle;}
    }

    public static class PlaylistData{
        long pId;
        String pTitle;
        List<SongData> pSongs = new ArrayList<SongData>();
        Context mContext;

        public PlaylistData(Context c, long id, String title){
            pId = id;
            pTitle = title;
            this.mContext = c;
            setupPlaylistSongs();
        }

        public long getId() {
            return pId;
        }

        public String getTitle() {
            return pTitle;
        }

        public SongData getSongAt(int position){
            SongData temp = pSongs.get(position);
            return temp;
        }

        private void setupPlaylistSongs(){

            if (this.mContext != null) {
                ContentResolver cR = this.mContext.getContentResolver();
                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",pId);
                String [] proj = {
                                MediaStore.Audio.Playlists.Members.ALBUM_ID,
                                MediaStore.Audio.Playlists.Members.TITLE,
                                MediaStore.Audio.Playlists.Members._ID
                };
                //String where = MediaStore.Audio.Playlists._ID + "=" +getId();
                /*where[0] = MediaStore.Audio.Media.IS_MUSIC + "=1";
                where[1] = MediaStore.Audio.Playlists._ID + "="+getId();*/
                Cursor cursor = cR.query(uri,proj,null, null, null);
                Log.d(LOG_TAG, "Playlist Title: "+getTitle() + " with id "+getId());
                if (cursor == null) {
                } else if (!cursor.moveToFirst()) {
                    Log.d(LOG_TAG, "no media found on device");
                } else {
                    int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Playlists.Members.TITLE);
                    int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Playlists.Members._ID);
                    int imgColumn = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID);
                    cursor.moveToFirst();
                    do {
                        long thisId = cursor.getLong(idColumn);
                        String thisTitle = cursor.getString(titleColumn);
                        long thisArtId = cursor.getLong(imgColumn);
                        Uri thisUri = getSongUri(thisId);
                        if (thisTitle != null) {
                            Log.d(LOG_TAG, "Song Title: "+thisTitle);
                            pSongs.add(new SongData(thisId, thisTitle, thisUri,thisArtId));
                        }
                    }while (cursor.moveToNext());
                }
            }

        }

        private Uri getSongUri(long id){
            Uri contentUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            return contentUri;

        }

        public Uri getAlbumArt(long id){
            Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), id);
            return contentUri;
        }

        public List<Long> getPlaylistSongIds(){
            List<Long> songIds = new ArrayList<Long>();
            for(int i = 0; i < pSongs.size(); i++){
                songIds.add(pSongs.get(i).getId());
            }
            return songIds;
        }

        public List<String> getPlaylistSongTitles(){
            List<String> songTitles = new ArrayList<String>();
            for (int i = 0; i < pSongs.size(); i++) {
                songTitles.add(pSongs.get(i).getTitle());
            }
            return songTitles;
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
                    Uri thisUri = MusicUtils.getSongUri(thisId);
                    if (thisTitle != null) {
                        mSongData.add(new SongData(thisId, thisTitle, thisUri,thisArtId));
                        mPosIdList.add(thisId);
                    }
                }
            }
        }
    }

    public static Uri getSongUri(long id){
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

    public String getSongTitle(int position){
        return mSongData.get(position).getTitle();
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

    private void setupPlaylists()
    {
        if(mContext != null){
            ContentResolver cR = mContext.getContentResolver();
            Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            //String where = MediaStore.Audio.Media.IS_MUSIC + "= !0";
            Cursor cursor = cR.query(uri,null,null,null,null);

            Log.d(LOG_TAG, "Uri : " + uri.toString());
            if (cursor == null) {
            } else if (!cursor.moveToFirst()) {
                Log.d(LOG_TAG, "no media found on device");
            } else {
                int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
                int idColumn = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
                //int imgColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    //long thisArtId = cursor.getLong(imgColumn);
                    //Uri thisUri = getSongUri(thisId);
                    if (thisTitle != null) {
                        mPlaylistData.add(new PlaylistData(mContext,thisId,thisTitle));
                    }
                }
            }

        }

    }

    public List<PlaylistData> getPlaylistData(){
        setupPlaylists();
        return mPlaylistData;
    }

    public static void createPlaylist(ContentResolver cR, String pName){
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentValues val = new ContentValues();
        val.put(MediaStore.Audio.Playlists.NAME, pName);
        Uri newUri = cR.insert(uri, val);
        Cursor cur = cR.query(newUri, null, null, null, null);
        cur.moveToFirst();
        long plId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Playlists._ID));
        uri = MediaStore.Audio.Playlists.Members.getContentUri("external",plId);
        /*Cursor curCheck = cR.query(uri,null,null,null,null);
        String nameCheck = curCheck.getString(curCheck.getColumnIndex(MediaStore.Audio.Playlists.Members.))*/
        ContentValues val2 = new ContentValues();
        val2.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,Integer.valueOf(0));
        cR.insert(uri,val2);
        cur.close();

    }

    public static void addSongToPlaylist(ContentResolver cR, long songId, long playlistId){
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Log.d(LOG_TAG, "playListId: " + playlistId);
        Cursor cur = cR.query(uri, null, null, null, null);
        int base = 0;
        if(cur.getCount() >0 ) {
            cur.moveToFirst();

            int playOrderIndex = cur.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER);
            Log.d(LOG_TAG, "playOrderIndex: " + playOrderIndex);
            base = cur.getInt(playOrderIndex);
            cur.close();

        }
        ContentValues val = new ContentValues();
        //Log.d(LOG_TAG,"base results: " +base);
        val.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base +1));
        val.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
        cR.insert(uri, val);
    }

    public static void deletePlaylist(ContentResolver cR, long playlistId){
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        cR.delete(uri, MediaStore.Audio.Playlists._ID + "= '" + playlistId + "'", null);

    }

    public static void deleteSongFromPlaylist(ContentResolver cR, long songId, long playlistId){
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        cR.delete(uri,MediaStore.Audio.Playlists.Members.AUDIO_ID + "= '"+songId+"'",null);

    }

    public static void deleteSong(ContentResolver cR, long songId){

    }

    public static void getSongInfo(ContentResolver cR, long songId){

    }
}
