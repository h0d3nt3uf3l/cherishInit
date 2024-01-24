package com.niceshit.cherishinit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class process_backup extends AppCompatActivity {
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
        ArrayList<String[]> generatedCommands = (ArrayList<String[]>) intent.getSerializableExtra("_generatedCommands");

        try {
            sendCommands(generatedCommands);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCommands(ArrayList<String[]> generatedCommands) throws ExecutionException, InterruptedException {
        Pair<Boolean,String> result;
        lastCommand = new HashMap<Integer, Boolean>();

        TextView tv = findViewById(R.id.txt_process);

        Integer akt_command = 0;
        lastCommand.put(akt_command, false);

        new sendCommandAsync().execute(generatedCommands.get(akt_command), new String[]{akt_command.toString()});

        while(true) {
            try {
                if (generatedCommands.size() > lastCommand.size()) {
                    if (Boolean.TRUE.equals(lastCommand.get(akt_command))) {
                        akt_command++;
                        lastCommand.put(akt_command, false);
                        new sendCommandAsync().execute(generatedCommands.get(akt_command), new String[]{akt_command.toString()});
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                Log.d("Cherish", "Error: " + e.getMessage());
            }
        }
    }

    private class sendCommandAsync
            extends AsyncTask<String[], Void, Pair<Boolean,String>> {
        Integer aktCommand;
        @Override
        protected Pair<Boolean, String> doInBackground(String[]... params) {
            String[] command = params[0];
            aktCommand = Integer.valueOf(params[1][0]);
            return KodakUtils.sendCommand(command);
        }

        @Override
        protected void onPostExecute( Pair<Boolean, String> result) {
            new updateTextView().execute(result.retrieveVal() + "\n----------\n");
            lastCommand.replace(aktCommand, true);
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