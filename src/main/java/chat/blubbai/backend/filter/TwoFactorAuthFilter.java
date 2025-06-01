package chat.blubbai.backend.filter;

import chat.blubbai.backend.model.AccessTokenDTO;
import chat.blubbai.backend.model.enums.ErrorResponse;
import chat.blubbai.backend.utils.TokenUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * TwoFactorAuthFilter
 *
 * This filter enforces two-factor authentication (2FA) for protected endpoints.
 * It checks the JWT for 2FA completion and secret method, and blocks requests if 2FA is required but not completed.
 *
 * <h2>Behavior:</h2>
 * <ul>
 *     <li>Skips endpoints containing "/no2fa" or "/noa".</li>
 *     <li>Checks for a valid JWT in the Authorization header.</li>
 *     <li>If 2FA is required but not completed, responds with 403 Forbidden and a JSON message.</li>
 *     <li>If 2FA method is not set, responds with 400 Bad Request and a JSON message.</li>
 *     <li>Otherwise, allows the request to proceed.</li>
 * </ul>
 */
public class TwoFactorAuthFilter extends OncePerRequestFilter {
    /**
     * Checks for 2FA requirements and blocks requests if not fulfilled.
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException If an error occurs during request processing.
     * @throws IOException If an I/O error occurs during request processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        // 2FA-Setup und -Verifizierung ausnehmen
        if (uri.contains("/no2fa") || uri.contains("/noa")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            AccessTokenDTO token = new AccessTokenDTO(authHeader.substring(7));
            if (TokenUtility.validateToken(token)) {
                String secretMethod = TokenUtility.getSecretMethod(token);
                boolean is2FACompleted = Boolean.TRUE.equals(TokenUtility.get2FACompleted(token));
                if (secretMethod == null && !is2FACompleted) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error_code\": \"" + ErrorResponse.TWO_FACTOR_REQUIRED.getValue() + "\", \"message\": \""+ ErrorResponse.TWO_FACTOR_REQUIRED.getMessage() + "\"} }");
                    return;
                }else if (secretMethod == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error_code\": \"" + ErrorResponse.METHOD_NOT_SET.getValue() + "\", \"message\": \"" + ErrorResponse.METHOD_NOT_SET.getMessage() + "\"}");
                    return;
                }
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        filterChain.doFilter(request, response);
    }
}
