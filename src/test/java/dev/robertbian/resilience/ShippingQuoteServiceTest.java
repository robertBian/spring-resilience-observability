package dev.robertbian.resilience;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShippingQuoteServiceTest {
    @Test void returnsLiveRateWhenProviderIsHealthy() {
        var client = mock(UpstreamRateClient.class);
        when(client.fetch("IT")).thenReturn(new UpstreamRateClient.UpstreamRate("IT", new BigDecimal("6.50")));
        var registry = new SimpleMeterRegistry();
        var service = new ShippingQuoteService(client, ObservationRegistry.create(), registry);

        var quote = service.quote("IT");
        assertThat(quote.amount()).isEqualByComparingTo("6.50");
        assertThat(quote.degraded()).isFalse();
        assertThat(registry.counter("shipping.quotes", "result", "live").count()).isEqualTo(1);
    }

    @Test void fallbackIsExplicitlyMarkedAsEstimate() {
        var registry = new SimpleMeterRegistry();
        var service = new ShippingQuoteService(mock(UpstreamRateClient.class),
                ObservationRegistry.create(), registry);
        var quote = service.fallback("DE", new RuntimeException("upstream offline"));
        assertThat(quote.degraded()).isTrue();
        assertThat(quote.source()).isEqualTo("ESTIMATED");
        assertThat(registry.counter("shipping.quotes", "result", "fallback").count()).isEqualTo(1);
    }
}
