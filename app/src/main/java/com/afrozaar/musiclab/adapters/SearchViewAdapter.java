package com.afrozaar.musiclab.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afrozaar.musiclab.MusicUtils;
import com.afrozaar.musiclab.R;

import java.util.List;

/**
 * Created by jay on 3/30/15.
 */
public class SearchViewAdapter extends RecyclerView.Adapter<SearchViewAdapter.ListViewHolder> {

    private final static String TAG = SearchViewAdapter.class.getName();
    public List<MusicUtils.SongData> mSongDataList;

    public void setData(List<MusicUtils.SongData> songDataList){
        mSongDataList = songDataList;
        notifyDataSetChanged();
    }

    @Override
    public SearchViewAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library, parent, false);
        Log.d(TAG, "SearchView onCreateViewHolder");

        ListViewHolder vh = new ListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SearchViewAdapter.ListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        if(mSongDataList != null){
            return mSongDataList.size();
        }else{
            return 0;
        }
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder{
        public CardView mCardView;
        public TextView mTextView;
        public ImageView mImageView;

        public ListViewHolder(View v){
            super(v);

            mCardView = (CardView)v.findViewById(R.id.cv_library_item);
            mTextView = (TextView)v.findViewById(R.id.tv_library_item);
            mImageView = (ImageView)v.findViewById(R.id.iv_library_item);

        }

    }
}
