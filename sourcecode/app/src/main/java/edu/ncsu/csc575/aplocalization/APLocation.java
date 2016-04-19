package edu.ncsu.csc575.aplocalization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Bryce on 4/7/2016.
 */
public class APLocation {

    private final int GRID_X = 3;
    private final int GRID_Y = 6;
    //Context for application
    Context context;
    //BSSID of ap that is being located
    private List<String> aps;
    //Array of samples to be analyzed
    private HashMap<String,List<Integer>> rssiSamples;
    //Cell that samples are being taken from
    private Vertex currentCell;
    //Array of Samples already recorded
    private HashMap<String, List<Sample>> measurements;
    //Hashmap of possible locations by Sample
    private HashMap<String, Integer> locations;
    //Receiver for apMan Scans
    private WifiScanReceiver apReciever;
    //Wifi Manager for starting scans and getting results.
    private WifiManager apMan;
    //Boolean to only sample once per button press
    private boolean sample = false;
    //class to locate ap
    private Trilateration tri;


    public APLocation(Context mContext, List<String> apNames){
        this.context = mContext;
        aps = new ArrayList<>();
        setAP(apNames);
        apReciever = new WifiScanReceiver();
        apMan =(WifiManager)context.getSystemService(context.WIFI_SERVICE);
        /**
         * public static final String SCAN_RESULTS_AVAILABLE_ACTION
         * Added in API level
         * An access point scan has completed, and results are available from the supplicant. Call
         * getScanResults() to obtain the results. EXTRA_RESULTS_UPDATED indicates if the scan was
         * completed successfully.
         * Constant Value: "android.net.wifi.SCAN_RESULTS"
         */
        context.registerReceiver(apReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        /**
         * public static final String EXTRA_RESULTS_UPDATED
         * Added in API level 23
         * Lookup key for a boolean representing the result of previous startScan() operation, reported with SCAN_RESULTS_AVAILABLE_ACTION.
         * Constant Value: "resultsUpdated"
         */

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
           // Log.d(this.getClass().toString(), ap);
            this.aps.add(ap);
        }
    }

    public void setSample(boolean sample){
        this.sample = sample;
    }

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

    public HashMap<String, Vertex> changeCell(Vertex cell) {
        HashMap<String,Vertex> locs = new HashMap<>();
        for(String ap : rssiSamples.keySet()) {
            locations = new HashMap<>();
            List<Integer> samples = rssiSamples.get(ap);
            if (samples != null && samples.size() > 0) {
                //find mode of rssi samples or lowest sample
                int rssi = -1000;
                //get min rssi sample
                for (int r : samples) {
                    if (r > rssi) {
                        rssi = r;
                    }
                }
                Sample s = new Sample(rssi, currentCell);
                measurements.get(ap).add(s);
                Sample m[] = measurements.get(ap).toArray(new Sample[measurements.get(ap).size()]);
                for (int i = 0; i < m.length - 1; i++) {
                    for (int j = i + 1; j < m.length; j++) {
                        Vertex[] vs = tri.findIntersections(m[i], m[j]);
                        for (int k = 0; k < vs.length; k++) {
                            if (vs[k] != null) {
                                int x = (int) (vs[k].getX() / s.CELL_SIZE);
                                int y = (int) (vs[k].getY() / s.CELL_SIZE);
                                boolean xInGrid = (x > 0 && x <= GRID_X);
                                boolean yInGrid = (y > 0 && y <= GRID_Y);
                                if(xInGrid && yInGrid){
                                    String vertex = (x) + " " + (y);
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
            if (locations != null && locations.size() > 0) {
                Set<String> keys = locations.keySet();

                String mostLikelyLocation = "";
                int max = 0;
                for (String k : keys) {
                    if (locations.get(k) > max) {
                        mostLikelyLocation = k;
                        max = locations.get(k);
                    }
                    Log.d(this.getClass().toString(), k + " " + locations.get(k));
                }
                Toast toast = Toast.makeText(context, "Most likely Location " + (mostLikelyLocation), Toast.LENGTH_SHORT);
                toast.show();
                Log.d(this.getClass().toString(), "Most likely Location " + mostLikelyLocation);

                locs.put(ap, new Vertex(Double.valueOf(mostLikelyLocation.split(" ")[0]), Double.valueOf(mostLikelyLocation.split(" ")[1])));
            }
        }
        currentCell = cell;
        //reset rssiSamples
        resetRSSISamples();
        //Return possible locations.
        return locs;
    }

    private void storeSample(List<ScanResult> wifis){
        Toast toast = Toast.makeText(context, aps + "  Sample Taken #" + (rssiSamples.size() + 1), Toast.LENGTH_SHORT);
        toast.show();
        if(wifis != null && wifis.size() > 0){
            Log.d(this.getClass().toString(), wifis.toString());
            Iterator<ScanResult> wifIter = wifis.iterator();
            while(wifIter.hasNext()){
                ScanResult s = wifIter.next();
                if(aps != null && aps.contains(s.BSSID)){
                    Log.d(this.getClass().toString(), s.level + "");
                    rssiSamples.get(s.BSSID).add(s.level);
                }
            }
        }else{
            Log.d(this.getClass().toString(), "wifis is null or has length 0");
        }
    }

    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            if (apMan != null) {
                List<ScanResult> wifiScanList = apMan.getScanResults();

                String data = "";
                if (wifiScanList.size() > 0) {
                    Log.d(this.getClass().toString(), "Scan results");
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

    private void resetRSSISamples(){
        for(String ap : aps){
            rssiSamples.put(ap, new ArrayList<Integer>());
        }
    }
}