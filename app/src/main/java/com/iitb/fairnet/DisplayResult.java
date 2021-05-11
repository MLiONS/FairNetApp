package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.iitb.fairnet.Globals.MAX_DATA_SIZE;
import static com.iitb.fairnet.Globals.MAX_NUM_CB;
import static com.iitb.fairnet.Globals.MAX_SLOT_TIME;
import static com.iitb.fairnet.Globals.MAX_TIME_RANGE;
import static com.iitb.fairnet.Globals.MEAS_MODE;
import static com.iitb.fairnet.Globals.MIN_NORM_TIME_RANGE;
import static com.iitb.fairnet.Globals.MIN_TH_RANGE;
import static com.iitb.fairnet.Globals.hl;
import static com.iitb.fairnet.Globals.mcl_apps_enum.INVALID_APP;
import static com.iitb.fairnet.Globals.mcl_meas_mode_enum.APP_BURST;
import static com.iitb.fairnet.Globals.mcl_meas_mode_enum.APP_DATA;
import static com.iitb.fairnet.Globals.net_name;
import static com.iitb.fairnet.Globals.sl;
import static java.lang.Thread.sleep;

public class DisplayResult extends AppCompatActivity {
    TextView ptextView;
    TextView trdtextView;
    TextView cmtextView;
    private DrawGraph graphView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static String report; //= "REPORT:\n";
    private static boolean result_ready = false;
    private static double[] th; //= new double[INVALID_APP.ordinal()];
    private static double[][] bth; //= new double[INVALID_APP.ordinal()][];
    public static Globals.mcl_apps_enum test_app = INVALID_APP;
    public static Globals.mcl_apps_enum[] app_list;
    /* +New algo */
    private static boolean qtd_status;
    private static int[] ntwth;
    private static twth_info[][] twth;
    //private static int[] ntwath;
    //private static twth_info[][] twath;
    private static double[] ath = new double[INVALID_APP.ordinal()];
    public static class twth_info{
        double time;
        double twth;
        twth_info()
        {
            time = 0;
            twth = 0;
        }
    }
    private static double stime;
    private static double etime;
    /* -New algo */

    private void mcl_init_data() {
        report = "REPORT:\n";
        result_ready = false;
        th = new double[INVALID_APP.ordinal()];
        bth = new double[INVALID_APP.ordinal()][];
        /* +New algo */
        ntwth = new int[INVALID_APP.ordinal()];
        twth = new twth_info[INVALID_APP.ordinal()][10000];
        //ntwath = new int[INVALID_APP.ordinal()];
        //twath = new twth_info[INVALID_APP.ordinal()][100];
        stime = 0;
        etime = 0;
        qtd_status = false;
        test_app = INVALID_APP;
        app_list = null;
        /* -New algo */
    }
    /* +BURST */
    private void mcl_ini_report (){
        int i = 0;
        String rep_ini = "ClientId:";
        String uniqueID = Globals.app_id;
        rep_ini += uniqueID + "\n";
        DisplayResult.report += rep_ini;
        // DisplayResult.report += Globals.dev.debug;
    }
    /* -BURST */

    private void mcl_wait_for_result_generation(){
        while (!result_ready) {
            ptextView.setText(R.string.wait);
            //handler.post(new Runnable() {
            //    public void run() {
            //        ptextView.setText(R.string.wait);
            //    }
            //});
            //SystemClock.sleep(500);
            //handler.post(new Runnable() {
            //    public void run() {
            //        ptextView.setText("");
            //    }
            //});
            SystemClock.sleep(500);
        }
        //ptextView.setText(R.string.TDDetectNotification);
        //mcl_display_runavg_speed();
        mcl_display_trdiff_status();
        handler.post(new Runnable() {
            public void run() {
                ptextView.setText(R.string.results);
            }
        });
    }

    private void mcl_display_trdiff_status(){
        handler.post(new Runnable() {
            public void run() {
                if (null != RunTest.adownloader) {
                    StringBuilder rbuilder = new StringBuilder();
                    /* +new Algo */
                    //String result = " ISP:" + Globals.dev.carrier_name + "\n\n";
                    rbuilder.append(" ISP:");
                    rbuilder.append(Globals.dev.carrier_name);
                    rbuilder.append("\n\n");
                    //result += "\n " + "Service : " + test_app + "\n";
                    rbuilder.append("\n ");
                    rbuilder.append("Service : ");
                    rbuilder.append(test_app);
                    rbuilder.append("\n");
                    for (int i = 0; i < INVALID_APP.ordinal(); i++)
                        if (!app_list[i].name().equals(INVALID_APP.name())) {
                            if (test_app.ordinal() == app_list[i].ordinal()) {
                                double cth = ath[app_list[i].ordinal()] / 1000000.0;
                                String speed = String.format("%.3f Mbps", cth);
                                //result += " " + "Average speed" + " : " + speed + "\n";
                                rbuilder.append(" ");
                                rbuilder.append("Average speed");
                                rbuilder.append(" : ");
                                rbuilder.append(speed);
                                rbuilder.append("\n");
                            }
                        }
                    //trdtextView.setText(result);
                    rbuilder.append(mcl_get_trdiff_status());
                    trdtextView.setText(rbuilder);
                    RunTest.RunTestInit();
                    graphView.stopDrawThread();
                }
            }
        });
    }

    private void mcl_display_cont_mod_status(){
        handler.post(new Runnable() {
            public void run() {
                String result = mcl_get_cont_mod_status(test_app);
                // cmtextView.setText(result);
            }
        });
    }

    private void mcl_display_runavg_speed(){
        graphView.mcl_update_graph_data(app_list, test_app, bth, twth, ntwth);
        graphView.startDrawThread();
    }


    private void mcl_get_intent_data(Intent intent) {
        Bundle intentData = intent.getExtras();
        if (intentData.containsKey("AppList"))
            app_list = (Globals.mcl_apps_enum[])intentData.get("AppList");
        if (intentData.containsKey("TestApp"))
            test_app = (Globals.mcl_apps_enum)intentData.get("TestApp");
        if (intentData.containsKey("TD"))
            qtd_status = (boolean)intentData.get("TD");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);
        ptextView = findViewById(R.id.progresstext);
        trdtextView = findViewById(R.id.trdiffstatus);
        cmtextView = findViewById(R.id.ContMod);
        graphView = (DrawGraph) findViewById(R.id.raspeed);
        mcl_init_data();
        mcl_get_intent_data(getIntent());
        if (null == app_list)
            return;
        if (qtd_status){
            /* +MOD_ALGO */
            /**
            new Thread(new Runnable() {
                public void run() {
                    mcl_display_trdiff_status();
                }
            }).start();
             **/
            /* -MOD_ALGO */
        } else {
            Runnable mcl_generate_result_handler = new mcl_generate_result_thread();
            new Thread(mcl_generate_result_handler).start();
            mcl_display_runavg_speed();
            new Thread(new Runnable() {
                public void run() {
                    mcl_wait_for_result_generation();
                    mcl_ini_report();
                    // SystemClock.sleep(500);
                    Runnable mcl_send_report_handler = new mcl_send_report_handler_thread(DisplayResult.report);
                    new Thread(mcl_send_report_handler).start();
                    // mcl_display_trdiff_status();
                    // mcl_display_cont_mod_status();
                    //graphView.mcl_update_graph_data(app_list, test_app, bth, twth, ntwth);
                }
            }).start();
            //mcl_display_runavg_speed();
        }
    }

    /* +BURST */
    private void mcl_goto_home() {
        graphView.stopDrawThread();
        Intent intent = new Intent(this, SelectServiceType.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
    }

    public void mcl_go_back_to_main (View view){
        mcl_goto_home();
    }

    public void onBackPressed() {
        mcl_goto_home();
    }

    public void mcl_open_td_detect_pdf (View view){
        String pdfurl = "https://www.ieor.iitb.ac.in/files/fairnet_td_detect.pdf";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfurl));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(browserIntent);
    }

    private void mcl_put_intent_data(Intent intent){
        int i = 0;
        Globals.mcl_apps_enum[] appl = app_list;
        for (i = 0; i < appl.length; i++)
        {
            if (appl[i].name() != INVALID_APP.name()) {
                intent.putExtra(appl[i].name(), DisplayResult.bth[appl[i].ordinal()]);
            }
        }
        intent.putExtra("AppList", app_list);
        intent.putExtra("TestApp", test_app);
    }

    public static String concatenateStrings(List<String> items)
    {
        if (items == null)
            return null;
        if (items.size() == 0)
            return "";
        int expectedSize = 0;
        for (String item: items)
            expectedSize += item.length();
        StringBuffer result = new StringBuffer(expectedSize);
        for (String item: items)
            result.append(item);
        return result.toString();
    }

    public static class mcl_generate_result_thread implements  Runnable {
        public class res_val {
            int lval = 0;
            BtsInfo[] val;
            res_val (){
                lval = 0;
                val = null;
            }
        }

        private class BtsInfo{
            int size;
            double time;
            void BtsInfo (){
                size = 0;
                time = 0;
            }
        }

        private int[] mcl_get_burst_size_list(Globals.mcl_apps_enum capp){
            int[] res = new int[Globals.MAX_NUM_BURST];
            if (null == RunTest.adownloader)
                return res;
            Globals.BurstData rbdata = RunTest.adownloader[capp.ordinal()].bdata;
            String bdata = concatenateStrings(rbdata.bdata);
            int count = 0;
            Pattern p = Pattern.compile("BURST-END:(\\d+)");
            Matcher m = p.matcher(bdata);
            while (m.find()) {
                String s = m.group();
                String c = s.replaceAll("[^0-9]", "");
                int l = Integer.valueOf(c);
                res[count] = l;
                count++;
                if (Globals.MAX_NUM_BURST < count)
                    break;
            }
            return res;
        }


        res_val mcl_get_ats_list(Globals.mcl_apps_enum capp){
            res_val rval = new res_val();
            if (INVALID_APP == capp)
                return rval;
            if (null == RunTest.adownloader)
                return rval;
            ArrayList cadata = RunTest.adownloader[capp.ordinal()].adata;
            int nadata = RunTest.adownloader[capp.ordinal()].num_adata;
            rval.val = new BtsInfo[nadata];
            Object[] ocadata = cadata.toArray();
            int i = 0;
            for (int j=0; j< nadata; j++)
            {
                if (null == ocadata[i]) {
                    continue;
                }
                BtsInfo cval = new BtsInfo();
                cval.size = ((Globals.AppData)ocadata[j]).size;
                cval.time = ((Globals.AppData)ocadata[j]).time;
                rval.val[i] = cval;
                i++;
            }
            //rval.lval = nadata;
            rval.lval = i-1;
            return rval;
        }


        res_val mcl_get_bts_list(Globals.mcl_apps_enum capp, int[] bs_list){
            res_val rval = new res_val();
            BtsInfo[] res = new BtsInfo[Globals.MAX_NUM_BURST];
            Globals.BurstData cbdata = RunTest.adownloader[capp.ordinal()].bdata;
            Globals.PerBurstInfo[] bt_list = cbdata.b_info;
            int bt_lis_len = cbdata.b_info_count;
            int count = 0;
            int i = 0;
            for (i=0;i<bt_lis_len;i++)
            {
                int j = 0;
                int n = bt_list[i].nbursts;
                for (j=0; j< n; j++)
                {
                    double dtime = bt_list[i].time;
                    int dsize = bs_list[count];
                    res[count] = new BtsInfo();
                    res[count].size = dsize;
                    res[count].time = dtime + 0.000001;
                    count++;
                }
            }
            rval.lval = count-1;
            rval.val = res.clone();
            return rval;
        }

        private void mcl_get_bth_data(Globals.mcl_apps_enum capp, res_val rval){
            int i = 0;
            BtsInfo[] bts_list = rval.val;
            int l_bts_list = rval.lval;
            int datas = 1;
            double dtime = 0;
            double rtime = 0;
            double dth = 0;
            double etime = 0;
            String rep = "App :" + capp + "\n";
            DisplayResult.report += rep;
            double bth[] = new double[l_bts_list];
            for (i=0; i<l_bts_list; i++)
            {
                datas += bts_list[i].size;
                dtime = bts_list[i].time;
                if (i == 0 || dtime == rtime){
                    rtime = dtime;
                    dth = 0;
                } else {
                    if (datas != 0 && dtime != 0) {
                        etime = dtime - rtime;
                        dth = (double)(datas*8) / (etime * 1000);
                    }
                }
                bth[i] = dth;
                rep = i + ":" + dtime + ":" + bts_list[i].size + "\n";
                DisplayResult.report += rep;
            }
            // Globals.bth[capp.ordinal()] = bth;
            DisplayResult.bth[capp.ordinal()] = bth;
            //Globals.th[capp.ordinal()] = dth;
            DisplayResult.th[capp.ordinal()] = dth;
        }

        /* +New algo */
        private void mcl_get_twbth_data(Globals.mcl_apps_enum capp, res_val rval, double stime, double edtime){
            int i = 0;
            boolean s_end = false;
            BtsInfo[] bts_list = rval.val;
            int l_bts_list = rval.lval;
            int datas = 1;
            double dtime = 0;
            double rtime = 0;
            double dth = 0;
            double etime = 0;
            String rep = "App :" + capp + "\n";
            DisplayResult.report += rep;
            double bth[] = new double[l_bts_list];
            int twcount = 0;
            double t_time = edtime - stime;
            Log.d("Total time = ",Double.toString(t_time));
            int max_slot_time = MAX_SLOT_TIME;//(int)t_time/MAX_NUM_TS;
            Log.d("Slots = ",Integer.toString((int)t_time/max_slot_time));
            for (i=0; i<l_bts_list; i++)
            {
                dtime = bts_list[i].time;
                // Log.d("dtime ",Double.toString(dtime));
                rep = i + ":" + dtime + ":" + bts_list[i].size + "\n";
                DisplayResult.report += rep;
                if (dtime < stime || dtime > edtime) {
                    // Log.d("Skipped ", Double.toString(dtime));
                    continue;
                }
                if (i == 0 || dtime == rtime){
                    rtime = dtime;
                    dth = 0;
                    // Log.d("Status","Init reference");
                } else {
                    if (datas != 0 && dtime != 0) {
                        double pdtime = dtime;
                        etime = dtime - rtime;
                        if (etime < max_slot_time)
                        {
                            datas += bts_list[i].size;
                            //Log.d("etime", Double.toString(etime));
                            continue;
                        }
                        /**
                        else {
                            if (!s_end && dtime != pdtime) {
                                datas += bts_list[i].size;
                                s_end = true;
                                continue;
                            } else if(s_end && dtime == pdtime)
                            {
                                datas += bts_list[i].size;
                                continue;
                            }
                        }
                         **/
                        if (0 == max_slot_time)
                        {
                            datas += bts_list[i].size;
                            dth = (double)(datas*8) / (etime * 1000);
                        } else {
                            dth = (double)(datas*8) / (etime * 1000);
                            rtime = dtime;
                        }
                    }
                }
                {
                    bth[twcount] = dth;
                    twth_info res = new twth_info();
                    res.time = dtime;
                    res.twth = dth;
                    DisplayResult.twth[capp.ordinal()][twcount] = res;
                    twcount++;
                    datas = 1;
                    /**
                    s_end = false;
                    datas = bts_list[i].size;
                     **/
                    //Log.d("i",Integer.toString(i));
                }
            }
            // Log.d("twcount" , Integer.toString(twcount));
            ntwth[capp.ordinal()] = twcount;
        }

        private void mcl_get_twath_data(Globals.mcl_apps_enum capp, res_val rval, double stime, double edtime){
            int i = 0;
            boolean s_end = false;
            BtsInfo[] ats_list = rval.val;
            int l_ats_list = rval.lval;
            int datas = 1;
            double dtime = 0;
            double rtime = 0;
            double dth = 0;
            double etime = 0;
            if (null == RunTest.adownloader)
                return;
            if (null == RunTest.adownloader[capp.ordinal()])
                return;
            String rep = "App :" + capp + ":";
            DisplayResult.report += rep;
            rep = RunTest.adownloader[capp.ordinal()].num_cb + ":";
            DisplayResult.report += rep;
            rep = RunTest.adownloader[capp.ordinal()].app_data_len  + "\n";
            DisplayResult.report += rep;
            double ath[] = new double[l_ats_list];
            int twcount = 0;
            double t_time = edtime - stime;
            Log.d("Total time = ",Double.toString(t_time));
            int max_slot_time = MAX_SLOT_TIME;//(int)t_time/MAX_NUM_TS;
            if (0 != max_slot_time)
                Log.d("Slots = ",Integer.toString((int)t_time/max_slot_time));
            for (i=0; i<l_ats_list; i++)
            {
                if (null == ats_list[i])
                    continue;
                dtime = ats_list[i].time;
                // Log.d("dtime ",Double.toString(dtime));
                rep = i + ":" + dtime + ":" + ats_list[i].size + "\n";
                DisplayResult.report += rep;
                if (dtime < stime || dtime > edtime) {
                    // Log.d("Skipped ", Double.toString(dtime));
                    continue;
                }
                if (i == 0 || dtime == rtime || rtime == 0){
                    rtime = dtime;
                    dth = 0;
                    // Log.d("Status","Init reference");
                } else {
                    if (dtime != 0) {
                        double pdtime = dtime;
                        etime = dtime - rtime;
                        if (etime < max_slot_time)
                        {
                            datas += ats_list[i].size;
                            //Log.d("etime", Double.toString(etime));
                            continue;
                        }
                        if (0 == max_slot_time)
                        {
                            datas += ats_list[i].size;
                            dth = (double)(datas*8) / (etime * 1000);
                        } else {
                            dth = (double)(datas*8) / (etime * 1000);
                            rtime = dtime;
                        }
                    }
                }
                {
                    ath[twcount] = dth;
                    twth_info res = new twth_info();
                    res.time = dtime;
                    res.twth = dth;
                    DisplayResult.twth[capp.ordinal()][twcount] = res;
                    twcount++;
                    if (max_slot_time !=0)
                        datas = 0;
                }
            }
            Log.d("twcount" , Integer.toString(twcount));
            ntwth[capp.ordinal()] = twcount;
        }

        double mcl_get_start_time(Globals.mcl_apps_enum[] appl, res_val[] rval)
        {
            double rtime = 0;
            int lapp = rval.length;
            int i = 0;
            for (i=0 ;i< lapp; i++)
            {
                if (null != rval[i])
                {
                    int lrval = rval[i].lval;
                    if ( 0 == rtime ) {
                        rtime = rval[i].val[0].time;
                        Log.d("Value" , "Start : Reference time = " + rtime );
                    }
                    else {
                        for (int j=0; j<lrval; j++){
                            double ctime = rval[i].val[j].time;
                            if (ctime >= rtime){
                                rtime = ctime;
                                Log.d("Value" , "Start : Updated time = " + rtime );
                                break;
                            }
                        }
                    }
                }
            }
            Log.d("Start Time : ", Double.toString(rtime));
            return rtime;
        }

        double mcl_get_end_time(Globals.mcl_apps_enum[] appl, res_val[] rval)
        {
            double rtime = 0;
            int lapp = rval.length;
            int i = 0;
            for (i=0 ;i< lapp; i++)
            {
                // Log.d("Interation = ",Integer.toString(i));
                if (null != rval[i])
                {
                    int lrval = rval[i].lval;
                    if ( 0 == rtime ) {
                        rtime = rval[i].val[lrval - 1].time;
                        Log.d("Value" , "End : Reference time = " + rtime );
                    }
                    else {
                        for (int j=lrval-1; j>0; j--){
                            double ctime = rval[i].val[j].time;
                            // Log.d("End time = ", Double.toString(ctime));
                            if (ctime <= rtime){
                                rtime = ctime;
                                Log.d("Value" , "End : Updated time = " + rtime );
                                break;
                            }
                        }
                    }
                }
            }
            Log.d("End Time : ", Double.toString(rtime));
            return rtime;
        }

        private void mcl_generate_bth_results(){
            int i = 0;
            Globals.mcl_apps_enum[] appl = app_list;
            int lapp = app_list.length;
            res_val[] brval = new res_val[INVALID_APP.ordinal()];
            for (i = 0; i < lapp; i++) {
                brval[i] = null;
                if (appl[i].name() != INVALID_APP.name()) {
                    int[] bs_list = mcl_get_burst_size_list(appl[i]);
                    brval[appl[i].ordinal()] = mcl_get_bts_list(appl[i], bs_list.clone());
                }
            }
            stime = mcl_get_start_time(appl, brval.clone());
            etime = mcl_get_end_time(appl, brval.clone());
            /* -New algo */
            for (i = 0; i < lapp; i++) {
                if (appl[i].name() != INVALID_APP.name()) {
                    /* +New algo */
                    //mcl_get_bth_data(appl[i], brval[appl[i].ordinal()]);
                    mcl_get_twbth_data(appl[i], brval[appl[i].ordinal()], stime, etime);
                    /* -New algo */
                }
            }
        }

        private void mcl_calc_avg_th(Globals.mcl_apps_enum[] appl, res_val[] rval) {
            int lapp = rval.length;
            int i = 0;
            double stime;
            double etime;
            double cth;
            if (null == RunTest.adownloader)
                return;
            for (i = 0; i < lapp; i++) {
                if (!appl[i].name().equals(INVALID_APP.name())) {
                    if (null != rval[appl[i].ordinal()]) {
                        stime = rval[i].val[0].time;
                        etime = rval[i].val[rval[i].lval - 1].time;
                        long dsize = RunTest.adownloader[appl[i].ordinal()].app_data_len*8;
                        cth = dsize/(etime - stime)*1000;
                        ath[appl[i].ordinal()] = cth;
                    }
                }
            }
        }

        private void mcl_generate_ath_results(){
            int i = 0;
            Globals.mcl_apps_enum[] appl = app_list;
            int lapp = app_list.length;
            res_val[] arval = new res_val[INVALID_APP.ordinal()];
            for (i = 0; i < lapp; i++) {
                arval[i] = null;
                //if (!appl[i].name().equals(INVALID_APP.name())) {
                if (appl[i] != INVALID_APP) {
                    arval[appl[i].ordinal()] = mcl_get_ats_list(appl[i]);
                }
            }
            stime = mcl_get_start_time(appl, arval.clone());
            etime = mcl_get_end_time(appl, arval.clone());
            for (i = 0; i < lapp; i++) {
                //if (!appl[i].name().equals(INVALID_APP.name())) {
                if (appl[i] != INVALID_APP) {
                    // mcl_get_bth_data(appl[i], arval[appl[i].ordinal()]);
                    mcl_get_twath_data(appl[i], arval[appl[i].ordinal()], stime, etime);
                }
            }
            mcl_calc_avg_th(appl, arval.clone());
        }
        /* -New algo */

        private void mcl_generate_results() {
            int i = 0;
            Globals.mcl_apps_enum[] appl = app_list;
            int lapp = app_list.length;
            /* +New algo */
            if (APP_BURST == MEAS_MODE)
                mcl_generate_bth_results();
            else if (APP_DATA == MEAS_MODE)
                mcl_generate_ath_results();
            else {
                assert false: "Measurement mode is not set";
            }
            /* +New algo */
            /* +BURST */
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");
            String ctime = sdf.format(new Date());
            String report = "Instance:" + ctime + ":" + Globals.dev.carrier_name + ":" + Globals.dev.country +
                    ":" +  Globals.dev.carrier_addr + "\n";
            DisplayResult.report += report;
            /* -BURST */
        }

        public void run() {
            mcl_generate_results();
            result_ready = true;
        }
    }
    public static class mcl_send_report_handler_thread implements  Runnable {
        String report = null;
        public mcl_send_report_handler_thread (String report){
            this.report = report;
        }
        private static String[] splitToNChar(String text, int size) {
            List<String> parts = new ArrayList<>();

            int length = text.length();
            for (int i = 0; i < length; i += size) {
                parts.add(text.substring(i, Math.min(length, i + size)));
            }
            return parts.toArray(new String[0]);
        }
        private void mcl_send_report_to_server(){
            Downloader app_downloader = new Downloader(INVALID_APP,Globals.server,Globals.port, null);
            app_downloader.mcl_display_sock_error_loop();
            Socket sock = app_downloader.mcl_get_socket(true);
            DataOutputStream sock_out = null;
            // BufferedReader sock_input = null;
            DataInputStream sock_input = null;
            Downloader.mcl_socket sock_api = null;
            try {
                int i = 0;
                sock_out = new DataOutputStream(sock.getOutputStream());
                // sock_input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                sock_input = new DataInputStream(sock.getInputStream());
                sock_api = new Downloader.mcl_socket(sock_input, sock_out);
                String report = this.report;
                String[] sreport = splitToNChar(report, 10000);
                Integer lreport = sreport.length;
                /* Split the report to 10 KB chunks and then send - TBD*/
                for (i=0; i< lreport; i++) {
                    sock_api.mcl_send_socket_data(sreport[i]);
                }
                sock_input.close();
                sock_out.close();
                sock.close();
            } catch (SocketException e) {
                Log.d("ERROR","Socket Error");
            } catch (IOException e) {
                Log.d("ERROR","IO Error");
            } catch (IllegalArgumentException e) {
                Log.d("ERROR","IO Error");
            } catch (SecurityException e) {
                Log.d("ERROR","IO Error");
            } catch (NullPointerException e) {
                Log.d("ERROR","IO Error");
            }
        }
        public void run () {
            Looper.prepare();
            mcl_send_report_to_server();
            Looper.loop();
        }
    }

    private int java_string_hashcode(String s){
        int h = 0;
        char[] ch = s.toCharArray();
        for (char c: ch) {
            h = (31 * h + (int)c) & 0xFFFFFFFF;
        }
        return ((h + 0x80000000) & 0xFFFFFFFF) - 0x80000000;
    }

    private String mcl_get_cont_mod_status(Globals.mcl_apps_enum capp) {
        int i = 0;
        String pat = "Content-Length: 625000";
        String result = "Content modification : Not detected";
        Globals.BurstData rbdata = RunTest.adownloader[capp.ordinal()].bdata;
        String data = concatenateStrings(rbdata.bdata);
        String[] srbdata =  data.split("Content-Length: 625000");
        Integer lsrbdata = srbdata.length;
        Integer[] srbdlen = new Integer[lsrbdata];
        Integer dlen = data.length();
        Integer rhcode = 0;
        for (i=1; i< lsrbdata; i++){
            StringBuilder cdatab = new StringBuilder(pat);
            cdatab = cdatab.append(srbdata[i]);
            String cdata = cdatab.toString();
            if (!cdata.contains("SEG-END"))
                continue;
            String[] ss = cdata.split("SEG-END#");
            ss[0] = ss[0].replace("null","");
            StringBuilder ssdata = new StringBuilder(ss[0]);
            ssdata.append("SEG-END#");
            ss[0] = ssdata.toString().trim().replace("null","");
            if (ss.length > 1) {
                ss[1] = ss[1].trim();
                rhcode = Integer.valueOf(ss[1]);
            }
            srbdlen[i] = ss[0].length();
            int hcode = ss[0].hashCode();
            if (hcode != rhcode){
                result = "Content modification detected";
            }
        }
        return result;
    }

    /* +BURST */
    /**
    private String mcl_get_trdiff_status(){
        int i = 0;
        double thdiff;
        //double th = Globals.th[Globals.test_app.ordinal()];
        double th = DisplayResult.th[test_app.ordinal()];
        double max_th = th;
        Globals.mcl_apps_enum[] appl = app_list;
        String result = "";


        for (i=0; i< INVALID_APP.ordinal(); i++)
        {
            if (appl[i].name() != INVALID_APP.name()) {
                // double cth = Globals.th[i];
                double cth = DisplayResult.th[i];
                if (max_th < cth) {
                    max_th = cth;
                }
                result += appl[i].name() + " : ";
                result += String.format( " %.3f Mbps\n", cth);
            }
        }

        if ( th >= max_th )
            thdiff = 0;
        else {
            thdiff = max_th - th;
            thdiff = (thdiff/th)*100;
        }

        if (thdiff > 1){
            result += String.format("%.2f", thdiff);
            result += "% Traffic differentiation detected";
        } else {
            result += "Traffic differentiation not detected";
        }

        return result;
    }
     **/
    /* +New Algo */
    private class mcl_detect_td_thr
    {
        Globals.mcl_apps_enum[] appl;
        int ntwbth;
        twth_info[][] twbth;
        app_res[] ares;

        private class app_res{
            int nlow;
            int nsame;
            app_res()
            {
                nlow = 0;
                nsame = 0;
            }
        }

        mcl_detect_td_thr(Globals.mcl_apps_enum[] appl, int ntwbth, twth_info[][] twbth){
            this.appl = appl;
            this.ntwbth = ntwbth;
            this.twbth = twbth;
            this.ares = new app_res[INVALID_APP.ordinal()];
        }

        private int mcl_get_slot_status(Globals.mcl_apps_enum tapp){
            int n_l = 0;
            for (int i=0; i< ntwbth; i++) {
                double hth = 0;
                double tth = 0;
                for (Globals.mcl_apps_enum app : appl) {
                    if (app.name() != INVALID_APP.name()) {
                        // Log.d("App", app.toString());
                        // Log.d("Interation", Integer.toString(i));
                        double cth = this.twbth[app.ordinal()][i].twth;
                        if (app == tapp)
                            tth = cth;
                        if (cth > hth)
                            hth = cth;
                    }
                }
                //Log.d("Tth = ",Double.toString(tth));
                //Log.d("Hth = ",Double.toString(hth));
                if (hth - tth > 1)
                    n_l++;
            }
            // Log.d("n_l = ",Integer.toString(n_l));
            return n_l;
        }

        private int mcl_get_same_perf_status(){
            int n_s = 0;
            int tn_l = this.ares[test_app.ordinal()].nlow;
            for (Globals.mcl_apps_enum app : appl) {
                if (app.name() != INVALID_APP.name() && app != test_app) {
                    int cn_l = this.ares[app.ordinal()].nlow;
                    if (tn_l - 2 < cn_l && cn_l < tn_l + 2)
                        n_s++;
                }
            }
            return n_s;
        }

        private void mcl_get_app_slot_status()
        {
            for (Globals.mcl_apps_enum app : appl) {
                if (app.name() != INVALID_APP.name()) {
                    this.ares[app.ordinal()] = new app_res();
                    this.ares[app.ordinal()].nlow = mcl_get_slot_status(app);
                }
            }
            this.ares[test_app.ordinal()].nsame = mcl_get_same_perf_status();
        }

        /* +MOD_ALGO */
        private boolean mcl_detect_td(){
            boolean result;
            /* -MOD_ALGO */
            int n_l = this.ares[test_app.ordinal()].nlow;
            int n_s = this.ares[test_app.ordinal()].nsame;
            if (n_l >= 0.8 * ntwbth)
                /* +MOD_ALGO */
                result = true;
                /* -MOD_ALGO */
            else if (n_l >= sl* ntwbth && n_l < hl * ntwbth) {
                if (n_s >= 1)
                    /* +MOD_ALGO */
                    result = false;
                    /* -MOD_ALGO */
                else
                    /* +MOD_ALGO */
                    result = true;
                /* -MOD_ALGO */
            } else
                /* +MOD_ALGO */
                result = false;
            /* -MOD_ALGO */
            return result;
        }

        /* +MOD_ALGO */
        private boolean run()
        {
            boolean result;
            /* -MOD_ALGO */
            mcl_get_app_slot_status();
            result = mcl_detect_td();
            return result;
        }
    }

    /* +MOD_ALGO */
    private class mcl_detect_td_cs {
        int num_cb;

        mcl_detect_td_cs(int num_cb){
            this.num_cb = num_cb;
        }

        private boolean mcl_detect_td() {
            boolean result = false;
            if (MAX_NUM_CB <= this.num_cb)
                result = true;
            return result;
        }

        private boolean run() {
            boolean result = mcl_detect_td();
            return  result;
        }
    }
    /* -MOD_ALGO */

    private class mcl_detect_td_range
    {
        Globals.mcl_apps_enum[] appl;
        int ntwbth;
        twth_info[][] twbth;
        ArrayList dranges;

        mcl_detect_td_range(Globals.mcl_apps_enum[] appl, int ntwbth, twth_info[][] twbth){
            this.appl = appl;
            this.ntwbth = ntwbth;
            this.twbth = twbth;
            this.dranges = null;
        }

        private void mcl_get_dranges(){
            double drange = 0;
            dranges = new ArrayList(0);
            double chigh = 0;
            double clow = 0;
            double htstart = 0;
            double ltstart = 0;
            boolean hdetect = false;
            boolean ldetect = false;

            for (int i=0; i< ntwbth; i++) {
                double cth = this.twbth[test_app.ordinal()][i].twth;
                double ctime = this.twbth[test_app.ordinal()][i].time;
                if (chigh <= cth) {
                    chigh = cth;
                    hdetect = true;
                    htstart = ctime;
                    if (ldetect) {
                        if (chigh - clow >= MIN_TH_RANGE) {
                            drange = ctime - ltstart;
                            Log.d("", "(L-H)drange = " + Double.toString(drange));
                            if (drange <= MAX_TIME_RANGE) {
                                //Log.d("drange = ",Double.toString(drange));
                                dranges.add(drange);
                            }
                            clow = 0;
                            ldetect = false;
                            ltstart = 0;
                        }
                    } else if (0 == chigh) {
                        //Log.d("htRestart",Double.toString(htstart));
                        ldetect = false;
                        clow = 0;
                    }
                } else {
                    clow = cth;
                    ldetect = true;
                    ltstart = ctime;
                    if (hdetect) {
                        if (chigh - clow >= MIN_TH_RANGE) {
                            drange = ctime - htstart;
                            Log.d("","(H-L)drange = "+Double.toString(drange));
                            if (drange <= MAX_TIME_RANGE) {
                                // Log.d("drange = ",Double.toString(drange));
                                dranges.add(drange);
                            }
                            chigh = 0;
                            hdetect = false;
                            htstart = 0;
                        }
                    } else if (0 == clow) {
                        // Log.d("ltRestart",Double.toString(ltstart));
                        hdetect = false;
                        chigh = 0;
                    }
                }
            }
        }

        private String mcl_detect_td(){
            String result = "";
            double tdrange = 0;
            double fdrange = 0;
            double t_total = etime - stime;
            int ldranges = dranges.size();
            for (int i=0; i< ldranges; i++) {
                double drange = (double) dranges.get(i);
                tdrange += drange;
            }
            Log.d("t_total",Double.toString(t_total));
            Log.d("tdrange",Double.toString(tdrange));
            fdrange = tdrange/t_total;
            Log.d("fdrange",Double.toString(fdrange));
            if (MIN_NORM_TIME_RANGE <= fdrange)
                result = "Traffic differentiation : Deteted";
            else
                result = "";
            return result;
        }

        private String run()
        {
            String result;
            mcl_get_dranges();
            result = mcl_detect_td();
            return result;
        }
    }


    private int mcl_get_min_list_size(Globals.mcl_apps_enum[] appl, int[] ntwbth)
    {
        int ncount = 0;
        for (Globals.mcl_apps_enum app : appl) {
            if (app.name() != INVALID_APP.name()) {
                int ltwbth = ntwbth[app.ordinal()];
                // Log.d("ltwbth",Integer.toString(ltwbth));
                if (0 == ncount || ncount > ltwbth)
                    ncount = ltwbth;
            }
        }
        return ncount;
    }

    private String mcl_get_truc_file_decision(boolean cs)
    {
        String result = "Traffic differentiation not detected";;
        String[] result_map = new String[2];
        result_map[0] = "Traffic differentiation not detected";
        result_map[1] = "Traffic differentiation detected";
        int i = 0;
        int tapp = 0;
        int papp = 0;
        Globals.mcl_apps_enum[] appl = app_list;
        int lapp = app_list.length;
        for (i = 0; i < lapp; i++) {
            if (appl[i].name() != INVALID_APP.name()) {
                long dlen = RunTest.adownloader[test_app.ordinal()].app_data_len;
                if (MAX_DATA_SIZE >= dlen){
                    tapp++;
                }
                papp++;
            }
        }
        if (tapp == papp)
            result = "Network speed too bad for measurement";
        else
            result = result_map[cs ? 1 : 0];
        return result;
    }

    private  String  mcl_get_td_status(boolean thr, boolean cs)
    {
        String result = " Traffic differentiation not detected";
        String[] result_map = new String[2];
        result_map[0] = " Traffic differentiation not detected";
        result_map[1] = " Traffic differentiation detected";
        int tapp = 0;
        int papp = 0;
        /* +MOD_ALGO */
        mcl_detect_td_cs td_cs = new mcl_detect_td_cs(RunTest.adownloader[test_app.ordinal()].num_cb);
        //boolean thr;
        //boolean cs;
        /* -MOD_ALGO */

        /* +MOD_ALGO */
        Globals.mcl_apps_enum[] appl = app_list;
        int lapp = app_list.length;
        for (int i = 0; i < lapp; i++) {
            if (appl[i].name() != INVALID_APP.name()) {
                long dlen = RunTest.adownloader[test_app.ordinal()].app_data_len;
                if (MAX_DATA_SIZE >= dlen){
                    tapp++;
                }
                papp++;
            }
        }
        if (tapp == papp) {
            result = "Network speed too bad for measurement";
            // Globals.dev.debug += "Partial download for all services ";
        }
        else {
            //thr = td_thr.run();
            //cs = td_cs.run();

            if (thr){
                result = result_map[thr ? 1 : 0];
                Log.d("Status", "Threshold detector ");
                // Globals.dev.debug += "THR : "+thr + " CS : False";
            }
            if (!thr && !cs) {
                // Globals.dev.debug += "False reported by both algo";
                return result;
            }
            if (!thr && cs) {
                // Globals.dev.debug += "THR : False" + " CS : True";
                long dlen = RunTest.adownloader[test_app.ordinal()].app_data_len;
                if (MAX_DATA_SIZE >= dlen){
                    // Globals.dev.debug += "Partial download for test service";
                    result = result_map[cs ? 1 : 0];
                    Log.d("Status", "CS detector ");
                } else {
                    // Globals.dev.debug += "Full download for test service";
                    result = result_map[thr ? 1 : 0];
                    Log.d("Status", "Threshold detector ");
                }
                return result;
            }

            /**
            if (!thr && !cs) {
                Globals.dev.debug += "False reported by both algo";
                return result;
            }
            if (thr && cs) {
                Globals.dev.debug += "True reported by both algo";
                result = " Traffic differentiation detected";
            }
            else {
                if (!cs) {
                    result = result_map[thr ? 1 : 0];
                    Log.d("Status", "Threshold detector ");
                    Globals.dev.debug += "THR : "+thr + " CS : False";
                }
                else
                {
                    Globals.dev.debug += "CS : True";
                    long dlen = RunTest.adownloader[test_app.ordinal()].app_data_len;
                    if (MAX_DATA_SIZE >= dlen){
                        Globals.dev.debug += "Partial download for test service";
                        result = result_map[cs ? 1 : 0];
                        Log.d("Status", "CS detector ");
                    } else {
                        Globals.dev.debug += "Full download for test service";
                        result = result_map[thr ? 1 : 0];
                        Log.d("Status", "Threshold detector ");
                    }
                }
            }
             **/
        }
        return  result;
    }

    private String mcl_get_trdiff_status(){
        String result = " Traffic differentiation : Not detected";
        String[] result_map = new String[2];
        result_map[0] = " Traffic differentiation : Not detected";
        result_map[1] = " Traffic differentiation : Detected";
        int tapp = 0;
        int papp = 0;
        twth_info[][] twbth = DisplayResult.twth;
        twth_info[][] twath = DisplayResult.twth;
        Globals.mcl_apps_enum[] appl = app_list;
        int ntwbth = mcl_get_min_list_size(appl, DisplayResult.ntwth);
        mcl_detect_td_thr td_thr = new mcl_detect_td_thr(appl, ntwbth, twbth);
        // mcl_detect_td_range td_range = new mcl_detect_td_range(appl, ntwbth, twbth);
        /* +MOD_ALGO */
        Log.d("Status","Test App : " + test_app.toString());
        if (null == RunTest.adownloader) {
            result = "Download error, Please try again";
            return result;
        }
        if (test_app == INVALID_APP || null == RunTest.adownloader[test_app.ordinal()]) {
            result = "Test app error, Please try again";
            return result;
        }
        int cnum_cb = RunTest.adownloader[test_app.ordinal()].num_cb;
        mcl_detect_td_cs td_cs = new mcl_detect_td_cs(cnum_cb);
        boolean thr = false;
        boolean cs = false;
        /* -MOD_ALGO */

        int lapp = app_list.length;
        for (int i = 0; i < lapp; i++) {
            if (appl[i].name() != INVALID_APP.name()) {
                long dlen = RunTest.adownloader[appl[i].ordinal()].app_data_len;
                if (MAX_DATA_SIZE >= dlen){
                    tapp++;
                }
                papp++;
            }
        }
        if (tapp == papp) {
            result = "Network speed too bad for measurement";
            // Globals.dev.debug += "Partial download for all services ";
        }
        else {
            thr = td_thr.run();
            cs = td_cs.run();

            if (thr) {
                result = result_map[thr ? 1 : 0];
                Log.d("Status", "Threshold detector ");
                // Globals.dev.debug += "THR : " + thr + " CS : False";
            }
            /*
            if (!thr && !cs) {
                // Globals.dev.debug += "False reported by both algo";
            }
             */
            if (!thr && cs) {
                // Globals.dev.debug += "THR : False" + " CS : True";
                long dlen = RunTest.adownloader[test_app.ordinal()].app_data_len;
                if (MAX_DATA_SIZE >= dlen) {
                    // Globals.dev.debug += "Partial download for test service";
                    result = result_map[cs ? 1 : 0];
                    Log.d("Status", "CS detector ");
                } else {
                    // Globals.dev.debug += "Full download for test service";
                    result = result_map[thr ? 1 : 0];
                    Log.d("Status", "Threshold detector ");
                }
            }
        }

        /*
        thr = td_thr.run();
        cs = td_cs.run();
        result = td_thr.run();
        if (result.equals(""))
            result = td_range.run();
        if (result.equals(""))
            result = "Traffic differentiation not detected";
        // Result = Get decision from table
        result = mcl_get_td_status(is_td_thr, is_td_cs);
        */
        DisplayResult.report += result + "\n";
        return result;
    }
    /* -New Algo */
    /* -BURST */
}
