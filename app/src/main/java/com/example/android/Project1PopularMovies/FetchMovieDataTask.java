package com.example.android.Project1PopularMovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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

public class FetchMovieDataTask extends AsyncTask<String, Void, ArrayList<MovieData>> {

    // Personalized API key for using themoviedb.org API.
    public static final String PERSONAL_API_KEY = "";

    public static final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();
    public static final String MOVIE_DATA_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    public static final String SORT_PARAM = "sort_by";
    public static final String API_PARAM = "api_key";
    public static final String RESULTS_PARAM = "results";
    public static final String POSTER_PATH_PARAM = "poster_path";
    public static final String OVERVIEW_PARAM = "overview";
    public static final String RATING_PARAM = "vote_average";
    public static final String RELEASE_DATE_PARAM = "release_date";
    public static final String TITLE_PARAM = "original_title";

    private ArrayList<MovieData> mMovieDataList = new ArrayList<>();
    private GridViewAdapter mGridViewAdapter;

    public FetchMovieDataTask(GridViewAdapter gridViewAdapter,
                              ArrayList<MovieData> movieDataList) {
        this.mMovieDataList = movieDataList;
        this.mGridViewAdapter = gridViewAdapter;
    }

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

        try {
            // Construct the URL for fetching movie data.
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
        mGridViewAdapter.notifyDataSetChanged();
    }

    /**
     * Gets the relative poster paths for each movie in the returned JSON response.
     */
    private ArrayList<MovieData> getDataAboutMovies(String moviesJsonStr)
            throws JSONException {
        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray results = moviesJson.getJSONArray(RESULTS_PARAM);
            for (int index = 0; index < results.length(); index++) {
                JSONObject movieData = results.getJSONObject(index);
                mMovieDataList.add(
                        new MovieData(movieData.getString(POSTER_PATH_PARAM),
                                movieData.getString(OVERVIEW_PARAM),
                                movieData.getString(RATING_PARAM),
                                movieData.getString(RELEASE_DATE_PARAM),
                                movieData.getString(TITLE_PARAM)));
            }
            return mMovieDataList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

