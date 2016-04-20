package edu.ncsu.csc575.aplocalization;

import android.util.Log;

/**
 * Class for Cells in grid.  Tracks state, coordinate, and id of related resource
 *
 * Created by Bryce
 */
public class Cell {

    public final int NOT_SCANNED = 0;
    public final int SCANNED = 1;

    private int state;
    private Vertex center;
    private int id;

    public Cell(int resourceID, Vertex coordinate){
        this.id = resourceID;
        this.center = coordinate;
        this.state = NOT_SCANNED;
    }

    public int getState(){
        return this.state;
    }

    public void setState(int newState){
        Log.d(this.getClass().toString(), "New State set : " + newState);
        this.state = newState;
    }

    public int getId(){
        return this.id;
    }

    public Vertex getCenter(){
        return this.center;
    }


}
