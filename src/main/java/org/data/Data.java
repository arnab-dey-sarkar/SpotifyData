package org.data;

public class Data {
    private String singer;
    private String song;

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    @Override
    public String toString() {
        return "Data{" +
                "singer='" + singer + '\'' +
                ", song='" + song + '\'' +
                '}';
    }

    public Data(String singer, String song) {
        this.singer = singer;
        this.song = song;
    }
}
