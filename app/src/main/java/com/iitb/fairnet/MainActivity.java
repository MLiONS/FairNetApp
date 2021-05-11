package com.iitb.fairnet;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    public static Context context;
    private TextView mTextMessage;

    private boolean mcl_get_wifi_connection_status(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    private boolean mcl_get_mobilenet_connection_status(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mWifi.isConnected();
    }

    private void mcl_get_mobilenet_carrier_name (){
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // String carrier_name = manager.getNetworkOperator();
        // String carrier_name = manager.getNetworkOperatorName().replace(" ","-");
        String carrier_name = null;
        try {
            carrier_name = manager.getNetworkOperatorName();
        } catch (NullPointerException e){
            Log.d("Status","Null Pointer");
        }
        if (null != carrier_name)
            carrier_name = carrier_name.replace(" ","-");
        String output_data = "Mobile Internet Carrier : " + carrier_name;
        Log.d("Value", output_data);
        String country = manager.getNetworkCountryIso();
        Log.d("Value", country);
        Globals.dev.carrier_name = carrier_name;
        Globals.dev.country = country;
        //output_data = "Operator : " + carrier_name;
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setMessage(output_data);
        //AlertDialog dialog = builder.create();
        //dialog.show();
        //String cname = mcl_get_isp_info();//mcl_get_cname();
        //output_data =  "Mobile Internet Carrier : WiFi " + cname;
        //Log.d("Value", output_data);
    }

    public String getLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    private String mcl_get_cname (){
        Log.d("Status","Getting carrier name manually ");
        String cname = "NA";
        Socket sock = null;
        try {
            sock = new Socket("35.200.160.204", 80);
            // Log.d("Socket", "New socket = " + sock);
            DataInputStream sock_input = new DataInputStream(sock.getInputStream());
            Downloader.mcl_socket sock_api = new Downloader.mcl_socket(sock_input, null);
            InetSocketAddress s = InetSocketAddress.createUnresolved("35.200.160.204", 80);
            // sock.connect(s);
            Downloader.mcl_socket_data sdata;
            while (true) {
                sdata = sock_api.mcl_receive_socket_data(70000);
                if (0 < sdata.sdlen) {
                    cname = sdata.sd;
                    break;
                }
            }
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
        return cname;
    }

    private String mcl_receive_isp_info(Downloader.mcl_socket sock_api){
        String isp_info = null;
        Downloader.mcl_socket_data sdata = null;
        while (true) {
            sdata = sock_api.mcl_receive_socket_data(70000);
            if (0 < sdata.sdlen) {
                isp_info = sdata.sd;
                break;
            }
        }
        return isp_info;
    }

    private void mcl_send_ipa_info(Downloader.mcl_socket sock_api){
        String report = "IPADDR";
        sock_api.mcl_send_socket_data(report);
    }

    private String mcl_get_isp_info(){
        String isp_info = null;
        Globals.tsContext = this;
        Downloader app_downloader = new Downloader(Globals.mcl_apps_enum.INVALID_APP,Globals.server
                ,Globals.port, null);
        Socket sock = app_downloader.mcl_get_socket(true);
        DataOutputStream sock_out = null;
        DataInputStream sock_input = null;
        Downloader.mcl_socket sock_api = null;
        try {
            int i = 0;
            sock_out = new DataOutputStream(sock.getOutputStream());
            // sock_input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            sock_input = new DataInputStream(sock.getInputStream());
            sock_api = new Downloader.mcl_socket(sock_input, sock_out);
            mcl_send_ipa_info(sock_api);
            isp_info = mcl_receive_isp_info(sock_api);
            if (null == isp_info)
                Log.d("Status","No ISP");
            else
                Log.d("Status","ISP info recevied: "+ isp_info);
            sock_input.close();
            sock_out.close();
            sock.close();
        } catch (SocketException e) {
            Log.d("ERROR","Socket Error");
        } catch (IOException e) {
            Log.d("ERROR","IO Error");
        } catch (IllegalArgumentException e) {
            Log.d("ERROR","IllegalArgumentException");
        } catch (SecurityException e) {
            Log.d("ERROR","SecurityException");
        } catch (NullPointerException e) {
            Log.d("ERROR","NullPointerException");
        }
        return isp_info;
    }


    private void mcl_get_wifi_carrier_name (){
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String country =  tm.getNetworkCountryIso();
        Log.d("Value", "Country = " + country);
        String cname;
        cname = mcl_get_isp_info();//mcl_get_cname();
        String output_data =  "Mobile Internet Carrier : WiFi " + cname;
        Log.d("Value", output_data);
        Globals.dev.carrier_name = cname;
        Globals.dev.country = country;
        Log.d("Value",Globals.dev.country);
    }

    private void mcl_get_carrier_name(){
        //+Operator name
        boolean wifi = mcl_get_wifi_connection_status();
        if (false == wifi ) {
            boolean mobilenet = mcl_get_mobilenet_connection_status();
            if (mobilenet){
                mcl_get_mobilenet_carrier_name();
            }
        } else {
            mcl_get_wifi_carrier_name();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = getApplicationContext();
        /*
        new Thread(new Runnable() {
            public void run() {
                mcl_get_carrier_name();
            }}).start();
        */
    }

    /**
    public void mcl_select_service_type(View view){
        Intent intent = new Intent(this, SelectServiceType.class);
        // start the activity connect to the specified class
        startActivity(intent);
    }
     **/
    public void mcl_select_geloloc_zone(View view){
        Intent intent = new Intent(this, GetGeoLocation.class);
        // start the activity connect to the specified class
        startActivity(intent);
    }
}
