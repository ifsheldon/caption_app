package com.example.caption.networking;

public interface IServerClient {

    boolean initConnection(String topic);
    void start();
    void terminate();
    void resumeClient();
    void stopClient();

}
