package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service.BinanceWebSocketClientService;

@Component
public class WebSocketStartupRunner implements CommandLineRunner {

    private final BinanceWebSocketClientService binanceWebSocketClient;

    public WebSocketStartupRunner(BinanceWebSocketClientService binanceWebSocketClient) {
        this.binanceWebSocketClient = binanceWebSocketClient;
    }

    @Override
    public void run(String... args) {
        // Start the Binance WebSocket client
        binanceWebSocketClient.subscribe("btcusdt", "1m");
        System.out.println("Binance WebSocket client started successfully.");
    }
}
