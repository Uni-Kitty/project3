package com.unikitty.project3;

/**
 * This is a simple example of an object that can be mapped from a POJO (Plain old java object)
 * to JSON with Jackson. Instance variables (xPos, yPos) need getters and setters, so Jackson can do it's thing.
 * See Main.java for Jackson examples.
 */
public class Model {
    
    private int xPos;
    private int yPos;
    
    public Model() {}
    
    public Model (int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }
    
    public boolean equals(Model m) {
        return xPos == m.xPos && yPos == m.yPos;
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
