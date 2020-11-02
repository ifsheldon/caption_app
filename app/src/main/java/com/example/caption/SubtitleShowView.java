package com.example.caption;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.caption.networking.ServiceClient;

import com.example.caption.networking.ServiceClient;

import java.util.ArrayList;
import java.util.Arrays;


import okhttp3.WebSocket;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;


public class SubtitleShowView extends AppCompatActivity
{
    private static final String LOG_VERBOSE = "SubtitleShow VERBOSE";
    private String topic;
    private Button goBackButton;
    private Button startButton;
    private ListView textList;
    private ServiceClient client;
    private AlertDialog alertDialog;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subtitle_view);
        Intent toSubtitleView = getIntent();
        topic = toSubtitleView.getStringExtra(MainActivity.TOPIC_KEY);
        Log.v(LOG_VERBOSE, String.format("Got topic = %s\n", topic));
        initViewComponents();
    }

    private void initClientConnection(String topic, ArrayAdapter<String> arrayAdapter)
    {
        client = new ServiceClient(arrayAdapter);
        boolean connected = client.initConnection(topic);
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
        textList = findViewById(R.id.textList);
        String[] tes = {"Test1", "Test2"};
        ArrayList<String> test = new ArrayList<>(Arrays.asList(tes));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.text_item, test);
        textList.setAdapter(arrayAdapter);

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
