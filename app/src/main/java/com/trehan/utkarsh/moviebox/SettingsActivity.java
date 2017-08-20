package com.trehan.utkarsh.moviebox;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ActionBar actionBar =this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //void setDisplayHomeAsUpEnabled (boolean showHomeAsUp)
            /*showHomeAsUp	boolean:
              true to show the user that selecting home will return one level up
              rather than to the top level of the app.*/
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            //void navigateUpFromSameTask (Activity sourceActivity)
            //sourceActivity	Activity: The current activity from which the user is attempting to navigate up
        }
        return super.onOptionsItemSelected(item);
    }
}
