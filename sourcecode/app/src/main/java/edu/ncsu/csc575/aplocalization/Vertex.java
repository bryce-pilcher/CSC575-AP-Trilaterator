package edu.ncsu.csc575.aplocalization;

/**
 * Class to be used as a holder for x and y coordinates
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
