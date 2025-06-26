package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.response.WsKlineDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BinanceMessageHandlerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void handleMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode kline = root.get("k");

            if (kline == null) {
                System.err.println("No 'k' field in message: " + message);
                return;
            }

            WsKlineDTO dto = objectMapper.treeToValue(kline, WsKlineDTO.class);

            // Send to WebSocket topic
            messagingTemplate.convertAndSend("/topic/kline", dto);

            // Print all kline data for debugging
            System.out.println("KlineDTO: " + dto);

            // Or print specific fields
            // System.out.println("Symbol: " + dto.getSymbol() + ", Close price: " + dto.getClose());
        } catch (Exception e) {
            System.err.println("Failed to parse kline message: " + e.getMessage());
        }
    }
}

