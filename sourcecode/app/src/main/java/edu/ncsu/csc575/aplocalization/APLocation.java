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

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(apReciever);
    }


    /* Get the approximate location of the ap by new samples
     * and previous measurements.
     */
    public Vertex getSample(){
        apMan.startScan();
        return null;
    }

    public void changeCell(Vertex cell){
        if(rssiSamples != null && rssiSamples.size() > 0){
            //find mode of rssi samples or lowest sample

            //reset rssiSamples
            //rssiSamples = new ArrayList<>();
            //calculate distance and create
            /*Sample s = new Sample(rssi,cell);
            measurements.add(s);
            Sample m[] = (Sample[]) measurements.toArray();
            for(int i = 0; i < m.length - 1; i++){
                for(int j = i + 1; j < m.length; j++){
                    Vertex[] vs = tri.findIntersections(m[i],m[j]);
                    for(int k = 0; k < vs.length; k++) {
                        if (!locations.containsKey(vs[k].toString())) {
                            locations.put(vs[k].toString(), 1);
                        } else {
                            locations.put(vs[k].toString(), locations.get(vs[k].toString()) + 1);
                        }
                    }
                }
            }*/
        }
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