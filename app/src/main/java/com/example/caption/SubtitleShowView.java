package com.example.caption;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.caption.networking.ServiceClient;

import com.example.caption.networking.ServiceClient;

import java.util.ArrayList;
import java.util.Arrays;


public class SubtitleShowView extends AppCompatActivity
{
    private static final String LOG_VERBOSE = "SubtitleShow VERBOSE";
    private String topic;
    private String serverAddr;
    private String serverPort;
    private Button goBackButton;
    private Button startButton;
    private TextView textView;
    private ServiceClient client;
    private AlertDialog alertDialog;
    private SeekBar fontSizeSeekBar;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subtitle_view_textview);
        Intent toSubtitleView = getIntent();
        topic = toSubtitleView.getStringExtra(MainActivity.TOPIC_KEY);
        serverAddr = toSubtitleView.getStringExtra(MainActivity.SERVER_ADDR_KEY);
        serverPort = toSubtitleView.getStringExtra(MainActivity.PORT_KEY);
        Log.v("port", serverPort);
        Log.v("serverAddr", serverAddr);
        Log.v(LOG_VERBOSE, String.format("Got topic = %s\n", topic));
        initViewComponents();
    }

    private void initClientConnection(String topic, ArrayAdapter<String> arrayAdapter)
    {
        Log.d(serverAddr,serverPort);
        client = new ServiceClient(arrayAdapter, textView);
        boolean connected = client.initConnection(topic, serverAddr, Integer.parseInt(serverPort));
        if (!connected)
            alertDialog.show();
        client.start();
    }

    private void goBackToMain()
    {
        Intent goBack = new Intent(this, MainActivity.class);
        client.terminate();
        startActivity(goBack);
    }

    @SuppressLint("CheckResult")
    private void initViewComponents()
    {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        String alertMsg = "Cannot connect";
        alertBuilder.setMessage(alertMsg)
                .setPositiveButton("Return to home page", (_a, _b) -> goBackToMain())
                .setCancelable(false);
        alertDialog = alertBuilder.create();
        goBackButton = findViewById(R.id.gbButton);
        goBackButton.setOnClickListener(_view -> goBackToMain());
        textView = findViewById(R.id.textView);
        fontSizeSeekBar = findViewById(R.id.fontSizeSeekBar);
        String[] tes = {"PlaceHolder1", "PlaceHolder2"};
        ArrayList<String> test = new ArrayList<>(Arrays.asList(tes));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.text_item, test);
//        textList.setAdapter(arrayAdapter);


        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView.setTextSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        initClientConnection(topic, arrayAdapter);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(button -> {
            if(!started)
            {
                started = true;
                client.resumeClient();
                ((Button)button).setText("Stop");
            }
            else
            {
                started = false;
                client.stopClient();
                ((Button)button).setText("Resume");
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        goBackToMain();
    }

}
