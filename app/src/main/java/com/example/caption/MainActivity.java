package com.example.caption;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity
{
    public static String TOPIC_KEY = "TOPIC_KEY";
    private static final String LOG_VERBOSE = "MainActivity VERBOSE";
    private AlertDialog alertDialog;
    //    private WebSocketClient webSocketClient;
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
        alertBuilder.setMessage("Please Enter the topic").setPositiveButton("OK",null);
        alertDialog = alertBuilder.create();
        connectButton.setOnClickListener(view -> {
            Intent switchToSubtitleView = new Intent(this, SubtitleShowView.class);
            String topic = topicInputText.getText().toString();
            Log.v(LOG_VERBOSE, String.format("Clicked button, topic=%s", topic));
            if(topic.length()==0)
                alertDialog.show();
            else
            {
                topicInputText.getText().clear();
                switchToSubtitleView.putExtra(TOPIC_KEY, topic);
                startActivity(switchToSubtitleView);
            }
        });

    }

//    private void createWebSocketClient() {
//        URI uri;
//        String webPath = "ws://localhost:23330/gs-guide-websocket";
//        try {
//            // Connect to local host
//            uri = new URI(webPath);
//        }
//        catch (URISyntaxException e) {
//            e.printStackTrace();
//            return;
//        }
//        webSocketClient = new WebSocketClient(uri) {
//            @Override
//            public void onOpen() {
//                Log.i("WebSocket", "Session is starting");
//            }
//            @Override
//            public void onTextReceived(String s) {
//                Log.i("WebSocket", "Message received");
//                final String message = s;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try{
//                            TextView textView = findViewById(R.id.test_text_view);
//                            textView.setText(message);
//                        } catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//            @Override
//            public void onBinaryReceived(byte[] data) {
//            }
//            @Override
//            public void onPingReceived(byte[] data) {
//            }
//            @Override
//            public void onPongReceived(byte[] data) {
//            }
//            @Override
//            public void onException(Exception e) {
//                System.out.println(e.getMessage());
//            }
//            @Override
//            public void onCloseReceived() {
//                Log.i("WebSocket", "Closed ");
//                System.out.println("onCloseReceived");
//            }
//        };
//        webSocketClient.setConnectTimeout(10000);
//        webSocketClient.setReadTimeout(60000);
//        webSocketClient.enableAutomaticReconnection(5000);
//        webSocketClient.connect();
//    }
}
