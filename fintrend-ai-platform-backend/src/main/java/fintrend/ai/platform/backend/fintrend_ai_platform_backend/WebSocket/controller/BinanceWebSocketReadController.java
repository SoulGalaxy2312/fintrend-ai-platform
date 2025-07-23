package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.response.WsKlineDTO;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service.BinanceWebSocketReadService;

@RestController
public class BinanceWebSocketReadController {

    private final BinanceWebSocketReadService bianceWebSocketReadService;

    public BinanceWebSocketReadController(BinanceWebSocketReadService bianceWebSocketReadService) {
        this.bianceWebSocketReadService = bianceWebSocketReadService;
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<WsKlineDTO>> getHistory(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam(defaultValue = "50") int limit
    ) {
        List<WsKlineDTO> data = bianceWebSocketReadService.fetchHistoricalKlines(symbol, interval, limit);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/model-evaluation")
    public String getModelEvaluation() {
        return "MAPE: " + String.valueOf(bianceWebSocketReadService.getModelEvaluation());
    }

}
