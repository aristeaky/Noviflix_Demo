package com.example.noviflix_demo;
import java.util.UUID;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView textView;
    private EditText inputTitle, inputDirector, inputPlot, inputMovieIndex;
    private Button updateButton, deleteButton, addButton, specificButton, randomButton, getallmoviesbutton;
    private List<Movie> movieList;
    private String generateUniqueID() {
        return UUID.randomUUID().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = findViewById(R.id.textView);
        inputTitle = findViewById(R.id.inputTitle);
        inputDirector = findViewById(R.id.inputDirector);
        inputPlot = findViewById(R.id.inputPlot);
        inputMovieIndex = findViewById(R.id.inputMovieIndex);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        addButton = findViewById(R.id.addButton);
        specificButton = findViewById(R.id.specificButton);
        randomButton = findViewById(R.id.randomButton);
        getallmoviesbutton = findViewById(R.id.getAllMoviesButton);

        new LoadMoviesTask().execute();

        updateButton.setOnClickListener(v -> {
            try {
                int index = Integer.parseInt (inputMovieIndex.getText().toString())-1;
                if (movieList != null && index >= 0 && index < movieList.size()) {
                    Movie movieToUpdate = movieList.get(index);
                    String newTitle = inputTitle.getText().toString();
                    String newDirector = inputDirector.getText().toString();
                    String newPlot = inputPlot.getText().toString();

                    if (!newTitle.isEmpty()) {
                        movieToUpdate.setTitle(newTitle);
                    }
                    if (!newDirector.isEmpty()) {
                        movieToUpdate.setDirector(newDirector);
                    }
                    if (!newPlot.isEmpty()) {
                        movieToUpdate.setPlot(newPlot);
                    }


                    new UpdateMovieTask().execute(movieToUpdate);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid index", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid input for movie index", e);
                Toast.makeText(MainActivity.this, "Invalid index", Toast.LENGTH_SHORT).show();
            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int index = Integer.parseInt(inputMovieIndex.getText().toString())-1;
                    if (movieList != null && index >= 0 && index < movieList.size()) {
                        Movie movieToDelete = movieList.get(index);
                        new DeleteMovieTask().execute(movieToDelete);
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid index", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid input for movie index", e);
                    Toast.makeText(MainActivity.this, "Invalid index", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = inputTitle.getText().toString();
                String director = inputDirector.getText().toString();
                String plot = inputPlot.getText().toString();

                Log.d(TAG, "Title: " + title);
                Log.d(TAG, "Director: " + director);
                Log.d(TAG, "Plot: " + plot);

                if (title.isEmpty() || director.isEmpty() || plot.isEmpty()) {
                    Toast.makeText(MainActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uniqueID = generateUniqueID();
                Log.d(TAG, "Generated unique ID: " + uniqueID);

                Movie newMovie = new Movie(uniqueID, title, director, plot);
                Log.d(TAG, "Created new Movie object: " + newMovie.toString());

                new AddMovieTask().execute(newMovie);
            }
        });


        getallmoviesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadMoviesTask().execute();
            }
        });

        specificButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int index = Integer.parseInt(inputMovieIndex.getText().toString())-1;
                    if (movieList != null && index >= 0 && index < movieList.size()) {
                        Movie specificMovie = movieList.get(index);
                        textView.setText("You have chosen the movie -> " + specificMovie.getTitle());
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid index", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid input for movie index", e);
                    Toast.makeText(MainActivity.this, "Invalid index", Toast.LENGTH_SHORT).show();
                }
            }
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (movieList != null && !movieList.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(movieList.size());
                    Movie randomMovie = movieList.get(randomIndex);
                    textView.setText("We recommend you this movie: " + randomMovie.getTitle());
                } else {
                    Toast.makeText(MainActivity.this, "No movies available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class LoadMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                return loadMoviesFromApi();
            } catch (Exception e) {
                Log.e(TAG, "Failed to load movies from API", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                movieList = movies;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < movies.size(); i++) {
                    sb.append(i + 1).append(") ").append(movies.get(i)).append("\n");
                }
                textView.setText(sb.toString());
            } else {
                textView.setText("Failed to load movies.");
            }
        }
    }

    private List<Movie> loadMoviesFromApi() throws Exception {
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
    private void refreshMovieList() {
        new LoadMoviesTask().execute();
    }
    private class UpdateMovieTask extends AsyncTask<Movie, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Movie... movies) {
            try {
                return updateMovie(movies[0]);
            } catch (Exception e) {
                Log.e(TAG, "Failed to update movie", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Movie updated successfully", Toast.LENGTH_SHORT).show();
                refreshMovieList();
            } else {
                Toast.makeText(MainActivity.this, "Failed to update movie", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean updateMovie(Movie movie) throws Exception {
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

    private class DeleteMovieTask extends AsyncTask<Movie, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Movie... movies) {
            try {
                return deleteMovie(movies[0]);
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete movie", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Movie deleted successfully", Toast.LENGTH_SHORT).show();
                refreshMovieList();
            } else {
                Toast.makeText(MainActivity.this, "Failed to delete movie", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean deleteMovie(Movie movie) throws Exception {
        URL url = new URL("http://dimcost421.ddns.net:9200/api/v1/movies/" + movie.getId());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("DELETE");

        int responseCode = httpURLConnection.getResponseCode();
        Log.d(TAG, "DELETE Response Code :: " + responseCode);

        httpURLConnection.disconnect();

        return responseCode == HttpURLConnection.HTTP_NO_CONTENT;
    }

    private class AddMovieTask extends AsyncTask<Movie, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Movie... movies) {
            Log.d(TAG, "Adding movie: " + movies[0].toString());
            try {
                addNewMovie(movies[0]);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to add movie", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this, "Movie added successfully", Toast.LENGTH_SHORT).show();
                refreshMovieList();
            } else {
                Toast.makeText(MainActivity.this, "Failed to add movie", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addNewMovie(Movie movie) throws Exception {
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
}
