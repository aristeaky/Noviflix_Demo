package com.example.noviflix_demo;
import android.os.AsyncTask;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddMovieTask extends AsyncTask<Movie, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Movie... movies) {
        if (movies.length != 1) {
            return false; // Task expects only one movie to be passed
        }

        Movie movie = movies[0];

        try {

            URL url = new URL("http://dimcost421.ddns.net:9200/api/v1/movies/");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);


            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = new Gson().toJson(movie).getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check the response code to determine if the movie was successfully added
            int responseCode = httpURLConnection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (IOException e) {
            e.printStackTrace();
            return false; // Failed to add movie due to an exception
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        // Handle the result of adding a movie
        if (success) {
            // Movie added successfully
            // You can show a success message or perform any other UI updates here
        } else {
            // Failed to add movie
            // You can show an error message or perform any other error handling here
        }
    }
}
