## RFQ & Purchase Order API Examples

### RFQ
```bash
# List RFQs
curl -X GET "http://localhost:8080/api/rfq?q=RFQ&status=DRAFT&page=0&size=20"

# Create RFQ (requires valid supplierId/itemId/uomId)
curl -X POST "http://localhost:8080/api/rfq" \
  -H "Content-Type: application/json" \
  -d '{
    "rfqNo": "RFQ-0001",
    "supplierId": 1,
    "rfqDate": "2025-01-15",
    "remarks": "Initial RFQ",
    "lines": [
      { "itemId": 1, "uomId": 1, "quantity": 10, "rateExpected": 100, "remarks": "Line 1" },
      { "itemId": 1, "uomId": 1, "quantity": 5, "rateExpected": 95, "remarks": "Line 2" }
    ]
  }'

# Update RFQ
curl -X PUT "http://localhost:8080/api/rfq/1" \
  -H "Content-Type: application/json" \
  -d '{
    "rfqNo": "RFQ-0001",
    "supplierId": 1,
    "rfqDate": "2025-01-15",
    "remarks": "Updated RFQ",
    "lines": [
      { "id": 1, "itemId": 1, "uomId": 1, "quantity": 12, "rateExpected": 98 }
    ]
  }'

# Submit / Approve RFQ
curl -X POST "http://localhost:8080/api/rfq/1/submit"
curl -X POST "http://localhost:8080/api/rfq/1/approve"
```

### Purchase Orders
```bash
# List POs
curl -X GET "http://localhost:8080/api/purchase-orders?q=PO&status=DRAFT&page=0&size=20"

# Create PO (requires valid supplierId/itemId/uomId)
curl -X POST "http://localhost:8080/api/purchase-orders" \
  -H "Content-Type: application/json" \
  -d '{
    "poNo": "PO-0001",
    "supplierId": 1,
    "poDate": "2025-01-15",
    "remarks": "Initial PO",
    "lines": [
      { "itemId": 1, "uomId": 1, "quantity": 10, "rate": 120, "amount": 1200 }
    ]
  }'

# Update PO
curl -X PUT "http://localhost:8080/api/purchase-orders/1" \
  -H "Content-Type: application/json" \
  -d '{
    "poNo": "PO-0001",
    "supplierId": 1,
    "poDate": "2025-01-15",
    "remarks": "Updated PO",
    "lines": [
      { "id": 1, "itemId": 1, "uomId": 1, "quantity": 12, "rate": 125, "amount": 1500 }
    ]
  }'

# Approve PO
curl -X POST "http://localhost:8080/api/purchase-orders/1/approve"
```
