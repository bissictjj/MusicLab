package com.afrozaar.musiclab.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.afrozaar.musiclab.R;

/**
 * Created by jay on 3/31/15.
 */
public class SongOptionsDialogFragment extends DialogFragment {

    long mSongId;

    final static String ADD = "playlist_select_add";
    final static String DELETE = "playlist_select_delete";
    final static String REMOVE = "playlist_select_remove";

    public static SongOptionsDialogFragment newInstance(long songId){
        SongOptionsDialogFragment frag = new SongOptionsDialogFragment();
        Bundle b = new Bundle();
        b.putLong("song_id",songId);
        frag.setArguments(b);
        return frag;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSongId = getArguments().getLong("song_id");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select an Option")
                .setItems(R.array.song_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String [] arr = getResources().getStringArray(R.array.song_options);
                        Toast.makeText(getActivity(),"Option : "+arr[which], Toast.LENGTH_LONG).show();
                        if( which == 0){
                            DialogFragment frag = PlaylistSelectDialogFragment.newInstance(mSongId);
                            frag.show(((FragmentActivity)getActivity()).getSupportFragmentManager(),ADD);
                        }else if(which ==1){

                        }else if(which ==2){

                        }
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }
}
