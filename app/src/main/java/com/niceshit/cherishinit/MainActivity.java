package com.niceshit.cherishinit;
import static com.niceshit.cherishinit.KodakSecret.aesEncryptToHexWithPadding;
import static com.niceshit.cherishinit.KodakSecret.generateSecretKey;
import static com.niceshit.cherishinit.KodakSecret.getPublicKeyFromString;
import static com.niceshit.cherishinit.KodakSecret.rsaEncryptSecretKeyToHex;
import static com.niceshit.cherishinit.KodakUtils.base64Encode;
import static com.niceshit.cherishinit.KodakUtils.getLocalHertz;
import static com.niceshit.cherishinit.KodakUtils.getTimeZoneName;
import static com.niceshit.cherishinit.KodakUtils.readFile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Locale;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    public static class ContextOnClickListener implements View.OnClickListener {
        Context mainContext;

        public ContextOnClickListener(Context mainContext) {
            this.mainContext = mainContext;
        }

        @Override
        public void onClick(View v) {
        }
    }

    public static class SaveValues_TextWatcher implements TextWatcher {
        String constValue;
        SharedPreferences pref;

        public SaveValues_TextWatcher(String _constValue, SharedPreferences _pref) {
            this.constValue = _constValue;
            this.pref = _pref;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            pref.edit().putString(constValue, s.toString()).commit();
        }
    }

    public static EditText mIP;
    public static EditText mSSID;
    public static EditText mPassword;
    public static EditText mMQTTServer;
    public static EditText mDNS1;
    public static EditText mDNS2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            String bArr = readFile(getResources().openRawResource(R.raw.fixed_key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        //Save IP and Restore it
        mIP = (EditText) findViewById(R.id.input_ip);
        mIP.setText(pref.getString("SAVED_IP", ""));
        mIP.addTextChangedListener(new SaveValues_TextWatcher("SAVED_IP", pref));

        //Save SSID and Restore it
        mSSID = (EditText) findViewById(R.id.input_ssid);
        mSSID.setText(pref.getString("SAVED_SSID", ""));
        mSSID.addTextChangedListener(new SaveValues_TextWatcher("SAVED_SSID", pref));

        //Save Password and Restore it
        mPassword = (EditText) findViewById(R.id.input_password);
        mPassword.setText(pref.getString("SAVED_PASSWORD", ""));
        mPassword.addTextChangedListener(new SaveValues_TextWatcher("SAVED_PASSWORD", pref));

        //Save MQTTServer and Restore it
        mMQTTServer = (EditText) findViewById(R.id.input_mqttserver);
        mMQTTServer.setText(pref.getString("SAVED_MQTT", ""));
        mMQTTServer.addTextChangedListener(new SaveValues_TextWatcher("SAVED_MQTT", pref));

        //Save DNS1 and Restore it
        mDNS1 = (EditText) findViewById(R.id.input_DNS1);
        mDNS1.setText(pref.getString("SAVED_DNS1", ""));
        mDNS1.addTextChangedListener(new SaveValues_TextWatcher("SAVED_DNS1", pref));

        //Save DNS2 and Restore it
        mDNS2 = (EditText) findViewById(R.id.input_DNS2);
        mDNS2.setText(pref.getString("SAVED_DNS2", ""));
        mDNS2.addTextChangedListener(new SaveValues_TextWatcher("SAVED_DNS2", pref));


        String[] commands = {"url_set", "set_flicker", "dns_config", "set_sec_type", "set_city_timezone", "set_nwk_info_v2", "restart_system"};

        View.OnClickListener submit_init = new ContextOnClickListener(this) {
            @Override
            public void onClick(View v) {
                SecretKey secretKey = generateSecretKey(128);
                Boolean success;
//                for (String command: commands) {
//                    success = false;
//                    try {
//                        success = new sendCommandAsync().execute(generateCommand(mainContext, command, secretKey)).get();
//                        if (!success) {
//                            break;
//                        }
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
                ArrayList<String[]> generatedCommands = new ArrayList<String[]>();
                for (String command : commands) {
                    try {
                        generatedCommands.add(generateCommand(mainContext, command, secretKey));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                Intent myIntent = new Intent(MainActivity.this, process.class);
                myIntent.putExtra("_generatedCommands", generatedCommands);
                startActivity(myIntent);
                finish();
            }
        };

        Button btn_submit = (Button) findViewById(R.id.btn_send);
        btn_submit.setOnClickListener(submit_init);

        if (savedInstanceState != null) {
        }
    }

    // On Button Klick
    // Generiere generatedSecretKey = generateSecretKey(128); muss durchgereicht werden
    // Sende alle commands der reihe nach ab
    protected String[] generateCommand(Context mainContext, @NonNull String command, SecretKey key) throws IOException, GeneralSecurityException {
        String mIP;
        String ssid;
        String pass;
        String responseType = "";
//        String mServer;
//        String mDNS1;
//        String mDNS2;
        String param = "";
        SecretKey generatedSecretKey = key;

        mIP = ((EditText) findViewById(R.id.input_ip)).getText().toString();
        ssid = ((EditText) findViewById(R.id.input_ssid)).getText().toString();
        pass = ((EditText) findViewById(R.id.input_password)).getText().toString();

        String command_url = "http://" + mIP + "/?req=";
        switch (command) {
            case "url_set":
                String api_url = "api-t01-r3.perimetersafe.com";
                String mqtt_url = mMQTTServer.getText().toString().equals("") ? "mqtt-t01-r3.perimetersafe.com:8894" : mMQTTServer.getText().toString();
                String stun_url = "stun-t01-r3.perimetersafe.com";
                String rms_url = "rms-t01-r3.perimetersafe.com";
                String ntp_url = "pool.ntp.org";
                String ana_url = "mt-t01-r3.perimetersafe.com:9100";


                param = String.format(Locale.US, "&api_url=%s&mqtt_url=%s&ntp_url=%s&rms_url=%s&stun_url=%s&ana_url=%s", Uri.encode(api_url), Uri.encode(mqtt_url), Uri.encode(ntp_url), Uri.encode(rms_url), Uri.encode(stun_url), Uri.encode(ana_url));
                responseType = "boolean";
                break;

            case "set_flicker":
                param = String.format(Locale.US, "&value=%d", Integer.valueOf(getLocalHertz()));
                responseType = "boolean";
                break;

            case "dns_config":
                String DNS1 = mDNS1.getText().toString().equals("") ? "8.8.4.4" : mDNS1.getText().toString();
                String DNS2 = mDNS1.getText().toString().equals("") ? "8.8.4.4" : mDNS2.getText().toString();

                param = String.format(Locale.US, "&value=%d&main=%s&sub=%s", 3, DNS1, DNS2);
                responseType = "boolean";
                break;

            case "set_sec_type":
                String secret = "";

                PublicKey pubkey = getPublicKeyFromString(readFile(getResources().openRawResource(R.raw.public_pem)));

                try {
                    secret = rsaEncryptSecretKeyToHex(pubkey, generatedSecretKey);
                } catch (Exception ex) {
                    Log.d("Cherish", "Error in getting Secret. Message: " + ex.getMessage());
                }
                param = String.format(Locale.US, "&value=%s", secret);
                responseType = "boolean";
                break;

            case "set_city_timezone":
                param = String.format(Locale.US, "&value=%s", getTimeZoneName());
                responseType = "boolean";
                break;
            case "set_nwk_info_v2":
                //Only WPA and WPA2 possible atm
                String base64Encode = base64Encode(ssid);
                String base64Encode2 = base64Encode(pass);
                String setup_core_request_utf8_supported = URLEncoder.encode("100" + "2" + "0" + "0" + String.format("%03d", Integer.valueOf(base64Encode.length())) + String.format("%02d", Integer.valueOf(base64Encode2.length())) + "0000000" + String.format("%02d", Integer.valueOf("".length())) + String.format("%02d", Integer.valueOf("".length())) + base64Encode + base64Encode2 + "" + "", "UTF-8");
                String build_setup_request = aesEncryptToHexWithPadding(generatedSecretKey, setup_core_request_utf8_supported);

                param = String.format(Locale.US, "&setup=%s", build_setup_request);
                responseType = "boolean";
                break;

            case "get_setup_log":
                param = (String) null;
                responseType = "string";
                break;

            case "restart_system":
                param = (String) null;
                responseType = "string";
                break;

            case "get_udid":
                param = (String) null;
                responseType = "string";
                break;

            case "get_caminfo":
                param = (String) null;
                responseType = "string";
                break;
        }

        if (param == "") {
            Log.d("Cherish", "Parameter not set!");
            return new String[]{command_url, command};
        }
        return new String[]{command_url, command, param, responseType};
    }
}
