package com.trehan.utkarsh.moviebox;

import java.io.Serializable;

//A Movie template
public class Movie implements Serializable{

    private static final String POSTER_ID = "https://image.tmdb.org/t/p/w500";
    private static final String YOUTUBE_ID = "https://www.youtube.com/watch?v=";
    private String id;
    private String tile;
    private String posterPath;
    private String duration;
    private String releaseYear;
    private String rating;
    private String synopsis;
    private String trailerLink1;
    private String trailerLink2;
    private String genre;
    //Movie Constructor
    public Movie(String title, String synopsis, String posterPath, String releaseYear, String duration, String rating, String id, String genre) {
        this.tile = title;
        this.posterPath = posterPath;
        this.releaseYear = releaseYear;
        this.duration = duration;
        this.rating = rating;
        this.synopsis = synopsis;
        this.id = id;
        this.genre = genre;
    }

    //Setter and getters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }

    public String getPosterPath() {
        return POSTER_ID+posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getTrailerLink1() {
        return YOUTUBE_ID+trailerLink1;
    }

    public void setTrailerLink1(String trailerLink1) {
        this.trailerLink1 = trailerLink1;
    }

    public String getTrailerLink2() {
        return YOUTUBE_ID+trailerLink2;
    }

    public void setTrailerLink2(String trailerLink2) {
        this.trailerLink2 = trailerLink2;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
