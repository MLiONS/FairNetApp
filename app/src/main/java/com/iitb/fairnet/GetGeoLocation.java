package com.iitb.fairnet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import static com.iitb.fairnet.Globals.mcl_gloc_enum.AFRICA;
import static com.iitb.fairnet.Globals.mcl_gloc_enum.AMERICA;
import static com.iitb.fairnet.Globals.mcl_gloc_enum.ASIA;
import static com.iitb.fairnet.Globals.mcl_gloc_enum.AUSTRALIA;
import static com.iitb.fairnet.Globals.mcl_gloc_enum.EUROPE;
import static com.iitb.fairnet.Globals.mcl_gloc_enum.INVALID_GLOC;

public class GetGeoLocation extends AppCompatActivity {
    RadioButton afbutton;
    RadioButton ambutton;
    RadioButton asbutton;
    RadioButton aubutton;
    RadioButton eubutton;
    Globals.mcl_gloc_enum gloc;

    private void mcl_init_gloc(){
        int max_gloc = INVALID_GLOC.ordinal();
        gloc = Globals.dev.gloc;
    }

    private void mcl_reset_gloc(){
        gloc = INVALID_GLOC;
    }

    private void mcl_set_gloc(Globals.mcl_gloc_enum capp, RadioButton rbutton){
        rbutton.setChecked(true);
    }

    private void mcl_set_gloc_default_view() {
        Globals.mcl_gloc_enum curr_app = INVALID_GLOC;
        int max_lapp = INVALID_GLOC.ordinal();
        afbutton = (RadioButton) findViewById(R.id.AfButton);
        ambutton = (RadioButton) findViewById(R.id.AmButton);
        aubutton = (RadioButton) findViewById(R.id.AuButton);
        asbutton = (RadioButton) findViewById(R.id.AsButton);
        eubutton = (RadioButton) findViewById(R.id.EuButton);
        if (gloc == INVALID_GLOC)
            return;
        for (int i = 0; i < max_lapp; i++) {
            if (i == gloc.ordinal() && i != INVALID_GLOC.ordinal()) {
                if (i == AFRICA.ordinal()) {
                    mcl_set_gloc(AFRICA, afbutton);
                } else if (i == AMERICA.ordinal()) {
                    mcl_set_gloc(AMERICA, ambutton);
                } else if (i == ASIA.ordinal()) {
                    mcl_set_gloc(ASIA, asbutton);
                } else if (i == AUSTRALIA.ordinal()) {
                    mcl_set_gloc(AUSTRALIA, aubutton);
                } else if (i == EUROPE.ordinal()) {
                    mcl_set_gloc(EUROPE, eubutton);
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_geo_location);
        setTitle("Select current location");
        mcl_init_gloc();
        mcl_get_intent_data(getIntent());
        mcl_set_gloc_default_view();
    }

    private void mcl_get_intent_data(Intent intent) {
        Bundle intentData = intent.getExtras();
        if (null != intentData) {
            if (intentData.containsKey("Gloc"))
                gloc = (Globals.mcl_gloc_enum) intentData.get("Gloc");
        }
    }

    private void mcl_put_intent_data(Intent intent){
        intent.putExtra("Gloc", gloc);
    }

    public void onBackPressed() {
        // Intent intent = new Intent(this, GetGeoLocation.class);
        // startActivity(intent);
        finish();
    }

    private boolean mcl_update_selected_gloc(Globals.mcl_gloc_enum cgloc){
        boolean res;
        gloc = cgloc;
        res = true;
        return res;
    }

    public void mcl_select_service_type(View view) {
        RadioButton afbutton = (RadioButton) findViewById(R.id.AfButton);
        RadioButton ambutton = (RadioButton) findViewById(R.id.AmButton);
        RadioButton asbutton = (RadioButton) findViewById(R.id.AsButton);
        RadioButton aubutton = (RadioButton) findViewById(R.id.AuButton);
        RadioButton eubutton = (RadioButton) findViewById(R.id.EuButton);
        boolean clicked = true;
        if (afbutton.isChecked()){
            afbutton.setChecked(mcl_update_selected_gloc(AFRICA));
        } else
        if (ambutton.isChecked()){
            ambutton.setChecked(mcl_update_selected_gloc(AMERICA));
        } else
        if (asbutton.isChecked()){
            asbutton.setChecked(mcl_update_selected_gloc(ASIA));
        } else
        if (aubutton.isChecked()){
            asbutton.setChecked(mcl_update_selected_gloc(AUSTRALIA));
        } else
        if (eubutton.isChecked()){
            eubutton.setChecked(mcl_update_selected_gloc(EUROPE));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please select current location");
            AlertDialog dialog = builder.create();
            dialog.show();
            clicked = false;
        }
        if (clicked) {
            Intent intent = new Intent(this, SelectServiceType.class);
            // start the activity connect to the specified class
            Globals.dev.gloc = gloc;
            mcl_put_intent_data(intent);
            mcl_reset_gloc();
            startActivity(intent);
            finish();
        }
    }
}
