package com.unikitty.project3;

// a message to be displayed somewhere on the screen, for a duration

public class TextDisplay {
    
    private String text;
    private int duration;
    private int xPos;
    private int yPos;
    
    public TextDisplay() {}
    
    public TextDisplay(String text, int duration, int x, int y) {
        this.text = text;
        this.duration = duration;
        this.xPos = x;
        this.yPos = y;
    }
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public int getxPos() {
        return xPos;
    }
    public void setxPos(int xPos) {
        this.xPos = xPos;
    }
    public int getyPos() {
        return yPos;
    }
    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

}
