
[![CI](https://github.com/kmarymee/inventory_app/actions/workflows/ci.yaml/badge.svg)](https://github.com/kmarymee/inventory_app/actions/workflows/ci.yaml)

# Inventory App - Microservices on Kubernetes

This is an app that I've built to learn Java, Docker, Github Actions, and Kubernetes. The goal was to create a CRUD application that could be split into independently scalable microservices running on a local KIND cluster. The project also has a basic CI pipeline that runs when the main branch is pushed to.


---

## Architecture
The CRUD application is a basic inventory-tracking app with two database tables: Products and Categories.
Each database table receives it's own postgres container, and microservice container that scales automatically under load.

In a monolithic app, the product table would contain a foreign key to point to a category table. Here, requisite validations from the product service are conducted via HTTP requests across the Kubernetes Service DNS to the category service, demonstrating scalable microservices with effective communication between them.

Ordinarily, a gateway would be included to route requests from clients to the appropriate service, however this was beyond the scope of the bandwidth I had for this project, and is not strictly necessary to demonstrate independent scalability.


### Components

- category-service: Handles CRUD requests relating to the category section of the app.
    - category-postgres: Private postgres database for the category service
    - category HPA: Scales the category-service pods under load

- product-service: handles CRUD requests and calls category-service to validate category database keys.
    - product-postgres: Private postgres database for the product service
    - product HPA: Scales the product-service pods under load

---

## Key Design Decisions

**2-service split (product + category)**
The CRUD app is split in two to demonstrate the independent scalability of each service.
This allows a portion of the app to scale relative to the amount of compute it requires instead of superfluously scaling the entire application.

**Database-per-service**
Each service is given its own database. This is intended to decouple the services as completely as possible. If the services shared a database, the purpose of the microservices architecture would be defeated by linking the services through a shared dependency. A secondary benefit of this design is resource isolation, since neither database will inherit load from the other.
A notable drawback of this strategy is the fact that an HTTP call from the product service to the category service is required to retrieve necessary validation. 

**Lean reads**
When HTTP requests are sent between the services, they are intentionally kept very light, restricted to boolean validation of row existence only. This is to reduce HTTP traffic across the services, thus decreasing latency. When information is required from both services, that can be accomplished on the front end in a separate request with the database key provided by the product service.

---

## Running It

### Prerequisites
- Running KIND cluster
- Installed metrics-server for Kubernetes
- Docker Desktop

### Build & deploy
Inside the repo, the services can be found under product-service and category-service.
Inside each service there is a Dockerfile that can be used to build the images.
```bash
cd category-service && docker build -t category-service:latest . && cd ..
cd product-service  && docker build -t product-service:latest  . && cd ..
```
Next, the images may be loaded into KIND, and the Kubernetes manifests may be applied:
```bash
kind load docker-image category-service:latest --name dev
kind load docker-image product-service:latest  --name dev

kubectl apply -f k8s/
```

From there, each service will spin up two app pods and a postgres pod, as can be observed with the following commands:
```bash
kubectl get pods
kubectl get hpa
```

### API usage
Once the services are operational, they can be accessed by port-forwarding each service in separate terminals:
```bash
kubectl port-forward svc/category-service 8081:8081   # terminal 1
kubectl port-forward svc/product-service  8080:8080   # terminal 2
```

Now we can interact with the API!
```bash
# Category creation
curl -X POST http://localhost:8081/categories -H "Content-Type: application/json" -d '{"name":"Electronics"}'

# Product creation
curl -X POST http://localhost:8080/products -H "Content-Type: application/json" -d '{"name":"Laptop","price":999.99,"quantity":5,"categoryId":1}'

# Product creation with an invalid categoryId (fails, demonstrating cross-service validation)
curl -X POST http://localhost:8080/products -H "Content-Type: application/json" -d '{"name":"Ghost","price":10,"quantity":1,"categoryId":999}'
```


---

## Load Test - Independent Scaling

### Method
I used Hey to load test the application, hitting only the product service to demonstrate independent scalability:
```bash
hey -z 3m -c 50 http://localhost:8080/products
```

### Results
```
NAME              REFERENCE                    TARGETS       MINPODS   MAXPODS   REPLICAS   AGE
product-service   Deployment/product-service   cpu: 1%/50%   2         10        2          29m
product-service   Deployment/product-service   cpu: 26%/50%   2         10        2          30m
product-service   Deployment/product-service   cpu: 250%/50%   2         10        2          30m
product-service   Deployment/product-service   cpu: 251%/50%   2         10        4          31m
product-service   Deployment/product-service   cpu: 250%/50%   2         10        8          31m
product-service   Deployment/product-service   cpu: 377%/50%   2         10        10         31m
product-service   Deployment/product-service   cpu: 219%/50%   2         10        10         31m
product-service   Deployment/product-service   cpu: 108%/50%   2         10        10         32m
product-service   Deployment/product-service   cpu: 51%/50%    2         10        10         32m
product-service   Deployment/product-service   cpu: 14%/50%    2         10        10         33m
product-service   Deployment/product-service   cpu: 1%/50%     2         10        10         34m
product-service   Deployment/product-service   cpu: 2%/50%     2         10        10         34m
product-service   Deployment/product-service   cpu: 1%/50%     2         10        10         34m
product-service   Deployment/product-service   cpu: 1%/50%     2         10        10         37m
product-service   Deployment/product-service   cpu: 1%/50%     2         10        10         38m
product-service   Deployment/product-service   cpu: 2%/50%     2         10        3          38m
```
As shown above, the product service will appropriately scale independently under load!



---

## Tech Stack
- Java 21
- Spring Boot 3.5
- Spring Data JPA
- RestClient
- PostgreSQL
- Docker
- Kubernetes
- KIND
- GitHub Actions