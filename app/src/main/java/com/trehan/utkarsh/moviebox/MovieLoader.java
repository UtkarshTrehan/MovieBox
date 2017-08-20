package com.trehan.utkarsh.moviebox;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.trehan.utkarsh.moviebox.database.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MovieLoader extends Loader<ArrayList<Movie>> {

    private Context mContext;
    private int moviesCount;
    private int moviesFlag;
    private String sortby;
    private String cachedSortby = new String();
    private ArrayList<Movie> movies = new ArrayList<>();;
    private ArrayList<Movie> Cachemovies = new ArrayList<>();;
    private ArrayList<Target> mTarget;
    private ContentResolver mContentResolver;
    public static final String ACTION = "com.trehan.utkarsh.moviebox.FORCE";

    public MovieLoader(Context context, String sortby) {
        super(context);
        Log.d("DEBUG","MovieLoader");
        mContext=context;
        this.sortby = sortby;
    }

    public MovieLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        Log.d("DEBUG","onStartLoading");
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter filter = new IntentFilter(ACTION);
        manager.registerReceiver(broadcastReceiver,filter);
        if(cachedSortby.equals(null))
        {
            Log.d("DEBUG","cachedSortby.equals(null)"+cachedSortby);
            forceLoad();
        }
        else if(!cachedSortby.equals(sortby)) {
            Log.d("DEBUG","!cachedSortby.equals(sortby)"+cachedSortby);
            forceLoad();
        }
        else {
            Log.d("DEBUG","cachedSortby.equals(sortby)--"+cachedSortby);
            deliverResult(Cachemovies);
        }

    }

    @Override
    public void forceLoad() {
        Log.d("DEBUG","forceLoad");
        super.forceLoad();
        movies.clear();
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Boolean networkStatus=activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (networkStatus) {
            if (!sortby.equals("favourite"))
                getMoviesJson(sortby);
            else
                setFavouites();
        }
        else
        {
            Toast.makeText(mContext, "No Internet Connection Showing Favourites", Toast.LENGTH_SHORT).show();
            setFavouites();
        }
    }

    @Override
    public void deliverResult(ArrayList<Movie> data) {
        Log.d("DEBUG","deliverResult");
        Cachemovies=data;
        cachedSortby=sortby;
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        Log.d("DEBUG","onReset");
        super.onReset();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean cancelLoad() {
        return super.cancelLoad();
    }

    public void getMoviesJson(String sortBy) {
        Log.d("DEBUG","getMoviesJson");
        String url = "https://api.themoviedb.org/3/movie/" + sortBy + "?api_key=ccaf78fee87c9c31d0a883dca9c71a69&language=en-US";
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getMoviesIDList(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(mContext).add(jsonRequest);
    }
    //Get a list of all the movie's id and store it in Arraylist
    //@param jsonObject   json response from server fetched by getMoviesJson
    public void getMoviesIDList(JSONObject jsonObject) throws JSONException {
        Log.d("MovieLoader","getMoviesIDList");
        ArrayList<String> moviesIDList = new ArrayList<>();
        JSONArray moviesArray = jsonObject.getJSONArray("results");
        moviesCount = moviesArray.length();
        for (int i = 0; i < moviesCount; i++) {
            JSONObject movie = moviesArray.getJSONObject(i);
            String id = movie.getString("id");
            moviesIDList.add(id);
        }
        getMoviesDetails(moviesIDList);
    }
    //Get all movie(s) details by passing each id to getMovieDetail
    //@params movieIDList    List of all movie's id
    private void getMoviesDetails(ArrayList<String> moviesIDList) {
        moviesFlag=0;
        Log.d("DEBUG","getMoviesDetails");
        for (String movieID : moviesIDList) {
            getMovieDetail(movieID);
        }
    }
    //Queries a movie details according to movie ID
    //@params movieID   ID used to query details of a movie
    public void getMovieDetail(String movieID) {
        Log.d("DEBUG","getMovieDetail");
        String url = "https://api.themoviedb.org/3/movie/" + movieID + "?api_key=ccaf78fee87c9c31d0a883dca9c71a69&language=en-US";
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            setMovieList(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        int socketTimeout = 30000; // 30 seconds. You can change it
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(mContext).add(jsonRequest);
    }
    //Inflate all Movie objects with data and adding to movies List
    public void setMovieList(JSONObject movieObject) throws JSONException {
        Log.d("DEBUG","setMovieList");
        String title = movieObject.getString("original_title");
        String synopsis = movieObject.getString("overview");
        String posterPath = movieObject.getString("poster_path");
        String releaseYear = getMovieYear(movieObject.getString("release_date"));
        String duration = movieObject.getString("runtime");
        String rating = movieObject.getString("vote_average");
        String id = movieObject.getString("id");
        ArrayList<String> genresList = new ArrayList<>();
        String genre = new String();
        JSONArray genres = movieObject.getJSONArray("genres");
        for (int i = 0; i < genres.length(); i++) {
            JSONObject jo = genres.getJSONObject(i);
            genre=genre+(jo.getString("name"))+", ";
        }
        movies.add(new Movie(title, synopsis, posterPath, releaseYear, duration, rating, id, genre));
        moviesFlag++;
        if (moviesFlag == moviesCount) {
            String extSdcard = Environment.getExternalStorageDirectory().toString();
            final File directory = new File(extSdcard+"/Android/data/com.trehan.utkarsh.moviebox/images");
            boolean d = directory.mkdirs();
            ArrayList<Movie> unDownloadedImages = new ArrayList<>();
            for (int i=0; i<movies.size(); i++)
            {   File f = new File(directory, movies.get(i).getTile()+".jpeg");
                if(f.exists()){
                    continue;
                }
                else
                    unDownloadedImages.add(movies.get(i));
            }
            Log.d("DEBUG","utraboom");
            if(!unDownloadedImages.isEmpty()) {
                Log.d("DEBUG","not null"+unDownloadedImages.size());
                dowloadImagePoster(unDownloadedImages);}

            else {
                Log.d("DEBUG","null");
                deliverResult(movies);
            }
        }
    }
    //Downlaoding Images of movie poster and saving to external memory
    private void dowloadImagePoster(final ArrayList<Movie> unDowloadedmovie) {
        Log.d("DEBUG","dowloadImagePoster");
        String extSdcard = Environment.getExternalStorageDirectory().toString();
        final File directory = new File(extSdcard + "/Android/data/com.trehan.utkarsh.moviebox/images");
        boolean d = directory.mkdirs();

        mTarget = new ArrayList<Target>();
        for (int i = 0; i < unDowloadedmovie.size(); i++) {
            final int k = i;
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d("DEBUG", "onBitmapLoaded");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            File myImageFile = new File(directory, unDowloadedmovie.get(k).getTile() + ".jpeg"); // Create image file
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(myImageFile);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            Log.i("image", "image saved to >>>" + myImageFile.getAbsolutePath());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            if (k == unDowloadedmovie.size() - 1)
                            {Log.d("dog", String.valueOf(k));
                                deliverResult(movies);}
                        }
                    }.execute();
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d("DEBUG", "onBitmapFailed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Log.d("DEBUG", "onPrepareLoad");
                }
            };
            mTarget.add(target);
            Picasso.with(mContext).load(unDowloadedmovie.get(k).getPosterPath()).into(target);
        }
    }
    //extracting year from release date of movie
    public String getMovieYear(String s) {
        String year = s.split("-")[0];
        return year;
    }
    //used to display favourites
    public void setFavouites() {
        mContentResolver = mContext.getContentResolver();
        Log.d("DEBUG","Iniside setFavouites");
        Cursor moviesCursor = mContentResolver.query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
        Log.d("Favourites",moviesCursor.toString());
        if (moviesCursor != null) {
            Log.d("Favourites","not null");
            // move cursor to first row
            if (moviesCursor.moveToFirst()) {
                do {
                    String title = moviesCursor.getString(moviesCursor.getColumnIndex("movieName"));
                    String rating = moviesCursor.getString(moviesCursor.getColumnIndex("movieRating"));
                    String duration = moviesCursor.getString(moviesCursor.getColumnIndex("movieDuration"));
                    String synopsis = moviesCursor.getString(moviesCursor.getColumnIndex("movieSynopsis"));
                    String poster = moviesCursor.getString(moviesCursor.getColumnIndex("moviePoster"));
                    String id = moviesCursor.getString(moviesCursor.getColumnIndex("movieID"));
                    String releaseYear = moviesCursor.getString(moviesCursor.getColumnIndex("movieReleaseYear"));

                    String genre = moviesCursor.getString(moviesCursor.getColumnIndex("movieGenre"));
                    Log.d("Favourites",title);
                    movies.add(new Movie(title,synopsis,poster,releaseYear,duration,rating,id,genre));

                } while (moviesCursor.moveToNext());

            }
            moviesCursor.close();
        }
        deliverResult(movies);
    }

    //Boradcast Receiver to listen the changes when user changes Settings
    private BroadcastReceiver broadcastReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sortby=intent.getStringExtra("SortBy");
            Log.d("DEBUG","onReceive");
        }
    };

}
