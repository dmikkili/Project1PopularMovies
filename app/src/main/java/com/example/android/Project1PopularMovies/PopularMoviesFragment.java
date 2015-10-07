package com.example.android.Project1PopularMovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * This fragment fetches movie data and displays it as a {@link GridView} layout.
 */
public class PopularMoviesFragment extends Fragment {

    private ArrayList<MovieData> mMovieDataList = new ArrayList<>();
    private GridViewAdapter mGridViewAdapter;

    public PopularMoviesFragment() {
        // Empty constructor for the PopularMoviesFragment class.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the width of the screen to dynamically set the number of coolumns.
        float scaleFactor = getResources().getDisplayMetrics().density * 100;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int columns = (int) ((float) displayMetrics.widthPixels / scaleFactor) / 2;
        if (columns == 0 || columns == 1) {
            columns = 2;
        }

        // Set the grid view adapter and click listener for the items in the grid view.
        View fragmentView = inflater.inflate(R.layout.popular_movies_fragment_grid, container,
                false);
        mGridViewAdapter = new GridViewAdapter(getActivity(),
                R.layout.popular_movies_fragment_grid_item, mMovieDataList);
        GridView gridView = (GridView) fragmentView.findViewById(R.id.popular_movies_grid);
        gridView.setNumColumns(columns);
        gridView.setAdapter(mGridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),
                        com.example.android.Project1PopularMovies.Details.class);
                MovieData movieData = mMovieDataList.get(position);
                intent.putExtra("movieData", movieData);
                startActivity(intent);
            }
        });
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMovieDataList.clear();
        mGridViewAdapter.notifyDataSetChanged();
        getMovieData();
    }

    private void getMovieData() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            FetchMovieDataTask movieTask = new FetchMovieDataTask(mGridViewAdapter, mMovieDataList);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortType = prefs.getString(getString(R.string.prefSortTypeKey),
                    getString(R.string.prefPopularityValue));
            String sortOrder = prefs.getString(getString(R.string.prefSortOrderKey),
                    getString(R.string.prefDescendingValue));
            movieTask.execute(sortType + sortOrder);
        }
    }

}