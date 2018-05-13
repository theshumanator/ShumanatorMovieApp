package com.example.fatoumeh.shumanatormovieapp;


/**
 * Created by fatoumeh on 13/05/2018.
 */

public class Movies {
    private Integer movieId;
    private String title;
    private String overview;
    private double vote_average;
    private String release_date;
    private String poster_path;

    public Movies(Integer movieId, String title, String overview, double vote_average, String release_date, String poster_path) {
        this.movieId=movieId;
        this.title=title;
        this.overview=overview;
        this.vote_average=vote_average;
        this.release_date=release_date;
        this.poster_path=poster_path;
    }

    public Integer getMovieId () {return movieId;}

    public void setMovieId (Integer movieId) { this.movieId=movieId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getVoteAverage() {
        return vote_average;
    }

    public void setVoteAverage(double vote_average) {
        this.vote_average = vote_average;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }

    public String getImage() {
        return poster_path;
    }

    public void setImage(String poster_path) {
        this.poster_path = poster_path;
    }
}
