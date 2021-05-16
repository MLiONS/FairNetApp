package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import static com.iitb.fairnet.Globals.mcl_apps_enum.GAANA_COM;
import static com.iitb.fairnet.Globals.mcl_apps_enum.GPLAYMUSIC;
import static com.iitb.fairnet.Globals.mcl_apps_enum.INVALID_APP;
import static com.iitb.fairnet.Globals.mcl_apps_enum.PRIMEMUSIC;
import static com.iitb.fairnet.Globals.mcl_apps_enum.PRIMEVIDEO;
import static com.iitb.fairnet.Globals.mcl_apps_enum.SAAVN;
import static com.iitb.fairnet.Globals.mcl_apps_enum.SPOTIFY;
import static com.iitb.fairnet.Globals.mcl_apps_enum.WYNK;
import static com.iitb.fairnet.Globals.tsContext;

public class SelectAudioServiceCompare extends AppCompatActivity {
    Globals.mcl_apps_enum curr_app = INVALID_APP;
    RadioButton gcrbutton;
    RadioButton wyrbutton;
    RadioButton sprbutton;
    RadioButton svrbutton;
    RadioButton pmrbutton;
    RadioButton gmrbutton;

    Globals.mcl_apps_enum[] app_list;
    Globals.mcl_apps_enum test_app;
    double speed;
    Globals.mcl_gloc_enum  gloc;

    private Globals.mcl_apps_enum mcl_set_audio_service_compare_default_view() {
        Globals.mcl_apps_enum curr_app = INVALID_APP;
        int max_lapp = INVALID_APP.ordinal();
        this.gcrbutton = (RadioButton) findViewById(R.id.ACGCRButton);
        this.wyrbutton = (RadioButton) findViewById(R.id.ACWYRButton);
        this.sprbutton = (RadioButton) findViewById(R.id.ACSPRButton);
        this.svrbutton = (RadioButton) findViewById(R.id.ACSVRButton);
        this.pmrbutton = (RadioButton) findViewById(R.id.ACPMRButton);
        //this.gmrbutton = (RadioButton) findViewById(R.id.ACGMRButton);
        for (int i = 0; i < max_lapp; i++) {
            if (app_list[i] != INVALID_APP && i != INVALID_APP.ordinal()) {
                if (i == GAANA_COM.ordinal()) {
                    gcrbutton.setChecked(true);
                    gcrbutton.setEnabled(false);
                    curr_app = GAANA_COM;
                } else if (i == WYNK.ordinal()) {
                    wyrbutton.setChecked(true);
                    wyrbutton.setEnabled(false);
                    curr_app = WYNK;
                } else if (i == SPOTIFY.ordinal()) {
                    sprbutton.setChecked(true);
                    sprbutton.setEnabled(false);
                    curr_app = SPOTIFY;
                } else if (i == SAAVN.ordinal()) {
                    svrbutton.setChecked(true);
                    svrbutton.setEnabled(false);
                    curr_app = SAAVN;
                } else if (i == PRIMEMUSIC.ordinal()) {
                    pmrbutton.setChecked(true);
                    pmrbutton.setEnabled(false);
                    curr_app = PRIMEMUSIC;
                } else if (i == GPLAYMUSIC.ordinal()) {
                    gmrbutton.setChecked(true);
                    gmrbutton.setEnabled(false);
                    curr_app = GPLAYMUSIC;
                }
            }
        }
        return curr_app;
    }

    private void mcl_init_audioservicecompare(){
        this.app_list = null;
        this.curr_app = INVALID_APP;
        this.speed = 0;
        this.test_app = INVALID_APP;
        this.gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_audio_service_compare);
        setTitle("Compare with");
        mcl_init_audioservicecompare();
        mcl_get_intent_data(getIntent());
        this.curr_app = mcl_set_audio_service_compare_default_view();
        tsContext = getApplicationContext();
    }

    private void mcl_get_intent_data(Intent intent) {
        Bundle intentData = intent.getExtras();
        if (null != intentData) {
            if (intentData.containsKey("AppList"))
                app_list = (Globals.mcl_apps_enum[]) intentData.get("AppList");
            if (intentData.containsKey("TestApp"))
                test_app = (Globals.mcl_apps_enum) intentData.get("TestApp");
            if (intentData.containsKey("Speed"))
                speed = (double)intentData.get("Speed");
            if (intentData.containsKey("Gloc"))
                gloc = (Globals.mcl_gloc_enum) intentData.get("Gloc");
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, SelectAudioService.class);
        mcl_put_intent_data_backward(intent);
        startActivity(intent);
        finish();
    }

    private void mcl_update_compare_audio_service(Globals.mcl_apps_enum capp){
        app_list[capp.ordinal()] = capp;
    }

    private void mcl_reset_audio_service(Globals.mcl_apps_enum capp){
        app_list[capp.ordinal()] = INVALID_APP;
    }

    private void mcl_process_button_click (int rbid, Globals.mcl_apps_enum app){
        RadioButton radioButton = findViewById(rbid);
        if (app_list[app.ordinal()] == app && test_app != app){
            radioButton.setChecked(false);
            mcl_reset_audio_service(app);
        } else {
            mcl_update_compare_audio_service(app);
        }
    }

    private void mcl_put_intent_data_backward(Intent intent){
        intent.putExtra("TestApp", test_app);
        intent.putExtra("Speed", speed);
        intent.putExtra("Gloc", gloc);
    }

    public void mcl_handle_button_click(View v){
        switch (v.getId()) {
            case R.id.ACGCRButton:
                mcl_process_button_click(R.id.ACGCRButton, GAANA_COM);
                break;
            case R.id.ACWYRButton:
                mcl_process_button_click(R.id.ACWYRButton, WYNK);
                break;
            case R.id.ACSPRButton:
                mcl_process_button_click(R.id.ACSPRButton, SPOTIFY);
                break;
            case R.id.ACSVRButton:
                mcl_process_button_click(R.id.ACSVRButton, SAAVN);
                break;
            case R.id.ACPMRButton:
                mcl_process_button_click(R.id.ACPMRButton, PRIMEMUSIC);
                break;
            //case R.id.ACGMRButton:
            //    mcl_process_button_click(R.id.ACGMRButton, GPLAYMUSIC);
            //    break;
        }
    }

    private boolean mcl_get_final_app_list(){
        boolean NotClicked = true;
        RadioButton gcrbutton = (RadioButton) findViewById(R.id.ACGCRButton);
        RadioButton wyrbutton = (RadioButton) findViewById(R.id.ACWYRButton);
        RadioButton sprbutton = (RadioButton) findViewById(R.id.ACSPRButton);
        RadioButton svrbutton = (RadioButton) findViewById(R.id.ACSVRButton);
        RadioButton pmrbutton = (RadioButton) findViewById(R.id.ACPMRButton);
        //RadioButton gmrbutton = (RadioButton) findViewById(R.id.ACGMRButton);
        if (gcrbutton.isChecked() && curr_app != GAANA_COM){
            mcl_update_compare_audio_service(GAANA_COM);
            NotClicked = false;
        }
        if (wyrbutton.isChecked() && curr_app != WYNK){
            mcl_update_compare_audio_service(WYNK);
            NotClicked = false;
        }
        if (sprbutton.isChecked() && curr_app != SPOTIFY){
            mcl_update_compare_audio_service(SPOTIFY);
            NotClicked = false;
        }
        if (svrbutton.isChecked() && curr_app != SAAVN){
            mcl_update_compare_audio_service(SAAVN);
            NotClicked = false;
        }
        if (pmrbutton.isChecked() && curr_app != PRIMEMUSIC){
            mcl_update_compare_audio_service(PRIMEMUSIC);
            NotClicked = false;
        }
        /***
        if (gmrbutton.isChecked() && curr_app != GPLAYMUSIC){
            mcl_update_compare_audio_service(GPLAYMUSIC);
            NotClicked = false;
        }
         **/
        return NotClicked;
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
        if (NotClicked){
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
