package me.bossaa55.quinamusical.objects;

import javafx.scene.image.Image;

import java.io.File;

public class Song {
    private String title;
    private String author="";
    private String album="";
    private Image cover;
    private final int start;
    private File file;
    private boolean played=false;

    public Song(File file, int start){
        this.file = file; this.start=start;
        fetchSongMetadata();
    }

    public Song(String title, int start){
        this.title = title; this.start=start;
    }

    private void fetchSongMetadata(){
        String[] data = MusicMetadata.getSongData(file.getAbsolutePath());
        if(data!=null){
            setTitle(data[0]);
            setAuthor(data[1]);
            setAlbum(data[2]);
        }else{
            setTitle(file.getName().substring(0,file.getName().lastIndexOf(".")));
        }
        setCover(MusicMetadata.getCoverArt(file.getAbsolutePath()));
    }

    public String getTitle() {
        return title;
    }

    public int getStart() {
        return start;
    }

    public File getFile() {
        return file;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Image getCover() {
        return cover;
    }

    public void setCover(Image cover) {
        this.cover = cover;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
