package com.example.caption;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubtitleShowView extends AppCompatActivity
{
    private static final String LOG_VERBOSE = "SubtitleShow VERBOSE";
    private String topic;
    private Button goBackButton;
    private Button testButton;
    private ListView textList;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subtitle_view);
        Intent toSubtitleView = getIntent();
        topic = toSubtitleView.getStringExtra(MainActivity.TOPIC_KEY);
        Log.v(LOG_VERBOSE,String.format("Got topic = %s\n",topic));
        initViewComponents();
    }

    private void initViewComponents()
    {
        goBackButton = findViewById(R.id.gbButton);
        goBackButton.setOnClickListener(view->{
            Intent goBack = new Intent(this, MainActivity.class);
            startActivity(goBack);
        });
        textList = findViewById(R.id.textList);
        String[] tes = {"Test1", "Test2"};
        ArrayList<String> test = new ArrayList<>(Arrays.asList(tes));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.text_item, test);
        textList.setAdapter(arrayAdapter);
        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(view->{
            arrayAdapter.add("Test Button Pressed");
        });
    }

}
