
[![CI](https://github.com/kmarymee/inventory_app/actions/workflows/ci.yaml/badge.svg)](https://github.com/kmarymee/inventory_app/actions/workflows/ci.yaml)

# Inventory App — Microservices on Kubernetes

<!-- ONE PARAGRAPH: What is this? A CRUD inventory system split from a monolith into
independently deployable, independently scalable microservices, running on a local
KIND cluster. State the goal in one line: demonstrate that one service can scale under
load while its peer stays idle — the thing a monolith can't do. -->



---

## Architecture

<!-- Describe the shape. Two domain services + per-service Postgres. Mention the gateway
was deliberately scoped out (see Limitations). A simple text/ASCII diagram or a bullet
list of the components works well here:

  Client → product-service → (HTTP, Service DNS) → category-service
              ↓                                          ↓
         product-postgres                          category-postgres

Explain in 2-3 sentences:
 - product-service owns Product, category-service owns Category
 - they communicate over Kubernetes Service DNS (http://category-service:8081)
 - each service has its OWN private Postgres (database-per-service)
 - the one inter-service call: product validates categoryId against category-service on writes
-->

### Components

<!-- Quick table or list. For each: what it is, what port, what it owns. Example rows:
 - product-service  (8080) — Product CRUD; calls category-service to validate categoryId
 - category-service (8081) — Category CRUD; owns nothing downstream (pure leaf)
 - product-postgres / category-postgres — private DBs, one per service
 - HPA per service; metrics-server provides CPU metrics
-->

---

## Key Design Decisions

<!-- This is your STRONGEST section — it shows reasoning, not just code. For each decision,
write 1-2 sentences: what you chose, why, and the tradeoff. Pull from what you actually
reasoned through. Prompts below — answer each in your own words. -->

**3-service split (gateway + product + category) → reduced to 2 domain services**
<!-- Why split per-entity rather than one domain service? (independent scaling demo needs
two comparable peers). Why did the gateway get cut? (deadline; it routes, doesn't prove
the scaling thesis). -->

**Database-per-service (not shared DB)**
<!-- Why? (each service owns its data exclusively; shared DB is the coupling anti-pattern
microservices exist to avoid). What's the cost you accepted? (two Postgres instances). -->

**Lean reads (ProductResponse returns bare categoryId, not embedded category data)**
<!-- Why? (avoids N+1 network calls on GET; keeps the read path free of inter-service
calls so load-testing product-service measures ITS cpu, not category's latency). -->

**Boolean existence check (not Optional<CategoryResponse>)**
<!-- Why a yes/no instead of returning the category data? (lean reads mean you never use
the body — you only need to know it exists). Note when you'd choose differently (a richer
app that caches category data). -->

**RestClient (not RestTemplate or WebClient)**
<!-- Why? (synchronous fits a "wait for the answer before saving" model; modern API;
no reactive stack needed for one blocking validation call). -->

**Monorepo (not polyrepo)**
<!-- Why? (one-person project, one clone, one `kubectl apply` story; CI complexity like
path filters/matrix is opt-in). Note the tradeoff (real orgs often use polyrepo for
independent team ownership). -->

**The inter-service tradeoff you accepted**
<!-- IMPORTANT — this shows distributed-systems awareness. In the monolith, creating a
product was one local transaction. Now it fans out to a network call to category-service.
You gained independent scaling; you accepted that product writes now depend on
category-service being reachable. Real systems manage this with timeouts/retries/circuit
breakers — out of scope here, named on purpose. -->

---

## Running It

### Prerequisites
<!-- KIND cluster running, metrics-server installed, Docker Desktop. -->

### Build & deploy
```bash
# Build images
cd category-service && docker build -t category-service:latest . && cd ..
cd product-service  && docker build -t product-service:latest  . && cd ..

# Load images into KIND (imagePullPolicy: Never — uses local images, no registry)
kind load docker-image category-service:latest --name dev
kind load docker-image product-service:latest  --name dev

# Deploy everything (both services + their Postgres + HPAs)
kubectl apply -f k8s/

# Verify
kubectl get pods        # expect 2 category, 2 product, 1 each postgres
kubectl get hpa
```

### Exercise the API
```bash
kubectl port-forward svc/category-service 8081:8081   # terminal 1
kubectl port-forward svc/product-service  8080:8080   # terminal 2

# Create a category
curl -X POST http://localhost:8081/categories -H "Content-Type: application/json" -d '{"name":"Electronics"}'

# Create a product in that category (succeeds — category exists)
curl -X POST http://localhost:8080/products -H "Content-Type: application/json" -d '{"name":"Laptop","price":999.99,"quantity":5,"categoryId":1}'

# Try a bogus category (fails with 400 — cross-service validation)
curl -X POST http://localhost:8080/products -H "Content-Type: application/json" -d '{"name":"Ghost","price":10,"quantity":1,"categoryId":999}'
```
<!-- Optionally paste the actual 400 response body here as proof:
     {"error":"Category with id 999 does not exist."} -->

---

## Load Test — Independent Scaling

<!-- THE PAYOFF. Describe the test and paste your REAL numbers. -->

### Method
<!-- Tool (hey), command, and WHY you hit GET /products specifically (CPU-bound, no
inter-service call, so load lands purely on product-service). -->
```bash
hey -z 3m -c 50 http://localhost:8080/products
```

### Results
<!-- Paste your actual HPA readouts. The story your numbers tell:
 - product-service: cpu 1% → 26% → 250% → 377%, REPLICAS 2 → 4 → 8 → 10 (hit max)
 - category-service: held flat at 1-2%, REPLICAS stayed at 2 the entire time
This contrast IS the thesis. A monolith would have had to scale the whole app. -->

<!-- Consider a small before/after table:

| Service           | CPU under load | Replicas (start → peak) |
|-------------------|----------------|--------------------------|
| product-service   | up to ~377%    | 2 → 10                   |
| category-service  | 1–2% (idle)    | 2 → 2 (unchanged)        |
-->

### What this proves
<!-- One paragraph: only the hot service scaled; its peer was untouched because each
service owns its own resources and its own HPA. This is impossible in a monolith. -->

---

## Observations & Limitations

<!-- The "I know what I didn't do and why" section — this reads as senior. -->

**HPA scale-down lag / startup CPU**
<!-- Your sharp observation. Two real things to capture:
 (1) After load eased, the HPA held at 10 replicas for several minutes before draining
     down — this is the intended stabilization window (eager up, reluctant down ~5min),
     not a bug.
 (2) JVM/Spring Boot warmup spikes CPU at pod startup, which CAN feed an HPA misleading
     readings and destabilize autoscaling. Mitigations NOT implemented here but known:
     readiness probes (don't count a pod until warmup settles), HPA `behavior` policies
     to tune scale rates, CPU requests sized for steady-state, JVM warmup tuning. -->

**Gateway deferred**
<!-- Scoped out under deadline. It routes/aggregates but doesn't prove the scaling thesis.
Would add as Spring Cloud Gateway or a RestClient proxy. -->

**Schema management: ddl-auto=update**
<!-- Fine for a learning build; production would use Flyway/Liquibase migrations instead
of letting Hibernate manage schema. -->

**Dev/prod parity (H2 vs Postgres)**
<!-- Local dev defaults to H2 for speed; all deployed environments use Postgres via
${VAR:default} env binding. Verified against in-cluster Postgres. (A hardcoded H2 driver
line, invisible locally, only surfaced against real Postgres in-cluster — a concrete
reminder of why parity matters.) -->

**No inter-service resilience**
<!-- Product writes depend on category-service being up. No timeout/retry/circuit-breaker.
Named as a known gap. -->

---

## Tech Stack

<!-- Quick list: Java 21, Spring Boot 3.5, Spring Data JPA, RestClient, PostgreSQL 16 /
H2, Docker (multi-stage builds), Kubernetes (KIND), metrics-server, HPA. -->