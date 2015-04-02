package com.afrozaar.musiclab.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.afrozaar.musiclab.MusicUtils;
import com.afrozaar.musiclab.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 4/1/15.
 */
public class PlaylistSelectDialogFragment extends DialogFragment {

    public static PlaylistSelectDialogFragment newInstance(long songId){
        PlaylistSelectDialogFragment frag = new PlaylistSelectDialogFragment();
        Bundle b = new Bundle();
        b.putLong("song_id",songId);
        frag.setArguments(b);
        return frag;
    }

    ArrayAdapter arrayAdapter;
    long mSongId;
    ArrayList<String> listTitles = new ArrayList<>();
    ArrayList<Long> listIds = new ArrayList<>();
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSongId = getArguments().getLong("song_id");
        String title = "";
        final String check = getTag();
        if(check.equals(SongOptionsDialogFragment.ADD)){
            title = "Select a Playlist to Add to";
        }else if(check.equals(SongOptionsDialogFragment.REMOVE)){
            title = "Delete from Playlist";
        }
        Toast.makeText(getActivity(),"TAG : "+check,Toast.LENGTH_LONG).show();
        setupList();
        arrayAdapter = new ArrayAdapter(getActivity(), R.layout.simple_list_item,R.id.tv_simple,listTitles);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (check.equals(SongOptionsDialogFragment.ADD)) {
                            MusicUtils.addSongToPlaylist(getActivity().getContentResolver(), mSongId, listIds.get(which));
                        }
                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }

        );


                    return builder.create();
                }

    private void setupList(){
        MusicUtils mu = new MusicUtils(getActivity());
        List<MusicUtils.PlaylistData> temp = mu.getPlaylistData();
        for(int i = 0; i < temp.size();i++){
            listTitles.add(temp.get(i).getTitle());
            listIds.add(temp.get(i).getId());
        }
    }
}

