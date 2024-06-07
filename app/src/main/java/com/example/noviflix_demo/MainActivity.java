package com.example.noviflix_demo;

import java.util.UUID;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        loadMovies();

        swipeRefreshLayout.setOnRefreshListener(this::loadMovies);
    }

    public void loadMovies() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Movie> movies = new MovieHandler().loadMoviesFromApi();
                runOnUiThread(() -> {
                    if (movies != null && !movies.isEmpty()) {
                        adapter = new MyListAdapter(MainActivity.this, movies);
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


}
