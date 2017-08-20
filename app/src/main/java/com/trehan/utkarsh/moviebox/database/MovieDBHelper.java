package com.trehan.utkarsh.moviebox.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MovieDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "movieList.db";
    SQLiteDatabase db;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("MovieDBHelper","constructorMovieDBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("MovieDBHelper","onCreateMovieDBHelper");
        addGenreTable(sqLiteDatabase);
        addMovieTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d("MovieDBHelper","onUpgrade");

    }
    private void addGenreTable(SQLiteDatabase db){

        db.execSQL(
                "CREATE TABLE " + MovieContract.GenreEntry.TABLE_NAME + " (" +
                        MovieContract.GenreEntry._ID + " INTEGER PRIMARY KEY, " +
                        MovieContract.GenreEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL);"
        );
    }
    private void addMovieTable(SQLiteDatabase db){
        Log.d("MovieDBHelper","addMovieTable");
        db.execSQL(
                "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                        MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                        MovieContract.MovieEntry.COLUMN_ID + " TEXT NOT NULL UNIQUE, " +
                        MovieContract.MovieEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_DURATION + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_RELEASE_YEAR + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_TRAILER_ONE + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_TRAILER_TWO + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.COLUMN_GENRE + " TEXT NOT NULL, " +
                        "FOREIGN KEY (" + MovieContract.MovieEntry.COLUMN_GENRE + ") " +
                        "REFERENCES " + MovieContract.GenreEntry.TABLE_NAME + " (" + MovieContract.GenreEntry._ID + "));"
        );
    }

}
