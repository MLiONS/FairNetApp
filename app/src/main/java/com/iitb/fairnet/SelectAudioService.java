package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import static com.iitb.fairnet.Globals.MAX_OTH_SERV;
import static com.iitb.fairnet.Globals.mcl_apps_enum.GAANA_COM;
import static com.iitb.fairnet.Globals.mcl_apps_enum.GPLAYMUSIC;
import static com.iitb.fairnet.Globals.mcl_apps_enum.INVALID_APP;
import static com.iitb.fairnet.Globals.mcl_apps_enum.MCL_MAX_AUD_SERVICE_ID;
import static com.iitb.fairnet.Globals.mcl_apps_enum.MCL_MAX_VID_SERVICE_ID;
import static com.iitb.fairnet.Globals.mcl_apps_enum.MCL_MIN_AUD_SERVICE_ID;
import static com.iitb.fairnet.Globals.mcl_apps_enum.MCL_MIN_VID_SERVICE_ID;
import static com.iitb.fairnet.Globals.mcl_apps_enum.PRIMEMUSIC;
import static com.iitb.fairnet.Globals.mcl_apps_enum.SAAVN;
import static com.iitb.fairnet.Globals.mcl_apps_enum.SPOTIFY;
import static com.iitb.fairnet.Globals.mcl_apps_enum.WYNK;

public class SelectAudioService extends AppCompatActivity {
    RadioButton gcrbutton;
    RadioButton wyrbutton;
    RadioButton sprbutton;
    RadioButton svrbutton;
    RadioButton pmrbutton;
    RadioButton gmrbutton;
    Globals.mcl_apps_enum[] app_list;
    Globals.mcl_apps_enum test_app;
    double speed;
    Globals.mcl_gloc_enum gloc;

    private void mcl_init_audio_service(){
        int max_lapp = INVALID_APP.ordinal();
        if (null == app_list)
            app_list = new Globals.mcl_apps_enum[max_lapp];
        for (int i=0;i<max_lapp;i++){
            app_list[i] = INVALID_APP;
        }
        test_app = INVALID_APP;
        speed = 0;
        gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }

    private void mcl_reset_audio_service(){
        app_list = null;
        test_app = INVALID_APP;
        speed = 0;
        gloc = Globals.mcl_gloc_enum.INVALID_GLOC;
    }

    private void mcl_set_audio_service(Globals.mcl_apps_enum capp, RadioButton rbutton){
        app_list[capp.ordinal()] = capp;
        rbutton.setChecked(true);
    }

    private void mcl_set_audio_service_default_view() {
        Globals.mcl_apps_enum curr_app = INVALID_APP;
        int max_lapp = INVALID_APP.ordinal();
        gcrbutton = (RadioButton) findViewById(R.id.GCRButton);
        wyrbutton = (RadioButton) findViewById(R.id.WYRButton);
        sprbutton = (RadioButton) findViewById(R.id.SPRButton);
        svrbutton = (RadioButton) findViewById(R.id.SVRButton);
        pmrbutton = (RadioButton) findViewById(R.id.PMRButton);
        //gmrbutton = (RadioButton) findViewById(R.id.GMRButton);
        if (test_app == INVALID_APP)
            return;
        for (int i = 0; i < max_lapp; i++) {
            if (i == test_app.ordinal() && i != INVALID_APP.ordinal()) {
                if (i == GAANA_COM.ordinal()) {
                    mcl_set_audio_service(GAANA_COM, gcrbutton);
                } else if (i == WYNK.ordinal()) {
                    mcl_set_audio_service(WYNK, wyrbutton);
                } else if (i == SPOTIFY.ordinal()) {
                    mcl_set_audio_service(SPOTIFY, sprbutton);
                } else if (i == SAAVN.ordinal()) {
                    mcl_set_audio_service(SAAVN, svrbutton);
                } else if (i == PRIMEMUSIC.ordinal()) {
                    mcl_set_audio_service(PRIMEMUSIC, pmrbutton);
                } else if (i == GPLAYMUSIC.ordinal()) {
                    mcl_set_audio_service(GPLAYMUSIC, gmrbutton);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_audio_service);
        setTitle("Select a service to test discrimination");
        mcl_init_audio_service();
        mcl_get_intent_data(getIntent());
        mcl_set_audio_service_default_view();
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

    private boolean mcl_update_selected_audio_service(Globals.mcl_apps_enum capp){
        boolean res = false;
        if (app_list[capp.ordinal()].name().equals(capp.name()) && test_app == capp) {
            app_list[capp.ordinal()] = INVALID_APP;
        } else {
            app_list[capp.ordinal()] = capp;
            if (test_app != INVALID_APP)
                app_list[test_app.ordinal()] = INVALID_APP;
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
                sord = Globals.mcl_gen_random_number(MCL_MIN_AUD_SERVICE_ID.ordinal() + 1,
                        MCL_MAX_AUD_SERVICE_ID.ordinal() - 1);
                if (psord != sord && sord != test_app.ordinal()) {
                    psord = sord;
                    break;
                }
            }
            // Fill in app_list
            mcl_fill_serv_in_app_list(sord);
        }
    }


    public void mcl_select_audio_service_compare(View view) {
        RadioButton gcrbutton = (RadioButton) findViewById(R.id.GCRButton);
        RadioButton wyrbutton = (RadioButton) findViewById(R.id.WYRButton);
        RadioButton sprbutton = (RadioButton) findViewById(R.id.SPRButton);
        RadioButton svrbutton = (RadioButton) findViewById(R.id.SVRButton);
        RadioButton pmrbutton = (RadioButton) findViewById(R.id.PMRButton);
        //RadioButton gmrbutton = (RadioButton) findViewById(R.id.GMRButton);

        boolean clicked = true;
        if (gcrbutton.isChecked()){
            gcrbutton.setChecked(mcl_update_selected_audio_service(GAANA_COM));
        } else
        if (wyrbutton.isChecked()){
            wyrbutton.setChecked(mcl_update_selected_audio_service(WYNK));
        } else
        if (sprbutton.isChecked()){
            sprbutton.setChecked(mcl_update_selected_audio_service(SPOTIFY));
        } else
        if (svrbutton.isChecked()){
            svrbutton.setChecked(mcl_update_selected_audio_service(SAAVN));
        } else
        if (pmrbutton.isChecked()){
            pmrbutton.setChecked(mcl_update_selected_audio_service(PRIMEMUSIC));
        } else
        if (gmrbutton.isChecked()){
            gmrbutton.setChecked(mcl_update_selected_audio_service(GPLAYMUSIC));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select a service to test discrimination");
            AlertDialog dialog = builder.create();
            dialog.show();
            clicked = false;
        }

        if (clicked) {
            if (false) {
                Intent intent = new Intent(this, SelectAudioServiceCompare.class);
                // start the activity connect to the specified class
                mcl_put_intent_data(intent);
                mcl_reset_audio_service();
                startActivity(intent);
                finish();
            } else {
                mcl_fill_app_list();
                Intent intent = new Intent(this, TestStatus.class);
                mcl_put_intent_data(intent);
                startActivity(intent);
                Globals.mcl_run_test(app_list, test_app, speed, gloc,this, null);
                finish();
            }
        }
    }
}