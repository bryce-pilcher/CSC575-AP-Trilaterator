package edu.ncsu.csc575.aplocalization;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LocateActivity extends AppCompatActivity {
    APLocation apl;
    String apName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        Intent intent = getIntent();
        apName= intent.getStringExtra(MainActivity.AP_NAME);

        Toast toast = Toast.makeText(this, apName.split(" ")[1], Toast.LENGTH_SHORT);
        toast.show();
        apl = new APLocation(this, apName.split(" ")[1]);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                StartSample();
            }
        });

        TextView btn=(TextView) findViewById(R.id.c11);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(1, 1));
            }
        });
        btn=(TextView) findViewById(R.id.c12);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(1, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c13);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(1, 3));
            }
        });
        btn=(TextView) findViewById(R.id.c21);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(2, 1));
            }
        });
        btn=(TextView) findViewById(R.id.c22);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(2, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c23);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(2, 3));
            }
        });
        btn=(TextView) findViewById(R.id.c31);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(3, 1));
            }
        });
        btn=(TextView) findViewById(R.id.c32);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(3, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c33);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(3, 3));
            }
        });
        btn=(TextView) findViewById(R.id.c41);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(4,1));
            }
        });
        btn=(TextView) findViewById(R.id.c42);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(4, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c43);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(4,3));
            }
        });
        btn=(TextView) findViewById(R.id.c51);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(5, 1));
            }
        });
        btn=(TextView) findViewById(R.id.c52);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(5, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c53);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(5, 3));
            }
        });
        btn=(TextView) findViewById(R.id.c61);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(6,1));
            }
        });
        btn=(TextView) findViewById(R.id.c62);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(6, 2));
            }
        });
        btn=(TextView) findViewById(R.id.c63);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apl.changeCell(new Vertex(6,3));
            }
        });
    }

    private void StartSample(){
        Log.d(this.getClass().toString(), "Starting Sampling");
        apl.setSample(true);
        apl.getSample();
    }

    protected void onPause() {
        if (apl != null) {
            apl.unregisterReceiver(this);
            apl = null;
        }
        super.onPause();
    }

    protected void onResume() {
        apl = new APLocation(this, apName.split(" ")[1]);
        super.onResume();
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
}
