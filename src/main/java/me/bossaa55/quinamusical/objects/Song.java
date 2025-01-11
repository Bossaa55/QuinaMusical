package me.bossaa55.quinamusical.objects;

public class Song {
    private String name;
    private int start;

    public Song(String name, int start){
        this.name=name; this.start=start;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
