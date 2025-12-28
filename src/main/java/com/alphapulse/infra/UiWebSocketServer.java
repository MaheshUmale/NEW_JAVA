package com.alphapulse.infra;

import com.alphapulse.events.AlphaSignalEvent;
import com.alphapulse.events.AlphaSignalListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * A WebSocket server that listens for alpha signals and broadcasts them to connected UI clients.
 */
public class UiWebSocketServer extends WebSocketServer implements AlphaSignalListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UiWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // We don't expect messages from the client in this simple implementation
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully on port " + getPort());
    }

    @Override
    public void onAlphaSignal(AlphaSignalEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            broadcast(jsonEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
