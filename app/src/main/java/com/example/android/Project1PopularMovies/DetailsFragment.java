package com.example.android.Project1PopularMovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailsFragment extends Fragment {

    public DetailsFragment() {
        // Empty constructor for the DetailsFragment class.
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.popular_movie_details_fragment, container, false);

        Intent intent = getActivity().getIntent();

        if (intent != null) {
            ((TextView) rootView.findViewById(R.id.details_title)).setText(
                    intent.getStringExtra("title"));

            ImageView imageView = (ImageView) rootView.findViewById(R.id.details_image);

            Picasso.with(getActivity()).load(intent.getStringExtra("image_path"))
                    .into(imageView);

            ((TextView) rootView.findViewById(R.id.details_overview)).setText(
                    "Plot Synopsis: " + intent.getStringExtra("overview"));
            ((TextView) rootView.findViewById(R.id.details_rating)).setText(
                    "User Rating: " + intent.getStringExtra("rating"));
            ((TextView) rootView.findViewById(R.id.details_release_date)).setText(
                    "Release Date: " + intent.getStringExtra("release_date"));
        }

        return rootView;
    }

}