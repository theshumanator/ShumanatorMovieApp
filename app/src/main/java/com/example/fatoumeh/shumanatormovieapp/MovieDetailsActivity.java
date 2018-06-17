package com.example.fatoumeh.shumanatormovieapp;

import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fatoumeh.shumanatormovieapp.data.FavouritesContract.FavouritesDB;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utilities.QueryUtils;

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //TODO: switch to retrofit

    private TextView tvTitle;
    private TextView tvOverview;
    private TextView tvVoteAvg;
    private TextView tvReleaseDate;
    private TextView tvReview;
    private TextView tvReviewAuthor;

    private ImageView imgPoster;

    private Button btTrailer;
    private Button btReview;

    private ImageButton btCloseReview;
    private ImageButton btCloseTrailer;
    private ImageButton btFavourite;

    private boolean isFavourite;

    private final int FAVOURITE_MANAGER=1;

    private Uri movieUri;

    private Cursor cursor;

    private Integer movieId=-1;

    private String title, overview, moviePosterPath, voteAvg, oldDate;

    public static final String [] FAVOURITES_PROJECTION={
            FavouritesDB._ID,
            FavouritesDB.COLUMN_MOVIE_ID,
            FavouritesDB.COLUMN_MOVIE_TITLE};

    public static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Intent intent = getIntent();

        String movieIdStr=getString(R.string.id);
        String titleStr = getString(R.string.original_title);
        String overviewStr = getString(R.string.overview);
        String voteStr = getString(R.string.vote_average);
        String dateStr = getString(R.string.release_date);
        String posterStr = getString(R.string.poster_path);

        if (intent.hasExtra(movieIdStr)) {
            movieId=intent.getIntExtra(movieIdStr, -1);
        }

        if (intent.hasExtra(titleStr)) {
            tvTitle=findViewById(R.id.tv_title);
            title=intent.getStringExtra(titleStr);
            tvTitle.setText(title);
            setTitle(title);
        }

        if (intent.hasExtra(overviewStr)) {
            tvOverview=findViewById(R.id.tv_overview);
            overview=intent.getStringExtra(overviewStr);
            tvOverview.setText(overview);
        }

        if (intent.hasExtra(posterStr)) {
            imgPoster=findViewById(R.id.img_movie_poster);
            moviePosterPath=intent.getStringExtra(posterStr);
            Picasso.with(this)
                    .load(moviePosterPath)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgPoster);
        }

        if (intent.hasExtra(voteStr)) {
            tvVoteAvg=findViewById(R.id.tv_rating);
            voteAvg=String.valueOf(intent.getDoubleExtra(voteStr,0));
            tvVoteAvg.setText(voteAvg);
        }

        if (intent.hasExtra(dateStr)) {
            tvReleaseDate=findViewById(R.id.tv_release_date);
            oldDate=intent.getStringExtra(dateStr);
            String releaseDate=getNewDate(oldDate);
            tvReleaseDate.setText(releaseDate);
        }

        btTrailer=findViewById(R.id.bt_get_trailers);
        btTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTralerRequest(movieId);
            }
        });

        btReview=findViewById(R.id.bt_get_review);
        btReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleReviewRequest(movieId);
            }
        });

        movieUri=Uri.withAppendedPath(FavouritesDB.CONTENT_URI, String.valueOf(movieId));
        getLoaderManager().initLoader(FAVOURITE_MANAGER,null, this);

        isFavourite=isMovieInFavourites();

        btFavourite=findViewById(R.id.img_bt_favourite);
        if (!isFavourite) {
            btFavourite.setImageResource(android.R.drawable.btn_star_big_off);
        } else {
            btFavourite.setImageResource(android.R.drawable.btn_star_big_on);
        }

        btFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                    if movie is already fav, then clicking the star button should set it to unfav
                    otherwise vice versa
                 */
                if (isFavourite) {
                    if (removeFromFavourites()) {
                        btFavourite.setImageResource(android.R.drawable.btn_star_big_off);
                        isFavourite=false;
                    }
                } else {
                    if (addToFavourites()) {
                        btFavourite.setImageResource(android.R.drawable.btn_star_big_on);
                        isFavourite=true;
                    }
                }
            }
        });

    }

    private boolean isMovieInFavourites() {
        boolean movieInFavourites;
        cursor=getContentResolver().query(movieUri,FAVOURITES_PROJECTION,null,null,null);
        if (cursor==null || cursor.getCount()==0) {
            movieInFavourites=false;
        } else {
            movieInFavourites=true;
        }
        return movieInFavourites;
    }

    private boolean removeFromFavourites() {
        boolean removeSuccessful;
        int rowsDeleted=getContentResolver().delete(movieUri, null,null);
        if (rowsDeleted<=0) {
            removeSuccessful=false;
            Toast.makeText(this, getString(R.string.remove_fav_error), Toast.LENGTH_LONG).show();
        } else {
         removeSuccessful=true;
        }
        return removeSuccessful;
    }

    private boolean addToFavourites() {
        boolean insertSuccessful;
        ContentValues contentValues=new ContentValues();
        contentValues.put(FavouritesDB.COLUMN_MOVIE_ID, movieId);
        contentValues.put(FavouritesDB.COLUMN_MOVIE_TITLE, title);
        contentValues.put(FavouritesDB.COLUMN_MOVIE_OVERVIEW, overview);
        contentValues.put(FavouritesDB.COLUMN_MOVIE_RATING, voteAvg);
        contentValues.put(FavouritesDB.COLUMN_MOVIE_RELEASE_DATE,oldDate );
        contentValues.put(FavouritesDB.COLUMN_MOVIE_POSTER_PATH, moviePosterPath);

        Uri addUri=getContentResolver().insert(FavouritesDB.CONTENT_URI, contentValues);
        if (addUri!=null) {
            insertSuccessful=true;
        } else {
            insertSuccessful=false;
        }
        if (!insertSuccessful ){
            Toast.makeText(this, getString(R.string.insert_fav_error), Toast.LENGTH_LONG).show();
        }
        return insertSuccessful;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, movieUri, FAVOURITES_PROJECTION, null,null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
        cursor=newCursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursor=null;
    }

    private void handleTralerRequest(Integer queryMovieId) {
        if (QueryUtils.isConnected(this)) {
            FetchTrailerTask fetchTrailerTask=new FetchTrailerTask(this);
            fetchTrailerTask.execute(queryMovieId);
        } else {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleReviewRequest(Integer queryMovieId) {
        if (QueryUtils.isConnected(this)) {
            FetchReviewTask fetchReviewTask = new FetchReviewTask(this);
            fetchReviewTask.execute(queryMovieId);
        } else {
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String getNewDate(String date) {
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat newDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date dateObj = null;
        try {
            Log.d("Debugging date part 2", date);
            dateObj = currentDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String newDateTime = newDateFormat.format(dateObj);
        return newDateTime;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

        /*if (item.getItemId()==android.R.id.home) {
            finish();

                return true;
        } else {
            return super.onOptionsItemSelected(item);
        }*/
    }

    public class FetchTrailerTask extends AsyncTask<Integer, Void, ArrayList<String>> {
        private Context taskContext;

        public FetchTrailerTask(Context taskContext) {
            this.taskContext=taskContext;
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... integers) {
            ArrayList<String> trailerInfo;
            if (integers.length==0) {
                Log.w(LOG_TAG, getString(R.string.no_trailers));
                return null;
            } else {
                int id=integers[0].intValue();
                trailerInfo= QueryUtils.getMovieTrailers(id, taskContext);
                if (trailerInfo.size()<1) {
                    Log.w(LOG_TAG, getString(R.string.no_trailers));
                    return null;
                } else {
                    //I'm checking if the first trailer isnt empty. if it is, show error
                    if (TextUtils.isEmpty(trailerInfo.get(0))) {
                        Log.w(LOG_TAG, getString(R.string.no_trailers));
                        return null;
                    } else {
                        return trailerInfo;
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            inflateTrailerWindow(strings);
        }
    }

    private void inflateTrailerWindow(ArrayList<String>trailers) {

        if (trailers==null || trailers.size()<1) {
            Toast.makeText(this, getString(R.string.no_trailers), Toast.LENGTH_SHORT).show();
        } else {
            LayoutInflater inflater = (LayoutInflater) MovieDetailsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.trailer_window, (ViewGroup) findViewById(R.id.ll_trailer_popup));

            LinearLayout llTrailerSection=layout.findViewById(R.id.ll_trailer_details);

            int count=0;
            for (final String trailer: trailers) {
                if (!TextUtils.isEmpty(trailer)) {
                    count++;
                    TextView tvTrailer=new TextView(llTrailerSection.getContext());
                    tvTrailer.setText("Trailer " + count);
                    tvTrailer.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                    if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M) {
                        tvTrailer.setTextAppearance(this, R.style.GenericMovieDetailsStyle);
                    } else {
                        tvTrailer.setTextAppearance(R.style.GenericMovieDetailsStyle);
                    }
                    tvTrailer.setPadding(0,8,0,8);
                    tvTrailer.setPaintFlags(tvTrailer.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    tvTrailer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent openYouTube=new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtube_app_link) + trailer));
                            try {
                                startActivity(openYouTube);
                            } catch (ActivityNotFoundException ane) {
                                //No youtube app found so going to open a browser
                                Intent openWebBrowser=new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtube_link)+ trailer));
                                startActivity(openWebBrowser);
                            }
                        }
                    });

                    llTrailerSection.addView(tvTrailer);
                }
            }

            final PopupWindow trailerPopup = new PopupWindow(layout,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            trailerPopup.setAnimationStyle(android.R.anim.fade_in);
            trailerPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);

            btCloseTrailer=layout.findViewById(R.id.bt_close_trailer);
            btCloseTrailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    trailerPopup.dismiss();
                }
            });

        }

    }

    public class FetchReviewTask extends AsyncTask<Integer, Void, String[]> {
        private Context taskContext;

        public FetchReviewTask(Context taskContext) {
            this.taskContext=taskContext;
        }

        @Override
        protected String[] doInBackground(Integer... integers) {
            String [] reviewsInfo;
            if (integers.length==0) {
                Log.w(LOG_TAG, getString(R.string.no_reviews));
                return null;
            } else {
                int id=integers[0].intValue();
                reviewsInfo= QueryUtils.getMovieReview(id, taskContext);
                if (reviewsInfo.length<2) {
                    Log.w(LOG_TAG, getString(R.string.part_review));
                    return null;
                } else {
                    if (TextUtils.isEmpty(reviewsInfo[0])||TextUtils.isEmpty(reviewsInfo[1])) {
                        Log.w(LOG_TAG, getString(R.string.no_reviews));
                        return null;
                    } else {
                        return reviewsInfo;
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            inflateReviewWindow(strings);
        }
    }

    private void inflateReviewWindow(String [] reviewInfo) {

        if (reviewInfo==null || reviewInfo.length<2) {
            Toast.makeText(this, getString(R.string.no_reviews), Toast.LENGTH_SHORT).show();
        } else {
            String review=reviewInfo[0];
            String reviewAuthor=reviewInfo[1];

            try {
                LayoutInflater inflater = (LayoutInflater) MovieDetailsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.review_info, (ViewGroup) findViewById(R.id.ll_review_popup));


                final PopupWindow reviewPopup = new PopupWindow(layout,
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                reviewPopup.setAnimationStyle(android.R.anim.fade_in);
                reviewPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);

                tvReview=layout.findViewById(R.id.tv_review);
                if (!TextUtils.isEmpty(review)) {

                    tvReview.setText(review);
                    tvReviewAuthor=layout.findViewById(R.id.tv_review_author);
                    if (!TextUtils.isEmpty(reviewAuthor)) {
                        tvReviewAuthor.setText(getString(R.string.str_review_author)+reviewAuthor);
                    }
                } else {
                    tvReview.setText(getString(R.string.no_reviews));
                }

                btCloseReview=layout.findViewById(R.id.bt_close);
                btCloseReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reviewPopup.dismiss();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
