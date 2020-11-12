package com.example.caption;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
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
    private static final boolean TESTING = true;
    private static final String LOG_VERBOSE = "SubtitleShow VERBOSE";
    private String topic;
    private String serverAddr;
    private String serverPort;
    private Button goBackButton;
    private Button startButton;
    private TextView textView;
    private ServiceClient client;
    private AlertDialog alertDialog;
    private SeekBar subTitleSizeSeekBar;
    private boolean started;
    private AlertDialog settingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subtitle_view);
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
        Log.d(serverAddr, serverPort);
        client = new ServiceClient(arrayAdapter, textView);
        boolean connected = client.initConnection(topic, serverAddr, Integer.parseInt(serverPort));
        if (TESTING)
            return;
        if (!connected)
            alertDialog.show();
        else
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        builder.setPositiveButton("OK", null);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.setting_dialog, null);
        builder.setView(dialogView);
        settingDialog = builder.create();

        Button settingButton = findViewById(R.id.setting_button);
        settingButton.setOnClickListener(v -> settingDialog.show());

        goBackButton = findViewById(R.id.gbButton);
        goBackButton.setOnClickListener(_view -> goBackToMain());
        textView = findViewById(R.id.textView);
        TextView demonstration_text = dialogView.findViewById(R.id.font_size_demonstration);
        subTitleSizeSeekBar = dialogView.findViewById(R.id.subtitle_size_seekbar);
        subTitleSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                demonstration_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        String[] tes = {"PlaceHolder1", "PlaceHolder2"};
        ArrayList<String> test = new ArrayList<>(Arrays.asList(tes));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.text_item, test);


        initClientConnection(topic, arrayAdapter);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(button -> {
            if (!started)
            {
                started = true;
                client.resumeClient();
                ((Button) button).setText("Stop");
            } else
            {
                started = false;
                client.stopClient();
                ((Button) button).setText("Resume");
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        goBackToMain();
    }

}
