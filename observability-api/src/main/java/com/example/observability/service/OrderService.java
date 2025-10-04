package com.example.observability.service;

import com.example.observability.config.OrderMetrics;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final ObservationRegistry observationRegistry;
    private final OrderMetrics orderMetrics;
    private final RestClient restClient;
    private final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();
    
    @Value("${other.service.url:http://localhost:8081}")
    private String otherServiceUrl;

    public OrderService(ObservationRegistry observationRegistry, RestClient restClient, OrderMetrics orderMetrics) {
        this.orderMetrics = orderMetrics;
        this.observationRegistry = observationRegistry;
        this.restClient = restClient;
    }

    @Observed(name = "kk.service.process.order", contextualName = "kk-process-order-service")
    public Map<String, Object> processOrder(Map<String, Object> orderData) {
        String orderId = UUID.randomUUID().toString();
        String orderType = orderData.getOrDefault("type", "standard").toString();

        // Start timing for this order type
        var timerSample = orderMetrics.startTimer();

        return Observation.createNotStarted("order.validation", observationRegistry)
            .lowCardinalityKeyValue("order.type", orderType)
            .observe(() -> {
                log.info("Validating order {}", orderId);
                
                // Simulate validation
                simulateWork(100, 300);
                
                Map<String, Object> order = Map.of(
                    "id", orderId,
                    "status", "CREATED",
                    "data", orderData,
                    "timestamp", System.currentTimeMillis()
                );
                
                orders.put(orderId, order);
                log.info("Order created: {}", orderId);

                // Increment counter for this order type
                orderMetrics.incrementOrderCreated(orderType);

                // Stop timer and record duration
                orderMetrics.stopTimer(timerSample, orderType);
                
                return order;
            });
    }

    @Observed(name = "kk.service.get.order", contextualName = "kk-get-order-service")
    public Map<String, Object> getOrder(String orderId) {
        return Observation.createNotStarted("order.retrieval", observationRegistry)
            .lowCardinalityKeyValue("order.id", orderId)
            .observe(() -> {
                simulateWork(50, 150);
                var order = orders.getOrDefault(orderId, Map.of(
                    "id", orderId,
                    "status", "NOT_FOUND"
                ));
                log.info("Retrieved order: {}", orderId);
                return order;
            });
    }

    @Observed(name = "kk.service.order.status", contextualName = "kk-order-status-service")
    public String getOrderStatus(String orderId) {
        simulateWork(30, 100);
        var order = orders.get(orderId);
        if (order != null) {
            return order.getOrDefault("status", "UNKNOWN").toString();
        }
        return "NOT_FOUND";
    }

    @Observed(name = "kk.service.process.async", contextualName = "kk-process-order-async")
    public void processOrderAsync(String orderId) {
        Observation.createNotStarted("order.async.processing", observationRegistry)
            .lowCardinalityKeyValue("order.id", orderId)
            .observe(() -> {
                log.info("Async processing for order: {}", orderId);
                simulateWork(500, 1000);
                
                var order = orders.get(orderId);
                if (order != null) {
                    order.put("status", "PROCESSED");
                    order.put("processedAt", System.currentTimeMillis());
                }
                
                log.info("Order processed: {}", orderId);
                return null;
            });
    }

    @Observed(name = "kk.service.call.other", contextualName = "kk-call-other-service")
    public Map<String, String> callOtherService() {
        return Observation.createNotStarted("http.client.call", observationRegistry)
            .lowCardinalityKeyValue("http.url", otherServiceUrl)
            .observe(() -> {
                try {
                    log.info("Calling other service at: {}", otherServiceUrl);
                    var response = restClient.get()
                        .uri(otherServiceUrl + "/api/hello")
                        .retrieve()
                        .body(Map.class);
                    
                    log.info("Received response from other service");
                    return (Map<String, String>) response;
                } catch (Exception e) {
                    log.error("Error calling other service", e);
                    return Map.of("error", "Failed to call other service: " + e.getMessage());
                }
            });
    }

    private void simulateWork(int minMs, int maxMs) {
        try {
            Thread.sleep((long) (minMs + Math.random() * (maxMs - minMs)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
