package edu.ncsu.csc575.aplocalization;

import android.util.Log;

/**
 * This class handles the trilateration algorithm.
 */
public class Trilateration {

    private Vertex[] intersections;

    public Vertex[] findIntersections(Sample s1, Sample s2){
        Vertex intersections[] = new Vertex[2];
        double dx = s2.center.x - s1.center.x;
        double dy = s2.center.y - s1.center.y;

        double d = Math.sqrt(dx*dx+dy*dy);

        if(d > s1.distance + s2.distance){
            Log.d(this.getClass().toString(),"Circles aren't touching");
            return intersections;
        }
        if(d < Math.abs(s1.distance - s2.distance)){
            Log.d(this.getClass().toString(),"Circles are inside one another");
            return intersections;
        }

        if(d == 0 && s1.distance == s2.distance){
            Log.d(this.getClass().toString(),"Circles are infinitely touching");
            return intersections;
        }

        double a = (s1.distance *s1.distance -s2.distance *s2.distance +d*d)/(2*d);
        double h = Math.sqrt(s1.distance * s1.distance - a *a);
        double xm = s1.center.x + a*dx/d;
        double ym = s1.center.y + a*dy/d;
        double xs1 = xm + h*dy/d;
        double xs2 = xm - h*dy/d;
        double ys1 = ym - h*dx/d;
        double ys2 = ym + h*dx/d;

        //Need to handle grid scaling here
        //Output grid coordinates to highlight.
        intersections[0] = new Vertex(xs1, ys1);
        intersections[1] = new Vertex(xs2, ys2);

        return intersections;
    }

}


