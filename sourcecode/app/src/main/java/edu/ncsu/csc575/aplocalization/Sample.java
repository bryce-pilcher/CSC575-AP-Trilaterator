package edu.ncsu.csc575.aplocalization;

/**
 * Created by Bryce on 4/6/2016.
 */
public class Sample {

    int r;
    Vertex center;

    public Sample(int rssi, int x, int y){
        this.r = rssi;
        this.center = new Vertex(x,y);
    }

    public Sample(int rssi, Vertex v){
        this.center = v;
        this.r = rssi;
    }

    private int calPathLoss(int rssi){
        //Do path loss calculation to determine distance
        return 0;
    }
}
