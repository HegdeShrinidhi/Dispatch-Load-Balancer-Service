# Dispatch Load Balancer Service

An optimized Spring Boot backend microservice that balances delivery loads across a vehicle fleet using a priority-driven Greedy routing algorithm, constrained by vehicle payload capacities and calculated using the Haversine distance formula.

---

## 📋 Evaluation Deliverables
* **Loom Demonstration Video:** https://www.loom.com/share/f9d992724c0246df8d084bbe27d1a1ca 
* **Live Interactive Swagger UI:** https://dispatch-load-balancer-service.onrender.com 

---

## 🛠️ Architecture & Core Engineering Decisions

1. **Decoupled Strategy Design Pattern:** The routing core is completely abstracted behind a clean `DispatchOptimizer` interface. This allows alternative optimization engines to be hot-swapped seamlessly in the future without modifying controller or repository classes.
2. **Transactional Batch Upserts:** High-volume data operations use a single `@Transactional` session block via `saveAll()`. This minimizes database round-trips for high-volume datasets and automatically handles record overrides gracefully if an identical ID is submitted again.
3. **Mathematical Clamping Constraints:** Input coordinates are strictly validated for geographic boundaries (-90 to 90 for latitude, -180 to 180 for longitude). The trignometric calculations wrap variables in a defensive `Math.max(0.0, Math.min(1.0, a))` clamp to prevent floating-point precision loss or `NaN` runtime thread failures.

---

## 🛰️ API Specifications & Validation Constraints

### 1. Register Fleet Details
* **Endpoint:** `POST /api/dispatch/vehicles`
* **Payload Structure:** Registers available carriers. Re-submitting an existing `vehicleId` triggers a clean record updates process.
```json
{
  "vehicles": [
    {
      "vehicleId": "VEH001",
      "capacity": 100.0,
      "currentLatitude": 28.7041,
      "currentLongitude": 77.1025,
      "currentAddress": "Karol Bagh, Delhi, India"
    }
  ]
}
```

### 2. Register Delivery Orders
* **Endpoint:** `POST /api/dispatch/orders`
* **Payload Structure:** Urgency is constrained to strict `HIGH`, `MEDIUM`, or `LOW` validation enums.
```json
{
  "orders": [
    {
      "orderId": "ORD001",
      "latitude": 28.6139,
      "longitude": 77.2090,
      "address": "Connaught Place, Delhi, India",
      "packageWeight": 15.0,
      "priority": "HIGH"
    }
  ]
}
```

### 3. Generate Optimized Dispatch Plan
* **Endpoint:** `GET /api/dispatch/plan`
* **Payload Output:** Orders are grouped by priority first. Distances are dynamically rounded and converted into requested string formats (`"X km"`) to preserve exact automated parsing requirements.
```json
{
  "dispatchPlan": [
    {
      "vehicleId": "VEH001",
      "totalLoad": 15.0,
      "totalDistance": "12 km",
      "assignedOrders": [
        {
          "orderId": "ORD001",
          "latitude": 28.6139,
          "longitude": 77.2090,
          "address": "Connaught Place, Delhi, India",
          "packageWeight": 15.0,
          "priority": "HIGH"
        }
      ]
    }
  ]
}
```

---

## 🚀 Local Run & Verification Steps

### Prerequisites
* **Java 21** or higher installed
* **Maven** (Bundled wrapper scripts included)

### 1. Execute Automated Testing Matrix
Run the comprehensive suite of 26 tests covering data sanity thresholds, complex cross-hemisphere polar routes, and automated scale performance stress-profiling:
```bash
# On Windows PowerShell
.\mvnw clean test

# On Linux / macOS
./mvnw clean test
```

### 2. Start the Local Server Context
Launch the embedded application server framework:
```bash
# On Windows PowerShell
.\mvnw spring-boot:run

# On Linux / macOS
./mvnw spring-boot:run
```
The application will spin up successfully on default port `8080`.

### 3. Access Local Specifications Docs
* **Interactive UI:** `http://localhost:8080/swagger-ui/index.html`
* **Raw Specs Object:** `http://localhost:8080/v3/api-docs`
