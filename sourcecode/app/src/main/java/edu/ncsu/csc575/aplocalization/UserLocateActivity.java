package edu.ncsu.csc575.aplocalization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class UserLocateActivity extends AppCompatActivity {



    String wifis[];
    //Receiver for wifi Scans
    private WifiScanReceiver wifiReciever;
    //Wifi Manager for starting scans and getting results.
    private WifiManager wifi;
    //Locations of APs to locate user from
    HashMap<String,Vertex> apLocs;
    //Class for trilateration
    Trilateration tri;
    //Hashmap of possible locations by Sample
    private HashMap<String, Integer> locations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_locate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        tri = new Trilateration();
        locations = new HashMap<>();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locateUser();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void locateUser(){
        wifi.startScan();
    }

    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            Log.d(this.getClass().toString(), "Received results.");
            if(wifi == null){
                Log.d(this.getClass().toString(),"apMan null");
            }
            List<ScanResult> wifiScanList = wifi.getScanResults();
            String data = "";
            if(wifiScanList.size() > 0){
                Log.d(this.getClass().toString(), "Scan results");
            } else{
                Log.d(this.getClass().toString(), "no scan results");
                data = wifiScanList.size() + "";
            }
            //TODO: Populate apLocs from previous phase
            List<Sample> samples = new ArrayList<>();
            for(ScanResult sr : wifiScanList){
                if(apLocs.containsKey(sr.BSSID))
                    samples.add(new Sample(sr.level,apLocs.get(sr.BSSID)));
            }

            locate(samples);
        }
    }

    private void locate(List<Sample> samples){
        if(samples != null && samples.size() > 0){
            Sample s = samples.get(0);
            Sample m[] = samples.toArray(new Sample[samples.size()]);
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
            Toast toast = Toast.makeText(this, "Most likely Location " + (mostLikelyLocation), Toast.LENGTH_SHORT);
            toast.show();
            Log.d(this.getClass().toString(), "Most likely Location " + mostLikelyLocation);
            //TODO: highlight the grid the user is in.
        }
    }

}
