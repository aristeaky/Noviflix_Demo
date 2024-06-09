package com.example.noviflix_demo;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    ListView listView;
    MyListAdapter adapter;
    Button addMovieBtn,getMovieBtn;
    List<Movie> movieList;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        addMovieBtn = findViewById(R.id.addMovieBtn);
        getMovieBtn = findViewById(R.id.getMovieBtn);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        loadMovies();

        swipeRefreshLayout.setOnRefreshListener(this::loadMovies);

        addMovieBtn.setOnClickListener(v -> {
            showEditDialog(new Movie());
        });
        getMovieBtn.setOnClickListener(v -> {
            getRandomMovie(new Movie());
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
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enableButton = !editTitle.getText().toString().trim().isEmpty();
                saveButton.setEnabled(enableButton);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        editTitle.addTextChangedListener(textWatcher);
        editDirector.addTextChangedListener(textWatcher);
        editPlot.addTextChangedListener(textWatcher);

        saveButton.setOnClickListener(v -> {
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
                            dialog.dismiss();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Failed to update movie " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void getRandomMovie(Movie movie) {

        if (movieList != null && !movieList.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(movieList.size());
            Movie randomMovie = movieList.get(randomIndex);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(randomMovie.getTitle());
            builder.setMessage("Director: " + randomMovie.getDirector() + "\nPlot: " + randomMovie.getPlot());
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();

            ;
        } else {
            Toast.makeText(this, "No movies available", Toast.LENGTH_SHORT).show();
        }
    }

}


