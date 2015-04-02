package com.afrozaar.musiclab.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afrozaar.musiclab.MusicUtils;
import com.afrozaar.musiclab.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jay on 3/20/15.
 */
public class MyExListAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private List<String> mHeaderTitles;
    //private HashMap<String, List<String>> mListData;
    private HashMap<String,List<String>> mPlaylistHashMap;
    final static String TAG = MyExListAdapter.class.getName();
    private LayoutInflater mInflater;
    private List<MusicUtils.PlaylistData> mPlaylistList;

    public MyExListAdapter(Activity context,List<String> titles, HashMap<String, List<String>> pIDs){
        Log.d(TAG,"In Expandable adapter constructor");
        this.context = context;
        this.mHeaderTitles = titles;
        //this.mPlaylistList = p;
        this.mPlaylistHashMap = pIDs;
        mPlaylistList = (new MusicUtils(context)).getPlaylistData();
    }

    @Override
    public int getGroupCount() {
        Log.d(TAG,"GetGroupCount " + mHeaderTitles.size());
        return mHeaderTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Log.d(TAG,"GetChildrenCount " + mPlaylistHashMap.get(mHeaderTitles.get(groupPosition)).size());
        return mPlaylistHashMap.get(mHeaderTitles.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        Log.d(TAG, "Get Group");
        return mHeaderTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Log.d(TAG, "Get Child");
        return mPlaylistHashMap.get(mHeaderTitles.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        Log.d(TAG, "Get GroupID");
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Log.d(TAG, "Get ChildID");
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String)getGroup(groupPosition);
        Log.d(TAG, "GetGroupView Called : " + headerTitle);
        if(convertView == null){
            LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_title,null);
        }
        TextView listHeader = (TextView)convertView.findViewById(R.id.tv_ListHeader);
        listHeader.setTypeface(null, Typeface.BOLD);
        listHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition, childPosition);
        Log.d(TAG,"GetChildView Called : " + childText);
        LayoutInflater mInflater = context.getLayoutInflater();
        if(convertView == null){
            //LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_playlist,null);
        }
        MusicUtils.SongData temp = mPlaylistList.get(groupPosition).getSongAt(childPosition);
        TextView txtListChild = (TextView)convertView.findViewById(R.id.tv_playlist_item);
        ImageView imgListChild = (ImageView)convertView.findViewById(R.id.iv_playlist_item);
        imgListChild.setImageURI(mPlaylistList.get(groupPosition).getAlbumArt(temp.getAlbumArtId()));
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void setupData()
    {

    }
}
