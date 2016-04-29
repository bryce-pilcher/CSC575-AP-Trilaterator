package edu.ncsu.csc575.aplocalization;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This activity handles user location
 */
public class UserLocateActivity extends AppCompatActivity {

    //Size of grid in x and y coordinates
    private final int GRID_X = 6;
    private final int GRID_Y = 6;

    //Receiver for wifi Scans
    private WifiScanReceiver wifiReciever;
    //Wifi Manager for starting scans and getting results.
    private WifiManager wifi;

    List<String> apNames;
    //Locations of APs to locate user from
    HashMap<String,Vertex> apLocs;
    //Class for trilateration
    Trilateration tri;
    //Hashmap of possible locations by Sample
    private HashMap<String, Integer> locations;

    //Array of cells for programmatically get the IDs for the xml elements.
    Cell[] cells;

    /**
     * This method is called when the activity is created.  Sets up variables and registers
     * receivers.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_locate);

        //Get wifi context and register the wifi receiver
        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        tri = new Trilateration();
        locations = new HashMap<>();
        apLocs = new HashMap<>();
        apNames = new ArrayList<>();
        //get intent that called this activity and get the apnames it passed along.
        Intent intent = getIntent();
        String names = intent.getStringExtra(MainActivity.AP_NAMES);

        //Split out the apnames according to how they were put together.
        for(String name : names.split(",")){
            String netName = name.split("->")[0];
            String bssid = name.split("->")[1];
            apNames.add(name);
        }

        //Sets up the array that holds the cells and their values to be programmatically called
        //for gui manipulation.  Arranged by columns bottom to top, from left to right.
        cells = new Cell[]{new Cell(R.id.c11, new Vertex(1,1)), new Cell(R.id.c21, new Vertex(2,1)), new Cell(R.id.c31, new Vertex(3,1)),
                new Cell(R.id.c41, new Vertex(4,1)), new Cell(R.id.c51, new Vertex(5,1)), new Cell(R.id.c61, new Vertex(6,1)),
                new Cell(R.id.c12, new Vertex(1,2)), new Cell(R.id.c22, new Vertex(2,2)), new Cell(R.id.c32, new Vertex(3,2)),
                new Cell(R.id.c42, new Vertex(4,2)), new Cell(R.id.c52, new Vertex(5,2)), new Cell(R.id.c62, new Vertex(6,2)),
                new Cell(R.id.c13, new Vertex(1,3)), new Cell(R.id.c23, new Vertex(2,3)), new Cell(R.id.c33, new Vertex(3,1)),
                new Cell(R.id.c43, new Vertex(4,3)), new Cell(R.id.c53, new Vertex(5,3)), new Cell(R.id.c63, new Vertex(6,1)),
                new Cell(R.id.c14, new Vertex(1,4)), new Cell(R.id.c24, new Vertex(2,4)), new Cell(R.id.c34, new Vertex(3,4)),
                new Cell(R.id.c44, new Vertex(4,4)), new Cell(R.id.c54, new Vertex(5,4)), new Cell(R.id.c64, new Vertex(6,4)),
                new Cell(R.id.c15, new Vertex(1,5)), new Cell(R.id.c25, new Vertex(2,5)), new Cell(R.id.c35, new Vertex(3,5)),
                new Cell(R.id.c45, new Vertex(4,5)), new Cell(R.id.c55, new Vertex(5,5)), new Cell(R.id.c65, new Vertex(6,5)),
                new Cell(R.id.c16, new Vertex(1,6)), new Cell(R.id.c26, new Vertex(2,6)), new Cell(R.id.c36, new Vertex(3,6)),
                new Cell(R.id.c46, new Vertex(4,6)), new Cell(R.id.c56, new Vertex(5,6)), new Cell(R.id.c66, new Vertex(6,6))};


        //find the pink button and assign it to locate the user when pressed.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locateUser(view);
            }
        });

        TextView btn;

        //Programmatically setting the onclick listner for all of the cells in the grid.
        for(int i = 0; i < cells.length; i++){
            Cell c = cells[i];
            btn = (TextView) findViewById(c.getId());
            btn.setOnClickListener(new CellOnClickListener(c));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * This method resets all of the cells and calls the wifi scan.  On the receipt of the scan
     * results, the location will take place.
     * @param v view to be used to reset the cells.
     */
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

    /**
     * This class receives the intent from the wifi scan when it returns.
     * and handles storing samples.
     */
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

    /**
     * This method sets the location of an AP to a cell.
     *
     * If another cell is chosen to hold the same ap, it will overwrite bc of the use
     * of a hashmap to store the ap and location.
     * @param v view to show the dialog
     * @param c cell to have an ap set in
     */
    private void setCell(View v, final Cell c){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] aps = apNames.toArray(new CharSequence[apNames.size()]);
        builder.setTitle("Select One AP:")
                .setItems(aps, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //put the ap name with the center of the cell.
                        apLocs.put(apNames.get(item).split("->")[1], c.getCenter());
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This method handles all of the location logic for finding a user.
     * @param samples
     */
    private void locate(List<Sample> samples){
        locations = new HashMap<>();
        if(samples != null && samples.size() > 0){
            Sample s = samples.get(0);
            //change the samples list to an array for iteration
            Sample m[] = samples.toArray(new Sample[samples.size()]);
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
                            Log.d(this.getClass().toString(), "User Location: " + x +", " + y);
                            //Check to make sure they are in the grid.
                            boolean xInGrid = (x > 0 && x <= GRID_X);
                            boolean yInGrid = (y > 0 && y <= GRID_Y);
                            //If they are both in the grid, store in hashmap as possible location
                            if(xInGrid && yInGrid) {
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
                Log.d(this.getClass().toString(),k + " " + locations.get(k));
            }
            Toast toast = Toast.makeText(this, "Most likely Location " + (mostLikelyLocation), Toast.LENGTH_SHORT);
            toast.show();
            Log.d(this.getClass().toString(), "Most likely Location " + mostLikelyLocation);
            int x = Integer.parseInt(mostLikelyLocation.split(" ")[0]);
            int y = Integer.parseInt(mostLikelyLocation.split(" ")[1]);
            Cell apCell = cells[(x-1)+(y-1)*GRID_X  ];
            //Set the cell to show that the user is in that one.
            TextView tv = (TextView) findViewById(apCell.getId());
            tv.setBackgroundResource(R.drawable.ap_cell_scanned);
        }
    }

    /**
     * Custom class for cell onClickListner to allow passing of extra values.
     */
    public class CellOnClickListener implements View.OnClickListener
    {

        Cell c;
        public CellOnClickListener(Cell cell) {
            this.c = cell;
        }

        @Override
        public void onClick(View v)
        {
            setCell(v, c);
        }

    };

    /**
     * Life cycle management method.
     */
    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    /**
     * Life cycle management method.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Life cycle management method.
     */
    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

}
