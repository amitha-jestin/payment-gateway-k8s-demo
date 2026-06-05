package com.platform.user.controller;

import com.platform.user.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * In Kubernetes, each pod gets a unique hostname equal to its pod name.
 * The "pod" field in every response is read from the OS hostname, making
 * it easy to see kube-proxy load-balancing traffic across replicas:
 *
 *   for i in {1..6}; do curl -s http://<gateway>/users | jq .pod; done
 *   "user-service-6d8f9b-xk2p1"
 *   "user-service-6d8f9b-mn7q3"
 *   "user-service-6d8f9b-xk2p1"   ← round-robins between the 3 replicas
 *   ...
 */
@RestController
@RequestMapping("/users")
public class UserController {

    // In a pod, System.getenv("HOSTNAME") == the pod name  e.g. user-service-6d8f9b-xk2p1
    private static final String POD = System.getenv().getOrDefault("HOSTNAME", "localhost");

    private static final List<User> USERS = List.of(
            new User(1L, "Alice Johnson", "alice@platform.com"),
            new User(2L, "Bob Smith",     "bob@platform.com"),
            new User(3L, "Carol White",   "carol@platform.com")
    );

    @GetMapping
    public Map<String, Object> getAllUsers() {
        return Map.of(
                "service", "user-service",
                "pod",     POD,
                "users",   USERS
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        User user = USERS.stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        return Map.of(
                "service", "user-service",
                "pod",     POD,
                "user",    user
        );
    }
}
