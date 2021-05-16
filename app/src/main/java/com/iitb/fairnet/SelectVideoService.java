package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import java.util.Random;

import static com.iitb.fairnet.Globals.MAX_OTH_SERV;
import static com.iitb.fairnet.Globals.mcl_apps_enum.INVALID_APP;
import static com.iitb.fairnet.Globals.mcl_apps_enum.MCL_MAX_VID_SERVICE_ID;
import static com.iitb.fairnet.Globals.mcl_apps_enum.MCL_MIN_VID_SERVICE_ID;

public class SelectVideoService extends AppCompatActivity {
    RadioButton hsrbutton;
    RadioButton nfrbutton;
    RadioButton ytrbutton;
    RadioButton pvrbutton;
    RadioButton mxrbutton;
    RadioButton gvrbutton;
    RadioButton z5rbutton;
    RadioButton vtrbutton;
    RadioButton enrbutton;
    RadioButton slrbutton;
    Globals.mcl_apps_enum[] app_list;
    Globals.mcl_apps_enum test_app;
    double speed;
    Globals.mcl_gloc_enum gloc;

    private void mcl_init_video_service(){
        int max_lapp = Globals.mcl_apps_enum.INVALID_APP.ordinal();
        if (null == app_list)
            app_list = new Globals.mcl_apps_enum[max_lapp];
        for (int i=0;i<max_lapp;i++){
            app_list[i] = Globals.mcl_apps_enum.INVALID_APP;
        }
        test_app = Globals.mcl_apps_enum.INVALID_APP;
        speed = 0;
        gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }

    private void mcl_reset_video_service(){
        app_list = null;
        test_app = Globals.mcl_apps_enum.INVALID_APP;
        speed = 0;
        gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }

    private void mcl_set_audio_service(Globals.mcl_apps_enum capp, RadioButton rbutton){
        app_list[capp.ordinal()] = capp;
        rbutton.setChecked(true);
    }

    private void mcl_set_video_service_default_view() {
        Globals.mcl_apps_enum curr_app = Globals.mcl_apps_enum.INVALID_APP;
        int max_lapp = Globals.mcl_apps_enum.INVALID_APP.ordinal();
        hsrbutton = (RadioButton) findViewById(R.id.HSRButton);
        nfrbutton = (RadioButton) findViewById(R.id.NFRButton);
        ytrbutton = (RadioButton) findViewById(R.id.YTRButton);
        pvrbutton = (RadioButton) findViewById(R.id.PVRButton);
        mxrbutton = (RadioButton) findViewById(R.id.MXRButton);
        gvrbutton = (RadioButton) findViewById(R.id.HMRButton);
        z5rbutton = (RadioButton) findViewById(R.id.Z5RButton);
        vtrbutton = (RadioButton) findViewById(R.id.VTRButton);
        enrbutton = (RadioButton) findViewById(R.id.ENRButton);
        slrbutton = (RadioButton) findViewById(R.id.SLRButton);
        if (test_app == Globals.mcl_apps_enum.INVALID_APP)
            return;
        for (int i = 0; i < max_lapp; i++) {
            if (i == test_app.ordinal() && i != Globals.mcl_apps_enum.INVALID_APP.ordinal()) {
                if (i == Globals.mcl_apps_enum.HOTSTAR.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.HOTSTAR, hsrbutton);
                } else if (i == Globals.mcl_apps_enum.NETFLIX.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.NETFLIX, nfrbutton);
                } else if (i == Globals.mcl_apps_enum.YOUTUBE.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.YOUTUBE, ytrbutton);
                } else if (i == Globals.mcl_apps_enum.PRIMEVIDEO.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.PRIMEVIDEO, pvrbutton);
                } else if (i == Globals.mcl_apps_enum.MXPLAYER.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.MXPLAYER, mxrbutton);
                } else if (i == Globals.mcl_apps_enum.HUNGAMA.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.HUNGAMA, gvrbutton);
                } else if (i == Globals.mcl_apps_enum.ZEE5.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.ZEE5, z5rbutton);
                } else if (i == Globals.mcl_apps_enum.VOOT.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.VOOT, vtrbutton);
                } else if (i == Globals.mcl_apps_enum.EROSNOW.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.EROSNOW, enrbutton);
                } else if (i == Globals.mcl_apps_enum.SONYLIV.ordinal()) {
                    mcl_set_audio_service(Globals.mcl_apps_enum.SONYLIV, slrbutton);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_video_service);
        setTitle("Select a service to test discrimination");
        mcl_init_video_service();
        mcl_get_intent_data(getIntent());
        mcl_set_video_service_default_view();
    }

    private void mcl_get_intent_data(Intent intent) {
        Bundle intentData = intent.getExtras();
        if (null != intentData) {
            if (intentData.containsKey("TestApp"))
                test_app = (Globals.mcl_apps_enum) intentData.get("TestApp");
            if (intentData.containsKey("Speed"))
                speed = (double) intentData.get("Speed");
            if (intentData.containsKey("Gloc"))
                gloc = (Globals.mcl_gloc_enum) intentData.get("Gloc");
        }
    }

    private void mcl_put_intent_data(Intent intent){
        intent.putExtra("AppList", app_list);
        intent.putExtra("TestApp", test_app);
        intent.putExtra("Speed", speed);
        intent.putExtra("Gloc", gloc);
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, SelectServiceType.class);
        startActivity(intent);
        finish();
    }

    private boolean mcl_update_selected_video_service(Globals.mcl_apps_enum capp){
        boolean res = false;
        if (app_list[capp.ordinal()].name().equals(capp.name()) && test_app == capp) {
            app_list[capp.ordinal()] = Globals.mcl_apps_enum.INVALID_APP;
        } else {
            app_list[capp.ordinal()] = capp;
            if (test_app != Globals.mcl_apps_enum.INVALID_APP)
                app_list[test_app.ordinal()] = Globals.mcl_apps_enum.INVALID_APP;
            test_app = capp;
            res = true;
        }
        return res;
    }


    private void mcl_fill_serv_in_app_list(int sord) {
        int i;
        Globals.mcl_apps_enum capp = Globals.mcl_apps_enum.values()[sord];
        app_list[capp.ordinal()] = capp;
    }


    private void mcl_fill_app_list(){
        int i;
        int psord = INVALID_APP.ordinal();
        for (i=0;i<MAX_OTH_SERV; i++) {
            // Select a services randomly
            int sord;
            while (true) {
                sord = Globals.mcl_gen_random_number(MCL_MIN_VID_SERVICE_ID.ordinal() + 1,
                        MCL_MAX_VID_SERVICE_ID.ordinal() - 1);
                if (psord != sord && sord != test_app.ordinal()) {
                    psord = sord;
                    break;
                }
            }
            // Fill in app_list
            mcl_fill_serv_in_app_list(sord);
        }
    }

    public void mcl_select_video_service_compare(View view) {
        RadioButton hsrbutton = (RadioButton) findViewById(R.id.HSRButton);
        RadioButton nfrbutton = (RadioButton) findViewById(R.id.NFRButton);
        RadioButton ytrbutton = (RadioButton) findViewById(R.id.YTRButton);
        RadioButton pvrbutton = (RadioButton) findViewById(R.id.PVRButton);
        RadioButton mxrbutton = (RadioButton) findViewById(R.id.MXRButton);
        RadioButton gvrbutton = (RadioButton) findViewById(R.id.HMRButton);
        RadioButton z5rbutton = (RadioButton) findViewById(R.id.Z5RButton);
        RadioButton vtrbutton = (RadioButton) findViewById(R.id.VTRButton);
        RadioButton enrbutton = (RadioButton) findViewById(R.id.ENRButton);
        RadioButton slrbutton = (RadioButton) findViewById(R.id.SLRButton);
        boolean clicked = true;
        if (hsrbutton.isChecked()){
            hsrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.HOTSTAR));
        } else
        if (nfrbutton.isChecked()){
            nfrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.NETFLIX));
        } else
        if (ytrbutton.isChecked()){
            ytrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.YOUTUBE));
        } else
        if (pvrbutton.isChecked()){
            pvrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.PRIMEVIDEO));
        } else
        if (mxrbutton.isChecked()){
            mxrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.MXPLAYER));
        } else
        if (gvrbutton.isChecked()){
            gvrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.HUNGAMA));
        } else
        if (z5rbutton.isChecked()){
            z5rbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.ZEE5));
        } else
        if (vtrbutton.isChecked()){
            vtrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.VOOT));
        } else
        if (enrbutton.isChecked()){
            enrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.EROSNOW));
        } else
        if (slrbutton.isChecked()){
            slrbutton.setChecked(mcl_update_selected_video_service(Globals.mcl_apps_enum.SONYLIV));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select a service to test discrimination");
            AlertDialog dialog = builder.create();
            dialog.show();
            clicked = false;
        }

        if (clicked) {
            if (false) {
                Intent intent = new Intent(this, SelectVideoServiceCompare.class);
                // start the activity connect to the specified class
                mcl_put_intent_data(intent);
                mcl_reset_video_service();
                startActivity(intent);
                finish();
            } else {
                mcl_fill_app_list();
                Intent intent = new Intent(this, TestStatus.class);
                mcl_put_intent_data(intent);
                startActivity(intent);
                //Globals.mcl_run_test(app_list, test_app, speed, gloc,this);
                finish();
            }
        }
    }
}
