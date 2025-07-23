package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PricePredictionService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Double predictPrice(List<Map<String, Object>> inputList) {
        String url = "http://localhost:8000/predict";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(inputList, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            System.out.println("Raw response: " + response.getBody());
            Object predicted = response.getBody().get("predictedClose");
            return predicted instanceof Number ? ((Number) predicted).doubleValue() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
