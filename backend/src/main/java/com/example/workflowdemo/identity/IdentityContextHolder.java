package com.example.workflowdemo.identity;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class IdentityContextHolder {

    public static final String USER_HEADER = "X-User-Id";
    public static final String ROLES_HEADER = "X-User-Roles";

    private static final ThreadLocal<MockIdentity> CONTEXT = new ThreadLocal<>();

    private IdentityContextHolder() {
    }

    public static void set(String userId, Set<String> roles) {
        if (!StringUtils.hasText(userId) && (roles == null || roles.isEmpty())) {
            CONTEXT.remove();
            return;
        }
        CONTEXT.set(new MockIdentity(StringUtils.hasText(userId) ? userId.trim() : null, roles));
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Optional<MockIdentity> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static String requireUserId(String override) {
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        return get()
                .map(MockIdentity::userId)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("Missing user identity. Provide X-User-Id header or include userId in the payload."));
    }

    public static String requireRole(String override) {
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        return get()
                .flatMap(MockIdentity::primaryRole)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("Missing user role. Provide X-User-Roles header or specify the role explicitly."));
    }

    public static Set<String> parseRoles(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return Set.of();
        }
        Set<String> roles = new LinkedHashSet<>();
        Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(role -> roles.add(role.toLowerCase()));
        return roles;
    }
}
