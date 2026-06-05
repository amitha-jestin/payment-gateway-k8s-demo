package com.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — single entry point for all client traffic.
 *
 * In Kubernetes mode, service discovery is handled entirely by the cluster:
 *   - Each microservice is registered as a Kubernetes Service (ClusterIP).
 *   - CoreDNS resolves "user-service" → ClusterIP of the user-service Service.
 *   - kube-proxy load-balances across all healthy pods behind that Service.
 *
 * This means NO Eureka, NO Spring Cloud LoadBalancer, and NO lb:// URIs.
 * Routes simply point to http://<kubernetes-service-name>.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
