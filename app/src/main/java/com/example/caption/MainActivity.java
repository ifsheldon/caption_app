package com.example.caption;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity
{
    public static String TOPIC_KEY = "TOPIC_KEY";
    private static final String LOG_VERBOSE = "MainActivity VERBOSE";
    private AlertDialog alertDialog;
    private Button connectButton;
    private EditText topicInputText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewComponents();
    }

    private void initViewComponents()
    {
        connectButton = findViewById(R.id.connectButton);
        topicInputText = findViewById(R.id.topicText);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Please Enter the topic").setPositiveButton("OK", null);
        alertDialog = alertBuilder.create();
        connectButton.setOnClickListener(view -> {
            Intent switchToSubtitleView = new Intent(this, SubtitleShowView.class);
            String topic = topicInputText.getText().toString();
            Log.v(LOG_VERBOSE, String.format("Clicked button, topic=%s", topic));
            if (topic.length() == 0)
                alertDialog.show();
            else
            {
                topicInputText.getText().clear();
                switchToSubtitleView.putExtra(TOPIC_KEY, topic);
                startActivity(switchToSubtitleView);
            }
        });
    }
}
