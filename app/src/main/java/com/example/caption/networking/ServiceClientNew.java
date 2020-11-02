//package com.example.caption.networking;
//
//import android.util.Log;
//import android.widget.ArrayAdapter;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import org.springframework.messaging.simp.stomp.StompFrameHandler;
//import org.springframework.messaging.simp.stomp.StompHeaders;
//import org.springframework.messaging.simp.stomp.StompSession;
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
//import org.springframework.util.concurrent.ListenableFuture;
//import org.springframework.web.socket.WebSocketHttpHeaders;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//import org.springframework.web.socket.sockjs.client.SockJsClient;
//import org.springframework.web.socket.sockjs.client.Transport;
//import org.springframework.web.socket.sockjs.client.WebSocketTransport;
//import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;
//
//import java.io.IOException;
//import java.lang.reflect.Type;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import tech.gusavila92.websocketclient.WebSocketClient;
//
//public class ServiceClientNew extends Thread
//{
//    private final String WEB_PATH = "10.0.2.2";
//    private final int PORT = 23330;
//
//
//    private static final String LOG_VERBOSE = "Client VERBOSE";
//    private static final String LOG_DEBUG = "Client DEBOG";
//    private final ArrayAdapter<String> arrayAdapter;
//    private final SubtitleStorage subtitleStorage = new SubtitleStorage();
//
//    private final AtomicBoolean stop = new AtomicBoolean(true);
//    private final AtomicBoolean terminate = new AtomicBoolean(false);
//
//
//    public ServiceClientNew(ArrayAdapter<String> arrayAdapter) throws URISyntaxException
//    {
//        this.arrayAdapter = arrayAdapter;
//
//    }
//
//
//    private ListenableFuture<StompSession> connect()
//    {
//
//        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
//        List<Transport> transports = Collections.singletonList(webSocketTransport);
//
//        SockJsClient sockJsClient = new SockJsClient(transports);
//        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());
//
//        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
//
//        String url = "ws://{host}:{port}/gs-guide-websocket";
//        return stompClient.connect(url, headers, new MyHandler(), WEB_PATH, PORT);
//    }
//
//    private void subscribeGreetings(StompSession stompSession) throws ExecutionException, InterruptedException
//    {
//        //TODO: change the topic
//        stompSession.subscribe("/topic/test", new ClientStompFrameHandler());
//        Log.v(LOG_VERBOSE, "subscribe done");
//    }
//
//    public void stopClient()
//    {
//        stop.set(true);
//    }
//
//    public void resumeClient()
//    {
//        stop.set(false);
//    }
//
//    public void terminate()
//    {
//        terminate.set(true);
//    }
//
//    // need to call this first before run()
//    public boolean initConnection()
//    {
//        ListenableFuture<StompSession> f = this.connect();
//        try
//        {
//            stompSession = f.get();
//            return stompSession != null;
//        }
//        catch (InterruptedException | ExecutionException e)
//        {
//            Log.e(LOG_DEBUG, e.getMessage());
//            return false;
//        }
//    }
//
//    @Override
//    public void run()
//    {
//        Log.v(LOG_VERBOSE, "Subscribing to topic using session " + stompSession);
//        try
//        {
//            this.subscribeGreetings(stompSession);
//        }
//        catch (ExecutionException | InterruptedException e)
//        {
//            Log.e(LOG_DEBUG, e.getMessage());
//        }
////        new Scanner(System.in).nextLine();
//        while (!terminate.get())
//        {
//            try
//            {
//                Thread.sleep(100);
//            }
//            catch (InterruptedException ignored)
//            {
//            }
//        }
//    }
//}
