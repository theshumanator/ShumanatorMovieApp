package com.example.fatoumeh.shumanatormovieapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by fatoumeh on 16/06/2018.
 */

public interface MovieRetroFitAPIInterface {
    @GET("/3/movie/popular?")
    Call<MoviesWithRetroFit> getPopularMovies(@Query("api_key") String apiKey);

    @GET("/3/movie/top_rated?")
    Call<MoviesWithRetroFit> getTopRatedMovies(@Query("api_key") String apiKey);
}
