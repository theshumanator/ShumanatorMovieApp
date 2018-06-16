package com.example.fatoumeh.shumanatormovieapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by fatoumeh on 13/05/2018.
 */

public class MovieAdapterWithRetroFit extends RecyclerView.Adapter<MovieAdapterWithRetroFit.MovieAdapterViewHolder> {

    private ArrayList<MoviesWithRetroFit.MoviesWithRetroFitMovie> moviesArrayList;
    private final MovieAdapterWithRetroFitOnClickHandler movieAdapterWithRetroFitOnClickHandler;

    public interface MovieAdapterWithRetroFitOnClickHandler {
        void onClick(MoviesWithRetroFit.MoviesWithRetroFitMovie movieItem);
    }

    public MovieAdapterWithRetroFit(MovieAdapterWithRetroFitOnClickHandler movieAdapterWithRetroFitOnClickHandler) {
        this.movieAdapterWithRetroFitOnClickHandler = movieAdapterWithRetroFitOnClickHandler;
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final ImageView movieImage;
        public final Context context;
        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            movieImage=itemView.findViewById(R.id.img_poster);
            itemView.setOnClickListener(this);
            context=itemView.getContext();
        }

        //get the position of the adapter where click has happened and pass to the adapterclickhandler
        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            MoviesWithRetroFit.MoviesWithRetroFitMovie movieItem = moviesArrayList.get(position);
            movieAdapterWithRetroFitOnClickHandler.onClick(movieItem);
        }
    }


    //inflate the view then create the VH
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        int layoutIdForGridItem=R.layout.movie_item;
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutIdForGridItem, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        MoviesWithRetroFit.MoviesWithRetroFitMovie movie=moviesArrayList.get(position);
        String imagePath="https://image.tmdb.org/t/p/w300/"+movie.getPosterPath();
        Picasso.with(holder.context)
                .load(imagePath)
                .error(R.drawable.ic_launcher_background)
                .into(holder.movieImage);

    }

    @Override
    public int getItemCount() {
        if (moviesArrayList==null) {
            return 0;
        } else {
            return moviesArrayList.size();
        }
    }

    public void setMovieData(ArrayList<MoviesWithRetroFit.MoviesWithRetroFitMovie> movieData) {
        moviesArrayList=movieData;
        notifyDataSetChanged();
    }
}
