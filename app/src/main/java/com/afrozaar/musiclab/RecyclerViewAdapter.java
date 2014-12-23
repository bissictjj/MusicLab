package com.afrozaar.musiclab;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by jay on 12/19/14.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    public List<SongData> mSongData = new ArrayList<SongData>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public CardView mCardView;
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View v){
            super(v);

            mCardView = (CardView)v.findViewById(R.id.cv_library_item);
            mTextView = (TextView)v.findViewById(R.id.tv_library_item);
            mImageView = (ImageView)v.findViewById(R.id.iv_library_item);

        }

    }

    public static class SongData{
         private long mId;
         private String mTitle;

        public SongData(long id, String title){
            mId = id;
            mTitle = title;
        }

    }

    Context mContext;

    public void setupLibrary() {
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
                if(thisTitle != null){
                    mSongData.add(new SongData(thisId, thisTitle));
                }
            }
        }
    }

    public RecyclerViewAdapter(Context context){
        mContext = context;


    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library, parent, false);
        Log.d("DEBUG","onCreateViewHolder");

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Log.d("DEBUG","onBindHolder");
        SongData curr = mSongData.get(position);
        if(curr !=null){
            if(curr.mTitle != null){
                holder.mTextView.setText(curr.mTitle);
            }
        }
    }

    @Override
    public int getItemCount(){
        if(mSongData != null) {
            return mSongData.size() - 1;
        }else{
            return 0;
        }
    }
}
