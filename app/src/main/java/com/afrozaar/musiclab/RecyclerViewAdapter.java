package com.afrozaar.musiclab;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 12/19/14.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    public List<MusicUtils.SongData> mSongData = new ArrayList<MusicUtils.SongData>();
    Context mContext;
    private LibItemClickListener mLibItemClickListener;
    private MusicUtils mu;
    private LruCache bitmapCache;
    private int cacheSize = 2 * 1024 * 1024; //2MiB

    private static final String LOG_TAG = RecyclerViewAdapter.class.getName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

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
        mu = new MusicUtils(context);

        mSongData = mu.getSongData();

    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library, parent, false);
        Log.d(LOG_TAG,"onCreateViewHolder");

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        Log.d(LOG_TAG,"onBindHolder");
        MusicUtils.SongData curr = mSongData.get(position);
        holder.mImageView.setImageDrawable(null);
        if(curr !=null){
            if(curr.mTitle != null){
                holder.mTextView.setText(curr.mTitle);
                Log.d(LOG_TAG,"Image Uri : "+mu.getAlbumArt(curr.mAlbumArtId));
                if(mu.getAlbumArt(curr.mAlbumArtId) != null) {
                    holder.mImageView.setImageURI(mu.getAlbumArt(curr.mAlbumArtId));
                }else{
                    holder.mImageView.setImageResource(R.drawable.ic_launcher);
                }
            }
        }
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLibItemClickListener.onItemClicked(position);
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

    public interface LibItemClickListener{
        void onItemClicked(int position);
    }

    public void setLibItemClickListener(LibItemClickListener libItemClickListener){
        mLibItemClickListener = libItemClickListener;
    }

}
