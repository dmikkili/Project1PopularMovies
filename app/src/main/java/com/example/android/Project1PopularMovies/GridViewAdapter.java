package com.example.android.Project1PopularMovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter<MovieData> {

    private Context mContext;
    private ArrayList<MovieData> mMovieDataList;

    public GridViewAdapter(Context context, int layoutResourceId,
                           ArrayList<MovieData> moviesDataList) {
        super(context, layoutResourceId, moviesDataList);
        mContext = context;
        mMovieDataList = moviesDataList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.popular_movies_fragment_grid_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) view.findViewById(R.id.image);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        Picasso.with(mContext).load(mMovieDataList.get(position).getImagePath("w185")).noFade()
                .into(viewHolder.image);
        return view;
    }

    class ViewHolder {
        ImageView image;
    }
}