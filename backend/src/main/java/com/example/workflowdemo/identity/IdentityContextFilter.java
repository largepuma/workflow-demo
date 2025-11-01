package com.example.workflowdemo.identity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(10)
public class IdentityContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userId = request.getHeader(IdentityContextHolder.USER_HEADER);
            var roles = IdentityContextHolder.parseRoles(request.getHeader(IdentityContextHolder.ROLES_HEADER));
            IdentityContextHolder.set(userId, roles);
            filterChain.doFilter(request, response);
        } finally {
            IdentityContextHolder.clear();
        }
    }
}
