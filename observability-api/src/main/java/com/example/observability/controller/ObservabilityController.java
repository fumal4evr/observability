package com.example.observability.controller;

import com.example.observability.service.OrderService;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ObservabilityController {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityController.class);
    private final OrderService orderService;

    public ObservabilityController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/hello")
    @Observed(name = "api.hello", contextualName = "hello-endpoint")
    public ResponseEntity<Map<String, String>> hello() {
        log.info("Hello endpoint called");
        return ResponseEntity.ok(Map.of(
            "message", "Hello from Observability API",
            "instance", System.getenv().getOrDefault("INSTANCE_NAME", "unknown")
        ));
    }

    @PostMapping("/orders")
    @Observed(name = "api.create.order", contextualName = "create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData) {
        log.info("Creating order: {}", orderData);
        var result = orderService.processOrder(orderData);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders/{id}")
    @Observed(name = "api.get.order", contextualName = "get-order")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String id) {
        log.info("Fetching order: {}", id);
        var order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/{id}/status")
    @Observed(name = "api.order.status", contextualName = "order-status")
    public ResponseEntity<Map<String, String>> getOrderStatus(@PathVariable String id) {
        log.info("Fetching order status: {}", id);
        var status = orderService.getOrderStatus(id);
        return ResponseEntity.ok(Map.of("orderId", id, "status", status));
    }

    @PostMapping("/orders/{id}/process")
    @Observed(name = "api.process.order", contextualName = "process-order")
    public ResponseEntity<Map<String, String>> processOrder(@PathVariable String id) {
        log.info("Processing order: {}", id);
        orderService.processOrderAsync(id);
        return ResponseEntity.ok(Map.of("message", "Order processing started", "orderId", id));
    }

    @GetMapping("/call-other-instance")
    @Observed(name = "api.call.other", contextualName = "call-other-instance")
    public ResponseEntity<Map<String, Object>> callOtherInstance() {
        log.info("Calling other instance");
        var result = orderService.callOtherService();
        return ResponseEntity.ok(Map.of(
            "currentInstance", System.getenv().getOrDefault("INSTANCE_NAME", "unknown"),
            "otherInstanceResponse", result
        ));
    }

    @GetMapping("/slow")
    @Observed(name = "api.slow", contextualName = "slow-endpoint")
    public ResponseEntity<Map<String, String>> slowEndpoint() throws InterruptedException {
        log.info("Slow endpoint called");
        Thread.sleep(2000 + (long)(Math.random() * 1000));
        return ResponseEntity.ok(Map.of("message", "Completed after delay"));
    }

    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> errorEndpoint() {
        log.error("Error endpoint called");
        throw new RuntimeException("Intentional error for testing");
    }
}
