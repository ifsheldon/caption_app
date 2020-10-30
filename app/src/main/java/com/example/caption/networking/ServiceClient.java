package com.example.caption.networking;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

class ServiceClient extends Thread
{
    private static class MyHandler extends StompSessionHandlerAdapter
    {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders)
        {
            Log.v(LOG_VERBOSE, "Now connected");
        }
    }

    private class ClientStompFrameHandler implements StompFrameHandler
    {
        final ObjectMapper mapper = new ObjectMapper();

        public Type getPayloadType(StompHeaders stompHeaders)
        {
            return byte[].class;
        }

        public void handleFrame(StompHeaders stompHeaders, Object o)
        {
//            Log.v(LOG_VERBOSE, "Received greeting " + new String((byte[]) o));
            if(stop.get())
                return;

            try
            {
                Map<String, Object> m1 = mapper.readValue(new String((byte[]) o), Map.class);
                Log.v(LOG_VERBOSE, m1.toString());
                String eventName = ((Map<String, Object>) m1.get("header")).get("name").toString();
                // get subtitles here
                String text = ((Map<String, Object>) m1.get("payload")).get("result").toString();

                if ("TranscriptionResultChanged".equals(eventName))
                {
                    subtitleStorage.add(text, false);
                } else if ("SentenceEnd".equals(eventName))
                {
                    subtitleStorage.add(text, true);
                }
                //TODO: check whether it works
                arrayAdapter.add(subtitleStorage.get());
            }
            catch (IOException e)
            {
                Log.e(LOG_DEBUG, e.getMessage());
            }
        }

    }

    private static final String LOG_VERBOSE = "Client VERBOSE";
    private static final String LOG_DEBUG = "Client DEBOG";
    private final ArrayAdapter<String> arrayAdapter;
    private final SubtitleStorage subtitleStorage = new SubtitleStorage();
    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final AtomicBoolean terminate = new AtomicBoolean(false);
    private StompSession stompSession = null;

    public ServiceClient(ArrayAdapter<String> arrayAdapter)
    {
        this.arrayAdapter = arrayAdapter;
    }


    private ListenableFuture<StompSession> connect()
    {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        String url = "ws://{host}:{port}/gs-guide-websocket";
        // TODO: notice this special path for Android virtual device
        String webPath = "10.0.2.2";
        return stompClient.connect(url, headers, new MyHandler(), webPath, 23330);
    }

    private void subscribeGreetings(StompSession stompSession) throws ExecutionException, InterruptedException
    {
        //TODO: change the topic
        stompSession.subscribe("/topic/test", new ClientStompFrameHandler());
        Log.v(LOG_VERBOSE, "subscribe done");
    }

    public void stopClient()
    {
        stop.set(true);
    }

    public void resumeClient()
    {
        stop.set(false);
    }

    // need to call this first before run()
    public boolean initConnection()
    {
        ListenableFuture<StompSession> f = this.connect();
        try
        {
            stompSession = f.get();
            return stompSession != null;
        }
        catch (InterruptedException | ExecutionException e)
        {
            Log.e(LOG_DEBUG, e.getMessage());
            return false;
        }
    }

    @Override
    public void run()
    {
        Log.v(LOG_VERBOSE, "Subscribing to topic using session " + stompSession);
        try
        {
            this.subscribeGreetings(stompSession);
        }
        catch (ExecutionException | InterruptedException e)
        {
            Log.e(LOG_DEBUG, e.getMessage());
        }
//        new Scanner(System.in).nextLine();
        while (!terminate.get())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ignored)
            {
            }
        }
    }
}
