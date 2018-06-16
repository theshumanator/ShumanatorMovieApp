package com.example.fatoumeh.shumanatormovieapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.fatoumeh.shumanatormovieapp.R;
import com.example.fatoumeh.shumanatormovieapp.data.FavouritesContract.FavouritesDB;

/**
 * Created by fatoumeh on 27/05/2018.
 */

public class FavouritesProvider extends ContentProvider {

    public static final int FAVOURITE_LIST = 100;
    public static final int FAVOURITE_ITEM = 101;

    private FavouritesDbHelper favouritesDbHelper;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher tmpUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = FavouritesContract.CONTENT_AUTHORITY;
        tmpUriMatcher.addURI(authority, FavouritesContract.PATH_FAVOURITES, FAVOURITE_LIST);
        tmpUriMatcher.addURI(authority, FavouritesContract.PATH_FAVOURITES + "/#", FAVOURITE_ITEM);
        return tmpUriMatcher;
    }


    @Override
    public boolean onCreate() {
        favouritesDbHelper = FavouritesDbHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase readDB = favouritesDbHelper.getReadableDatabase();
        Cursor queryCursor;
        int match = uriMatcher.match(uri);
        switch (match) {
            case FAVOURITE_LIST:
                queryCursor = readDB.query(FavouritesDB.TABLE_NAME, projection, null,
                        null, null, null, null);
                break;
            case FAVOURITE_ITEM:
                selection = FavouritesContract.FavouritesDB.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                queryCursor = readDB.query(FavouritesDB.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, null);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.query_unknown_uri) + match);
        }

        queryCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return queryCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case FAVOURITE_LIST:
                return FavouritesContract.FavouritesDB.CONTENT_LIST_TYPE;
            case FAVOURITE_ITEM:
                return FavouritesContract.FavouritesDB.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_uri) + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase writeDB = favouritesDbHelper.getWritableDatabase();

        long rowId;
        switch (match) {
            case FAVOURITE_LIST:
                rowId = writeDB.insert(FavouritesDB.TABLE_NAME, null, contentValues);
                if (rowId != -1) {
                    Toast.makeText(getContext(), getContext().getString(R.string.insert_fav_successful), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.insert_fav_error), Toast.LENGTH_SHORT).show();
                    return null;
                }
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.insert_unknown_uri) + match);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase writeDB = favouritesDbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (match) {
            case FAVOURITE_ITEM:
                selection = FavouritesContract.FavouritesDB.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = writeDB.delete(FavouritesDB.TABLE_NAME, selection,
                        selectionArgs);

                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.delete_unknown_uri) + match);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException(getContext().getString(R.string.unhandled_request));
    }
}
