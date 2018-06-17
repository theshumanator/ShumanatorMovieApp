package com.example.fatoumeh.shumanatormovieapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fatoumeh.shumanatormovieapp.MovieAdapter.MovieAdapterOnClickHandler;
import com.example.fatoumeh.shumanatormovieapp.data.FavouritesContract.FavouritesDB;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utilities.QueryUtils;

public class MainActivity extends AppCompatActivity implements //MovieAdapterOnClickHandler,
        MovieAdapterWithRetroFit.MovieAdapterWithRetroFitOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG =this.getClass().getSimpleName();
    private RecyclerView rvMovies;
    private GridLayoutManager gridLayoutManager;
    private MovieAdapter movieAdapter;
    private MovieAdapterWithRetroFit movieAdapterWithRetroFit;
    private TextView tvError;
    private ProgressBar pbProgress;
    private Cursor cursor;

    /*
        Note: I will not be using Parcelable as i'm only keeping track of the scroll position
        (no objects).
        I'm using SharedPrefs for the sorting.
    */
    private int scrollPos;
    private String scrollPosKey;

    private SharedPreferences sortPreferences;
    private String sortPref;
    private String sortKey;

    private final int FAVOURITE_LOADER = 2;

    public static final String[] FAVOURITES_PROJECTION = {
            FavouritesDB.COLUMN_MOVIE_ID,
            FavouritesDB.COLUMN_MOVIE_TITLE,
            FavouritesDB.COLUMN_MOVIE_OVERVIEW,
            FavouritesDB.COLUMN_MOVIE_RATING,
            FavouritesDB.COLUMN_MOVIE_RELEASE_DATE,
            FavouritesDB.COLUMN_MOVIE_POSTER_PATH};

    public static final int COLUMN_MOVIE_ID = 0;
    public static final int COLUMN_MOVIE_TITLE = 1;
    public static final int COLUMN_MOVIE_OVERVIEW = 2;
    public static final int COLUMN_MOVIE_RATING = 3;
    public static final int COLUMN_MOVIE_RELEASE_DATE = 4;
    public static final int COLUMN_MOVIE_POSTER_PATH = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvError = findViewById(R.id.tv_error);
        pbProgress = findViewById(R.id.pb_progress);
        rvMovies = findViewById(R.id.rvMovies);

        //we want two columns per row
        gridLayoutManager = new GridLayoutManager(this, 2);
        rvMovies.setLayoutManager(gridLayoutManager);

        //movieAdapter = new MovieAdapter(this);
        //rvMovies.setAdapter(movieAdapter);


        movieAdapterWithRetroFit = new MovieAdapterWithRetroFit(this);
        rvMovies.setAdapter(movieAdapterWithRetroFit);

        scrollPosKey=getString(R.string.scroll_position);
        scrollPos=0;
        if (savedInstanceState!=null) {
            scrollPos=savedInstanceState.getInt(scrollPosKey, 0);
        }

        sortPref = getString(R.string.sort_pref);
        sortKey = getString(R.string.sort_key);
        sortPreferences = getSharedPreferences(sortPref, Context.MODE_PRIVATE);

        //check connectivity first
        if (QueryUtils.isConnected(this)) {
            tvError.setVisibility(View.GONE);
            if (sortPreferences.contains(sortKey)) {
                int sortValue=sortPreferences.getInt(sortKey, R.string.sort_popular);
                decideOnSorting(sortValue);
            } else {
                //by default we sort by popularity if prefs not set already
                sortByPopularity();
            }
        } else {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.connection_error));
        }
    }


    private void sortByPopularity() {
        setTitle(R.string.sort_popular);
        //movieAdapter.setMovieData(null);
        //new FetchMoviesTask().execute(getString(R.string.popular_query));
        movieAdapterWithRetroFit.setMovieData(null);
        MovieRetroFitAPIInterface movieRetroFitAPIInterface=MovieRetroFitClient.getRetrofitInstance()
                .create(MovieRetroFitAPIInterface.class);
        final Call<MoviesWithRetroFit> moviesWithRetroFitCall=movieRetroFitAPIInterface
                .getPopularMovies(getString(R.string.api_key_value));
        moviesWithRetroFitCall.enqueue(new Callback<MoviesWithRetroFit>() {
            @Override
            public void onResponse(Call<MoviesWithRetroFit> call, Response<MoviesWithRetroFit> response) {
                MoviesWithRetroFit moviesWithRetroFit=response.body();
                ArrayList<MoviesWithRetroFit.MoviesWithRetroFitMovie> movieList=moviesWithRetroFit.getResults();
                movieAdapterWithRetroFit.setMovieData(movieList);
            }

            @Override
            public void onFailure(Call<MoviesWithRetroFit> call, Throwable t) {
                Log.d("onFailure", "FAILED!" + t.getMessage());
                moviesWithRetroFitCall.cancel();
            }
        });

    }

    /*
        TODO: switch adapter to retrofit
        TODO: create/call the retrofit method and remove async task
     */

    private void sortByRating() {
        setTitle(R.string.sort_top_rated);
        movieAdapter.setMovieData(null);
        new FetchMoviesTask().execute(getString(R.string.rating_query));
    }

    /*
        TODO: switch to adapter to retrofit or keep db adapter separate (?)
     */
    private void showFavourites() {
        setTitle(R.string.show_favourites);
        movieAdapter.setMovieData(null);
        getLoaderManager().initLoader(FAVOURITE_LOADER, null, this);
        getAllFavourites();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void decideOnSorting(int sortValue) {
        switch (sortValue) {
            case R.id.sort_popular:
                sortByPopularity();
                break;
            case R.id.sort_top_rated:
                sortByRating();
                break;
            case R.id.show_favourites:
                showFavourites();
                break;
            default:
                break;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.sort_popular || itemId == R.id.sort_top_rated
                || itemId == R.id.show_favourites) {
            SharedPreferences.Editor editor = sortPreferences.edit();
            editor.putInt(sortKey, itemId);
            editor.apply();
            editor.commit();
        }
        if (itemId == R.id.sort_popular) {
            sortByPopularity();
            return true;
        } else if (itemId == R.id.sort_top_rated) {
            sortByRating();
            return true;
        } else if (itemId == R.id.show_favourites) {
            showFavourites();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(MoviesWithRetroFit.MoviesWithRetroFitMovie movieItem) {
        Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
        //intent.putExtra(getString(R.string.id), movieItem.getMovieId());
        intent.putExtra(getString(R.string.id), movieItem.getId());
        intent.putExtra(getString(R.string.original_title), movieItem.getTitle());
        intent.putExtra(getString(R.string.overview), movieItem.getOverview());
        //intent.putExtra(getString(R.string.poster_path), movieItem.getImage());
        intent.putExtra(getString(R.string.poster_path), movieItem.getPosterPath());
        intent.putExtra(getString(R.string.vote_average), movieItem.getVoteAverage());
        intent.putExtra(getString(R.string.release_date), movieItem.getReleaseDate());
        startActivity(intent);
    }

    //TODO: switch to retrofit arraylist
    private void getAllFavourites() {
        cursor = getContentResolver().query(FavouritesDB.CONTENT_URI, FAVOURITES_PROJECTION, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            tvError.setText(getString(R.string.no_fav_movies));
            tvError.setVisibility(View.VISIBLE);
        } else {
            ArrayList<Movies> favMovieList = new ArrayList<Movies>();
            while (cursor.moveToNext()) {
                String movieId = cursor.getString(COLUMN_MOVIE_ID);
                Integer movieIdInt;
                try {
                    movieIdInt = Integer.parseInt(movieId);
                } catch (NumberFormatException nfe) {
                    continue;
                }

                String movieTitle = cursor.getString(COLUMN_MOVIE_TITLE);
                String movieOverview = cursor.getString(COLUMN_MOVIE_OVERVIEW);
                String movieRating = cursor.getString(COLUMN_MOVIE_RATING);
                double movieRatingDbl;
                try {
                    movieRatingDbl = Double.parseDouble(movieRating);
                } catch (NumberFormatException nfe) {
                    movieRatingDbl = 0;
                }
                String movieRelease = cursor.getString(COLUMN_MOVIE_RELEASE_DATE);
                String moviePoster = cursor.getString(COLUMN_MOVIE_POSTER_PATH);
                Movies movie = new Movies(movieIdInt, movieTitle, movieOverview, movieRatingDbl, movieRelease, moviePoster);
                favMovieList.add(movie);
            }

            if (favMovieList.size() > 0) {
                movieAdapter.setMovieData(favMovieList);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        scrollPos=gridLayoutManager.findFirstVisibleItemPosition();
        outState.putInt(scrollPosKey, scrollPos);
        Log.d(LOG_TAG, "onSaveInstanceState " + scrollPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sortPreferences.contains(sortKey)) {
            int sortValue=sortPreferences.getInt(sortKey, R.string.sort_popular);
            decideOnSorting(sortValue);
        }
        Log.d(LOG_TAG, "onResume " + scrollPos);
        if (scrollPos!=RecyclerView.NO_POSITION) {
            gridLayoutManager.scrollToPosition(scrollPos);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, FavouritesDB.CONTENT_URI, FAVOURITES_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor resultCursor) {
        cursor = resultCursor;
        if (cursor == null || cursor.getCount() <= 0) {
            tvError.setText(getString(R.string.no_fav_movies));
            tvError.setVisibility(View.VISIBLE);
        } else {
            ArrayList<Movies> favMovieList = new ArrayList<Movies>();
            while (cursor.moveToNext()) {
                String movieId = cursor.getString(COLUMN_MOVIE_ID);
                Integer movieIdInt;
                try {
                    movieIdInt = Integer.parseInt(movieId);
                } catch (NumberFormatException nfe) {
                    continue;
                }

                String movieTitle = cursor.getString(COLUMN_MOVIE_TITLE);
                String movieOverview = cursor.getString(COLUMN_MOVIE_OVERVIEW);
                String movieRating = cursor.getString(COLUMN_MOVIE_RATING);
                double movieRatingDbl;
                try {
                    movieRatingDbl = Double.parseDouble(movieRating);
                } catch (NumberFormatException nfe) {
                    movieRatingDbl = 0;
                }
                String movieRelease = cursor.getString(COLUMN_MOVIE_RELEASE_DATE);
                String moviePoster = cursor.getString(COLUMN_MOVIE_POSTER_PATH);
                Movies movie = new Movies(movieIdInt, movieTitle, movieOverview, movieRatingDbl, movieRelease, moviePoster);
                favMovieList.add(movie);
            }

            if (favMovieList.size() > 0) {
                movieAdapter.setMovieData(favMovieList);
            }
        }
        if (scrollPos!=RecyclerView.NO_POSITION) {
            gridLayoutManager.scrollToPosition(scrollPos);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursor = null;
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
            if (queryTypes.length == 0) {
                //something isn't right
                Log.e(LOG_TAG, getString(R.string.incorrect_query));
                return null;
            } else {
                String queryType = queryTypes[0];
                moviesArrayList = QueryUtils.queryMovies(queryType, getApplicationContext());
            }
            return moviesArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Movies> movies) {
            pbProgress.setVisibility(View.GONE);
            if (movies != null) {
                tvError.setVisibility(View.GONE);
                rvMovies.setVisibility(View.VISIBLE);
                movieAdapter.setMovieData(movies);
                if (scrollPos!=RecyclerView.NO_POSITION) {
                    gridLayoutManager.scrollToPosition(scrollPos);
                }
            } else {
                tvError.setText(getString(R.string.no_movies));
                tvError.setVisibility(View.VISIBLE);
                rvMovies.setVisibility(View.GONE);
                super.onPostExecute(movies);
            }
        }
    }
}
