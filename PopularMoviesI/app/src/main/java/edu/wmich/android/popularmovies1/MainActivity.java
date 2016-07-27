package edu.wmich.android.popularmovies1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity  {

    /* OnCreate creates a new MovieFragment.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(id==R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
            return true;
        }else if(id==R.id.action_popular){
            preferences.edit().putInt(String.valueOf(R.string.sortOrder),R.string.popular).apply();
        }
        return super.onOptionsItemSelected(item);

    }


}
