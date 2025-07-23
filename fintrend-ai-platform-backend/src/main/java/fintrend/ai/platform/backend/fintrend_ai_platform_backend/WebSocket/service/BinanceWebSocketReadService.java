package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.ModelEvaluation.service.ModelEvaluationService;
import fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.response.WsKlineDTO;

@Service
public class BinanceWebSocketReadService {

    private final PricePredictionService pricePredictionService;
    private final ModelEvaluationService modelEvaluationService;

    public BinanceWebSocketReadService(PricePredictionService pricePredictionService, ModelEvaluationService modelEvaluationService) {
        this.pricePredictionService = pricePredictionService;
        this.modelEvaluationService = modelEvaluationService;
    } 
    
    public List<WsKlineDTO> fetchHistoricalKlines(String symbol, String interval, int limit) {
        String url = String.format("https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=%d",
                symbol.toUpperCase(), interval, limit);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<WsKlineDTO> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode arr = mapper.readTree(response.getBody());
            for (JsonNode k : arr) {
                WsKlineDTO dto = new WsKlineDTO();
                dto.setOpenTime(k.get(0).asLong());
                dto.setOpen(k.get(1).asText());
                dto.setHigh(k.get(2).asText());
                dto.setLow(k.get(3).asText());
                dto.setClose(k.get(4).asText());
                dto.setVolume(k.get(5).asText());
                dto.setCloseTime(k.get(6).asLong());
                dto.setNumberOfTrades(k.get(8).asInt());
                dto.setQuoteAssetVolume(k.get(7).asText());
                dto.setTakerBuyBaseVolume(k.get(9).asText());
                dto.setTakerBuyQuoteVolume(k.get(10).asText());
                dto.setIgnore(k.get(11).asText());

                dto.setSymbol(symbol);
                dto.setInterval(interval);
                dto.setClosed(true);

                result.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public double getModelEvaluation() {
        int numCandles = 99; // 50 combinations for 50 actualPrice and 50 predictedPrice
        List<WsKlineDTO> candles = fetchHistoricalKlines("BTCUSDT", "1m", numCandles);
        
        List<Double> predictedPrices = new ArrayList<>();

        for (int i = 0; i < 49; i++) {
            List<Map<String, Object>> predictionInput = candles.subList(i, i + 50).stream()
                    .map(candle -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("ds", Instant.ofEpochMilli(candle.getCloseTime())
                                            .toString()); // Hoặc ISO format
                        row.put("y", Double.parseDouble(candle.getClose()));
                        row.put("sentiment_score", 0.3); // dummy
                        return row;
                    })
                    .collect(Collectors.toList());
            double predicted = pricePredictionService.predictPrice(predictionInput);
            predictedPrices.add(predicted);
        }

        List<Double> actualPrices = candles.subList(50, 99).stream()
                .map(candle -> Double.parseDouble(candle.getClose()))
                .collect(Collectors.toList());
        
        return modelEvaluationService.calculateMAPE(actualPrices, predictedPrices);
    }
}
