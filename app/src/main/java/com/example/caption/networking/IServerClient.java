package com.example.caption.networking;

public interface IServerClient {

    boolean initConnection(String topic, String serverAddr, int serverPort);
    void start();
    void terminate();
    void resumeClient();
    void stopClient();

}
