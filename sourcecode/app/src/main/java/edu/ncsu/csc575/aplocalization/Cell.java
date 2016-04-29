package edu.ncsu.csc575.aplocalization;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for Cells in grid.  Tracks state, coordinate, and id of related resource
 *
 */
public class Cell {

    //State finals for indicating whether a cell has been scanned or not.
    public final int NOT_SCANNED = 0;
    public final int SCANNED = 1;

    //State of the cell
    private int state;
    //Vertex for center of the cell
    private Vertex center;
    //The TextID of the xml element corresponding to this cell
    private int id;
    //List of distances calculated to each AP
    private List<Double> distToAPs;

    /**
     * Constructor for cell class
     * @param resourceID the TextID of the xml element that this cell corresponds to
     * @param coordinate the center of the cell
     */
    public Cell(int resourceID, Vertex coordinate){
        this.id = resourceID;
        this.center = coordinate;
        this.state = NOT_SCANNED;
        this.distToAPs = new ArrayList<>();
    }

    public int getState(){
        return this.state;
    }

    public void setState(int newState){
        this.state = newState;
    }

    public int getId(){
        return this.id;
    }

    public String getDistToAPs(){
        String dist = " ";
        //Build a string representation of the list and shorten the number of significant digits
        for(Double d : distToAPs){
            dist += ("" + d).substring(0,4) + "\n";
        }
        return dist;
    }

    public void addDistToAPs(Double dist){
        this.distToAPs.add(dist);
    }

    public Vertex getCenter(){
        return this.center;
    }


}
