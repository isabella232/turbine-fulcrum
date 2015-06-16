package org.apache.fulcrum.json;

public final class Rectangle {
    private int w, h;
    private String name;
 
    public Rectangle() {
        // may be this is needed for deserialization, if not set otherwise
    }
    
    public Rectangle(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public Rectangle(int w, int h, String name) {
        this.w = w;
        this.h = h;
        this.name = name;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getSize() {
        return w * h;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}