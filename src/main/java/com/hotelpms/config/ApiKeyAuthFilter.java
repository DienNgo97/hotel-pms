package com.hotelpms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Bao ve API + cac thao tac ghi cua Admin UI bang header X-API-KEY (hoac admin token).
 *
 * <ul>
 *   <li><b>/api/**</b>: luon yeu cau X-API-KEY hop le (booking-platform goi qua header nay).</li>
 *   <li><b>/admin/** GET</b>: van xem duoc tu do trong dev (chi doc, khong doi state).</li>
 *   <li><b>/admin/** POST/PUT/DELETE/PATCH</b> (PROV-X1): cac thao tac PHA HUY (xoa room type,
 *       huy reservation, set gia/ton kho) yeu cau auth — chap nhan cung X-API-KEY HOAC admin token
 *       ({@code app.admin-ui.token}, mac dinh = api-key). Co the mo lai hoan toan cho dev demo bang
 *       {@code app.admin-ui.public=true} (mac dinh false).</li>
 * </ul>
 *
 * <p>So sanh key dung {@link MessageDigest#isEqual} de constant-time, tranh timing oracle (PROV-X2).
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";

    private final String validKey;
    /** Token rieng cho admin UI; mac dinh fallback ve api-key de dev demo van chay duoc. */
    private final String adminToken;
    /** Neu true: admin UI public hoan toan (ke ca POST) — chi nen bat trong dev demo. */
    private final boolean adminUiPublic;

    public ApiKeyAuthFilter(@Value("${app.api-key}") String validKey,
                            @Value("${app.admin-ui.token:${app.api-key}}") String adminToken,
                            @Value("${app.admin-ui.public:false}") boolean adminUiPublic) {
        this.validKey = validKey;
        this.adminToken = adminToken;
        this.adminUiPublic = adminUiPublic;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1) Moi request /api/** phai co X-API-KEY hop le.
        if (uri.startsWith("/api/")) {
            String provided = request.getHeader(HEADER);
            if (!matches(validKey, provided)) {
                unauthorized(response, "Invalid API key");
                return;
            }
        }
        // 2) Cac thao tac GHI cua admin UI cung phai co auth (tru khi admin-ui public).
        else if (uri.startsWith("/admin/") && isWrite(request) && !adminUiPublic) {
            String provided = request.getHeader(HEADER);
            if (!matches(validKey, provided) && !matches(adminToken, provided)) {
                unauthorized(response, "Admin write requires X-API-KEY");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWrite(HttpServletRequest request) {
        String method = request.getMethod();
        return HttpMethod.POST.matches(method)
                || HttpMethod.PUT.matches(method)
                || HttpMethod.DELETE.matches(method)
                || HttpMethod.PATCH.matches(method);
    }

    /** So sanh constant-time, null-safe (PROV-X2). */
    private boolean matches(String expected, String provided) {
        if (expected == null || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
