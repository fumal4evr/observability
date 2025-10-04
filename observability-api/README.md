# Spring Boot Observability API

A production-ready Spring Boot API designed for exploring observability concepts including metrics, distributed tracing, and monitoring with two instances running in Docker Compose.

## Features

- **Distributed Tracing**: Zipkin integration for tracking requests across services
- **Metrics**: Prometheus metrics with Micrometer
- **Observations**: Custom observations with spans for detailed monitoring
- **Health Checks**: Spring Boot Actuator endpoints
- **Multi-Instance**: Two API instances for testing distributed scenarios
- **Visualization**: Grafana dashboards for metrics

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Gradle (included via wrapper)

## Quick Start

```bash
# Start everything
docker-compose up --build

# Test the setup
curl http://localhost:8080/api/hello
```

## Access Services

- API Instance 1: http://localhost:8080
- API Instance 2: http://localhost:8081
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Zipkin: http://localhost:9411

## API Endpoints

- `GET /api/hello` - Simple hello endpoint
- `POST /api/orders` - Create a new order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/{id}/status` - Get order status
- `POST /api/orders/{id}/process` - Process an order
- `GET /api/call-other-instance` - Call the other API instance
- `GET /api/slow` - Slow endpoint (2-3 seconds)
- `GET /api/error` - Error endpoint for testing

## Testing

```bash
# Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"type": "electronics", "quantity": 2}'

# Test distributed tracing
curl http://localhost:8080/api/call-other-instance

# Generate load
for i in {1..50}; do curl http://localhost:8080/api/hello; done
```

## Development

```bash
# Build the project
./gradlew build

# Run locally
./gradlew bootRun

# Run tests
./gradlew test
```

## Useful Prometheus Queries

```promql
# Request rate
rate(http_server_requests_seconds_count[1m])

# 95th percentile latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[1m]))

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[1m])
```

## Cleanup

```bash
docker-compose down -v
```
