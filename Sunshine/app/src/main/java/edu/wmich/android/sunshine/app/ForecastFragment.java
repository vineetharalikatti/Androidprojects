package edu.wmich.android.sunshine.app;

/**
 * Created by vineeth on 6/13/16.
 */
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.Activity;

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

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
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
        String postCode = "";
//        postCode = Integer.toString(R.id.enterPostalCodeArea);
        int id = menu.getItemId();
        if(id == R.id.action_refresh){
            FetchWeatherTask weather = new FetchWeatherTask();
            weather.execute("48084");
            return true;
        }
        return super.onOptionsItemSelected(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create some dummy data for the ListView. Here's a sample weekly forecast
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... postcode) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String numOfDays = "9";
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //  String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?zip=48084&mode=json&units=metric&cnt=7";
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("zip", postcode[0])
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units","metric")
                        .appendQueryParameter("cnt",numOfDays);
                String baseUrl = builder.build().toString();
                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(baseUrl.concat(apiKey));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                   // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

//                String[] resultData = parseJson(forecastJsonStr,Integer.parseInt(numOfDays));

//               for(String s : resultData){
//                   Log.e(LOG_TAG, "Forecast entry: " +s);
//               }
                Log.e(LOG_TAG,"json dude"+forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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

            JSONObject json = new JSONObject(forecastJsonStr);

            JSONArray listArray = json.getJSONArray("list");
            String[] resultStr = new String[numOfDays];
            for(int i = 0; i<listArray.length();i++){
                JSONObject object = listArray.getJSONObject(i);

                JSONObject tempObject = object.getJSONObject("temp");

                String max = tempObject.getString("max");
                String min = tempObject.getString("min");

                String highlow = formatHighLow(max, min);

                JSONObject weatherObject = object.getJSONArray("weather").getJSONObject(0);

                String descriptionObject = weatherObject.getString("description");

                String day;
                android.text.format.Time dayTime = new android.text.format.Time();
                dayTime.setToNow();
                int julianStartDay = android.text.format.Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);
                dayTime = new android.text.format.Time();
                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                resultStr[i]=day + " - " + descriptionObject + " - " + highlow;
            }
        return resultStr;

        }

            private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
             SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
             return shortenedDateFormat.format(time);
                 }

        private String formatHighLow(String max, String min) {
            String highLow = max+" / "+min;
            return highLow;
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

