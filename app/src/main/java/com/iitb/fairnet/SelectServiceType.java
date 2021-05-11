package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import static com.iitb.fairnet.Globals.MAX_SPEED;

public class SelectServiceType extends AppCompatActivity {
    public static Context context;
    private TextView mTextMessage;
    double speed;
    TextView textView;
    SeekBar seekBar;
    /* +GLOC */
    Globals.mcl_gloc_enum gloc;

    private void mcl_get_intent_data(Intent intent) {
        Bundle intentData = intent.getExtras();
        if (null != intentData) {
            if (intentData.containsKey("Gloc"))
                gloc = (Globals.mcl_gloc_enum) intentData.get("Gloc");
        }
    }
    /* -GLOC */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_service_type);
        setTitle("Select service type");
        this.gloc = Globals.dev.gloc;
        this.context = getApplicationContext();
        /* +GLOC */
        mcl_get_intent_data(getIntent());
        Log.d("Value","Location is "+gloc);
        /* -GLOC */
        /***
        textView = (TextView) findViewById(R.id.sbtext);
        //seekBar = findViewById(R.id.seekBar);
        //seekBar.setProgress(MAX_SPEED);
        speed = (MAX_SPEED/100)*10.0;
        String msg = String.format("Maximum server speed = %.2f Mbps",speed);
        textView.setText(msg);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            float pmax = seekBar.getMax();

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speed = (progress*1.0/pmax)*10;
                String msg = String.format("Maximum server speed = %.2f Mbps",speed);
                textView.setText(msg);
            }
        });
            **/
    }

    private void mcl_put_intent_data(Intent intent){
        intent.putExtra("Speed", speed);
        intent.putExtra("Gloc", gloc);
    }

    private  void mcl_init_sst_activity(){
        speed = 0;
        gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }

    public void mcl_select_video_service(View view){
        Intent intent = new Intent(this, SelectVideoService.class);
        mcl_put_intent_data(intent);
        mcl_init_sst_activity();
        // start the activity connect to the specified class
        startActivity(intent);
        finish();
    }

    public void mcl_select_audio_service(View view){
        Intent intent = new Intent(this, SelectAudioService.class);
        mcl_put_intent_data(intent);
        mcl_init_sst_activity();
        // start the activity connect to the specified class
        startActivity(intent);
        finish();
    }
}
