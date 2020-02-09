/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EarthquakeActivity extends AppCompatActivity {

    public static final String LOG_TAG = EarthquakeActivity.class.getSimpleName();

    public static String USGS_REQUEST_URL = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = new Date();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -14);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date todate1 = cal.getTime();

        String startTime = simpleDateFormat.format(todate1);


        String endTime = simpleDateFormat.format(date1);

        USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&minmagnitude=5&starttime="+ startTime + "&endtime=" + endTime;


        TsunamiAsyncTask task = new TsunamiAsyncTask();
        task.execute();



    }

    private class TsunamiAsyncTask extends AsyncTask<URL, Void, ArrayList>{

        @Override
        protected ArrayList doInBackground(URL... urls) {

            Log.i("output", USGS_REQUEST_URL);

            URL url = createUrl(USGS_REQUEST_URL);
            ArrayList<Earthquake> earthquakes = new ArrayList<>();

             String jsonResponse ="";
             try {
                 jsonResponse = makeHttpRequest(url);
             }catch (IOException  e){
                 Log.e("error", "doInBackgroundError", e);
             }

            try {

                JSONObject jsonObject = new JSONObject(jsonResponse);

                JSONArray features = jsonObject.getJSONArray("features");


                for (int i = 0; i < features.length(); i++){
                    JSONObject array = features.getJSONObject(i);
                    JSONObject properties = array.getJSONObject("properties");
                    Double magnitude = properties.getDouble("mag");
                    String location = properties.getString("place");
                    String url1 = properties.getString("url");
                    Long time = properties.getLong("time");
                    Date dateObject = new Date(time);
                    Log.i("date", dateObject.toString());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
                    String date = dateFormat.format(dateObject);
                    String timeing = timeFormat.format(dateObject);
                    Log.i("string", date);

                    DecimalFormat decimalFormat = new DecimalFormat("0.0");

                    String output = decimalFormat.format(magnitude);


                    earthquakes.add(new Earthquake(output,location,date, timeing, url1));

                }


            }catch (Exception e){

            }

            return earthquakes;
        }

        @Override
        protected void onPostExecute(ArrayList earthquakes) {

            ListView earthquakeListView = (ListView) findViewById(R.id.list);

            // Create a new {@link ArrayAdapter} of earthquakes
            final EarthquakeAdapter earthquakeAdapter =  new EarthquakeAdapter(getApplicationContext(), earthquakes);
            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            earthquakeListView.setAdapter(earthquakeAdapter);

            earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // Find the current earthquake that was clicked on
                    Earthquake currentEarthquake = earthquakeAdapter.getItem(position);

                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                    // Create a new intent to view the earthquake URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                }
            });

        }

        private URL createUrl(String stringUrl){
            URL url = null;

            try {
                url = new URL(stringUrl);
            }catch (MalformedURLException exception){
                Log.e(LOG_TAG, "Error with creating URL", exception);
            }
            return url;
        }

        private String makeHttpRequest(URL url)throws IOException{
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }catch (IOException e){
                // TOTO: Handle the exception
            }finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (inputStream != null){
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream)throws IOException{
            StringBuilder output = new StringBuilder();
            if (inputStream != null){
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null){
                    output.append(line);
                    line = reader.readLine();
                }

            }
            return  output.toString();
        }
    }

}
