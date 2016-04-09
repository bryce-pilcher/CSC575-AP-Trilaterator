package edu.ncsu.csc575.aplocalization;

/**
 * Created by Bryce on 4/6/2016.
 */
public class Vertex {
    double x;
    double y;

    public Vertex(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Vertex(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString(){
        return x + " " + y;
    }
}
