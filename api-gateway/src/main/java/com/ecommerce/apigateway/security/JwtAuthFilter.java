package com.ecommerce.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.Enumeration;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    /**
     * Secret key used to verify JWT signature.
     * This must be the SAME secret used by Auth Service to generate JWT.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * This filter executes ONCE per request at API Gateway level.
     * Responsibility:
     *  - Validate JWT for protected routes
     *  - Extract user context (id, email, role)
     *  - Propagate user context to downstream services via headers
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.debug("Incoming request path: {}", path);

        /**
         * STEP 1: Allow public endpoints WITHOUT JWT
         * - Auth endpoints (login/register)
         * - Product browsing APIs
         */
        if (path.startsWith("/auth") || path.startsWith("/api/products")) {
            logger.debug("Public endpoint accessed, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        /**
         * STEP 2: Extract Authorization header
         */
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            logger.warn("Blocking request {} : Authorization header missing", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: JWT token missing");
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Blocking request {} : Invalid Authorization header format", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid Authorization header");
            return;
        }

        /**
         * STEP 3: Extract JWT token value (remove 'Bearer ')
         */
        String token = authHeader.substring(7);

        try {
            // 3️⃣ Validate JWT
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

              String userId = claims.getSubject();
             String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            logger.info(
                    "[GATEWAY] JWT validated | userId={} | email={} | role={}",
                    userId, email, role
            );

            // ================= ROLE-BASED AUTHORIZATION =================

            // API path user is trying to access
            String requestPath = request.getRequestURI();

            //User Should not manage Products
            if(requestPath.startsWith("/api/product") && "USER".equalsIgnoreCase(role)){
                logger.warn(
                        "[GATEWAY] Access denied | role = {} | path = {}",role,requestPath
                );
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                        "Forbidden: USER is not allowed to manage products"
                );
                return; // stop request here
            }

            //Seller should not place order
            if(requestPath.startsWith("/api/order") && "SELLER".equalsIgnoreCase(role)){
                logger.warn(
                        "[GATEWAY] Access denied | role = {} | path =  {}",
                        role,requestPath
                );
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                        "Forbidden: SELLER is not allowed to place order"
                );
                return; //Stop request here
            }

            //=========================== END ROLE CHECK ===============================

            // 4️⃣ Wrap request and inject headers (MVC CORRECT WAY)
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {

                @Override
                public String getHeader(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return userId;
                    }
                    if ("X-User-Email".equalsIgnoreCase(name)) {
                        return email;
                    }
                    if ("X-User-Role".equalsIgnoreCase(name)) {
                        return role;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Id".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(userId));
                    }
                    if ("X-User-Email".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(email));
                    }
                    if ("X-User-Role".equalsIgnoreCase(name)) {
                        return Collections.enumeration(List.of(role));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> headers = Collections.list(super.getHeaderNames());
                    headers.add("X-User-Id");
                    headers.add("X-User-Email");
                    headers.add("X-User-Role");
                    return Collections.enumeration(headers);
                }
            };

            logger.info("[GATEWAY] Injected headers: X-User-Id, X-User-Email, X-User-Role");

            // 5️⃣ Forward request
            filterChain.doFilter(wrappedRequest, response);

        } catch (Exception ex) {
            logger.warn("[GATEWAY] Unauthorized request - invalid or expired JWT");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
        }
    }
}
