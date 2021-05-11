package com.iitb.fairnet;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static com.iitb.fairnet.Globals.MAX_SOCKET_TIMEOUT;
import static com.iitb.fairnet.Globals.MEAS_MODE;
import static com.iitb.fairnet.Globals.mcl_meas_mode_enum.APP_BURST;
import static com.iitb.fairnet.Globals.mcl_meas_mode_enum.APP_DATA;
import static com.iitb.fairnet.Globals.tsContext;
import static java.lang.StrictMath.floorMod;
import static java.lang.Thread.sleep;

import android.widget.Toast;

public class Downloader {
    private Globals.mcl_apps_enum app = Globals.mcl_apps_enum.INVALID_APP;
    private String rserver = null;
    private int rport = 0xFFFF;
    public Socket sock = null;
    /* +3MIN */
    boolean dcomp;
    /* -3MIN */
    public DataOutputStream sock_out = null;
    public DataInputStream sock_input = null;
    // private BufferedReader sock_input = null;
    RunTest.RunTestInfo runTestInfo = null;
    public long app_data_len;
    public Globals.BurstData bdata;
    /* +New Algo */
    public ArrayList adata;
    public int num_adata;
    /* -New Algo */
    int num_req;
    long ctime;
    long ptime;
    int num_cb;
    long dlen;
    private Handler handler = new Handler();

    public Downloader(Globals.mcl_apps_enum capp, String crserver, int crport, RunTest.RunTestInfo runTestInfo) {
        int i = 0;
        this.app = capp;
        this.rserver = crserver;
        this.rport = crport;
        this.runTestInfo = runTestInfo;
        this.sock = null;
        this.sock_out = null;
        this.sock_input = null;
        this.app_data_len = 0;
        this.bdata = new Globals.BurstData();
        this.num_req = 0;
        this.ctime = 0;
        this.ptime = 0;
        /* +New Algo */
        this.adata = new ArrayList(0);
        this.num_adata = 0;
        /* -New Algo */
        /* +MOD_ALGO */
        this.num_cb = 0;
        /* -MOD_ALGO */
        /* +3MIN */
        dcomp = false;
        /* -3MIN */
        dlen = 0;
    }

    /* +Burst */
    /*
    private void mcl_store_sock_info(Globals.mcl_apps_enum app){
        Globals.app_sock[app.ordinal()] = new Globals.AppSockInfo();
        Globals.app_sock[app.ordinal()].sock = this.sock;
        Globals.app_sock[app.ordinal()].sock_out = this.sock_out;
        Globals.app_sock[app.ordinal()].sock_input = this.sock_input;
    }
    */
    private SSLContext mcl_get_ssl_context(){
        Context context = MainActivity.context;
        SSLContext ssl_context = null;
        try {
            ssl_context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e){
            Log.e(this.getClass().toString(), "Exception", e);
        }
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.netflix);
            trustStore.load(trustStoreStream, "1234567".toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            ssl_context.init(null, trustManagerFactory.getTrustManagers(), null);
        } catch (GeneralSecurityException e) {
            Log.e(this.getClass().toString(), "Exception while creating context: ", e);
        } catch (IOException e) {
            Log.e(this.getClass().toString(), "Exception", e);
        }
        return ssl_context;
    }


    private String mcl_get_sni(Globals.mcl_apps_enum capp){
        String sni = "INVALID";
        switch (capp){
            case NETFLIX:
                sni = "ipv4-c004-bom001-hathway-isp.1.oca.nflxvideo.net";
                break;
            case YOUTUBE:
                sni = "r2---sn-i5uif5t-cvhl.googlevideo.com";
                break;
            case HOTSTAR:
                sni = "www.hotstar.com";
                break;
            case PRIMEVIDEO:
                sni = "d25xi40x97liuc.cloudfront.net";
                break;
            case MXPLAYER:
                sni = "media-content.akamaized.net";
                break;
            case HUNGAMA:
                sni = "content1.hungama.com";
                break;
            case ZEE5:
                sni = "akamaividz2.zee5.com";
                break;
            case VOOT:
                sni = "vootvideo.akamaized.net";
                break;
            case EROSNOW:
                sni = "tvshowhls-b.erosnow.com";
                break;
            case SONYLIV:
                sni = "securetoken.sonyliv.com";
                break;
            case WYNK:
                sni = "desktopsecurehls-vh.akamaihd.net";
                break;
            case SAAVN:
                sni = "aa.cf.saavncdn.com";
                break;
            case SPOTIFY:
                sni = "audio4-aki-spotify-com.akamaized.net";
                break;
            case GAANA_COM:
                sni = "a10.gaanacdn.com";
                break;
            case PRIMEMUSIC:
                sni = "dfqzuzzcqflbd.cloudfront.net";
                break;
            case GPLAYMUSIC:
                sni = "music-pa.clients6.google.com";
                break;
            default:
                break;
        }
        return sni;
    }


    protected Socket mcl_get_ssl_socket(String rserver, int rport) throws IOException  {
        SSLContext ssl_context = mcl_get_ssl_context();
        SSLSocketFactory factory = ssl_context.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(rserver, rport);
        //Log.d("Status", "rserver = " + rserver);
        SSLParameters sslParameters = socket.getSSLParameters();
        String sni = mcl_get_sni(this.app);
        SNIHostName sniHostName = new SNIHostName(sni);
        List<SNIServerName> sniHostNameList = new ArrayList<>(1);
        sniHostNameList.add(sniHostName);
        sslParameters.setServerNames(sniHostNameList);
        socket.setSSLParameters(sslParameters);
        socket.setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
        return socket;
    }

    public void mcl_set_socket_error_msg (){
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("Connection Error", "Connection Error");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void mcl_display_sock_error_loop () {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(tsContext, "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }

    public Socket mcl_get_socket(boolean is_ssl){
        /* -BURST */
        Socket sock = null;
        while (null == sock) {
            try {
                // sock = new Socket("34.93.220.209",80);
                // sock = new Socket("10.0.2.2",8084);
                if (is_ssl && 80 != this.rport)
                    sock = mcl_get_ssl_socket(this.rserver, this.rport);
                else
                    sock = new Socket(this.rserver, this.rport);

                sock.setKeepAlive(true);
                sock.setSoTimeout(MAX_SOCKET_TIMEOUT);

                /****
                 this.sock_out = new DataOutputStream(sock.getOutputStream());
                 this.sock_input =  new BufferedReader(new InputStreamReader(sock.getInputStream()));
                 ***/
                /* -BURST */
                //new DataInputStream(sock.getInputStream());
            } catch (SocketException e) {
                // mcl_dsplay_socket_error();
                Log.d("ERROR", "Socket Error");
                mcl_set_socket_error_msg();
            } catch (IOException e) {
                Log.d("ERROR", "IO Error");
                mcl_set_socket_error_msg();
            } catch (IllegalArgumentException e) {
                Log.d("ERROR", "IllegalArgumentException");
                mcl_set_socket_error_msg();
            } catch (SecurityException e) {
                Log.d("ERROR", "SecurityException");
                mcl_set_socket_error_msg();
            } catch (NullPointerException e) {
                Log.d("ERROR", "NullPointerException");
                mcl_set_socket_error_msg();
            }
            if (null == sock) {
                try {
                    sleep(250);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
        return sock;
    }
    /* -Burst */

    private Socket mcl_setup_comm_channel(){
        //InetAddress serv = InetAddress.getByName(rserver);
        Socket sock = mcl_get_socket(true);
        /* +BURST */
        this.sock = sock;
        // Log.d("Status", "Socket = " + sock);
        try {
            this.sock_out = new DataOutputStream(sock.getOutputStream());
            this.sock_input = new DataInputStream(sock.getInputStream());
            // this.sock_input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            // Log.d("Status", "Socket o/p = " + this.sock_out);
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
        // mcl_store_sock_info(this.app);
        /* -BURST */
        return sock;
    }


    /* +BURST */
    public static class mcl_socket_data {
        long sdlen;
        String sd;
        public mcl_socket_data(){
            sdlen = 0;
            sd = null;
        }
    }

    public static class mcl_socket{
        /* -BURST */
        private DataOutputStream sock_out = null;
        // BufferedReader sock_input = null;
        private DataInputStream sock_input = null;

        public mcl_socket (/* BufferedReader */ DataInputStream  in,  DataOutputStream out){
            this.sock_input = in;
            this.sock_out = out;
        }
        public void mcl_send_socket_data(String data){
            if (null != data) {
                try {
                    /* +BURST */
                    // Log.d("Status", "Req data = " + data);
                    byte[] bytes = data.getBytes("UTF-8");
                    // Log.d("Status", "Req bytes = " + bytes + " Socket out = "+this.sock_out);
                    this.sock_out.write(bytes);
                    // this.sock_out.writeUTF(data);
                    /* -BURST */
                } catch (IOException e) {
                    //Log.d("ERROR","IO Error in sock_out");
                }
            }
        }

        /* +Web-App-Server */
        public mcl_socket_data mcl_receive_socket_data(int bsize){
            /* -Web-App-Server */
            mcl_socket_data res = new mcl_socket_data();
            int dlen = 0;
            String data;
            try {
                //final int bsize = 700000;
                byte[] buffer = new byte[bsize];
                /* +MOD_ALGO */
                /**
                if (0 == this.sock_input.available()) {
                    dlen = -1;
                } else
                    **/
                {
                    dlen = this.sock_input.read(buffer);
                }
                /* -MOD_ALGO */

                if (dlen == -1) {
                    res.sdlen = dlen;
                    res.sd = null; //.trim().replace("\n","");
                } else if (0 == dlen){
                    res.sdlen = 0;
                    res.sd = null;
                } else {
                    data = new String(buffer, "UTF-8");
                    res.sdlen = dlen;
                    res.sd = data; //.trim().replace("\n","");
                    if (bsize > dlen) {
                        res.sd = data.substring(0, dlen);
                    }
                }
            } catch (IOException e) {
                res.sdlen = -1;
                res.sd = null;
            }
            return res;
        }
    }

    private void mcl_send_hget_req(String type){
        String req = null;
        mcl_socket sock_api = new mcl_socket(this.sock_input,this.sock_out);
        if (type == "INITIAL")
            req = this.runTestInfo.mcl_app_ini_hget_req_map[this.app.ordinal()];
        else if (type == "FINAL")
            req = this.runTestInfo.mcl_app_fin_hget_req;
        else
            req = this.runTestInfo.mcl_app_hget_req_map[this.app.ordinal()];
        // Log.d("Status", this.app + "Init req = " + req);
        sock_api.mcl_send_socket_data(req);
    }

    /* +MOD */
    private long mcl_get_sdlen(String data)
    {
        long len = 0;

        String slen;
        Pattern pattern = Pattern.compile("Content-Length: (\\d{6})");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            slen = matcher.group(1);
            len = Long.parseLong(slen);
        }

        return len;
    }
    /* -MOD */


    private int java_string_hashcode(String s){
        int h = 0;
        char[] ch = s.toCharArray();
        for (char c: ch) {
            h = (31 * h + (int)c) & 0xFFFFFFFF;
        }
        return ((h + 0x80000000) & 0xFFFFFFFF) - 0x80000000;
    }

    private long mcl_receive_app_data(int max_data_size){
        String data = null;
        mcl_socket sock_api = new mcl_socket(this.sock_input,this.sock_out);
        long dctime = 0;
        int c = 0;
        int bcount = 0;
        Globals.mcl_apps_enum capp = this.app;
        long tlen = 0;
        int rlen = 0;
        /* +MOD */
        long sdlen = 0;
        int zdlen = 0;
        // Globals.app_data_len[capp.ordinal()] =  0;
        /* -MOD */
        /* +BURST */
        int datal;
        if (null == bdata){
            //Log.d("Status","App ="+this.app);
            return tlen;
        }
        Globals.BurstData bdata = this.bdata;
        if (null == bdata){
            //Log.d("Status","App ="+this.app);
            return tlen;
        }
        /* -BURST */

        /* +New Algo */
        ArrayList ladata = this.adata;
        /* -New Algo */

        int i = 1;
        while (true){
            try {
                if (0 == this.sock_input.available()) {
                    int dlen = 0;
                }
            } catch (IOException e) {
                Log.d("Status","I/O exception in AVAILABLE");
            }
            /* +BURST */
            mcl_socket_data sdata = sock_api.mcl_receive_socket_data(70000);
            data = sdata.sd;
            // Log.d("Value", capp + ": Received data length = " + Long.toString(sdata.sdlen));

            /* +MOD_ALGO */
            long temp_sd_len;
            {
                // long ttlen = floorMod(tlen, 5000000);
                boolean debug = false;
                if (debug && 312501*i < tlen && 0 != tlen) {
                    // tlen += sdata.sdlen;
                    sdata.sdlen = -1;
                    i++;
                }
            }
            /* -MOD_ALGO */

            /* +MOD_ALGO */
            if (sdata.sdlen == -1) {
                tlen = -1;
                this.num_cb++;
                break;
            }
            /* +MOD_ALGO */

            zdlen = 0;
            rlen = (int)sdata.sdlen;

            /* -BURST */
            /* +MOD */
            if (0 == sdlen && null != data)
            {
                sdlen = mcl_get_sdlen(data);
                if (625000 > sdlen )
                    continue;
            }
            /* -MOD */

            if (data != null)
            {
                int l = 0;
                /*
                Pattern pattern = Pattern.compile("SEG-END");
                Matcher matcher = pattern.matcher(data);
                if (matcher.find()) {
                    rlen = rlen - "0000000000".length();
                    seg_fin = true;
                }
                */
                /* +BURST */
                dctime = System.currentTimeMillis();
                 if (APP_BURST == MEAS_MODE) {
                     bcount = bdata.b_info_count;
                     String pat = "BURST-END:";
                     int nbursts = (data.split(pat, -1).length) - 1;
                     if (bcount > Globals.MAX_NUM_BURST)
                         break;
                     Globals.PerBurstInfo b_info = new Globals.PerBurstInfo();
                     b_info.time = dctime;
                     b_info.nbursts = nbursts;
                     bdata.b_info[bcount] = b_info;
                     bdata.bdata.add(data);
                     bcount++;
                     bdata.b_info_count = bcount;
                 }
                /* -BURST */
                app_data_len += rlen;
                dlen = rlen;
                /* +New Algo */
                Globals.AppData cladata = new Globals.AppData();
                cladata.size = rlen;
                cladata.time = dctime;
                ladata.add(cladata);
                this.num_adata++;
                /* -New Algo */
                //ptime = ctime;
                ctime = dctime;
                tlen += rlen;
            }
            else
                continue;
            //Log.d("Status", this.app + ": Data received = " + tlen + " Bytes");
            if (/* seg_fin == true || */tlen >= sdlen /* +MOD */ || tlen >= max_data_size /* -MOD */) {
                // Log.d("Status", this.app + ": Seg Data received =" + bdata.bdata.length() + " Bytes");
                // Log.d("Status", "Tlen = " + tlen);
                break;
            }
        }
        /* +MOD */
        return tlen;
        /* -MOD */
    }

    private void mcl_close_socket(){
        try {
            this.sock_input.close();
            this.sock_out.close();
            this.sock.close();
        } catch (SocketException e) {
            //Log.d("ERROR", "Socket Error");
        } catch (IOException e) {
            //Log.d("ERROR", "IO Error");
        }
    }

    private long mcl_restart_download(){
        long adata = 0;
        Log.d("Status","Download Restarted");
        // Globals.dev.debug += "\nDownload Restarted for " + app + "\n";
        while (-1 != adata && !dcomp) {
            mcl_close_socket();
            mcl_setup_comm_channel();
            mcl_socket sock_api = new mcl_socket(this.sock_input,this.sock_out);
            Log.d("status","Socket created for "+this.app.name());
            mcl_send_hget_req("INITIAL");
            mcl_socket_data sdata = new mcl_socket_data();
            sdata.sdlen = 0;
            while (true) {
                sdata = sock_api.mcl_receive_socket_data(2000);
                if (null != sdata.sd) {
                    String response = sdata.sd.replace("\'", "").trim();
                    if (response.equals("OK")) break;
                }
            }
            Log.d("Status ", "OK received for " + this.app.name());
            mcl_send_hget_req("NORMAL");
            adata = mcl_receive_app_data(Globals.MAX_DATA_SIZE);
        }
        return adata;
    }

    private long mcl_download_app_data (){
        long app_data = 0;
        mcl_socket sock_api = new mcl_socket(this.sock_input,this.sock_out);
        Log.d("status","Socket created for "+this.app.name());
        try {
            mcl_send_hget_req("INITIAL");
            mcl_socket_data sdata = new mcl_socket_data();
            sdata.sdlen = 0;
            while (true) {
                sdata = sock_api.mcl_receive_socket_data(2000);
                if (null != sdata.sd) {
                    String response = sdata.sd.replace("\'", "").trim();
                    if (response.equals("OK")) break;
                }
            }
            Log.d("Status ","OK received for "+this.app.name());
            ptime = System.currentTimeMillis();
            /* +3MIN */
            while (Globals.MAX_DATA_SIZE > app_data && !dcomp) {
            // while (!dcomp) {
                /* +3MIN */
                mcl_send_hget_req("NORMAL");
                long adata = mcl_receive_app_data(Globals.MAX_DATA_SIZE);
                if (-1 == adata && !dcomp) {
                    /* Create new communication channel
                    * send new get request */
                    adata = mcl_restart_download();
                }
                app_data += adata;
                // Log.d("Value", "Download size = " + app_data);
                this.num_req++;
            }
            dcomp = true;
            Log.d("Status","Download completed for "+ app + ":" + app_data);
            mcl_send_hget_req("FINAL");
            /* -MOD */
            this.sock_out.close();
            this.sock_input.close();
            this.sock.close();
            boolean debug = false;
        } catch (SocketException e) {
            //Log.d("ERROR", "Socket Error");
        } catch (IOException e) {
            //Log.d("ERROR", "IO Error");
        }
        return app_data;
    }

    public long mcl_start_downloader(){
        long app_data = 0;
        mcl_display_sock_error_loop();
        Socket sock = mcl_setup_comm_channel();
        Log.d("status","Common channel setup done "+this.app.name());
        app_data = mcl_download_app_data();
        return app_data;
    }
}