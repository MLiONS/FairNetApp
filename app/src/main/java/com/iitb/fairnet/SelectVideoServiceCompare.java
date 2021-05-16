package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import static com.iitb.fairnet.Globals.tsContext;

public class SelectVideoServiceCompare extends AppCompatActivity {
    Globals.mcl_apps_enum curr_app = Globals.mcl_apps_enum.INVALID_APP;
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
    Globals.mcl_gloc_enum  gloc;

    private Globals.mcl_apps_enum mcl_set_video_service_compare_default_view() {
        Globals.mcl_apps_enum curr_app = Globals.mcl_apps_enum.INVALID_APP;
        int max_lapp = Globals.mcl_apps_enum.INVALID_APP.ordinal();
        hsrbutton = (RadioButton) findViewById(R.id.VCHSRButton);
        nfrbutton = (RadioButton) findViewById(R.id.VCNFRButton);
        ytrbutton = (RadioButton) findViewById(R.id.VCYTRButton);
        pvrbutton = (RadioButton) findViewById(R.id.VCPVRButton);
        mxrbutton = (RadioButton) findViewById(R.id.VCMXRButton);
        gvrbutton = (RadioButton) findViewById(R.id.VCHMRButton);
        z5rbutton = (RadioButton) findViewById(R.id.VCZ5RButton);
        vtrbutton = (RadioButton) findViewById(R.id.VCVTRButton);
        enrbutton = (RadioButton) findViewById(R.id.VCENRButton);
        slrbutton = (RadioButton) findViewById(R.id.VCSLRButton);
        for (int i = 0; i < max_lapp; i++) {
            if (app_list[i] != Globals.mcl_apps_enum.INVALID_APP && i != Globals.mcl_apps_enum.INVALID_APP.ordinal()) {
                if (i == Globals.mcl_apps_enum.HOTSTAR.ordinal()) {
                    hsrbutton.setChecked(true);
                    hsrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.HOTSTAR;
                } else if (i == Globals.mcl_apps_enum.NETFLIX.ordinal()) {
                    nfrbutton.setChecked(true);
                    nfrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.NETFLIX;
                } else if (i == Globals.mcl_apps_enum.YOUTUBE.ordinal()) {
                    ytrbutton.setChecked(true);
                    ytrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.YOUTUBE;
                } else if (i == Globals.mcl_apps_enum.PRIMEVIDEO.ordinal()) {
                    pvrbutton.setChecked(true);
                    pvrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.PRIMEVIDEO;
                } else if (i == Globals.mcl_apps_enum.MXPLAYER.ordinal()) {
                    mxrbutton.setChecked(true);
                    mxrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.MXPLAYER;
                } else if (i == Globals.mcl_apps_enum.HUNGAMA.ordinal()) {
                    gvrbutton.setChecked(true);
                    gvrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.HUNGAMA;
                } else if (i == Globals.mcl_apps_enum.ZEE5.ordinal()) {
                    z5rbutton.setChecked(true);
                    z5rbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.ZEE5;
                } else if (i == Globals.mcl_apps_enum.VOOT.ordinal()) {
                    vtrbutton.setChecked(true);
                    vtrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.VOOT;
                } else if (i == Globals.mcl_apps_enum.EROSNOW.ordinal()) {
                    enrbutton.setChecked(true);
                    enrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.EROSNOW;
                } else if (i == Globals.mcl_apps_enum.SONYLIV.ordinal()) {
                    slrbutton.setChecked(true);
                    slrbutton.setEnabled(false);
                    curr_app = Globals.mcl_apps_enum.SONYLIV;
                }
            }
        }
        return curr_app;
    }

    private void mcl_init_videoservicecompare(){
        this.app_list = null;
        this.curr_app = Globals.mcl_apps_enum.INVALID_APP;
        this.speed = 0;
        this.test_app = Globals.mcl_apps_enum.INVALID_APP;
        this.gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_video_service_compare);
        setTitle("Compare with");
        mcl_init_videoservicecompare();
        mcl_get_intent_data(getIntent());
        this.curr_app = mcl_set_video_service_compare_default_view();
        tsContext = this;
    }

    private void mcl_get_intent_data(Intent intent) {
        Bundle intentData = intent.getExtras();
        if (null != intentData) {
            if (intentData.containsKey("AppList"))
                app_list = (Globals.mcl_apps_enum[]) intentData.get("AppList");
            if (intentData.containsKey("TestApp"))
                test_app = (Globals.mcl_apps_enum) intentData.get("TestApp");
            if (intentData.containsKey("Gloc"))
                gloc = (Globals.mcl_gloc_enum) intentData.get("Gloc");
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, SelectVideoService.class);
        mcl_put_intent_data_backward(intent);
        startActivity(intent);
        finish();
    }

    private void mcl_update_compare_video_service(Globals.mcl_apps_enum capp){
        app_list[capp.ordinal()] = capp;
    }

    private void mcl_reset_video_service(Globals.mcl_apps_enum capp){
        app_list[capp.ordinal()] = Globals.mcl_apps_enum.INVALID_APP;
    }

    private void mcl_process_button_click (int rbid, Globals.mcl_apps_enum app){
        RadioButton radioButton = findViewById(rbid);
        if (app_list[app.ordinal()] == app && test_app != app){
            radioButton.setChecked(false);
            mcl_reset_video_service(app);
        } else {
            mcl_update_compare_video_service(app);
        }
    }

    private void mcl_put_intent_data_backward(Intent intent){
        intent.putExtra("TestApp", test_app);
        intent.putExtra("Speed", speed);
        intent.putExtra("Gloc", gloc);
    }

    public void mcl_handle_button_click(View v){
        switch (v.getId()) {
            case R.id.VCHSRButton:
                mcl_process_button_click(R.id.VCHSRButton, Globals.mcl_apps_enum.HOTSTAR);
                break;
            case R.id.VCNFRButton:
                mcl_process_button_click(R.id.VCNFRButton, Globals.mcl_apps_enum.NETFLIX);
                break;
            case R.id.VCYTRButton:
                mcl_process_button_click(R.id.VCYTRButton, Globals.mcl_apps_enum.YOUTUBE);
                break;
            case R.id.VCPVRButton:
                mcl_process_button_click(R.id.VCPVRButton, Globals.mcl_apps_enum.PRIMEVIDEO);
                break;
            case R.id.VCMXRButton:
                mcl_process_button_click(R.id.VCMXRButton, Globals.mcl_apps_enum.MXPLAYER);
                break;
            case R.id.VCHMRButton:
                mcl_process_button_click(R.id.VCHMRButton, Globals.mcl_apps_enum.HUNGAMA);
                break;
            case R.id.VCZ5RButton:
                mcl_process_button_click(R.id.VCZ5RButton, Globals.mcl_apps_enum.ZEE5);
                break;
            case R.id.VCVTRButton:
                mcl_process_button_click(R.id.VCVTRButton, Globals.mcl_apps_enum.VOOT);
                break;
            case R.id.VCENRButton:
                mcl_process_button_click(R.id.VCENRButton, Globals.mcl_apps_enum.EROSNOW);
                break;
            case R.id.VCSLRButton:
                mcl_process_button_click(R.id.VCSLRButton, Globals.mcl_apps_enum.SONYLIV);
                break;
        }
    }

    private boolean mcl_get_final_app_list(){
        boolean NotClicked = true;
        RadioButton hsrbutton = (RadioButton) findViewById(R.id.VCHSRButton);
        RadioButton nfrbutton = (RadioButton) findViewById(R.id.VCNFRButton);
        RadioButton ytrbutton = (RadioButton) findViewById(R.id.VCYTRButton);
        RadioButton pvrbutton = (RadioButton) findViewById(R.id.VCPVRButton);
        RadioButton mxrbutton = (RadioButton) findViewById(R.id.VCMXRButton);
        if (hsrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.HOTSTAR){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.HOTSTAR);
            NotClicked = false;
        }
        if (nfrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.NETFLIX){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.NETFLIX);
            NotClicked = false;
        }
        if (ytrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.YOUTUBE){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.YOUTUBE);
            NotClicked = false;
        }
        if (pvrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.PRIMEVIDEO){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.PRIMEVIDEO);
            NotClicked = false;
        }
        if (mxrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.MXPLAYER){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.MXPLAYER);
            NotClicked = false;
        }
        if (gvrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.HUNGAMA){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.HUNGAMA);
            NotClicked = false;
        }
        if (z5rbutton.isChecked() && curr_app != Globals.mcl_apps_enum.ZEE5){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.ZEE5);
            NotClicked = false;
        }
        if (vtrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.VOOT){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.VOOT);
            NotClicked = false;
        }
        if (enrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.EROSNOW){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.EROSNOW);
            NotClicked = false;
        }
        if (slrbutton.isChecked() && curr_app != Globals.mcl_apps_enum.SONYLIV){
            mcl_update_compare_video_service(Globals.mcl_apps_enum.SONYLIV);
            NotClicked = false;
        }
        return NotClicked;
    }

    private void mcl_run_test(Globals.mcl_apps_enum[] app_list,
                              Globals.mcl_apps_enum test_app,
                              double speed,
                              Globals.mcl_gloc_enum gloc){
        RunTest rt = new RunTest(app_list, test_app, 5, gloc,this, null);
        rt.start();
    }

    private void mcl_put_intent_data_forward(Intent intent){
        intent.putExtra("AppList", app_list);
        intent.putExtra("TestApp", test_app);
        intent.putExtra("Speed", speed);
        intent.putExtra("Gloc", gloc);
    }

    public void mcl_test_status(View view) {
        boolean NotClicked = true;
        NotClicked = mcl_get_final_app_list();
        if (true == NotClicked){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select minimum two services to compare");
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Intent intent = new Intent(this, TestStatus.class);
            mcl_put_intent_data_forward(intent);
            startActivity(intent);
            Globals.mcl_run_test(app_list, test_app, speed, gloc,this, null);
        }
    }
}
