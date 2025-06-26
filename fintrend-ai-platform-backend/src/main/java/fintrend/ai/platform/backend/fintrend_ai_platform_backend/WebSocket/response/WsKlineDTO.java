package fintrend.ai.platform.backend.fintrend_ai_platform_backend.WebSocket.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WsKlineDTO {

    @JsonProperty("t")
    private long openTime;

    @JsonProperty("T")
    private long closeTime;

    @JsonProperty("s")
    private String symbol;

    @JsonProperty("i")
    private String interval;

    @JsonProperty("f")
    private long firstTradeId;

    @JsonProperty("L")
    private long lastTradeId;

    @JsonProperty("o")
    private String open;

    @JsonProperty("c")
    private String close;

    @JsonProperty("h")
    private String high;

    @JsonProperty("l")
    private String low;

    @JsonProperty("v")
    private String volume;

    @JsonProperty("n")
    private int numberOfTrades;

    @JsonProperty("x")
    private boolean isClosed;

    @JsonProperty("q")
    private String quoteAssetVolume;

    @JsonProperty("V")
    private String takerBuyBaseVolume;

    @JsonProperty("Q")
    private String takerBuyQuoteVolume;

    @JsonProperty("B")
    private String ignore; // Sometimes used as filler (can be safely ignored)
}