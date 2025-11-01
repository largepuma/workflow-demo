package com.example.workflowdemo.identity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class MockIdentity {

    private final String userId;
    private final Set<String> roles;

    public MockIdentity(String userId, Set<String> roles) {
        this.userId = userId;
        this.roles = roles == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(roles));
    }

    public String userId() {
        return userId;
    }

    public Set<String> roles() {
        return roles;
    }

    public Optional<String> primaryRole() {
        return roles.stream().findFirst();
    }

    public boolean hasRole(String role) {
        return role != null && roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }
}
