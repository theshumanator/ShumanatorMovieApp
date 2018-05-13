package utilities;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.fatoumeh.shumanatormovieapp.Movies;
import com.example.fatoumeh.shumanatormovieapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Created by fatoumeh on 13/05/2018.
 */

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();


    private static final String MOVIES_BASE_URL="https://api.themoviedb.org/3/movie/";
    private static  String QUERY_BRANCH;

    private static  String API_KEY;
    private static  String API_KEY_VALUE;
    private static  String LANGUAGE;
    private static  String LANGUAGE_VALUE;
    private static  String PAGE;
    private static  int PAGE_VALUE;

    private static final String IMAGES_BASE_URL="https://image.tmdb.org/t/p/";
    private static final String DIMENSION="w300/";

    private static int READ_TIMEOUT;
    private static int CONNECT_TIMEOUT;

    private static String POPULAR_QUERY;
    private static String RATING_QUERY;

    //json related strings
    private static String JSON_RESULTS;
    private static String JSON_ID;
    private static String JSON_ORIGINAL_TITLE;
    private static String JSON_OVERVIEW;
    private static String JSON_VOTE_AVERAGE;
    private static String JSON_RELEASE_DATE;
    private static String JSON_POSTER_PATH;

    private static String MISSING_DETAILS;
    private static String JSON_ERROR;
    private static String JSON_RETRIEVE_ERROR;
    private static String HTTP_ERROR;

    public static ArrayList<Movies> queryMovies(String queryType, Context context){
        setupStrings(context);
        URL url = buildURL(queryType, context);
        String jsonResponse=null;
        try {
            jsonResponse=makeHttpQuery(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Movies>movies;
        movies=fetchMovies(jsonResponse);
        return movies;
    }


    private static URL buildURL(String queryType, Context context){
        URL url=null;
        if (queryType.equals(POPULAR_QUERY)||queryType.equals(RATING_QUERY)) {
            QUERY_BRANCH=MOVIES_BASE_URL+queryType;
            Uri builtUri=Uri.parse(QUERY_BRANCH).buildUpon()
                    .appendQueryParameter(API_KEY,API_KEY_VALUE)
                    .appendQueryParameter(LANGUAGE,LANGUAGE_VALUE)
                    .appendQueryParameter(PAGE,Integer.toString(PAGE_VALUE))
                    .build();
            try {
                url=new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return url;
        } else {
            //if image then we do something else but for now return null
            return url;
        }
    }

    private static void setupStrings(Context context) {
        POPULAR_QUERY=context.getString(R.string.popular_query);
        RATING_QUERY=context.getString(R.string.rating_query);
        API_KEY=context.getString(R.string.api_key);
        API_KEY_VALUE=context.getString(R.string.api_key_value);
        LANGUAGE=context.getString(R.string.language);
        LANGUAGE_VALUE=context.getString(R.string.language_value);
        PAGE=context.getString(R.string.page);
        PAGE_VALUE=Integer.parseInt(context.getString(R.string.page_value));
        READ_TIMEOUT=Integer.parseInt(context.getString(R.string.read_timeout));
        CONNECT_TIMEOUT=Integer.parseInt(context.getString(R.string.connect_timeout));
        JSON_RESULTS=context.getString(R.string.results);
        JSON_ID=context.getString(R.string.id);
        JSON_ORIGINAL_TITLE=context.getString(R.string.original_title);
        JSON_OVERVIEW=context.getString(R.string.overview);
        JSON_VOTE_AVERAGE=context.getString(R.string.vote_average);
        JSON_RELEASE_DATE=context.getString(R.string.release_date);
        JSON_POSTER_PATH=context.getString(R.string.poster_path);
        MISSING_DETAILS=context.getString(R.string.missing_details);
        JSON_ERROR=context.getString(R.string.json_error);
        JSON_RETRIEVE_ERROR=context.getString(R.string.json_retrieve_error);
        HTTP_ERROR=context.getString(R.string.http_error);

    }

    private static ArrayList<Movies> fetchMovies(String jsonResponse){
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        ArrayList<Movies>movies=new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray resultsArray = baseJsonResponse.getJSONArray(JSON_RESULTS);
            if (resultsArray.length()>0) {
                for (int i=0; i<resultsArray.length(); i++) {
                    JSONObject movieDetails=resultsArray.getJSONObject(i);
                    Integer id=movieDetails.getInt(JSON_ID);
                    String title=movieDetails.getString(JSON_ORIGINAL_TITLE);
                    String overview=movieDetails.getString(JSON_OVERVIEW);
                    double vote_average=movieDetails.getDouble(JSON_VOTE_AVERAGE);
                    String release_date=movieDetails.getString(JSON_RELEASE_DATE);
                    String poster_path=movieDetails.getString(JSON_POSTER_PATH);

                    if (id==null || title==null||poster_path==null) {
                        Log.w(LOG_TAG,MISSING_DETAILS );
                    } else {
                        poster_path=IMAGES_BASE_URL+DIMENSION+poster_path;
                    }
                    movies.add(new Movies(id, title, overview, vote_average, release_date, poster_path));
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG,JSON_ERROR);
            e.printStackTrace();
        }
        return movies;
    }

    private static String makeHttpQuery(URL url) throws IOException{
        String jsonResponse="";
        if (url==null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT) ;
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, HTTP_ERROR+ urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, JSON_RETRIEVE_ERROR, e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);
        scanner.useDelimiter("\\A");
        boolean hasInput = scanner.hasNext();
        if (hasInput) {
            return scanner.next();
        } else {
            return null;
        }
    }

}
