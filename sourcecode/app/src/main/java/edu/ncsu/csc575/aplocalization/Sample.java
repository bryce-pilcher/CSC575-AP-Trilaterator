package edu.ncsu.csc575.aplocalization;

import android.util.Log;

/**
 * Created by Bryce on 4/6/2016.
 */
public class Sample {

    int rssi;
    double distance;
    Vertex center;
    public final int CELL_SIZE = 2;

    /**
     * This constructor populates the distance automagically
     * @param r rssi value measured from the AP.
     * @param x the x value of the center of the grid the sample was taken from
     * @param y the y value of the center of the grid the sample was taken from
     */
    public Sample(int r, int x, int y){
        this.rssi = r;
        this.distance = calPathLoss(r);
        this.center = new Vertex(x * CELL_SIZE,y * CELL_SIZE);
    }

    public Sample(int r, Vertex v){
        this.center = new Vertex(v.getX() * CELL_SIZE, v.getY() * CELL_SIZE);
        this.rssi = r;
        this.distance = calPathLoss(r);
    }

    private double calPathLoss(int rssi){
        double dist = Math.pow(10,-((rssi+25.57)/31.87));
        Log.d(this.getClass().toString(), center.getX() + " " + center.getY() + " Distance: " + dist);
        return dist;
    }
}
