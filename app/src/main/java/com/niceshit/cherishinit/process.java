package com.niceshit.cherishinit;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class process extends AppCompatActivity {
    Map<Integer, Boolean> lastCommand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        //Close Button
        Button btn_close = (Button) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });

        Intent intent = getIntent();
        final ArrayList<String[]> generatedCommands = (ArrayList<String[]>) intent.getSerializableExtra("_generatedCommands");

        IdleHandler handler = new IdleHandler() {
            @Override
            public boolean queueIdle() {
                try {
                    sendCommands(generatedCommands);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        };
        ((ProgressBar)findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        Looper.myQueue().addIdleHandler(handler);
    }

    public void sendCommands(ArrayList<String[]> generatedCommands) throws ExecutionException, InterruptedException {
        Integer i = 0;
        TextView tv = findViewById(R.id.txt_process);
        Pair<Boolean, String> result = new Pair<Boolean,String>(false, "No Command was done.");

        for (String[] command: generatedCommands) {
            for (i = 0; i<3; i++) { // try 3 times
                sleep(500);
                result = new sendCommandAsync().execute(command).get();

                if (result.retrieveKey()) { // result is true, break try-loop
                    break;
                }
            }
            if (!result.retrieveKey()) { // result is false, break command-loop
                break;
            }
        }
        ((ProgressBar)findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);

        if (result.retrieveKey()) {
            ((TextView) findViewById(R.id.txt_headProcess)).setText("All done! Good Luck.");
            ((ScrollView)findViewById(R.id.scroll_process)).setBackgroundColor(0x5522FF22);
        } else {
            ((TextView)findViewById(R.id.txt_headProcess)).setText("Error! See below.");
            ((ScrollView)findViewById(R.id.scroll_process)).setBackgroundColor(0x55FF2222);

        }
        ((ProgressBar)findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
    }

    private class sendCommandAsync
            extends AsyncTask<String[], Void, Pair<Boolean,String>> {

        @Override
        protected Pair<Boolean, String> doInBackground(String[]... params) {
            String[] command = params[0];
            return KodakUtils.sendCommand(command);
        }

        @Override
        protected void onPostExecute( Pair<Boolean, String> result) {
            new updateTextView().execute(result.retrieveVal() + "\n----------\n");
            super.onPostExecute(result);
        }
    }

    private class updateTextView extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... text) {
            return text[0];
        }
        protected void onPostExecute(String text) {
            ((TextView)findViewById(R.id.txt_process)).append(text);
        }
    }
}