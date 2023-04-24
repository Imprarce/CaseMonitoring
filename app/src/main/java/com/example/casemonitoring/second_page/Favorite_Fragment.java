package com.example.casemonitoring.second_page;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.casemonitoring.R;

public class Favorite_Fragment extends Fragment {

    private ImageView exit;
    private FavoriteFragmentListener listener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        exit = view.findViewById(R.id.exit_from_fragment);


        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {

                    listener.onExitClicked();
                }
            }
        });

        return view;
    }

    public void setListener(FavoriteFragmentListener listener) {
        this.listener = listener;
    }
}