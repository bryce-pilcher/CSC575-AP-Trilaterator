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

    private final int numOfSamples = 5;
    //Context for application
    Context context;
    //BSSID of ap that is being located
    private String ap;
    //Array of samples to be analyzed
    private List<Integer> rssiSamples;
    //Cell that samples are being taken from
    private Vertex currentCell;
    //Array of Samples already recorded
    private List<Sample> measurements;
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


    public APLocation(Context mContext, String apName){
        this.context = mContext;
        this.ap = apName;
        apReciever = new WifiScanReceiver();
        apMan =(WifiManager)context.getSystemService(context.WIFI_SERVICE);
        context.registerReceiver(apReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        rssiSamples = new ArrayList<>();
        measurements = new LinkedList<>();
        tri = new Trilateration();
        locations = new HashMap<>();
    }

    public void setAP(String ap){
        this.ap = ap;
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

    public Vertex changeCell(Vertex cell) {
        locations = new HashMap<>();
        if (rssiSamples != null && rssiSamples.size() > 0) {
            //find mode of rssi samples or lowest sample
            int rssi = 1000;
            //get min rssi sample
            for (int r : rssiSamples) {
                if (r < rssi) {
                    rssi = r;
                }
            }
            Sample s = new Sample(rssi, currentCell);
            //reset rssiSamples
            rssiSamples = new ArrayList<>();
            currentCell = cell;
            measurements.add(s);
            Sample m[] = measurements.toArray(new Sample[measurements.size()]);
            for (int i = 0; i < m.length - 1; i++) {
                for (int j = i + 1; j < m.length; j++) {
                    Vertex[] vs = tri.findIntersections(m[i], m[j]);
                    for (int k = 0; k < vs.length; k++) {
                        if (vs[k] != null) {
                            String vertex = (int)(vs[k].getX()/s.CELL_SIZE) + " " + (int)(vs[k].getY()/s.CELL_SIZE);
                            if (!locations.containsKey(vertex)) {
                                locations.put(vertex, 1);
                            } else {
                                locations.put(vertex, locations.get(vertex) + 1);
                            }
                        }
                    }
                }
            }
        }else{
            currentCell = cell;
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
                Log.d(this.getClass().toString(),k + " " + locations.get(k));
            }
            Toast toast = Toast.makeText(context, "Most likely Location " + (mostLikelyLocation), Toast.LENGTH_SHORT);
            toast.show();
            Log.d(this.getClass().toString(), "Most likely Location " + mostLikelyLocation);
            return new Vertex(Double.valueOf(mostLikelyLocation.split(" ")[0]),Double.valueOf(mostLikelyLocation.split(" ")[1]));
        }
        return null;
    }

    private void storeSample(List<ScanResult> wifis){
        Toast toast = Toast.makeText(context, ap + "  Sample Taken #" + (rssiSamples.size() + 1), Toast.LENGTH_SHORT);
        toast.show();
        if(wifis != null && wifis.size() > 0){
            Log.d(this.getClass().toString(), wifis.toString());
            Iterator<ScanResult> wifIter = wifis.iterator();
            while(wifIter.hasNext()){
                ScanResult s = wifIter.next();
                if(ap != null && s.BSSID.contentEquals(ap)){
                    Log.d(this.getClass().toString(), s.level + "");
                    rssiSamples.add(s.level);
                }
            }
        }else{
            Log.d(this.getClass().toString(), "wifis is null or has length 0");
        }
    }

    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            Log.d(this.getClass().toString(), "Received results.");
            if(apMan == null){
                Log.d(this.getClass().toString(),"apMan null");
            }
            List<ScanResult> wifiScanList = apMan.getScanResults();
            String data = "";
            if(wifiScanList.size() > 0){
                Log.d(this.getClass().toString(), "Scan results");
                if(sample) {
                    sample = false;
                    storeSample(wifiScanList);
                }
            } else{
                Log.d(this.getClass().toString(), "no scan results");
                data = wifiScanList.size() + "";
            }
        }
    }
}