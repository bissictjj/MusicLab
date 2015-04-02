package com.afrozaar.musiclab.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.afrozaar.musiclab.MusicUtils;
import com.afrozaar.musiclab.R;

/**
 * Created by jay on 3/31/15.
 */
public class NewPlaylistDialogFragment extends DialogFragment {
    String newPlaylist = "";

    NewPlaylistAddedListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            mListener = (NewPlaylistAddedListener)getTargetFragment();
        }catch(ClassCastException e){
            throw new ClassCastException("calling fragment must implement NewPlaylistAddedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_create_playlist,null))
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText et = (EditText)getDialog().findViewById(R.id.et_playlist_name);
                        newPlaylist = et.getText().toString();
                        Toast.makeText(getActivity(),"Playlist Created : "+newPlaylist,Toast.LENGTH_LONG).show();
                        MusicUtils.createPlaylist(getActivity().getContentResolver(), newPlaylist);
                        mListener.onPositiveButtonClicked();
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    public interface NewPlaylistAddedListener{
        public void onPositiveButtonClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


    }
}
