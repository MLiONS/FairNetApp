package com.iitb.fairnet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DnsResolver;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.BoringLayout;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import static android.app.DownloadManager.Request.NETWORK_MOBILE;
import static android.app.DownloadManager.Request.NETWORK_WIFI;
import static com.iitb.fairnet.Globals.NETWORK_NONE;
import static com.iitb.fairnet.Globals.server;
import static com.iitb.fairnet.Globals.tsContext;
import static com.iitb.fairnet.SelectServiceType.context;
import static java.lang.Thread.sleep;

public class RunTest {
    public static Downloader[] adownloader;
    Globals.mcl_apps_enum[] app_list;
    Globals.mcl_apps_enum test_app;
    double speed;
    Globals.mcl_gloc_enum gloc;
    Activity cActivity;
    boolean app_server_info_availalble = false;
    Handler handler;


    public RunTest(Globals.mcl_apps_enum[] app_list,
                   Globals.mcl_apps_enum test_app,
                   double speed,
                   Globals.mcl_gloc_enum gloc,
                   Activity cActivity,
                   Handler handler) {
        adownloader = new Downloader[Globals.mcl_apps_enum.INVALID_APP.ordinal()];
        this.app_list = app_list;
        this.test_app = test_app;
        this.speed = speed;
        this.gloc = gloc;
        this.cActivity = cActivity;
        this.handler = handler;
    }

    public static void RunTestInit () {
        adownloader = null;
    }

    public class RunTestInfo {
        /* +Web-App-Server */
        String app_server = null;
        int port = 65356;
        /* -Web-App-Server */
        int in_port = 65356;
        public String[] mcl_app_hget_req_map;
        public String[] mcl_app_ini_hget_req_map;
        public String mcl_app_fin_hget_req;
        Globals.mcl_apps_enum[] app_list;
        Globals.mcl_apps_enum test_app;
        Globals.mcl_gloc_enum gloc;

        public RunTestInfo(Globals.mcl_apps_enum[] app_list, Globals.mcl_apps_enum test_app, Globals.mcl_gloc_enum gloc) {
            /* +Web-App-Server */
            app_server = null;
            port = 65356;
            in_port = 65356;
            /* -Web-App-Server */
            mcl_app_hget_req_map = null;
            mcl_app_ini_hget_req_map = null;
            mcl_app_fin_hget_req = null;
            this.app_list = app_list;
            this.test_app = test_app;
            this.gloc = gloc;
        }
    }

    RunTestInfo runTestInfo = new RunTestInfo(app_list, test_app, gloc);

    Globals mcl_glovars = new Globals();

    private String mcl_get_app_rserver(Globals.mcl_apps_enum capp) {
        String rval = null;
        switch (capp) {
            case HOTSTAR:
                rval = "gcloud.hotstar.com";
                break;
            case NETFLIX:
                rval = "ipv4-c017-bom001-jio-isp.1.oca.nflxvideo.net";
                //rval = "ipv4-c001-lhr001-ix.1.oca.nflxvideo.net";
                break;
            case YOUTUBE:
                rval = "r5---sn-cvh76ned.googlevideo.com";
                break;
            case PRIMEVIDEO:
                rval = "s3-sin-ww.cf.dash.row.aiv-cdn.net";
                break;
            case MXPLAYER:
                rval = "media-content.akamaized.net";
                break;
            case HUNGAMA:
                rval = "content1.hungama.com";
                break;
            case ZEE5:
                rval = "akamaividz2.zee5.com";
                break;
            case VOOT:
                rval = "vootvideo.akamaized.net";
                break;
            case EROSNOW:
                rval = "tvshowhls-b.erosnow.com";
                break;
            case SONYLIV:
                rval = "securetoken.sonyliv.com";
                break;
            /* +AUDIO */
            case WYNK:
                rval = "desktopsecurehls-vh.akamaihd.net";
                break;
            case GAANA_COM:
                rval = "vodhls-vh.akamaihd.net";
                break;
            case SAAVN:
                rval = "aa.cf.saavncdn.com";
                break;
            case SPOTIFY:
                rval = "audio4-fa.scdn.co";
                break;
            case PRIMEMUSIC:
                rval = "dfqzuzzcqflbd.cloudfront.net";
                break;
            case GPLAYMUSIC:
                rval = "music-pa.clients6.google.com";
                break;
            /* -AUDIO */
            case INVALID_APP:
                break;
            default:
                break;
        }
        return rval;
    }

    private String mcl_get_app_url(Globals.mcl_apps_enum capp) {
        String rval = null;
        switch (capp) {
            case HOTSTAR:
                rval = "https://www.hotstar.com/";
                break;
            case NETFLIX:
                rval = "https://www.netflix.com/";
                break;
            case YOUTUBE:
                rval = "https://www.youtube.com/";
                break;
            case PRIMEVIDEO:
                rval = "https://www.primevideo.com/";
                break;
            case MXPLAYER:
                rval = "https://www.mxplayer.in/";
                break;
            case HUNGAMA:
                rval = "https://www.hungama.com";
                break;
            case ZEE5:
                rval = "https://www.zee5.com/";
                break;
            case VOOT:
                rval = "https://www.voot.com/";
                break;
            case EROSNOW:
                rval = "https://erosnow.com/";
                break;
            case SONYLIV:
                rval = "https://www.sonyliv.com/";
                break;
            /* +AUDIO */
            case WYNK:
                rval = "https://wynk.in/music";
                break;
            case GAANA_COM:
                rval = "https://gaana.com/";
                break;
            case SAAVN:
                rval = "https://www.jiosaavn.com/";
                break;
            case SPOTIFY:
                rval = "https://www.spotify.com/";
                break;
            case PRIMEMUSIC:
                rval = "https://music.amazon.in/";
                break;
            case GPLAYMUSIC:
                rval = "https://play.google.com/music";
                break;
            /* -AUDIO */
            case INVALID_APP:
                break;
            default:
                break;
        }
        return rval;
    }

    private String mcl_get_hget_req(Globals.mcl_apps_enum capp, String dfile, String host) {
        String rval = null;
        rval = "GET " + dfile + " HTTP/1.1\r\n";
        if (host != null)
            rval = rval + "HOST: " + host + "\r\n";
        return rval;
    }

    private String mcl_get_dfile(Globals.mcl_apps_enum capp) {
        String rval = null;
        switch (capp) {
            case HOTSTAR:
                rval = "/videos/plus/nzrsp/200_c87ac2ef92/1000234882/1556648252010/5d0f83c3ccbf4501cf952bdfc8c0d785/media-5/segment-1.ts";
                break;
            case NETFLIX:
                rval = "/range/13799793-16280625?o=AQFGTfl4VAi-FaHnBM9bs9z9Je6awXE04iPZFhE9CTZQKqJ25FaQKei-dTJOv7u34PZV3bTAdMe4yoaha7kXUVfMvR9eD5clp9lOlTaVd__hv7ovp_U6HlvAA5SnkRZS9SeQUK96MYvnp8r70wqh71-4Y9EB0NAXb7X";
                break;
            case YOUTUBE:
                rval = "/videoplayback?expire=1562904733&ei=PbQnXdHFHOKZ8QOMkaOQDg&ip=2405%3A204%3A2219%3A2a15%3A6557%3Acb58%3A668c%3Af7";
                break;
            case PRIMEVIDEO:
                rval = "/dm/2$IiZdB0kcptjDslQmssXiOX6FBAc~/a55c/036c/9111/4b7b-b7a7-4371fd71e407/64b9e7f4-97c5-4278-ba1e-b4909715452c_video_11.mp4";
                break;
            case MXPLAYER:
                rval = "/video/26461dcf57c91948e6ee9fc55c58883e/5/dash/segments/h264_1080_baseline_5800k_11.m4s";
                break;
            case HUNGAMA:
                rval = "/c/5/c7a/66f/49092553/49092553_1000.mp4_1.m3u8?pF_TqrsNPcNoULokMGEIMx8Pce9b2KAb-ehERlnxp4YQRHpmSe3TMMJ4QvCYYpejc0_";
                break;
            case ZEE5:
                rval = "/drm1/elemental/dash/ORIGINAL_CONTENT/DOMESTIC/HINDI/RAGNI_MMS_2_UNCENSORD_REVISED/manifest1080p/1080p_000000055.mp4";
                break;
            case VOOT:
                rval = "/s/enc/hls/p/1982551/sp/198255100/serveFlavor/entryId/0_8mvarek0/v/21/pv/1/ev/42/flavorId/0_5zp612pq/name/a.mp4";
                break;
            case EROSNOW:
                rval = "/hls/tv/4/1029204/episode/6674642/1248/1029204_6674642_640_360_48_1200_6.ts";
                break;
            case SONYLIV:
                rval = "/beyhad_2_mahamovie_revised_20200306T181315_1200k_20200306T191848_000000014.mp4?hdntl=exp=1594542606~acl=/";
                break;
            case WYNK:
                rval = "/i/srch_universalmusic/music/,128,64,32,320,/1548664947/srch_universalmusic_00602577433320-US2BU1900125.mp4.csmil/segment1_0_a.ts?null=0&hdntl=exp=1563056231~acl=*/srch_universalmusic/music/*/1548664947/srch_universalmusic_00602577433320-US2BU1900125.mp4.csmil*~data=hdntl~hmac=37501ae3c3ac2b2cfe3a95e609f3998bc3538842e8f9d505530b7c243d277b2e";
                break;
            case GAANA_COM:
                rval = "/i/songs/69/2437469/25658817/25658817_64.mp4/segment1_0_a.ts?set-akamai-hls-revision=5&hdntl=exp=1562971199~acl=/i/songs/69/2437469/25658817/25658817_64.mp4/*~data=hdntl~hmac=efff171e28022490f6818a44505eb032fc027ca28bf12d88bcea3b7dafa47490";
                break;
            case SAAVN:
                rval = "/506/15d49653a626440d5463b0b54ea939fd.mp3?Expires=1563574268&Signature=C7UPEIVaQXLNcgLBVv74btthE5ECTsQLS1ikaIWtiswGMWRCL36P~p70Yx5BDdjUObmOENVHehVR-BEdBg2GyiW0KRcUm81jhb7JO1ZuYSoUha8WbJsfCbudurb3Fgi5qy7Q-12L25OO83ieldnEj1b7WMZPr9F5vF7y4MVXBdB5KaO1S2YWVBEgnF7ydRJIIbo9Epv84l7yKN6Yrb4FKv9uOM7DdRW1hRQ1zuPB6Jm-QrC1s7Zx7zWWiH21DVA~lMKLqGxnK472RBatE9TaNzI-U4BGiepsgHFmkceM1wiX1u1YSVuia~MJCc1-Tpb5i-bGSu3b6hpk~OAVRo77Hg__&Key-Pair-Id=APKAJB334VX63D3WJ5ZQ";
                break;
            case SPOTIFY:
                rval = "/audio/ec50aef8a65614acfd6d2eabe95b784e2b333b95?1562972269_nEPD3UIfYOEpHvGQymrZ-KDCt3JZeTyUrMDYsVj5IWo";
                break;
            case PRIMEMUSIC:
                rval = "/746abbd1-4282715717/b43b38f0-bb0f-342f-a513-29850b033e08.m4s?r=b4bcaa20-0cb5-4f31-8a89-92aad824cddf&rs=EU&mt=IN";
                break;
            case GPLAYMUSIC:
                rval = "/videoplayback?id=9d73b5c19e3d0d94&itag=141&source=skyjam&ei=sioKX8KxGLmJz7sPi7G32A4&o=08526904058750200680&range=4161536-5210111&segment=8&ratebypass=yes&cpn=OJadDSYEfBqW8_RT&ip=0.0.0.0&ipbits=0&expire=1594501969&sparams=ei%2Cexpire%2Cid%2Cip%2Cipbits%2Citag%2Cmh%2Cmip";
                break;
            case INVALID_APP:
                break;
            default:
                break;
        }
        return rval;
    }

    private String mcl_get_header(Globals.mcl_apps_enum capp) {
        String rval = "";
        switch (capp) {
            case HOTSTAR:
                rval = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nHost: hses.akamaized.net\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case NETFLIX:
                rval = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36\r\n\r\n";
                break;
            case YOUTUBE:
                rval = "Connection: keep-alive\r\nUser-agent: Mozilla/5.0 (Windows NT 10.0; -) Gecko/20100101 Firefox/66.0\r\nAccept: */*\r\ncache-control: max-age=0\r\nupgrade-insecure-requests: 1\r\n\r\n";
                break;
            case PRIMEVIDEO:
                rval = "HOST: s3-sin-ww.cf.dash.row.aiv-cdn.net\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case MXPLAYER:
                rval = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nHost: media-content.akamaized.net\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case HUNGAMA:
                rval = "HOST: hunstream.hungama.com\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case ZEE5:
                rval = "HOST: zee5vod.akamaized.net\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case VOOT:
                rval = "HOST: vootvideo.akamaized.net\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case EROSNOW:
                rval = "HOST: tvshowhls-b.erosnow.com\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case SONYLIV:
                rval = "HOST: securetoken.sonyliv.com\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            /* +AUDIO */
            case WYNK:
                rval = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\\r\\nAccept-Encoding: gzip, deflate, br\\r\\nAccept-Language: en-US,en;q=0.9\\r\\nConnection: keep-alive\\r\\nCookie: _alid_=LEAWeSdl8QO8wBjtOrraWg==; hdntl=exp=1564347548~acl=*%2fsrch_tipsmusic%2fmusic%2f*%2f1467397498%2fsrch_tipsmusic_INT101303504.mp4.csmil*~data=hdntl~hmac=e4c76a2a21addd5930f1da8e522e67b745ba0fe791a07238cbd483fbd97f8c0e\\r\\nHost: desktopsecurehls-vh.akamaihd.net\\r\\nUpgrade-Insecure-Requests: 1\\r\\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\\r\\n\\r\\n\\";
                break;
            case GAANA_COM:
                rval = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\\r\\nAccept-Encoding: gzip, deflate, br\\r\\nAccept-Language: en-US,en;q=0.9\\r\\nConnection: keep-alive\\r\\nCookie: _alid_=Q7W4WuFYau4djubYQujGqA==; hdntl=exp=1564350818~acl=%2fi%2fsongs%2f20%2f1855520%2f21250887%2f21250887_64.mp4%2f*~data=hdntl~hmac=eee41ca29a05f75d8f498f5bad222978e9a2cd51cdd6bbc4e3345999a486c219\\r\\nHost: vodhls-vh.akamaihd.net\\r\\nUpgrade-Insecure-Requests: 1\\r\\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\\r\\n\\r\\n";
                break;
            case SAAVN:
                rval = "accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\\r\\naccept-encoding: gzip, deflate, br\\r\\naccept-language: en-US,en;q=0.9\\r\\ncache-control: max-age=2\\r\\nupgrade-insecure-requests: 1\\r\\nuser-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\\r\\n\\r\\n";
                break;
            case SPOTIFY:
                rval = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\\r\\nAccept-Encoding: gzip, deflate, br\\r\\nAccept-Language: en-US,en;q=0.9\\r\\nConnection: keep-alive\\r\\nUpgrade-Insecure-Requests: 1\\r\\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36\\r\\n\\r\\n";
                break;
            case PRIMEMUSIC:
                rval = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nHost: music.amazon.in\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            case GPLAYMUSIC:
                rval = "HOST: r2---sn-cvh7knez.c.doc-0-0-sj.sj.googleusercontent.com\r\nAccept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\r\nAccept-Encoding: gzip, deflate, br\r\nAccept-Language: en-US,en;q=0.9\r\nConnection: keep-alive\r\nUpgrade-Insecure-Requests: 1\r\nUser-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36\r\n\r\n";
                break;
            /* -AUDIO */
            case INVALID_APP:
                break;
            default:
                break;
        }
        StringBuilder header = new StringBuilder(rval);
        header.append("SPEED:");
        String sparam = Double.toString(this.speed);
        Log.d("Status", "Updated speed = " + this.speed);
        header.append(sparam);
        return header.toString();
        // return rval;
    }

    private void mcl_setup_app_env(Globals.mcl_apps_enum capp, Globals mcl_glovars) {
        String header = mcl_get_header(capp);
        String ini_hget_req = mcl_get_hget_req(capp, mcl_get_app_url(capp), null);
        String hget_req = mcl_get_hget_req(capp, mcl_get_dfile(capp), mcl_get_app_rserver(capp));
        /* +MOD */
        String fin_hget_req = mcl_get_hget_req(capp, "END", null);
        /* -MOD */
        StringBuilder cdatab = new StringBuilder(hget_req);
        cdatab = cdatab.append(header);
        String hhget_req = cdatab.toString();
        this.runTestInfo.mcl_app_hget_req_map[capp.ordinal()] = hhget_req;//hget_req + header;
        this.runTestInfo.mcl_app_ini_hget_req_map[capp.ordinal()] = ini_hget_req;
        this.runTestInfo.mcl_app_fin_hget_req = fin_hget_req;
    }

    /* ISP name */
    private boolean mcl_get_wifi_connection_status() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    private int getNetWorkState() {
        int connType = NETWORK_NONE;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connMgr.getAllNetworks();
        for (Network network : networks) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    connType = NETWORK_MOBILE;
                } else {
                    connType = NETWORK_WIFI;
                    break;
                }
            }
        }
        return connType;
    }

    private boolean mcl_get_mobilenet_connection_status() {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
        return isMobileConn;
    }

    /*
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
     */

    public void getRequiredPermissions() {
        int REQUEST_RPS = 0;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cActivity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_RPS);
        }
        while (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            try {
                wait(100);
            } catch (Exception ignored) {
            }
        }
    }


    private void mcl_get_mobilenet_carrier_name (){
        SubscriptionInfo subscriptionInfo;
        int activeDataSubId;
        String cisp = " None";
        getRequiredPermissions();
        SubscriptionManager subManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        try {
            //Method getActiveDataSubId = SubscriptionManager.class.getMethod("getActiveDataSubscriptionId");
            Method getActiveDataSubId = SubscriptionManager.class.getMethod("getDefaultDataSubscriptionId");
            activeDataSubId = (int) getActiveDataSubId.invoke(subManager);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            subscriptionInfo = subManager.getActiveSubscriptionInfo(activeDataSubId);
            cisp = (String) subscriptionInfo.getCarrierName();
        } catch (Exception e) {
            cisp += "\n " + e;
        }
        Globals.dev.carrier_name = cisp;
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
        Downloader app_downloader = new Downloader(Globals.mcl_apps_enum.INVALID_APP,
                Globals.server,
                Globals.port,
                null,
                handler);
        app_downloader.mcl_display_sock_error_loop();
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
            if (!sock.isClosed())
                sock_input.close();
            if (!sock.isClosed())
                sock_out.close();
            if (!sock.isClosed())
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


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public String checkISP (String ipa) {
        String res = "None";
        String[] res_a = null;
        try {
            String iurl = "http://ip-api.com/json/";
            iurl = iurl + ipa;
            Log.d("Value", "ISP Check URL : " + iurl);
            URL ip = new URL(iurl);
            //URL ip = new URL("http://checkip.amazonaws.com/");
            BufferedReader in = new BufferedReader(new InputStreamReader(ip.openStream()));
            res = in.readLine();
            // Split the response (",")
            res_a = res.split(",");
            // Extract the ISP name ("isp:<isp name>")
            res_a = res_a[10].split(":");
            //System.out.println(res);
            res = res_a[1].replace("\"","");
            Log.d("Value", "ISP Check URL Response: " + res);
        } catch (IOException e) {
            e.printStackTrace();
            res = e.getMessage();
        }
        return res;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void mcl_get_wifi_carrier_name (){
        // WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String country =  tm.getNetworkCountryIso();
        Log.d("Value", "Country = " + country);
        String cname;
        String ispName = "";
        String output_data = "None";
        cname = mcl_get_isp_info();//mcl_get_cname();
        ispName += checkISP(cname);
        Globals.dev.carrier_addr = cname;
        Globals.dev.carrier_name = ispName;
        Globals.dev.country = country;
        output_data =  "WiFi Internet Carrier Name :" + ispName;
        Log.d("Value", output_data);
        Log.d("Value","Carrier country = " + Globals.dev.country);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void mcl_get_carrier_name(){
        //+Operator name
        //boolean wifi = mcl_get_wifi_connection_status();
        int nwstate = getNetWorkState();
        //if (!wifi) {
        if (NETWORK_MOBILE == nwstate) {
            mcl_get_mobilenet_carrier_name();
            //boolean mobilenet = mcl_get_mobilenet_connection_status();
            //if (mobilenet){
            //    mcl_get_mobilenet_carrier_name();
            //} else {
            //    Globals.dev.carrier_name = "Mobile ISP not accessible";
            //}
        } else if (NETWORK_WIFI == nwstate ) {
            mcl_get_wifi_carrier_name();
        } else {
            Globals.dev.carrier_name = "Unknown data connection type";
        }
    }

    /* ISP name */


    private void mcl_setup_env() {
        int max_lapp = Globals.mcl_apps_enum.INVALID_APP.ordinal();
        this.runTestInfo.mcl_app_hget_req_map = new String[max_lapp];
        this.runTestInfo.mcl_app_ini_hget_req_map = new String[max_lapp];
        {
            Globals.mcl_apps_enum[] appl = app_list;
            int lapp = app_list.length;
            //Globals.num_app_data= new int[lapp];
            //Globals.app_data = new Globals.AppData[INVALID_APP.ordinal()+1][10000];
            for (int i = 0; i < lapp; i++) {
                if (!appl[i].name().equals(Globals.mcl_apps_enum.INVALID_APP.name())) {
                    if (null != adownloader)
                        adownloader[appl[i].ordinal()] = null;
                    mcl_setup_app_env(app_list[i], mcl_glovars);
                }
            }
        }
        /* +Web-App-Server */
        Runnable mcl_appserv_handler = new mcl_app_server_thread(this.runTestInfo,this.handler);
        new Thread(mcl_appserv_handler).start();

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            public void run() {
                Looper.prepare();
                mcl_get_carrier_name();
                Looper.loop();
            }}).start();
        /* -Web-App-Server */
    }

    private void mcl_start_apps(){
        Globals.mcl_apps_enum[] appl = app_list;
        for (int i = 0; i < appl.length; i++)
        {
            if (!appl[i].name().equals(Globals.mcl_apps_enum.INVALID_APP.name())) {
                // Globals.mcl_app_dl_status_bmp = Globals.mcl_app_dl_status_bmp << 1 | 1;
                Runnable mcl_app_handler = new mcl_app_handler_thread(appl[i], mcl_glovars, this.runTestInfo);
                new Thread(mcl_app_handler).start();
                Log.d("Status","Starting app "+ appl[i].name());
            }
        }
    }

    public void start(){
        mcl_setup_env();
        Log.d("Status ","Setting Environment ");
        mcl_start_apps();
    }

    /* +Web-App-Server */
    public class mcl_app_server_thread implements Runnable {
        Globals.mcl_apps_enum app = null;
        RunTestInfo lrunTestInfo = null;
        Handler handler;

        public mcl_app_server_thread(RunTestInfo runTestInfo,
                                     Handler handler) {
            this.lrunTestInfo = runTestInfo;
            this.lrunTestInfo.app_server = Globals.server;
            this.lrunTestInfo.port = Globals.port;
            this.handler = handler;
        }

        private Globals.ServerInfo mcl_receive_app_server_info(Downloader.mcl_socket sock_api){
            Globals.ServerInfo server_info = null;
            Downloader.mcl_socket_data sdata = null;
            String[] sdata_a = null;
            while (true) {
                sdata = sock_api.mcl_receive_socket_data(70000);
                if (0 < sdata.sdlen) {
                    sdata_a = sdata.sd.split("\\r?\\n");
                    break;
                }
            }
            String appserv_status = sdata_a[0];
            if (appserv_status.equals("RUNNING")){
                server_info = new Globals.ServerInfo();
                server_info.server = sdata_a[1];
                server_info.port = Integer.parseInt(sdata_a[2]);
                server_info.in_port = Integer.parseInt(sdata_a[3]);
                Log.d("Status","New app server info "+ server_info.server);
            }
            return server_info;
        }

        private void mcl_send_client_info(Downloader.mcl_socket sock_api){
            String report = Globals.app_id + ":" + gloc;
            sock_api.mcl_send_socket_data(report);
        }

        private Globals.ServerInfo mcl_get_app_server_info(){
            Globals.ServerInfo server_info = null;
            Downloader app_downloader = new Downloader(Globals.mcl_apps_enum.INVALID_APP,
                    this.lrunTestInfo.app_server,
                    this.lrunTestInfo.port,
                    null,
                    handler);
            Log.d("Status","webserver = "+this.lrunTestInfo.app_server + ":" + this.lrunTestInfo.port);
            //app_downloader.mcl_display_sock_error_loop();
            DataOutputStream sock_out = null;
            // BufferedReader sock_input = null;
            DataInputStream sock_input = null;
            Downloader.mcl_socket sock_api = null;
            String serror = "Connection error";
            while (null == server_info) {
                Socket sock = app_downloader.mcl_get_socket(true);
                try {
                    int i = 0;
                    sock_out = new DataOutputStream(sock.getOutputStream());
                    // sock_input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    sock_input = new DataInputStream(sock.getInputStream());
                    sock_api = new Downloader.mcl_socket(sock_input, sock_out);
                    mcl_send_client_info(sock_api);
                    server_info = mcl_receive_app_server_info(sock_api);
                    if (null == server_info)
                        Log.d("Status", "No server available");
                    else
                        Log.d("Status", "server info recevied ");
                    if (!sock.isClosed())
                        sock_input.close();
                    if (!sock.isClosed())
                        sock_out.close();
                    if (!sock.isClosed())
                        sock.close();
                } catch (SocketException e) {
                    Log.d("ERROR", "Socket Error");
                    Globals.mcl_print_toast(handler, tsContext, serror);
                } catch (IOException e) {
                    Log.d("ERROR", "IO Error");
                    Globals.mcl_print_toast(handler, tsContext, serror);
                } catch (IllegalArgumentException e) {
                    Log.d("ERROR", "IllegalArgumentException");
                    Globals.mcl_print_toast(handler, tsContext, serror);
                } catch (SecurityException e) {
                    Log.d("ERROR", "SecurityException");
                    Globals.mcl_print_toast(handler, tsContext, serror);
                } catch (NullPointerException e) {
                    Log.d("ERROR", "NullPointerException" + e);
                    Globals.mcl_print_toast(handler, tsContext, serror);
                }
            }

            return server_info;
        }

        public void run(){
            Looper.prepare();
            boolean debug = false;
            Globals.ServerInfo server_info = null;
            Log.d("status", "Getting app server info ");
            server_info = mcl_get_app_server_info();
            /*
            while (server_info == null) {
                server_info = mcl_get_app_server_info();
            }
            try {
                sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            */
            if (null == server_info) {
                Log.d("Status", "Server info not available");
            }
            if (debug){
                //runTestInfo.app_server = "10.0.2.2";
                runTestInfo.app_server = "192.168.0.13";
                runTestInfo.port = 8086;
                runTestInfo.in_port = 8085;
            } else {
                runTestInfo.app_server = server_info.server;
                runTestInfo.port = server_info.port;
                runTestInfo.in_port = server_info.in_port;
            }
            Log.d("Status","Updated server inf "+runTestInfo.app_server + ":" + runTestInfo.port);
            app_server_info_availalble = true;
            Looper.loop();
        }
    }
    /* -Web-App-Server */

    public class mcl_app_handler_thread implements Runnable {
        Globals.mcl_apps_enum app = null;
        Globals mcl_glovars;
        RunTestInfo lrunTestInfo = null;

        public mcl_app_handler_thread(Globals.mcl_apps_enum capp,Globals cmcl_glovars, RunTestInfo runTestInfo) {
            app = capp;
            mcl_glovars = cmcl_glovars;
            this.lrunTestInfo = runTestInfo;
        }
        private void mcl_app_handler(){
            /* +Web-App-Server */
            // Downloader app_downloader = new Downloader(app,Globals.server,Globals.port, this.runTestInfo);
            while (!app_server_info_availalble) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
            Downloader app_downloader = new Downloader(app,
                                                       runTestInfo.app_server,
                                                       runTestInfo.port,
                                                       this.lrunTestInfo,
                                                       handler);
            /* -Web-App-Server */
            adownloader[this.app.ordinal()] = app_downloader;
            long app_data;
            Log.d("Status", "Started downloading "+ app.name());
            app_data = app_downloader.mcl_start_downloader();
            // assert(app_data < MAX_DATA_SIZE);
        }

        public void run () {
            Looper.prepare();
            /* +Web-App-Server */
            Log.d("Status","Started running ap handler "+ Boolean.toString(app_server_info_availalble));
            /* -Web-App-Server */
            Log.d("Status","Starting ap handers ");
            mcl_app_handler();
            Looper.loop();
        }
    }
}
