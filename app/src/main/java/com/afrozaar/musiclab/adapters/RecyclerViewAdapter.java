package com.afrozaar.musiclab.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afrozaar.musiclab.MusicService;
import com.afrozaar.musiclab.MusicUtils;
import com.afrozaar.musiclab.R;
import com.afrozaar.musiclab.dialogs.SongOptionsDialogFragment;
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

    public void setSongData(List<MusicUtils.SongData> s){
        if(s == null){
            mSongData = mu.getSongData();
        }else {
            mSongData = s;
            notifyDataSetChanged();
        }
    }

    public RecyclerViewAdapter(Context context){
        mContext = context;
        mu = new MusicUtils(context);


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
        final MusicUtils.SongData curr = mSongData.get(position);
        holder.mImageView.setImageDrawable(null);
        if(curr !=null){
            if(curr.getTitle() != null){
                holder.mTextView.setText(curr.getTitle());
                Log.d(LOG_TAG,"Image Uri : "+mu.getAlbumArt(curr.getAlbumArtId()));
                if(mu.getAlbumArt(curr.getAlbumArtId()) != null) {
                    holder.mImageView.setImageURI(mu.getAlbumArt(curr.getAlbumArtId()));
                }else{
                    holder.mImageView.setImageResource(R.drawable.ic_launcher);
                }
            }
        }
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,MusicService.class);
                intent.setAction(MusicService.SERVICE_PLAY_SINGLE);
                intent.putExtra("SongId", curr.getId());
                Log.d(LOG_TAG,"SongId :" + curr.getId());
                mContext.startService(intent);
                //mLibItemClickListener.onItemClicked(position);
            }
        });
        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DialogFragment frag = SongOptionsDialogFragment.newInstance(curr.getId());
                frag.show(((FragmentActivity)mContext).getSupportFragmentManager(),"song_options");
                return true;
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
