package com.example.noviflix_demo;

public class Movie {

   private final String id;
    private String title;
    private String director;
    private String plot;
    public Movie(String id, String title, String director, String plot) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.plot = plot;
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    @Override
    public String toString() {
        return "Movie: "  +
        " title='" + title + "'\n" +
                " director='" + director + "'\n" +
                " plot='" + plot + "'\n"
                ;
    }

}