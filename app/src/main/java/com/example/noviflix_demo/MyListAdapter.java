package com.example.noviflix_demo;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executors;

public class MyListAdapter extends ArrayAdapter<Movie> {
    private List<Movie> movieList;
    private Activity activity;
    private MovieHandler movieHandler;

    public MyListAdapter(Activity activity, List<Movie> movieList) {
        super(activity, R.layout.list_item, movieList);
        this.activity = activity;
        this.movieList = movieList;
        this.movieHandler = new MovieHandler();
    }

    static class ViewHolder {
        TextView titleText;
        TextView director;
        TextView plot;
        Button editBtn;
        Button deleteBtn;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            holder = new ViewHolder();
            holder.titleText = convertView.findViewById(R.id.title);
            holder.director = convertView.findViewById(R.id.director);
            holder.plot = convertView.findViewById(R.id.plot);
            holder.editBtn = convertView.findViewById(R.id.editBtn);
            holder.deleteBtn = convertView.findViewById(R.id.deleteBtn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movie movie = movieList.get(position);
        holder.titleText.setText(movie.getTitle());
        holder.director.setText(movie.getDirector());
        holder.plot.setText(movie.getPlot());
        Log.d("MyListAdapter", "Director: " + movie.getDirector());

        convertView.setOnLongClickListener(v -> {
            showEditDialog(movie);
            return false;
        });

        holder.deleteBtn.setOnClickListener(v -> {
           deleteDialog(movie);
        });

        holder.editBtn.setOnClickListener(v -> {
            showEditDialog(movie);
        });

        return convertView;
    }

    private void showEditDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_movie, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editDirector = dialogView.findViewById(R.id.editDirector);
        EditText editPlot = dialogView.findViewById(R.id.editPlot);

        String initialTitle = movie.getTitle();
        String initialDirector = movie.getDirector();
        String initialPlot = movie.getPlot();

        editTitle.setText(initialTitle);
        editDirector.setText(initialDirector);
        editPlot.setText(initialPlot);

        builder.setTitle("Edit Movie")
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
                boolean isChanged = !editTitle.getText().toString().equals(initialTitle) ||
                        !editDirector.getText().toString().equals(initialDirector) ||
                        !editPlot.getText().toString().equals(initialPlot);
                saveButton.setEnabled(isChanged);
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
                    if (movieHandler.updateMovie(movie)) {
                        activity.runOnUiThread(() -> {
                            notifyDataSetChanged();
                            Toast.makeText(activity, "Movie updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }
                } catch (Exception e) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Failed to update movie " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void deleteDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_movie, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editDirector = dialogView.findViewById(R.id.editDirector);
        EditText editPlot = dialogView.findViewById(R.id.editPlot);

        editTitle.setText(movie.getTitle());
        editDirector.setText(movie.getDirector());
        editPlot.setText(movie.getPlot());

        editTitle.setEnabled(false);
        editDirector.setEnabled(false);
        editPlot.setEnabled(false);

        builder.setTitle("Delete Movie")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new AlertDialog.Builder(activity)
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure you want to delete this movie?")
                            .setPositiveButton("Yes", (confirmDialog, confirmWhich) -> {
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    try {
                                        if (movieHandler.deleteMovie(movie)) {
                                            activity.runOnUiThread(() -> {
                                                remove(movie);
                                                notifyDataSetChanged();
                                                Toast.makeText(activity, "Successfully deleted", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    } catch (Exception e) {
                                        activity.runOnUiThread(() -> {
                                            Toast.makeText(activity, "Failed to delete " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                            })
                            .setNegativeButton("No", (confirmDialog, confirmWhich) -> confirmDialog.dismiss())
                            .show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
