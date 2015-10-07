package com.example.android.Project1PopularMovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * This fragment fetches movie data and displays it as a {@link GridView} layout.
 */
public class PopularMoviesFragment extends Fragment {

    private ArrayList<MovieData> moviesDataList = new ArrayList<>();
    private GridViewAdapter gridViewAdapter;

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
        float scaleFactor = getResources().getDisplayMetrics().density * 100;
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        int width = displaymetrics.widthPixels;
        int columns = (int) ((float) width / scaleFactor) / 2;
        if (columns == 0 || columns == 1)
            columns = 2;

        View fragmentView = inflater.inflate(R.layout.popular_movies_fragment_grid, container,
                false);
        gridViewAdapter = new GridViewAdapter(getActivity(),
                R.layout.popular_movies_fragment_grid_item);
        GridView gridView = (GridView) fragmentView.findViewById(R.id.popular_movies_grid);
        gridView.setNumColumns(columns);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),
                        com.example.android.Project1PopularMovies.Details.class);
                MovieData movieData = moviesDataList.get(position);
                intent.putExtra("title", movieData.getTitle());
                intent.putExtra("overview", movieData.getOverview());
                intent.putExtra("rating", movieData.getRating());
                intent.putExtra("release_date", movieData.getReleaseDate());
                intent.putExtra("image_path", movieData.getImagePath("w780"));
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
        moviesDataList.clear();
        gridViewAdapter.notifyDataSetChanged();
        getMovieData();
    }

    private void getMovieData() {
        FetchMovieDataTask movieTask = new FetchMovieDataTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_type = prefs.getString(getString(R.string.pref_sort_type_key),
                getString(R.string.pref_popularity_value));
        String sort_order = prefs.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_descending_value));
        movieTask.execute(sort_type + sort_order);
    }

    public class FetchMovieDataTask extends AsyncTask<String, Void, ArrayList<MovieData>> {

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        @Override
        protected ArrayList<MovieData> doInBackground(String... params) {

            // If no sort order is provided, do nothing.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            // Personalized API key for using themoviedb.org API.
            String PERSONAL_API_KEY = "";

            try {
                // Construct the URL for fetching movie data.
                final String MOVIE_DATA_BASE_URL =
                        "https://api.themoviedb.org/3/discover/movie";
                final String SORT_PARAM = "sort_by";
                final String API_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_DATA_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(API_PARAM, PERSONAL_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());
                // Create the request to themoviedb.org, and open the connection.
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String.
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error, IOException.", e);
                // If the code didn't successfully get the movie data, there's no point in
                // attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream ", e);
                    }
                }
            }

            try {
                return getDataAboutMovies(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movies data.
            return null;
        }

        @Override
        public void onPostExecute(ArrayList<MovieData> movieData) {
            gridViewAdapter.notifyDataSetChanged();
        }

        /**
         * Gets the relative poster paths for each movie in the returned JSON response.
         */
        private ArrayList<MovieData> getDataAboutMovies(String moviesJsonStr)
                throws JSONException {
            try {
                final String RESULTS_PARAM = "results";
                final String POSTER_PATH_PARAM = "poster_path";
                final String OVERVIEW_PARAM = "overview";
                final String RATING_PARAM = "vote_average";
                final String RELEASE_DATE_PARAM = "release_date";
                final String TITLE_PARAM = "original_title";
                JSONObject moviesJson = new JSONObject(moviesJsonStr);
                JSONArray results = moviesJson.getJSONArray(RESULTS_PARAM);
                for (int index = 0; index < results.length(); index++) {
                    JSONObject movieData = results.getJSONObject(index);
                    moviesDataList.add(
                            new MovieData(movieData.getString(POSTER_PATH_PARAM),
                                    movieData.getString(OVERVIEW_PARAM),
                                    movieData.getString(RATING_PARAM),
                                    movieData.getString(RELEASE_DATE_PARAM),
                                    movieData.getString(TITLE_PARAM)));
                }
                return moviesDataList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class GridViewAdapter extends ArrayAdapter<MovieData> {

        private Context context;

        public GridViewAdapter(Context context, int layoutResourceId) {
            super(context, layoutResourceId, moviesDataList);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            ImageView imageView;
            if (view == null) {
                view = inflater.inflate(R.layout.popular_movies_fragment_grid_item, parent, false);
                imageView = (ImageView) view.findViewById(R.id.image);
                view.setTag(imageView);
            } else {
                imageView = (ImageView) view.getTag();
            }
            Picasso.with(context).load(moviesDataList.get(position).getImagePath("w185")).noFade()
                    .into(imageView);
            return view;
        }
    }

    public class MovieData {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        private String imagePath;
        private String overview;
        private String rating;
        private String releaseDate;
        private String title;

        public MovieData(String imagePath, String overview, String rating, String releaseDate,
                         String title) {
            this.imagePath = imagePath;
            this.overview = overview;
            this.rating = rating;
            this.releaseDate = releaseDate;
            this.title = title;
        }

        public String getImagePath(String imageSize) {
            return BASE_URL + imageSize + this.imagePath;
        }

        public String getOverview() {
            return this.overview;
        }

        public String getRating() {
            return this.rating;
        }

        public String getReleaseDate() {
            return this.releaseDate;
        }

        public String getTitle() {
            return this.title;
        }
    }
}