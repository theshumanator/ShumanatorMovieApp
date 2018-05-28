package utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {}

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
    private static String REVIEW_QUERY;
    private static String TRAILER_QUERY;

    //json related strings
    private static String JSON_RESULTS;
    private static String JSON_ID;
    private static String JSON_ORIGINAL_TITLE;
    private static String JSON_OVERVIEW;
    private static String JSON_VOTE_AVERAGE;
    private static String JSON_RELEASE_DATE;
    private static String JSON_POSTER_PATH;
    private static String JSON_REVIEW_AUTHOR;
    private static String JSON_REVIEW_TEXT;

    private static String JSON_TRAILER_KEY;
    private static String JSON_TRAILER_TYPE;
    private static String JSON_TRAILER_SOURCE;
    private static String TRAILER;
    private static String TRAILER_SOURCE;

    private static String MISSING_DETAILS;
    private static String JSON_ERROR;
    private static String JSON_RETRIEVE_ERROR;
    private static String HTTP_ERROR;

    private static int movieId=-1;

    public static String [] getMovieReview(int queryMovieId, Context context) {
        movieId=queryMovieId;
        String [] review;
        if (movieId==-1) {
            throw new IllegalArgumentException("MovieId cannot be -1");
        } else {
            setupReviewStrings(context);
            URL url = buildURL(context.getString(R.string.reviews_query));
            String jsonResponse=null;
            try {
                jsonResponse=makeHttpQuery(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            review=getReviewContent(jsonResponse);
        }
        return review;
    }

    public static ArrayList<String> getMovieTrailers(int queryMovieId, Context context) {
        movieId=queryMovieId;
        ArrayList<String> trailers;
        if (movieId==-1) {
            throw new IllegalArgumentException("MovieId cannot be -1");
        } else {
            setupTrailerStrings(context);
            URL url = buildURL(context.getString(R.string.trailers_query));
            String jsonResponse=null;
            try {
                jsonResponse=makeHttpQuery(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            trailers=getTrailerContent(jsonResponse);
        }
        return trailers;
    }

    public static ArrayList<Movies> queryMovies(String queryType, Context context){
        setupStrings(context);
        URL url = buildURL(queryType);
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


    private static URL buildURL(String queryType){
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
        } else if (queryType.equals(REVIEW_QUERY) || queryType.equals(TRAILER_QUERY)) {
            QUERY_BRANCH=MOVIES_BASE_URL+movieId+"/"+queryType;
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

    private static void setupTrailerStrings(Context context) {
        setupReviewStrings(context);
        TRAILER_QUERY=context.getString(R.string.trailers_query);
        JSON_TRAILER_KEY=context.getString(R.string.trailer_key);
        JSON_TRAILER_TYPE=context.getString(R.string.trailer_type);
        JSON_TRAILER_SOURCE=context.getString(R.string.trailer_source);
        TRAILER=context.getString(R.string.trailer);
        TRAILER_SOURCE=context.getString(R.string.trailer_youtube);
    }

    private static void setupReviewStrings(Context context) {
        API_KEY=context.getString(R.string.api_key);
        API_KEY_VALUE=context.getString(R.string.api_key_value);
        LANGUAGE=context.getString(R.string.language);
        LANGUAGE_VALUE=context.getString(R.string.language_value);
        PAGE=context.getString(R.string.page);
        PAGE_VALUE=Integer.parseInt(context.getString(R.string.page_value));
        READ_TIMEOUT=Integer.parseInt(context.getString(R.string.read_timeout));
        CONNECT_TIMEOUT=Integer.parseInt(context.getString(R.string.connect_timeout));
        JSON_RESULTS=context.getString(R.string.results);
        JSON_REVIEW_AUTHOR=context.getString(R.string.review_author);
        JSON_REVIEW_TEXT=context.getString(R.string.review_content);
        JSON_ERROR=context.getString(R.string.json_error);
        JSON_RETRIEVE_ERROR=context.getString(R.string.json_retrieve_error);
        HTTP_ERROR=context.getString(R.string.http_error);
        MISSING_DETAILS=context.getString(R.string.missing_details);
        REVIEW_QUERY=context.getString(R.string.reviews_query);
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

    private static ArrayList<String> getTrailerContent (String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        ArrayList<String>trailers=new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray resultsArray = baseJsonResponse.getJSONArray(JSON_RESULTS);
            if (resultsArray.length()>0) {
                for (int i=0; i<resultsArray.length(); i++) {
                    JSONObject trailerDetails=resultsArray.getJSONObject(i);
                    String trailerType=trailerDetails.getString(JSON_TRAILER_TYPE);
                    String trailerSource=trailerDetails.getString(JSON_TRAILER_SOURCE);
                    if (trailerType==null||trailerSource==null) {
                        Log.w(LOG_TAG,MISSING_DETAILS );
                        throw new JSONException(MISSING_DETAILS);
                    } else {
                        if (trailerType.equals(TRAILER) && trailerSource.equals(TRAILER_SOURCE)) {
                            String trailerKey=trailerDetails.getString(JSON_TRAILER_KEY);
                            trailers.add(trailerKey);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG,JSON_ERROR);
            e.printStackTrace();
        }
        return trailers;
    }

    private static String [] getReviewContent(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }
        String [] reviewContent=new String[2];

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray resultsArray = baseJsonResponse.getJSONArray(JSON_RESULTS);
            if (resultsArray.length()>0) {
                for (int i=0; i<resultsArray.length(); i++) {
                    JSONObject reviewDetails=resultsArray.getJSONObject(i);
                    String reviewText=reviewDetails.getString(JSON_REVIEW_TEXT);
                    String reviewAuthor=reviewDetails.getString(JSON_REVIEW_AUTHOR);
                    if (reviewContent==null||reviewAuthor==null) {
                        Log.w(LOG_TAG,MISSING_DETAILS );
                        throw new JSONException(MISSING_DETAILS);
                    } else {
                        reviewContent[0]=reviewText;
                        reviewContent[1]=reviewAuthor;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG,JSON_ERROR);
            e.printStackTrace();
        }
        return reviewContent;
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

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        return isConnected;
    }

}
