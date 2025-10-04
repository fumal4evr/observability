# Observability API - Bruno Collection

This Bruno collection contains all API endpoints for the Observability API project.

## Setup

1. Install Bruno from https://www.usebruno.com/
2. Open Bruno
3. Click "Open Collection"
4. Select this directory (`bruno-collection`)

## Environments

Two environments are pre-configured:

- **Local**: Use when running the application locally
- **Docker**: Use when running with docker-compose (same URLs as Local)

Both point to:
- Instance 1: http://localhost:8080
- Instance 2: http://localhost:8081

## Collection Structure

### 📁 Basic Endpoints
- Simple hello endpoints for both instances
- Test basic connectivity

### 📁 Order Management
- Create Order - Creates a new order and saves the order ID
- Get Order - Retrieves order details
- Get Order Status - Checks order status
- Process Order - Initiates async processing
- Complete Order Flow - End-to-end workflow

### 📁 Testing Endpoints
- Slow Endpoint - Artificial delay for testing latency
- Error Endpoint - Intentional error for testing error handling
- Load Test - Quick load generation

### 📁 Cross Instance
- Call Other Instance - Demonstrates distributed tracing
- Instance 1 to 2 - Explicit flow from instance 1
- Instance 2 to 1 - Explicit flow from instance 2

### 📁 Actuator Endpoints
- Health Check - Application health status
- Prometheus Metrics - Raw Prometheus metrics
- Available Metrics - List of all metrics
- HTTP Request Metrics - Detailed HTTP metrics
- Environment Info - Configuration and properties

## Usage Tips

### 1. Complete Order Workflow

Execute in order:
1. Create Order → Saves `orderId` variable
2. Get Order → Uses saved `orderId`
3. Get Order Status → Check it's "CREATED"
4. Process Order → Start async processing
5. Wait 2-3 seconds
6. Get Order Status → Should be "PROCESSED"

### 2. Test Distributed Tracing

1. Run "Call Other Instance"
2. Open Zipkin at http://localhost:9411
3. Find the trace
4. See parent-child span relationship

### 3. Generate Load for Metrics

1. Run "Load Test - Quick" multiple times
2. Or use multiple tabs
3. Check Prometheus: http://localhost:9090
4. Query: `rate(http_server_requests_seconds_count[1m])`

### 4. Monitor Performance

1. Run "Slow Endpoint" a few times
2. Check Prometheus for latency percentiles
3. Query: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[1m]))`

## Variables

The collection automatically manages these variables:

- `orderId` - Set when creating an order
- `lastOrderId` - Alternative order ID storage
- `baseUrl` - Base URL for API (from environment)
- `instance1` - URL for instance 1 (from environment)
- `instance2` - URL for instance 2 (from environment)

## Testing

Most requests include automated tests that verify:
- Response status codes
- Response structure
- Expected values
- Data types

Run a request and check the "Tests" tab to see results.

## Observability Links

After starting the application with `docker-compose up --build`:

- **Zipkin**: http://localhost:9411 - Distributed traces
- **Prometheus**: http://localhost:9090 - Metrics and queries
- **Grafana**: http://localhost:3000 - Dashboards (admin/admin)
- **API Docs**: Check the main project README

## Tips

- Use the "Run Folder" feature to execute all requests in a folder
- Check "Tests" tab after each request for validation results
- Use "Scripts" tab to see pre/post request logic
- Export results for reporting or analysis

## Troubleshooting

**Services not responding?**
```bash
# Check if services are running
docker-compose ps

# View logs
docker-compose logs -f api-instance-1
```

**Order ID not saved?**
- Check the "Tests" tab after "Create Order"
- Manually copy order ID from response
- Set it in environments: `orderId = "your-order-id"`

**Want to reset everything?**
```bash
docker-compose down -v
docker-compose up --build
```
