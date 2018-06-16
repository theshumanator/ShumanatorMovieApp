package com.example.fatoumeh.shumanatormovieapp;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by fatoumeh on 16/06/2018.
 */

public class MovieRetroFitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.themoviedb.org/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
