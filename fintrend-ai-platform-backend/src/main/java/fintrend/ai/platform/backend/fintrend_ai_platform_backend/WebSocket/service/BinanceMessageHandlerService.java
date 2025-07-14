package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.response.WsKlineDTO;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BinanceMessageHandlerService {

    private final BinanceWebSocketReadService readService;
    private final PricePredictionService predictionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public BinanceMessageHandlerService(BinanceWebSocketReadService binanceWebSocketReadService, PricePredictionService predictionService) {
        this.readService = binanceWebSocketReadService;
        this.predictionService = predictionService;
    }

    public void handleMessage(String message) {
        try {

            JsonNode root = objectMapper.readTree(message);
            JsonNode kline = root.get("k");

            if (kline == null) return;

            WsKlineDTO newCandle = objectMapper.treeToValue(kline, WsKlineDTO.class);
            newCandle.setClosed(kline.get("x").asBoolean());

            long openTime = kline.get("t").asLong(); // timestamp (ms)
            String closePrice = kline.get("c").asText();
            boolean isClosed = kline.get("x").asBoolean();

            System.out.println("🕒 Time (openTime): " + new Date(openTime));
            System.out.println("💰 Close price: " + closePrice);
            System.out.println("✅ Is closed: " + isClosed);

            if (!newCandle.isClosed()) return; // chỉ predict nếu nến đã đóng

            // 1. Fetch 49 historical candles
            List<WsKlineDTO> history = readService.fetchHistoricalKlines(
                    newCandle.getSymbol(), newCandle.getInterval(), 49);

            // 2. Append current candle
            history.add(newCandle);

            // 3. Tạo list object đúng định dạng
            List<Map<String, Object>> predictionInput = history.stream()
                    .map(candle -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("ds", Instant.ofEpochMilli(candle.getCloseTime())
                                            .toString()); // Hoặc ISO format
                        row.put("y", Double.parseDouble(candle.getClose()));
                        row.put("sentiment_score", 0.3); // dummy
                        return row;
                    })
                    .collect(Collectors.toList());

            // 4. Gửi sang model
            Double predicted = predictionService.predictPrice(predictionInput);

            // 5. In kết quả
            System.out.println("Predicted close: " + predicted);


            // // 6. Send predicted value to frontend (optional)
            // Map<String, Object> response = new HashMap<>();
            // response.put("symbol", newCandle.getSymbol());
            // response.put("interval", newCandle.getInterval());
            // response.put("latest_close", newCandle.getClose());
            // response.put("predicted_close", predicted);

            // messagingTemplate.convertAndSend("/topic/prediction", response);

        } catch (Exception e) {
            System.err.println("Failed to parse kline message: " + e.getMessage());
        }
    }
}

