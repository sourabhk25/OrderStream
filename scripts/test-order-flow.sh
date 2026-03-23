#!/bin/bash

echo "========================================="
echo "OrderStream - Testing Order Flow"
echo "========================================="

# Create a test order
echo ""
echo "Creating test order..."

RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "productId": "PROD-001",
    "quantity": 2,
    "totalAmount": 1999.99
  }')

echo "$RESPONSE" | jq .

ORDER_NUMBER=$(echo "$RESPONSE" | jq -r '.orderNumber')

echo ""
echo "✓ Order created: $ORDER_NUMBER"

# Track order status
echo ""
echo "Tracking order status (will check every 2 seconds for 20 seconds)..."

for i in {1..10}
do
  echo ""
  echo "Check #$i:"
  ORDER_STATUS=$(curl -s http://localhost:8080/api/orders/$ORDER_NUMBER)
  STATUS=$(echo "$ORDER_STATUS" | jq -r '.status')
  echo "  Status: $STATUS"

  if [ "$STATUS" == "COMPLETED" ] || [ "$STATUS" == "PAYMENT_FAILED" ] || [ "$STATUS" == "INVENTORY_FAILED" ]; then
    echo ""
    echo "========================================="
    echo "Order flow completed!"
    echo "Final status: $STATUS"
    echo "========================================="
    exit 0
  fi

  sleep 2
done

echo ""
echo "========================================="
echo "Monitoring complete. Check Kafka UI for detailed event flow:"
echo "http://localhost:8090"
echo "========================================="
