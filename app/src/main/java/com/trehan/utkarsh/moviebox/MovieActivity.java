package com.trehan.utkarsh.moviebox;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.trehan.utkarsh.moviebox.utils.ItemOffsetDecoration;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.Serializable;
import java.util.ArrayList;

public class MovieActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Movie>>, SharedPreferences.OnSharedPreferenceChangeListener {

    private ImageView mImageView;
    private com.wang.avi.AVLoadingIndicatorView loadingIndicatorView;
    private RecyclerView mRecyclerView;
    private LoaderManager mLoaderManager;
    private MovieLoader mMovieLoader;
    private MovieAdapter adapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences mPrefs;
    private FrameLayout mFramlayout;
    private LinearLayout storagePermissionLayout;
    private Button mPermissionButton;
    private static final int LOADER_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_activity);


        mPrefs = this.getPreferences(Context.MODE_PRIVATE);
        mPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                //do nothing
            }
        });
        Boolean permission = mPrefs.getBoolean("permissionStorage", false);

        if(!permission)
        {
            isStoragePermissionGranted();

        }

        Log.d("MovieActivity", "phale");
        Log.d("MovieActivity", "baad");
        Log.d("DEBUG", "onCreate");

        mRecyclerView = (RecyclerView) findViewById(R.id.rvMovies);
        View view = getLayoutInflater().inflate(R.layout.movie_item, null); //inflating movie_item
        mImageView = (ImageView) view.findViewById(R.id.moviesCardImage); //refrence to moviesCardimage
        loadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.homeScreenIndicator);
        mFramlayout = (FrameLayout) findViewById(R.id.noFavouriteFrame);
        setHomeScreenLayout();
        mLoaderManager = getSupportLoaderManager();
        if(permission) {
            if (mLoaderManager != null) {
                mLoaderManager.initLoader(LOADER_ID, null, this);
            }
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Set movies image sizes on main screen according to screen size and orientation
    //Setting Decorator for proper in between spacing between items
    private void setHomeScreenLayout() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int Deviceheight = displayMetrics.heightPixels; //Device height
        int Devicewidth = displayMetrics.widthPixels; //Device width
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //If device in portrait setting images width to over 50% each
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            mImageView.getLayoutParams().height = Deviceheight / 2;
            mImageView.getLayoutParams().width = Devicewidth / 2;
        } else {
            //If device in portrait setting images width to over 33.33% each
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            mImageView.getLayoutParams().height = Deviceheight / 3;
            mImageView.getLayoutParams().width = Devicewidth / 3;
        }
        //setting proper spacing between items in RecyclerView
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.home_screen_ItemOffsetDecoration);
        mRecyclerView.addItemDecoration(itemDecoration);

    }

    //Loader Callbacks
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d("DEBUG", "onCreateLoader");
        //getting changed pref value and passing to MovieConstructor
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortBy = prefs.getString("urlKey", "popular");
        return new MovieLoader(this, sortBy);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, final ArrayList<Movie> data) {
        if (data.isEmpty()) {
            loadingIndicatorView.setVisibility(View.INVISIBLE);
            mFramlayout.setVisibility(View.VISIBLE);
        } else {
            Log.d("DEBUG", "onLoadFinished");
            mFramlayout.setVisibility(View.GONE);
            loadingIndicatorView.setVisibility(View.INVISIBLE); //Making Loading icon disappear
            mRecyclerView.setVisibility(View.VISIBLE); //Making All images appear
            adapter = new MovieAdapter(this, data); // creating new MovieAdapter
            mRecyclerView.setAdapter(adapter); //connecting RecyclerView with adapater
            //CickListener to open more DetaileView when a movie is clicked
            adapter.setOnItemClickListener(new MovieAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(MovieActivity.this, DetailedActivity.class);
                    intent.putExtra("clickedMovie", (Serializable) data.get(position));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d("DEBUG", "onLoaderReset");
    }

    //Callback when any prefrences are changed
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d("DEBUG", "onSharedPreferenceChanged");
        mRecyclerView.setVisibility(View.INVISIBLE);
        mFramlayout.setVisibility(View.GONE);
        loadingIndicatorView.setVisibility(View.VISIBLE);
        String sortBy = sharedPreferences.getString(s, "popular"); //getting changed pref value
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this); //restarting new Loader
        Intent intent = new Intent(MovieLoader.ACTION);
        intent.putExtra("SortBy", sortBy.toString());
        LocalBroadcastManager.getInstance(MovieActivity.this).sendBroadcast(intent); //Instantiating broadcast to set sortby value in MovieLoader

    }

    //Asking user for Storage Permssion if already not given
    public boolean isStoragePermissionGranted() {
        Log.d("MovieActivity", "isStoragePermissionGranted");
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("MovieActivity", "Permission is granted");
                return true;
            } else {

                Log.v("MovieActivity", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("MovieActivity", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("MovieActivity", "onRequestPermissionsResult " + requestCode + grantResults[0]+permissions[0]);
        Log.d("MovieActivity", "onRequestPermissionsResult " + grantResults.length+" "+permissions.length);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MovieActivity", "granted");
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean("permissionStorage", true);
                    editor.commit();
                    mLoaderManager.initLoader(LOADER_ID, null, this);

                } else {
                    Log.d("MovieActivity", "denied");
                    Toast.makeText(this, "Opps! You Need Storage Permission", Toast.LENGTH_SHORT).show();
                    isStoragePermissionGranted();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exiting MovieBox")
                .setMessage("Are you sure you want to Exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}

