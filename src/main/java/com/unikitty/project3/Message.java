package com.unikitty.project3;

// Generic message, stores a type and some data
public class Message {
    
    private String type;
    private int id;
    private Object data;
    
    public Message() {}
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
