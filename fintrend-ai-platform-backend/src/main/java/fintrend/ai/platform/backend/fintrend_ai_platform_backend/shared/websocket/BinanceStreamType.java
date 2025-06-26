package fintrend.ai.platform.backend.fintrend_ai_platform_backend.shared.websocket;

public enum BinanceStreamType {
    KLINE;

    public String buildStream(String symbol, String interval) {
        switch (this) {
            case KLINE:
                return symbol.toLowerCase() + "@kline_" + interval;
            default:
                throw new IllegalArgumentException("Unsupported stream type");
        }
    }
}
