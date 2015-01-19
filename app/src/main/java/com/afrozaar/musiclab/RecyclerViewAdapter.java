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

    public List<MusicUtils.SongData> mSongData = new ArrayList<MusicUtils.SongData>();
    Context mContext;

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

    public RecyclerViewAdapter(Context context){
        mContext = context;
        MusicUtils mu = new MusicUtils(context);
        mSongData = mu.getSongData();
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
        MusicUtils.SongData curr = mSongData.get(position);
        if(curr !=null){
            if(curr.mTitle != null){
                holder.mTextView.setText(curr.mTitle);
            }
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
