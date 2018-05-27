package com.example.fatoumeh.shumanatormovieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fatoumeh.shumanatormovieapp.data.FavouritesContract.FavouritesDB;

/**
 * Created by fatoumeh on 27/05/2018.
 */

public class FavouritesDbHelper extends SQLiteOpenHelper{

    public FavouritesDbHelper(Context context){
        super(context, FavouritesDB.DB_NAME, null, FavouritesDB.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable="CREATE TABLE " + FavouritesDB.TABLE_NAME + " ("
                + FavouritesDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FavouritesDB.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                + FavouritesDB.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                + FavouritesDB.COLUMN_MOVIE_OVERVIEW + " TEXT , "
                + FavouritesDB.COLUMN_MOVIE_RATING + " TEXT , "
                + FavouritesDB.COLUMN_MOVIE_RELEASE_DATE + " TEXT , "
                + FavouritesDB.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, "

                + "UNIQUE (" + FavouritesDB.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String dropTable="DROP TABLE IF EXISTS " + FavouritesDB.TABLE_NAME;
        sqLiteDatabase.execSQL(dropTable);
    }
}
