package com.example.observability.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> orderCountersByType = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> orderTimersByType = new ConcurrentHashMap<>();

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Increment the counter for orders created by type
     */
    public void incrementOrderCreated(String orderType) {
        orderCountersByType.computeIfAbsent(orderType, type ->
                Counter.builder("orders.created")
                        .tag("type", type)
                        .description("Number of orders created by type")
                        .register(meterRegistry)
        ).increment();
    }

    /**
     * Record the processing time for orders by type
     */
    public void recordOrderProcessingTime(String orderType, long durationMillis) {
        orderTimersByType.computeIfAbsent(orderType, type ->
                Timer.builder("orders.processing.time")
                        .tag("type", type)
                        .description("Order processing time by type")
                        .register(meterRegistry)
        ).record(java.time.Duration.ofMillis(durationMillis));
    }

    /**
     * Create a timer sample to measure duration
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop the timer and record with the given tags
     */
    public void stopTimer(Timer.Sample sample, String orderType) {
        sample.stop(Timer.builder("orders.processing.duration")
                .tag("type", orderType)
                .description("Duration of order processing by type")
                .register(meterRegistry));
    }
}