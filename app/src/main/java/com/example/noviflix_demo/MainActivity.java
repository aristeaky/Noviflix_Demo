package com.example.noviflix_demo;

import java.util.UUID;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    ListView listView;
    MyListAdapter adapter;
    Button addMovieBtn;
    List<Movie> movieList;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        addMovieBtn = findViewById(R.id.addMovieBtn);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        loadMovies();

        swipeRefreshLayout.setOnRefreshListener(this::loadMovies);

        addMovieBtn.setOnClickListener(v -> {
            showEditDialog(new Movie());
        });
    }

    public void loadMovies() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Movie> movies = new MovieHandler().loadMoviesFromApi();
                runOnUiThread(() -> {
                    if (movies != null && !movies.isEmpty()) {
                        movieList = movies;
                        adapter = new MyListAdapter(MainActivity.this, movieList);
                        listView.setAdapter(adapter);
                        Toast.makeText(MainActivity.this, "Movies loaded: " + movies.size(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "No movies found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to load movies", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        });
        runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showEditDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_movie, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editDirector = dialogView.findViewById(R.id.editDirector);
        EditText editPlot = dialogView.findViewById(R.id.editPlot);

        editTitle.setText(movie.getTitle());
        editDirector.setText(movie.getDirector());
        editPlot.setText(movie.getPlot());

        builder.setTitle("Add Movie")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Handle saving the edited movie
                    movie.setTitle(editTitle.getText().toString());
                    movie.setDirector(editDirector.getText().toString());
                    movie.setPlot(editPlot.getText().toString());

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            if (new MovieHandler().addNewMovie(movie)) {
                                runOnUiThread(() -> {
                                    movieList.add(movie);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(MainActivity.this, "Movie updated", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Failed to update movie " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
