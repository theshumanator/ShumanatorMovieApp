package com.example.fatoumeh.shumanatormovieapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by fatoumeh on 27/05/2018.
 */

public final class FavouritesContract {
    private FavouritesContract() {};

    //content authority
    public static final String CONTENT_AUTHORITY = "com.example.fatoumeh.shumanatormovieapp";

    //base uri with content authority
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //path to each table
    public static final String PATH_FAVOURITES= "favourites";

    public static final class FavouritesDB implements BaseColumns {
        //path for a list of fav movies
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVOURITES;

        //path for a single fav movie
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVOURITES;

        //full usi for: content://com.example.fatoumeh.shumanatorbookstore/favourites
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVOURITES);

        //constants on db level
        public final static String DB_NAME="favourites.db";
        public final static int DB_VERSION=5;
        public final static String TABLE_NAME = "favourites";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_MOVIE_ID = "movie_id";
        public final static String COLUMN_MOVIE_TITLE = "movie_title";
        public final static String COLUMN_MOVIE_OVERVIEW = "movie_overview";
        public final static String COLUMN_MOVIE_RATING = "movie_rating";
        public final static String COLUMN_MOVIE_RELEASE_DATE = "movie_release_date";
        public final static String COLUMN_MOVIE_POSTER_PATH = "movie_poster_path";


    }
}
