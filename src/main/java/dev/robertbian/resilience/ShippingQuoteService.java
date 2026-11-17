package dev.robertbian.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ShippingQuoteService {
    private final UpstreamRateClient client;
    private final ObservationRegistry observations;
    private final MeterRegistry meters;

    public ShippingQuoteService(UpstreamRateClient client, ObservationRegistry observations, MeterRegistry meters) {
        this.client = client;
        this.observations = observations;
        this.meters = meters;
    }

    @CircuitBreaker(name = "shipping-circuit", fallbackMethod = "fallback")
    public ShippingQuote quote(String country) {
        var upstream = Observation.createNotStarted("shipping.upstream", observations)
                .lowCardinalityKeyValue("dependency", "rate-service")
                .observe(() -> client.fetch(country));
        meters.counter("shipping.quotes", "result", "live").increment();
        return new ShippingQuote(country, upstream.amount(), "LIVE", false);
    }

    public ShippingQuote fallback(String country, Throwable reason) {
        meters.counter("shipping.quotes", "result", "fallback").increment();
        // Explicitly label this as an estimate; do not pretend the price is authoritative.
        return new ShippingQuote(country, new BigDecimal("9.99"), "ESTIMATED", true);
    }

    public record ShippingQuote(String country, BigDecimal amount, String source, boolean degraded) {}
}
