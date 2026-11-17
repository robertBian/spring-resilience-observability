package dev.robertbian.resilience;

import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/shipping")
public class ShippingQuoteController {
    private final ShippingQuoteService service;
    public ShippingQuoteController(ShippingQuoteService service) { this.service = service; }

    @GetMapping("/quotes/{country}")
    public ShippingQuoteService.ShippingQuote quote(
            @PathVariable @Pattern(regexp="^[A-Z]{2}$") String country) {
        return service.quote(country);
    }
}
