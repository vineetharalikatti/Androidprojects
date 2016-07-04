package edu.wmich.android.popularmovies1;

import android.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by vineeth on 7/3/16.
 */
public class MainActivityFragment extends Fragment {

    LayoutInflater inflater;
    ViewGroup container;
    View rootView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        this.inflater = inflater;
        this.container = container;

        rootView = inflater.inflate(R.layout.fragment_main,container,false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridlayout_moviesdb);
        gridView.setAdapter(new ImageAdapter(getContext(),R.layout.fragment_main, new ArrayList<String>()));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String pos = AdapterView.
                Toast.makeText(getContext(), id+""+position, Toast.LENGTH_SHORT).show();
                Intent startDetailActivity = new Intent(getActivity(),DetailActivity.class);
                if(startDetailActivity!=null){
                    startActivity(startDetailActivity);
                }
            }
        });

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            FetchMoviesActivity movies = new FetchMoviesActivity();
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)
                movies.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                movies.execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchMoviesActivity extends AsyncTask<Void, Void, Void> {

        //https://api.themoviedb.org/3/movie/550?api_key=c0631d5a0d4a9400627f4628a065c66b
        //http://api.openweathermap.org/data/2.5/forecast/daily?zip=48084&mode=json&units=metric&cnt=7&APPID=123456

        private final String LOG_TAG =  FetchMoviesActivity.class.getSimpleName();

        private final String API_KEY_STRING = "api_key";
        private final String API_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;
        private final String HTTP = "https";
        private final String URL = "api.themoviedb.org";
        private final String DATA ="3";
        private final String MOVIE = "movie";
        private final String NUMBER = "550";

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieDBJsonStr = null;

            try {
                Uri.Builder uri = new Uri.Builder();
                uri.scheme(HTTP)
                        .authority(URL)
                        .appendPath(DATA)
                        .appendPath(MOVIE)
                        .appendPath(NUMBER)
                        .appendQueryParameter(API_KEY_STRING,API_KEY);
                String baseURL = uri.build().toString();
                String apikey = "?api_key="+API_KEY;
                java.net.URL url = new URL(baseURL);

                Log.e(LOG_TAG, "vineeth url" + baseURL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                try{
                    urlConnection.connect();
                }catch(Throwable connection){
                    Log.e(LOG_TAG,connection.toString());
                }
                InputStream input = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(input == null){
                    //do nothing
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(input));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieDBJsonStr = buffer.toString();

                Log.e(LOG_TAG,movieDBJsonStr);

            }catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }
    }
}
