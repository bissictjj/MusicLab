package com.afrozaar.musiclab.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.afrozaar.musiclab.HomeActivity;
import com.afrozaar.musiclab.MusicUtils;
import com.afrozaar.musiclab.adapters.MyExListAdapter;
import com.afrozaar.musiclab.dialogs.NewPlaylistDialogFragment;
import com.afrozaar.musiclab.R;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jay on 3/20/15.
 */
public class PlaylistFragment extends Fragment implements NewPlaylistDialogFragment.NewPlaylistAddedListener{

    private Activity activity;
    MyExListAdapter exListAdapter;
    ExpandableListView exListView;
    private List<String> mHeaderList;
    private HashMap<String, List<String>> mPlaylistHashMap;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = PlaylistFragment.class.getName();
    MusicUtils mUtils;
    private List<MusicUtils.PlaylistData> mPlaylistDataList;
    private FloatingActionButton btnAddNewPlaylist;

    public static PlaylistFragment newInstance(int sectionNumber){
        PlaylistFragment frag = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        frag.setArguments(args);
        return frag;
    }

    public PlaylistFragment(){

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ((HomeActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        return inflater.inflate(R.layout.fragment_playlist,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        prepareListData();

        Log.d(TAG, "OnViewCreated()");
        btnAddNewPlaylist = (FloatingActionButton)view.findViewById(R.id.btnAddPlaylist);
        exListView = (ExpandableListView)view.findViewById(R.id.playlist_exlistview);
        final PlaylistFragment frag1 = this;
        btnAddNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment frag = new NewPlaylistDialogFragment();
                frag.setTargetFragment(frag1,0);
                frag.show(getActivity().getSupportFragmentManager(),"create_playlist");
            }
        });

        //btnAddNewPlaylist.attachToListView(exListView);

        Log.d(TAG, "mHeaderList size : " + mHeaderList.size());
        //Log.d(TAG, "mDataList size : " + mDataList.size());
        exListAdapter = new MyExListAdapter(getActivity(), mHeaderList, mPlaylistHashMap);
        exListView.setAdapter(exListAdapter);

    }

    private void prepareListData() {
        Log.d(TAG,"prepareListData");
        mUtils = new MusicUtils(getActivity());
        mPlaylistDataList = mUtils.getPlaylistData();

        mHeaderList = new ArrayList<String>();
        mPlaylistHashMap = new HashMap<String, List<String>>();
        for(int i = 0; i < mPlaylistDataList.size(); i++){
            mHeaderList.add(mPlaylistDataList.get(i).getTitle());
            mPlaylistHashMap.put(mPlaylistDataList.get(i).getTitle(), mPlaylistDataList.get(i).getPlaylistSongTitles());
            //mDataList.put(mHeaderList.get(i),)
        }

       /* // Adding child data
        mHeaderList.add("Top 250");
        mHeaderList.add("Now Showing");
        mHeaderList.add("Coming Soon..");*/

        // Adding child data
        /*List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");*/
/*
        mDataList.put(mHeaderList.get(0), top250); // Header, Child data
        mDataList.put(mHeaderList.get(1), nowShowing);
        mDataList.put(mHeaderList.get(2), comingSoon);*/
    }

    @Override
    public void onPositiveButtonClicked() {
        prepareListData();

        exListAdapter = null;
        exListAdapter = new MyExListAdapter(getActivity(), mHeaderList, mPlaylistHashMap);
        exListView.setAdapter(exListAdapter);
        //exListAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(),"CALLBACK WORKED",Toast.LENGTH_LONG).show();
    }
}
