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
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the main activity class for the App.  Handles the startup and calling of the AP locate
 * activity and the User locate Activity.
 */
public class MainActivity extends AppCompatActivity {

    //Final string that is used to pass parameters to other activities.
    public final static String AP_NAMES = "edu.ncsu.csc575.aplocalization.APNAMES";
    //Check if wifi Permission has been given
    private boolean wifiPermission = true;
    ListView lv;
    List<String> wifis;
    //Receiver for wifi Scans
    private WifiScanReceiver wifiReciever;
    //Wifi Manager for starting scans and getting results.
    private WifiManager wifi;
    //AP to locate
    private String ap;

    /**
     * This method is called when the activity is created.  Sets up variables and registers
     * receivers.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get wifi context and register receiver
        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //Set up the list view for showing all available SSIDs
        lv=(ListView)findViewById(R.id.listView);

        String data = "No request";

        //Check if wifi is enables
        if(!wifi.isWifiEnabled()){
            Toast toast = Toast.makeText(this, "WiFi must be enabled.", Toast.LENGTH_LONG);
            toast.show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }

        //Android api 23 requires runtime permission, so check if it is 23 to do runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            wifiPermission = false;
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else{
            Log.d(this.getClass().toString(), "Calling get APs");
            //Start the scan to let them know what ssids are available
            wifi.startScan();
        }

        TextView textView = (TextView) findViewById(R.id.rssi_value);
        textView.setText(data);
    }

    /**
     * Open AP scan map
     * @param position
     */
    public void openScanMap(int position){
        Intent intent = new Intent(this, LocateActivity.class);
        String apName = (lv.getItemAtPosition(position)).toString();
        intent.putExtra(AP_NAMES,apName);
        wifi = null;
        startActivity(intent);
    }

    /**
     * Lifecycle management method.
     */
    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    /**
     * Lifecycle management method.
     */
    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    /**
     * Lifecycle management method.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Lifecycle management method.
     */
    protected void onStart(){ super.onStart(); }

    /**
     * Lifecycle management method.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Method that creates the options bar at the top
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Method that handles buttons on the menu bar being clicked.
     * @param item the item that was clicked
     * @return
     */
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

        //if the AP Localize button is selected
        if (id == R.id.action_localize) {
            Intent intent = new Intent(getApplicationContext(), LocateActivity.class);
            lv.getCheckedItemPositions();
            String selected = "";
            int countChoice = lv.getCount();

            //Check the list for selected items and add it to the string of selected items
            //to be sent to the next activity.
            SparseBooleanArray sparseBooleanArray = lv.getCheckedItemPositions();
            for (int i = 0; i < countChoice; i++) {
                if (sparseBooleanArray.get(i)) {
                    selected += lv.getItemAtPosition(i).toString() + ",";
                }
                wifi = null;
            }

            //If nothing selected, don't let it continue to another activity.
            if(selected.equals("")){
                Toast toast = Toast.makeText(this, "Please choose at least one AP", Toast.LENGTH_LONG);
                toast.show();
            }else {
                intent.putExtra(AP_NAMES, selected);
                startActivity(intent);
            }
        }

        //If the locate user button is pressed.
        if(id == R.id.action_locate_user){
            Intent intent = new Intent(getApplicationContext(), UserLocateActivity.class);
            lv.getCheckedItemPositions();
            String selected = "";
            int countChoice = lv.getCount();

            //Go through the list and get the selected items.
            SparseBooleanArray sparseBooleanArray = lv.getCheckedItemPositions();
            for (int i = 0; i < countChoice; i++) {
                if (sparseBooleanArray.get(i)) {
                    selected += lv.getItemAtPosition(i).toString() + ",";
                }
                wifi = null;
            }
            //If nothing selected, don't let it continue to another activity.
            if(selected.equals("")){
                Toast toast = Toast.makeText(this, "Please choose at least one AP", Toast.LENGTH_LONG);
                toast.show();
            }else {
                intent.putExtra(AP_NAMES, selected);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method for handling request permissionresult.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    wifiPermission = true;
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
    /**
     * This class receives the intent from the wifi scan when it returns.
     * and populates the list with available ssids.
     */
    public class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();
            String data = "";
            wifis = new ArrayList<>();
            for (int i = 0; i < wifiScanList.size(); i++) {
                if(!wifiScanList.get(i).SSID.contains("ncsu") && !wifiScanList.get(i).SSID.contains("eduroam")) {
                    wifis.add((wifiScanList.get(i).SSID) + "->" + wifiScanList.get(i).BSSID);
                }
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