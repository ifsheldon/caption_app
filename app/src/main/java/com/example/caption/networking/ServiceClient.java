package com.example.caption.networking;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

@SuppressLint("CheckResult")
public class ServiceClient extends Thread implements IServerClient
{
    private String SERVER_ADDR = "10.0.2.2";
    private int PORT = 23330;

    private static final String LOG_VERBOSE = "Client VERBOSE";
    private static final String LOG_DEBUG = "Client DEBUG";
    private final ArrayAdapter<String> arrayAdapter;
    private final TextView textView;
    private final SubtitleStorage subtitleStorage = new SubtitleStorage();
    private final AtomicBoolean stop = new AtomicBoolean(true);
    private final AtomicBoolean terminate = new AtomicBoolean(false);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String topic;

    private StompClient mStompClient;

    public ServiceClient(ArrayAdapter<String> arrayAdapter, TextView textView)
    {
        this.arrayAdapter = arrayAdapter;
        this.textView = textView;
    }

    public void stopClient()
    {
        stop.set(true);
    }

    public void resumeClient()
    {
        stop.set(false);
    }

    public void terminate()
    {
        terminate.set(true);
    }

    // need to call this first before run()
    public boolean initConnection(String topic, String serverAddr, int serverPort)
    {

        this.topic = topic;
        this.SERVER_ADDR = serverAddr;
        this.PORT = serverPort;

        String connectionURL = String.format("ws://%s:%d/gs-guide-websocket/websocket", this.SERVER_ADDR, this.PORT);

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, connectionURL);

        AtomicBoolean connectSuccess = new AtomicBoolean(true);

        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType())
            {

                case OPENED:
                    Log.d(LOG_VERBOSE, "Stomp connection opened");
                    break;

                case ERROR:
                    Log.e(LOG_VERBOSE, "Error", lifecycleEvent.getException());
                    connectSuccess.set(false);
                    break;

                case CLOSED:
                    Log.d(LOG_VERBOSE, "Stomp connection closed");
                    break;
            }
        });

        mStompClient.connect();

        return connectSuccess.get();
    }

    private void subscribe(String topic)
    {
        mStompClient.topic("/topic/" + topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.i(LOG_VERBOSE, topicMessage.getPayload());
                    if (stop.get() || terminate.get())
                        return;

                    try
                    {
                        Map<String, Object> m1 = objectMapper.readValue(topicMessage.getPayload(), Map.class);
                        Log.v(LOG_VERBOSE, m1.toString());
                        if (m1.containsKey("header"))
                        {
                            String eventName = ((Map<String, Object>) m1.get("header")).get("name").toString();
                            String text = ((Map<String, Object>) m1.get("payload")).get("result").toString();

                            if ("TranscriptionResultChanged".equals(eventName))
                            {
                                subtitleStorage.add(text, false);
                            } else if ("SentenceEnd".equals(eventName))
                            {
                                subtitleStorage.add(text, true);
                            }
                            //TODO: check whether it works
//                    arrayAdapter.clear();
//                    arrayAdapter.add(subtitleStorage.get());
                            textView.setText(subtitleStorage.get());
                        } else
                        {
                            Log.v(LOG_VERBOSE, "Not subtitle!");
                        }

                    }
                    catch (IOException e)
                    {
                        Log.e(LOG_DEBUG, e.getMessage());
                    }
                }, throwable -> Log.e(LOG_DEBUG, "err!", throwable));
    }

    @Override
    public void run()
    {
        Log.v(LOG_VERBOSE, "Subscribing to topic using session " + mStompClient);
        this.subscribe(topic);
    }
}
