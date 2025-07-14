package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.response.WsKlineDTO;

@Service
public class BinanceWebSocketReadService {
    
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

}
