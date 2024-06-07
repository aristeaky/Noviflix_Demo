package com.example.noviflix_demo;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MovieHandler {
    private static final String TAG = "MovieHandler";

    public List<Movie> loadMoviesFromApi() throws Exception {
        URL url = new URL("http://dimcost421.ddns.net:9200/api/v1/movies");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");

        int responseCode = httpURLConnection.getResponseCode();
        Log.d(TAG, "GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String apiResult = stringBuilder.toString();
            inputStream.close();

            Gson gson = new Gson();
            Type movieListType = new TypeToken<List<Movie>>() {}.getType();

            return gson.fromJson(apiResult, movieListType);
        } else {
            Log.e(TAG, "GET request failed with response code " + responseCode);
            return null;
        }
    }

    public boolean deleteMovie(Movie movie) throws Exception {
        URL url = new URL("http://dimcost421.ddns.net:9200/api/v1/movies/" + movie.getId());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("DELETE");

        int responseCode = httpURLConnection.getResponseCode();
        Log.d(TAG, "DELETE Response Code :: " + responseCode);

        httpURLConnection.disconnect();

        return responseCode == HttpURLConnection.HTTP_NO_CONTENT;
    }



    public void addNewMovie(Movie movie) throws Exception {
        URL url = new URL("http://dimcost421.ddns.net:9200/api/v1/movies/");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);

        try (OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] input = new Gson().toJson(movie, Movie.class).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = httpURLConnection.getResponseCode();
        Log.d(TAG, "POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
            }
            String response = responseBuilder.toString();
            inputStream.close();
            httpURLConnection.disconnect();

            Log.d(TAG, "POST Response: " + response);
        } else {
            Log.e(TAG, "POST request failed with response code " + responseCode);
        }
    }

    public boolean updateMovie(Movie movie) throws Exception {
        URL url = new URL("http://dimcost421.ddns.net:9200/api/v1/movies/" + movie.getId());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("PUT");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);

        try (OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] input = new Gson().toJson(movie, Movie.class).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = httpURLConnection.getResponseCode();
        Log.d(TAG, "PUT Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
            }
            String response = responseBuilder.toString();
            inputStream.close();
            httpURLConnection.disconnect();

            Log.d(TAG, "PUT Response: " + response);

            return true;
        } else {
            Log.e(TAG, "PUT request failed with response code " + responseCode);
            return false;
        }
    }

}
