package com.example.fatoumeh.shumanatormovieapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fatoumeh.shumanatormovieapp.MovieAdapter.MovieAdapterOnClickHandler;
import java.util.ArrayList;

import utilities.QueryUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private final String LOG_TAG=this.getClass().getSimpleName();
    private RecyclerView rvMovies;
    private GridLayoutManager gridLayoutManager;
    private MovieAdapter movieAdapter;
    private TextView tvError;
    private ProgressBar pbProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvError=findViewById(R.id.tv_error);
        pbProgress=findViewById(R.id.pb_progress);
        rvMovies=findViewById(R.id.rvMovies);
        //we want two columns per row
        gridLayoutManager=new GridLayoutManager(this, 2);
        rvMovies.setLayoutManager(gridLayoutManager);
        movieAdapter=new MovieAdapter(this);
        rvMovies.setAdapter(movieAdapter);

        //check connectivity first
        if (isConnected()) {
            tvError.setVisibility(View.GONE);
            //by default we sort by popularity
            sortByPopularity();
        } else {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.connection_error));
        }

    }

    private void sortByPopularity() {
        setTitle(R.string.sort_popular);
        movieAdapter.setMovieData(null);
        new FetchMoviesTask().execute(getString(R.string.popular_query));

    }

    private void sortByRating() {
        setTitle(R.string.sort_top_rated);
        movieAdapter.setMovieData(null);
        new FetchMoviesTask().execute(getString(R.string.rating_query));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId=item.getItemId();
        if(itemId==R.id.sort_popular) {
            sortByPopularity();
            return true;
        } else if (itemId==R.id.sort_top_rated) {
            sortByRating();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(Movies movieItem) {
        Intent intent = new Intent (this, MovieDetails.class);
        intent.putExtra(getString(R.string.original_title), movieItem.getTitle());
        intent.putExtra(getString(R.string.overview), movieItem.getOverview());
        intent.putExtra(getString(R.string.poster_path), movieItem.getImage());
        intent.putExtra(getString(R.string.vote_average), movieItem.getVoteAverage());
        intent.putExtra(getString(R.string.release_date), movieItem.getReleaseDate());
        startActivity(intent);
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        return isConnected;
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movies>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Movies> doInBackground(String... queryTypes) {
            ArrayList<Movies> moviesArrayList;
            if (queryTypes.length==0) {
                //something isn't right
                Log.e(LOG_TAG, getString(R.string.incorrect_query));
                return null;
            } else {
                String queryType=queryTypes[0];
                moviesArrayList=QueryUtils.queryMovies(queryType, getApplicationContext());
            }
            return moviesArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Movies> movies) {
            pbProgress.setVisibility(View.GONE);
            if (movies!=null) {
                tvError.setVisibility(View.GONE);
                rvMovies.setVisibility(View.VISIBLE);
                movieAdapter.setMovieData(movies);
            } else {
                tvError.setText(getString(R.string.no_movies));
                tvError.setVisibility(View.VISIBLE);
                rvMovies.setVisibility(View.GONE);
                super.onPostExecute(movies);
            }
        }
    }
}
