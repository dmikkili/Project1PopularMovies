package com.example.android.Project1PopularMovies;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable {
    public static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private String mImagePath;
    private String mOverview;
    private String mRating;
    private String mReleaseDate;
    private String mTitle;

    public MovieData(String imagePath, String overview, String rating, String releaseDate,
                     String title) {
        mImagePath = imagePath;
        mOverview = overview;
        mRating = rating;
        mReleaseDate = releaseDate;
        mTitle = title;
    }

    public String getImagePath(String imageSize) {
        return BASE_URL + imageSize + mImagePath;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getRating() { return mRating; }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mImagePath);
        out.writeString(mOverview);
        out.writeString(mRating);
        out.writeString(mReleaseDate);
        out.writeString(mTitle);
    }

    public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {

        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in.readString(), in.readString(), in.readString(), in.readString(),
                    in.readString());
        }

        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };
}