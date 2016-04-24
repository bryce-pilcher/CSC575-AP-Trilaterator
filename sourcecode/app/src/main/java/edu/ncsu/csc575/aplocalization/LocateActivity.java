package edu.ncsu.csc575.aplocalization;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocateActivity extends AppCompatActivity {

    public final static String APS = "edu.ncsu.csc575.aplocalization.APNAMES";
    APLocation apl;
    List<String> apNames;
    HashMap<String,String> ssid;
    Cell[] cells;
    final int GRID_SIZE = 36;
    final int GRID_X = 6;
    final int GRID_Y = 6;
    HashMap<String, Vertex> apLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        apNames = new ArrayList<>();
        ssid = new HashMap<>();

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

        Intent intent = getIntent();
        String names = intent.getStringExtra(MainActivity.AP_NAMES);

        for(String name : names.split(",")){
            String netName = name.split("->")[0];
            String bssid = name.split("->")[1];
            apNames.add(bssid);
            ssid.put(bssid,netName);
        }

        Toast toast = Toast.makeText(this, apNames.get(0), Toast.LENGTH_SHORT);
        toast.show();
        apl = new APLocation(this, this, apNames);
        /*ConfigureGridDialogFragment frag = new ConfigureGridDialogFragment();
        FragmentManager fragmentManager = getFragmentManager();
        frag.show(fragmentManager,"string");*/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sampling....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startSample();
            }
        });

        TextView btn;

        for(int i = 0; i < cells.length; i++){
                Cell c = cells[i];
                btn = (TextView) findViewById(c.getId());
                btn.setOnClickListener(new CellOnClickListener(c));
        }

    }

    private void startSample(){
        Log.d(this.getClass().toString(), "Starting Sampling");
        apl.setSample(true);
        apl.getSample();
    }

    protected void onPause() {
        if (apl != null) {
            Log.d(this.getClass().toString(),"About to unregister receiver on Pause");
            apl.unregisterReceiver(this);
            apl = null;
        }
        super.onPause();
    }

    protected void onDestroy(){
        if (apl != null) {
            Log.d(this.getClass().toString(),"About to unregister receiver on Destroy");
            apl.unregisterReceiver(this);
            apl = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onResume() {
      /*  apl = new APLocation(this, apNames.split(" ")[1]);*/

        super.onResume();
    }

    private void changeCell(View v, Cell cell){

        cell.setState(cell.SCANNED);

        ViewGroup vg = (ViewGroup)v.getParent().getParent();
        for(int i=0; i < vg.getChildCount(); i++) {
            View nextChild = vg.getChildAt(i);
            for (int j = 0; j < ((ViewGroup) nextChild).getChildCount(); j++) {
                View child = ((ViewGroup) nextChild).getChildAt(j);
                int cellLoc = j+(GRID_Y-1-i)*GRID_X;
                if(cells[cellLoc].getState() == cell.SCANNED){
                    child.setBackgroundResource(R.drawable.scanned_cell);
                }else{
                    child.setBackgroundResource(R.drawable.cell);
                }
                TextView tv = (TextView) findViewById(cells[cellLoc].getId());
                tv.setText("");
            }
        }


        v.setBackgroundColor(Color.parseColor("#000000"));
        apLoc = apl.changeCell(cell);

        if(apLoc != null) {
            for (String ap : apNames) {
                if (apLoc.get(ap) != null) {
                    int x = (int) apLoc.get(ap).getX();
                    int y = (int) apLoc.get(ap).getY();
                    Log.d(this.getClass().toString(),  + x + " " + y);
                    Cell apCell = cells[(x-1)+(y-1)*GRID_X];
                    TextView tv = (TextView) findViewById(apCell.getId());
                    if(apCell.getState() == apCell.SCANNED) {
                        tv.setBackgroundResource(R.drawable.ap_cell_scanned);
                    }else{
                        tv.setBackgroundResource(R.drawable.ap_cell);
                    }
                    String locatedSSID = tv.getText().equals("") ? ssid.get(ap) : tv.getText() + " " + ssid.get(ap);
                    tv.setText(locatedSSID);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_locate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle app bar item clicks here. The app bar
        // automatically handles clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_locate_user){
            Intent intent = new Intent(getApplicationContext(), UserLocateActivity.class);
            String apLocations = "";
            for(String k : apLoc.keySet()){
                apLocations += k + ">" + apLoc.get(k).toString() + ",";
            }
            apLocations = apLocations.substring(0,apLocations.lastIndexOf(","));
            Log.d(this.getClass().toString(), apLocations);
            intent.putExtra(APS,apLocations);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public class ConfigureGridDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(R.layout.configure_grid);
            builder.setMessage("Grid Configuration")
                    .setPositiveButton("Fire", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public class CellOnClickListener implements View.OnClickListener
    {

        Cell c;
        public CellOnClickListener(Cell cell) {
            this.c = cell;
        }

        @Override
        public void onClick(View v)
        {
            changeCell(v, c);
        }

    };
}
