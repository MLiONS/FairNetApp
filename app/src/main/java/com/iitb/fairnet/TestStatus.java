package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.SocketException;


import static com.iitb.fairnet.Globals.MAX_DOWNLOAD_TIME;
import static com.iitb.fairnet.Globals.N_RETRY;
import static com.iitb.fairnet.Globals.tsContext;
import static com.iitb.fairnet.R.id.surfaceView;
import static com.iitb.fairnet.R.id.wrap;
import static java.lang.Thread.sleep;


public class TestStatus extends AppCompatActivity {
    private DrawGraph graphView;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView textView;
    private Handler handler = new Handler();
    long ctime = 0;
    long ptime = 0;
    Globals.mcl_apps_enum[] app_list;
    Globals.mcl_apps_enum test_app;
    double speed;
    int nretry;
    boolean td_status;
    Globals.mcl_gloc_enum gloc;

    public TestStatus(){
        speed = 0;
        app_list = null;
        test_app = Globals.mcl_apps_enum.INVALID_APP;
        nretry = 0;
        td_status = false;
        gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }


    /***
    private long mcl_get_other_app_progress(Globals.mcl_apps_enum eapp){
        int lapp = Globals.mcl_apps_enum.INVALID_APP.ordinal();
        long progress = 0;
        for (int i=0;i<lapp;i++)
        {
            Globals.mcl_apps_enum capp = app_list[i];
            if (app_list[i] == Globals.mcl_apps_enum.INVALID_APP ||
                    eapp == capp)
                continue;
            if (null == RunTest.adownloader[capp.ordinal()])
                continue;
            long adata_len = RunTest.adownloader[capp.ordinal()].app_data_len;
            //if (progress == 0 || progress > adata_len)
            if (capp == test_app)
                progress = adata_len;
        }
        return progress;
    }
     ***/

    private long mcl_get_progress(){
        int lapp = Globals.mcl_apps_enum.INVALID_APP.ordinal();
        long progress = 0;
        if (null == RunTest.adownloader)
            return progress;
        for (int i=0;i<lapp;i++)
        {
            Globals.mcl_apps_enum capp = app_list[i];
            if (app_list[i] == Globals.mcl_apps_enum.INVALID_APP)
                continue;
            if (null == RunTest.adownloader[capp.ordinal()])
                continue;
            long adata_len = RunTest.adownloader[capp.ordinal()].app_data_len;
            /* +3MIN */
            if (progress == 0 || progress > adata_len)
                progress = adata_len;
                /* -3MIN */
            //if (capp == test_app)
        }
        return progress;
    }

    private void mcl_reset_test_status_data(){
        handler = new Handler();
        progressStatus = 0;
    }

    private void mcl_stop_app_download(){
        Globals.mcl_apps_enum[] appl = app_list;
        int lapp = app_list.length;
        if (null == RunTest.adownloader)
            return;
        for (int i = 0; i < lapp; i++) {
            if (appl[i].name() != Globals.mcl_apps_enum.INVALID_APP.name()) {
                if (null != RunTest.adownloader[appl[i].ordinal()]) {
                    if (null != RunTest.adownloader[appl[i].ordinal()].sock) {
                        try {
                            RunTest.adownloader[appl[i].ordinal()].sock_input.close();
                            RunTest.adownloader[appl[i].ordinal()].sock_out.close();
                            RunTest.adownloader[appl[i].ordinal()].sock.close();
                        } catch (SocketException e) {
                            //Log.d("ERROR", "Socket Error");
                        } catch (IOException e) {
                            //Log.d("ERROR", "IO Error");
                        }
                        /* +3MIN*/
                        RunTest.adownloader[appl[i].ordinal()].dcomp = true;
                        /* -3MIN*/
                    }
                }
            }
        }
    }

    private void mcl_get_intent_data(Intent intent) {
        Bundle intentData = intent.getExtras();
        app_list = (Globals.mcl_apps_enum[])intentData.get("AppList");
        test_app = (Globals.mcl_apps_enum)intentData.get("TestApp");
        speed = (double)intentData.get("Speed");
        gloc = (Globals.mcl_gloc_enum)intentData.get("Gloc");
        /* +New Algo */
        if (intentData.containsKey("Nretry"))
            nretry = (int)intentData.get("Nretry");
        Log.d("Status", "nretry set to "+ nretry);
        /* +New Algo */
    }

    /* +new Algo */
    /***
    private void mcl_put_intent_data_back(Intent intent){
        intent.putExtra("AppList", app_list);
        intent.putExtra("TestApp", test_app);
        intent.putExtra("Speed", speed);
        intent.putExtra("Nretry", nretry);
        intent.putExtra("Gloc", gloc);
    }

    private void mcl_run_test(Globals.mcl_apps_enum[] app_list,
                              Globals.mcl_apps_enum test_app,
                              double speed,
                              Globals.mcl_gloc_enum gloc){
        RunTest rt = new RunTest(app_list, test_app, speed, gloc);
        rt.start();
    }

    private void mcl_restart_app_download() throws InterruptedException {
        nretry++;
        mcl_stop_app_download();
        sleep(10);
        Intent intent = new Intent(this, TestStatus.class);
        mcl_put_intent_data_back(intent);
        startActivity(intent);
        mcl_run_test(app_list, test_app, speed, gloc);
    }

    private boolean mcl_get_td_status(){
        boolean rval = false;
        long oapp_prog = mcl_get_other_app_progress(test_app);
        long opstatus = Math.round(((oapp_prog*1.0)/Globals.MAX_DATA_SIZE)*100.0);
        if (N_RETRY == nretry && 100 == opstatus)
            rval = true;
        return rval;
    }
     ***/
    /* -new Algo */

    DrawView drawView;

    private boolean mcl_get_dcomp_status(){
        int lapp = Globals.mcl_apps_enum.INVALID_APP.ordinal();
        boolean dcomp = false;
        int dcapp = 0;
        int tapp = 0;
        if (null == RunTest.adownloader)
            return dcomp;
        for (int i=0;i<lapp;i++)
        {
            Globals.mcl_apps_enum capp = app_list[i];
            if (app_list[i] == Globals.mcl_apps_enum.INVALID_APP)
                continue;
            if (null == RunTest.adownloader[capp.ordinal()])
                continue;
            boolean adcomp = RunTest.adownloader[capp.ordinal()].dcomp;
            if (capp == test_app && adcomp){
                dcomp = true;
                for (int j=0;j<lapp;j++) {
                    Globals.mcl_apps_enum oapp = app_list[j];
                    if (app_list[j] == Globals.mcl_apps_enum.INVALID_APP)
                        continue;
                    RunTest.adownloader[oapp.ordinal()].dcomp = true;
                }
                break;
            }
        }
        return dcomp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_status);
        graphView = (DrawGraph) findViewById(surfaceView);
        mcl_reset_test_status_data();
        mcl_get_intent_data(getIntent());
        graphView.mcl_update_graph_data(app_list, test_app, null, null, null);
        graphView.startDrawThread();
        progressBar = (ProgressBar) findViewById(R.id.HprogressBar);
        textView = (TextView) findViewById(R.id.textView);
        ptime = System.currentTimeMillis();
        final int STATUS_SLEEP = 1000; // in ms
        tsContext = this;
        new Thread(new Runnable() {
            public void run() {
                int oprogress = 0;
                /* +MOD */
                float pstatus = 0;
                final long rtime = MAX_DOWNLOAD_TIME/(60*1000);
                long rtime_ms = rtime*(60*1000);
                /* +3MIN */
                while (progressStatus < 100) {
                // while (true) {
                    long progress = mcl_get_progress();
                    pstatus = Math.round(((progress*1.0)/Globals.MAX_DATA_SIZE)*100.0);
                    progressStatus = Math.round(pstatus);
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            // textView.setText(progressStatus+"/"+progressBar.getMax()+": Runs for a maximum of "+ rtime + " mins");
                            String output_data = "Runs for maximum " + rtime + " minutes";
                            textView.setText(output_data);
                        }
                    });
                    if (30 == progressStatus)
                        Log.d("Status", "30 percent reached");

                    /***
                    pstatus = Math.round(((((ctime - ptime))*1.0)/rtime_ms)*100.0);
                    progressStatus = Math.round(pstatus);
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            String stext = progressStatus+"/"+progressBar.getMax()+ " Runs for "+ rtime + " mins";
                            textView.setText(stext);
                        }
                    });
                     ***/
                    /* -3MIN */
                    ctime = System.currentTimeMillis();
                    long tdiff = (ctime - ptime);
                    if (rtime_ms < tdiff) {
                        /* +new Algo */
                        /* +MOD_ALGO */
                        /**
                        if (70 >= progressStatus && nretry < N_RETRY) {
                            Log.d("Status", "Restarting download as nretry is "+ nretry);
                            try {
                                mcl_restart_app_download();
                            } catch (InterruptedException e) {
                                Log.d("Status","Interrupted");
                            }
                        }
                        else
                            **/
                            /* -MOD_ALGO */
                        {
                            /* -new Algo */
                            mcl_stop_app_download();
                            graphView.stopDrawThread();
                            /* +new Algo */
                            /* +MOD_ALGO */
                            // td_status = mcl_get_td_status();
                            /* -MOD_ALGO */
                            /* -new Algo */
                            mcl_show_results();
                        }
                        break;
                    }
                    try {
                        Thread.sleep(STATUS_SLEEP);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }

                if (100 <= progressStatus)
                {
                    boolean dcomp = false;
                    while (!dcomp){
                        dcomp = mcl_get_dcomp_status();
                        try {
                            Thread.sleep(STATUS_SLEEP);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                    mcl_stop_app_download();
                    graphView.stopDrawThread();
                    mcl_show_results();
                }
            }
        }).start();
    }

    private void mcl_put_intent_data(Intent intent){
        intent.putExtra("AppList", app_list);
        intent.putExtra("TestApp", test_app);
        intent.putExtra("TD", td_status);
        intent.putExtra("Gloc", gloc);
    }

    public void mcl_show_results() {
        Intent intent = new Intent(this, DisplayResult.class);
        mcl_put_intent_data(intent);
        // start the activity connect to the specified class
        startActivity(intent);
        finish();
    }

    /* Drawline */
    public class DrawView extends View {
        Paint paint = new Paint();

        private void init() {
            paint.setColor(Color.BLACK);
        }

        public DrawView(Context context) {
            super(context);
            init();
        }

        public DrawView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DrawView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawLine(0, 0, 200, 200, paint);
            canvas.drawLine(200, 0, 0, 200, paint);
        }
    }
}
