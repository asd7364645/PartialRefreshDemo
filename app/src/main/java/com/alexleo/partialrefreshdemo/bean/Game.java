package com.alexleo.partialrefreshdemo.bean;

/**
 * Created by Alex on 2017/1/17.
 * Alex
 */

public class Game {

    private String gameName;
    private long gameTotalSize;
    private long gameDownSize;
    private String gameUrl;
    private int state;

    public Game(String gameName, long gameTotalSize) {
        this.gameName = gameName;
        this.gameTotalSize = gameTotalSize;
        gameUrl = "http://dynamic-image.yesky.com/600x-/uploadImages/upload/20140912/upload/201409/smkxwzdt1n1jpg.jpg";
        this.gameDownSize = 0;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public String getGameName() {
        return gameName;
    }

    public long getGameTotalSize() {
        return gameTotalSize;
    }

    public long getGameDownSize() {
        return gameDownSize;
    }

    public void setGameDownSize(long gameDownSize) {
        this.gameDownSize = gameDownSize;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
