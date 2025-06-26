package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.websocket.BinanceStreamType;
import jakarta.annotation.PreDestroy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BinanceWebSocketClientService {

    private final Map<String, WebSocket> activeStreams = new ConcurrentHashMap<>();

    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/";

    private final HttpClient client = HttpClient.newHttpClient();

    @Autowired
    private BinanceMessageHandlerService messageHandler;

    public void subscribe(String symbol, String interval) {
        String streamKey = BinanceStreamType.KLINE.buildStream(symbol.toLowerCase(), interval);
        if (activeStreams.containsKey(streamKey)) {
            System.out.println("Already subscribed to: " + streamKey);
            return;
        }

        String url = BINANCE_WS_URL + streamKey;

        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        messageHandler.handleMessage(data.toString());
                        webSocket.request(1);
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .thenAccept(ws -> {
                    activeStreams.put(streamKey, ws);
                    System.out.println("Connected to Binance WebSocket: " + url);
                })
                .exceptionally(e -> {
                    System.err.println("Failed to connect to Binance WebSocket: " + e.getMessage());
                    return null;
                });
    }

    public void unsubscribe(String symbol, String interval) {
        String streamKey = symbol.toLowerCase() + "@" + interval;
        WebSocket ws = activeStreams.remove(streamKey);
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "User requested close");
            System.out.println("Closed WebSocket for: " + streamKey);
        } else {
            System.out.println("No active stream to close for: " + streamKey);
        }
    }

    @PreDestroy
    public void shutdownAll() {
        activeStreams.values().forEach(ws -> ws.sendClose(WebSocket.NORMAL_CLOSURE, "App shutdown"));
        activeStreams.clear();
    }
}
