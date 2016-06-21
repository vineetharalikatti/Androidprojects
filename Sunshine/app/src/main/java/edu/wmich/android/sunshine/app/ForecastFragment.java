package edu.wmich.android.sunshine.app;

/**
 * Created by vineeth on 6/13/16.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.Activity;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;

public class ForecastFragment extends Fragment {

    public ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu){
        int id = menu.getItemId();
        if(id == R.id.action_refresh){
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(menu);
    }

    private void updateWeather() {
        FetchWeatherTask weather = new FetchWeatherTask();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        weather.execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String pos = mForecastAdapter.getItem(position);
                Intent startDetailActivity = new Intent(getActivity(),DetailActivity.class);
                startDetailActivity.putExtra(Intent.EXTRA_TEXT, pos);
                if(startDetailActivity!=null){
                    startActivity(startDetailActivity);
                }

            }
        });
        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private final String HTTP = "http";
        private final String URL = "api.openweathermap.org";
        private final String DATA = "data";
        private final String DECIMAL = "2.5";
        private final String FORECAST = "forecast";
        private final String DAILY = "daily";
        private final String ZIP = "zip";
        private final String MODE = "mode";
        private final String JSON = "json";
        private final String UNITS = "units";
        private final String METRIC = "metric";
        private final String IMPERIAL = "imperial";
        private final String COUNT = "cnt";

        @Override
        protected String[] doInBackground(String... postcode) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String numOfDays = "9";
            String forecastJsonStr = null;

            try {
                // http://openweathermap.org/API#forecast
                //  String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?zip=48084&mode=json&units=metric&cnt=7";
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(HTTP)
                        .authority(URL)
                        .appendPath(DATA)
                        .appendPath(DECIMAL)
                        .appendPath(FORECAST)
                        .appendPath(DAILY)
                        .appendQueryParameter(ZIP, postcode[0])
                        .appendQueryParameter(MODE, JSON)
                        .appendQueryParameter(UNITS, METRIC)
                        .appendQueryParameter(COUNT,numOfDays);
                String baseUrl = builder.build().toString();
                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(baseUrl.concat(apiKey));


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return parseJson(forecastJsonStr, Integer.parseInt(numOfDays));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private String[] parseJson(String forecastJsonStr, int numOfDays) throws JSONException {

            final String LIST = "list";
            final String TEMP = "temp";
            final String MAX = "max";
            final String MIN = "min";
            final String WEATHER = "weather";
            final String DESCRIPTION = "description";
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPreferences.getString(getString(R.string.temperature),getString(R.string.defaultUnit));

            Log.e(LOG_TAG,"unit type is "+unitType);
            //if(unitType.equals("Imperial"))
            JSONObject json = new JSONObject(forecastJsonStr);

            JSONArray listArray = json.getJSONArray(LIST);
            String[] resultStr = new String[numOfDays];
            for(int i = 0; i<listArray.length();i++){
                JSONObject object = listArray.getJSONObject(i);

                JSONObject tempObject = object.getJSONObject(TEMP);

                String max = tempObject.getString(MAX);
                String min = tempObject.getString(MIN);

                String highlow = formatHighLow(max, min, unitType);

                JSONObject weatherObject = object.getJSONArray(WEATHER).getJSONObject(0);

                String descriptionObject = weatherObject.getString(DESCRIPTION);

                String day;
                android.text.format.Time dayTime = new android.text.format.Time();
                dayTime.setToNow();
                int julianStartDay = android.text.format.Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);
                dayTime = new android.text.format.Time();
                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);
                if(unitType.equals("Metric")){
                    resultStr[i]=day + " - " + descriptionObject + " - " + highlow + " C";
                }else if(unitType.equals("Imperial")){
                    resultStr[i]=day + " - " + descriptionObject + " - " + highlow + " F";
                }

            }
            return resultStr;
        }

         private String getReadableDateString(long time){
             SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
             return shortenedDateFormat.format(time);
         }

        private String formatHighLow(String max, String min, String unitType) {
            Double maximum,minimum;
            String highLow = null;
            if(unitType.equals("Metric")){
                maximum = Double.parseDouble(max);
                minimum = Double.parseDouble(min);
                highLow = Math.round(maximum)+" / "+ Math.round(minimum);
            }else if(unitType.equals("Imperial")){

                maximum = Double.parseDouble(max);
                minimum = Double.parseDouble(min);
                maximum = (maximum*1.8)+32;
                minimum = (minimum*1.8)+32;
                highLow = Math.round(maximum)+"/"+Math.round(minimum);
            }
            return  highLow;
        }

        @Override
        protected void onPostExecute(String[] resultStr) {
            if(resultStr!=null){
                mForecastAdapter.clear();
                for(String dayForecaster : resultStr){
                    mForecastAdapter.add(dayForecaster);
                }
            }
        }
    }
}

