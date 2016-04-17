package edu.ncsu.csc575.aplocalization;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String AP_NAME = "edu.ncsu.csc575.aplocalization.APNAME";
    private boolean wifiPermission = true;
    ListView lv;
    FloatingActionButton floatingActionButton;
    String wifis[];
    //Receiver for wifi Scans
    private WifiScanReceiver wifiReciever;
    //Wifi Manager for starting scans and getting results.
    private WifiManager wifi;
    //AP to locate
    private String ap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        lv=(ListView)findViewById(R.id.listView);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        wifi.startScan();

        String data = "No request";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            wifiPermission = false;
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else{
            Log.d(this.getClass().toString(), "Calling get APs");
            getAPs();
        }

        TextView textView = (TextView) findViewById(R.id.rssi_value);
        textView.setText(data);

/*        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
                Snackbar.make(arg1, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                openScanMap(position);
            }
        })*/
    }

    public void openScanMap(int position){
        Intent intent = new Intent(this, LocateActivity.class);
        String apName = (lv.getItemAtPosition(position)).toString();
        intent.putExtra(AP_NAME,apName);
        wifi = null;
        startActivity(intent);
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_localize) {
            Intent intent = new Intent(getApplicationContext(), LocateActivity.class);
            lv.getCheckedItemPositions();
            String selected = "";
            int countChoice = lv.getCount();
            startActivity(intent);

/*            SparseBooleanArray sparseBooleanArray = lv.getCheckedItemPositions();
            for (int i = 0; i < countChoice; i++) {
                if (sparseBooleanArray.get(i)) {
                    selected += lv.getItemAtPosition(i).toString() + ",";
                }

                *//*intent.putExtra("SELECTED_ACCESS_POINTS", selected);*//*
                Toast toast = Toast.makeText(this, selected, Toast.LENGTH_SHORT);
                toast.show();
                wifi = null;
            }*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    wifiPermission = true;
                    getAPs();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void getAPs(){
        String data = "No WiFi Permission";
        if(wifiPermission) {
            Log.d(this.getClass().toString(), "Starting scan");
            data = "Scan started";
            if(ap == null) {
                Toast toast = Toast.makeText(this, "Please choose an AP", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        TextView textView = (TextView) findViewById(R.id.rssi_value);
        textView.setText(data);

    }

    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            /**
             * public boolean getBooleanExtra (String name, boolean defaultValue)
             * Added in API level 1
             * Retrieve extended data from the intent.
             * Parameters
             * name	String: The name of the desired item.
             * defaultValue	boolean: the value to be returned if no value of the desired type is stored with the given name.
             * Returns
             * boolean	the value of an item that previously added with putExtra() or the default value if none was found.
             */

            /**
             * public static final String EXTRA_RESULTS_UPDATED
             * Added in API level 23
             * Lookup key for a boolean representing the result of previous startScan() operation, reported with SCAN_RESULTS_AVAILABLE_ACTION.
             * Constant Value: "resultsUpdated"
             */

            if(intent.getBooleanExtra("EXTRA_RESULTS_UPDATED",false)){
                Log.d("EXTRA_RESULTS_UPDATED","EXTRA_RESULTS_UPDATED");
            }
            List<ScanResult> wifiScanList = wifi.getScanResults();
            String data = "";
            wifis = new String[wifiScanList.size()];
            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = (wifiScanList.get(i).SSID) + " " + wifiScanList.get(i).BSSID;
            }
            if (wifiScanList.size() > 0) {
                data = "scanlist > 0";
            } else {
                data = wifiScanList.size() + "";
            }
            TextView textView = (TextView) findViewById(R.id.rssi_value);
            textView.setText(data);
            lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, wifis));
            lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }
    }

    }