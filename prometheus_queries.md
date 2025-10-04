# Prometheus Queries for Order Metrics

Now that we've added custom metrics with the `order.type` tag, here are useful Prometheus queries you can use.

## Order Creation Metrics

### Total Orders Created by Type
```promql
orders_created_total
```

### Orders Created Rate (per second) by Type
```promql
rate(orders_created_total[1m])
```

### Total Orders per Type (Histogram view)
```promql
sum by (type) (orders_created_total)
```

### Order Creation Rate by Type and Instance
```promql
sum by (type, instance) (rate(orders_created_total[5m]))
```

## Order Processing Time Metrics

### Average Processing Time by Type
```promql
rate(orders_processing_duration_seconds_sum[5m]) / rate(orders_processing_duration_seconds_count[5m])
```

### 95th Percentile Processing Time by Type
```promql
histogram_quantile(0.95, sum by (type, le) (rate(orders_processing_duration_seconds_bucket[5m])))
```

### 99th Percentile Processing Time by Type
```promql
histogram_quantile(0.99, sum by (type, le) (rate(orders_processing_duration_seconds_bucket[5m])))
```

### Max Processing Time by Type
```promql
orders_processing_time_seconds_max{type=~".*"}
```

## Comparison Queries

### Compare Order Volumes Across Types
```promql
sum by (type) (increase(orders_created_total[1h]))
```

### Which Order Type is Most Common?
```promql
topk(3, sum by (type) (orders_created_total))
```

### Order Type Distribution (Percentage)
```promql
sum by (type) (orders_created_total) / ignoring(type) group_left sum(orders_created_total) * 100
```

## Time-based Analysis

### Orders Created in Last Hour by Type
```promql
increase(orders_created_total[1h])
```

### Orders Created Today by Type
```promql
increase(orders_created_total[24h])
```

### Order Creation Trend (5-minute rate)
```promql
rate(orders_created_total[5m])
```

## Combined Metrics

### Orders per Instance per Type
```promql
sum by (instance, type) (orders_created_total)
```

### Average Orders per Minute by Type
```promql
rate(orders_created_total[5m]) * 60
```

## Alerting Queries

### Slow Order Processing (over 500ms average)
```promql
rate(orders_processing_duration_seconds_sum[5m]) / rate(orders_processing_duration_seconds_count[5m]) > 0.5
```

### High Order Volume for Specific Type
```promql
rate(orders_created_total{type="premium"}[5m]) > 10
```

### No Orders Created in Last 5 Minutes
```promql
increase(orders_created_total[5m]) == 0
```

## Using in Grafana

### Create a Bar Chart Panel
```promql
sum by (type) (orders_created_total)
```
Visualization: Bar Chart or Pie Chart

### Create a Time Series Graph
```promql
sum by (type) (rate(orders_created_total[1m]))
```
Visualization: Time Series

### Create a Stat Panel (Single Value)
```promql
sum(orders_created_total)
```
Visualization: Stat

### Create a Histogram Panel
```promql
histogram_quantile(0.95, sum by (type, le) (rate(orders_processing_duration_seconds_bucket[5m])))
```
Visualization: Time Series with multiple series

## Testing Your Metrics

1. **Create Different Order Types:**
```bash
# Electronics
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"type": "electronics", "quantity": 1}'

# Premium
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"type": "premium", "quantity": 1}'

# Standard
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"type": "standard", "quantity": 1}'

# Books
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"type": "books", "quantity": 5}'
```

2. **Generate Volume for Histogram:**
```bash
# Create 50 orders of different types
for i in {1..50}; do
  TYPES=("electronics" "premium" "standard" "books" "clothing")
  TYPE=${TYPES[$RANDOM % ${#TYPES[@]}]}
  curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
    -d "{\"type\": \"$TYPE\", \"quantity\": $((RANDOM % 10 + 1))}"
done
```

3. **View in Prometheus:**
   - Go to http://localhost:9090
   - Enter any query above
   - Click "Execute"
   - Switch to "Graph" tab for time series

4. **Create Grafana Dashboard:**
   - Go to http://localhost:3000
   - Create new dashboard
   - Add panel with queries above
   - Choose appropriate visualization (Bar, Pie, Time Series)

## Key Differences: Observations vs Custom Metrics

| Feature | Observations (Zipkin Tags) | Custom Metrics (Prometheus) |
|---------|---------------------------|----------------------------|
| Purpose | Distributed tracing | Aggregated metrics |
| Granularity | Per request/span | Aggregated over time |
| Storage | Individual traces | Time series data |
| Querying | By trace ID | By labels/time ranges |
| Use Case | Debugging specific requests | Understanding patterns/trends |
| Cardinality | High (unique per trace) | Low (grouped by labels) |

**The key insight:** 
- Observations with tags (like `order.type`) are perfect for **tracing individual requests** in Zipkin
- Custom metrics with tags are needed for **aggregated analysis** in Prometheus/Grafana

Both are valuable and complementary!
