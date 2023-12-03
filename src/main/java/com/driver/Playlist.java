package com.driver;

import java.util.List;

public class Playlist {
    private String title;

    public Playlist(){

    }

    public Playlist(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "title='" + title + '\'' +
                '}';
    }
}
