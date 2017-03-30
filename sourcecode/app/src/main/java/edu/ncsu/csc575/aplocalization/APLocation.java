package edu.ncsu.csc575.aplocalization;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class handles all of the logic for locating an AP.  It has a method that is triggered
 * by the scan button press to perform the scanning and store the samples.  It has another method
 * that handles changing cells and calculating the real time location of any AP being located.
 */
public class APLocation {

    //The size of the Grid in x and y coordinates
    //Used for making sure intersections fall in measurable space
    private final int GRID_X = 6;
    private final int GRID_Y = 6;

    //Context and activity for application
    //Used to display messages or change the gui
    Context context;
    Activity activity;

    //List of BSSID (MAC Addresses) of aps that are being located
    private List<String> aps;

    //Hashmap of samples to be analyzed
    //Key: BSSID of ap
    //Value: list of RSSI levels
    private HashMap<String,List<Integer>> rssiSamples;

    //Hashmap of Samples already recorded
    //Key: BSSID of AP
    //Value: list of Samples
    private HashMap<String, List<Sample>> measurements;

    //Hashmap of possible locations by Sample
    //Key: String represenation of x, y coordinates of cell
    //Value: number of votes
    private HashMap<String, Integer> locations;

    //Receiver for apMan Scans
    private WifiScanReceiver apReciever;
    //Wifi Manager for starting scans and getting results.
    private WifiManager apMan;
    //Boolean to only sample once per button press
    private boolean sample = false;
    //class to locate ap
    private Trilateration tri;

    //Current cell for which calculation is being computed
    private Cell currentCell;


    /**
     * Constructor which initializes values and registers receivers
     * @param mContext LocateActivity context for registering receiver
     * @param mActivity LocateActivity activity for changing gui and toasts
     * @param apNames List of AP names to locate
     */
    public APLocation(Context mContext, Activity mActivity, List<String> apNames){
        this.context = mContext;
        this.activity = mActivity;
        aps = new ArrayList<>();
        setAP(apNames);
        apReciever = new WifiScanReceiver();
        //Getting wifi context to scan and get results
        apMan =(WifiManager)context.getSystemService(context.WIFI_SERVICE);
        currentCell = null;
        //Register receiver for wifi scan results
        context.registerReceiver(apReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        rssiSamples = new HashMap<>();
        resetRSSISamples();
        measurements = new HashMap<>();
        for(String ap : aps){
            measurements.put(ap, new LinkedList<Sample>());
        }
        tri = new Trilateration();
        locations = new HashMap<>();
    }

    public void setAP(List<String> aps){
        for(String ap : aps){
            this.aps.add(ap);
        }
    }

    public void setSample(boolean sample){
        this.sample = sample;
    }

    /**
     * This method is used by the lifecycle management in LocateActivity to
     * unregister the receiver when it into certain states.
     * @param mContext
     */
    public void unregisterReceiver(Context mContext) {
        Log.d(this.getClass().toString(), "Unregistering apReceiver");
        context.unregisterReceiver(apReciever);
    }


    /* Get the approximate location of the ap by new samples
     * and previous measurements.
     */
    public Vertex getSample(){
        apMan.startScan();
        return null;
    }

    /**
     * The method handles the logic of finding the max RSSI, feeding the location
     * class with samples, and figuring out which location is the best.
     * @param cell the cell that has just been switched to, which will be calculated for next call
     * @return A mapping of AP BSSIDs to the most likely location.
     */
    public HashMap<String, Vertex> changeCell(Cell cell) {
        HashMap<String,Vertex> locs = new HashMap<>();
        //Each AP's results need to be processed separately.
        for(String ap : rssiSamples.keySet()) {
            locations = new HashMap<>();
            //Get the samples that correspond to current ap
            List<Integer> samples = rssiSamples.get(ap);


            if (samples != null && samples.size() > 0) {
                int rssi = -1000;
                //Find the max rssi sample (the one that is least negative)
                for (int r : samples) {
                    if (r > rssi) {
                        rssi = r;
                    }
                }
                //Use that max RSSI to create a new sample
                Sample s = new Sample(rssi, currentCell.getCenter());
                //This adds the distance calculated via path loss to the cell to be displayed in the
                //gui :)
                currentCell.addDistToAPs(s.distance);
                measurements.get(ap).add(s);

                Sample m[] = measurements.get(ap).toArray(new Sample[measurements.get(ap).size()]);
                //These two for-loops run through the array of samples and
                //submit each pair to the trilateration findIntersections function
                for (int i = 0; i < m.length - 1; i++) {
                    for (int j = i + 1; j < m.length; j++) {
                        //Send each pair to findIntersections
                        Vertex[] vs = tri.findIntersections(m[i], m[j]);
                        //For the Intersections returned, check them and store them
                        for (int k = 0; k < vs.length; k++) {
                            if (vs[k] != null) {
                                //Convert the location to an x and y coordinate of cell
                                int x = (int) (vs[k].getX() / s.CELL_SIZE);
                                int y = (int) (vs[k].getY() / s.CELL_SIZE);
                                //Check to make sure they are in the grid.
                                boolean xInGrid = (x > 0 && x <= GRID_X);
                                boolean yInGrid = (y > 0 && y <= GRID_Y);
                                //If they are both in the grid, store in hashmap as possible location
                                if(xInGrid && yInGrid){
                                    String vertex = (x) + " " + (y);
                                    //If the location has not been seen before, initialize it.
                                    if (!locations.containsKey(vertex)) {
                                        locations.put(vertex, 1);
                                    } else {
                                        locations.put(vertex, locations.get(vertex) + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //If we have some possible locations, we need to figure out which
            //location has the most votes.
            if (locations != null && locations.size() > 0) {
                Set<String> keys = locations.keySet();

                String mostLikelyLocation = "";
                int max = 0;
                //Grab all the keys and use them to iterate through the hashmap
                for (String k : keys) {
                    //find the location with the max votes
                    if (locations.get(k) > max) {
                        mostLikelyLocation = k;
                        max = locations.get(k);
                    }
                    Log.d(this.getClass().toString(), k + " " + locations.get(k));
                }
                Toast toast = Toast.makeText(context, "Most likely Location " + (mostLikelyLocation), Toast.LENGTH_SHORT);
                toast.show();
                Log.d(this.getClass().toString(), "Most likely Location " + mostLikelyLocation);

                //put the location in a Hashmap keyed by the corresponding AP
                locs.put(ap, new Vertex(Double.valueOf(mostLikelyLocation.split(" ")[0]), Double.valueOf(mostLikelyLocation.split(" ")[1])));
            }
        }
        if(currentCell != null){
            TextView tv = (TextView) activity.findViewById(currentCell.getId());
            tv.setText(currentCell.getDistToAPs());
        }
        currentCell = cell;
        //reset rssiSamples
        resetRSSISamples();
        //Return possible locations.
        return locs;
    }

    /**
     * This method stores RSSI value that we want to keep.  This is necessary because the wifi scan
     * sometimes returns extra results that we want to ignore.
     * @param wifis list of scan results.
     */
    private void storeSample(List<ScanResult> wifis){
        Toast toast = Toast.makeText(context, aps + "  Sample Taken #" + (rssiSamples.get(aps.get(0)).size() + 1), Toast.LENGTH_SHORT);
        toast.show();
        if(wifis != null && wifis.size() > 0){
            Log.d(this.getClass().toString(), wifis.toString());
            Iterator<ScanResult> wifIter = wifis.iterator();
            while(wifIter.hasNext()){
                ScanResult s = wifIter.next();
                //If the ap is the one we are looking for, save the RSSI value to the
                //hashmap keyed by BSSID.
                if(aps != null && aps.contains(s.BSSID)){
                    Log.d(this.getClass().toString(), s.level + "");
                    rssiSamples.get(s.BSSID).add(s.level);
                }
            }
        }else{
            Log.d(this.getClass().toString(), "wifis is null or has length 0");
        }
    }

    /**
     * This class receives the intent from the wifi scan when it returns.
     * and handles storing samples if we want to collect one
     */
    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            if (apMan != null) {
                List<ScanResult> wifiScanList = apMan.getScanResults();

                String data = "";
                if (wifiScanList.size() > 0) {
                    Log.d(this.getClass().toString(), "Scan results");
                    //If this is a sample we want to store, store it
                    if (sample) {
                        sample = false;
                        storeSample(wifiScanList);
                    }
                } else {
                    Log.d(this.getClass().toString(), "no scan results");
                    data = wifiScanList.size() + "";
                }
            }
            Log.d(this.getClass().toString(), "Received results.");
        }
    }

    /**
     * Resets the RSSI Samples Hashmap for use within a new cell.
     */
    private void resetRSSISamples(){
        for(String ap : aps){
            rssiSamples.put(ap, new ArrayList<Integer>());
        }
    }
}