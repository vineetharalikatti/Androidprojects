package edu.wmich.android.popularmovies1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    ArrayList<ImageObject> mMovieAdapter;
    GridView gridView;
    ImageAdapter imageAdapter;

    public MainActivityFragment(){

    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String onStartPreference = sharedPreferences.getString(getString(R.string.preferedsortorder_key),getString(R.string.top_rated));
        updatePopularMovies(onStartPreference);
    }

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
        mMovieAdapter = new ArrayList<ImageObject>();
        gridView = (GridView) rootView.findViewById(R.id.gridlayout_moviesdb);
        imageAdapter = new ImageAdapter(getContext(), R.layout.fragment_main, mMovieAdapter);
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent startDetailActivity = new Intent(getActivity(), DetailActivity.class);
                String originalTitle = mMovieAdapter.get(position).getOriginal_title();
                String overview = mMovieAdapter.get(position).getOverview();
                String releaseDate = mMovieAdapter.get(position).getRelease_date();
                String voteAverage = mMovieAdapter.get(position).getVote_average();
                String imagePath = mMovieAdapter.get(position).getPoster_path();

                ArrayList<String> imageArray = new ArrayList<String>();
                imageArray.add(originalTitle);
                imageArray.add(overview);
                imageArray.add(releaseDate);
                imageArray.add(voteAverage);
                imageArray.add(imagePath);

                startDetailActivity.putStringArrayListExtra(Intent.EXTRA_TEXT,imageArray);

                if (startDetailActivity != null) {
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(id == R.id.action_popular){
            String popular = sharedPreferences.getString(getString(R.string.preferedsortorder_key),getString(R.string.popular));
            updatePopularMovies(popular);
        }else if(id == R.id.action_highest){
            String highest = sharedPreferences.getString(getString(R.string.preferedsortorder_key),getString(R.string.top_rated));
            updatePopularMovies(highest);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePopularMovies(String passedSortOrder) {
        FetchMoviesActivity movies = new FetchMoviesActivity();

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)
            movies.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, passedSortOrder);
        else
            movies.execute(passedSortOrder);
    }


    private class FetchMoviesActivity extends AsyncTask<String, Void, ArrayList<ImageObject>> {

        private final String LOG_TAG =  FetchMoviesActivity.class.getSimpleName();

        private final String API_KEY_STRING = "api_key";
        private final String API_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;
        private final String HTTP = "https";
        private final String URL = "api.themoviedb.org";
        private final String DATA ="3";
        private final String DISCOVER = "discover";
        private final String MOVIE = "movie";
        private final String SORT = "sort_by";

        @Override
        protected ArrayList<ImageObject> doInBackground(String... sortorder) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieDBJsonStr = null;

            try {
                Uri.Builder uri = new Uri.Builder();
                uri.scheme(HTTP)
                        .authority(URL)
                        .appendPath(DATA)
                        .appendPath(MOVIE)
                        .appendPath(sortorder[0])
                        .appendQueryParameter(API_KEY_STRING,API_KEY);
                String baseURL = uri.build().toString();

                String apikey = "?api_key="+API_KEY;
                java.net.URL url = new URL(baseURL);

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

            try{
                return parseMovieJson(movieDBJsonStr);
            }catch(JSONException ex){
                Log.e(LOG_TAG,ex.getMessage(), ex);
                ex.printStackTrace();
            }

            return null;
        }

        private ArrayList<ImageObject> parseMovieJson(String movieDBJsonStr) throws JSONException {

            final String RESULTS = "results";
            final String POSTER_PATH = "poster_path";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String VOTE_AVERAGE = "vote_average";
            final String ORIGINAL_TITLE ="original_title";

            JSONObject json = new JSONObject(movieDBJsonStr);
            JSONArray listArray = json.getJSONArray(RESULTS);
            ArrayList<ImageObject> movieArray = new ArrayList<>();
            for(int i=0;i<listArray.length();i++){
                JSONObject obj  = listArray.getJSONObject(i);
                ImageObject imageObject = new ImageObject();

                imageObject.setPoster_path(obj.getString(POSTER_PATH));
                imageObject.setOriginal_title(obj.getString(ORIGINAL_TITLE));
                imageObject.setOverview(obj.getString(OVERVIEW));
                imageObject.setRelease_date(obj.getString(RELEASE_DATE));
                imageObject.setVote_average(obj.getString(VOTE_AVERAGE));

                movieArray.add(imageObject);

            }
                return movieArray;
        }

        @Override
        protected void onPostExecute(ArrayList<ImageObject> imageObjects) {
            mMovieAdapter.clear();

            if(imageObjects!=null){

                for(ImageObject a : imageObjects){
                    mMovieAdapter.add(a);
                }
                imageAdapter.notifyDataSetChanged();
              }
        }
    }
}
