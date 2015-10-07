package com.example.android.Project1PopularMovies;

import android.content.Intent;
import android.content.res.Resources;
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
            MovieData movieData = intent.getParcelableExtra("movieData");

            ((TextView) rootView.findViewById(R.id.details_title)).setText(movieData.getTitle());

            ImageView imageView = (ImageView) rootView.findViewById(R.id.details_image);
            Picasso.with(getActivity()).load(movieData.getImagePath("w780")).into(imageView);

            Resources res = getResources();
            String overview = String.format(res.getString(R.string.detailsSummary),
                    movieData.getOverview());
            String rating = String.format(res.getString(R.string.detailsRating),
                    movieData.getRating());
            String releaseDate = String.format(res.getString(R.string.detailsReleaseDate),
                    movieData.getReleaseDate());

            ((TextView) rootView.findViewById(R.id.details_overview)).setText(overview);
            ((TextView) rootView.findViewById(R.id.details_rating)).setText(rating);
            ((TextView) rootView.findViewById(R.id.details_release_date)).setText(releaseDate);
        }

        return rootView;
    }

}