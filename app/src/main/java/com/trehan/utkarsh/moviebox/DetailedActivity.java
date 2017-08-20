package com.trehan.utkarsh.moviebox;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.trehan.utkarsh.moviebox.database.MovieContract;
import com.trehan.utkarsh.moviebox.utils.ReviewAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class DetailedActivity extends AppCompatActivity {

    private ContentResolver mContentResolver;
    private ShareActionProvider mShareActionProvider;
    private ToggleButton mButtonFav;
    private ImageView mvideo1;
    private ImageView mvideo2;
    private Movie mMovieStr;
    private ArrayList<Review> mReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        mContentResolver = getContentResolver(); //Getting a ContentResolver
        Intent intent = getIntent(); //Getting an Intent
        //Checking for passed value with Intent ie. a Movie object
        mReviews = new ArrayList<>();
        if (intent != null && intent.hasExtra("clickedMovie")) {

            mMovieStr = (Movie) intent.getSerializableExtra("clickedMovie");

            getYouTubekey(mMovieStr.getId()); //Getting YouTube Links
            getUserReviews(mMovieStr.getId()); //Getting Reviews

            setTitle("");
            FrameLayout mFrameLayout = (FrameLayout) findViewById(R.id.DetailFrame);
            File file = new File("/storage/emulated/0/Android/data/com.trehan.utkarsh.moviebox/images/" + mMovieStr.getTile() + ".jpeg");
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            Bitmap blurredBitmap = com.trehan.utkarsh.moviebox.utils.BlurBuilder.blur(DetailedActivity.this, bitmap);
            mFrameLayout.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
            ((TextView) findViewById(R.id.detail_movie_name)).setText(mMovieStr.getTile());
            ((TextView) findViewById(R.id.detail_rating)).setText(getRatingFormat(mMovieStr.getRating()));
            ((TextView) findViewById(R.id.detail_synopsis)).setText(mMovieStr.getSynopsis());
            ((TextView) findViewById(R.id.detail_year)).setText(mMovieStr.getReleaseYear());
            Picasso.with(DetailedActivity.this).load(file).into(((ImageView) findViewById(R.id.detail_poster_image)));
        }

        mButtonFav = (ToggleButton) findViewById(R.id.detail_fav);
        //Checks wheather a movie is store in Favourite database and set its corresponding icon
        Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, MovieContract.MovieEntry.COLUMN_NAME + "=?", new String[]{mMovieStr.getTile()}, null, null);
        if (cursor.getCount() <= 0)
        {   Log.d("DetailedActivity","onStart false"+cursor.getCount());
            mButtonFav.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.favorite_border_white));
        }
        else {
            Log.d("DetailedActivity","onStart true "+cursor.getCount());
            mButtonFav.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.favorite_filled_white));
        }
        cursor.close();

        //A listner for favourite icon toggle button to add or remove a movie from favourites
        mButtonFav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mButtonFav.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.favorite_filled_white));
                    AddToFavourite(mMovieStr);
                } else {
                    mButtonFav.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.favorite_border_white));
                    RemoveFromFavourite(mMovieStr);
                }
            }
        });
        mvideo1 = (ImageView) findViewById(R.id.detail_video_play1);
        //An Implicit Intent to view trailer online
        mvideo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("JOKER", mMovieStr.getTrailerLink1());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mMovieStr.getTrailerLink1())));
            }
        });
        //An Implicit Intent to view trailer online
        mvideo2 = (ImageView) findViewById(R.id.detail_video_play2);
        mvideo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mMovieStr.getTrailerLink2())));
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_share) {

            shareText(mMovieStr.getTrailerLink1());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //Queries the server of movie trailer links
    public void getYouTubekey(String id) {
        String url = "https://api.themoviedb.org/3/movie/" + id + "/videos?api_key=ccaf78fee87c9c31d0a883dca9c71a69&language=en-US";
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            JSONArray results = response.getJSONArray("results");
                            if (results.length() == 1) {
                                JSONObject link1 = (JSONObject) results.get(0);
                                mMovieStr.setTrailerLink1(link1.getString("key"));
                                mMovieStr.setTrailerLink2(link1.getString("key"));
                            } else {
                                JSONObject link1 = (JSONObject) results.get(0);
                                JSONObject link2 = (JSONObject) results.get(1);
                                mMovieStr.setTrailerLink1(link1.getString("key"));
                                mMovieStr.setTrailerLink2(link2.getString("key"));
                            }
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

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void getUserReviews(String id)
    {
        String url = "https://api.themoviedb.org/3/movie/" + id + "/reviews?api_key=ccaf78fee87c9c31d0a883dca9c71a69&language=en-US&page=1";
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            JSONArray results = response.getJSONArray("results");
                            if (results.length() > 0) {
                                for(int i=0; i< results.length();i++)
                                {
                                    JSONObject review = (JSONObject) results.get(i);
                                    String author = review.getString("author");
                                    String content = review.getString("content");
                                    mReviews.add(new Review(author, content));
                                }
                            }
                            setReviewsLayout();
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

        Volley.newRequestQueue(this).add(jsonRequest);
    }


    //Used to share movie trailers
    //@param textToShare Text to be shared
    private void shareText(String textToShare) {

        String mimeType = "text/plain";
        String title = "Share Movie Trailer";
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setText(textToShare)
                .startChooser();
    }
    //set rating foramt in format x/10
    //@params    is x
    public String getRatingFormat(String s) {
        String rating = s + "/10";
        return rating;
    }
    //Add Favourite movie to databse
    //@param movieStr  moive to be added
    private void AddToFavourite(Movie movieStr) {
        ContentValues values = new ContentValues();
        values.put("movieName", movieStr.getTile());
        values.put("movieID", movieStr.getId());
        values.put("movieRating", movieStr.getRating());
        values.put("movieDuration", movieStr.getDuration());
        values.put("movieSynopsis", movieStr.getSynopsis());
        values.put("moviePoster", movieStr.getPosterPath());
        values.put("movieReleaseYear", movieStr.getReleaseYear());
        values.put("movieTrailerOne", movieStr.getTrailerLink1());
        values.put("movieTrailerTwo", movieStr.getTrailerLink2());
        values.put("movieGenre", movieStr.getGenre());
        Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null,
                MovieContract.MovieEntry.COLUMN_NAME + "=?", new String[]{mMovieStr.getTile()}, null, null);
        if (cursor.getCount() <= 0)
        {   Log.d("DetailedActivity","AddToFavourite"+cursor.getCount());
            try {
                mContentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, values);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        cursor.close();

    }
    //Remove Favourite movie to databse
    //@param movieStr  moive to be removed
    private void RemoveFromFavourite(Movie movieStr) {
        String where = "movieName=?";
        String[] args = new String[]{movieStr.getTile()};
        mContentResolver.delete(MovieContract.MovieEntry.CONTENT_URI, where, args);
    }

    private void setReviewsLayout()
    {
        RecyclerView rvContacts = (RecyclerView) findViewById(R.id.rvReviews);

        // Create adapter passing in the sample user data
        ReviewAdapter adapter = new ReviewAdapter(this, mReviews);
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
    }
}
