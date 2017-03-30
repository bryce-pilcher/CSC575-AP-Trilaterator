package edu.ncsu.csc575.aplocalization;

import android.util.Log;

/**
 * This class holds sample information which will be used to find the intersections
 * of samples and thereby the location of any APs.
 */
public class Sample {

    int rssi;
    double distance;
    Vertex center;
    public final int CELL_SIZE = 1;

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

    /**
     * This constructor also populates the distance automagically
     * @param r the rssi level measured
     * @param v the vertex corresponding to the center of the cell
     */
    public Sample(int r, Vertex v){
        this.center = new Vertex(v.getX() * CELL_SIZE, v.getY() * CELL_SIZE);
        this.rssi = r;
        this.distance = calPathLoss(r);
    }


    /**
     * Calculates distance based on measured RSSI for an indoor model
     * @param rssi the RSSI level measured in dBm
     * @return the distance in meteres the AP is from the cell
     */
    private double calPathLoss(int rssi){
        double dist = Math.pow(10,-((rssi+25.57)/31.87));
        Log.d(this.getClass().toString(), center.getX() + " " + center.getY() + " Distance: " + dist);
        return dist;
    }
}
