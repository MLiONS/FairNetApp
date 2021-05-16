package com.iitb.fairnet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.iitb.fairnet.Globals.mcl_gloc_enum.INVALID_GLOC;

public class Globals {
    public static class DeviceInfo {
        String carrier_addr;
        String carrier_name;
        String country;
        Globals.mcl_gloc_enum gloc;
        String debug;
        DeviceInfo() {
            carrier_addr = "0.0.0.0";
            carrier_name = " NA";
            country = "NA";
            gloc = INVALID_GLOC;
            debug = "DEBUG:";
        }
    }

    public static class ServerInfo {
        String server;
        int port;
        int in_port;
        public ServerInfo() {
            this.server = "0.0.0.0";
            this.port = 443;
            this.in_port = 80;
        }
    }

    public static class AppData {
        int size;
        double time;
        AppData (){
            size = 0;
            time = 0;
        }
    }

    public static class PerBurstInfo {
        int nbursts;
        double time;
        public PerBurstInfo (){
            nbursts = 0;
            time = 0;
        }
    }
    public static class BurstData {
        int b_info_count = 0;
        PerBurstInfo[]  b_info;
        public List<String> bdata;
        public BurstData() {
            this.b_info_count = 0;
            this.bdata  = new ArrayList<String>();
            this.b_info = new PerBurstInfo[MAX_NUM_BURST];
        }
    }

    public static  final long MAX_SEG_LEN = 625000;
    public static final String app_id = UUID.randomUUID().toString();
    public static final String server = "s3.ieor.iitb.ac.in";
    //public static final String server = "10.0.2.2";
    // public static final String server = "34.93.220.209";
    //public static final String server = "192.168.0.13";
    // public static final String server = "192.168.43.28";
    // public static final String server = "192.168.42.40";
    public static final int port = 8084;
    public static final double sl = 0.6;
    public static final double hl = 0.8;
    //public static final int port = 443;
    public static final int MAX_DOWNLOAD_TIME = 3*60*1000;
    public static final int MAX_DATA_SIZE = 625000*32; // 12500000 b
    public static final int MAX_NUM_BURST = 20000;
    public static final boolean DEBUG = false;
    public  static final long MAX_NUM_SEG = MAX_DATA_SIZE/MAX_SEG_LEN;
    public static final int N_RETRY = 3;
    public static final int MAX_SLOT_TIME = 0;//1000; // Org : 750
    public static final mcl_meas_mode_enum MEAS_MODE = mcl_meas_mode_enum.APP_DATA;
    public static final int MAX_SPEED = 50; // Percentage of 10 Mbps; 50% to 5 Mbps
    public static final int MAX_SOCKET_TIMEOUT = 12*1000;
    public static int MAX_OTH_SERV = 2;
    public static int NETWORK_NONE = 255;
    /* +TD Range detect params */
    public static final int MIN_TH_RANGE = 1;
    public static final int MAX_TIME_RANGE = 2500;
    public static final double MIN_NORM_TIME_RANGE = 0.5;
    public static String net_name = "";
    public static final int MAX_NUM_CB = 5;
    /* -TD Range detect params */
    public static DeviceInfo dev = new DeviceInfo();
    public static Context tsContext;
    public enum mcl_meas_mode_enum {
        APP_DATA,
        APP_BURST,
        INVALID_MEAS
    }
    public enum mcl_apps_enum {
        MCL_MIN_VID_SERVICE_ID,
        HOTSTAR,
        NETFLIX,
        YOUTUBE,
        PRIMEVIDEO,
        MXPLAYER,
        HUNGAMA,
        ZEE5,
        VOOT,
        EROSNOW,
        SONYLIV,
        MCL_MAX_VID_SERVICE_ID,
        MCL_MIN_AUD_SERVICE_ID,
        WYNK,
        GAANA_COM,
        SAAVN,
        SPOTIFY,
        PRIMEMUSIC,
        GPLAYMUSIC,
        MCL_MAX_AUD_SERVICE_ID,
        INVALID_APP
    }

    public enum mcl_gloc_enum {
        AFRICA,
        AMERICA,
        ASIA,
        AUSTRALIA,
        EUROPE,
        INVALID_GLOC
    }

    public static int mcl_gen_random_number(int min, int max){
        final int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }

    public static void mcl_run_test(Globals.mcl_apps_enum[] app_list,
                              Globals.mcl_apps_enum test_app,
                              double speed,
                              Globals.mcl_gloc_enum gloc,
                              Activity cActivity, Handler handler){
        RunTest rt = new RunTest(app_list, test_app, speed, gloc,cActivity,handler);
        rt.start();
    }

    // public static mcl_apps_enum[] app_list = null;
    public static void mcl_set_socket_error_msg (Handler handler){
        handler = new Handler(Looper.getMainLooper());
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("Connection Error", "Connection Error");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static void mcl_print_toast (Handler handler, Context cContext, String msg) {
        if (null != handler) {
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(cContext.getApplicationContext(),
                            msg,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public Globals(){
    }

}