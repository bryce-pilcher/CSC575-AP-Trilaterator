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
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class UserLocateActivity extends AppCompatActivity {


    private final int GRID_X = 3;
    private final int GRID_Y = 6;
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

    Cell[] cells;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_locate);

        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        tri = new Trilateration();
        locations = new HashMap<>();
        apLocs = new HashMap<>();
        Intent intent = getIntent();
        String apLocations = intent.getStringExtra(LocateActivity.APS);
        Log.d(this.getClass().toString(), apLocations);
        for(String ap : apLocations.split(",")){
            Log.d(this.getClass().toString(), ap);
            String vertex = ap.split(">")[1];
            double x = Double.parseDouble(vertex.split(" ")[0]);
            double y = Double.parseDouble(vertex.split(" ")[1]);
            apLocs.put(ap.split(">")[0],new Vertex(x,y));
        }

        cells = new Cell[]{new Cell(R.id.c11, new Vertex(1,1)), new Cell(R.id.c12, new Vertex(2,1)),
                new Cell(R.id.c13, new Vertex(3,1)), new Cell(R.id.c21, new Vertex(1,2)),
                new Cell(R.id.c22, new Vertex(2,2)), new Cell(R.id.c23, new Vertex(3,2)),
                new Cell(R.id.c31, new Vertex(1,3)), new Cell(R.id.c32, new Vertex(2,3)),
                new Cell(R.id.c33, new Vertex(3,3)), new Cell(R.id.c41, new Vertex(1,4)),
                new Cell(R.id.c42, new Vertex(2,4)), new Cell(R.id.c43, new Vertex(3,4)),
                new Cell(R.id.c51, new Vertex(1,5)), new Cell(R.id.c52, new Vertex(2,5)),
                new Cell(R.id.c53, new Vertex(3,5)), new Cell(R.id.c61, new Vertex(1,6)),
                new Cell(R.id.c62, new Vertex(2,6)), new Cell(R.id.c63, new Vertex(3,6))};


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locateUser(view);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void locateUser(View v){
        ViewGroup vg = (ViewGroup)v.getParent().getParent();
        for(int i=0; i < vg.getChildCount(); i++) {
            View nextChild = vg.getChildAt(i);
            for (int j = 0; j < ((ViewGroup) nextChild).getChildCount(); j++) {
                View child = ((ViewGroup) nextChild).getChildAt(j);
                child.setBackgroundResource(R.drawable.cell);
            }
        }

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
            List<Sample> samples = new ArrayList<>();
            for(ScanResult sr : wifiScanList){
                if(apLocs.containsKey(sr.BSSID))
                    samples.add(new Sample(sr.level,apLocs.get(sr.BSSID)));
            }

            locate(samples);
        }
    }

    private void locate(List<Sample> samples){
        locations = new HashMap<>();
        if(samples != null && samples.size() > 0){
            Sample s = samples.get(0);
            Sample m[] = samples.toArray(new Sample[samples.size()]);
            for (int i = 0; i < m.length - 1; i++) {
                for (int j = i + 1; j < m.length; j++) {
                    Vertex[] vs = tri.findIntersections(m[i], m[j]);
                    for (int k = 0; k < vs.length; k++) {
                        if (vs[k] != null) {
                            int x = (int) (vs[k].getX() / s.CELL_SIZE);
                            int y = (int) (vs[k].getY() / s.CELL_SIZE);
                            boolean xInGrid = (x > 0 && x <= GRID_X);
                            boolean yInGrid = (y > 0 && y <= GRID_Y);
                            if(xInGrid && yInGrid) {
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
                Log.d(this.getClass().toString(),k + " " + locations.get(k));
            }
            Toast toast = Toast.makeText(this, "Most likely Location " + (mostLikelyLocation), Toast.LENGTH_SHORT);
            toast.show();
            Log.d(this.getClass().toString(), "Most likely Location " + mostLikelyLocation);
            int x = Integer.parseInt(mostLikelyLocation.split(" ")[0]);
            int y = Integer.parseInt(mostLikelyLocation.split(" ")[1]);
            Cell apCell = cells[(x-1)+(y-1)*3];
            TextView tv = (TextView) findViewById(apCell.getId());
            tv.setBackgroundResource(R.drawable.ap_cell_scanned);
        }
    }

}
