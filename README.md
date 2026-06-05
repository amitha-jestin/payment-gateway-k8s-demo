# Payment Platform — Kubernetes Edition

A production-shaped microservices demo using **Java 21**, **Spring Boot 3.x**, and **Spring Cloud Gateway**, running on **Kubernetes** with zero external service registries.

---

## Architecture

```
 ┌─────────────────────────────────────────────────────────────────┐
 │  Kubernetes Cluster                                             │
 │                                                                 │
 │   External Traffic                                              │
 │        │                                                        │
 │        ▼                                                        │
 │  ┌─────────────┐  NodePort :30080                              │
 │  │ api-gateway │──────────────────────────────────────┐        │
 │  │  (1 pod)    │                                      │        │
 │  └──────┬──────┘                                      │        │
 │         │  http://user-service    (Kubernetes DNS)    │        │
 │         │  http://order-service                       │        │
 │         │  http://payment-service                     │        │
 │         │                                             │        │
 │  ┌──────▼──────────────────────────────┐             │        │
 │  │         CoreDNS                     │             │        │
 │  │  resolves service names → ClusterIP │             │        │
 │  └──────┬──────────────────────────────┘             │        │
 │         │                                             │        │
 │  ┌──────▼──────┐  ┌─────────────┐  ┌──────────────┐ │        │
 │  │user-service │  │order-service│  │payment-service│ │        │
 │  │  ClusterIP  │  │  ClusterIP  │  │   ClusterIP  │ │        │
 │  └──────┬──────┘  └──────┬──────┘  └──────┬───────┘ │        │
 │         │                │                 │         │        │
 │    ┌────┴────┐      ┌────┴────┐      ┌────┴────┐    │        │
 │    │ pod  1  │      │ pod  1  │      │ pod  1  │    │        │
 │    │ pod  2  │      │ pod  2  │      │ pod  2  │    │        │
 │    │ pod  3  │      └─────────┘      └─────────┘    │        │
 │    └─────────┘                                       │        │
 │    (3 replicas — kube-proxy load-balances these)     │        │
 │                                                      │        │
 └──────────────────────────────────────────────────────┘        
```

### How Kubernetes replaces Eureka

| Eureka model                        | Kubernetes equivalent                                  |
|-------------------------------------|--------------------------------------------------------|
| Service registers itself on startup | Pod is created with a `label` (e.g. `app=user-service`)|
| Eureka stores IP + port             | Service object watches pods matching the label         |
| Client queries Eureka for IPs       | CoreDNS resolves `user-service` → ClusterIP            |
| Client-side load balancer picks one | kube-proxy IPTables rules distribute traffic           |
| `lb://user-service` in gateway      | `http://user-service` — plain HTTP, no lb:// needed    |

The application code and config become **simpler** in Kubernetes mode: no Eureka client dependency, no heartbeat config, and no `lb://` URIs in the gateway.

---

## Project Structure

```
payment-platform-k8s/
│
├── pom.xml                              ← parent POM
│
├── api-gateway/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../ApiGatewayApplication.java
│       └── resources/application.yml   ← routes use http://service-name
│
├── user-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── UserServiceApplication.java
│       ├── model/User.java
│       └── controller/UserController.java
│
├── order-service/                       ← same structure
├── payment-service/                     ← same structure
│
└── k8s/
    ├── gateway/
    │   └── deployment.yml              ← Deployment + NodePort Service
    ├── user-service/
    │   └── deployment.yml              ← Deployment (3 replicas) + ClusterIP
    ├── order-service/
    │   └── deployment.yml              ← Deployment (2 replicas) + ClusterIP
    └── payment-service/
        └── deployment.yml              ← Deployment (2 replicas) + ClusterIP
```

---

## Prerequisites

| Tool       | Version  | Purpose                          |
|------------|----------|----------------------------------|
| Java       | 21+      | Build                            |
| Maven      | 3.9+     | Build                            |
| Docker     | 24+      | Build and tag images             |
| Minikube   | 1.32+    | Local Kubernetes cluster         |
| kubectl    | 1.29+    | Apply manifests, check status    |

> **Alternative to Minikube:** [kind](https://kind.sigs.k8s.io/) works equally well.  
> For kind, replace `minikube image load` with `kind load docker-image`.

---

## Step-by-Step: Build and Deploy

### 1. Start Minikube

```bash
minikube start
```

### 2. Build the JAR files

```bash
mvn clean package -DskipTests
```

### 3. Build Docker images inside Minikube's Docker daemon

Building inside Minikube means the images are immediately available to the cluster — no registry push needed.

```bash
# Point your shell at Minikube's Docker daemon
eval $(minikube docker-env) or minikube docker-env | Invoke-Expression

# Build all four images
docker build -f api-gateway/Dockerfile -t payment-platform/api-gateway:1.0.0 .
docker build -f user-service/Dockerfile -t payment-platform/user-service:1.0.0 .
docker build -f order-service/Dockerfile -t payment-platform/order-service:1.0.0 .
docker build -f payment-service/Dockerfile -t payment-platform/payment-service:1.0.0 .

# Verify
docker images | grep payment-platform
```

### 4. Deploy to Kubernetes

```bash
kubectl apply -f k8s/user-service/deployment.yml
kubectl apply -f k8s/order-service/deployment.yml
kubectl apply -f k8s/payment-service/deployment.yml
kubectl apply -f k8s/gateway/deployment.yml
```

### 5. Wait for all pods to be Ready

```bash
kubectl get pods -w
```

Expected output (wait until all show `Running`):
```
NAME                               READY   STATUS    RESTARTS
api-gateway-7d9f6b-abc12           1/1     Running   0
order-service-5c8d4f-def34         1/1     Running   0
order-service-5c8d4f-ghi56         1/1     Running   0
payment-service-6b7e3a-jkl78       1/1     Running   0
payment-service-6b7e3a-mno90       1/1     Running   0
user-service-4a2c1d-pqr12          1/1     Running   0
user-service-4a2c1d-stu34          1/1     Running   0
user-service-4a2c1d-vwx56          1/1     Running   0
```

### 6. Get the gateway URL

```bash
minikube service api-gateway --url
# → http://192.168.49.2:30080
```

Export it for the commands below:
```bash
export GW=$(minikube service api-gateway --url)
```

---

## Testing the Endpoints

```bash
# All users
curl $GW/users

# Single user
curl $GW/users/1

# All orders
curl $GW/orders

# Orders for user 1
curl $GW/orders/user/1

# All payments
curl $GW/payments

# Payment for order 101
curl $GW/payments/order/101
```

---

## Observing Kubernetes Load Balancing

The `"pod"` field in every response is the pod's hostname (= pod name).  
With 3 user-service replicas, kube-proxy distributes requests round-robin:

```bash
for i in {1..9}; do
  curl -s $GW/users | grep '"pod"'
done
```

Expected output — the pod name rotates across the three replicas:
```
"pod": "user-service-4a2c1d-pqr12"
"pod": "user-service-4a2c1d-stu34"
"pod": "user-service-4a2c1d-vwx56"
"pod": "user-service-4a2c1d-pqr12"
"pod": "user-service-4a2c1d-stu34"
...
```

This is **server-side load balancing** by kube-proxy — no client-side code required.

---

## Scaling

```bash
# Scale user-service to 5 replicas
kubectl scale deployment user-service --replicas=5

# Watch new pods come up
kubectl get pods -l app=user-service -w

# Scale back down
kubectl scale deployment user-service --replicas=3
```

---

## Useful kubectl Commands

```bash
# Check all resources
kubectl get all

# Pod logs (replace <pod-name> from kubectl get pods output)
kubectl logs <pod-name>

# Describe a Service (shows Endpoints = pod IPs)
kubectl describe service user-service

# Check gateway routes via actuator
curl $GW/actuator/gateway/routes | jq .

# Health check
curl $GW/actuator/health
```

---

## Teardown

```bash
kubectl delete -f k8s/gateway/deployment.yml
kubectl delete -f k8s/user-service/deployment.yml
kubectl delete -f k8s/order-service/deployment.yml
kubectl delete -f k8s/payment-service/deployment.yml

minikube stop
```

---

## Port Reference

| Component       | Container Port | Service Port | External (NodePort) |
|-----------------|---------------|-------------|----------------------|
| api-gateway     | 8080          | 8080        | **30080**            |
| user-service    | 8080          | **80**      | none (ClusterIP)     |
| order-service   | 8080          | **80**      | none (ClusterIP)     |
| payment-service | 8080          | **80**      | none (ClusterIP)     |

> Note: all containers listen on **8080** but the ClusterIP Services expose port **80**.  
> `http://user-service` (no port) resolves to port 80 → forwarded to container:8080.

---

## Replica Count Summary

| Service         | Replicas | Reason                                  |
|-----------------|----------|-----------------------------------------|
| api-gateway     | 1        | Entry point; scale up for HA in prod    |
| user-service    | **3**    | Primary demo of load balancing          |
| order-service   | 2        | Demonstrates HA for a critical service  |
| payment-service | 2        | Demonstrates HA for a critical service  |

---

## What This Demo Does NOT Include (Intentionally)

- Ingress controller (routes are kept internal via NodePort for simplicity)  
- Service mesh (Istio / Linkerd)  
- Persistent storage (JPA / databases)  
- ConfigMaps / Secrets (environment config)  
- Horizontal Pod Autoscaler  
- Observability (Prometheus, Grafana, Zipkin)  

These are standard production additions, left out to keep the focus on Kubernetes-native **service discovery** and **load balancing**.
