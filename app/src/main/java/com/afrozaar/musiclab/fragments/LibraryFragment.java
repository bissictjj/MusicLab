package com.afrozaar.musiclab.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afrozaar.musiclab.HomeActivity;
import com.afrozaar.musiclab.R;
import com.afrozaar.musiclab.adapters.RecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LibraryFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public RecyclerView recyclerView;
    public RecyclerViewAdapter recyclerViewAdapter;
    public RecyclerView.LayoutManager layoutManager;


    private static final String ARG_SECTION_NUMBER = "section_number";

    public static LibraryFragment newInstance(int sectionNumber) {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;
    }

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*layoutManager = new GridLayoutManager(getActivity(),2);*/
        layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity());
        recyclerViewAdapter.setSongData(null);
        recyclerView.setAdapter(recyclerViewAdapter);
        //recyclerViewAdapter.setLibItemClickListener(this);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }


    /*@Override
    public void onItemClicked(int position) {
        Intent intent = new Intent(getActivity(),MusicService.class);
        intent.setAction(MusicService.SERVICE_PLAY_SINGLE);
        intent.putExtra("SongPosition",position);
        getActivity().startService(intent);
    }*/
}