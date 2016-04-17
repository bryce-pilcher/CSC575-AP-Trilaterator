package edu.ncsu.csc575.aplocalization;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class LocateActivity extends AppCompatActivity {
    APLocation apl;
    String apNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        Intent intent = getIntent();
       /* apNames= intent.getStringExtra("SELECTED_ACCESS_POINT_NAMES");*/

   /*     Toast toast = Toast.makeText(this, apNames.split(" ")[1], Toast.LENGTH_SHORT);
        toast.show();*/
        /*apl = new APLocation(this, apNames.split(" ")[1]);*/
        ConfigureGridDialogFragment frag = new ConfigureGridDialogFragment();
        FragmentManager fragmentManager = getFragmentManager();
        frag.show(fragmentManager,"string");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startSample();
            }
        });

        TextView btn=(TextView) findViewById(R.id.c11);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(1, 1));
            }
        });
        btn=(TextView) findViewById(R.id.c12);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(2, 1));
            }
        });
        btn=(TextView) findViewById(R.id.c13);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(3, 1));
            }
        });
        btn=(TextView) findViewById(R.id.c21);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(1, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c22);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(2, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c23);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(3, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c31);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(1, 3));
            }
        });
        btn=(TextView) findViewById(R.id.c32);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(2, 3));
            }
        });
        btn=(TextView) findViewById(R.id.c33);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(3, 3));
            }
        });
        btn=(TextView) findViewById(R.id.c41);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(1, 4));
            }
        });
        btn=(TextView) findViewById(R.id.c42);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(2, 4));
            }
        });
        btn=(TextView) findViewById(R.id.c43);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(3, 4));
            }
        });
        btn=(TextView) findViewById(R.id.c51);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(1, 5));
            }
        });
        btn=(TextView) findViewById(R.id.c52);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(2, 5));
            }
        });
        btn=(TextView) findViewById(R.id.c53);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(3, 5));
            }
        });
        btn=(TextView) findViewById(R.id.c61);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(1, 6));
            }
        });
        btn=(TextView) findViewById(R.id.c62);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(2, 6));
            }
        });
        btn=(TextView) findViewById(R.id.c63);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCell(v, new Vertex(3, 6));
            }
        });
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

    private void changeCell(View v,Vertex center){
        ViewGroup vg = (ViewGroup)v.getParent().getParent();
        for(int i=0; i< vg.getChildCount(); ++i) {
            View nextChild = vg.getChildAt(i);
            for(int j=0; j < ((ViewGroup)nextChild).getChildCount(); j++){
                View child = ((ViewGroup)nextChild).getChildAt(j);
                child.setBackgroundResource(R.drawable.cell);
            }
        }
        v.setBackgroundColor(Color.parseColor("#000000"));
        Vertex apLoc = apl.changeCell(center);
        if(apLoc != null) {
            Log.d(this.getClass().toString(), apLoc.getX() + " " + apLoc.getY());
        }
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
}
