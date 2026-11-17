package dev.robertbian.resilience;

import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UpstreamRateClient {
    private final RestClient restClient;
    public UpstreamRateClient(RestClient restClient) { this.restClient = restClient; }

    /** Retry lives in a separate Spring bean to keep proxy/advice ordering explicit. */
    @Retry(name = "shipping-upstream")
    public UpstreamRate fetch(String country) {
        UpstreamRate rate = restClient.get().uri("/rates/{country}", country)
                .retrieve().body(UpstreamRate.class);
        if (rate == null || rate.amount() == null || rate.amount().signum() < 0) {
            throw new IllegalStateException("Invalid response from upstream service");
        }
        return rate;
    }

    public record UpstreamRate(String country, BigDecimal amount) {}
}
