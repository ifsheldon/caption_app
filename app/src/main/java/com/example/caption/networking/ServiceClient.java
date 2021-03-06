package com.example.caption.networking;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

@SuppressLint("CheckResult")
public class ServiceClient extends Thread implements IServerClient
{
    private static final String LOG_VERBOSE = "Client VERBOSE";
    private static final String LOG_ERROR = "Client ERROR";
    private static final String LOG_DEBUG = "Client DEBUG";
    private final ArrayAdapter<String> arrayAdapter;
    private final TextView realtimeText;
    private final SubtitleStorage subtitleStorage = new SubtitleStorage();
    private final AtomicBoolean stop = new AtomicBoolean(true);
    private final AtomicBoolean terminate = new AtomicBoolean(false);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String topic;

    private StompClient mStompClient;
    private final Object monitor = new Object();

    public ServiceClient(ArrayAdapter<String> arrayAdapter, TextView realtimeText)
    {
        this.arrayAdapter = arrayAdapter;
        this.realtimeText = realtimeText;
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
        String connectionURL = String.format("ws://%s:%d/gs-guide-websocket/websocket", serverAddr, serverPort);

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, connectionURL);

        AtomicBoolean connectSuccess = new AtomicBoolean(true);

        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            synchronized (monitor)
            {
                switch (lifecycleEvent.getType())
                {

                    case OPENED:
                        Log.d(LOG_VERBOSE, "Stomp connection opened");
                        monitor.notify();
                        break;

                    case ERROR:
                        Log.e(LOG_ERROR, "Error", lifecycleEvent.getException());
                        monitor.notify();
                        connectSuccess.set(false);
                        break;

                    case CLOSED:
                        Log.d(LOG_VERBOSE, "Stomp connection closed");
                        break;
                }
            }
        });

        mStompClient.connect();
        synchronized (monitor)
        {
            try
            {
                monitor.wait();
            }
            catch (InterruptedException e)
            {

            }
        }
        return connectSuccess.get();
    }

    private void subscribe(String topic)
    {
        Queue<String> fullSentences = subtitleStorage.getAllFinishedSentences();
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
                            realtimeText.setText(subtitleStorage.get());
                            String sentence = fullSentences.poll();
                            if (sentence!=null)
                                arrayAdapter.insert(sentence, 0);
                        } else
                        {
                            Log.v(LOG_VERBOSE, "Not subtitle!");
                        }

                    }
                    catch (IOException e)
                    {
                        Log.e(LOG_ERROR, e.getMessage());
                    }
                }, throwable -> Log.e(LOG_ERROR, "err!", throwable));
    }

    @Override
    public void run()
    {
        Log.v(LOG_VERBOSE, "Subscribing to topic using session " + mStompClient);
        this.subscribe(topic);
    }
}
