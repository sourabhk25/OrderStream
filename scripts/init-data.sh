#!/bin/bash

echo "========================================="
echo "OrderStream - Initializing Sample Data"
echo "========================================="

# Wait for services to be ready
echo "Waiting for Inventory Service to be ready..."
sleep 5

# Add sample inventory products
echo ""
echo "Adding sample products to inventory..."

curl -X POST "http://localhost:8083/api/inventory?productId=PROD-001&productName=Dell%20XPS%2015%20Laptop&quantity=100"
echo " ✓ Added: Dell XPS 15 Laptop (100 units)"

curl -X POST "http://localhost:8083/api/inventory?productId=PROD-002&productName=Logitech%20MX%20Master%203&quantity=500"
echo " ✓ Added: Logitech MX Master 3 (500 units)"

curl -X POST "http://localhost:8083/api/inventory?productId=PROD-003&productName=Mechanical%20Keyboard&quantity=300"
echo " ✓ Added: Mechanical Keyboard (300 units)"

curl -X POST "http://localhost:8083/api/inventory?productId=PROD-004&productName=4K%20Monitor&quantity=150"
echo " ✓ Added: 4K Monitor (150 units)"

curl -X POST "http://localhost:8083/api/inventory?productId=PROD-005&productName=USB-C%20Hub&quantity=1000"
echo " ✓ Added: USB-C Hub (1000 units)"

echo ""
echo "========================================="
echo "Sample data initialized successfully!"
echo "========================================="
echo ""
echo "You can now create orders with these product IDs:"
echo "  - PROD-001: Dell XPS 15 Laptop"
echo "  - PROD-002: Logitech MX Master 3"
echo "  - PROD-003: Mechanical Keyboard"
echo "  - PROD-004: 4K Monitor"
echo "  - PROD-005: USB-C Hub"
echo ""
echo "Create a test order:"
echo 'curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '"'"'{"customerId":"CUST-001","productId":"PROD-001","quantity":2,"totalAmount":1999.99}'"'"
echo ""
